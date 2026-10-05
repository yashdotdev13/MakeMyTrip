package com.company.MakeMyTrip.pricing_service.service.Impl;

import com.company.MakeMyTrip.events.BookingDemandEvent;
import com.company.MakeMyTrip.events.BookingDemandEventType;
import com.company.MakeMyTrip.pricing_service.entity.DemandProjection;
import com.company.MakeMyTrip.pricing_service.entity.ProcessedEvent;
import com.company.MakeMyTrip.pricing_service.repository.DemandProjectionRepository;
import com.company.MakeMyTrip.pricing_service.repository.ProcessedEventRepository;
import com.company.MakeMyTrip.pricing_service.service.DemandProjectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemandProjectionServiceImpl implements DemandProjectionService {

    private final DemandProjectionRepository demandProjectionRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Override
    @Transactional
    public void processBookingDemandEvent(BookingDemandEvent event) {

        if (processedEventRepository.existsByEventId(event.eventId())) {
            log.info("Ignoring duplicate booking demand event: eventId={}", event.eventId());
            return;
        }

        LocalDate travelDate = LocalDate.parse(event.travelDate());
        DemandProjection projection = demandProjectionRepository
                .findByReferenceIdAndBookingTypeAndTravelDate(event
                        .referenceId(), event.bookingType(),
                        travelDate).orElseGet(() ->
                        DemandProjection.builder().referenceId(event.referenceId())
                                .bookingType(event.bookingType()).travelDate(travelDate)
                                .currentBookings(0).updatedAt(LocalDateTime.now()).build());

        int currentBookings = projection.getCurrentBookings();
        int quantity = event.quantity();
        if (event.eventType() == BookingDemandEventType.BOOKING_CREATED) {
            currentBookings += quantity;
        } else if (event.eventType() == BookingDemandEventType.BOOKING_CANCELLED) {
            currentBookings = Math.max(0, currentBookings - quantity);
        }
        projection.setCurrentBookings(currentBookings);
        projection.setUpdatedAt(LocalDateTime.now());
        demandProjectionRepository.save(projection);
        processedEventRepository.save(ProcessedEvent.builder()
                .eventId(event.eventId())
                .processedAt(LocalDateTime.now())
                .build());
        log.info("Demand projection updated: eventId={}, referenceId={}, bookingType={}," +
                " travelDate={}, eventType={}, quantity={}, currentBookings={}",
                event.eventId(), event.referenceId(), event.bookingType(), travelDate,
                event.eventType(), quantity, currentBookings);
    }
}