package com.example.miapp.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitario para SecurityConfig
 * Se enfoca en testear la lógica específica sin dependencias complejas de Spring
 */
@DisplayName("SecurityConfig - Pruebas Unitarias")
class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        // Inicializar SecurityConfig con mocks nulos (solo necesitamos passwordEncoder)
        securityConfig = new SecurityConfig(null, null, null);
    }

    @Test
    @DisplayName("passwordEncoder() debe retornar BCryptPasswordEncoder")
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
        // Ejecutar
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // Verificar
        assertNotNull(passwordEncoder, "El passwordEncoder no debe ser null");
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder, 
                "El passwordEncoder debe ser una instancia de BCryptPasswordEncoder");
    }

    @Test
    @DisplayName("passwordEncoder() debe codificar contraseñas correctamente")
    void passwordEncoder_ShouldEncodePasswordsCorrectly() {
        // Preparar
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // Ejecutar
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Verificar
        assertNotNull(encodedPassword, "La contraseña codificada no debe ser null");
        assertNotEquals(rawPassword, encodedPassword, "La contraseña codificada debe ser diferente a la original");
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword), 
                "El passwordEncoder debe validar correctamente la contraseña original");
        
        // Verificar formato BCrypt (debe empezar con $2a$, $2b$, o $2y$)
        assertTrue(encodedPassword.startsWith("$2a$") || 
                  encodedPassword.startsWith("$2b$") || 
                  encodedPassword.startsWith("$2y$"), 
                "La contraseña codificada debe tener formato BCrypt válido");
    }

    @Test
    @DisplayName("passwordEncoder() debe producir hashes diferentes para la misma contraseña")
    void passwordEncoder_ShouldProduceDifferentHashesForSamePassword() {
        // Preparar
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String password = "samePassword123";

        // Ejecutar
        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);

        // Verificar que cada hash es único (gracias al salt de BCrypt)
        assertNotEquals(hash1, hash2, "BCrypt debe generar hashes únicos para la misma contraseña");
        
        // Pero ambos deben validar el password original
        assertTrue(passwordEncoder.matches(password, hash1), "El primer hash debe validar la contraseña");
        assertTrue(passwordEncoder.matches(password, hash2), "El segundo hash debe validar la contraseña");
    }

    @Test
    @DisplayName("passwordEncoder() debe rechazar contraseñas incorrectas")
    void passwordEncoder_ShouldRejectWrongPasswords() {
        // Preparar
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String correctPassword = "correctPassword123";
        String wrongPassword = "wrongPassword456";

        // Ejecutar
        String hash = passwordEncoder.encode(correctPassword);

        // Verificar contraseñas correctas e incorrectas
        assertTrue(passwordEncoder.matches(correctPassword, hash), 
                "Debe validar la contraseña correcta");
        assertFalse(passwordEncoder.matches(wrongPassword, hash), 
                "Debe rechazar contraseñas incorrectas");
        assertFalse(passwordEncoder.matches("", hash), 
                "Debe rechazar contraseñas vacías cuando la original no lo era");
        assertFalse(passwordEncoder.matches("123", hash), 
                "Debe rechazar contraseñas completamente diferentes");
    }

    @Test
    @DisplayName("passwordEncoder() debe rechazar null en matches()")
    void passwordEncoder_ShouldRejectNullInMatches() {
        // Preparar
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String correctPassword = "correctPassword123";
        String hash = passwordEncoder.encode(correctPassword);

        // Verificar que lanza excepción para null
        assertThrows(IllegalArgumentException.class, () -> {
            passwordEncoder.matches(null, hash);
        }, "Debe lanzar IllegalArgumentException para contraseña null en matches()");
    }

    @Test
    @DisplayName("passwordEncoder() debe manejar caracteres especiales")
    void passwordEncoder_ShouldHandleSpecialCharacters() {
        // Preparar
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String specialPassword = "P@ssw0rd!#$%^&*()_+-=[]{}|;:,.<>?";

        // Ejecutar
        String hash = passwordEncoder.encode(specialPassword);

        // Verificar
        assertNotNull(hash, "El hash de contraseña con caracteres especiales no debe ser null");
        assertTrue(passwordEncoder.matches(specialPassword, hash), 
                "Debe manejar correctamente contraseñas con caracteres especiales");
    }

    @Test
    @DisplayName("passwordEncoder() debe manejar contraseñas de longitud límite")
    void passwordEncoder_ShouldHandleLimitLengthPasswords() {
        // Preparar
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        
        // BCrypt maneja bien contraseñas hasta 72 bytes
        String longPassword = "a".repeat(70) + "12"; // 72 caracteres exactos

        // Ejecutar
        String hash = passwordEncoder.encode(longPassword);

        // Verificar
        assertNotNull(hash, "El hash de contraseña larga no debe ser null");
        assertTrue(passwordEncoder.matches(longPassword, hash), 
                "Debe manejar correctamente contraseñas de longitud límite");
    }

    @Test
    @DisplayName("passwordEncoder() debe manejar contraseñas vacías")
    void passwordEncoder_ShouldHandleEmptyPassword() {
        // Preparar
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String emptyPassword = "";

        // Ejecutar - BCrypt puede manejar contraseñas vacías
        String hash = passwordEncoder.encode(emptyPassword);

        // Verificar
        assertNotNull(hash, "El hash de contraseña vacía no debe ser null");
        assertTrue(passwordEncoder.matches(emptyPassword, hash), 
                "Debe manejar correctamente contraseñas vacías");
        assertFalse(passwordEncoder.matches("noEmpty", hash), 
                "No debe validar contraseñas no vacías contra hash de contraseña vacía");
    }

    @Test
    @DisplayName("passwordEncoder() debe rechazar null en encode()")
    void passwordEncoder_ShouldRejectNullInEncode() {
        // Preparar
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // Verificar que lanza excepción para password null en encode
        assertThrows(IllegalArgumentException.class, () -> {
            passwordEncoder.encode(null);
        }, "Debe lanzar IllegalArgumentException para contraseña null en encode()");
    }

    @Test
    @DisplayName("passwordEncoder() debe ser consistente en múltiples llamadas")
    void passwordEncoder_ShouldBeConsistentAcrossMultipleCalls() {
        // Preparar
        PasswordEncoder encoder1 = securityConfig.passwordEncoder();
        PasswordEncoder encoder2 = securityConfig.passwordEncoder();
        String password = "testConsistency123";

        // Ejecutar
        String hash1 = encoder1.encode(password);
        String hash2 = encoder2.encode(password);

        // Verificar que ambos encoders son del mismo tipo y funcionan correctamente
        assertEquals(encoder1.getClass(), encoder2.getClass(), 
                "Múltiples llamadas deben retornar el mismo tipo de encoder");
        
        // Cada encoder debe poder validar hashes del otro
        assertTrue(encoder1.matches(password, hash2), 
                "El primer encoder debe validar hash del segundo");
        assertTrue(encoder2.matches(password, hash1), 
                "El segundo encoder debe validar hash del primero");
    }

    @Test
    @DisplayName("passwordEncoder() debe tener fuerza de cifrado apropiada")
    void passwordEncoder_ShouldHaveAppropriateCipherStrength() {
        // Preparar
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String password = "strengthTest123";

        // Ejecutar
        String hash = passwordEncoder.encode(password);

        // Verificar características de seguridad de BCrypt
        assertTrue(hash.length() >= 59, "El hash BCrypt debe tener al menos 59 caracteres");
        
        // El hash debe contener información del algoritmo, costo y salt
        String[] parts = hash.split("\\$");
        assertTrue(parts.length >= 4, "El hash BCrypt debe tener al menos 4 partes separadas por $");
        
        // Verificar que tiene un costo razonable (típicamente entre 10-12)
        if (parts.length >= 3) {
            String costStr = parts[2];
            assertDoesNotThrow(() -> {
                int cost = Integer.parseInt(costStr);
                assertTrue(cost >= 4 && cost <= 31, 
                        "El costo de BCrypt debe estar entre 4 y 31 (típicamente 10-12)");
            }, "El costo de BCrypt debe ser un número válido");
        }
    }
}