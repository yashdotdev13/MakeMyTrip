package com.company.MakeMyTrip.events;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCompletedEvent(
        String eventId,
        Long paymentId,
        Long bookingId,
        Long userId,
        BigDecimal amount,
        String transactionId,
        Instant occurredAt
) {
}