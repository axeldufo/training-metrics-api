package com.axel.trainingmetricsapi.controller.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration}")
    private long expiration;

    private JwtParser jwtParser;

    @PostConstruct
    public void init() {
        this.jwtParser = Jwts.parser()
            .verifyWith(getSigningKey())
            .build();
    }

    public String generateToken(long coachId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("coachId", coachId);
        return Jwts.builder()
            .claims().add(claims)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .and().signWith(getSigningKey()).compact();
    }

    public Long extractCoachId(String token) {
        Long coachId;
        try {
            coachId = jwtParser.parseSignedClaims(token).getPayload()
                .get("coachId", Long.class);
        } catch (JwtException e) {
            return null; // Token invalid or expired
        }
        return coachId;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
