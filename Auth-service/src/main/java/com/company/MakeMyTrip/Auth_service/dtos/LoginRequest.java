package com.company.MakeMyTrip.Auth_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    @Size(max = 255, message = "Username or email is too long")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 1, max = 100, message = "Invalid password")
    private String password;
}