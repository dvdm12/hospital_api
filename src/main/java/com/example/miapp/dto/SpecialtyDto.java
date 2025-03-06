package com.example.miapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for transferring specialty data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SpecialtyDto {

    private Long id;

    @NotNull(message = "Name cannot be null")
    @Size(min = 3, max = 100, message = "Specialty name must be between 3 and 100 characters")
    private String name;

    private String description;
}
