package com.example.miapp.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for transferring specialty data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SpecialtyDto {

    /** Unique identifier for the specialty. */
    @Positive(message = "ID must be positive")
    private Long id;

    /** Name of the specialty (e.g., Cardiology, Neurology). Must be unique. */
    @NotNull(message = "Name cannot be null")
    @Size(min = 3, max = 100, message = "Specialty name must be between 3 and 100 characters")
    private String name;

    /** Description of the specialty. Cannot be null and must not exceed 255 characters. */
    @NotNull(message = "Description cannot be null")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
}
