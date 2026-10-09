package com.company.MakeMyTrip.review_service.service.Impl;

import com.company.MakeMyTrip.review_service.auth.UserContextHolder;
import com.company.MakeMyTrip.review_service.dtos.ReviewRequest;
import com.company.MakeMyTrip.review_service.dtos.ReviewResponse;
import com.company.MakeMyTrip.review_service.dtos.ReviewSummaryResponse;
import com.company.MakeMyTrip.review_service.entity.BookingProjection;
import com.company.MakeMyTrip.review_service.entity.Review;
import com.company.MakeMyTrip.review_service.exceptions.AccessDeniedException;
import com.company.MakeMyTrip.review_service.exceptions.ResourceNotFoundException;
import com.company.MakeMyTrip.review_service.exceptions.RuntimeConflictException;
import com.company.MakeMyTrip.review_service.repository.BookingProjectionRepository;
import com.company.MakeMyTrip.review_service.repository.ReviewRepository;
import com.company.MakeMyTrip.review_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingProjectionRepository bookingProjectionRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {

        Long userId = UserContextHolder.getCurrentUserId();

        if (userId == null) {
            throw new AccessDeniedException(
                    "Authenticated user ID is missing"
            );
        }

        Long bookingId = request.getBookingId();

        log.info(
                "Creating review: userId={}, bookingId={}",
                userId,
                bookingId
        );

        BookingProjection booking = bookingProjectionRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found in local projection: " + bookingId
                ));

        if (!userId.equals(booking.getUserId())) {
            log.warn(
                    "Review creation denied: userId={}, bookingId={}",
                    userId,
                    bookingId
            );

            throw new AccessDeniedException(
                    "You are not allowed to review this booking"
            );
        }

        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new RuntimeConflictException(
                    "Reviews can only be created for confirmed bookings"
            );
        }

        if (reviewRepository
                .findByBookingIdAndUserId(bookingId, userId)
                .isPresent()) {

            throw new RuntimeConflictException(
                    "You have already reviewed this booking"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        Review review = Review.builder()
                .bookingId(bookingId)
                .userId(userId)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .createdAt(now)
                .updatedAt(now)
                .build();

        try {
            Review savedReview = reviewRepository.saveAndFlush(review);

            log.info(
                    "Review created: reviewId={}, bookingId={}, userId={}",
                    savedReview.getId(),
                    bookingId,
                    userId
            );

            return modelMapper.map(savedReview, ReviewResponse.class);

        } catch (DataIntegrityViolationException exception) {
            // The unique database constraint also protects against concurrent
            // requests that pass the application-level existence check.
            log.warn(
                    "Duplicate review rejected by database: bookingId={}, userId={}",
                    bookingId,
                    userId
            );

            throw new RuntimeConflictException(
                    "You have already reviewed this booking"
            );
        }
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(
            Long reviewId,
            ReviewRequest request
    ) {

        Long userId = UserContextHolder.getCurrentUserId();

        if (userId == null) {
            throw new AccessDeniedException(
                    "Authenticated user ID is missing"
            );
        }

        log.info(
                "Updating review: reviewId={}, userId={}",
                reviewId,
                userId
        );

        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review not found with id: " + reviewId
                ));

        if (!userId.equals(existingReview.getUserId())) {
            log.warn(
                    "Review update denied: reviewId={}, userId={}",
                    reviewId,
                    userId
            );

            throw new AccessDeniedException(
                    "You are not authorized to update this review"
            );
        }

        existingReview.setRating(request.getRating());
        existingReview.setTitle(request.getTitle());
        existingReview.setComment(request.getComment());
        existingReview.setUpdatedAt(LocalDateTime.now());

        Review updatedReview = reviewRepository.save(existingReview);

        log.info("Review updated: reviewId={}", reviewId);

        return modelMapper.map(updatedReview, ReviewResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewByBookingId(Long bookingId) {

        log.info("Fetching reviews for bookingId={}", bookingId);

        return reviewRepository.findByBookingId(bookingId)
                .stream()
                .map(review -> modelMapper.map(review, ReviewResponse.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewResponse> getReviewByBookingIdAndUserId(
            Long bookingId,
            Long userId
    ) {

        log.info(
                "Fetching review for bookingId={}, userId={}",
                bookingId,
                userId
        );

        return reviewRepository.findByBookingIdAndUserId(bookingId, userId)
                .map(review -> modelMapper.map(review, ReviewResponse.class));
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewSummaryResponse getReviewSummaryByBookingId(
            Long bookingId
    ) {

        log.info("Fetching review summary for bookingId={}", bookingId);

        Double averageRating =
                reviewRepository.findAverageRatingByBookingId(bookingId);

        Long totalReviews =
                reviewRepository.countByBookingId(bookingId);

        double normalizedAverage = averageRating == null
                ? 0.0
                : Math.round(averageRating * 10.0) / 10.0;

        double normalizedCount = totalReviews == null
                ? 0.0
                : totalReviews.doubleValue();

        ReviewSummaryResponse summary = new ReviewSummaryResponse(
                bookingId,
                normalizedAverage,
                normalizedCount
        );

        log.info(
                "Review summary: bookingId={}, averageRating={}, totalReviews={}",
                bookingId,
                summary.getAverageRating(),
                summary.getTotalReviews()
        );

        return summary;
    }
}
