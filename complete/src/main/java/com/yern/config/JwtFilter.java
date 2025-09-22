package com.yern.config;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.yern.service.security.authentication.JwtService;
import com.yern.service.security.authentication.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {
    // TODO: probably a library that has this
    private final String AUTH_HEADER = "Authorization";

    @Value("${security.auth.oauth2.token-prefix}")
    private String tokenPrefix;

    @Value("${api.endpoints.auth.base-uri}")
    private List<String> uriToIgnore;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    public JwtFilter(
        JwtService jwtService,
        UserDetailsServiceImpl userDetailsService,
        @Value("${api.endpoints.auth.base-uri}") List<String> uriToIgnore
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.uriToIgnore = uriToIgnore;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws IOException, ServletException {
        Optional<String> token = jwtService.extractToken(request);
        if (token.isEmpty() || token.get().isEmpty()) {
            throw new AccessDeniedException("Token is empty.");
        }

        Optional<UserDetails> userDetails = extractUserDetails(token.get());
        if (userDetails.isEmpty()) {
            throw new AccessDeniedException("No user found.");
        }

        jwtService.validateToken(token.get(), userDetails.get());
        setSecurityContextAuthentication(request, userDetails.get());

        filterChain.doFilter(request, response);
    }

    protected void setSecurityContextAuthentication(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authToken = getAuthentication(userDetails);

        authToken.setDetails(getWebAuthenticationDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    public Optional<UserDetails> extractUserDetails(String token) throws AccessDeniedException {
        String username = jwtService.extractUsername(token);
        if (username == null || username.isEmpty()) {
            return Optional.empty();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return Optional.empty();
        }

        return Optional.of(
            userDetailsService.loadUserByUsername(username)
        );
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return uriToIgnore.stream().anyMatch(path::startsWith);
    }

    public UsernamePasswordAuthenticationToken getAuthentication(UserDetails userDetails) {
       return new UsernamePasswordAuthenticationToken(
           userDetails,
           null,
           userDetails.getAuthorities()
       );
    }

    public WebAuthenticationDetails getWebAuthenticationDetails(HttpServletRequest request) {
       return new WebAuthenticationDetailsSource().buildDetails(request);
    }
}
