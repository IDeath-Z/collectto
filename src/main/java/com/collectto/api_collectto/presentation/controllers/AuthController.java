package com.collectto.api_collectto.presentation.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collectto.api_collectto.application.usecases.auth.ProcessUserLoginUseCase;
import com.collectto.api_collectto.application.usecases.auth.RefreshSessionUseCase;
import com.collectto.api_collectto.domain.shared.AuthToken;
import com.collectto.api_collectto.infrastructure.persistence.shared.TransactionalProxy;
import com.collectto.api_collectto.infrastructure.security.SpringSecurityAuthentication;
import com.collectto.api_collectto.presentation.dto.auth.LoginRequest;
import com.collectto.api_collectto.presentation.dto.auth.LoginResponse;
import com.collectto.api_collectto.presentation.dto.auth.RefreshTokenRequest;
import com.collectto.api_collectto.presentation.dto.auth.TokenRefreshResponse;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public final class AuthController {

    private final SpringSecurityAuthentication springSecurityAuthentication;
    private final ProcessUserLoginUseCase processUserLoginUseCase;
    private final RefreshSessionUseCase refreshSessionUseCase;
    private final TransactionalProxy transactionalProxy;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and generate JWT tokens", description = "Validates user credentials and returns a JWT access token and a refresh token.")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        var user = springSecurityAuthentication.authenticate(request.email(), request.password());
        
        AuthToken tokens = transactionalProxy.execute(() -> processUserLoginUseCase.execute(user));
        
        return ResponseEntity.ok(new LoginResponse(user.getId(), tokens.accessToken(), tokens.refreshToken()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Validates the refresh token and returns a new pair of access and refresh tokens.")
    public ResponseEntity<TokenRefreshResponse> refresh(@RequestBody RefreshTokenRequest request) {
        
        AuthToken newTokens = transactionalProxy.execute(() -> refreshSessionUseCase.execute(request.refreshToken()));
        
        return ResponseEntity.ok(new TokenRefreshResponse(newTokens.accessToken(), newTokens.refreshToken()));
    }
}