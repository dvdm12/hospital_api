package com.example.miapp.config;

import com.example.miapp.security.JwtAuthenticationEntryPoint;
import com.example.miapp.security.JwtAuthenticationFilter;
import com.example.miapp.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void passwordEncoder_ReturnsPasswordEncoder() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder.matches("password", passwordEncoder.encode("password")));
    }

    @Test
    void authenticationProvider_ReturnsDaoAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = securityConfig.authenticationProvider();
        assertNotNull(authProvider);
    }

    @Test
    void apiFilterChain_ReturnsSecurityFilterChain() throws Exception {
        SecurityFilterChain filterChain = securityConfig.apiFilterChain(null);
        assertNotNull(filterChain);
    }

    @Test
    void formLoginFilterChain_ReturnsSecurityFilterChain() throws Exception {
        SecurityFilterChain filterChain = securityConfig.formLoginFilterChain(null);
        assertNotNull(filterChain);
    }
}