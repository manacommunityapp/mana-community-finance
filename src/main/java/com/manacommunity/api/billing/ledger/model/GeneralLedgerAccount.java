package com.manacommunity.api.billing.ledger.model;

import com.manacommunity.api.billing.ledger.enums.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "general_ledger_accounts", uniqueConstraints = {
    @UniqueConstraint(name = "uq_community_account_code", columnNames = {"community_id", "account_code"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralLedgerAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "community_id", nullable = false)
    private Long communityId;

    @Column(name = "account_code", length = 32, nullable = false)
    private String accountCode;

    @Column(name = "account_name", length = 128, nullable = false)
    private String accountName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", length = 32, nullable = false)
    private AccountType accountType;

    @Column(name = "current_balance", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
