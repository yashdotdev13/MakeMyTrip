package com.company.MakeMyTrip.pricing_service.controller;

import com.company.MakeMyTrip.pricing_service.dtos.PriceQuoteRequest;
import com.company.MakeMyTrip.pricing_service.dtos.PriceQuoteResponse;
import com.company.MakeMyTrip.pricing_service.dtos.PriceLockResponse;
import com.company.MakeMyTrip.pricing_service.service.PricingService;
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
    public ResponseEntity<PriceQuoteResponse> getPriceQuote(@RequestBody PriceQuoteRequest request) {
        log.info("Received PriceQuoteRequest: {}", request);
        PriceQuoteResponse response = pricingService.getPriceQuote(
                request.getReferenceId(),
                request.getBookingType(),
                request.getQuantity(),
                request.getTravelDate()
        );
        return ResponseEntity.ok(response);
    }



    @PostMapping("/lock/{referenceId}")
    public ResponseEntity<PriceLockResponse> lockPrice(
            @PathVariable Long referenceId,
            @RequestParam String bookingType
    ) {
        log.info("Locking price for refId={}, bookingType={}", referenceId, bookingType);

        PriceLockResponse response = pricingService.lockPrice(referenceId, bookingType);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/lock/{lockId}")
    public ResponseEntity<Void> releasePriceLock(@PathVariable Long lockId) {
        log.info("Releasing price lock with lockId={}", lockId);
        pricingService.releasePriceLock(lockId);
        return ResponseEntity.noContent().build();
    }
}
