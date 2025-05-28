package com.example.miapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador personalizado para manejar errores no capturados por otros controladores.
 * Proporciona una experiencia de usuario más amigable para páginas de error.
 */
@Controller
@Slf4j
public class BasicErrorController implements ErrorController {

    /**
     * Maneja todas las solicitudes que resultan en error
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, HttpServletResponse response, Model model) {
        // Obtener código de estado del error
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        
        // Obtener la excepción si está disponible
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        
        // Obtener la URI original que causó el error
        String originalUri = (String) request.getAttribute("javax.servlet.error.request_uri");
        
        log.error("Error {} en la solicitud a {}: {}", 
                statusCode, originalUri, throwable != null ? throwable.getMessage() : "Desconocido");
        
        // Añadir información relevante al modelo
        model.addAttribute("errorCode", statusCode != null ? statusCode : 500);
        model.addAttribute("errorMessage", getErrorMessage(statusCode));
        model.addAttribute("requestUri", originalUri);
        
        // Si tenemos información detallada de la excepción, agregarla al modelo
        if (throwable != null) {
            model.addAttribute("exceptionMessage", throwable.getMessage());
            model.addAttribute("exceptionType", throwable.getClass().getName());
            
            // En modo desarrollo, agregar el stack trace
            if (isDevelopmentMode()) {
                model.addAttribute("stackTrace", getStackTraceAsString(throwable));
            }
        }
        
        // Verificar si es una solicitud AJAX
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.setStatus(statusCode != null ? statusCode : 500);
            return "error-ajax";
        }
        
        // Personalizar la página de error según el código de estado
        if (statusCode != null) {
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error-404";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "error-403";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error-500";
            }
        }
        
        // Página de error genérica como fallback
        return "error";
    }
    
    /**
     * Devuelve un mensaje de error adecuado según el código de estado
     */
    private String getErrorMessage(Integer statusCode) {
        if (statusCode == null) {
            return "Ha ocurrido un error inesperado";
        }
        
        switch (statusCode) {
            case 400:
                return "La solicitud contiene sintaxis errónea";
            case 401:
                return "No está autorizado para acceder a este recurso";
            case 403:
                return "Acceso denegado";
            case 404:
                return "El recurso solicitado no existe";
            case 405:
                return "Método HTTP no permitido";
            case 500:
                return "Error interno del servidor";
            case 503:
                return "Servicio no disponible temporalmente";
            default:
                return "Error " + statusCode;
        }
    }
    
    /**
     * Determina si la aplicación está en modo desarrollo
     */
    private boolean isDevelopmentMode() {
        // Lógica para determinar si estamos en entorno dev, por ejemplo basado en propiedades
        // Por ahora retornamos true para fines de ejemplo
        return true;
    }
    
    /**
     * Convierte el stack trace a String para mostrarlo en la vista
     */
    private String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}