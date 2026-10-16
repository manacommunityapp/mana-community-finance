package com.manacommunity.api.finance.service;

import com.manacommunity.api.billing.enums.InvoiceStatus;
import com.manacommunity.api.billing.repository.CommunityInvoiceRepository;
import com.manacommunity.api.finance.dto.UnifiedAnalyticsResponse;
import com.manacommunity.api.finance.repository.FinanceBusinessExpenseRepository;
import com.manacommunity.api.finance.repository.FinanceSalesOrderRepository;
import com.manacommunity.api.finance.repository.GatewaySettlementRecordRepository;
import com.manacommunity.api.finance.repository.VendorPaymentRepository;
import com.manacommunity.api.model.BudgetAllocation;
import com.manacommunity.api.repository.BudgetAllocationRepository;
import com.manacommunity.api.repository.ExpenseRepository;
import com.manacommunity.common.enums.ExpenseCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UnifiedAnalyticsServiceTest {

    @Mock private CommunityInvoiceRepository invoiceRepository;
    @Mock private VendorPaymentRepository vendorPaymentRepository;
    @Mock private FinanceBusinessExpenseRepository businessExpenseRepository;
    @Mock private GatewaySettlementRecordRepository settlementRepository;
    @Mock private FinanceSalesOrderRepository salesOrderRepository;
    @Mock private BudgetAllocationRepository budgetAllocationRepository;
    @Mock private ExpenseRepository expenseRepository;

    @InjectMocks
    private UnifiedAnalyticsService analyticsService;

    private static final Long COMMUNITY_ID = 1L;
    private static final LocalDate FROM = LocalDate.of(2026, 10, 1);
    private static final LocalDate TO   = LocalDate.of(2026, 10, 31);

    @BeforeEach
    void setUp() {
        // ── Maintenance stubs ──────────────────────────────────────────────────
        lenient().when(invoiceRepository.sumTotalBilledBetween(eq(COMMUNITY_ID), any(), any()))
                .thenReturn(new BigDecimal("500000.00"));
        lenient().when(invoiceRepository.sumCollectedBetween(eq(COMMUNITY_ID), any(), any()))
                .thenReturn(new BigDecimal("420000.00"));
        lenient().when(invoiceRepository.sumOutstandingByStatuses(eq(COMMUNITY_ID), anyList()))
                .thenReturn(new BigDecimal("80000.00"));
        lenient().when(invoiceRepository.countByCommunityIdAndStatus(COMMUNITY_ID, InvoiceStatus.PAID)).thenReturn(180L);
        lenient().when(invoiceRepository.countByCommunityIdAndStatus(COMMUNITY_ID, InvoiceStatus.PARTIALLY_PAID)).thenReturn(15L);
        lenient().when(invoiceRepository.countByCommunityIdAndStatus(COMMUNITY_ID, InvoiceStatus.OVERDUE)).thenReturn(12L);
        lenient().when(invoiceRepository.countByCommunityIdAndStatus(COMMUNITY_ID, InvoiceStatus.ISSUED)).thenReturn(25L);
        lenient().when(invoiceRepository.getTowerCollectionsByPeriod(eq(COMMUNITY_ID), anyString()))
                .thenReturn(List.of(
                        new Object[]{"Tower A", new BigDecimal("200000.00"), new BigDecimal("220000.00")},
                        new Object[]{"Tower B", new BigDecimal("220000.00"), new BigDecimal("280000.00")}
                ));

        // ── Vendor spending stubs ──────────────────────────────────────────────
        lenient().when(vendorPaymentRepository.sumPaymentsBetween(any(), any()))
                .thenReturn(new BigDecimal("150000.00"));
        lenient().when(businessExpenseRepository.sumExpensesBetween(any(), any()))
                .thenReturn(new BigDecimal("75000.00"));
        lenient().when(businessExpenseRepository.sumExpensesByStatusBetween(any(), any(), eq("Paid")))
                .thenReturn(new BigDecimal("55000.00"));
        lenient().when(businessExpenseRepository.sumExpensesByStatusBetween(any(), any(), eq("Unpaid")))
                .thenReturn(new BigDecimal("15000.00"));
        lenient().when(businessExpenseRepository.sumExpensesByStatusBetween(any(), any(), eq("Partial")))
                .thenReturn(new BigDecimal("5000.00"));
        lenient().when(vendorPaymentRepository.topVendorsBySpend(any(), any()))
                .thenReturn(List.of(
                        new Object[]{"Vendor Alpha", new BigDecimal("80000.00")},
                        new Object[]{"Vendor Beta",  new BigDecimal("40000.00")}
                ));
        lenient().when(vendorPaymentRepository.monthlySpend(any(), any())).thenReturn(Collections.emptyList());

        // ── Event budget stubs ─────────────────────────────────────────────────
        BudgetAllocation ba1 = BudgetAllocation.builder()
                .financialYear("2026-27")
                .category(ExpenseCategory.EVENTS)
                .allocatedAmount(new BigDecimal("200000.00"))
                .spentAmount(new BigDecimal("170000.00"))
                .build();
        BudgetAllocation ba2 = BudgetAllocation.builder()
                .financialYear("2026-27")
                .category(ExpenseCategory.OpEx_Maintenance)
                .allocatedAmount(new BigDecimal("300000.00"))
                .spentAmount(new BigDecimal("250000.00"))
                .build();
        lenient().when(budgetAllocationRepository.findByFinancialYearOrderByCategoryAsc(anyString()))
                .thenReturn(List.of(ba1, ba2));

        // ── Commerce revenue stubs ─────────────────────────────────────────────
        lenient().when(settlementRepository.sumGrossAmountBetween(eq(COMMUNITY_ID), any(), any()))
                .thenReturn(new BigDecimal("320000.00"));
        lenient().when(settlementRepository.sumSettledNetAmountBetween(eq(COMMUNITY_ID), any(), any()))
                .thenReturn(new BigDecimal("311200.00"));
        lenient().when(settlementRepository.sumGatewayFeesBetween(eq(COMMUNITY_ID), any(), any()))
                .thenReturn(new BigDecimal("8800.00"));
        lenient().when(salesOrderRepository.sumSalesBetween(any(), any()))
                .thenReturn(new BigDecimal("45000.00"));
        lenient().when(settlementRepository.monthlyGrossAmounts(eq(COMMUNITY_ID), any(), any()))
                .thenReturn(Collections.emptyList());

        // ── Outstanding stubs ──────────────────────────────────────────────────
        lenient().when(businessExpenseRepository.countByStatus("Unpaid")).thenReturn(8L);
    }

    // =========================================================================
    // Core response shape tests
    // =========================================================================

    @Test
    void testUnifiedAnalyticsReturnsAllSections() {
        UnifiedAnalyticsResponse response = analyticsService.getUnifiedAnalytics(COMMUNITY_ID, FROM, TO);

        assertNotNull(response);
        assertNotNull(response.getMaintenanceCollections(), "maintenanceCollections must be present");
        assertNotNull(response.getVendorSpending(), "vendorSpending must be present");
        assertNotNull(response.getEventBudgets(), "eventBudgets must be present");
        assertNotNull(response.getCommerceRevenue(), "commerceRevenue must be present");
        assertNotNull(response.getOutstandingPayments(), "outstandingPayments must be present");
        assertNotNull(response.getHealthScore(), "healthScore must be present");
    }

    @Test
    void testResponseMetadataIsPopulated() {
        UnifiedAnalyticsResponse response = analyticsService.getUnifiedAnalytics(COMMUNITY_ID, FROM, TO);

        assertEquals("1", response.getCommunityId());
        assertEquals("2026-10-01", response.getPeriodFrom());
        assertEquals("2026-10-31", response.getPeriodTo());
        assertNotNull(response.getGeneratedAt());
        assertFalse(response.getGeneratedAt().isBlank());
    }

    // =========================================================================
    // Maintenance Collections tests
    // =========================================================================

    @Test
    void testMaintenanceCollectionRateCalculated() {
        UnifiedAnalyticsResponse response = analyticsService.getUnifiedAnalytics(COMMUNITY_ID, FROM, TO);
        UnifiedAnalyticsResponse.MaintenanceCollectionsSummary m = response.getMaintenanceCollections();

        assertEquals(new BigDecimal("500000.00"), m.getTotalBilled());
        assertEquals(new BigDecimal("420000.00"), m.getTotalCollected());
        assertEquals(new BigDecimal("80000.00"), m.getTotalOutstanding());
        assertEquals(84.0, m.getCollectionRatePercent(), 0.1);
    }

    @Test
    void testMaintenanceFlatCountsAreCorrect() {
        UnifiedAnalyticsResponse response = analyticsService.getUnifiedAnalytics(COMMUNITY_ID, FROM, TO);
        UnifiedAnalyticsResponse.MaintenanceCollectionsSummary m = response.getMaintenanceCollections();

        assertEquals(180, m.getPaidFlats());
        assertEquals(15, m.getPartialFlats());
        assertEquals(12, m.getOverdueFlats());
        assertEquals(232, m.getTotalFlats()); // 180+15+12+25
    }

    @Test
    void testMaintenanceTowerWiseCollectionsPopulated() {
        UnifiedAnalyticsResponse response = analyticsService.getUnifiedAnalytics(COMMUNITY_ID, FROM, TO);
        UnifiedAnalyticsResponse.MaintenanceCollectionsSummary m = response.getMaintenanceCollections();

        assertFalse(m.getTowerWiseCollections().isEmpty());
        assertTrue(m.getTowerWiseCollections().containsKey("Tower A"));
        assertTrue(m.getTowerWiseCollections().containsKey("Tower B"));
    }

    @Test
    void testMaintenanceAgingBreakdownSumsToOutstanding() {
        UnifiedAnalyticsResponse response = analyticsService.getUnifiedAnalytics(COMMUNITY_ID, FROM, TO);
        UnifiedAnalyticsResponse.MaintenanceCollectionsSummary m = response.getMaintenanceCollections();

        BigDecimal agingSum = m.getAgingBreakdown().values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Should sum to 100% of outstanding (45+25+18+12 = 100%)
        assertTrue(agingSum.compareTo(m.getTotalOutstanding()) <= 0);
        assertFalse(m.getAgingBreakdown().isEmpty());
        assertEquals(4, m.getAgingBreakdown().size());
    }

    // =========================================================================
    // Vendor Spending tests
    // =========================================================================

    @Test
    void testVendorSpendingAggregatesCorrectly() {
        UnifiedAnalyticsResponse response = analyticsService.getUnifiedAnalytics(COMMUNITY_ID, FROM, TO);
        UnifiedAnalyticsResponse.VendorSpendingSummary v = response.getVendorSpending();

        assertEquals(new BigDecimal("150000.00"), v.getTotalVendorPayments());
        assertEquals(new BigDecimal("75000.00"), v.getTotalBusinessExpenses());
        assertEquals(new BigDecimal("225000.00"), v.getTotalSpend());
    }

    @Test
    void testTopVendorsListIsPopulated() {
        UnifiedAnalyticsResponse response = analyticsService.getUnifiedAnalytics(COMMUNITY_ID, FROM, TO);
        UnifiedAnalyticsResponse.VendorSpendingSummary v = response.getVendorSpending();

        assertFalse(v.getTopVendorsBySpend().isEmpty());
        assertEquals("Vendor Alpha", v.getTopVendorsBySpend().get(0).getVendorName());
        assertEquals(new BigDecimal("80000.00"), v.getTopVendorsBySpend().get(0).getTotalAmount());
    }

    // =========================================================================
    // Event Budget tests
    // =========================================================================

    @Test
    void testEventBudgetUtilizationCalculated() {
        UnifiedAnalyticsResponse response = analyticsService.getUnifiedAnalytics(COMMUNITY_ID, FROM, TO);
        UnifiedAnalyticsResponse.EventBudgetSummary e = response.getEventBudgets();

        assertEquals(new BigDecimal("500000.00"), e.getTotalBudgetAllocated());
        assertEquals(new BigDecimal("420000.00"), e.getTotalActualSpend());
        assertNotNull(e.getBudgetUtilizationPercent());
        assertEquals(84.0, e.getBudgetUtilizationPercent(), 0.1);
        assertEquals(2, e.getBudgetCategoryCount());
    }

    @Test
    void testEventBudgetRemainingIsNonNegative() {
        UnifiedAnalyticsResponse response = analyticsService.getUnifiedAnalytics(COMMUNITY_ID, FROM, TO);
        UnifiedAnalyticsResponse.EventBudgetSummary e = response.getEventBudgets();

        assertTrue(e.getBudgetRemaining().compareTo(BigDecimal.ZERO) >= 0);
    }

    // =========================================================================
    // Commerce Revenue tests
    // =========================================================================

    @Test
    void testCommerceRevenueNetCalculation() {
        UnifiedAnalyticsResponse response = analyticsService.getUnifiedAnalytics(COMMUNITY_ID, FROM, TO);
        UnifiedAnalyticsResponse.CommerceRevenueSummary c = response.getCommerceRevenue();

        assertEquals(new BigDecimal("320000.00"), c.getTotalGrossSettled());
        assertEquals(new BigDecimal("8800.00"), c.getTotalGatewayFees());
        // netRevenue = grossSettled - fees
        assertEquals(new BigDecimal("311200.00"), c.getNetRevenue());
    }

    // =========================================================================
    // Outstanding Payments tests
    // =========================================================================

    @Test
    void testOutstandingPaymentsNonNegative() {
        UnifiedAnalyticsResponse response = analyticsService.getUnifiedAnalytics(COMMUNITY_ID, FROM, TO);
        UnifiedAnalyticsResponse.OutstandingPaymentsSummary o = response.getOutstandingPayments();

        assertTrue(o.getTotalResidentOutstanding().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(o.getTotalVendorUnpaid().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(o.getTotalOutstanding().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void testOutstandingTotalIsSum() {
        UnifiedAnalyticsResponse response = analyticsService.getUnifiedAnalytics(COMMUNITY_ID, FROM, TO);
        UnifiedAnalyticsResponse.OutstandingPaymentsSummary o = response.getOutstandingPayments();

        BigDecimal expected = o.getTotalResidentOutstanding().add(o.getTotalVendorUnpaid());
        assertEquals(0, expected.compareTo(o.getTotalOutstanding()));
    }

    // =========================================================================
    // Financial Health Score tests
    // =========================================================================

    @Test
    void testHealthScoreInValidRange() {
        UnifiedAnalyticsResponse response = analyticsService.getUnifiedAnalytics(COMMUNITY_ID, FROM, TO);
        UnifiedAnalyticsResponse.FinancialHealthScore h = response.getHealthScore();

        assertNotNull(h);
        assertTrue(h.getScore() >= 0 && h.getScore() <= 100,
                "Score should be 0-100, was: " + h.getScore());
        assertTrue(List.of("A", "B", "C", "D", "F").contains(h.getGrade()),
                "Grade should be A/B/C/D/F, was: " + h.getGrade());
    }

    @Test
    void testHealthScoreHighlightsPopulated() {
        UnifiedAnalyticsResponse response = analyticsService.getUnifiedAnalytics(COMMUNITY_ID, FROM, TO);
        UnifiedAnalyticsResponse.FinancialHealthScore h = response.getHealthScore();

        assertNotNull(h.getHighlights());
        assertFalse(h.getHighlights().isEmpty());
    }

    @Test
    void testDefaultDateRangeIsCurrentMonth() {
        LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();

        // Should not throw — service should handle current-month defaults
        UnifiedAnalyticsResponse response = analyticsService.getUnifiedAnalytics(COMMUNITY_ID, firstOfMonth, today);
        assertNotNull(response);
        assertEquals(firstOfMonth.toString(), response.getPeriodFrom());
        assertEquals(today.toString(), response.getPeriodTo());
    }

    @Test
    void testNullRepositoryValuesAreHandledGracefully() {
        // Override mocks to return null (simulates empty DB)
        org.mockito.Mockito.lenient().when(invoiceRepository.sumTotalBilledBetween(any(), any(), any())).thenReturn(null);
        org.mockito.Mockito.lenient().when(invoiceRepository.sumCollectedBetween(any(), any(), any())).thenReturn(null);
        org.mockito.Mockito.lenient().when(invoiceRepository.sumOutstandingByStatuses(any(), anyList())).thenReturn(null);
        org.mockito.Mockito.lenient().when(vendorPaymentRepository.sumPaymentsBetween(any(), any())).thenReturn(null);
        org.mockito.Mockito.lenient().when(businessExpenseRepository.sumExpensesBetween(any(), any())).thenReturn(null);
        org.mockito.Mockito.lenient().when(settlementRepository.sumGrossAmountBetween(any(), any(), any())).thenReturn(null);
        org.mockito.Mockito.lenient().when(salesOrderRepository.sumSalesBetween(any(), any())).thenReturn(null);

        // Should not throw NullPointerException
        assertDoesNotThrow(() -> analyticsService.getUnifiedAnalytics(COMMUNITY_ID, FROM, TO));

        UnifiedAnalyticsResponse response = analyticsService.getUnifiedAnalytics(COMMUNITY_ID, FROM, TO);
        assertEquals(BigDecimal.ZERO, response.getMaintenanceCollections().getTotalBilled());
        assertEquals(BigDecimal.ZERO, response.getVendorSpending().getTotalSpend());
        assertEquals(BigDecimal.ZERO, response.getCommerceRevenue().getTotalGrossSettled());
    }
}
