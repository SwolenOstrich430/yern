package com.yern.service.security.authentication;

import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = JwtServiceTests.class)
public class JwtServiceTests {
    @Value("${security.auth.oauth2.token-prefix}")
    private String tokenPrefix;
    @Value("${security.auth.signing-key}")
    private String secretKey;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;
    @Value("${spring.security.oauth2.resourceserver.jwt.audiences}")
    private List<String> allowedAudiences;
    @Value("${security.auth.oauth2.invalid-token-error}")
    private String invalidTokenError;
    @Value("${security.auth.oauth2.expires-in-minutes}")
    private int expiresInMinutes;

    private JwtService jwtService;
    // TODO: something better than this
    private final String email = "blah@google.com";
    private String mockToken;
    private final UserDetails userDetails = mock(UserDetails.class);
    @Autowired
    private ServletRequest httpServletRequest;

    @BeforeEach
    public void setup() {
        this.jwtService = new JwtService(
            tokenPrefix,
            secretKey,
            issuerUri,
            allowedAudiences,
            invalidTokenError,
            expiresInMinutes
        );

        mockToken = jwtService.generateToken(email);
    }

    @Test
    public void generateToken_returnsAValidToken_whenEmailIsValid() throws AuthenticationException {
        String token = jwtService.generateToken(email);
        assertInstanceOf(String.class, token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void generateToken_setsSubjectClaim_whenEmailIsValid() throws AuthenticationException, AccessDeniedException {
        String token = jwtService.generateToken(email);
        String subject = jwtService.extractUsername(token);

        assertEquals(email, subject);
    }

    @Test
    public void generateToken_setsIssuerToIssuerUri_whenEmailIsValid() throws AuthenticationException, AccessDeniedException {
        String token = jwtService.generateToken(email);
        String foundIssuer = jwtService.extractIssuer(token);

        assertEquals(issuerUri, foundIssuer);
    }

    @Test
    public void generateToken_setsAudienceToAllowedAudiences_whenEmailIsValid() throws AuthenticationException, AccessDeniedException {
        String token = jwtService.generateToken(email);
        Set<String> foundAudiences = jwtService.extractAudience(token);

        assertEquals(foundAudiences, new HashSet<>(allowedAudiences));
    }

    @Test void generateToken_setsIssuedAtToCurrentTime_whenEmailIsValid() throws AuthenticationException, AccessDeniedException, InterruptedException {
        Date dateBefore = Date.from(Instant.now());
        Thread.sleep(1000);
        String token = jwtService.generateToken(email);
        Date issuedAt = jwtService.extractIssuedAt(token);

        assert(issuedAt.compareTo(dateBefore) >= 0);
    }

    @Test void generateToken_setsExpirationToCurrentTimestampPlusExpiresInMinutes_whenEmailIsValid() throws AuthenticationException, AccessDeniedException {
        Date dateBefore = Date.from(Instant.now());
        String token = jwtService.generateToken(email);

        Date issuedAt = jwtService.extractIssuedAt(token);
        Date expiresAt = jwtService.extractExpiration(token);
        Date expectedExpiresAt = Date.from(issuedAt.toInstant().plus(expiresInMinutes, ChronoUnit.MINUTES));

        assertTrue(expiresAt.after(dateBefore));
        assertTrue(expiresAt.after(issuedAt));
        assertEquals(expiresAt, expectedExpiresAt);
    }

    // TODO: extract methods

    @Test
    public void validateToken_throwsInvalidTokenError_whenTokenIsExpired() throws AuthenticationException, AccessDeniedException {
        JwtService spyJwtService = spy(jwtService);

        doReturn(true).when(spyJwtService).isTokenExpired(mockToken);
        doReturn(email).when(spyJwtService).extractUsername(mockToken);
        when(userDetails.getUsername()).thenReturn(email);

        assertThrows(AccessDeniedException.class, () -> {
            doCallRealMethod().when(spyJwtService).throwInvalidTokenError();
            spyJwtService.validateToken(mockToken, userDetails);
        });
    }

    @Test
    public void validateToken_throwsInvalidTokenError_whenTokenUsernameNotMatchUserDetails() throws AuthenticationException, AccessDeniedException {
        JwtService spyJwtService = spy(jwtService);

        doReturn(false).when(spyJwtService).isTokenExpired(mockToken);
        doReturn(email).when(spyJwtService).extractUsername(mockToken);
        when(userDetails.getUsername()).thenReturn("");

        assertThrows(AccessDeniedException.class, () -> {
            doCallRealMethod().when(spyJwtService).throwInvalidTokenError();
            spyJwtService.validateToken(mockToken, userDetails);
        });
    }

    @Test
    public void validateToken_throwsInvalidTokenError_whenTokenIssuerNotValid() throws AuthenticationException, AccessDeniedException {
        JwtService spyJwtService = spy(jwtService);

        doReturn(false).when(spyJwtService).isTokenExpired(mockToken);
        doReturn(email).when(spyJwtService).extractUsername(mockToken);
        when(userDetails.getUsername()).thenReturn(email);

        doReturn("not the issuer").when(spyJwtService).extractIssuer(mockToken);

        assertThrows(AccessDeniedException.class, () -> {
            doCallRealMethod().when(spyJwtService).throwInvalidTokenError();
            spyJwtService.validateToken(mockToken, userDetails);
        });
    }

    @Test
    public void validateToken_throwsInvalidTokenError_whenTokenAudienceNotValid() throws AuthenticationException, AccessDeniedException {
        JwtService spyJwtService = spy(jwtService);

        doReturn(false).when(spyJwtService).isTokenExpired(mockToken);
        doReturn(email).when(spyJwtService).extractUsername(mockToken);
        when(userDetails.getUsername()).thenReturn(email);
        doReturn(issuerUri).when(spyJwtService).extractIssuer(mockToken);

        Set<String> badAudiences = new HashSet<>();
        badAudiences.add("not the audience");
        doReturn(badAudiences).when(spyJwtService).extractAudience(mockToken);

        assertThrows(AccessDeniedException.class, () -> {
            doCallRealMethod().when(spyJwtService).throwInvalidTokenError();
            spyJwtService.validateToken(mockToken, userDetails);
        });
    }

    @Test
    public void validateToken_returnsNull_whenTokenIsValid() throws AuthenticationException, AccessDeniedException {
        JwtService spyJwtService = spy(jwtService);

        doReturn(false).when(spyJwtService).isTokenExpired(mockToken);
        doReturn(email).when(spyJwtService).extractUsername(mockToken);
        when(userDetails.getUsername()).thenReturn(email);
        doReturn(issuerUri).when(spyJwtService).extractIssuer(mockToken);

        Set<String> testAllowedAudiences = new HashSet<>();
        testAllowedAudiences.add(allowedAudiences.get(0));
        doReturn(testAllowedAudiences).when(spyJwtService).extractAudience(mockToken);

        doCallRealMethod().when(spyJwtService).throwInvalidTokenError();
        spyJwtService.validateToken(mockToken, userDetails);

        verify(spyJwtService, times(0)).throwInvalidTokenError();
    }

    @Test
    public void getTokenBody_shouldReturnClaims_whenGivenAValidToken() throws AuthenticationException, AccessDeniedException {
        Claims claims = jwtService.getTokenBody(mockToken);
        assertInstanceOf(Claims.class, claims);
    }

    @Test
    public void getTokenBody_throwsAccessDeniedException_whenTokenIsInvalid() throws AuthenticationException, AccessDeniedException {
        assertThrows(AccessDeniedException.class, () -> {
            jwtService.getTokenBody("");
        });
    }

    @Test
    public void extractToken_shouldReturnAnEmptyOptional_whenNoAuthHeaderSupplied() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader(anyString())).thenReturn(null);

        Optional<String> token = jwtService.extractToken(mockRequest);
        assertTrue(token.isEmpty());
    }

    @Test
    public void extractToken_shouldReturnAnEmptyOptional_whenMismatchTokenPrefix() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader(anyString())).thenReturn("Not Bearer the_token");

        Optional<String> token = jwtService.extractToken(mockRequest);
        assertTrue(token.isEmpty());
    }

    @Test
    public void extractToken_shouldAnOptionalContainingOnlyBearerToken_whenValidTokenSuppliedInAuthHeader() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader(anyString())).thenReturn("Bearer " + mockToken);

        Optional<String> token = jwtService.extractToken(mockRequest);
        assertTrue(token.isPresent());
        assertEquals(token.get(), mockToken);
    }
}
