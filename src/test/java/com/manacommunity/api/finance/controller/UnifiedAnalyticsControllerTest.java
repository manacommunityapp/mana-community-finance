package com.manacommunity.api.finance.controller;

import com.manacommunity.api.finance.dto.UnifiedAnalyticsResponse;
import com.manacommunity.api.finance.dto.UnifiedAnalyticsResponse.*;
import com.manacommunity.api.finance.service.UnifiedAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UnifiedAnalyticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UnifiedAnalyticsService analyticsService;

    @InjectMocks
    private UnifiedAnalyticsController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGetUnifiedAnalyticsSuccess() throws Exception {
        Long communityId = 1L;
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        UnifiedAnalyticsResponse mockResponse = UnifiedAnalyticsResponse.builder()
                .communityId("1")
                .periodFrom(from.toString())
                .periodTo(to.toString())
                .generatedAt("2026-10-31T12:00:00Z")
                .maintenanceCollections(MaintenanceCollectionsSummary.builder()
                        .totalBilled(new BigDecimal("500000.00"))
                        .totalCollected(new BigDecimal("450000.00"))
                        .totalOutstanding(new BigDecimal("50000.00"))
                        .collectionRatePercent(90.0)
                        .totalFlats(100)
                        .paidFlats(90)
                        .partialFlats(5)
                        .overdueFlats(5)
                        .towerWiseCollections(Map.of("Tower A", new BigDecimal("250000.00")))
                        .agingBreakdown(Map.of("0-30 Days", new BigDecimal("50000.00")))
                        .build())
                .vendorSpending(VendorSpendingSummary.builder()
                        .totalSpend(new BigDecimal("120000.00"))
                        .totalVendorPayments(new BigDecimal("80000.00"))
                        .totalBusinessExpenses(new BigDecimal("40000.00"))
                        .topVendorsBySpend(List.of(new VendorSpendEntry("CleanCo", new BigDecimal("30000.00"))))
                        .build())
                .eventBudgets(EventBudgetSummary.builder()
                        .totalBudgetAllocated(new BigDecimal("100000.00"))
                        .totalActualSpend(new BigDecimal("75000.00"))
                        .budgetRemaining(new BigDecimal("25000.00"))
                        .budgetUtilizationPercent(75.0)
                        .build())
                .commerceRevenue(CommerceRevenueSummary.builder()
                        .totalGrossSettled(new BigDecimal("60000.00"))
                        .netRevenue(new BigDecimal("58000.00"))
                        .build())
                .outstandingPayments(OutstandingPaymentsSummary.builder()
                        .totalResidentOutstanding(new BigDecimal("50000.00"))
                        .totalVendorUnpaid(new BigDecimal("15000.00"))
                        .totalOutstanding(new BigDecimal("65000.00"))
                        .overdueResidentInvoices(5)
                        .unpaidVendorBills(2)
                        .build())
                .healthScore(FinancialHealthScore.builder()
                        .score(88)
                        .grade("B")
                        .collectionEfficiency(90.0)
                        .highlights(List.of("Strong collections"))
                        .build())
                .build();

        when(analyticsService.getUnifiedAnalytics(eq(communityId), eq(from), eq(to)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/finance/analytics/unified")
                        .param("communityId", "1")
                        .param("from", "2026-01-01")
                        .param("to", "2026-10-31")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.communityId").value("1"))
                .andExpect(jsonPath("$.periodFrom").value("2026-01-01"))
                .andExpect(jsonPath("$.periodTo").value("2026-10-31"))
                .andExpect(jsonPath("$.maintenanceCollections.totalBilled").value(500000.00))
                .andExpect(jsonPath("$.maintenanceCollections.collectionRatePercent").value(90.0))
                .andExpect(jsonPath("$.vendorSpending.totalSpend").value(120000.00))
                .andExpect(jsonPath("$.eventBudgets.budgetUtilizationPercent").value(75.0))
                .andExpect(jsonPath("$.commerceRevenue.netRevenue").value(58000.00))
                .andExpect(jsonPath("$.outstandingPayments.totalOutstanding").value(65000.00))
                .andExpect(jsonPath("$.healthScore.score").value(88))
                .andExpect(jsonPath("$.healthScore.grade").value("B"));

        verify(analyticsService).getUnifiedAnalytics(eq(communityId), eq(from), eq(to));
    }

    @Test
    void testGetUnifiedAnalyticsDefaultDateRange() throws Exception {
        Long communityId = 1L;

        UnifiedAnalyticsResponse mockResponse = UnifiedAnalyticsResponse.builder()
                .communityId("1")
                .healthScore(FinancialHealthScore.builder().score(80).grade("B").build())
                .build();

        when(analyticsService.getUnifiedAnalytics(eq(communityId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/finance/analytics/unified")
                        .param("communityId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.communityId").value("1"));

        LocalDate expectedFrom = LocalDate.now().withDayOfMonth(1);
        LocalDate expectedTo = LocalDate.now();
        verify(analyticsService).getUnifiedAnalytics(eq(communityId), eq(expectedFrom), eq(expectedTo));
    }

    @Test
    void testGetUnifiedAnalyticsInvalidDateRangeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/finance/analytics/unified")
                        .param("communityId", "1")
                        .param("from", "2026-12-31")
                        .param("to", "2026-01-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetUnifiedAnalyticsMissingCommunityIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/finance/analytics/unified")
                        .param("from", "2026-01-01")
                        .param("to", "2026-10-31")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
