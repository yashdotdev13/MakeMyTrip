package com.company.MakeMyTrip.pricing_service.kafka;

import com.company.MakeMyTrip.events.BookingDemandEvent;
import com.company.MakeMyTrip.events.KafkaTopics;
import com.company.MakeMyTrip.pricing_service.service.DemandProjectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingDemandConsumer {

    private final DemandProjectionService demandProjectionService;

    @KafkaListener(
            topics = KafkaTopics.BOOKING_DEMAND
    )
    public void consume(BookingDemandEvent event) {

        log.info(
                "Received booking demand event: bookingId={}, referenceId={}, eventType={}",
                event.bookingId(),
                event.referenceId(),
                event.eventType()
        );

        demandProjectionService.processBookingDemandEvent(event);
    }
}