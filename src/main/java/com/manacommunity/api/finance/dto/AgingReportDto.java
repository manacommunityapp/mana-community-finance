package com.manacommunity.api.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgingReportDto {
    private Long communityId;
    private LocalDate asOfDate;
    private BigDecimal totalOverdue;
    private BigDecimal currentDue;           // 0 - 30 days
    private BigDecimal overdue31To60Days;    // 31 - 60 days
    private BigDecimal overdue61To90Days;    // 61 - 90 days
    private BigDecimal overdueOver90Days;    // > 90 days
    private List<FlatAgingItemDto> flatBreakdowns;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlatAgingItemDto {
        private String flatNumber;
        private String tower;
        private BigDecimal outstandingAmount;
        private String oldestInvoiceDate;
        private Integer daysOverdue;
        private String bucket;
    }
}
