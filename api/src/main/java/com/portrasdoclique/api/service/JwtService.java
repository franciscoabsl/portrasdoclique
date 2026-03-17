package com.portrasdoclique.api.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret:portrasdoclique-secret-key-must-be-at-least-256-bits-long}")
    private String secret;

    @Value("${jwt.expiration:3600000}")
    private Long expiration;

    public String gerarTokenAnonimo() {
        String subject = "anon_" + UUID.randomUUID().toString().substring(0, 8);
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        return Jwts.builder()
                .subject(subject)
                .claims(Map.of(
                        "tipo", "anonimo",
                        "gerado_em", System.currentTimeMillis()
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public String extrairSubject(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}