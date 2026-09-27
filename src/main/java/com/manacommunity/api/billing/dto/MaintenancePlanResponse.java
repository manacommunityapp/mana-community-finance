package com.manacommunity.api.billing.dto;

import com.manacommunity.api.billing.enums.BillingCalculationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenancePlanResponse {
    private Long id;
    private Long communityId;
    private String name;
    private BillingCalculationType calculationType;
    private BigDecimal ratePerSqFt;
    private BigDecimal fixedAmountPerFlat;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean active;
    private List<ChargeRuleDto> chargeRules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
