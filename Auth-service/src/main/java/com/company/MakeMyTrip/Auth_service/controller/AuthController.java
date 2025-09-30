package com.company.MakeMyTrip.Auth_service.controller;


import com.company.MakeMyTrip.Auth_service.dtos.*;
import com.company.MakeMyTrip.Auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;


    // register a new User
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest){
        log.info("Received registration request for email: {}",registerRequest.getEmail());

        RegisterResponse response = authService.register(registerRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // login a user
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) throws InvalidCredentialsException {
        log.info("Received login request for: {}", loginRequest.getUsername());
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }



    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        log.info("Received refresh token request");
        try {
            AuthResponse authResponse = authService.refreshToken(refreshTokenRequest.getRefreshToken());
            return ResponseEntity.ok(authResponse);
        } catch (InvalidCredentialsException e) {
            log.warn("Refresh token invalid or expired: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
