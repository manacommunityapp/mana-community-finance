package com.manacommunity.api.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeExpenseStatementDto {
    private Long communityId;
    private LocalDate startDate;
    private LocalDate endDate;

    // Income components
    private BigDecimal maintenanceDuesCollected;
    private BigDecimal penaltiesCollected;
    private BigDecimal advanceDepositsReceived;
    private BigDecimal otherIncome;
    private BigDecimal totalRevenue;

    // Operating expense components
    private BigDecimal vendorExpensesPaid;
    private BigDecimal utilityExpensesPaid;
    private BigDecimal repairAndMaintenancePaid;
    private BigDecimal refundPayouts;
    private BigDecimal otherExpenses;
    private BigDecimal totalExpenses;

    // Net Result
    private BigDecimal netSurplusOrDeficit;
}
