package com.company.MakeMyTrip.review_service.repository;

import com.company.MakeMyTrip.review_service.entity.BookingProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingProjectionRepository
        extends JpaRepository<BookingProjection, Long> {

    Optional<BookingProjection> findByBookingIdAndUserId(
            Long bookingId,
            Long userId
    );
}