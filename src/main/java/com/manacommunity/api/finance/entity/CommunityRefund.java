package com.manacommunity.api.finance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "community_refunds")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityRefund {

    public enum RefundStatus {
        REQUESTED,
        UNDER_REVIEW,
        APPROVED,
        PROCESSED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "community_id", nullable = false)
    private Long communityId;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "resident_id", nullable = false)
    private Long residentId;

    @Column(name = "refund_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    @Column(name = "refund_mode", length = 50)
    private String refundMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private RefundStatus status = RefundStatus.REQUESTED;

    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
