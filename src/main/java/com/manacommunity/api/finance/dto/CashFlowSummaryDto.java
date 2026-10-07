package com.manacommunity.api.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowSummaryDto {
    private Long communityId;
    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal openingCashBalance;

    // Operating Inflows
    private BigDecimal cashInflowsCollections;
    private BigDecimal cashInflowsAdvanceDeposits;
    private BigDecimal cashInflowsOther;
    private BigDecimal totalCashInflows;

    // Operating Outflows
    private BigDecimal cashOutflowsVendorPayments;
    private BigDecimal cashOutflowsGeneralExpenses;
    private BigDecimal cashOutflowsRefunds;
    private BigDecimal totalCashOutflows;

    // Net Result
    private BigDecimal netCashFlow;
    private BigDecimal closingCashBalance;
}
