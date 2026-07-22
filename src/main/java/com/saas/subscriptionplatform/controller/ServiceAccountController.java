package com.saas.subscriptionplatform.controller;

import com.saas.subscriptionplatform.entity.ServiceAccount;
import com.saas.subscriptionplatform.service.ServiceAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/accounts") @RequiredArgsConstructor
public class ServiceAccountController {
    private final ServiceAccountService service;
    @GetMapping public List<ServiceAccount> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public ServiceAccount findOne(@PathVariable Long id) { return service.findById(id); }
    @PostMapping public ServiceAccount create(@Valid @RequestBody ServiceAccount body) { return service.create(body); }
    @PutMapping("/{id}") public ServiceAccount update(@PathVariable Long id, @Valid @RequestBody ServiceAccount body) { return service.update(id, body); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
