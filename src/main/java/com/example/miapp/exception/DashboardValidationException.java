package com.example.miapp.exception;

/**
 * Custom exception for dashboard validation errors
 */
public class DashboardValidationException extends RuntimeException {
    
    public DashboardValidationException(String message) {
        super(message);
    }
    
    public DashboardValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}