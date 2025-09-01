package com.yern.controller;

import com.yern.dto.authentication.LoginRequest;
import com.yern.dto.authentication.LoginResponse;
import com.yern.dto.user.UserPostDto;
import com.yern.service.AuthenticationService;
import com.yern.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
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

        String token = jwtService.generateToken(request.email());
        return ResponseEntity.ok(new LoginResponse(request.email(), token));
    }

    @PostMapping("/api/auth/register")
    public void registerUser(@RequestBody UserPostDto user) {
        authenticationService.registerUser(user);
    }
}
