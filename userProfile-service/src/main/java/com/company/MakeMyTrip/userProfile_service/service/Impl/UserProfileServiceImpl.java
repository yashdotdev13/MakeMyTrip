package com.company.MakeMyTrip.userProfile_service.service.Impl;

import com.company.MakeMyTrip.userProfile_service.auth.UserContextHolder;
import com.company.MakeMyTrip.userProfile_service.dtos.UserProfileRequest;
import com.company.MakeMyTrip.userProfile_service.dtos.UserProfileResponse;
import com.company.MakeMyTrip.userProfile_service.entity.UserProfile;
import com.company.MakeMyTrip.userProfile_service.repository.UserProfileRepository;
import com.company.MakeMyTrip.userProfile_service.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final ModelMapper modelMapper;


    @Override
    public UserProfileResponse createOrUpdateProfile(UserProfileRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User ID not found in context");
        }

        log.info("Creating/updating profile for userId: {}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(new UserProfile());

        profile.setUserId(userId);
        profile.setFullName(request.getFullName());
        profile.setPhone(request.getPhone());
        profile.setAddress(request.getAddress());
        profile.setPreferences(request.getPreferences());

        UserProfile saved = userProfileRepository.save(profile);
        log.info("Profile saved for userId: {}", userId);
        return modelMapper.map(saved, UserProfileResponse.class);
    }



    @Override
    public UserProfileResponse getProfile() {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User ID not found in context");
        }

        log.info("Fetching profile for userId: {}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for userId: " + userId));

        return modelMapper.map(profile, UserProfileResponse.class);
    }
}
