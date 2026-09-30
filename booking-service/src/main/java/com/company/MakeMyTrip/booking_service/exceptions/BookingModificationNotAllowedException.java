package com.company.MakeMyTrip.booking_service.exceptions;

public class BookingModificationNotAllowedException extends RuntimeException {
    public BookingModificationNotAllowedException(String message) {
        super(message);
    }
}
