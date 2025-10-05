package com.company.MakeMyTrip.pricing_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "booking-service", url = "${booking.service.url}") // set your booking-service URL in application.properties
public interface BookingServiceClient {

    @GetMapping("/booking/count")
    int getBookingCount(@RequestParam("referenceId") Long referenceId,
                        @RequestParam("travelDate") String travelDate);
}