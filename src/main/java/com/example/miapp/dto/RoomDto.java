package com.example.miapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO for transferring room data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RoomDto {

    private Long id;

    @NotNull(message = "Room number cannot be null")
    private String number;

    @NotNull(message = "Floor cannot be null")
    private String floor;

    @NotNull(message = "Type cannot be null")
    private String type;

    private String occupancyStatus;
}
