package com.company.MakeMyTrip.events;

import java.time.Instant;

public record BookingLifecycleEvent(
        String eventId,
        Long bookingId,
        Long userId,
        String bookingType,
        String status,
        Instant occurredAt
) {
}