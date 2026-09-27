package com.manacommunity.api.billing.model;

import com.manacommunity.api.billing.enums.InvoiceStatus;
import com.manacommunity.api.billing.enums.ResponsiblePartyType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    schema = "manacommunity",
    name = "community_invoices",
    indexes = {
        @Index(name = "idx_comm_inv_num", columnList = "invoiceNumber"),
        @Index(name = "idx_comm_inv_flat", columnList = "communityId, flatNumber, tower"),
        @Index(name = "idx_comm_inv_period", columnList = "communityId, billingPeriod")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long communityId;

    @Column(nullable = false, unique = true, length = 50)
    private String invoiceNumber; // e.g. "INV-2026-09-001245"

    private Long flatId;

    @Column(nullable = false)
    private String flatNumber;

    @Column(nullable = false)
    private String tower;

    @Column(nullable = false, length = 20)
    private String billingPeriod; // "2026-09"

    @Column(nullable = false)
    private LocalDate invoiceDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal adjustmentAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal outstandingAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.ISSUED;

    private Long responsiblePartyId; // User ID

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ResponsiblePartyType responsiblePartyType = ResponsiblePartyType.OWNER;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    @Builder.Default
    private List<CommunityInvoiceItem> items = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
