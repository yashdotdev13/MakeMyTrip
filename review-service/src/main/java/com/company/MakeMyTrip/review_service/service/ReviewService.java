package com.company.MakeMyTrip.review_service.service;

import com.company.MakeMyTrip.review_service.dtos.ReviewRequest;
import com.company.MakeMyTrip.review_service.dtos.ReviewResponse;
import com.company.MakeMyTrip.review_service.dtos.ReviewSummaryResponse;

import java.util.List;
import java.util.Optional;

public interface ReviewService {

    ReviewResponse createReview(ReviewRequest request);
    ReviewResponse updateReview(Long reviewId, ReviewRequest request);

    List<ReviewResponse> getReviewByBookingId(Long bookingId);

    // Fetch a review by user for a booking (optional)
    Optional<ReviewResponse> getReviewByBookingIdAndUserId(Long bookingId, Long userId);

    // Fetch review summary for a booking (average rating + total reviews)
    ReviewSummaryResponse getReviewSummaryByBookingId(Long bookingId);

}
