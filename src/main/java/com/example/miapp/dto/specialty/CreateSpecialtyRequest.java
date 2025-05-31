package com.example.miapp.dto.specialty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitudes de creación de especialidades
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSpecialtyRequest {
    
    @NotBlank(message = "El nombre de la especialidad es requerido")
    @Size(max = 50, message = "El nombre no debe exceder los 50 caracteres")
    private String name;
    
    @Size(max = 255, message = "La descripción no debe exceder los 255 caracteres")
    private String description;
}