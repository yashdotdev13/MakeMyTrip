
package com.company.MakeMyTrip.agent_service.service;

import com.company.MakeMyTrip.agent_service.client.BookingClient;
import com.company.MakeMyTrip.agent_service.dto.AgentBookingResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentBookingService {

    private final BookingClient bookingClient;

    public AgentBookingService(BookingClient bookingClient) {
        this.bookingClient = bookingClient;
    }

    public List<AgentBookingResponse> getMyBookings(String authorization) {
        return bookingClient.getMyBookings(authorization);
    }
}
