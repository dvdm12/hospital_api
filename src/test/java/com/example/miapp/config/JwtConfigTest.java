package com.example.miapp.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "app.jwt.secret=testSecret123",
        "app.jwt.expirationInMs=60000",
        "app.jwt.refreshExpirationInMs=120000",
        "app.jwt.cookieName=test_jwt_token"
})
class JwtConfigTest {

    @Autowired
    private JwtConfig jwtConfig;

    @Test
    void testJwtConfigProperties() {
        assertEquals("testSecret123", jwtConfig.getSecret());
        assertEquals(60000, jwtConfig.getExpirationInMs());
        assertEquals(120000, jwtConfig.getRefreshExpirationInMs());
        assertEquals("test_jwt_token", jwtConfig.getCookieName());
    }
}