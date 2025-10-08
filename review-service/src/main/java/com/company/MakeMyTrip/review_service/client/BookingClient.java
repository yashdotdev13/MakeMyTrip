package com.company.MakeMyTrip.review_service.client;


import com.company.MakeMyTrip.review_service.dtos.BookingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "booking-service", path = "/booking")
public interface BookingClient {


    @GetMapping("/{bookingId}")
    BookingResponse getBookingById(@PathVariable Long bookingId);
}
