package com.company.MakeMyTrip.Auth_service.service;

import com.company.MakeMyTrip.Auth_service.dtos.*;
import org.apache.http.auth.InvalidCredentialsException;

public interface AuthService {

    RegisterResponse register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest) throws InvalidCredentialsException;

    AuthResponse refreshToken(String refreshToken) throws InvalidCredentialsException;

    void logout(LogoutRequest logoutRequest);
}
