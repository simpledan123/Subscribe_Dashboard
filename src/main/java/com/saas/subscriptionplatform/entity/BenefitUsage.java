package com.saas.subscriptionplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "benefit_usages")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BenefitUsage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false, unique = true)
    private Subscription subscription;

    @Column(nullable = false)
    @Builder.Default
    private Long usedAmount = 0L;

    private Long limitAmount;
    private String unit;
    private LocalDate resetDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }
}
