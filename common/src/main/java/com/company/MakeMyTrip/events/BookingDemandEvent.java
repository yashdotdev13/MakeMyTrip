package com.company.MakeMyTrip.events;

import java.time.Instant;

public record BookingDemandEvent(
        String eventId,
        Long bookingId,
        Long referenceId,
        String bookingType,
        Integer quantity,
        String travelDate,
        BookingDemandEventType eventType,
        Instant occurredAt
) {
}