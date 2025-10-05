package com.company.MakeMyTrip.pricing_service.client;

import com.company.MakeMyTrip.pricing_service.dtos.BookingCountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "booking-service", url = "${booking.service.url}")
public interface BookingServiceClient {

    @GetMapping("/booking/count")
    BookingCountResponse getBookingCount(@RequestParam("referenceId") Long referenceId,
                                         @RequestParam("travelDate") String travelDate);
}