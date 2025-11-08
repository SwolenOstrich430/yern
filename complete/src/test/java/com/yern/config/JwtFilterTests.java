package com.yern.config;

import com.yern.dto.security.authentication.UserDetailsImpl;
import com.yern.service.security.authentication.JwtService;
import com.yern.service.security.authentication.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
    JwtFilter.class
})
public class JwtFilterTests {

    private final String noTokenError = "Token is empty.";
    private JwtFilter jwtFilter;
    private final String validToken = "validToken";
    private final String noUserMessage = "No user found.";

    @Value("${api.endpoints.auth.base-uri}")
    private List<String> uriToIgnore;

    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private FilterChain mockChain;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertiesResolver() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @BeforeEach
    void setUp() {
        jwtFilter = new JwtFilter(jwtService, userDetailsService, uriToIgnore); // Instantiate your filter
    }


    @Test
    @DisplayName(
        "Do Filter Internal: throws AccessDeniedException when no JWT is found in Authorization Header"
    )
    public void doFilterInternal_shouldThrowAccessDeniedException_whenHeaderTokenIsNull() throws ServletException, IOException {
        when(
            jwtService.extractToken(mockRequest)
        ).thenReturn(Optional.empty());

        AccessDeniedException thrown = assertThrows(
                AccessDeniedException.class,
                () -> {
                    jwtFilter.doFilterInternal(
                        mockRequest,
                        mockResponse,
                        mockChain
                    );
                }
        );

        assertEquals(noTokenError, thrown.getMessage());
    }

    @Test
    @DisplayName(
        "Do Filter Internal: throws AccessDeniedException when an empty JWT is found in Authorization Header"
    )
    public void doFilterInternal_shouldThrowAccessDeniedException_whenHeaderTokenIsEmpty() {
        when(
            jwtService.extractToken(mockRequest)
        ).thenReturn(Optional.of(""));

        AccessDeniedException thrown = assertThrows(
            AccessDeniedException.class,
            () -> {
                jwtFilter.doFilterInternal(
                    mockRequest,
                    mockResponse,
                    mockChain
                );
            }
        );

        assertEquals(noTokenError, thrown.getMessage());
    }

    @Test
    @DisplayName(
        "Do Filter Internal: throws AccessDeniedException when no associated User is found based on the provided Bearer token."
    )
    public void doFilterInternal_shouldThrowAccessDeniedException_whenNoUserIsFound() throws AccessDeniedException {
        when(
            jwtService.extractToken(mockRequest)
        ).thenReturn(Optional.of(validToken));

        JwtFilter spyJwtFilter = Mockito.spy(jwtFilter);

        doReturn(
            Optional.empty()
        ).when(spyJwtFilter).extractUserDetails(validToken);

        AccessDeniedException thrown = assertThrows(
            AccessDeniedException.class,
            () -> {
                jwtFilter.doFilterInternal(
                    mockRequest,
                    mockResponse,
                    mockChain
                );
            }
        );

        assertEquals(noUserMessage, thrown.getMessage());
    }

    @Test
    @DisplayName(
        "Do Filter Internal: validates the provided token when a JWT and valid user are found."
    )
    public void doFilterInternal_shouldValidateToken_whenValidUserAndTokenAreFound() throws IOException, ServletException {
        when(
            jwtService.extractToken(mockRequest)
        ).thenReturn(Optional.of(validToken));

        JwtFilter spyJwtFilter = Mockito.spy(jwtFilter);

        UserDetailsImpl userDetails = new UserDetailsImpl("testUser", "");

        doReturn(Optional.of(userDetails)).when(spyJwtFilter).extractUserDetails(anyString());

        when(
            spyJwtFilter.extractUserDetails(validToken)
        ).thenReturn(Optional.of(userDetails));

        doNothing().when(jwtService).validateToken(validToken, userDetails);
        doNothing().when(spyJwtFilter).setSecurityContextAuthentication(mockRequest,  userDetails);

        spyJwtFilter.doFilterInternal(
            mockRequest,
            mockResponse,
            mockChain
        );


        verify(jwtService, times(1)).validateToken(validToken, userDetails);
    }

