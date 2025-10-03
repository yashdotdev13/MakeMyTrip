package com.company.MakeMyTrip.pricing_service.controller;

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
    public ResponseEntity<PriceQuoteResponse> getPriceQuote(
            @RequestParam Long referenceId,
            @RequestParam String bookingType,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) String travelDate
    ) {
        log.info("Received request for price quote: refId={}, bookingType={}, quantity={}, travelDate={}",
                referenceId, bookingType, quantity, travelDate);

        PriceQuoteResponse response = pricingService.getPriceQuote(referenceId, bookingType, quantity, travelDate);
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
