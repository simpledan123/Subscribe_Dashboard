package com.saas.subscriptionplatform.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.subscriptionplatform.entity.Tenant;
import com.saas.subscriptionplatform.repository.TenantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantService {
    
    private final TenantRepository tenantRepository;
    
    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }
    
    public Tenant findById(Long id) {
        return tenantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tenant not found"));
    }
    
    @Transactional
    public Tenant create(Tenant tenant) {
        tenant.setStatus("ACTIVE");
        return tenantRepository.save(tenant);
    }
    
    @Transactional
    public Tenant update(Long id, Tenant updated) {
        Tenant tenant = findById(id);
        tenant.setCompanyName(updated.getCompanyName());
        tenant.setEmail(updated.getEmail());
        tenant.setPhone(updated.getPhone());
        return tenantRepository.save(tenant);
    }
    
    @Transactional
    public void delete(Long id) {
        tenantRepository.deleteById(id);
    }
}