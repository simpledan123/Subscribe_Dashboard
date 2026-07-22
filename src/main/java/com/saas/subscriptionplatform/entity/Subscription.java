package com.saas.subscriptionplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_account_id", nullable = false)
    private ServiceAccount account;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_plan_id", nullable = false)
    private ServicePlan servicePlan;
    
    @Column(nullable = false)
    private String billingCycle; // MONTHLY, YEARLY, FREE
    
    @Column(nullable = false)
    private String status; // ACTIVE, CANCELLED, EXPIRED, PENDING
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private LocalDate nextBillingDate;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private Boolean autoRenew = true;

    @Column(nullable = false)
    @Builder.Default
    private String benefitType = "PAID"; // PAID, FREE_TRIAL, STUDENT, FAMILY
    
    private LocalDateTime cancelledAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
