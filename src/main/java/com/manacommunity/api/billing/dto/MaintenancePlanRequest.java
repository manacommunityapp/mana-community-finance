package com.manacommunity.api.billing.dto;

import com.manacommunity.api.billing.enums.BillingCalculationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenancePlanRequest {
    @NotBlank(message = "Plan name is required")
    private String name;

    @NotNull(message = "Calculation type is required")
    private BillingCalculationType calculationType;

    private BigDecimal ratePerSqFt;
    private BigDecimal fixedAmountPerFlat;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean active;

    private List<ChargeRuleDto> chargeRules;
}
