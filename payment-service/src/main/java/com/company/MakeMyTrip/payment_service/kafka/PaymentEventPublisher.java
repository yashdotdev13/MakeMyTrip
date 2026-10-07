package com.company.MakeMyTrip.payment_service.kafka;

import com.company.MakeMyTrip.events.KafkaTopics;
import com.company.MakeMyTrip.events.PaymentCompletedEvent;
import com.company.MakeMyTrip.events.PaymentFailedEvent;
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
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentCompleted(
            Long paymentId,
            Long bookingId,
            Long userId,
            BigDecimal amount,
            String transactionId
    ) {

        PaymentCompletedEvent event = new PaymentCompletedEvent(
                UUID.randomUUID().toString(),
                paymentId,
                bookingId,
                userId,
                amount,
                transactionId,
                Instant.now()
        );

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(
                        KafkaTopics.PAYMENT_COMPLETED,
                        bookingId.toString(),
                        event
                );

        future.whenComplete((result, exception) -> {

            if (exception != null) {
                log.error(
                        "Failed to publish payment completed event: paymentId={}, bookingId={}, transactionId={}",
                        paymentId,
                        bookingId,
                        transactionId,
                        exception
                );
                return;
            }

            log.info(
                    "Payment completed event published successfully: paymentId={}, bookingId={}, topic={}, partition={}, offset={}",
                    paymentId,
                    bookingId,
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
    }

    public void publishPaymentFailed(
            Long bookingId,
            Long userId,
            BigDecimal amount,
            String reason
    ) {

        PaymentFailedEvent event = new PaymentFailedEvent(
                UUID.randomUUID().toString(),
                bookingId,
                userId,
                amount,
                reason,
                Instant.now()
        );

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(
                        KafkaTopics.PAYMENT_FAILED,
                        bookingId.toString(),
                        event
                );

        future.whenComplete((result, exception) -> {

            if (exception != null) {
                log.error(
                        "Failed to publish payment failed event: bookingId={}, reason={}",
                        bookingId,
                        reason,
                        exception
                );
                return;
            }

            log.info(
                    "Payment failed event published successfully: bookingId={}, topic={}, partition={}, offset={}",
                    bookingId,
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
    }
}