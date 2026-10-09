package com.company.MakeMyTrip.booking_service.kafka;


import com.company.MakeMyTrip.events.BookingLifecycleEvent;
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
public class BookingLifecycleEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(
            Long bookingId,
            Long userId,
            String bookingType,
            String status
    ) {
        BookingLifecycleEvent event = new BookingLifecycleEvent(
                UUID.randomUUID().toString(),
                bookingId,
                userId,
                bookingType,
                status,
                Instant.now()
        );

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(
                        KafkaTopics.BOOKING_LIFECYCLE,
                        bookingId.toString(),
                        event
                );

        future.whenComplete((result, exception) -> {
            if (exception != null) {
                log.error(
                        "Failed to publish booking lifecycle event: bookingId={}, status={}",
                        bookingId,
                        status,
                        exception
                );
            } else {
                log.info(
                        "Published booking lifecycle event: bookingId={}, status={}, topic={}, partition={}, offset={}",
                        bookingId,
                        status,
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            }
        });
    }
}