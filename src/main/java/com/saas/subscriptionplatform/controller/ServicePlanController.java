package com.saas.subscriptionplatform.controller;

import com.saas.subscriptionplatform.entity.ServicePlan;
import com.saas.subscriptionplatform.service.ServicePlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/services") @RequiredArgsConstructor
public class ServicePlanController {
    private final ServicePlanService service;
    @GetMapping public List<ServicePlan> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public ServicePlan findOne(@PathVariable Long id) { return service.findById(id); }
    @PostMapping public ServicePlan create(@Valid @RequestBody ServicePlan body) { return service.create(body); }
    @PutMapping("/{id}") public ServicePlan update(@PathVariable Long id, @Valid @RequestBody ServicePlan body) { return service.update(id, body); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
