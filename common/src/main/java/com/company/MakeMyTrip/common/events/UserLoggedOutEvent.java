package com.company.MakeMyTrip.common.events;

import java.time.Instant;

public record UserLoggedOutEvent(
        Long userId,
        Instant occurredAt
) {
}