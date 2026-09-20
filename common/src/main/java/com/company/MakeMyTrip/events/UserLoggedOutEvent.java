package com.company.MakeMyTrip.events;

import java.time.Instant;

public record UserLoggedOutEvent(
        Long userId,
        Instant occurredAt
) {
}