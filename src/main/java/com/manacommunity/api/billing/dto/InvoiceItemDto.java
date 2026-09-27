package com.manacommunity.api.billing.dto;

import com.manacommunity.api.billing.enums.ChargeComponentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemDto {
    private Long id;
    private Long invoiceId;
    private ChargeComponentType componentType;
    private String description;
    private BigDecimal quantity;
    private BigDecimal rate;
    private BigDecimal amount;
}
