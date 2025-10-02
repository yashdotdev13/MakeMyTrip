package com.company.MakeMyTrip.userProfile_service.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private Long userId;
    private String fullName;
    private String phone;
    private String address;
    private String preferences;
}
