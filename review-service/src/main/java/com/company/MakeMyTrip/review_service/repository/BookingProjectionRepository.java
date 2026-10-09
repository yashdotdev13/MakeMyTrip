package com.company.MakeMyTrip.review_service.repository;

import com.company.MakeMyTrip.review_service.entity.BookingProjection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingProjectionRepository
        extends JpaRepository<BookingProjection, Long> {
}