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
public class FlatChargeAssignmentResponse {
    private Long id;
    private Long communityId;
    private Long flatId;
    private String flatNumber;
    private String tower;
    private BigDecimal areaSqFt;
    private String flatType;
    private Long planId;
    private String planName;
    private Long ownerUserId;
    private String ownerName;
    private Long tenantUserId;
    private String tenantName;
    private Boolean active;
    private LocalDateTime createdAt;
}
