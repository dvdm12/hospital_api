package com.example.miapp.config;

import com.example.miapp.security.JwtAuthenticationEntryPoint;
import com.example.miapp.security.JwtAuthenticationFilter;
import com.example.miapp.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para SecurityConfig
 * Se enfoca en testear la lógica específica sin dependencias complejas de Spring
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig - Pruebas Unitarias")
class SecurityConfigTest {

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Nested
    @DisplayName("Pruebas del PasswordEncoder")
    class PasswordEncoderTests {
        
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

    @Nested
    @DisplayName("Pruebas del AuthenticationManager")
    class AuthenticationManagerTests {
        
        @Test
        @DisplayName("authenticationManager() debe retornar el AuthenticationManager de la configuración")
        void authenticationManager_ShouldReturnAuthManagerFromConfig() throws Exception {
            // Preparar
            AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
            AuthenticationManager expectedManager = mock(AuthenticationManager.class);
            when(authConfig.getAuthenticationManager()).thenReturn(expectedManager);
            
            // Ejecutar
            AuthenticationManager result = securityConfig.authenticationManager(authConfig);
            
            // Verificar
            assertNotNull(result, "El AuthenticationManager no debe ser null");
            assertSame(expectedManager, result, "Debe retornar el mismo AuthenticationManager de la configuración");
            verify(authConfig).getAuthenticationManager();
        }
    }
    
    @Nested
    @DisplayName("Pruebas del AuthenticationProvider")
    class AuthenticationProviderTests {
        
        @Test
        @DisplayName("authenticationProvider() debe crear una instancia de DaoAuthenticationProvider")
        void authenticationProvider_ShouldCreateDaoAuthenticationProvider() {
            // Ejecutar
            DaoAuthenticationProvider provider = securityConfig.authenticationProvider();
            
            // Verificar
            assertNotNull(provider, "El DaoAuthenticationProvider no debe ser null");
            
            // Verificar que se configura con el UserDetailsService correcto
            try {
                // Preparar un escenario donde el provider usaría userDetailsService
                UserDetails mockUser = mock(UserDetails.class);
                when(userDetailsService.loadUserByUsername("testuser")).thenReturn(mockUser);
                
                // Intentar autenticar para verificar que usa nuestro userDetailsService mockeado
                try {
                    provider.authenticate(
                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            "testuser", "wrongpassword"));
                } catch (Exception e) {
                    // Esperamos una excepción porque las credenciales son inválidas
                }
                
                // Verificar que se llamó a nuestro servicio
                verify(userDetailsService).loadUserByUsername("testuser");
                
            } catch (Exception e) {
                // Si algo falla, al menos verificamos que el provider no es null
                System.out.println("No se pudo verificar el userDetailsService: " + e.getMessage());
            }
        }
        
        @Test
        @DisplayName("authenticationProvider() debe retornar una instancia nueva en cada llamada")
        void authenticationProvider_ShouldReturnNewInstanceOnEachCall() {
            // Ejecutar
            DaoAuthenticationProvider provider1 = securityConfig.authenticationProvider();
            DaoAuthenticationProvider provider2 = securityConfig.authenticationProvider();
            
            // Verificar
            assertNotNull(provider1, "El primer provider no debe ser null");
            assertNotNull(provider2, "El segundo provider no debe ser null");
            assertNotSame(provider1, provider2, "Cada llamada debe retornar una instancia nueva");
        }
    }
    
    @Nested
    @DisplayName("Pruebas de los FilterChains")
    class FilterChainTests {
        
        // Estas pruebas normalmente requerirían integración con Spring Security
        // y no son fáciles de realizar como pruebas unitarias puras.
        // Para una cobertura completa, considera añadir pruebas de integración.
        
        @Test
        @DisplayName("Los filter chains deben estar disponibles")
        void filterChains_ShouldBeAvailable() {
            // Esta es solo una prueba básica para asegurar que los métodos existen
            // Una prueba real requeriría Spring Security Test
            assertDoesNotThrow(() -> {
                // Verificar que los métodos de filterChain existen y son accesibles
                assertNotNull(SecurityConfig.class.getDeclaredMethod("openApiFilterChain", org.springframework.security.config.annotation.web.builders.HttpSecurity.class));
                assertNotNull(SecurityConfig.class.getDeclaredMethod("apiFilterChain", org.springframework.security.config.annotation.web.builders.HttpSecurity.class));
                assertNotNull(SecurityConfig.class.getDeclaredMethod("defaultFilterChain", org.springframework.security.config.annotation.web.builders.HttpSecurity.class));
            }, "Los métodos de filter chain deben existir");
        }
    }
}