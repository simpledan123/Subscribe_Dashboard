package com.saas.subscriptionplatform.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.subscriptionplatform.entity.Admin;
import com.saas.subscriptionplatform.repository.AdminRepository;
import com.saas.subscriptionplatform.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Transactional
    public Admin register(String username, String password) {
        if (adminRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("이미 존재하는 사용자입니다.");
        }
        
        Admin admin = Admin.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .role("ADMIN")
            .build();
        
        return adminRepository.save(admin);
    }
    
    public String login(String username, String password) {
        Admin admin = adminRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        
        return jwtUtil.generateToken(username);
    }
}