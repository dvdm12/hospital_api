package com.example.miapp.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Manejador global de excepciones para la aplicación web.
 * Captura las excepciones no manejadas y las procesa adecuadamente.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de validación de doctores
     */
    @ExceptionHandler(DoctorValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleDoctorValidationException(DoctorValidationException ex, HttpServletRequest request) {
        log.warn("Error de validación de doctor en {}: {} (Código: {})", 
                request.getRequestURI(), ex.getMessage(), ex.getErrorCode());
        
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("errorCode", ex.getErrorCode());
        mav.addObject("requestUri", request.getRequestURI());
        
        // Determinar la vista a mostrar basada en la URI de la solicitud
        if (request.getRequestURI().contains("/admin/register")) {
            // Si estamos en la página de registro de doctor, volvemos a ella con el mensaje de error
            mav.setViewName("admin/register-doctor");
            
            // Agregar atributos necesarios para la vista de registro
            mav.addObject("adminUser", "admin"); // Esto debería ser reemplazado por el usuario actual
            mav.addObject("specialties", getSpecialties());
            mav.addObject("doctorRequest", extractDoctorRequestFromSession(request));
        } else if (request.getRequestURI().contains("/api/")) {
            // Para solicitudes API, retornamos una vista simple
            mav.setViewName("error");
            mav.addObject("message", "Error de validación: " + ex.getMessage());
        } else {
            // Vista de error genérica para otras rutas
            mav.setViewName("error");
            mav.addObject("message", "Error de validación: " + ex.getMessage());
        }
        
        return mav;
    }
    
    /**
     * Maneja excepciones de integridad de datos (duplicados, restricciones, etc.)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ModelAndView handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Error de integridad de datos en {}: {}", request.getRequestURI(), ex.getMessage());
        
        String errorMessage = "Error de integridad en la base de datos. ";
            
        // Intentar extraer mensaje más amigable
        if (ex.getMessage().contains("unique constraint") || ex.getMessage().contains("Duplicate entry")) {
            if (ex.getMessage().contains("username")) {
                errorMessage += "El nombre de usuario ya existe.";
            } else if (ex.getMessage().contains("email")) {
                errorMessage += "El email ya está registrado.";
            } else if (ex.getMessage().contains("license")) {
                errorMessage += "El número de licencia ya está registrado.";
            } else {
                errorMessage += "Hay información duplicada en la solicitud.";
            }
        } else {
            errorMessage += "Por favor verifique la información ingresada.";
        }
        
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", errorMessage);
        mav.addObject("requestUri", request.getRequestURI());
        
        // Determinar la vista a mostrar basada en la URI de la solicitud
        if (request.getRequestURI().contains("/admin/register")) {
            mav.setViewName("admin/register-doctor");
            // Agregar atributos necesarios para la vista de registro
            mav.addObject("adminUser", "admin"); // Esto debería ser reemplazado por el usuario actual
            mav.addObject("specialties", getSpecialties());
            mav.addObject("doctorRequest", extractDoctorRequestFromSession(request));
        } else if (request.getRequestURI().contains("/api/")) {
            mav.setViewName("error");
            mav.addObject("message", errorMessage);
        } else {
            mav.setViewName("error");
            mav.addObject("message", errorMessage);
        }
        
        return mav;
    }
    
    /**
     * Maneja excepciones de acceso denegado
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Acceso denegado a {}: {}", request.getRequestURI(), ex.getMessage());
        
        // Suponemos que existe una página de acceso denegado
        return "redirect:/access-denied";
    }
    
    /**
     * Maneja errores 404 (recurso no encontrado)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundError(NoHandlerFoundException ex, Model model) {
        log.warn("Recurso no encontrado: {}", ex.getRequestURL());
        
        model.addAttribute("errorMessage", "El recurso solicitado no existe");
        model.addAttribute("requestUri", ex.getRequestURL());
        
        // Usar la página de error estándar
        return "error";
    }
    
    /**
     * Maneja todas las demás excepciones no capturadas
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleAllExceptions(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", "Ha ocurrido un error inesperado en el sistema");
        mav.addObject("errorDetails", ex.getMessage());
        mav.addObject("requestUri", request.getRequestURI());
        
        // En entorno de desarrollo, incluir detalles adicionales
        if (isDevelopmentEnvironment()) {
            mav.addObject("stackTrace", getStackTraceAsString(ex));
        }
        
        // Usar la página de error estándar de Spring
        mav.setViewName("error");
        
        return mav;
    }
    
    /**
     * Determina si la aplicación está en entorno de desarrollo
     */
    private boolean isDevelopmentEnvironment() {
        // Lógica para determinar si estamos en entorno dev, por ejemplo basado en propiedades
        // Por ahora retornamos true para fines de ejemplo
        return true;
    }
    
    /**
     * Convierte el stack trace a String para mostrarlo en la vista
     */
    private String getStackTraceAsString(Exception ex) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Método auxiliar para obtener especialidades para la vista de registro
     * En un caso real, esto debería usar un servicio inyectado
     */
    private Object getSpecialties() {
        // En un caso real, esto debería usar el repositorio de especialidades
        // Como es solo para el manejador de excepciones, retornamos una lista vacía
        return java.util.Collections.emptyList();
    }
    
    /**
     * Método auxiliar para extraer el objeto DoctorRequest de la sesión o crear uno nuevo
     */
    private Object extractDoctorRequestFromSession(HttpServletRequest request) {
        // En un caso real, intentaríamos recuperar el objeto de la sesión
        // Como es solo para el manejador de excepciones, retornamos un objeto vacío
        return new Object(); // Esto debería ser un CreateDoctorRequest
    }
}