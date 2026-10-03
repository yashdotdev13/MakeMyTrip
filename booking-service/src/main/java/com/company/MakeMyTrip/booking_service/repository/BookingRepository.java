package com.company.MakeMyTrip.booking_service.repository;

import com.company.MakeMyTrip.booking_service.entity.Booking;
import com.company.MakeMyTrip.booking_service.enums.BookingType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);
    List<Booking> findByUserIdAndBookingType(Long userId, BookingType bookingType);
    List<Booking> findAllByUserId(Long userId);
    Optional<Booking> findByIdAndUserId(Long bookingId, Long userId);


    @Query("SELECT COUNT(b) FROM Booking b WHERE b.referenceId = :referenceId AND b.travelDate = :travelDate")
    int countByReferenceIdAndTravelDate(@Param("referenceId") Long referenceId,
                                        @Param("travelDate") LocalDate travelDate);

}