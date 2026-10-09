package com.company.MakeMyTrip.review_service.service;

import com.company.MakeMyTrip.events.BookingLifecycleEvent;
import com.company.MakeMyTrip.review_service.entity.BookingProjection;
import com.company.MakeMyTrip.review_service.repository.BookingProjectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingProjectionService {

    private final BookingProjectionRepository repository;

    @Transactional
    public void process(BookingLifecycleEvent event) {
        BookingProjection projection = repository
                .findById(event.bookingId())
                .orElseGet(() -> BookingProjection.builder()
                        .bookingId(event.bookingId())
                        .build());

        // Don't let an older event overwrite newer state.
        if (projection.getOccurredAt() != null
                && !event.occurredAt().isAfter(projection.getOccurredAt())) {
            return;
        }

        projection.setUserId(event.userId());
        projection.setBookingType(event.bookingType());
        projection.setStatus(event.status());
        projection.setOccurredAt(event.occurredAt());

        repository.save(projection);
    }
}