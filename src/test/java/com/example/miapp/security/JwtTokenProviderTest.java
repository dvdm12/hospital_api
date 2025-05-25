package com.example.miapp.security;

import com.example.miapp.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenProviderTest {

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private JwtTokenProvider tokenProvider;

    private UserDetailsImpl userDetails;
    private String secretKey;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        secretKey = "testSecretKeyMustBeAtLeast32CharsLong1234";
        when(jwtConfig.getSecret()).thenReturn(secretKey);
        when(jwtConfig.getExpirationInMs()).thenReturn(60000);
        when(jwtConfig.getCookieName()).thenReturn("jwt_token");
        
        // Reinicializar tokenProvider para que use el secretKey mock
        tokenProvider = new JwtTokenProvider(jwtConfig);
        
        userDetails = new UserDetailsImpl(
            1L, 
            "testuser",
            "test@example.com",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
            true
        );
        
        when(authentication.getPrincipal()).thenReturn(userDetails);
    }

    @Test
    void generateToken_ReturnsValidToken() {
        // Ejecutar
        String token = tokenProvider.generateToken(authentication);

        // Verificar
        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("testuser", tokenProvider.getUsernameFromToken(token));
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        // Preparar
        String token = tokenProvider.generateToken(authentication);

        // Ejecutar y verificar
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        // Preparar
        String invalidToken = "invalid.token.string";

        // Ejecutar y verificar
        assertFalse(tokenProvider.validateToken(invalidToken));
    }

    @Test
    void getAuthenticationToken_ReturnsAuthenticationToken() {
        // Preparar
        String token = tokenProvider.generateToken(authentication);

        // Ejecutar
        UsernamePasswordAuthenticationToken authToken = tokenProvider.getAuthenticationToken(token, userDetails);

        // Verificar
        assertNotNull(authToken);
        assertEquals(userDetails, authToken.getPrincipal());
        assertEquals(1, authToken.getAuthorities().size());
        assertTrue(authToken.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void resolveToken_FromAuthorizationHeader_ReturnsToken() {
        // Preparar
        when(request.getHeader("Authorization")).thenReturn("Bearer testToken");

        // Ejecutar
        String resolvedToken = tokenProvider.resolveToken(request);

        // Verificar
        assertEquals("testToken", resolvedToken);
    }

    @Test
    void resolveToken_FromCookie_ReturnsToken() {
        // Preparar
        when(request.getHeader("Authorization")).thenReturn(null);
        Cookie[] cookies = new Cookie[]{new Cookie("jwt_token", "cookieToken")};
        when(request.getCookies()).thenReturn(cookies);

        // Ejecutar
        String resolvedToken = tokenProvider.resolveToken(request);

        // Verificar
        assertEquals("cookieToken", resolvedToken);
    }

    @Test
    void resolveToken_NoToken_ReturnsNull() {
        // Preparar
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        // Ejecutar
        String resolvedToken = tokenProvider.resolveToken(request);

        // Verificar
        assertNull(resolvedToken);
    }
}