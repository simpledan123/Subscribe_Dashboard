package com.saas.subscriptionplatform.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.subscriptionplatform.entity.Tenant;
import com.saas.subscriptionplatform.service.TenantService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {
    
    private final TenantService tenantService;
    
    @GetMapping
    public List<Tenant> findAll() {
        return tenantService.findAll();
    }
    
    @GetMapping("/{id}")
    public Tenant findById(@PathVariable Long id) {
        return tenantService.findById(id);
    }
    
    @PostMapping
    public Tenant create(@RequestBody Tenant tenant) {
        return tenantService.create(tenant);
    }
    
    @PutMapping("/{id}")
    public Tenant update(@PathVariable Long id, @RequestBody Tenant tenant) {
        return tenantService.update(id, tenant);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tenantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}