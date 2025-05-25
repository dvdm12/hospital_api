package com.example.miapp.exception;

/**
 * Custom exception for appointment validation errors
 */
public class AppointmentValidationException extends RuntimeException {
    
    public AppointmentValidationException(String message) {
        super(message);
    }
    
    public AppointmentValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}