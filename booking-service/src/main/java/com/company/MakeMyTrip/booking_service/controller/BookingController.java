package com.company.MakeMyTrip.booking_service.controller;

import com.company.MakeMyTrip.booking_service.advices.ApiResponse;
import com.company.MakeMyTrip.booking_service.dtos.BookingConfirmationRequest;
import com.company.MakeMyTrip.booking_service.dtos.BookingConfirmationResponse;
import com.company.MakeMyTrip.booking_service.dtos.BookingCountResponse;
import com.company.MakeMyTrip.booking_service.dtos.BookingRequest;
import com.company.MakeMyTrip.booking_service.dtos.BookingResponse;
import com.company.MakeMyTrip.booking_service.service.BookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        log.info("Received request to create booking");
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable("id") @NotNull @Positive Long bookingId) {
        log.info("Received request to fetch booking bookingId={}", bookingId);
        BookingResponse response = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookingsForUser() {
        log.info("Received request to fetch bookings for current user");
        List<BookingResponse> responses = bookingService.getBookingByUser();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable("id") @NotNull @Positive Long bookingId,
                                                         @Valid @RequestBody BookingRequest request) {
        log.info("Received request to update booking bookingId={}", bookingId);
        BookingResponse response = bookingService.updateBooking(bookingId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> cancelBooking(@PathVariable("id") @NotNull @Positive Long bookingId) {
        log.info("Received request to cancel booking bookingId={}", bookingId);
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.<String>builder().data("Booking with ID "
                + bookingId + " cancelled successfully").build());
    }

    @GetMapping("/count")
    public ResponseEntity<BookingCountResponse> getBookingCount(@RequestParam @NotNull @Positive Long referenceId,
                                                                @RequestParam String travelDate) {
        log.info("Getting booking count referenceId={} travelDate={}", referenceId, travelDate);
        BookingCountResponse response = bookingService.getBookingCount(referenceId, travelDate);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<BookingConfirmationResponse> confirmBooking(@Valid @RequestBody
                                                                          BookingConfirmationRequest request) {
        log.info("Received request to confirm booking bookingId={}", request.getBookingId());
        BookingConfirmationResponse response = bookingService.confirmBooking(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/internal/{id}")
    public ResponseEntity<BookingResponse> getBookingByIdInternal(@PathVariable("id") @NotNull @Positive
                                                                      Long bookingId) {
        log.info("Internal request to fetch booking bookingId={}", bookingId);
        BookingResponse response = bookingService.getBookingByIdInternal(bookingId);
        return ResponseEntity.ok(response);
    }
}