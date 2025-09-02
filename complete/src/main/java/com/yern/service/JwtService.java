package com.yern.service;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class JwtService {
    @Value("${security.auth.signing_key}")
    private String secretKey;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.audiences}")
    private List<String> allowedAudiences;

    private final String INVALID_TOKEN_ERROR = "Invalid Token";
    private final int MINUTES = 60;

    public String generateToken(String email) {
        var now = Instant.now();

        return Jwts.builder()
                .subject(email)
                .issuer(issuerUri)
                .claim("aud", allowedAudiences)
                .signWith(getSecretKey())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(MINUTES, ChronoUnit.MINUTES)))
                .compact();
    }

    public String extractUsername(String token) throws AccessDeniedException {
        Claims claims = getTokenBody(token);
        return claims.getSubject();
    }

    public String extractIssuer(String token) throws AccessDeniedException {
        Claims claims = getTokenBody(token);
        return claims.getIssuer();
    }

    public Set<String> extractAudience(String token) throws AccessDeniedException {
        Claims claims = getTokenBody(token);
        return claims.getAudience();
    }

    public void validateToken(String token, UserDetails userDetails) throws AccessDeniedException {
        final String username = extractUsername(token);
        if (!username.equals(userDetails.getUsername()) || isTokenExpired(token)) {
            throw new AccessDeniedException(INVALID_TOKEN_ERROR);
        }

        final String issuer = extractIssuer(token);
        if (!issuer.equals(issuerUri)) {
            throw new AccessDeniedException(INVALID_TOKEN_ERROR);
        }

        final Set<String> audiences = extractAudience(token);
        audiences.retainAll(allowedAudiences);
        if (audiences.isEmpty()) {
            throw new AccessDeniedException(INVALID_TOKEN_ERROR);
        }
    }

    public Claims getTokenBody(String token) throws AccessDeniedException {
        try {
            return Jwts
                    .parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) { // Invalid signature or expired token
            throw new AccessDeniedException("Access denied: " + e.getMessage());
        }
    }

    private boolean isTokenExpired(String token) throws AccessDeniedException {
        Claims claims = getTokenBody(token);
        return claims.getExpiration().before(new Date());
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(this.secretKey)
        );
    }
}
