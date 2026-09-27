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
public class ReceiptDto {
    private Long id;
    private Long communityId;
    private String receiptNumber;
    private Long invoiceId;
    private String invoiceNumber;
    private Long transactionId;
    private BigDecimal amountPaid;
    private String issuedToName;
    private String flatNumber;
    private String tower;
    private String receiptPdfUrl;
    private LocalDateTime receiptDate;
}
