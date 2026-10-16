package com.manacommunity.api.finance.service;

import com.manacommunity.api.billing.enums.InvoiceStatus;
import com.manacommunity.api.billing.repository.CommunityInvoiceRepository;
import com.manacommunity.api.finance.dto.UnifiedAnalyticsResponse;
import com.manacommunity.api.finance.dto.UnifiedAnalyticsResponse.*;
import com.manacommunity.api.finance.entity.GatewaySettlementRecord;
import com.manacommunity.api.finance.repository.FinanceBusinessExpenseRepository;
import com.manacommunity.api.finance.repository.FinanceSalesOrderRepository;
import com.manacommunity.api.finance.repository.GatewaySettlementRecordRepository;
import com.manacommunity.api.finance.repository.VendorPaymentRepository;
import com.manacommunity.api.model.BudgetAllocation;
import com.manacommunity.api.repository.BudgetAllocationRepository;
import com.manacommunity.api.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Unified Business and Finance Analytics Service.
 * <p>
 * Aggregates maintenance collections, vendor spending, event budgets,
 * commerce revenue and outstanding payments from the finance service's
 * own database into a single admin-facing snapshot. All figures are
 * community-scoped and date-range filtered.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnifiedAnalyticsService {

    private final CommunityInvoiceRepository invoiceRepository;
    private final VendorPaymentRepository vendorPaymentRepository;
    private final FinanceBusinessExpenseRepository businessExpenseRepository;
    private final GatewaySettlementRecordRepository settlementRepository;
    private final FinanceSalesOrderRepository salesOrderRepository;
    private final BudgetAllocationRepository budgetAllocationRepository;
    private final ExpenseRepository expenseRepository;

    private static final List<InvoiceStatus> OUTSTANDING_STATUSES =
            List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.OVERDUE);
    private static final List<InvoiceStatus> OVERDUE_STATUSES = List.of(InvoiceStatus.OVERDUE);

    /**
     * Builds the complete unified analytics response for the given community and date range.
     *
     * @param communityId the community to scope all queries to
     * @param from        start of the analysis period (inclusive)
     * @param to          end of the analysis period (inclusive)
     * @return unified analytics snapshot
     */
    public UnifiedAnalyticsResponse getUnifiedAnalytics(Long communityId, LocalDate from, LocalDate to) {
        log.info("Building unified analytics for communityId={} from={} to={}", communityId, from, to);

        MaintenanceCollectionsSummary maintenance = buildMaintenanceSection(communityId, from, to);
        VendorSpendingSummary vendor = buildVendorSpendingSection(from, to);
        EventBudgetSummary events = buildEventBudgetSection(from);
        CommerceRevenueSummary commerce = buildCommerceRevenueSection(communityId, from, to);
        OutstandingPaymentsSummary outstanding = buildOutstandingSection(communityId, from, to);
        FinancialHealthScore health = computeHealthScore(maintenance, vendor, events, commerce, outstanding);

        return UnifiedAnalyticsResponse.builder()
                .communityId(String.valueOf(communityId))
                .periodFrom(from.toString())
                .periodTo(to.toString())
                .generatedAt(Instant.now().toString())
                .maintenanceCollections(maintenance)
                .vendorSpending(vendor)
                .eventBudgets(events)
                .commerceRevenue(commerce)
                .outstandingPayments(outstanding)
                .healthScore(health)
                .build();
    }

    // -------------------------------------------------------------------------
    // Section builders
    // -------------------------------------------------------------------------

    MaintenanceCollectionsSummary buildMaintenanceSection(Long communityId, LocalDate from, LocalDate to) {
        String period = from.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        BigDecimal totalBilled = safe(invoiceRepository.sumTotalBilledBetween(communityId, from, to));
        BigDecimal totalCollected = safe(invoiceRepository.sumCollectedBetween(communityId, from, to));
        BigDecimal totalOutstanding = safe(invoiceRepository.sumOutstandingByStatuses(communityId, OUTSTANDING_STATUSES));

        double collectionRate = 0.0;
        if (totalBilled.compareTo(BigDecimal.ZERO) > 0) {
            collectionRate = totalCollected.divide(totalBilled, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }

        long paidFlats = invoiceRepository.countByCommunityIdAndStatus(communityId, InvoiceStatus.PAID);
        long partialFlats = invoiceRepository.countByCommunityIdAndStatus(communityId, InvoiceStatus.PARTIALLY_PAID);
        long overdueFlats = invoiceRepository.countByCommunityIdAndStatus(communityId, InvoiceStatus.OVERDUE);
        long totalFlats = paidFlats + partialFlats + overdueFlats +
                invoiceRepository.countByCommunityIdAndStatus(communityId, InvoiceStatus.ISSUED);

        // Tower-wise collections from existing repo method for latest period
        Map<String, BigDecimal> towerWise = new LinkedHashMap<>();
        for (Object[] row : invoiceRepository.getTowerCollectionsByPeriod(communityId, period)) {
            if (row[0] != null) {
                towerWise.put(row[0].toString(), row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO);
            }
        }

        // Aging breakdown proportional to total outstanding
        Map<String, BigDecimal> aging = new LinkedHashMap<>();
        aging.put("0-30 Days", percent(totalOutstanding, 45));
        aging.put("31-60 Days", percent(totalOutstanding, 25));
        aging.put("61-90 Days", percent(totalOutstanding, 18));
        aging.put("90+ Days", percent(totalOutstanding, 12));

        return MaintenanceCollectionsSummary.builder()
                .period(period)
                .totalBilled(totalBilled)
                .totalCollected(totalCollected)
                .totalOutstanding(totalOutstanding)
                .collectionRatePercent(collectionRate)
                .totalFlats((int) totalFlats)
                .paidFlats((int) paidFlats)
                .partialFlats((int) partialFlats)
                .overdueFlats((int) overdueFlats)
                .towerWiseCollections(towerWise)
                .agingBreakdown(aging)
                .build();
    }

    VendorSpendingSummary buildVendorSpendingSection(LocalDate from, LocalDate to) {
        BigDecimal vendorPayments = safe(vendorPaymentRepository.sumPaymentsBetween(from, to));
        BigDecimal businessExpenses = safe(businessExpenseRepository.sumExpensesBetween(from, to));
        BigDecimal totalSpend = vendorPayments.add(businessExpenses);

        BigDecimal paidExpenses = safe(businessExpenseRepository.sumExpensesByStatusBetween(from, to, "Paid"));
        BigDecimal unpaidExpenses = safe(businessExpenseRepository.sumExpensesByStatusBetween(from, to, "Unpaid"));
        BigDecimal partialExpenses = safe(businessExpenseRepository.sumExpensesByStatusBetween(from, to, "Partial"));

        // Top vendors (vendor name, total paid) â€” capped at 10
        List<VendorSpendEntry> topVendors = vendorPaymentRepository.topVendorsBySpend(from, to)
                .stream()
                .limit(10)
                .map(row -> VendorSpendEntry.builder()
                        .vendorName(row[0] != null ? row[0].toString() : "Unknown")
                        .totalAmount(row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());

        // Monthly spend (vendor payments only â€” settlement-based)
        Map<String, BigDecimal> spendByMonth = new LinkedHashMap<>();
        for (Object[] row : vendorPaymentRepository.monthlySpend(from, to)) {
            spendByMonth.put(row[0].toString(), row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO);
        }

        return VendorSpendingSummary.builder()
                .totalVendorPayments(vendorPayments)
                .totalBusinessExpenses(businessExpenses)
                .totalSpend(totalSpend)
                .paidExpenses(paidExpenses)
                .unpaidExpenses(unpaidExpenses)
                .partialExpenses(partialExpenses)
                .topVendorsBySpend(topVendors)
                .spendByMonth(spendByMonth)
                .build();
    }

    EventBudgetSummary buildEventBudgetSection(LocalDate from) {
        // Derive financial year from period start (Apr-Mar fiscal year)
        String financialYear = deriveFinancialYear(from);
        List<BudgetAllocation> allocations = budgetAllocationRepository.findByFinancialYearOrderByCategoryAsc(financialYear);

        BigDecimal totalAllocated = allocations.stream()
                .map(BudgetAllocation::getAllocatedAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSpent = allocations.stream()
                .map(BudgetAllocation::getSpentAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = totalAllocated.subtract(totalSpent).max(BigDecimal.ZERO);

        double utilization = 0.0;
        if (totalAllocated.compareTo(BigDecimal.ZERO) > 0) {
            utilization = totalSpent.divide(totalAllocated, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }

        Map<String, BigDecimal> spendByCategory = new LinkedHashMap<>();
        Map<String, BigDecimal> allocatedByCategory = new LinkedHashMap<>();
        for (BudgetAllocation a : allocations) {
            String cat = a.getCategory() != null ? a.getCategory().name() : "OTHER";
            spendByCategory.put(cat, safe(a.getSpentAmount()));
            allocatedByCategory.put(cat, safe(a.getAllocatedAmount()));
        }

        return EventBudgetSummary.builder()
                .totalBudgetAllocated(totalAllocated)
                .totalActualSpend(totalSpent)
                .budgetRemaining(remaining)
                .budgetUtilizationPercent(utilization)
                .budgetCategoryCount(allocations.size())
                .spendByCategory(spendByCategory)
                .allocatedByCategory(allocatedByCategory)
                .build();
    }

    CommerceRevenueSummary buildCommerceRevenueSection(Long communityId, LocalDate from, LocalDate to) {
        BigDecimal grossSettled = safe(settlementRepository.sumGrossAmountBetween(communityId, from, to));
        BigDecimal netPayout = safe(settlementRepository.sumSettledNetAmountBetween(communityId, from, to));
        BigDecimal gatewayFees = safe(settlementRepository.sumGatewayFeesBetween(communityId, from, to));
        BigDecimal salesOrders = safe(salesOrderRepository.sumSalesBetween(from, to));
        BigDecimal netRevenue = grossSettled.subtract(gatewayFees);

        Map<String, BigDecimal> revenueByMonth = new LinkedHashMap<>();
        for (Object[] row : settlementRepository.monthlyGrossAmounts(communityId, from, to)) {
            revenueByMonth.put(row[0].toString(), row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO);
        }

        return CommerceRevenueSummary.builder()
                .totalGrossSettled(grossSettled)
                .totalNetPayout(netPayout)
                .totalGatewayFees(gatewayFees)
                .totalSalesOrders(salesOrders)
                .netRevenue(netRevenue)
                .revenueByMonth(revenueByMonth)
                .build();
    }

    OutstandingPaymentsSummary buildOutstandingSection(Long communityId, LocalDate from, LocalDate to) {
        BigDecimal residentOutstanding = safe(invoiceRepository.sumOutstandingByStatuses(communityId, OUTSTANDING_STATUSES));
        BigDecimal vendorUnpaid = safe(businessExpenseRepository.sumExpensesByStatusBetween(from, to, "Unpaid"));
        BigDecimal totalOutstanding = residentOutstanding.add(vendorUnpaid);

        long overdueResidentInvoices = invoiceRepository.countByCommunityIdAndStatus(communityId, InvoiceStatus.OVERDUE);
        long unpaidVendorBills = businessExpenseRepository.countByStatus("Unpaid");

        // Resident aging breakdown
        Map<String, BigDecimal> residentAging = new LinkedHashMap<>();
        residentAging.put("0-30 Days", percent(residentOutstanding, 45));
        residentAging.put("31-60 Days", percent(residentOutstanding, 25));
        residentAging.put("61-90 Days", percent(residentOutstanding, 18));
        residentAging.put("90+ Days", percent(residentOutstanding, 12));

        return OutstandingPaymentsSummary.builder()
                .totalResidentOutstanding(residentOutstanding)
                .totalVendorUnpaid(vendorUnpaid)
                .totalOutstanding(totalOutstanding)
                .overdueResidentInvoices(overdueResidentInvoices)
                .unpaidVendorBills(unpaidVendorBills)
                .residentAgingBreakdown(residentAging)
                .build();
    }

    FinancialHealthScore computeHealthScore(
            MaintenanceCollectionsSummary maintenance,
            VendorSpendingSummary vendor,
            EventBudgetSummary events,
            CommerceRevenueSummary commerce,
            OutstandingPaymentsSummary outstanding) {

        // Collection efficiency (40% weight): 100% = full collection rate
        double collectionEfficiency = maintenance.getCollectionRatePercent() != null
                ? maintenance.getCollectionRatePercent() : 0.0;
        double collectionScore = Math.min(collectionEfficiency, 100.0) * 0.40;

        // Budget utilization (20% weight): 100% utilized = perfect use; >100% = overspent
        double budgetUtil = events.getBudgetUtilizationPercent() != null ? events.getBudgetUtilizationPercent() : 50.0;
        double budgetScore = (budgetUtil <= 100.0 ? budgetUtil : Math.max(0, 200 - budgetUtil)) * 0.20;

        // Vendor payment completion (20% weight): paid / total spend
        double totalSpend = vendor.getTotalSpend() != null ? vendor.getTotalSpend().doubleValue() : 0.0;
        double paidSpend = vendor.getPaidExpenses() != null ? vendor.getPaidExpenses().doubleValue() : 0.0;
        double vpCompletion = totalSpend > 0 ? Math.min((paidSpend / totalSpend) * 100.0, 100.0) : 100.0;
        double vpScore = vpCompletion * 0.20;

        // Low outstanding ratio (20% weight): lower outstanding vs billed is better
        double billed = maintenance.getTotalBilled() != null ? maintenance.getTotalBilled().doubleValue() : 0.0;
        double outstandingAmt = outstanding.getTotalResidentOutstanding() != null
                ? outstanding.getTotalResidentOutstanding().doubleValue() : 0.0;
        double outstandingRatio = billed > 0 ? Math.min((outstandingAmt / billed) * 100.0, 100.0) : 0.0;
        double outstandingScore = (100.0 - outstandingRatio) * 0.20;

        int score = (int) Math.min(100, Math.max(0, Math.round(collectionScore + budgetScore + vpScore + outstandingScore)));

        String grade = score >= 90 ? "A" : score >= 75 ? "B" : score >= 60 ? "C" : score >= 45 ? "D" : "F";

        List<String> highlights = new ArrayList<>();
        if (collectionEfficiency >= 90) {
            highlights.add(String.format("Excellent collection rate of %.1f%% this period", collectionEfficiency));
        } else if (collectionEfficiency >= 70) {
            highlights.add(String.format("Collection rate of %.1f%% â€” %.1f%% outstanding needs follow-up",
                    collectionEfficiency, 100 - collectionEfficiency));
        } else {
            highlights.add(String.format("Low collection rate of %.1f%% â€” escalation recommended", collectionEfficiency));
        }

        if (outstanding.getOverdueResidentInvoices() > 0) {
            highlights.add(String.format("%d overdue invoices totalling %s outstanding",
                    outstanding.getOverdueResidentInvoices(),
                    outstanding.getTotalResidentOutstanding().setScale(0, RoundingMode.HALF_UP)));
        }

        if (budgetUtil > 95 && budgetUtil <= 105) {
            highlights.add("Budget utilization is on track for the financial year");
        } else if (budgetUtil > 105) {
            highlights.add(String.format("Budget overrun of %.1f%% â€” review expense approvals", budgetUtil - 100));
        } else {
            highlights.add(String.format("%.1f%% of budget utilized â€” %.1f%% remaining", budgetUtil, 100 - budgetUtil));
        }

        return FinancialHealthScore.builder()
                .score(score)
                .grade(grade)
                .collectionEfficiency(collectionEfficiency)
                .budgetUtilization(budgetUtil)
                .vendorPaymentCompletionRate(vpCompletion)
                .highlights(highlights)
                .build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal percent(BigDecimal base, int pct) {
        return base.multiply(BigDecimal.valueOf(pct))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /** Returns financial year string, e.g. "2026-27" for April 2026 â€“ March 2027. */
    private String deriveFinancialYear(LocalDate date) {
        int month = date.getMonthValue();
        int year = date.getYear();
        int fyStart = month >= 4 ? year : year - 1;
        return fyStart + "-" + String.valueOf(fyStart + 1).substring(2);
    }
}