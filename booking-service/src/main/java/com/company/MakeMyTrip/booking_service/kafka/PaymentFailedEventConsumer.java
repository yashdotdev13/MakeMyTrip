package com.company.MakeMyTrip.booking_service.kafka;

import com.company.MakeMyTrip.booking_service.service.BookingService;
import com.company.MakeMyTrip.events.KafkaTopics;
import com.company.MakeMyTrip.events.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFailedEventConsumer {

    private final BookingService bookingService;

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED,
            containerFactory = "paymentFailedKafkaListenerContainerFactory"
    )
    public void consume(PaymentFailedEvent event) {

        log.warn(
                "Received payment failed event: bookingId={}, reason={}",
                event.bookingId(),
                event.reason()
        );

        bookingService.handlePaymentFailed(event);
    }
}