
package com.company.MakeMyTrip.agent_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AgentBookingResponse(
        Long id,
        Long userId,
        String bookingType,
        Long referenceId,
        String status,
        LocalDateTime bookingDate,
        LocalDate travelDate,
        BigDecimal amount
) {
}
