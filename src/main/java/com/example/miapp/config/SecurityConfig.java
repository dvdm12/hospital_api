package com.example.miapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security configuration class for the application.
 * Provides beans related to security features.
 */
@Configuration
public class SecurityConfig {

    /**
     * Creates a BCrypt password encoder bean.
     * This encoder is used for secure password hashing when creating or validating user credentials.
     *
     * @return the password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}