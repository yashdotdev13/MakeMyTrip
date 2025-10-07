package com.company.MakeMyTrip.review_service.repository;

import com.company.MakeMyTrip.review_service.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review,Long> {

    List<Review> findByBookingId(Long bookingId);

    Optional<Review> findByBookingIdAndUserId(Long bookingId, Long userId);


    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.bookingId = :bookingId")
    Double findAverageRatingByBookingId(Long bookingId);

    @Query("SELECT COUNT(r.id) FROM Review r WHERE r.bookingId = :bookingId")
    Long countByBookingId(Long bookingId);
}
