package com.manacommunity.api.billing.service;

import com.manacommunity.api.billing.dto.BillingCycleSummaryResponse;
import com.manacommunity.api.billing.dto.GenerateMonthlyBillsRequest;
import com.manacommunity.api.billing.enums.BillingCalculationType;
import com.manacommunity.api.billing.enums.ChargeComponentType;
import com.manacommunity.api.billing.enums.InvoiceStatus;
import com.manacommunity.api.billing.enums.ResponsiblePartyType;
import com.manacommunity.api.billing.model.*;
import com.manacommunity.api.billing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingCycleService {

    private final FlatChargeAssignmentRepository flatAssignmentRepository;
    private final MaintenancePlanRepository planRepository;
    private final MaintenanceChargeRuleRepository chargeRuleRepository;
    private final CommunityInvoiceRepository invoiceRepository;
    private final ResidentAdvancePaymentRepository advancePaymentRepository;

    @Transactional
    public BillingCycleSummaryResponse generateMonthlyBills(Long communityId, GenerateMonthlyBillsRequest req) {
        log.info("Starting batch invoice generation for community: {}, period: {}", communityId, req.getBillingPeriod());

        List<FlatChargeAssignment> flats = req.getTower() != null && !req.getTower().isBlank()
                ? flatAssignmentRepository.findByCommunityIdAndTower(communityId, req.getTower())
                : flatAssignmentRepository.findByCommunityIdAndActiveTrue(communityId);

        if (flats.isEmpty()) {
            throw new IllegalStateException("No flats assigned for billing in this community / tower.");
        }

        int count = 0;
        BigDecimal totalBilled = BigDecimal.ZERO;
        BigDecimal totalAdvanceUtilized = BigDecimal.ZERO;

        for (FlatChargeAssignment flat : flats) {
            // Check if already billed for this period
            Optional<CommunityInvoice> existing = invoiceRepository
                    .findByCommunityIdAndFlatNumberAndTowerAndBillingPeriod(
                            communityId, flat.getFlatNumber(), flat.getTower(), req.getBillingPeriod());

            if (existing.isPresent()) {
                continue; // Skip already generated invoices
            }

            // Find effective plan
            Long planId = flat.getPlanId() != null ? flat.getPlanId() : req.getPlanId();
            MaintenancePlan plan = planId != null
                    ? planRepository.findById(planId).orElse(null)
                    : planRepository.findFirstByCommunityIdAndActiveTrueOrderByCreatedAtDesc(communityId).orElse(null);

            if (plan == null) {
                // Fallback default fixed plan
                plan = MaintenancePlan.builder()
                        .name("Default Maintenance")
                        .calculationType(BillingCalculationType.FIXED_PER_FLAT)
                        .fixedAmountPerFlat(new BigDecimal("4000.00"))
                        .build();
            }

            // Calculate components
            List<CommunityInvoiceItem> items = new ArrayList<>();
            BigDecimal subtotal = BigDecimal.ZERO;

            // 1. Base maintenance charge
            BigDecimal baseRate = BigDecimal.ZERO;
            if (plan.getCalculationType() == BillingCalculationType.PER_SQUARE_FEET && flat.getAreaSqFt() != null && plan.getRatePerSqFt() != null) {
                baseRate = flat.getAreaSqFt().multiply(plan.getRatePerSqFt()).setScale(2, RoundingMode.HALF_UP);
                items.add(CommunityInvoiceItem.builder()
                        .componentType(ChargeComponentType.REGULAR_MAINTENANCE)
                        .description("Maintenance (" + flat.getAreaSqFt() + " sq.ft @ ₹" + plan.getRatePerSqFt() + "/sq.ft)")
                        .quantity(flat.getAreaSqFt())
                        .rate(plan.getRatePerSqFt())
                        .amount(baseRate)
                        .build());
            } else {
                baseRate = plan.getFixedAmountPerFlat() != null ? plan.getFixedAmountPerFlat() : new BigDecimal("4000.00");
                items.add(CommunityInvoiceItem.builder()
                        .componentType(ChargeComponentType.REGULAR_MAINTENANCE)
                        .description("Regular Monthly Maintenance")
                        .quantity(BigDecimal.ONE)
                        .rate(baseRate)
                        .amount(baseRate)
                        .build());
            }
            subtotal = subtotal.add(baseRate);

            // 2. Extra charge rules
            if (plan.getId() != null) {
                List<MaintenanceChargeRule> rules = chargeRuleRepository.findByPlanId(plan.getId());
                for (MaintenanceChargeRule rule : rules) {
                    if (rule.getFlatType() == null || rule.getFlatType().equalsIgnoreCase(flat.getFlatType())) {
                        if (rule.getTower() == null || rule.getTower().equalsIgnoreCase(flat.getTower())) {
                            items.add(CommunityInvoiceItem.builder()
                                    .componentType(rule.getComponentType())
                                    .description(rule.getDescription())
                                    .quantity(BigDecimal.ONE)
                                    .rate(rule.getRate())
                                    .amount(rule.getRate())
                                    .build());
                            subtotal = subtotal.add(rule.getRate());
                        }
                    }
                }
            }

            // Generate invoice number
            String invoiceNumber = "INV-" + req.getBillingPeriod().replace("-", "") + "-"
                    + flat.getTower().replace(" ", "") + "-" + flat.getFlatNumber();

            BigDecimal totalAmount = subtotal;
            BigDecimal paidAmount = BigDecimal.ZERO;
            BigDecimal outstanding = totalAmount;
            InvoiceStatus status = InvoiceStatus.ISSUED;

            // 3. Auto-utilize advance payment if enabled
            if (Boolean.TRUE.equals(req.getAutoUtilizeAdvance())) {
                Optional<ResidentAdvancePayment> advanceOpt = advancePaymentRepository
                        .findByCommunityIdAndFlatNumberAndTower(communityId, flat.getFlatNumber(), flat.getTower());

                if (advanceOpt.isPresent() && advanceOpt.get().getBalanceAmount().compareTo(BigDecimal.ZERO) > 0) {
                    ResidentAdvancePayment adv = advanceOpt.get();
                    BigDecimal utilize = adv.getBalanceAmount().min(totalAmount);
                    adv.setBalanceAmount(adv.getBalanceAmount().subtract(utilize));
                    adv.setTotalUtilized(adv.getTotalUtilized().add(utilize));
                    adv.setLastUtilizedAt(LocalDateTime.now());
                    advancePaymentRepository.save(adv);

                    paidAmount = paidAmount.add(utilize);
                    outstanding = totalAmount.subtract(paidAmount);
                    totalAdvanceUtilized = totalAdvanceUtilized.add(utilize);

                    if (outstanding.compareTo(BigDecimal.ZERO) == 0) {
                        status = InvoiceStatus.PAID;
                    } else {
                        status = InvoiceStatus.PARTIALLY_PAID;
                    }
                }
            }

            CommunityInvoice invoice = CommunityInvoice.builder()
                    .communityId(communityId)
                    .invoiceNumber(invoiceNumber)
                    .flatId(flat.getFlatId())
                    .flatNumber(flat.getFlatNumber())
                    .tower(flat.getTower())
                    .billingPeriod(req.getBillingPeriod())
                    .invoiceDate(req.getInvoiceDate())
                    .dueDate(req.getDueDate())
                    .subtotal(subtotal)
                    .penaltyAmount(BigDecimal.ZERO)
                    .discountAmount(BigDecimal.ZERO)
                    .adjustmentAmount(BigDecimal.ZERO)
                    .totalAmount(totalAmount)
                    .paidAmount(paidAmount)
                    .outstandingAmount(outstanding)
                    .status(status)
                    .responsiblePartyId(flat.getTenantUserId() != null ? flat.getTenantUserId() : flat.getOwnerUserId())
                    .responsiblePartyType(flat.getTenantUserId() != null ? ResponsiblePartyType.TENANT : ResponsiblePartyType.OWNER)
                    .items(items)
                    .build();

            invoiceRepository.save(invoice);
            count++;
            totalBilled = totalBilled.add(totalAmount);
        }

        BigDecimal netOutstanding = totalBilled.subtract(totalAdvanceUtilized);
        log.info("Batch generation complete: {} flats billed, Total: ₹{}, Advance utilized: ₹{}", count, totalBilled, totalAdvanceUtilized);

        return BillingCycleSummaryResponse.builder()
                .billingPeriod(req.getBillingPeriod())
                .totalFlatsBilled(count)
                .totalBilledAmount(totalBilled)
                .totalAdvanceUtilized(totalAdvanceUtilized)
                .netOutstandingAmount(netOutstanding)
                .status("COMPLETED")
                .build();
    }
}
