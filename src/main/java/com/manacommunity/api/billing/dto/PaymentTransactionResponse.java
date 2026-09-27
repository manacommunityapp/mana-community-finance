package com.manacommunity.api.billing.dto;

import com.manacommunity.api.billing.enums.PaymentMode;
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
public class PaymentTransactionResponse {
    private Long id;
    private Long communityId;
    private String transactionRef;
    private Long invoiceId;
    private Long flatId;
    private String flatNumber;
    private String tower;
    private BigDecimal amount;
    private PaymentMode paymentMode;
    private Long payerUserId;
    private String payerName;
    private String status;
    private String gatewayRef;
    private LocalDateTime paymentDate;
    private String receiptNumber;
}
