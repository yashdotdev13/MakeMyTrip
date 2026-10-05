package com.company.MakeMyTrip.pricing_service.exceptions;

public class BasePriceNotFoundException extends RuntimeException {

    public BasePriceNotFoundException(String message) {
        super(message);
    }
}