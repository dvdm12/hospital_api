package com.example.miapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtConfig {
    private String secret;
    private int expirationInMs;
    private int refreshExpirationInMs;
    private String cookieName;
}