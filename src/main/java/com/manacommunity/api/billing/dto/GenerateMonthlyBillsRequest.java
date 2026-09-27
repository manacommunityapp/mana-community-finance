package com.manacommunity.api.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateMonthlyBillsRequest {
    @NotBlank(message = "Billing period is required (e.g. 2026-09)")
    private String billingPeriod; // "YYYY-MM"

    @NotNull(message = "Invoice date is required")
    private LocalDate invoiceDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private Long planId; // Optional: specify plan ID or use default active
    private String tower; // Optional: generate for specific tower or all
    private Boolean autoUtilizeAdvance; // Auto-deduct from resident advance balances
}
