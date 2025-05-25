package com.example.miapp.dto.specialty;

import lombok.Data;

/**
 * DTO for Specialty responses
 */
@Data
public class SpecialtyDto {
    private Long id;
    private String name;
    private String description;
    private long doctorCount;
}