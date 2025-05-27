package com.example.miapp.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {JwtConfig.class})
@EnableConfigurationProperties(JwtConfig.class)
@TestPropertySource(properties = {
    "app.jwt.secret=testSecretKey123456789012345678901234567890",
    "app.jwt.expiration-in-ms=3600000",
    "app.jwt.refresh-expiration-in-ms=86400000",
    "app.jwt.cookie-name=test_jwt_token"
})
class JwtConfigTest {

    @Autowired
    private JwtConfig jwtConfig;

    @Test
    void jwtConfig_ShouldLoadProperties() {
        // Verificar que la configuración se carga correctamente
        assertNotNull(jwtConfig);
        assertEquals("testSecretKey123456789012345678901234567890", jwtConfig.getSecret());
        assertEquals(3600000, jwtConfig.getExpirationInMs());
        assertEquals(86400000, jwtConfig.getRefreshExpirationInMs());
        assertEquals("test_jwt_token", jwtConfig.getCookieName());
    }

    @Test
    void jwtConfig_SecretShouldNotBeEmpty() {
        // Verificar que el secret no esté vacío
        assertNotNull(jwtConfig.getSecret());
        assertFalse(jwtConfig.getSecret().isEmpty());
        assertTrue(jwtConfig.getSecret().length() >= 32, "JWT Secret debe tener al menos 32 caracteres");
    }

    @Test
    void jwtConfig_ExpirationShouldBePositive() {
        // Verificar que los tiempos de expiración sean positivos
        assertTrue(jwtConfig.getExpirationInMs() > 0, "Expiration time debe ser positivo");
        assertTrue(jwtConfig.getRefreshExpirationInMs() > 0, "Refresh expiration time debe ser positivo");
    }

    @Test
    void jwtConfig_RefreshExpirationShouldBeLongerThanExpiration() {
        // Verificar que el refresh token dure más que el token normal
        assertTrue(jwtConfig.getRefreshExpirationInMs() > jwtConfig.getExpirationInMs(),
            "Refresh expiration debe ser mayor que expiration normal");
    }

    @Test
    void jwtConfig_CookieNameShouldNotBeEmpty() {
        // Verificar que el nombre de la cookie no esté vacío
        assertNotNull(jwtConfig.getCookieName());
        assertFalse(jwtConfig.getCookieName().isEmpty());
    }
}