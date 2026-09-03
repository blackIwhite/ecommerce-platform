package com.ecommerce.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class AuthProperties {

    private String secret;

    /** Access token expiration in milliseconds. Default 2 hours. */
    private long accessExpiration = 7200000L;

    /** Refresh token expiration in milliseconds. Default 7 days. */
    private long refreshExpiration = 604800000L;
}
