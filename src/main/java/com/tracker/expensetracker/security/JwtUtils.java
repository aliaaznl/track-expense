package com.tracker.expensetracker.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    // 1. THE SECRET KEY
    // IMPORTANT: In a real app, hide this in application.properties!
    // It must be at least 32 characters long for security.
    private static final String SECRET = "ExpenseTrackerSecretKeyForEncryptionSafety123456";

    // 2. EXPIRATION TIME (24 Hours in milliseconds)
    private static final long EXPIRATION_TIME = 86400000;

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // --- GENERATE TOKEN (When User Logs In) ---
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // --- EXTRACT USERNAME (Who owns this token?) ---
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // --- VALIDATE TOKEN (Is it fake or expired?) ---
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}