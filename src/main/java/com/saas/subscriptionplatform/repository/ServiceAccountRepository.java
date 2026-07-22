package com.saas.subscriptionplatform.repository;

import com.saas.subscriptionplatform.entity.ServiceAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ServiceAccountRepository extends JpaRepository<ServiceAccount, Long> {
    Optional<ServiceAccount> findByEmail(String email);
}
