package com.example.miapp.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for transferring room data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RoomDto {

    /** Unique identifier for the room. */
    @Positive(message = "ID must be positive")
    private Long id;

    /** Room number. Cannot be null and must be between 1 and 10 characters. */
    @NotNull(message = "Room number cannot be null")
    @Size(min = 1, max = 10, message = "Room number must be between 1 and 10 characters")
    private String number;

    /** Floor where the room is located. Cannot be null and must be between 1 and 5 characters. */
    @NotNull(message = "Floor cannot be null")
    @Size(min = 1, max = 5, message = "Floor must be between 1 and 5 characters")
    private String floor;

    /** Type of room (e.g., ICU, General, VIP). Cannot be null and must be between 3 and 20 characters. */
    @NotNull(message = "Type cannot be null")
    @Size(min = 3, max = 20, message = "Type must be between 3 and 20 characters")
    private String type;

    /** Occupancy status (Available, Occupied). Must follow a valid pattern. */
    @Pattern(regexp = "^(Available|Occupied)$", message = "Occupancy status must be 'Available' or 'Occupied'")
    private String occupancyStatus;
}
