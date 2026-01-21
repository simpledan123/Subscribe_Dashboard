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

import com.saas.subscriptionplatform.entity.Plan;
import com.saas.subscriptionplatform.service.PlanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {
    
    private final PlanService planService;
    
    @GetMapping
    public List<Plan> findAll() {
        return planService.findAll();
    }
    
    @GetMapping("/{id}")
    public Plan findById(@PathVariable Long id) {
        return planService.findById(id);
    }
    
    @PostMapping
    public Plan create(@RequestBody Plan plan) {
        return planService.create(plan);
    }
    
    @PutMapping("/{id}")
    public Plan update(@PathVariable Long id, @RequestBody Plan plan) {
        return planService.update(id, plan);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        planService.delete(id);
        return ResponseEntity.noContent().build();
    }
}