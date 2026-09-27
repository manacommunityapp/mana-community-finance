package com.manacommunity.api.billing.dto;

import com.manacommunity.api.billing.enums.PenaltyType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PenaltyRuleDto {
    private Long id;
    private Long communityId;

    @NotNull(message = "Penalty type is required")
    private PenaltyType penaltyType;

    private Integer gracePeriodDays;
    private BigDecimal penaltyRateOrAmount;
    private Boolean active;
}
