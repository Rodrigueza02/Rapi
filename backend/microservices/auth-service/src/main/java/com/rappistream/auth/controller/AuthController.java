
package com.rappistream.auth.controller;

import com.rappistream.auth.service.AuthService;

import dtos.AuthResponse;
import dtos.LoginRequest;
import dtos.RegisterRequest;
import dtos.UserProfileResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            new UserProfileResponse(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaimAsString("nombre"),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("rol")
            )
        );
    }
}
