package com.manacommunity.api.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBillingDashboardResponse {
    private String billingPeriod;
    private BigDecimal totalBilling;
    private BigDecimal totalCollected;
    private BigDecimal totalOutstanding;
    private Double collectionRatePercent;
    private int totalFlatsCount;
    private int paidFlatsCount;
    private int partialFlatsCount;
    private int outstandingFlatsCount;
    private int overdueFlatsCount;
    private Map<String, BigDecimal> towerWiseCollections;
    private Map<String, BigDecimal> agingBreakdown; // "0-30 Days", "31-60 Days", "61-90 Days", "90+ Days"
}
