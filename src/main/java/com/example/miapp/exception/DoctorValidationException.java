package com.example.miapp.exception;

/**
 * Custom exception for doctor validation errors
 */
public class DoctorValidationException extends RuntimeException {
    
    public DoctorValidationException(String message) {
        super(message);
    }
    
    public DoctorValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}