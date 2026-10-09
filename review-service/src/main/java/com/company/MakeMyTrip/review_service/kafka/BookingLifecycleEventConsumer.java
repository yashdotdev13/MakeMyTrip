package com.company.MakeMyTrip.review_service.kafka;

import com.company.MakeMyTrip.events.BookingLifecycleEvent;
import com.company.MakeMyTrip.events.KafkaTopics;
import com.company.MakeMyTrip.review_service.service.BookingProjectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingLifecycleEventConsumer {

    private final BookingProjectionService projectionService;

    @KafkaListener(
            topics = KafkaTopics.BOOKING_LIFECYCLE,
            containerFactory = "bookingLifecycleKafkaListenerContainerFactory"
    )
    public void consume(BookingLifecycleEvent event) {
        log.info(
                "Received booking lifecycle event: bookingId={}, userId={}, status={}",
                event.bookingId(),
                event.userId(),
                event.status()
        );

        projectionService.process(event);
    }
}