package com.manacommunity.api.billing.ledger.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.manacommunity.api.billing.ledger.enums.EntryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "general_ledger_entries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralLedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private GeneralLedgerTransaction transaction;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private GeneralLedgerAccount account;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", length = 16, nullable = false)
    private EntryType entryType;

    @Column(name = "amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "notes", length = 256)
    private String notes;
}
