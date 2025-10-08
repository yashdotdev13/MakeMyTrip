package com.company.MakeMyTrip.booking_service.client;

import com.company.MakeMyTrip.booking_service.dtos.ReviewResponse;
import com.company.MakeMyTrip.booking_service.dtos.ReviewSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "review-service", path = "/review")
public interface ReviewClient {

    @GetMapping("/booking/{bookingId}")
    List<ReviewResponse> getReviewsByBookingId(@PathVariable Long bookingId);

    @GetMapping("/booking/{bookingId}/summary")
    ReviewSummaryResponse getReviewSummary(@PathVariable Long bookingId);
}