package com.company.MakeMyTrip.booking_service.exceptions;

public class ReservationAlreadyExistsException extends RuntimeException {

    public ReservationAlreadyExistsException(String message) {
        super(message);
    }
}