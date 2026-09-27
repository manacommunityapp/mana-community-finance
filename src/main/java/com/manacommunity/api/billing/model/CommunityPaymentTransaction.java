package com.manacommunity.api.billing.model;

import com.manacommunity.api.billing.enums.PaymentMode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(schema = "manacommunity", name = "community_payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityPaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long communityId;

    @Column(nullable = false, unique = true, length = 60)
    private String transactionRef; // e.g. "PAY-20260925-92837281"

    @Column(nullable = false)
    private Long invoiceId;

    private Long flatId;
    private String flatNumber;
    private String tower;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMode paymentMode;

    private Long payerUserId;
    private String payerName;

    @Builder.Default
    private String status = "SUCCESS"; // SUCCESS, FAILED, PENDING

    private String gatewayRef; // e.g. Razorpay payment_id or UPI UTR

    @CreationTimestamp
    private LocalDateTime paymentDate;
}
