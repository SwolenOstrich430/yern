package com.yern.config;

import com.yern.service.JwtService;
import com.yern.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {
    // TODO: probably a library that has this
    private final String AUTH_HEADER = "Authorization";

    @Value("${security.auth.oauth2.token_prefix}")
    private String tokenPrefix;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    public JwtFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws IOException, ServletException {
        Optional<String> token = extractToken(request);
        if (token.isEmpty() || token.get().isEmpty()) {
            throw new AccessDeniedException("Token is empty.");
        }

        Optional<UserDetails> userDetails = extractUserDetails(token.get());
        if (userDetails.isEmpty()) {
            throw new AccessDeniedException("No user found,");
        }

        jwtService.validateToken(token.get(), userDetails.get());
        setSecurityContextAuthentication(request, userDetails.get());

        filterChain.doFilter(request, response);
    }

    protected void setSecurityContextAuthentication(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTH_HEADER);

        if (authHeader == null || !authHeader.startsWith(tokenPrefix)) {
            return Optional.empty();
        }

        final String jwtToken = authHeader.substring(tokenPrefix.length());
        return Optional.of(jwtToken);
    }

    private Optional<UserDetails> extractUserDetails(String token) throws AccessDeniedException {
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
}
