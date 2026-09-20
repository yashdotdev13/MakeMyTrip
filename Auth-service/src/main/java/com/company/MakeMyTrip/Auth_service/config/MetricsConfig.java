package com.company.MakeMyTrip.Auth_service.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter loginSuccessCounter(MeterRegistry registry) {
        return Counter.builder("auth.login.success")
                .description("Number of successful login attempts")
                .register(registry);
    }

    @Bean
    public Counter loginFailureCounter(MeterRegistry registry) {
        return Counter.builder("auth.login.failure")
                .description("Number of failed login attempts")
                .register(registry);
    }

    @Bean
    public Counter registrationCounter(MeterRegistry registry) {
        return Counter.builder("auth.registration.success")
                .description("Number of successful registrations")
                .register(registry);
    }

    @Bean
    public Counter refreshTokenCounter(MeterRegistry registry) {
        return Counter.builder("auth.refresh.success")
                .description("Number of successful refresh token operations")
                .register(registry);
    }

    @Bean
    public Counter logoutCounter(MeterRegistry registry) {
        return Counter.builder("auth.logout.success")
                .description("Number of successful logout operations")
                .register(registry);
    }
}