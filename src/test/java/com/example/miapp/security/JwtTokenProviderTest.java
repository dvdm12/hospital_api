package com.example.miapp.security;

import com.example.miapp.config.JwtConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @Mock
    private JwtConfig jwtConfig;

    private JwtTokenProvider tokenProvider;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        // Configurar mocks ANTES de crear la instancia
        lenient().when(jwtConfig.getSecret()).thenReturn("testSecretKeyMustBeAtLeast32CharsLong1234567890123456789");
        lenient().when(jwtConfig.getExpirationInMs()).thenReturn(60000);
        lenient().when(jwtConfig.getCookieName()).thenReturn("jwt_token");
        
        // Crear instancia manualmente después de configurar mocks
        tokenProvider = new JwtTokenProvider(jwtConfig);
        
        userDetails = new UserDetailsImpl(
            1L, 
            "testuser",
            "test@example.com",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
            true
        );
    }

    @Test
    void generateToken_ReturnsValidToken() {
        // Preparar
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

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
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
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
    void validateToken_EmptyToken_ReturnsFalse() {
        // Ejecutar y verificar
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken(null));
    }

    @Test
    void getUsernameFromToken_ValidToken_ReturnsUsername() {
        // Preparar
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        String token = tokenProvider.generateToken(authentication);

        // Ejecutar
        String username = tokenProvider.getUsernameFromToken(token);

        // Verificar
        assertEquals("testuser", username);
    }

    @Test
    void getAuthenticationToken_ReturnsAuthenticationToken() {
        // Preparar
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        String token = tokenProvider.generateToken(authentication);

        // Ejecutar
        UsernamePasswordAuthenticationToken authToken = 
            tokenProvider.getAuthenticationToken(token, userDetails);

        // Verificar
        assertNotNull(authToken);
        assertEquals(userDetails, authToken.getPrincipal());
        assertNull(authToken.getCredentials());
        assertEquals(1, authToken.getAuthorities().size());
        assertTrue(authToken.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void resolveToken_FromAuthorizationHeader_ReturnsToken() {
        // Preparar
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer testToken123");

        // Ejecutar
        String resolvedToken = tokenProvider.resolveToken(request);

        // Verificar
        assertEquals("testToken123", resolvedToken);
    }

    @Test
    void resolveToken_FromAuthorizationHeaderWithoutBearer_ReturnsNull() {
        // Preparar
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("testToken123");
        when(request.getCookies()).thenReturn(null);

        // Ejecutar
        String resolvedToken = tokenProvider.resolveToken(request);

        // Verificar
        assertNull(resolvedToken);
    }

    @Test
    void resolveToken_FromCookie_ReturnsToken() {
        // Preparar
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        Cookie[] cookies = new Cookie[]{
            new Cookie("other_cookie", "other_value"),
            new Cookie("jwt_token", "cookieToken123")
        };
        when(request.getCookies()).thenReturn(cookies);

        // Ejecutar
        String resolvedToken = tokenProvider.resolveToken(request);

        // Verificar
        assertEquals("cookieToken123", resolvedToken);
    }

    @Test
    void resolveToken_NoCookies_ReturnsNull() {
        // Preparar
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        // Ejecutar
        String resolvedToken = tokenProvider.resolveToken(request);

        // Verificar
        assertNull(resolvedToken);
    }

    @Test
    void resolveToken_EmptyCookies_ReturnsNull() {
        // Preparar
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[]{});

        // Ejecutar
        String resolvedToken = tokenProvider.resolveToken(request);

        // Verificar
        assertNull(resolvedToken);
    }

    @Test
    void resolveToken_WrongCookieName_ReturnsNull() {
        // Preparar
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        Cookie[] cookies = new Cookie[]{
            new Cookie("wrong_cookie", "some_value")
        };
        when(request.getCookies()).thenReturn(cookies);

        // Ejecutar
        String resolvedToken = tokenProvider.resolveToken(request);

        // Verificar
        assertNull(resolvedToken);
    }
}