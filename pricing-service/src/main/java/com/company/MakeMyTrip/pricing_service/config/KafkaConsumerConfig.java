package com.company.MakeMyTrip.pricing_service.config;

import com.company.MakeMyTrip.events.BookingDemandEvent;
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
    public ConsumerFactory<String, BookingDemandEvent> consumerFactory() {

        JsonDeserializer<BookingDemandEvent> deserializer = new JsonDeserializer<>(BookingDemandEvent.class);
        deserializer.addTrustedPackages("com.company.MakeMyTrip.events");
        Map<String, Object> properties = new HashMap<>();
        properties.put("bootstrap.servers", "localhost:9092");
        properties.put("group.id", "pricing-service");
        properties.put("auto.offset.reset", "latest");
        properties.put("enable.auto.commit", false);
        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BookingDemandEvent>
    kafkaListenerContainerFactory(ConsumerFactory<String, BookingDemandEvent> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, BookingDemandEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}