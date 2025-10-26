package com.barclays.challenge.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(String userId) {
        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
            .compact();
    }

    public String extractUserId(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(secret.getBytes())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
}