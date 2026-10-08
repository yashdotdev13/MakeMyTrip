package com.company.MakeMyTrip.notification_service.kafka;

import com.company.MakeMyTrip.events.UserRegisteredEvent;
import com.company.MakeMyTrip.notification_service.service.NotificationContactStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegisteredEventConsumer {

    private final NotificationContactStore notificationContactStore;

    @KafkaListener(
            topics = "user.registered",
            groupId = "notification-service"
    )
    public void consume(UserRegisteredEvent event) {

        log.info(
                "Received user registered event: userId={}, email={}",
                event.userId(),
                event.email()
        );

        notificationContactStore.saveEmail(
                event.userId(),
                event.email()
        );

        log.info(
                "Notification contact stored: userId={}",
                event.userId()
        );
    }
}