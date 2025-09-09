package com.yern.service.security.authentication;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
@Getter
public class JwtService {
    private String tokenPrefix;
    private String secretKey;
    private String issuerUri;
    private List<String> allowedAudiences;
    private String invalidTokenError;
    private int expiresInMinutes;

    public JwtService(
        @Value("${security.auth.oauth2.token-prefix}") String tokenPrefix,
        @Value("${security.auth.signing-key}") String secretKey,
        @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
        @Value("${spring.security.oauth2.resourceserver.jwt.audiences}") List<String> allowedAudiences,
        @Value("${security.auth.oauth2.invalid-token-error}") String invalidTokenError,
        @Value("${security.auth.oauth2.expires-in-minutes}") int expiresInMinutes
    ) {
        this.tokenPrefix = tokenPrefix;
        this.secretKey = secretKey;
        this.issuerUri = issuerUri;
        this.allowedAudiences = allowedAudiences;
        this.invalidTokenError = invalidTokenError;
        this.expiresInMinutes = expiresInMinutes;
    }

    // TODO: validate the email?
    public String generateToken(String email) {
        var now = Instant.now();

        return Jwts.builder()
                .subject(email)
                .issuer(issuerUri)
                .claim("aud", allowedAudiences)
                .signWith(getSecretKey())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiresInMinutes, ChronoUnit.MINUTES)))
                .compact();
    }

    public void validateToken(String token, UserDetails userDetails) throws AccessDeniedException {
        final String username = extractUsername(token);
        if (!username.equals(userDetails.getUsername()) || isTokenExpired(token)) {
            throwInvalidTokenError();
        }

        final String issuer = extractIssuer(token);
        if (!issuer.equals(issuerUri)) {
            throwInvalidTokenError();
        }

        Set<String> audiences = extractAudience(token);
        audiences.retainAll(allowedAudiences);
        if (audiences.isEmpty()) {
            throwInvalidTokenError();
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
        } catch (Exception e) {
            throw new AccessDeniedException("Access denied: " + e.getMessage());
        }
    }

    public Optional<String> extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(tokenPrefix)) {
            return Optional.empty();
        }

        final String jwtToken = authHeader.substring(tokenPrefix.length());
        return Optional.of(jwtToken.strip());
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

    public Date extractIssuedAt(String token) throws AccessDeniedException {
        Claims claims = getTokenBody(token);
        return claims.getIssuedAt();
    }

    public Date extractExpiration(String token) throws AccessDeniedException {
        Claims claims = getTokenBody(token);
        return claims.getExpiration();
    }

    public boolean isTokenExpired(String token) throws AccessDeniedException {
        Claims claims = getTokenBody(token);
        return claims.getExpiration().before(new Date());
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(this.secretKey)
        );
    }

    public void throwInvalidTokenError() throws AccessDeniedException {
        throw new AccessDeniedException(invalidTokenError);
    }
}
