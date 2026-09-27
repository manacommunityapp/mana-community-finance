package com.manacommunity.api.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvanceBalanceResponse {
    private Long id;
    private Long communityId;
    private String flatNumber;
    private String tower;
    private BigDecimal balanceAmount;
    private BigDecimal totalDeposited;
    private BigDecimal totalUtilized;
    private LocalDateTime lastUtilizedAt;
}
