package com.manacommunity.api.billing.ledger.dto;

import com.manacommunity.api.billing.ledger.enums.EntryType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GeneralLedgerTransactionDto {
    private Long id;
    private Long communityId;
    private String transactionRef;
    private LocalDate transactionDate;
    private String referenceType;
    private Long referenceId;
    private String narration;
    private BigDecimal totalAmount;
    private List<EntryItemDto> entries;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class EntryItemDto {
        private Long id;
        private Long accountId;
        private String accountCode;
        private String accountName;
        private EntryType entryType;
        private BigDecimal amount;
        private String notes;
    }
}
