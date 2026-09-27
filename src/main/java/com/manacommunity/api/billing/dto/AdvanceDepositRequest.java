package com.manacommunity.api.billing.dto;

import com.manacommunity.api.billing.enums.PaymentMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvanceDepositRequest {
    @NotBlank(message = "Flat number is required")
    private String flatNumber;

    @NotBlank(message = "Tower is required")
    private String tower;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;

    private String transactionRef;
}
