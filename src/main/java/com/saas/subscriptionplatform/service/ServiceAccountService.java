package com.saas.subscriptionplatform.service;

import com.saas.subscriptionplatform.entity.ServiceAccount;
import com.saas.subscriptionplatform.exception.ResourceNotFoundException;
import com.saas.subscriptionplatform.repository.ServiceAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class ServiceAccountService {
    private final ServiceAccountRepository repository;

    public List<ServiceAccount> findAll() { return repository.findAll(); }

    @Cacheable(value = "serviceAccounts", key = "#id")
    public ServiceAccount findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("계정을 찾을 수 없습니다."));
    }

    @Transactional
    public ServiceAccount create(ServiceAccount account) {
        repository.findByEmail(account.getEmail()).ifPresent(it -> {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        });
        account.setStatus("ACTIVE");
        return repository.save(account);
    }

    @Transactional @CacheEvict(value = "serviceAccounts", key = "#id")
    public ServiceAccount update(Long id, ServiceAccount input) {
        ServiceAccount saved = findById(id);
        saved.setNickname(input.getNickname());
        saved.setEmail(input.getEmail());
        saved.setPurpose(input.getPurpose());
        saved.setMemo(input.getMemo());
        saved.setStatus(input.getStatus() == null ? saved.getStatus() : input.getStatus());
        return repository.save(saved);
    }

    @Transactional @CacheEvict(value = "serviceAccounts", key = "#id")
    public void delete(Long id) { repository.delete(findById(id)); }
}
