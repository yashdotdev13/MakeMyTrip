package com.company.MakeMyTrip.booking_service.client;

import com.company.MakeMyTrip.booking_service.advices.ApiResponse;
import com.company.MakeMyTrip.booking_service.dtos.PriceLockResponse;
import com.company.MakeMyTrip.booking_service.dtos.PriceQuoteRequest;
import com.company.MakeMyTrip.booking_service.dtos.PriceQuoteResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "pricing-service")
public interface PricingClient {

    @PostMapping("/pricing/quote")
    ApiResponse<PriceQuoteResponse> getPriceQuote(@RequestBody PriceQuoteRequest request);

    @PostMapping("/pricing/lock/{referenceId}")
    ApiResponse<PriceLockResponse> lockPrice(@PathVariable Long referenceId,
                                             @RequestParam String bookingType);
}
