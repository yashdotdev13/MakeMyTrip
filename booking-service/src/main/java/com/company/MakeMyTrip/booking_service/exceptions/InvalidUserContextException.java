package com.company.MakeMyTrip.booking_service.exceptions;

public class InvalidUserContextException extends RuntimeException {
    public InvalidUserContextException(String message) {
        super(message);
    }
}
