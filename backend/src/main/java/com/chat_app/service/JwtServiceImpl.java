package com.chat_app.service;

import com.chat_app.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.*;

@Service
public class JwtServiceImpl implements JwtService {
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @Override
    public String extractUserId(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(SIGNER_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    @Override
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("scope",  user.getRole().getName());
        return generateToken(claims, user);
    }

    @Override
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("scope",  user.getRole().getName());
        return generateRefreshToken(claims, user);
    }

    private String generateToken(Map<String, Object> claims, User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setClaims(claims)
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(60 * 60 * 1000 * VALID_DURATION)))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    private String generateRefreshToken(Map<String, Object> claims, User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setClaims(claims)
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(60 * 60 * 1000 * 7 * REFRESHABLE_DURATION)))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean verifyToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(SIGNER_KEY)
                    .build()
                    .parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SIGNER_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}