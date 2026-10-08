package com.company.MakeMyTrip.notification_service.kafka;

import com.company.MakeMyTrip.events.PaymentCompletedEvent;
import com.company.MakeMyTrip.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCompletedEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "payment.completed",
            containerFactory = "paymentCompletedKafkaListenerContainerFactory"
    )
    public void consume(PaymentCompletedEvent event) {

        log.info(
                "Received payment completed event: paymentId={}, bookingId={}, userId={}",
                event.paymentId(),
                event.bookingId(),
                event.userId()
        );

        notificationService.sendPaymentCompletedEmail(event);
    }
}