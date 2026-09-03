package com.ecommerce.common.mq.config;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMQ configuration.
 * <p>
 * Relies on spring-boot-starter-rocketmq auto-configuration.
 * This class serves as an extension point for custom producer / consumer beans
 * if needed in the future.
 */
@Configuration
@AutoConfigureAfter(RocketMQAutoConfiguration.class)
public class RocketMQConfig {
    // Default auto-configuration is sufficient for basic usage.
    // Custom RocketMQTemplate beans can be added here when needed.
}
