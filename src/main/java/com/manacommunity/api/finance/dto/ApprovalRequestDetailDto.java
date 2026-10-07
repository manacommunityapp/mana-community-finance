package com.manacommunity.api.finance.dto;

import com.manacommunity.api.finance.entity.FinancialApprovalRequest.ApprovalEntityType;
import com.manacommunity.api.finance.entity.FinancialApprovalRequest.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequestDetailDto {
    private Long id;
    private Long communityId;
    private ApprovalEntityType entityType;
    private Long entityId;
    private String title;
    private BigDecimal amount;
    private Long requestedBy;
    private String notes;
    private Integer requiredSignoffs;
    private Integer currentSignoffs;
    private ApprovalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime finalizedAt;
    private List<SignoffItemDto> signoffs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignoffItemDto {
        private Long id;
        private Long approverId;
        private String approverName;
        private String approverRole;
        private String decision;
        private String comments;
        private LocalDateTime signedAt;
    }
}
