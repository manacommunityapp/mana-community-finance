package com.manacommunity.api.billing.model;

import com.manacommunity.api.billing.enums.PenaltyType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(schema = "manacommunity", name = "penalty_rule_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenaltyRuleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long communityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PenaltyType penaltyType = PenaltyType.FIXED_AMOUNT;

    @Builder.Default
    private Integer gracePeriodDays = 10; // e.g. Due on 10th

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal penaltyRateOrAmount = new BigDecimal("100.00"); // ₹100 or 2% or ₹10/day

    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
