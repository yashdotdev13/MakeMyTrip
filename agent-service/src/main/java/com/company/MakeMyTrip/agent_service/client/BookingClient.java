
package com.company.MakeMyTrip.agent_service.client;

import com.company.MakeMyTrip.agent_service.dto.AgentBookingResponse;
import com.company.MakeMyTrip.agent_service.dto.BookingListResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class BookingClient {

    private final RestClient restClient;

    public BookingClient(
            RestClient.Builder restClientBuilder,
            @Value("${booking-service.base-url:http://localhost:8081}") String baseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public List<AgentBookingResponse> getMyBookings(String authorizationHeader) {
        BookingListResponse response = restClient.get()
                .uri("/booking")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(BookingListResponse.class);

        if (response == null || response.data() == null) {
            return List.of();
        }

        return response.data();
    }
}
