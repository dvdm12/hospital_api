package com.example.miapp.exception;

/**
 * Custom exception for medical record validation errors
 */
public class MedicalRecordValidationException extends RuntimeException {
    
    public MedicalRecordValidationException(String message) {
        super(message);
    }
    
    public MedicalRecordValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}