package com.company.MakeMyTrip.userProfile_service.service;

import com.company.MakeMyTrip.userProfile_service.dtos.UserProfileRequest;
import com.company.MakeMyTrip.userProfile_service.dtos.UserProfileResponse;

public interface UserProfileService {

    UserProfileResponse createOrUpdateProfile(Long userId, UserProfileRequest request);
    UserProfileResponse getProfile(Long userId);
}
