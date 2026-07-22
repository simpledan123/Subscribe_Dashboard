package com.saas.subscriptionplatform.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.subscriptionplatform.entity.AppUser;
import com.saas.subscriptionplatform.repository.AppUserRepository;
import com.saas.subscriptionplatform.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Transactional
    public AppUser register(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.length() < 4) {
            throw new IllegalArgumentException("아이디와 4자 이상의 비밀번호를 입력해 주세요.");
        }
        if (appUserRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자입니다.");
        }
        
        AppUser user = AppUser.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .role("USER")
            .build();
        
        return appUserRepository.save(user);
    }
    
    public String login(String username, String password) {
        AppUser user = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호를 확인해 주세요."));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호를 확인해 주세요.");
        }
        
        return jwtUtil.generateToken(username);
    }
}
