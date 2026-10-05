package com.company.MakeMyTrip.pricing_service.exceptions;

public class InvalidTravelDateException extends RuntimeException {

    public InvalidTravelDateException(String message) {
        super(message);
    }
}