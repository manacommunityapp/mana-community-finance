package com.manacommunity.api.billing.ledger.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class TrialBalanceResponse {
    private Long communityId;
    private List<TrialBalanceItem> items;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private Boolean isBalanced;

    @Data
    @Builder
    public static class TrialBalanceItem {
        private String accountCode;
        private String accountName;
        private String accountType;
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
    }
}
