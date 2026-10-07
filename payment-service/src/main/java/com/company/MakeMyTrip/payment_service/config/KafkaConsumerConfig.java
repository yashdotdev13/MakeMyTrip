package com.company.MakeMyTrip.payment_service.config;

import com.company.MakeMyTrip.events.PaymentRequestedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, PaymentRequestedEvent> paymentRequestedConsumerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092"
        );

        config.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "payment-service"
        );

        config.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "latest"
        );

        config.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        config.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        config.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class
        );

        config.put(
                JsonDeserializer.TRUSTED_PACKAGES,
                "com.company.MakeMyTrip.events"
        );

        config.put(
                JsonDeserializer.USE_TYPE_INFO_HEADERS,
                false
        );

        JsonDeserializer<PaymentRequestedEvent> deserializer =
                new JsonDeserializer<>(PaymentRequestedEvent.class);

        deserializer.addTrustedPackages(
                "com.company.MakeMyTrip.events"
        );

        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentRequestedEvent>
    paymentRequestedKafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentRequestedEvent> consumerFactory
    ) {

        ConcurrentKafkaListenerContainerFactory<String, PaymentRequestedEvent>
                factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }
}