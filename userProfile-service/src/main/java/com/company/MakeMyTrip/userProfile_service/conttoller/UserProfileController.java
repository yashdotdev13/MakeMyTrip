package com.company.MakeMyTrip.userProfile_service.conttoller;


import com.company.MakeMyTrip.userProfile_service.dtos.UserProfileRequest;
import com.company.MakeMyTrip.userProfile_service.dtos.UserProfileResponse;
import com.company.MakeMyTrip.userProfile_service.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private  final UserProfileService profileService;

    @PostMapping
    public ResponseEntity<UserProfileResponse> createOrUpdateProfile(
            @RequestHeader("X-User-Id") Long userId, // extracted from JWT in API Gateway
            @RequestBody UserProfileRequest request) {

        return ResponseEntity.ok(profileService.createOrUpdateProfile(userId, request));
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }
}
