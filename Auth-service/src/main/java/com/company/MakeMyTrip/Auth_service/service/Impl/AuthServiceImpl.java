package com.company.MakeMyTrip.Auth_service.service.Impl;


import com.company.MakeMyTrip.Auth_service.dtos.*;
import com.company.MakeMyTrip.Auth_service.entity.RefreshToken;
import com.company.MakeMyTrip.Auth_service.entity.User;
import com.company.MakeMyTrip.Auth_service.enums.Role;
import com.company.MakeMyTrip.Auth_service.exceptions.RuntimeConflictException;
import com.company.MakeMyTrip.Auth_service.repository.RefreshTokenRepository;
import com.company.MakeMyTrip.Auth_service.repository.UserRepository;
import com.company.MakeMyTrip.Auth_service.service.AuthService;
import com.company.MakeMyTrip.Auth_service.service.JwtService;
import com.company.MakeMyTrip.Auth_service.utils.TokenHashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final long REFRESH_TOKEN_EXPIRY_DAYS = 7;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeConflictException("Username is already registered");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeConflictException("Email is already registered");
        }
        User user = User.builder().username(username).email(email).password(passwordEncoder.encode(request.getPassword())).role(Role.USER).build();
        User savedUser = userRepository.save(user);
        log.info("User registered successfully. userId={}", savedUser.getId());
        return new RegisterResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), "User registered successfully");
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {

        String identifier = request.getUsername().trim();
        User user = userRepository.findByUsernameOrEmail(identifier, identifier.toLowerCase()).orElseThrow(()
                -> new BadCredentialsException("Invalid username/email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username/email or password");
        }
        String accessToken = jwtService.generateToken(user);
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder().token(refreshTokenValue).user(user)
                .expiryDate(Instant.now().plus(REFRESH_TOKEN_EXPIRY_DAYS, ChronoUnit.DAYS)).build();
        refreshTokenRepository.save(refreshToken);
        log.info("User authenticated successfully. userId={}", user.getId());
        return new AuthResponse(accessToken, refreshTokenValue);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String rawToken) {

        String hashedToken = TokenHashUtils.sha256(rawToken);

        RefreshToken existingToken = refreshTokenRepository.findByToken(hashedToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired refresh token"));

        if (existingToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(existingToken);
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        User user = existingToken.getUser();

        refreshTokenRepository.delete(existingToken);
        String accessToken = jwtService.generateToken(user);
        String newRawRefreshToken = UUID.randomUUID().toString();
        String newHashedRefreshToken = TokenHashUtils.sha256(newRawRefreshToken);

        RefreshToken newRefreshToken = RefreshToken.builder().token(newHashedRefreshToken)
                .user(user).expiryDate(Instant.now().plus(REFRESH_TOKEN_EXPIRY_DAYS, ChronoUnit.DAYS)).build();

        refreshTokenRepository.save(newRefreshToken);
        log.info("Refresh token rotated successfully. userId={}", user.getId());
        return new AuthResponse(accessToken, newRawRefreshToken);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest logoutRequest) {
        String username = logoutRequest.getUsername().trim();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            log.warn("Logout requested for unknown username");
            return;
        }
        refreshTokenRepository.deleteByUser(user);
        log.info("User logged out successfully. userId={}", user.getId());
    }
}