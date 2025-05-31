package com.example.miapp.exception;

/**
 * Excepción para manejar errores de validación en citas
 */
public class AppointmentValidationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public AppointmentValidationException(String message) {
        super(message);
    }
    
    public AppointmentValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}