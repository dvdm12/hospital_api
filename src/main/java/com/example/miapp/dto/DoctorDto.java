package com.example.miapp.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for transferring doctor data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DoctorDto {

    /** Unique identifier for the doctor. */
    @Positive(message = "ID must be positive")
    private Long id;

    /** Doctor's first name. Cannot be null and must be between 2 and 50 characters. */
    @NotNull(message = "First name cannot be null")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    /** Doctor's last name. Cannot be null and must be between 2 and 50 characters. */
    @NotNull(message = "Last name cannot be null")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    /** Doctor's phone number. Must follow a valid format. */
    @Pattern(regexp = "^\\+?[0-9\\- ]{7,20}$", message = "Phone number must be valid and between 7-20 characters")
    private String phone;

    /** Doctor's email address. Cannot be null and must follow email format. */
    @NotNull(message = "Email cannot be null")
    @Email(message = "Email should be valid")
    private String email;
}
