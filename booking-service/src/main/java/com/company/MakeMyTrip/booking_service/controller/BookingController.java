package com.company.MakeMyTrip.booking_service.controller;


import com.company.MakeMyTrip.booking_service.advices.ApiResponse;
import com.company.MakeMyTrip.booking_service.dtos.BookingRequest;
import com.company.MakeMyTrip.booking_service.dtos.BookingResponse;
import com.company.MakeMyTrip.booking_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;


    // create a new booking
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        log.info("Received request to create booking: {}", request);
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.ok(response);
    }


    // ger a booking by id
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable("id") Long bookingId) {
        log.info("Received request to fetch booking with ID: {}", bookingId);
        BookingResponse response = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(response);
    }

    // get all booking for current user
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookingsForUser() {
        log.info("Received request to fetch all bookings for current user");
        List<BookingResponse> responses = bookingService.getBookingByUser();
        return ResponseEntity.ok(responses);
    }

    // update booking

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable("id") Long bookingId,
                                                         @RequestBody BookingRequest request) {
        log.info("Received request to update booking with ID: {}", bookingId);
        BookingResponse response = bookingService.updateBooking(bookingId, request);
        return ResponseEntity.ok(response);
    }

    // cancel booking
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> cancelBooking(@PathVariable Long id) {
        log.info("Received request to cancel booking with ID: {}", id);

        bookingService.cancelBooking(id);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .data("Booking with ID " + id + " cancelled successfully")
                        .build()
        );
    }
}
