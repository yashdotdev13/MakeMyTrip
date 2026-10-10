
package com.company.MakeMyTrip.agent_service.client;

import com.company.MakeMyTrip.agent_service.dto.AgentBookingResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class BookingClient {

    private final RestClient restClient;

    public BookingClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://localhost:8081")
                .build();
    }

    public List<AgentBookingResponse> getMyBookings(String authorization) {
        return restClient.get()
                .uri("/booking")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(new ParameterizedTypeReference<
                        List<AgentBookingResponse>>() {});
    }
}