    @Test
    @DisplayName(
        "Do Filter Internal: sets the security context for the associated user when a valid token is found."
    )
    public void doFilterInternal_shouldSecurityContext_whenValidUserAndTokenAreFound() throws IOException, ServletException {
        when(
                jwtService.extractToken(mockRequest)
        ).thenReturn(Optional.of(validToken));

        JwtFilter spyJwtFilter = Mockito.spy(jwtFilter);

        UserDetailsImpl userDetails = new UserDetailsImpl("testUser", "");

        doReturn(Optional.of(userDetails)).when(spyJwtFilter).extractUserDetails(anyString());

        when(
                spyJwtFilter.extractUserDetails(validToken)
        ).thenReturn(Optional.of(userDetails));

        doNothing().when(jwtService).validateToken(validToken, userDetails);
        doNothing().when(spyJwtFilter).setSecurityContextAuthentication(mockRequest,  userDetails);

        spyJwtFilter.doFilterInternal(
                mockRequest,
                mockResponse,
                mockChain
        );


        verify(jwtService, times(1)).validateToken(validToken, userDetails);
        verify(
            spyJwtFilter, times(1)
        ).setSecurityContextAuthentication(mockRequest, userDetails);
    }

    @Test
    @DisplayName(
        "Set Security Context Authentication: sets the provided user details on the request's security context."
    )
    public void setSecurityContextAuthentication_setsSecurityContext_withProvidedUserDetails() throws IOException, ServletException {
        UsernamePasswordAuthenticationToken mockToken =  mock(UsernamePasswordAuthenticationToken.class);
        WebAuthenticationDetails mockDetails = mock(WebAuthenticationDetails.class);

        JwtFilter spyJwtFilter = Mockito.spy(jwtFilter);
        UserDetailsImpl userDetails = new UserDetailsImpl("testUser", "");

        doReturn(mockDetails).when(spyJwtFilter).getWebAuthenticationDetails(mockRequest);
        doReturn(mockToken).when(spyJwtFilter).getAuthentication(userDetails);

        SecurityContext mockContext = Mockito.mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(mockContext);
            spyJwtFilter.setSecurityContextAuthentication(
                mockRequest,
                userDetails
            );

            verify(mockToken, times(1)).setDetails(mockDetails);
            verify(mockContext, times(1)).setAuthentication(mockToken);
        }
    }

    @Test
    public void extractUserDetails_shouldReturnAnEmptyOptional_whenNoUserIsFound() throws IOException, ServletException {
        when(jwtService.extractUsername(validToken)).thenReturn(null);
        Optional<UserDetails> details = jwtFilter.extractUserDetails(validToken);

        assertTrue(details.isEmpty());

        when(jwtService.extractUsername(validToken)).thenReturn("");
        Optional<UserDetails> emptyDetails = jwtFilter.extractUserDetails(validToken);

        assertTrue(emptyDetails.isEmpty());
    }

    @Test
    public void extractUserDetails_shouldReturnAnEmptyOptional_whenAnAuthenticationIsAlreadyFound() throws IOException, ServletException {
        when(jwtService.extractUsername(validToken)).thenReturn(validToken);
        SecurityContext mockContext = Mockito.mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(mockContext);
            when(mockContext.getAuthentication()).thenReturn(mock(Authentication.class));

            Optional<UserDetails> emptyDetails = jwtFilter.extractUserDetails(validToken);
            assert(emptyDetails.isEmpty());
        }
    }

    @Test
    public void extractUserDetails_shouldReturnAnOptionalOfUserDetails_whenAValidUsernameIsFound() throws IOException, ServletException {
        when(jwtService.extractUsername(validToken)).thenReturn(validToken);
        SecurityContext mockContext = Mockito.mock(SecurityContext.class);
        UserDetailsImpl userDetails = new UserDetailsImpl("testUser", "");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(mockContext);
            when(mockContext.getAuthentication()).thenReturn(null);

            when(userDetailsService.loadUserByUsername(validToken)).thenReturn(userDetails);
            Optional<UserDetails> nonEmptyDetails = jwtFilter.extractUserDetails(validToken);

            assertTrue(nonEmptyDetails.isPresent());
            assertEquals(nonEmptyDetails.get().getUsername(), userDetails.getUsername());
        }
    }

    @Test
    public void shouldNotFilter_shouldReturnTrue_whenTheRequestUriStartsWithUriToIgnore() throws ServletException {
        uriToIgnore.forEach(uri -> {
            when(mockRequest.getRequestURI()).thenReturn(uri);

            try {
                assertTrue(
                    jwtFilter.shouldNotFilter(mockRequest)
                );
            } catch (ServletException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void shouldNotFilter_shouldReturnFalse_whenTheRequestUriDoesntStartWithUriToIgnore() throws ServletException {
        List<String> uriToFilter = new ArrayList<>();
        uriToFilter.add("/api/users");
        uriToFilter.add("/api/yarn");

        uriToFilter.forEach(uri -> {
            when(mockRequest.getRequestURI()).thenReturn(uri);

            try {
                assertFalse(
                    jwtFilter.shouldNotFilter(mockRequest)
                );
            } catch (ServletException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
