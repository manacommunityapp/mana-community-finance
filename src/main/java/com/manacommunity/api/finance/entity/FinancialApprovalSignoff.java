package com.manacommunity.api.finance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "financial_approval_signoffs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialApprovalSignoff {

    public enum SignoffDecision {
        APPROVED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "approver_id", nullable = false)
    private Long approverId;

    @Column(name = "approver_name", length = 100)
    private String approverName;

    @Column(name = "approver_role", nullable = false, length = 50)
    private String approverRole; // e.g. TREASURER, PRESIDENT, SECRETARY, AUDITOR

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 20)
    private SignoffDecision decision;

    @Column(name = "comments", length = 500)
    private String comments;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;

    @PrePersist
    void onCreate() {
        if (signedAt == null) {
            signedAt = LocalDateTime.now();
        }
    }
}
