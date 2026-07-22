package com.saas.subscriptionplatform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_accounts")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ServiceAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Column(nullable = false)
    private String nickname;

    @Email @NotBlank @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private String purpose = "PERSONAL"; // PERSONAL, STUDENT_BENEFIT, AI, JOB_SEARCH

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }
}
