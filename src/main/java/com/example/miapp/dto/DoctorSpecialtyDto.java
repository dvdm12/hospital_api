package com.example.miapp.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Date;

/**
 * DTO for transferring doctor-specialty relation data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DoctorSpecialtyDto {

    /** Unique identifier for the doctor-specialty relation. */
    @Positive(message = "ID must be positive")
    private Long id;

    /** The ID of the doctor assigned to the specialty. Cannot be null. */
    @NotNull(message = "Doctor ID cannot be null")
    private Long doctorId;

    /** The ID of the specialty assigned to the doctor. Cannot be null. */
    @NotNull(message = "Specialty ID cannot be null")
    private Long specialtyId;

    /** Certification date of the doctor in the specialty. Must be in the past or present. */
    @PastOrPresent(message = "Certification date must be in the past or present")
    private Date certificationDate;

    /** Experience level of the doctor in the specialty. */
    @Size(min = 3, max = 50, message = "Experience level must be between 3 and 50 characters")
    private String experienceLevel;
}
