package com.company.MakeMyTrip.payment_service.kafka;

import com.company.MakeMyTrip.events.KafkaTopics;
import com.company.MakeMyTrip.events.PaymentRequestedEvent;
import com.company.MakeMyTrip.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestedEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_REQUESTED,
            containerFactory = "paymentRequestedKafkaListenerContainerFactory"
    )
    public void consume(PaymentRequestedEvent event) {

        log.info(
                "Received payment request: bookingId={}, userId={}, amount={}",
                event.bookingId(),
                event.userId(),
                event.amount()
        );

        paymentService.initiatePayment(event);
    }
}