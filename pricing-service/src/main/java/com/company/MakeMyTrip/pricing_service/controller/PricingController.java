package com.company.MakeMyTrip.pricing_service.controller;

import com.company.MakeMyTrip.pricing_service.advices.ApiResponse;
import com.company.MakeMyTrip.pricing_service.dtos.PriceLockRequest;
import com.company.MakeMyTrip.pricing_service.dtos.PriceLockResponse;
import com.company.MakeMyTrip.pricing_service.dtos.PriceQuoteRequest;
import com.company.MakeMyTrip.pricing_service.dtos.PriceQuoteResponse;
import com.company.MakeMyTrip.pricing_service.service.PricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pricing")
@RequiredArgsConstructor
@Slf4j
public class PricingController {

    private final PricingService pricingService;

    @PostMapping("/quote")
    public ResponseEntity<ApiResponse<PriceQuoteResponse>> getPriceQuote(
            @Valid @RequestBody PriceQuoteRequest request) {
        log.info("Received price quote request: referenceId={}, bookingType={}, quantity={}, travelDate={}",
                request.getReferenceId(), request.getBookingType(), request.getQuantity(), request.getTravelDate());

        PriceQuoteResponse response = pricingService.getPriceQuote(request.getReferenceId(),
                request.getBookingType(), request.getQuantity(), request.getTravelDate());
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @PostMapping("/lock")
    public ResponseEntity<ApiResponse<PriceLockResponse>> lockPrice(@Valid @RequestBody PriceLockRequest request) {

        log.info("Received price lock request: referenceId={}, bookingType={}, quantity={}, travelDate={}",
                request.getReferenceId(), request.getBookingType(), request.getQuantity(), request.getTravelDate());

        PriceLockResponse response = pricingService.lockPrice(request);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @DeleteMapping("/lock/{lockId}")
    public ResponseEntity<ApiResponse<Void>> releasePriceLock(@PathVariable Long lockId) {

        log.info("Releasing price lock: lockId={}", lockId);

        boolean released = pricingService.releasePriceLock(lockId);
        if (!released) {
            throw new RuntimeException("Price lock cannot be released");
        }
        return ResponseEntity.ok(new ApiResponse<>(null));
    }
}