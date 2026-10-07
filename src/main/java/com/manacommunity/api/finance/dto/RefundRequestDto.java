package com.manacommunity.api.finance.dto;

import com.manacommunity.api.finance.entity.CommunityRefund.RefundStatus;
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
public class RefundRequestDto {
    private Long communityId;
    private Long paymentId;
    private Long invoiceId;
    private Long residentId;
    private BigDecimal refundAmount;
    private String refundReason;
    private String refundMode;
}
