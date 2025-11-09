package com.yern.controller.security;

import com.yern.dto.security.authentication.LoginRequest;
import com.yern.dto.security.authentication.LoginResponse;
import com.yern.dto.user.UserPostDto;
import com.yern.exceptions.DuplicateException;
import com.yern.service.security.authentication.AuthenticationService;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.IOException;
import java.net.URI;
import java.security.NoSuchProviderException;


@RestController
public class AuthenticationController {

    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private final AuthenticationService authenticationService;

    // Probably should make a global util method for this
    @Value("${api.constants.redirect-prefix}")
    private String redirectPrefix;

    public AuthenticationController(
            AuthenticationManager authenticationManager,
            AuthenticationService authenticationService
    ) {
        this.authenticationManager = authenticationManager;
        this.authenticationService = authenticationService;
    }

    // Basic Login
    // TODO: this is where you'll need to add handling for multiple providers/auth types
    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) throws AuthenticationException {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
            )
        );

        return ResponseEntity.ok(
            authenticationService.loginUser(request.email())
        );
    }

    @PostMapping("/api/auth/register")
    public void registerUser(@RequestBody UserPostDto userDto) throws DuplicateException {
        authenticationService.registerUser(userDto);
    }

    // Starts the OAuth flow once a user hits "sign-in with provider
    @GetMapping("api/auth/oauth2/{provider}/start")
    public void startOauthFlow(
        @PathVariable String provider,
        @RequestParam String email,
        HttpServletResponse response
    ) throws NoSuchProviderException, IOException {
        URI redirectUri = authenticationService.getOauthInitiateUri(
            provider,
            email
        );

        response.setStatus(HttpStatus.MOVED_PERMANENTLY.value());
        response.sendRedirect(redirectUri.toString());
    }

    // Callback used once the initial auth/oauth2/provider/start has been hit
    @GetMapping("api/auth/oauth2/{provider}/response-oidc")
    public LoginResponse grantCode(
        @PathVariable String provider,
        @RequestParam("code") String code,
        @RequestParam("scope") String scope,
        @RequestParam("authuser") String authUser,
        @RequestParam("prompt") String prompt
    ) throws NoSuchProviderException {
        return authenticationService.processGrantCode(provider, code);
    }
}
