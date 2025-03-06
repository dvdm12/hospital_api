package com.example.miapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

/**
 * DTO for transferring doctor-specialty relation data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DoctorSpecialtyDto {

    private Long id;

    @NotNull(message = "Doctor ID cannot be null")
    private Long doctorId;

    @NotNull(message = "Specialty ID cannot be null")
    private Long specialtyId;

    private Date certificationDate;

    private String experienceLevel;
}
