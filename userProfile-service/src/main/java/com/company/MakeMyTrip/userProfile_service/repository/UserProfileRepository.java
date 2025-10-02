package com.company.MakeMyTrip.userProfile_service.repository;

import com.company.MakeMyTrip.userProfile_service.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile,Long> {

    Optional<UserProfile> findByUserId(Long userId);
}
