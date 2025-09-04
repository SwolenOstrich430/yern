package com.yern.controller.security;

import com.yern.dto.authentication.LoginRequest;
import com.yern.dto.authentication.LoginResponse;
import com.yern.service.security.authentication.AuthenticationService;
import com.yern.service.security.authentication.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.NoSuchProviderException;


@RestController
public class AuthenticationController {

    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private final AuthenticationService authenticationService;

    @Autowired
    private final JwtService jwtService;

    // Probably should make a global util method for this
    @Value("${api.constants.redirect-prefix}")
    private String redirectPrefix;

    public AuthenticationController(
            AuthenticationManager authenticationManager,
            AuthenticationService authenticationService,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }

    // Basic Login
    // TODO: this is where you'll need to add handling for multiple providers/auth types
    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
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

    // Starts the OAuth flow once a user hits "sign-in with provider
    @GetMapping("api/auth/oauth2/{provider}/start")
    public String startOauthFlow(
        @PathVariable String provider,
        @RequestParam String email
    ) throws NoSuchProviderException {
        String redirectUri = authenticationService.getOauthInitiateUri(
            provider,
            email
        );

        return (redirectPrefix + redirectUri);
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
