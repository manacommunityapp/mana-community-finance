package com.manacommunity.api.finance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "reconciliation_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationRecord {

    public enum ReconciliationStatus {
        MATCHED,
        MISMATCHED,
        MISSING_INTERNAL,
        MISSING_GATEWAY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "transaction_ref", nullable = false, length = 100)
    private String transactionRef;

    @Column(name = "gateway_amount", precision = 12, scale = 2)
    private BigDecimal gatewayAmount;

    @Column(name = "internal_amount", precision = 12, scale = 2)
    private BigDecimal internalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReconciliationStatus status;

    @Column(name = "discrepancy_reason", length = 255)
    private String discrepancyReason;
}
