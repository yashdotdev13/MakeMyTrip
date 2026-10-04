package com.company.MakeMyTrip.booking_service.service;

import com.company.MakeMyTrip.booking_service.entity.Reservation;
import com.company.MakeMyTrip.booking_service.enums.BookingType;

import java.time.LocalDate;

public interface ReservationService {

    Reservation reserveInventory(
            Long bookingId,
            BookingType bookingType,
            Long referenceId,
            LocalDate travelDate,
            int quantity
    );

    Reservation confirmReservation(Long bookingId);

    Reservation releaseReservation(Long bookingId);

    int expireDueReservations();
}