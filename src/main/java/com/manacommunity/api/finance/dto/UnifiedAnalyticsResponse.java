package com.manacommunity.api.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Unified Business and Finance Analytics response.
 * <p>
 * Consolidates maintenance collections, vendor spending, event budgets,
 * commerce revenue and outstanding payments into a single community-scoped
 * snapshot for community administrators.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedAnalyticsResponse {

    private String communityId;
    private String periodFrom;
    private String periodTo;
    private String generatedAt;

    private MaintenanceCollectionsSummary maintenanceCollections;
    private VendorSpendingSummary vendorSpending;
    private EventBudgetSummary eventBudgets;
    private CommerceRevenueSummary commerceRevenue;
    private OutstandingPaymentsSummary outstandingPayments;
    private FinancialHealthScore healthScore;

    // -------------------------------------------------------------------------
    // Section 1: Maintenance Collections (from CommunityInvoice)
    // -------------------------------------------------------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaintenanceCollectionsSummary {
        private String period;
        private BigDecimal totalBilled;
        private BigDecimal totalCollected;
        private BigDecimal totalOutstanding;
        private Double collectionRatePercent;
        private int totalFlats;
        private int paidFlats;
        private int partialFlats;
        private int overdueFlats;
        private Map<String, BigDecimal> towerWiseCollections;
        /** Outstanding bucketed: "0-30 Days", "31-60 Days", "61-90 Days", "90+ Days" */
        private Map<String, BigDecimal> agingBreakdown;
    }

    // -------------------------------------------------------------------------
    // Section 2: Vendor Spending (from VendorPayment + FinanceBusinessExpense)
    // -------------------------------------------------------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendorSpendingSummary {
        private BigDecimal totalVendorPayments;
        private BigDecimal totalBusinessExpenses;
        private BigDecimal totalSpend;
        private BigDecimal paidExpenses;
        private BigDecimal unpaidExpenses;
        private BigDecimal partialExpenses;
        private List<VendorSpendEntry> topVendorsBySpend;
        private Map<String, BigDecimal> spendByMonth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendorSpendEntry {
        private String vendorName;
        private BigDecimal totalAmount;
    }

    // -------------------------------------------------------------------------
    // Section 3: Event Budgets (from BudgetAllocation + ExpenseRepository)
    // -------------------------------------------------------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventBudgetSummary {
        private BigDecimal totalBudgetAllocated;
        private BigDecimal totalActualSpend;
        private BigDecimal budgetRemaining;
        private Double budgetUtilizationPercent;
        private int budgetCategoryCount;
        /** Spend mapped by ExpenseCategory name */
        private Map<String, BigDecimal> spendByCategory;
        /** Allocated mapped by ExpenseCategory name */
        private Map<String, BigDecimal> allocatedByCategory;
    }

    // -------------------------------------------------------------------------
    // Section 4: Commerce Revenue (from GatewaySettlementRecord + FinanceSalesOrder)
    // -------------------------------------------------------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommerceRevenueSummary {
        private BigDecimal totalGrossSettled;
        private BigDecimal totalNetPayout;
        private BigDecimal totalGatewayFees;
        private BigDecimal totalSalesOrders;
        private BigDecimal netRevenue;
        private Map<String, BigDecimal> revenueByMonth;
    }

    // -------------------------------------------------------------------------
    // Section 5: Outstanding Payments
    // -------------------------------------------------------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutstandingPaymentsSummary {
        private BigDecimal totalResidentOutstanding;
        private BigDecimal totalVendorUnpaid;
        private BigDecimal totalOutstanding;
        private long overdueResidentInvoices;
        private long unpaidVendorBills;
        private Map<String, BigDecimal> residentAgingBreakdown;
    }

    // -------------------------------------------------------------------------
    // Section 6: Financial Health Score (derived)
    // -------------------------------------------------------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialHealthScore {
        /** 0–100 composite score */
        private int score;
        /** A / B / C / D / F */
        private String grade;
        private Double collectionEfficiency;
        private Double budgetUtilization;
        private Double vendorPaymentCompletionRate;
        private List<String> highlights;
    }
}
