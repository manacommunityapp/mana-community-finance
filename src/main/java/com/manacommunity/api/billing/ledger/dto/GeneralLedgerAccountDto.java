package com.manacommunity.api.billing.ledger.dto;

import com.manacommunity.api.billing.ledger.enums.AccountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GeneralLedgerAccountDto {
    private Long id;
    private Long communityId;
    private String accountCode;
    private String accountName;
    private AccountType accountType;
    private BigDecimal currentBalance;
    private String description;
    private Boolean isActive;
}
