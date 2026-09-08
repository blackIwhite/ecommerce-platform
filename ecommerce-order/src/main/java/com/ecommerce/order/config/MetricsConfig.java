package com.ecommerce.order.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter orderCreatedCounter(MeterRegistry registry) {
        return Counter.builder("order.created")
                .description("Number of orders created")
                .register(registry);
    }

    @Bean
    public Counter orderPaidCounter(MeterRegistry registry) {
        return Counter.builder("order.paid")
                .description("Number of orders paid")
                .register(registry);
    }

    @Bean
    public Counter orderCancelledCounter(MeterRegistry registry) {
        return Counter.builder("order.cancelled")
                .description("Number of orders cancelled")
                .register(registry);
    }

    @Bean
    public Timer orderProcessingTimer(MeterRegistry registry) {
        return Timer.builder("order.processing.time")
                .description("Time taken to process orders")
                .register(registry);
    }
}
