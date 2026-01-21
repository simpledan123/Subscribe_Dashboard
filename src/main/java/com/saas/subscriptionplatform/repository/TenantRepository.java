package com.saas.subscriptionplatform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.subscriptionplatform.entity.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByEmail(String email);
}