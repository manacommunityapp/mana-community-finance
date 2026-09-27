package com.manacommunity.api.billing.model;

import com.manacommunity.api.billing.enums.BillingCalculationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(schema = "manacommunity", name = "maintenance_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenancePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long communityId;

    @Column(nullable = false)
    private String name; // e.g. "Standard 2026 Maintenance Plan"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCalculationType calculationType;

    private BigDecimal ratePerSqFt; // e.g. 4.50

    private BigDecimal fixedAmountPerFlat; // e.g. 4000.00

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
