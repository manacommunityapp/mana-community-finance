package com.manacommunity.api.finance.dto;

import com.manacommunity.api.finance.entity.ReconciliationBatch.BatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationResultDto {
    private Long batchId;
    private Long communityId;
    private String gatewayName;
    private LocalDate statementDate;
    private Integer totalTransactions;
    private Integer matchedCount;
    private Integer mismatchedCount;
    private BatchStatus status;
    private LocalDateTime processedAt;
    private List<ReconciliationRecordDto> records;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReconciliationRecordDto {
        private String transactionRef;
        private java.math.BigDecimal gatewayAmount;
        private java.math.BigDecimal internalAmount;
        private String status;
        private String discrepancyReason;
    }
}
