package com.manacommunity.api.billing.model;

import com.manacommunity.api.billing.enums.ChargeComponentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(schema = "manacommunity", name = "community_invoice_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityInvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargeComponentType componentType;

    @Column(nullable = false)
    private String description;

    @Builder.Default
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(nullable = false)
    private BigDecimal rate;

    @Column(nullable = false)
    private BigDecimal amount;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
