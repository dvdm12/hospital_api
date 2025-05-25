package com.example.miapp.dto.appointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO for available time slots
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotDto {
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
    private String location;
}