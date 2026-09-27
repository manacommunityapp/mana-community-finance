package com.manacommunity.api.billing.model;

import com.manacommunity.api.billing.enums.ChargeComponentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(schema = "manacommunity", name = "maintenance_charge_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceChargeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long planId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargeComponentType componentType; // WATER_CHARGES, PARKING, SINKING_FUND, etc.

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal rate; // e.g. 300.00 or per-unit rate

    @Builder.Default
    private Boolean isMandatory = true;

    private String flatType; // Optional: "1 BHK", "2 BHK", "3 BHK", "4 BHK" (null for all)
    private String tower;    // Optional: "Tower A" (null for all)

    @CreationTimestamp
    private LocalDateTime createdAt;
}
