package com.yern.controller.security;

import com.yern.dto.authentication.LoginRequest;
import com.yern.dto.authentication.LoginResponse;
import com.yern.dto.user.UserPostDto;
import com.yern.service.security.authentication.AuthenticationService;
import com.yern.service.security.authentication.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


@RestController
public class AuthenticationController {

    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private final AuthenticationService authenticationService;

    @Autowired
    private final JwtService jwtService;

    public AuthenticationController(
            AuthenticationManager authenticationManager,
            AuthenticationService authenticationService,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }

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

    @PostMapping("/api/auth/register")
    public void registerUser(@RequestBody UserPostDto user) {
        authenticationService.registerUser(user);
    }

    @GetMapping("api/auth/oauth2/start")
    public String grantCode(
        @RequestParam("code") String code,
        @RequestParam("scope") String scope,
        @RequestParam("authuser") String authUser,
        @RequestParam("prompt") String prompt
    ) {
        return authenticationService.processGrantCode(code);
    }
}
