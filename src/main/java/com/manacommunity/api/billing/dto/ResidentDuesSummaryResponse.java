package com.manacommunity.api.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResidentDuesSummaryResponse {
    private String flatNumber;
    private String tower;
    private BigDecimal totalCurrentDue;
    private BigDecimal totalOverdue;
    private BigDecimal totalAdvanceBalance;
    private CommunityInvoiceDto currentInvoice;
    private List<CommunityInvoiceDto> pendingInvoices;
    private List<PaymentTransactionResponse> recentPayments;
}
