package com.manacommunity.api.finance.dto;

import com.manacommunity.api.finance.entity.FinancialApprovalRequest.ApprovalEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApprovalRequestDto {
    private Long communityId;
    private ApprovalEntityType entityType;
    private Long entityId;
    private String title;
    private BigDecimal amount;
    private Long requestedBy;
    private String notes;
    private Integer requiredSignoffs;
}
