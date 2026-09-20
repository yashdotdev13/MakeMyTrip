package com.company.MakeMyTrip.Auth_service.service;


import com.company.MakeMyTrip.events.KafkaTopics;
import com.company.MakeMyTrip.events.UserLoggedInEvent;
import com.company.MakeMyTrip.events.UserLoggedOutEvent;
import com.company.MakeMyTrip.events.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserRegistered(UserRegisteredEvent event) {
        publish(
                KafkaTopics.USER_REGISTERED,
                event.userId().toString(),
                event
        );
    }
    public void publishUserLoggedIn(UserLoggedInEvent event) {
        publish(
                KafkaTopics.USER_LOGGED_IN,
                event.userId().toString(),
                event
        );
    }
    public void publishUserLoggedOut(UserLoggedOutEvent event) {
        publish(
                KafkaTopics.USER_LOGGED_OUT,
                event.userId().toString(),
                event
        );
    }
    private void publish(String topic, String key, Object event) {

        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, exception) -> {

                    if (exception != null) {
                        log.error(
                                "Failed to publish Kafka event. topic={} key={}",
                                topic,
                                key,
                                exception
                        );
                        return;
                    }

                    log.info(
                            "Kafka event published successfully. topic={} key={} partition={} offset={}",
                            topic,
                            key,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                });
    }
}