package com.saas.subscriptionplatform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_plans")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ServicePlan implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Column(nullable = false)
    private String serviceName;

    @NotBlank @Column(nullable = false)
    private String planName;

    private String category; // AI, PRODUCTIVITY, CAREER, DEVELOPMENT, ENTERTAINMENT
    private String description;
    private String homepageUrl;

    @NotNull @Column(nullable = false)
    @Builder.Default
    private BigDecimal monthlyPrice = BigDecimal.ZERO;

    @NotNull @Column(nullable = false)
    @Builder.Default
    private BigDecimal yearlyPrice = BigDecimal.ZERO;

    private Long usageLimit;
    private String usageUnit; // CREDITS, REQUESTS, GB, HOURS

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }
}
