package com.company.MakeMyTrip.pricing_service.controller;

import com.company.MakeMyTrip.pricing_service.advices.ApiError;
import com.company.MakeMyTrip.pricing_service.advices.ApiResponse;
import com.company.MakeMyTrip.pricing_service.dtos.PriceLockResponse;
import com.company.MakeMyTrip.pricing_service.dtos.PriceQuoteRequest;
import com.company.MakeMyTrip.pricing_service.dtos.PriceQuoteResponse;
import com.company.MakeMyTrip.pricing_service.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pricing")
@RequiredArgsConstructor
@Slf4j
public class PricingController {

    private final PricingService pricingService;

    @PostMapping("/quote")
    public ResponseEntity<ApiResponse<PriceQuoteResponse>> getPriceQuote(@RequestBody PriceQuoteRequest request) {
        try {
            log.info("Received PriceQuoteRequest: {}", request);
            PriceQuoteResponse response = pricingService.getPriceQuote(
                    request.getReferenceId(),
                    request.getBookingType(),
                    request.getQuantity(),
                    request.getTravelDate()
            );
            return ResponseEntity.ok(new ApiResponse<>(response));
        } catch (Exception e) {
            log.error("Error generating price quote for referenceId {}: {}", request.getReferenceId(), e.getMessage(), e);
            ApiError error = ApiError.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Pricing service returned null adjusted price for referenceId " + request.getReferenceId())
                    .build();
            return ResponseEntity.internalServerError().body(new ApiResponse<>(error));
        }
    }

    @PostMapping("/lock/{referenceId}")
    public ResponseEntity<ApiResponse<PriceLockResponse>> lockPrice(
            @PathVariable Long referenceId,
            @RequestParam String bookingType
    ) {
        try {
            log.info("Locking price for refId={}, bookingType={}", referenceId, bookingType);
            PriceLockResponse response = pricingService.lockPrice(referenceId, bookingType);
            return ResponseEntity.ok(new ApiResponse<>(response));
        } catch (Exception e) {
            log.error("Error locking price for referenceId {}: {}", referenceId, e.getMessage(), e);
            ApiError error = ApiError.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Failed to lock price for referenceId " + referenceId)
                    .build();
            return ResponseEntity.internalServerError().body(new ApiResponse<>(error));
        }
    }

    @DeleteMapping("/lock/{lockId}")
    public ResponseEntity<ApiResponse<Void>> releasePriceLock(@PathVariable Long lockId) {
        try {
            log.info("Releasing price lock with lockId={}", lockId);
            boolean released = pricingService.releasePriceLock(lockId);
            if (released) {
                return ResponseEntity.ok(new ApiResponse<>(null));
            } else {
                ApiError error = ApiError.builder()
                        .status(HttpStatus.NOT_FOUND)
                        .message("Price lock not found with lockId " + lockId)
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(error));
            }
        } catch (Exception e) {
            log.error("Error releasing price lock {}: {}", lockId, e.getMessage(), e);
            ApiError error = ApiError.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Failed to release price lock with lockId " + lockId)
                    .build();
            return ResponseEntity.internalServerError().body(new ApiResponse<>(error));
        }
    }
}
