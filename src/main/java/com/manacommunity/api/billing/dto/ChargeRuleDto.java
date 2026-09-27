package com.manacommunity.api.billing.dto;

import com.manacommunity.api.billing.enums.ChargeComponentType;
import jakarta.validation.constraints.NotBlank;
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
public class ChargeRuleDto {
    private Long id;
    private Long planId;

    @NotNull(message = "Component type is required")
    private ChargeComponentType componentType;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Rate is required")
    private BigDecimal rate;

    private Boolean isMandatory;
    private String flatType;
    private String tower;
}
