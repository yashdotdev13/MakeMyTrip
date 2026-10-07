package com.company.MakeMyTrip.events;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentRequestedEvent(
        String eventId,
        Long bookingId,
        Long userId,
        BigDecimal amount,
        String paymentMethod,
        Instant occurredAt
) {
}