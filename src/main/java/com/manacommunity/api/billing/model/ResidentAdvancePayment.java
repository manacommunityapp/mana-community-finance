package com.manacommunity.api.billing.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    schema = "manacommunity",
    name = "resident_advance_payments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"communityId", "flatNumber", "tower"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidentAdvancePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long communityId;

    private Long flatId;

    @Column(nullable = false)
    private String flatNumber;

    @Column(nullable = false)
    private String tower;

    private Long userId;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal balanceAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal totalDeposited = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal totalUtilized = BigDecimal.ZERO;

    private LocalDateTime lastUtilizedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
