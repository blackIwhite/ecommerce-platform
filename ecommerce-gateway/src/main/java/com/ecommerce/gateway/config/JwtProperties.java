package com.ecommerce.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT configuration properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Secret key for signing JWT tokens (must be at least 256 bits for HS256).
     */
    private String secret;

    /**
     * Token expiration in milliseconds. Default is 24 hours.
     */
    private long expiration = 86400000L;
}
