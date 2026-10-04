package com.company.MakeMyTrip.booking_service.repository;


import com.company.MakeMyTrip.booking_service.entity.Reservation;
import com.company.MakeMyTrip.booking_service.enums.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByBookingId(Long bookingId);

    List<Reservation> findByStatusAndExpiresAtBefore(
            ReservationStatus status,
            LocalDateTime currentTime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.id = :reservationId
    """)
    Optional<Reservation> findByIdForUpdate(
            @Param("reservationId") Long reservationId
    );
}