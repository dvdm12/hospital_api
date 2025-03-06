package com.example.miapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;

/**
 * DTO for transferring patient data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PatientDto {

    private Long id;

    @NotNull(message = "First name cannot be null")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotNull(message = "Last name cannot be null")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Past(message = "Birth date must be in the past")
    private Date birthDate;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phone;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
}
