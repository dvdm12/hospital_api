package com.example.miapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        // Preparar
        String token = "valid.jwt.token";
        UserDetails userDetails = mock(UserDetails.class);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        when(tokenProvider.resolveToken(request)).thenReturn(token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(tokenProvider.getAuthenticationToken(token, userDetails)).thenReturn(authToken);

        // Ejecutar
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Verificar
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_NoToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Preparar
        when(tokenProvider.resolveToken(request)).thenReturn(null);

        // Ejecutar
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Verificar
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Preparar
        String token = "invalid.jwt.token";
        when(tokenProvider.resolveToken(request)).thenReturn(token);
        when(tokenProvider.validateToken(token)).thenReturn(false);

        // Ejecutar
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Verificar
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ExceptionThrown_DoesNotSetAuthentication() throws ServletException, IOException {
        // Preparar
        String token = "valid.jwt.token";
        when(tokenProvider.resolveToken(request)).thenReturn(token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(token)).thenThrow(new RuntimeException("Token error"));

        // Ejecutar
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Verificar
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}