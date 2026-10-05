package com.company.MakeMyTrip.pricing_service.exceptions;

public class BasePriceAlreadyExistsException
        extends RuntimeException {

    public BasePriceAlreadyExistsException(String message) {
        super(message);
    }
}