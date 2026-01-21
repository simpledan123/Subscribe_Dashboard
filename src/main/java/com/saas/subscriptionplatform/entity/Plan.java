package com.saas.subscriptionplatform.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plans")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name; // Free, Pro, Enterprise
    
    private String description;
    
    @Column(nullable = false)
    private BigDecimal monthlyPrice;
    
    @Column(nullable = false)
    private BigDecimal yearlyPrice;
    
    @Column(nullable = false)
    private Integer maxApiCalls; // 월간 API 호출 제한
    
    @Column(nullable = false)
    private Integer maxStorage; // 저장공간 MB 단위
    
    @Column(nullable = false)
    private Integer maxUsers; // 최대 사용자 수
    
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