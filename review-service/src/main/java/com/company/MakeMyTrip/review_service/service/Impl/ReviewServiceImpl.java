package com.company.MakeMyTrip.review_service.service.Impl;


import com.company.MakeMyTrip.review_service.auth.UserContextHolder;
import com.company.MakeMyTrip.review_service.dtos.ReviewRequest;
import com.company.MakeMyTrip.review_service.dtos.ReviewResponse;
import com.company.MakeMyTrip.review_service.dtos.ReviewSummaryResponse;
import com.company.MakeMyTrip.review_service.entity.Review;
import com.company.MakeMyTrip.review_service.repository.ReviewRepository;
import com.company.MakeMyTrip.review_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.ResourceClosedException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ModelMapper modelMapper;

    @Override
    public ReviewResponse createReview(ReviewRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
       log.info("Creating a new review for bookingId={}, userId={}", request.getBookingId(), userId);

       Review review = modelMapper.map(request, Review.class);
       review.setUserId(userId);
       review.setCreatedAt(LocalDateTime.now());
       review.setUpdatedAt(LocalDateTime.now());

       Review savedReview = reviewRepository.save(review);
       log.info("Review created successfully with id={} by userId={}", savedReview.getId(), userId);

       return modelMapper.map(savedReview, ReviewResponse.class);
    }

    @Override
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request) {
       Long userId = UserContextHolder.getCurrentUserId();
       log.info("user {} attempting to update review with id={}", userId, reviewId);

       Review existingReview = reviewRepository.findById(reviewId)
               .orElseThrow(()->new ResourceClosedException("Review not found with id" +reviewId));

       if(!existingReview.getUserId().equals(userId)){
           log.error("Unauthorized attempt: userId={} does not own reviewId={}",userId, reviewId);

           throw new SecurityException("You are not authorized to update this  review");
       }

       existingReview.setRating(request.getRating());
       existingReview.setComment(request.getComment());
       existingReview.setUpdatedAt(LocalDateTime.now());

       Review updatedReview= reviewRepository.save(existingReview);
       log.info("Review updated successfully for reviewId={} by userId={}",reviewId, userId);

       return modelMapper.map(updatedReview, ReviewResponse.class);
    }

    @Override
    public List<ReviewResponse> getReviewByBookingId(Long bookingId) {
        log.info("Fetching all reviews for bookingId={}", bookingId);

        return reviewRepository.findByBookingId(bookingId)
                .stream()
                .map(review->modelMapper.map(review, ReviewResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ReviewResponse> getReviewByBookingIdAndUserId(Long bookingId, Long userId) {
        log.info("Fetching review for bookingId={} and  userId={}",bookingId,userId);

        return reviewRepository.findByBookingIdAndUserId(bookingId, userId)
                .map(review->modelMapper.map(review, ReviewResponse.class));
    }

    @Override
    public ReviewSummaryResponse getReviewSummaryByBookingId(Long bookingId) {
       log.info("Fetching review summary for bookingId={}",bookingId);

       List<Review> reviews = reviewRepository.findByBookingId(bookingId);
       if(reviews.isEmpty()){
           log.warn("No reviews found for booking={}",bookingId);
           return new ReviewSummaryResponse(bookingId, (double) 0, 0.0);
       }

       double avgRating = reviews.stream()
               .mapToInt(Review::getRating)
               .average()
               .orElse(0.0);

        ReviewSummaryResponse summary = new ReviewSummaryResponse(
                bookingId,
                (double) reviews.size(),
                Math.round(avgRating * 10.0) / 10.0
        );

        log.info("Review summary for bookingId={} -> totalReview={}, avgRating={}",
                bookingId, summary.getTotalReviews(), summary.getAverageRating());
        return summary;
    }
}
