package com.example.tuyendung.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Authentication-related lifecycles.
 * Maps the 'auth.*' properties from application.yaml
 */
@Data
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    
    /**
     * Verification token expiration time in hours.
     * Default: 24 hours
     */
    private int verificationExpirationHours = 24;

    /**
     * Password reset token expiration time in minutes.
     * Default: 15 minutes
     */
    private int resetExpirationMinutes = 15;
}
