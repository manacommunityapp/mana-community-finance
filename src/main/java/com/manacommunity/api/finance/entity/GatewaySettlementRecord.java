package com.manacommunity.api.finance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "gateway_settlement_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewaySettlementRecord {

    public enum PayoutStatus {
        PENDING,
        SETTLED,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "community_id", nullable = false)
    private Long communityId;

    @Column(name = "gateway_name", nullable = false, length = 50)
    private String gatewayName;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "gross_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "gateway_fee", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal gatewayFee = BigDecimal.ZERO;

    @Column(name = "gst_on_fee", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal gstOnFee = BigDecimal.ZERO;

    @Column(name = "net_payout_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal netPayoutAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_status", nullable = false, length = 30)
    @Builder.Default
    private PayoutStatus payoutStatus = PayoutStatus.PENDING;

    @Column(name = "utr_number", length = 100)
    private String utrNumber;

    @Column(name = "bank_account_ref", length = 100)
    private String bankAccountRef;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
