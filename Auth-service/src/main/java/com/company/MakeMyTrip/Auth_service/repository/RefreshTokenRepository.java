package com.company.MakeMyTrip.Auth_service.repository;

import com.company.MakeMyTrip.Auth_service.entity.RefreshToken;
import com.company.MakeMyTrip.Auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);



}
