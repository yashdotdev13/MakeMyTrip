package com.company.MakeMyTrip.agent_service.tool;

import com.company.MakeMyTrip.agent_service.dto.AgentBookingResponse;
import com.company.MakeMyTrip.agent_service.service.AgentBookingService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MyBookingsTool implements AgentTool {

    private final AgentBookingService agentBookingService;

    public MyBookingsTool(AgentBookingService agentBookingService) {
        this.agentBookingService = agentBookingService;
    }

    @Override
    public String getName() {
        return "my_bookings";
    }

    @Override
    public String getDescription() {
        return "Retrieve the authenticated user's travel bookings.";
    }

    @Override
    public Map<String, Object> execute(
            Map<String, Object> arguments,
            String authorization
    ) {
        List<AgentBookingResponse> bookings =
                agentBookingService.getMyBookings(authorization);

        return Map.of(
                "bookings",
                bookings,
                "count",
                bookings.size()
        );
    }
}