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
    public ResponseEntity<UserProfileResponse> createOrUpdateProfile(@RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(profileService.createOrUpdateProfile(request));
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile() {
        return ResponseEntity.ok(profileService.getProfile());
    }

}
