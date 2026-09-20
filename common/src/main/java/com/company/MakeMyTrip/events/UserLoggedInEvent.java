package com.company.MakeMyTrip.events;

import java.time.Instant;

public record UserLoggedInEvent(
        Long userId,
        String username,
        String email,
        String role,
        Instant occurredAt
) {
}