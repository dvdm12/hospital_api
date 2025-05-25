package com.example.miapp.exception;

/**
 * Custom exception for prescription validation errors
 */
public class PrescriptionValidationException extends RuntimeException {
    
    public PrescriptionValidationException(String message) {
        super(message);
    }
    
    public PrescriptionValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}