package com.example.miapp.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    @InjectMocks
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void commence_ApiRequest_ReturnsJsonResponse() throws IOException {
        // Preparar
        when(request.getRequestURI()).thenReturn("/api/resource");
        when(authException.getMessage()).thenReturn("Unauthorized");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Ejecutar
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Verificar
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        writer.flush();
        assertTrue(stringWriter.toString().contains("Unauthorized"));
    }

    @Test
    void commence_WebRequest_RedirectsToLogin() throws IOException {
        // Preparar
        when(request.getRequestURI()).thenReturn("/web/resource");

        // Ejecutar
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Verificar
        verify(response).sendRedirect("/login?error=true");
    }
}