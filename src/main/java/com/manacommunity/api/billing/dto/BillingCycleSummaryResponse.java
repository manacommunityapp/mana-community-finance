package com.manacommunity.api.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingCycleSummaryResponse {
    private String billingPeriod;
    private int totalFlatsBilled;
    private BigDecimal totalBilledAmount;
    private BigDecimal totalAdvanceUtilized;
    private BigDecimal netOutstandingAmount;
    private String status;
}
