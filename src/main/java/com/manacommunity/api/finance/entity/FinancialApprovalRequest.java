package com.manacommunity.api.finance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_approval_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialApprovalRequest {

    public enum ApprovalEntityType {
        VENDOR_INVOICE,
        EXPENSE,
        REFUND,
        BUDGET_TRANSFER
    }

    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "community_id", nullable = false)
    private Long communityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 40)
    private ApprovalEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "requested_by", nullable = false)
    private Long requestedBy;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "required_signoffs", nullable = false)
    @Builder.Default
    private Integer requiredSignoffs = 2; // Makers-checkers quorum (e.g., Treasurer + President)

    @Column(name = "current_signoffs", nullable = false)
    @Builder.Default
    private Integer currentSignoffs = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
