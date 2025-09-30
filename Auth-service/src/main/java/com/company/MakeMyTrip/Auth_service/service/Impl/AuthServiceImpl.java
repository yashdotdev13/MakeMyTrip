package com.company.MakeMyTrip.Auth_service.service.Impl;

import com.company.MakeMyTrip.Auth_service.dtos.*;
import com.company.MakeMyTrip.Auth_service.entity.User;
import com.company.MakeMyTrip.Auth_service.enums.Role;
import com.company.MakeMyTrip.Auth_service.repository.RefreshTokenRepository;
import com.company.MakeMyTrip.Auth_service.repository.UserRepository;
import com.company.MakeMyTrip.Auth_service.service.AuthService;
import com.company.MakeMyTrip.Auth_service.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


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
    public AuthResponse login(LoginRequest loginRequest) {
        return null;
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        return null;
    }
}
