package com.company.MakeMyTrip.userProfile_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Full name must be between 2 and 100 characters"
    )
    private String fullName;

    @Size(
            max = 20,
            message = "Phone number must not exceed 20 characters"
    )
    private String phone;

    @Size(
            max = 255,
            message = "Address must not exceed 255 characters"
    )
    private String address;

    @Size(
            max = 1000,
            message = "Preferences must not exceed 1000 characters"
    )
    private String preferences;
}