package com.company.MakeMyTrip.pricing_service.controller;

import com.company.MakeMyTrip.pricing_service.advices.ApiResponse;
import com.company.MakeMyTrip.pricing_service.dtos.BasePriceRequest;
import com.company.MakeMyTrip.pricing_service.dtos.BasePriceResponse;
import com.company.MakeMyTrip.pricing_service.service.BasePriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pricing/base-prices")
@RequiredArgsConstructor
@Slf4j
public class BasePriceController {

    private final BasePriceService basePriceService;

    @PostMapping
    public ResponseEntity<ApiResponse<BasePriceResponse>> createBasePrice(@Valid @RequestBody
                                                                              BasePriceRequest request) {

        log.info("Received base price creation request: referenceId={}, bookingType={}",
                request.getReferenceId(), request.getBookingType());
        BasePriceResponse response = basePriceService.createBasePrice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(response));
    }

    @PutMapping("/{basePriceId}")
    public ResponseEntity<ApiResponse<BasePriceResponse>> updateBasePrice(
            @PathVariable Long basePriceId, @Valid @RequestBody BasePriceRequest request) {

        log.info("Received base price update request: basePriceId={}", basePriceId);
        BasePriceResponse response = basePriceService.updateBasePrice(basePriceId, request);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @GetMapping("/{basePriceId}")
    public ResponseEntity<ApiResponse<BasePriceResponse>> getBasePrice(@PathVariable Long basePriceId) {
        BasePriceResponse response = basePriceService.getBasePriceById(basePriceId);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @DeleteMapping("/{basePriceId}")
    public ResponseEntity<ApiResponse<Void>> deactivateBasePrice(@PathVariable Long basePriceId) {
        log.info("Received base price deactivation request: basePriceId={}", basePriceId);
        basePriceService.deactivateBasePrice(basePriceId);
        return ResponseEntity.ok(new ApiResponse<>(null));
    }
}