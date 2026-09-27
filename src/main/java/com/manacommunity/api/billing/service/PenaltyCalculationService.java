package com.manacommunity.api.billing.service;

import com.manacommunity.api.billing.dto.PenaltyRuleDto;
import com.manacommunity.api.billing.enums.InvoiceStatus;
import com.manacommunity.api.billing.enums.PenaltyType;
import com.manacommunity.api.billing.model.CommunityInvoice;
import com.manacommunity.api.billing.model.PenaltyRuleConfig;
import com.manacommunity.api.billing.repository.CommunityInvoiceRepository;
import com.manacommunity.api.billing.repository.PenaltyRuleConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PenaltyCalculationService {

    private final PenaltyRuleConfigRepository penaltyRuleConfigRepository;
    private final CommunityInvoiceRepository invoiceRepository;

    public PenaltyRuleDto getPenaltyRule(Long communityId) {
        return penaltyRuleConfigRepository.findByCommunityIdAndActiveTrue(communityId)
                .map(this::mapDto)
                .orElse(PenaltyRuleDto.builder()
                        .communityId(communityId)
                        .penaltyType(PenaltyType.FIXED_AMOUNT)
                        .gracePeriodDays(10)
                        .penaltyRateOrAmount(new BigDecimal("100.00"))
                        .active(true)
                        .build());
    }

    @Transactional
    public PenaltyRuleDto configurePenaltyRule(Long communityId, PenaltyRuleDto req) {
        PenaltyRuleConfig config = penaltyRuleConfigRepository.findByCommunityIdAndActiveTrue(communityId)
                .orElse(PenaltyRuleConfig.builder().communityId(communityId).build());

        config.setPenaltyType(req.getPenaltyType());
        config.setGracePeriodDays(req.getGracePeriodDays() != null ? req.getGracePeriodDays() : 10);
        config.setPenaltyRateOrAmount(req.getPenaltyRateOrAmount() != null ? req.getPenaltyRateOrAmount() : new BigDecimal("100.00"));
        config.setActive(req.getActive() != null ? req.getActive() : true);

        return mapDto(penaltyRuleConfigRepository.save(config));
    }

    @Transactional
    public void scanAndApplyPenalties(Long communityId) {
        LocalDate today = LocalDate.now();
        List<CommunityInvoice> overdueInvoices = invoiceRepository.findByCommunityIdAndStatusAndDueDateBefore(
                communityId, InvoiceStatus.ISSUED, today);

        PenaltyRuleConfig rule = penaltyRuleConfigRepository.findByCommunityIdAndActiveTrue(communityId)
                .orElse(null);

        if (rule == null || !Boolean.TRUE.equals(rule.getActive())) {
            return;
        }

        for (CommunityInvoice inv : overdueInvoices) {
            long daysOverdue = ChronoUnit.DAYS.between(inv.getDueDate(), today);
            if (daysOverdue > rule.getGracePeriodDays()) {
                BigDecimal penalty = BigDecimal.ZERO;
                if (rule.getPenaltyType() == PenaltyType.FIXED_AMOUNT) {
                    penalty = rule.getPenaltyRateOrAmount();
                } else if (rule.getPenaltyType() == PenaltyType.MONTHLY_PERCENTAGE) {
                    // e.g. 2% on outstanding
                    penalty = inv.getOutstandingAmount().multiply(rule.getPenaltyRateOrAmount())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                } else if (rule.getPenaltyType() == PenaltyType.DAILY_RATE) {
                    penalty = rule.getPenaltyRateOrAmount().multiply(BigDecimal.valueOf(daysOverdue));
                }

                inv.setPenaltyAmount(penalty);
                inv.setTotalAmount(inv.getSubtotal().add(penalty).subtract(inv.getDiscountAmount()).add(inv.getAdjustmentAmount()));
                inv.setOutstandingAmount(inv.getTotalAmount().subtract(inv.getPaidAmount()));
                inv.setStatus(InvoiceStatus.OVERDUE);
                invoiceRepository.save(inv);
                log.info("Applied penalty of ₹{} to overdue invoice {}", penalty, inv.getInvoiceNumber());
            }
        }
    }

    private PenaltyRuleDto mapDto(PenaltyRuleConfig c) {
        return PenaltyRuleDto.builder()
                .id(c.getId())
                .communityId(c.getCommunityId())
                .penaltyType(c.getPenaltyType())
                .gracePeriodDays(c.getGracePeriodDays())
                .penaltyRateOrAmount(c.getPenaltyRateOrAmount())
                .active(c.getActive())
                .build();
    }
}
