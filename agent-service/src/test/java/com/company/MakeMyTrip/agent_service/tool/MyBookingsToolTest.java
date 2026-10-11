package com.company.MakeMyTrip.agent_service.tool;

import com.company.MakeMyTrip.agent_service.dto.AgentBookingResponse;
import com.company.MakeMyTrip.agent_service.service.AgentBookingService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MyBookingsToolTest {

    @Test
    void returnsBookingsAndCount() {
        AgentBookingService bookingService =
                mock(AgentBookingService.class);

        AgentBookingResponse booking = new AgentBookingResponse(
                1L,
                10L,
                "FLIGHT",
                100L,
                "CONFIRMED",
                LocalDateTime.now(),
                LocalDate.now().plusDays(7),
                new BigDecimal("12500.00")
        );

        when(bookingService.getMyBookings("Bearer test-token"))
                .thenReturn(List.of(booking));

        MyBookingsTool tool = new MyBookingsTool(bookingService);

        Map<String, Object> result = tool.execute(
                Map.of(),
                "Bearer test-token"
        );

        assertEquals(1, result.get("count"));
        assertEquals(List.of(booking), result.get("bookings"));

        verify(bookingService).getMyBookings("Bearer test-token");
    }

    @Test
    void returnsZeroWhenNoBookingsExist() {
        AgentBookingService bookingService =
                mock(AgentBookingService.class);

        when(bookingService.getMyBookings("Bearer test-token"))
                .thenReturn(List.of());

        MyBookingsTool tool = new MyBookingsTool(bookingService);

        Map<String, Object> result = tool.execute(
                Map.of(),
                "Bearer test-token"
        );

        assertEquals(0, result.get("count"));
        assertEquals(List.of(), result.get("bookings"));
    }
}