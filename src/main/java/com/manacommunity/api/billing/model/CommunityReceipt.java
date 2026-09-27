package com.manacommunity.api.billing.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(schema = "manacommunity", name = "community_receipts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long communityId;

    @Column(nullable = false, unique = true, length = 50)
    private String receiptNumber; // e.g. "REC-2026-009823"

    @Column(nullable = false)
    private Long invoiceId;

    private String invoiceNumber;

    @Column(nullable = false)
    private Long transactionId;

    @Column(nullable = false)
    private BigDecimal amountPaid;

    private String issuedToName;
    private String flatNumber;
    private String tower;

    private String receiptPdfUrl;

    @CreationTimestamp
    private LocalDateTime receiptDate;
}
