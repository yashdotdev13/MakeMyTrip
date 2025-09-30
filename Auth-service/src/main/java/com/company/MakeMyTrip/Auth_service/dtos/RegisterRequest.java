package com.company.MakeMyTrip.Auth_service.dtos;


import lombok.Data;

@Data
public class RegisterRequest {

    private String username;
    private String email;
    private String password;
    private String role;  // default USER, if not provided
}
