package com.company.MakeMyTrip.Auth_service.dtos;


import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
