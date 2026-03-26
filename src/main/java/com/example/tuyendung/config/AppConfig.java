package com.example.tuyendung.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    /**
     * Singleton Bean - PasswordEncoder
     */
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

    /**
     * Singleton Bean - RestTemplate cho HTTP calls
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}