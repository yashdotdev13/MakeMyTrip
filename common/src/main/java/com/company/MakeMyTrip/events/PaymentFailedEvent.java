package com.company.MakeMyTrip.events;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentFailedEvent(
        String eventId,
        Long bookingId,
        Long userId,
        BigDecimal amount,
        String reason,
        Instant occurredAt
) {
}