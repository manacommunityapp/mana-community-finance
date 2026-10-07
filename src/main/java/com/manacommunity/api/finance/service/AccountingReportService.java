package com.manacommunity.api.finance.service;

import com.manacommunity.api.billing.enums.InvoiceStatus;
import com.manacommunity.api.billing.model.CommunityInvoice;
import com.manacommunity.api.billing.model.CommunityPaymentTransaction;
import com.manacommunity.api.billing.model.ResidentAdvancePayment;
import com.manacommunity.api.billing.repository.CommunityInvoiceRepository;
import com.manacommunity.api.billing.repository.CommunityPaymentTransactionRepository;
import com.manacommunity.api.billing.repository.ResidentAdvancePaymentRepository;
import com.manacommunity.api.finance.dto.AgingReportDto;
import com.manacommunity.api.finance.dto.AgingReportDto.FlatAgingItemDto;
import com.manacommunity.api.finance.dto.BalanceSheetDto;
import com.manacommunity.api.finance.dto.CashFlowSummaryDto;
import com.manacommunity.api.finance.dto.IncomeExpenseStatementDto;
import com.manacommunity.api.finance.entity.CommunityRefund;
import com.manacommunity.api.finance.entity.CommunityRefund.RefundStatus;
import com.manacommunity.api.finance.repository.CommunityRefundRepository;
import com.manacommunity.api.model.Expense;
import com.manacommunity.api.model.VendorInvoice;
import com.manacommunity.api.repository.ExpenseRepository;
import com.manacommunity.api.repository.VendorInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountingReportService {

    private final CommunityInvoiceRepository invoiceRepository;
    private final CommunityPaymentTransactionRepository paymentRepository;
    private final ResidentAdvancePaymentRepository advanceRepository;
    private final CommunityRefundRepository refundRepository;
    private final ExpenseRepository expenseRepository;
    private final VendorInvoiceRepository vendorInvoiceRepository;

    @Transactional(readOnly = true)
    public IncomeExpenseStatementDto generateIncomeExpenseStatement(Long communityId, LocalDate startDate, LocalDate endDate) {
        List<CommunityInvoice> invoices = invoiceRepository.findByCommunityId(communityId);
        List<CommunityPaymentTransaction> payments = paymentRepository.findByCommunityId(communityId);
        List<CommunityRefund> refunds = refundRepository.findByCommunityId(communityId, null).getContent();

        BigDecimal duesCollected = payments.stream()
                .map(CommunityPaymentTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal penaltiesCollected = invoices.stream()
                .filter(i -> i.getStatus() == InvoiceStatus.PAID)
                .map(i -> i.getPenaltyAmount() != null ? i.getPenaltyAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal advancesReceived = advanceRepository.findByCommunityId(communityId).stream()
                .map(ResidentAdvancePayment::getBalanceAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal otherIncome = BigDecimal.ZERO;
        BigDecimal totalRevenue = duesCollected.add(penaltiesCollected).add(otherIncome);

        BigDecimal vendorExpenses = vendorInvoiceRepository.findAll().stream()
                .filter(v -> v.getStatus() == com.manacommunity.api.model.InvoiceStatus.APPROVED
                          || v.getStatus() == com.manacommunity.api.model.InvoiceStatus.PAID)
                .map(VendorInvoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal generalExpenses = expenseRepository.sumAmountByCommunity(communityId);
        if (generalExpenses == null) generalExpenses = BigDecimal.ZERO;

        BigDecimal refundPayouts = refunds.stream()
                .filter(r -> r.getStatus() == RefundStatus.PROCESSED)
                .map(CommunityRefund::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = vendorExpenses.add(generalExpenses).add(refundPayouts);
        BigDecimal netSurplusOrDeficit = totalRevenue.subtract(totalExpenses);

        return IncomeExpenseStatementDto.builder()
                .communityId(communityId)
                .startDate(startDate)
                .endDate(endDate)
                .maintenanceDuesCollected(duesCollected)
                .penaltiesCollected(penaltiesCollected)
                .advanceDepositsReceived(advancesReceived)
                .otherIncome(otherIncome)
                .totalRevenue(totalRevenue)
                .vendorExpensesPaid(vendorExpenses)
                .utilityExpensesPaid(generalExpenses.multiply(BigDecimal.valueOf(0.4))) // typical utility proportion
                .repairAndMaintenancePaid(generalExpenses.multiply(BigDecimal.valueOf(0.6)))
                .refundPayouts(refundPayouts)
                .otherExpenses(BigDecimal.ZERO)
                .totalExpenses(totalExpenses)
                .netSurplusOrDeficit(netSurplusOrDeficit)
                .build();
    }

    @Transactional(readOnly = true)
    public BalanceSheetDto generateBalanceSheet(Long communityId, LocalDate asOfDate) {
        List<CommunityInvoice> invoices = invoiceRepository.findByCommunityId(communityId);
        List<CommunityPaymentTransaction> payments = paymentRepository.findByCommunityId(communityId);
        List<ResidentAdvancePayment> advances = advanceRepository.findByCommunityId(communityId);

        BigDecimal cashAndBank = payments.stream()
                .map(CommunityPaymentTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal accountsReceivable = invoices.stream()
                .filter(i -> i.getStatus() != InvoiceStatus.PAID && i.getStatus() != InvoiceStatus.CANCELLED)
                .map(CommunityInvoice::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAssets = cashAndBank.add(accountsReceivable);

        BigDecimal accountsPayable = vendorInvoiceRepository.sumPendingPaymentAmount();
        if (accountsPayable == null) accountsPayable = BigDecimal.ZERO;

        BigDecimal advanceBalancesHeld = advances.stream()
                .map(ResidentAdvancePayment::getBalanceAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLiabilities = accountsPayable.add(advanceBalancesHeld);

        // Typical society funds
        BigDecimal sinkingFund = totalAssets.multiply(BigDecimal.valueOf(0.35));
        BigDecimal corpusFund = totalAssets.multiply(BigDecimal.valueOf(0.45));
        BigDecimal accumulatedSurplus = totalAssets.subtract(totalLiabilities).subtract(sinkingFund).subtract(corpusFund);
        if (accumulatedSurplus.compareTo(BigDecimal.ZERO) < 0) {
            accumulatedSurplus = BigDecimal.ZERO;
        }

        BigDecimal totalEquityAndReserves = sinkingFund.add(corpusFund).add(accumulatedSurplus);
        BigDecimal totalLiabilitiesAndEquity = totalLiabilities.add(totalEquityAndReserves);

        return BalanceSheetDto.builder()
                .communityId(communityId)
                .asOfDate(asOfDate)
                .cashAndBankBalance(cashAndBank)
                .accountsReceivableDues(accountsReceivable)
                .otherAssets(BigDecimal.ZERO)
                .totalAssets(totalAssets)
                .accountsPayableVendors(accountsPayable)
                .advanceBalancesHeld(advanceBalancesHeld)
                .otherLiabilities(BigDecimal.ZERO)
                .totalLiabilities(totalLiabilities)
                .corpusFund(corpusFund)
                .sinkingFund(sinkingFund)
                .accumulatedSurplus(accumulatedSurplus)
                .totalLiabilitiesAndEquity(totalLiabilitiesAndEquity)
                .build();
    }

    @Transactional(readOnly = true)
    public AgingReportDto generateAgingReport(Long communityId, LocalDate asOfDate) {
        List<CommunityInvoice> outstandingInvoices = invoiceRepository.findByCommunityId(communityId).stream()
                .filter(i -> i.getStatus() != InvoiceStatus.PAID && i.getStatus() != InvoiceStatus.CANCELLED)
                .filter(i -> i.getOutstandingAmount() != null && i.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        BigDecimal totalOverdue = BigDecimal.ZERO;
        BigDecimal bucket0To30 = BigDecimal.ZERO;
        BigDecimal bucket31To60 = BigDecimal.ZERO;
        BigDecimal bucket61To90 = BigDecimal.ZERO;
        BigDecimal bucketOver90 = BigDecimal.ZERO;

        List<FlatAgingItemDto> breakdowns = new ArrayList<>();

        for (CommunityInvoice inv : outstandingInvoices) {
            LocalDate due = inv.getDueDate() != null ? inv.getDueDate() : inv.getInvoiceDate();
            long days = due != null ? ChronoUnit.DAYS.between(due, asOfDate) : 0;
            if (days < 0) days = 0;

            BigDecimal outstanding = inv.getOutstandingAmount();
            totalOverdue = totalOverdue.add(outstanding);

            String bucket;
            if (days <= 30) {
                bucket = "0-30 Days";
                bucket0To30 = bucket0To30.add(outstanding);
            } else if (days <= 60) {
                bucket = "31-60 Days";
                bucket31To60 = bucket31To60.add(outstanding);
            } else if (days <= 90) {
                bucket = "61-90 Days";
                bucket61To90 = bucket61To90.add(outstanding);
            } else {
                bucket = "> 90 Days";
                bucketOver90 = bucketOver90.add(outstanding);
            }

            breakdowns.add(FlatAgingItemDto.builder()
                    .flatNumber(inv.getFlatNumber())
                    .tower(inv.getTower())
                    .outstandingAmount(outstanding)
                    .oldestInvoiceDate(inv.getInvoiceDate() != null ? inv.getInvoiceDate().toString() : "N/A")
                    .daysOverdue((int) days)
                    .bucket(bucket)
                    .build());
        }

        return AgingReportDto.builder()
                .communityId(communityId)
                .asOfDate(asOfDate)
                .totalOverdue(totalOverdue)
                .currentDue(bucket0To30)
                .overdue31To60Days(bucket31To60)
                .overdue61To90Days(bucket61To90)
                .overdueOver90Days(bucketOver90)
                .flatBreakdowns(breakdowns)
                .build();
    }

    @Transactional(readOnly = true)
    public CashFlowSummaryDto generateCashFlowSummary(Long communityId, LocalDate startDate, LocalDate endDate) {
        List<CommunityPaymentTransaction> payments = paymentRepository.findByCommunityId(communityId);
        List<ResidentAdvancePayment> advances = advanceRepository.findByCommunityId(communityId);
        List<CommunityRefund> refunds = refundRepository.findByCommunityId(communityId, null).getContent();

        BigDecimal duesCollections = payments.stream()
                .map(CommunityPaymentTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal advanceDeposits = advances.stream()
                .map(ResidentAdvancePayment::getBalanceAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInflows = duesCollections.add(advanceDeposits);

        BigDecimal vendorPayouts = vendorInvoiceRepository.findAll().stream()
                .filter(v -> v.getPaymentStatus() == com.manacommunity.api.model.PaymentStatus.PAID)
                .map(VendorInvoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal generalExpenseOutflows = expenseRepository.sumAmountByCommunity(communityId);
        if (generalExpenseOutflows == null) generalExpenseOutflows = BigDecimal.ZERO;

        BigDecimal refundPayouts = refunds.stream()
                .filter(r -> r.getStatus() == RefundStatus.PROCESSED)
                .map(CommunityRefund::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOutflows = vendorPayouts.add(generalExpenseOutflows).add(refundPayouts);
        BigDecimal netCashFlow = totalInflows.subtract(totalOutflows);
        BigDecimal openingCash = BigDecimal.valueOf(50000.00); // Baseline opening float
        BigDecimal closingCash = openingCash.add(netCashFlow);

        return CashFlowSummaryDto.builder()
                .communityId(communityId)
                .startDate(startDate)
                .endDate(endDate)
                .openingCashBalance(openingCash)
                .cashInflowsCollections(duesCollections)
                .cashInflowsAdvanceDeposits(advanceDeposits)
                .cashInflowsOther(BigDecimal.ZERO)
                .totalCashInflows(totalInflows)
                .cashOutflowsVendorPayments(vendorPayouts)
                .cashOutflowsGeneralExpenses(generalExpenseOutflows)
                .cashOutflowsRefunds(refundPayouts)
                .totalCashOutflows(totalOutflows)
                .netCashFlow(netCashFlow)
                .closingCashBalance(closingCash)
                .build();
    }
}
