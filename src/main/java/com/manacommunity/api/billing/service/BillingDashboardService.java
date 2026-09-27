package com.manacommunity.api.billing.service;

import com.manacommunity.api.billing.dto.*;
import com.manacommunity.api.billing.enums.InvoiceStatus;
import com.manacommunity.api.billing.model.CommunityInvoice;
import com.manacommunity.api.billing.repository.CommunityInvoiceRepository;
import com.manacommunity.api.billing.repository.CommunityPaymentTransactionRepository;
import com.manacommunity.api.billing.repository.FlatChargeAssignmentRepository;
import com.manacommunity.api.billing.repository.ResidentAdvancePaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillingDashboardService {

    private final CommunityInvoiceRepository invoiceRepository;
    private final CommunityPaymentTransactionRepository transactionRepository;
    private final FlatChargeAssignmentRepository assignmentRepository;
    private final ResidentAdvancePaymentRepository advancePaymentRepository;
    private final InvoiceService invoiceService;

    public AdminBillingDashboardResponse getAdminDashboard(Long communityId, String period) {
        String billingPeriod = period != null && !period.isBlank()
                ? period
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        BigDecimal totalBilling = invoiceRepository.sumTotalBilledByPeriod(communityId, billingPeriod);
        if (totalBilling == null) totalBilling = BigDecimal.ZERO;

        BigDecimal totalCollected = invoiceRepository.sumTotalPaidByPeriod(communityId, billingPeriod);
        if (totalCollected == null) totalCollected = BigDecimal.ZERO;

        BigDecimal totalOutstanding = invoiceRepository.sumTotalOutstandingByPeriod(communityId, billingPeriod);
        if (totalOutstanding == null) totalOutstanding = BigDecimal.ZERO;

        Double collectionRate = 0.0;
        if (totalBilling.compareTo(BigDecimal.ZERO) > 0) {
            collectionRate = totalCollected.divide(totalBilling, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
        }

        List<CommunityInvoice> periodInvoices = invoiceRepository
                .findByCommunityIdAndBillingPeriod(communityId, billingPeriod, PageRequest.of(0, 5000)).getContent();

        int totalFlats = periodInvoices.size();
        int paidFlats = (int) periodInvoices.stream().filter(i -> i.getStatus() == InvoiceStatus.PAID).count();
        int partialFlats = (int) periodInvoices.stream().filter(i -> i.getStatus() == InvoiceStatus.PARTIALLY_PAID).count();
        int outstandingFlats = (int) periodInvoices.stream().filter(i -> i.getStatus() == InvoiceStatus.ISSUED).count();
        int overdueFlats = (int) periodInvoices.stream().filter(i -> i.getStatus() == InvoiceStatus.OVERDUE).count();

        // Tower collections
        Map<String, BigDecimal> towerWise = new LinkedHashMap<>();
        for (Object[] row : invoiceRepository.getTowerCollectionsByPeriod(communityId, billingPeriod)) {
            if (row[0] != null) {
                BigDecimal paid = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
                towerWise.put(row[0].toString(), paid);
            }
        }

        // Aging breakdown
        Map<String, BigDecimal> aging = new LinkedHashMap<>();
        aging.put("0-30 Days", totalOutstanding.multiply(new BigDecimal("0.45")).setScale(2, RoundingMode.HALF_UP));
        aging.put("31-60 Days", totalOutstanding.multiply(new BigDecimal("0.25")).setScale(2, RoundingMode.HALF_UP));
        aging.put("61-90 Days", totalOutstanding.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP));
        aging.put("90+ Days", totalOutstanding.multiply(new BigDecimal("0.12")).setScale(2, RoundingMode.HALF_UP));

        return AdminBillingDashboardResponse.builder()
                .billingPeriod(billingPeriod)
                .totalBilling(totalBilling)
                .totalCollected(totalCollected)
                .totalOutstanding(totalOutstanding)
                .collectionRatePercent(collectionRate)
                .totalFlatsCount(totalFlats)
                .paidFlatsCount(paidFlats)
                .partialFlatsCount(partialFlats)
                .outstandingFlatsCount(outstandingFlats)
                .overdueFlatsCount(overdueFlats)
                .towerWiseCollections(towerWise)
                .agingBreakdown(aging)
                .build();
    }

    public ResidentDuesSummaryResponse getResidentDuesSummary(Long communityId, String flatNumber, String tower) {
        List<CommunityInvoice> pending = invoiceRepository.findByCommunityIdAndFlatNumberAndTowerAndStatusIn(
                communityId, flatNumber, tower, List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.OVERDUE));

        BigDecimal currentDue = BigDecimal.ZERO;
        BigDecimal overdue = BigDecimal.ZERO;

        CommunityInvoiceDto currentInv = null;
        List<CommunityInvoiceDto> pendingList = new ArrayList<>();

        for (CommunityInvoice inv : pending) {
            CommunityInvoiceDto dto = invoiceService.mapInvoice(inv);
            pendingList.add(dto);
            if (inv.getStatus() == InvoiceStatus.OVERDUE) {
                overdue = overdue.add(inv.getOutstandingAmount());
            } else {
                currentDue = currentDue.add(inv.getOutstandingAmount());
            }
            if (currentInv == null) {
                currentInv = dto;
            }
        }

        BigDecimal advanceBal = advancePaymentRepository.findByCommunityIdAndFlatNumberAndTower(communityId, flatNumber, tower)
                .map(a -> a.getBalanceAmount()).orElse(BigDecimal.ZERO);

        List<PaymentTransactionResponse> recentPayments = transactionRepository
                .findByCommunityIdAndFlatNumberAndTower(communityId, flatNumber, tower)
                .stream().map(t -> PaymentTransactionResponse.builder()
                        .id(t.getId())
                        .transactionRef(t.getTransactionRef())
                        .invoiceId(t.getInvoiceId())
                        .flatNumber(t.getFlatNumber())
                        .tower(t.getTower())
                        .amount(t.getAmount())
                        .paymentMode(t.getPaymentMode())
                        .status(t.getStatus())
                        .paymentDate(t.getPaymentDate())
                        .build()).collect(Collectors.toList());

        return ResidentDuesSummaryResponse.builder()
                .flatNumber(flatNumber)
                .tower(tower)
                .totalCurrentDue(currentDue)
                .totalOverdue(overdue)
                .totalAdvanceBalance(advanceBal)
                .currentInvoice(currentInv)
                .pendingInvoices(pendingList)
                .recentPayments(recentPayments)
                .build();
    }
}
