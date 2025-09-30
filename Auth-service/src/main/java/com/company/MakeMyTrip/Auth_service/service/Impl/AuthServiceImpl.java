package com.company.MakeMyTrip.Auth_service.service.Impl;

import com.company.MakeMyTrip.Auth_service.dtos.*;
import com.company.MakeMyTrip.Auth_service.entity.RefreshToken;
import com.company.MakeMyTrip.Auth_service.entity.User;
import com.company.MakeMyTrip.Auth_service.enums.Role;
import com.company.MakeMyTrip.Auth_service.exceptions.ResourceNotFoundException;
import com.company.MakeMyTrip.Auth_service.repository.RefreshTokenRepository;
import com.company.MakeMyTrip.Auth_service.repository.UserRepository;
import com.company.MakeMyTrip.Auth_service.service.AuthService;
import com.company.MakeMyTrip.Auth_service.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.InvalidCredentialsException;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;


    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        log.info("Attempting to register new user with email: {}",registerRequest.getEmail());

        if(userRepository.existsByEmail(registerRequest.getEmail())){
            log.warn("Email already in user: {}", registerRequest.getEmail());
            throw new RuntimeException("Email already in use");
        }

        if(userRepository.existsByUsername(registerRequest.getUsername())){
            log.warn("Username already taken: {}", registerRequest.getUsername());
            throw new RuntimeException("Username already taken");
        }

        User user = modelMapper.map(registerRequest, User.class);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        if (registerRequest.getRole() == null || registerRequest.getRole().isEmpty()) {
            user.setRole(Role.USER);
        } else {
            user.setRole(Role.valueOf(registerRequest.getRole()));
        }


        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}",savedUser.getId());

        return new RegisterResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), "User registered successfully");
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) throws InvalidCredentialsException {
        log.info("Attempting login for: {}", loginRequest.getUsername());

        User user = userRepository.findByUsernameOrEmail(loginRequest.getUsername(),
                        loginRequest.getUsername())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", loginRequest.getUsername());
                    return new ResourceNotFoundException("User not found");
                });

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Invalid credentials for user: {}", loginRequest.getUsername());
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // Generate JWT
        String accessToken = jwtService.generateAccessToken(user);

        // Generate refresh token
        String refreshToken = createRefreshToken(user);

        log.info("Login successful for user id: {}", user.getId());
        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse refreshToken(String refreshTokenStr) throws InvalidCredentialsException {
        log.info("Refreshing JWT for refresh token: {}", refreshTokenStr);

        RefreshToken refreshToken = refreshTokenRepository.findByName(refreshTokenStr)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found: {}", refreshTokenStr);
                    return new ResourceNotFoundException("Invalid refresh token");
                });

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            log.warn("Refresh token expired for user id: {}", refreshToken.getUser().getId());
            throw new InvalidCredentialsException("Refresh token expired");
        }

        // Generate new JWT access token
        String newAccessToken = jwtService.generateAccessToken(refreshToken.getUser());

        log.info("JWT refreshed successfully for user id: {}", refreshToken.getUser().getId());
        return new AuthResponse(newAccessToken, refreshToken.getName(), "Bearer");
    }



    private String createRefreshToken(User user) {
        // Delete old token if exists
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setName(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(7 * 24 * 3600)); // 7 days
        refreshTokenRepository.save(refreshToken);

        log.info("Refresh token created for user id: {}", user.getId());
        return refreshToken.getName();
    }
}
