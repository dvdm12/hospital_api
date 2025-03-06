package com.example.miapp.dto;

import jakarta.validation.constraints.*;
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

    /** Unique identifier for the patient. */
    @Positive(message = "ID must be positive")
    private Long id;

    /** Patient's first name. Cannot be null and must be between 2 and 50 characters. */
    @NotNull(message = "First name cannot be null")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    /** Patient's last name. Cannot be null and must be between 2 and 50 characters. */
    @NotNull(message = "Last name cannot be null")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    /** Patient's birth date. Must be in the past. */
    @Past(message = "Birth date must be in the past")
    private Date birthDate;

    /** Patient's phone number. Must follow a valid format. */
    @Pattern(regexp = "^\\+?[0-9\\- ]{7,20}$", message = "Phone number must be valid and between 7-20 characters")
    private String phone;

    /** Patient's address. Cannot be null and must not exceed 255 characters. */
    @NotNull(message = "Address cannot be null")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
}
