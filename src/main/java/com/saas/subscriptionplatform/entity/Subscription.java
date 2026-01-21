package com.saas.subscriptionplatform.entity;

import jakarta.persistence.*;
import lombok.*;
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
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;
    
    @Column(nullable = false)
    private String billingCycle; // MONTHLY, YEARLY
    
    @Column(nullable = false)
    private String status; // ACTIVE, CANCELLED, EXPIRED, PENDING
    
    @Column(nullable = false)
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private LocalDateTime nextBillingDate;
    
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