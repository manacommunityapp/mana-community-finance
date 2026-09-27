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
    name = "flat_charge_assignments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"communityId", "flatNumber", "tower"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlatChargeAssignment {

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

    private BigDecimal areaSqFt; // e.g. 1200.00

    private String flatType;     // "2 BHK", "3 BHK"

    private Long planId;

    private Long ownerUserId;
    private String ownerName;

    private Long tenantUserId;
    private String tenantName;

    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
