package com.company.MakeMyTrip.userProfile_service.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileRequest {

    private String fullName;
    private String phone;
    private String address;
    private String preferences;
}
