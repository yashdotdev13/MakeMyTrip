package com.company.MakeMyTrip.pricing_service.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PricingContext(Long referenceId, String bookingType, BigDecimal basePrice, int quantity,
                             LocalDate travelDate, int currentBookings) {

    public PricingContext withBasePrice(BigDecimal newBasePrice) {
        return new PricingContext(referenceId, bookingType, newBasePrice, quantity, travelDate, currentBookings);
    }
}