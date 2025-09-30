package com.company.MakeMyTrip.Auth_service.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {

    private Long userId;
    private String username;
    private String email;
    private String message;
}
