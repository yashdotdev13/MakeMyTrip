package com.company.MakeMyTrip.Auth_service.service;

import com.company.MakeMyTrip.Auth_service.dtos.*;

public interface AuthService {

    RegisterResponse register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest);

    AuthResponse refreshToken(String refreshToken);

    void logout(LogoutRequest logoutRequest);
}