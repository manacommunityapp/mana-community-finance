package com.manacommunity.api.billing.ledger.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "general_ledger_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralLedgerTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "community_id", nullable = false)
    private Long communityId;

    @Column(name = "transaction_ref", length = 64, nullable = false, unique = true)
    private String transactionRef;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "reference_type", length = 32, nullable = false)
    private String referenceType; // INVOICE, PAYMENT, PENALTY, CREDIT_NOTE, DEBIT_NOTE, ADVANCE

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "narration", length = 512)
    private String narration;

    @Column(name = "total_amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GeneralLedgerEntry> entries = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
