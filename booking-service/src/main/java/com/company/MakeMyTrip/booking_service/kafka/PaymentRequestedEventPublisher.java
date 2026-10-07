package com.company.MakeMyTrip.booking_service.kafka;

import com.company.MakeMyTrip.events.KafkaTopics;
import com.company.MakeMyTrip.events.PaymentRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestedEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(
            Long bookingId,
            Long userId,
            BigDecimal amount,
            String paymentMethod
    ) {

        PaymentRequestedEvent event = new PaymentRequestedEvent(
                UUID.randomUUID().toString(),
                bookingId,
                userId,
                amount,
                paymentMethod,
                Instant.now()
        );

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(
                        KafkaTopics.PAYMENT_REQUESTED,
                        bookingId.toString(),
                        event
                );

        future.whenComplete((result, exception) -> {

            if (exception != null) {

                log.error(
                        "Failed to publish payment requested event: bookingId={}, userId={}",
                        bookingId,
                        userId,
                        exception
                );

                return;
            }

            log.info(
                    "Payment requested event published successfully: bookingId={}, userId={}, topic={}, partition={}, offset={}",
                    bookingId,
                    userId,
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
    }
}