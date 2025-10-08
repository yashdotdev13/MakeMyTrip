package com.company.MakeMyTrip.review_service.controller;

import com.company.MakeMyTrip.review_service.advices.ApiResponse;
import com.company.MakeMyTrip.review_service.auth.UserContextHolder;
import com.company.MakeMyTrip.review_service.dtos.ReviewRequest;
import com.company.MakeMyTrip.review_service.dtos.ReviewResponse;
import com.company.MakeMyTrip.review_service.dtos.ReviewSummaryResponse;
import com.company.MakeMyTrip.review_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Create a new review for a booking
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(@RequestBody ReviewRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("User {} is creating a review for bookingId={}", userId, request.getBookingId());

        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    /**
     * Update an existing review (only by owner)
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewRequest request) {

        Long userId = UserContextHolder.getCurrentUserId();
        log.info("User {} is attempting to update reviewId={}", userId, reviewId);

        ReviewResponse response = reviewService.updateReview(reviewId, request);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    /**
     * Get all reviews for a specific booking
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByBookingId(@PathVariable Long bookingId) {
        log.info("Fetching reviews for bookingId={}", bookingId);

        List<ReviewResponse> responses = reviewService.getReviewByBookingId(bookingId);
        return ResponseEntity.ok(new ApiResponse<>(responses));
    }

    /**
     * Get review summary (average rating + total count) for a booking
     */
    @GetMapping("/booking/{bookingId}/summary")
    public ResponseEntity<ApiResponse<ReviewSummaryResponse>> getReviewSummary(@PathVariable Long bookingId) {
        log.info("Fetching review summary for bookingId={}", bookingId);

        ReviewSummaryResponse response = reviewService.getReviewSummaryByBookingId(bookingId);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    /**
     * Get a user's review for a specific booking
     */
    @GetMapping("/booking/{bookingId}/my-review")
    public ResponseEntity<ApiResponse<?>> getMyReviewForBooking(@PathVariable Long bookingId) {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Fetching review for userId={} and bookingId={}", userId, bookingId);

        return reviewService.getReviewByBookingIdAndUserId(bookingId, userId)
                .<ResponseEntity<ApiResponse<?>>>map(review ->
                        ResponseEntity.ok(new ApiResponse<>(review)))
                .orElseGet(() ->
                        ResponseEntity.ok(new ApiResponse<>("No review found for this booking")));
    }
}
