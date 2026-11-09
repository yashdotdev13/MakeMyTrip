package com.company.MakeMyTrip.userProfile_service.service;

import com.company.MakeMyTrip.userProfile_service.dtos.UserProfileRequest;
import com.company.MakeMyTrip.userProfile_service.dtos.UserProfileResponse;

public interface UserProfileService {



    // create or update User Profile
    UserProfileResponse createOrUpdateProfile(UserProfileRequest request);


    // Get profile of user
    UserProfileResponse getProfile();


}
