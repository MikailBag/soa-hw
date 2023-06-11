package com.example.demo.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class TokenService {
    public static class VerificationException extends Exception {
        VerificationException(Throwable cause) {
            super(cause);
        }
    }

    private final Key key;
    private final int ttl;

    @Autowired
    TokenService(
            @Value("${jwt.key}") String key,
            @Value("${jwt.ttl-secs:3600}") int ttl
    ) {

        if (key.isEmpty()) {
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        } else {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            this.key = Keys.hmacShaKeyFor(keyBytes);

        }
        this.ttl = ttl;
    }

    public String makeToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(Date.from(Instant.now().plus(ttl, ChronoUnit.SECONDS)))
                .signWith(key)
                .compact();
    }

    public String verify(String token) throws VerificationException {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException ex) {
            throw new VerificationException(ex);
        }
    }
}
