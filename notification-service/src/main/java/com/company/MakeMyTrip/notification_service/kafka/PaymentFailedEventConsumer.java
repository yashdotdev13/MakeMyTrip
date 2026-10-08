package com.company.MakeMyTrip.notification_service.kafka;

import com.company.MakeMyTrip.events.PaymentFailedEvent;
import com.company.MakeMyTrip.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFailedEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "payment.failed",
            containerFactory = "paymentFailedKafkaListenerContainerFactory"
    )
    public void consume(PaymentFailedEvent event) {

        log.info(
                "Received payment failed event: bookingId={}, userId={}, reason={}",
                event.bookingId(),
                event.userId(),
                event.reason()
        );

        notificationService.sendPaymentFailedEmail(event);
    }
}