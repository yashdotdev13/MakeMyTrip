package com.company.MakeMyTrip.booking_service.kafka;


import com.company.MakeMyTrip.events.BookingDemandEvent;
import com.company.MakeMyTrip.events.BookingDemandEventType;
import com.company.MakeMyTrip.events.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingDemandEventPublisher {

    private final KafkaTemplate<String, BookingDemandEvent> kafkaTemplate;

    public void publish(
            Long bookingId,
            Long referenceId,
            String bookingType,
            Integer quantity,
            String travelDate,
            BookingDemandEventType eventType
    ) {

        BookingDemandEvent event = new BookingDemandEvent(
                UUID.randomUUID().toString(),
                bookingId,
                referenceId,
                bookingType,
                quantity,
                travelDate,
                eventType,
                Instant.now()
        );

        CompletableFuture<SendResult<String, BookingDemandEvent>> future =
                kafkaTemplate.send(
                        KafkaTopics.BOOKING_DEMAND,
                        bookingId.toString(),
                        event
                );

        future.whenComplete((result, exception) -> {

            if (exception != null) {
                log.error(
                        "Failed to publish booking demand event: bookingId={}, referenceId={}, eventType={}",
                        bookingId,
                        referenceId,
                        eventType,
                        exception
                );
                return;
            }

            log.info(
                    "Booking demand event published successfully: bookingId={}, referenceId={}, eventType={}, topic={}, partition={}, offset={}",
                    bookingId,
                    referenceId,
                    eventType,
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
    }
}