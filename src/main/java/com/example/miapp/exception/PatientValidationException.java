package com.example.miapp.exception;

/**
 * Custom exception for patient validation errors
 */
public class PatientValidationException extends RuntimeException {
    
    public PatientValidationException(String message) {
        super(message);
    }
    
    public PatientValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}