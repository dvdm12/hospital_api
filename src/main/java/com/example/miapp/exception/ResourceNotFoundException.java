package com.example.miapp.exception;

/**
 * Excepción para manejar casos donde no se encuentran recursos solicitados
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}