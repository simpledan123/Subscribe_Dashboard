package com.saas.subscriptionplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_records", uniqueConstraints =
    @UniqueConstraint(name = "uk_payment_subscription_date", columnNames = {"subscription_id", "scheduled_date"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status; // SCHEDULED, PAID, FAILED, SKIPPED

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    private LocalDateTime paidAt;
    private String paymentMethod;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }
}
