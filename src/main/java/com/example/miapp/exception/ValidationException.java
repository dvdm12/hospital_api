package com.example.miapp.exception;

/**
 * Excepción lanzada cuando fallan las validaciones de negocio.
 * Se utiliza para errores de validación que no son capturados por las validaciones
 * de Jakarta Bean Validation (como @NotNull, @Size, etc.)
 */
public class ValidationException extends RuntimeException {

    /**
     * Constructor con mensaje de error.
     *
     * @param message el mensaje de error
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa.
     *
     * @param message el mensaje de error
     * @param cause la causa de la excepción
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}