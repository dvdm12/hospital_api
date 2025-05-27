package com.example.miapp.security;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    @InjectMocks
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private ByteArrayOutputStream outputStream;
    private ServletOutputStream servletOutputStream;

    @BeforeEach
    void setUp() throws Exception {
        outputStream = new ByteArrayOutputStream();
        servletOutputStream = new ServletOutputStream() {
            @Override
            public void write(int b) {
                outputStream.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
                // No implementation needed for test
            }
        };
        
        // Solo configurar stubs que se usan en TODOS los tests
        // Los stubs específicos se configuran en cada test individual
    }

    @Test
    void commence_ApiRequest_ReturnsJsonResponse() throws Exception {
        // Configurar stubs específicos para este test
        when(request.getRequestURI()).thenReturn("/api/test");
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        when(authException.getMessage()).thenReturn("Test unauthorized access");

        // Ejecutar
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Verificar configuración de respuesta
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // Verificar contenido de respuesta
        String jsonResponse = outputStream.toString();
        
        assertNotNull(jsonResponse);
        assertTrue(jsonResponse.contains("401"));
        assertTrue(jsonResponse.contains("Unauthorized"));
        assertTrue(jsonResponse.contains("Test unauthorized access"));
        assertTrue(jsonResponse.contains("/api/test"));
    }

    @Test
    void commence_ApiRequestWithNestedPath_ReturnsJsonResponse() throws Exception {
        // Configurar stubs específicos para este test
        when(request.getRequestURI()).thenReturn("/api/auth/protected");
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        when(authException.getMessage()).thenReturn("Test unauthorized access");

        // Ejecutar
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Verificar
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        String jsonResponse = outputStream.toString();
        assertTrue(jsonResponse.contains("/api/auth/protected"));
    }

    @Test
    void commence_WebRequest_RedirectsToLogin() throws Exception {
        // Configurar stubs específicos para este test
        when(request.getRequestURI()).thenReturn("/dashboard");

        // Ejecutar
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Verificar
        verify(response).sendRedirect("/login?error=true");
        
        // Verificar que NO se configuró como JSON
        verify(response, never()).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void commence_RootWebRequest_RedirectsToLogin() throws Exception {
        // Configurar stubs específicos para este test
        when(request.getRequestURI()).thenReturn("/");

        // Ejecutar
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Verificar
        verify(response).sendRedirect("/login?error=true");
    }

    @Test
    void commence_AdminWebRequest_RedirectsToLogin() throws Exception {
        // Configurar stubs específicos para este test
        when(request.getRequestURI()).thenReturn("/admin/users");

        // Ejecutar
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Verificar
        verify(response).sendRedirect("/login?error=true");
    }

    @Test
    void commence_NullUri_RedirectsToLogin() throws Exception {
        // Configurar stubs específicos para este test
        when(request.getRequestURI()).thenReturn(null);

        // Ejecutar
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Verificar que se maneja el null gracefully
        verify(response).sendRedirect("/login?error=true");
    }

    @Test
    void commence_EmptyUri_RedirectsToLogin() throws Exception {
        // Configurar stubs específicos para este test
        when(request.getRequestURI()).thenReturn("");

        // Ejecutar
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Verificar
        verify(response).sendRedirect("/login?error=true");
    }

    @Test
    void commence_ApiRequestWithNullException_HandlesGracefully() throws Exception {
        // Configurar stubs específicos para este test
        when(request.getRequestURI()).thenReturn("/api/test");
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        when(authException.getMessage()).thenReturn(null);

        // Ejecutar
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Verificar que maneja el mensaje null gracefully
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        String jsonResponse = outputStream.toString();
        assertNotNull(jsonResponse);
        // Debe contener null o manejar el null apropiadamente
        assertTrue(jsonResponse.contains("null") || jsonResponse.length() > 0);
    }
}