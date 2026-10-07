package com.manacommunity.api.finance.dto;

import com.manacommunity.api.finance.entity.FinancialApprovalSignoff.SignoffDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitSignoffRequestDto {
    private Long approverId;
    private String approverName;
    private String approverRole;
    private SignoffDecision decision;
    private String comments;
}
