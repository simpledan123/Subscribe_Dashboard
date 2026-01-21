package com.saas.subscriptionplatform.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    
    private final SecretKey key = Keys.hmacShaKeyFor(
        "mySecretKeyForJwtTokenMustBe32BytesLong!".getBytes()
    );
    private final long EXPIRATION = 86400000; // 24시간
    
    public String generateToken(String username) {
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
            .signWith(key)
            .compact();
    }
    
    public String validateTokenAndGetUsername(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return claims.getSubject();
        } catch (JwtException e) {
            return null;
        }
    }
}