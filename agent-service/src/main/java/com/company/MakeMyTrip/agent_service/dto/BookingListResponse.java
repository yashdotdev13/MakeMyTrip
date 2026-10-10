
package com.company.MakeMyTrip.agent_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingListResponse(
        String timeStamp,
        List<AgentBookingResponse> data,
        Object error
) {
}
