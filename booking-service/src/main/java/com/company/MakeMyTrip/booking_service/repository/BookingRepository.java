package com.company.MakeMyTrip.booking_service.repository;

import com.company.MakeMyTrip.booking_service.entity.Booking;
import com.company.MakeMyTrip.booking_service.enums.BookingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // find all bookings for a given user
    List<Booking> findByUserId(Long userId);

    // Optional: Find bookings by type for a user
    List<Booking> findByUserIdAndBookingType(Long userId, BookingType bookingType);

    // Fetch all bookings for a specific user
    List<Booking> findAllByUserId(Long userId);

    // Fetch a single booking by its ID and the user ID to enforce ownership
    Optional<Booking> findByIdAndUserId(Long bookingId, Long userId);
}
