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
public class BalanceSheetDto {
    private Long communityId;
    private LocalDate asOfDate;

    // Assets
    private BigDecimal cashAndBankBalance;
    private BigDecimal accountsReceivableDues;
    private BigDecimal otherAssets;
    private BigDecimal totalAssets;

    // Liabilities
    private BigDecimal accountsPayableVendors;
    private BigDecimal advanceBalancesHeld;
    private BigDecimal otherLiabilities;
    private BigDecimal totalLiabilities;

    // Reserves & Equity
    private BigDecimal corpusFund;
    private BigDecimal sinkingFund;
    private BigDecimal accumulatedSurplus;
    private BigDecimal totalLiabilitiesAndEquity;
}
