package com.saas.subscriptionplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;
    
    @Column(nullable = false, unique = true)
    private String invoiceNumber; // INV-2025-0001
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private String status; // PENDING, PAID, OVERDUE, CANCELLED
    
    @Column(nullable = false)
    private LocalDateTime issueDate;
    
    @Column(nullable = false)
    private LocalDateTime dueDate;
    
    private LocalDateTime paidAt;
    
    private String pdfUrl; // S3에 저장된 청구서 PDF
    
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