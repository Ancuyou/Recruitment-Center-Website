package com.example.tuyendung.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for JWT token management.
 * Maps the 'jwt.*' properties from application.yaml
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * Secret key for signing JWT tokens.
     * Should be kept secure and not exposed.
     */
    private String secret;
    
    /**
     * Token expiration time in milliseconds.
     * Default: 24 hours (86400000 ms)
     */
    private long expiration;
    
    /**
     * Refresh token expiration time in milliseconds.
     * Default: 7 days (604800000 ms)
     */
    private Long refreshExpiration;
}
