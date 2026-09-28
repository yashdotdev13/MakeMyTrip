package com.company.MakeMyTrip.userProfile_service.service.Impl;

import com.company.MakeMyTrip.userProfile_service.auth.UserContextHolder;
import com.company.MakeMyTrip.userProfile_service.dtos.UserProfileRequest;
import com.company.MakeMyTrip.userProfile_service.dtos.UserProfileResponse;
import com.company.MakeMyTrip.userProfile_service.entity.UserProfile;
import com.company.MakeMyTrip.userProfile_service.exceptions.InvalidUserContextException;
import com.company.MakeMyTrip.userProfile_service.exceptions.ProfileNotFoundException;
import com.company.MakeMyTrip.userProfile_service.repository.UserProfileRepository;
import com.company.MakeMyTrip.userProfile_service.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;


    @Override
    public UserProfileResponse createOrUpdateProfile(UserProfileRequest request) {


        Long userId = UserContextHolder.getCurrentUserId();

        if(userId == null){
            throw new InvalidUserContextException("User ID not found in request context");
        }
        log.debug("Creating or updating profile for userId={}",userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(UserProfile:: new);

        profile.setUserId(userId);
        profile.setFullName(request.getFullName());
        profile.setPhone(request.getPhone());
        profile.setAddress(request.getAddress());
        profile.setPreferences(request.getPreferences());

        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("User profile saved successfully for userId={}",userId);

        return toResponse(savedProfile);
    }

    @Override
    public UserProfileResponse getProfile() {

        Long userId = UserContextHolder.getCurrentUserId();

        if(userId== null){
            throw new InvalidUserContextException("User ID not found in request context");
        }
        log.debug("Fetching profile for userId={}",userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(()->
                        new ProfileNotFoundException("Profile not found for userId="+userId));

        return toResponse(profile);
    }

    private UserProfileResponse toResponse(UserProfile profile){

        return new UserProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getFullName(),
                profile.getPhone(),
                profile.getAddress(),
                profile.getPreferences()
        );
    }
}
