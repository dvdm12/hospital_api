package com.example.miapp.exception;

/**
 * Excepción específica para errores de validación relacionados con doctores.
 * Permite diferenciar los errores de validación de otros tipos de excepciones.
 */
public class DoctorValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    /** Código de error específico para categorizar el tipo de error */
    private String errorCode;
    
    /** 
     * Constructor simple con mensaje de error
     * 
     * @param message Mensaje descriptivo del error
     */
    public DoctorValidationException(String message) {
        super(message);
        this.errorCode = "DOCTOR_VALIDATION_ERROR";
    }
    
    /**
     * Constructor con mensaje y código de error específico
     * 
     * @param message Mensaje descriptivo del error
     * @param errorCode Código de error para categorización
     */
    public DoctorValidationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructor con mensaje y causa raíz
     * 
     * @param message Mensaje descriptivo del error
     * @param cause Excepción original que causó el error
     */
    public DoctorValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DOCTOR_VALIDATION_ERROR";
    }
    
    /**
     * Constructor completo con mensaje, código de error y causa raíz
     * 
     * @param message Mensaje descriptivo del error
     * @param errorCode Código de error para categorización
     * @param cause Excepción original que causó el error
     */
    public DoctorValidationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Obtiene el código de error
     * 
     * @return El código de error asociado a esta excepción
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    // Códigos de error comunes como constantes para uso consistente
    public static final String USERNAME_EXISTS = "USERNAME_EXISTS";
    public static final String EMAIL_EXISTS = "EMAIL_EXISTS";
    public static final String LICENSE_EXISTS = "LICENSE_EXISTS";
    public static final String INVALID_SPECIALTY = "INVALID_SPECIALTY";
    public static final String INVALID_SCHEDULE = "INVALID_SCHEDULE";
    public static final String ACTIVE_APPOINTMENTS = "ACTIVE_APPOINTMENTS";
    public static final String ACTIVE_PRESCRIPTIONS = "ACTIVE_PRESCRIPTIONS";
}