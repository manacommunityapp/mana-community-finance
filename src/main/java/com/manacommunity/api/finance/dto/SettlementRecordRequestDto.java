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
public class SettlementRecordRequestDto {
    private Long communityId;
    private String gatewayName;
    private LocalDate settlementDate;
    private BigDecimal grossAmount;
    private BigDecimal gatewayFee;
    private BigDecimal gstOnFee;
    private String utrNumber;
    private String bankAccountRef;
}
