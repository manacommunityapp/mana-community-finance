package com.manacommunity.api.billing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatChargeAssignmentRequest {
    private Long flatId;

    @NotBlank(message = "Flat number is required")
    private String flatNumber;

    @NotBlank(message = "Tower is required")
    private String tower;

    private BigDecimal areaSqFt;
    private String flatType;
    private Long planId;
    private Long ownerUserId;
    private String ownerName;
    private Long tenantUserId;
    private String tenantName;
}
