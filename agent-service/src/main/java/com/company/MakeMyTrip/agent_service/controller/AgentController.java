
package com.company.MakeMyTrip.agent_service.controller;

import com.company.MakeMyTrip.agent_service.dto.AgentBookingResponse;
import com.company.MakeMyTrip.agent_service.service.AgentBookingService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private final AgentBookingService agentBookingService;

    public AgentController(AgentBookingService agentBookingService) {
        this.agentBookingService = agentBookingService;
    }

    @GetMapping("/bookings")
    public List<AgentBookingResponse> getMyBookings(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        return agentBookingService.getMyBookings(authorization);
    }
}
