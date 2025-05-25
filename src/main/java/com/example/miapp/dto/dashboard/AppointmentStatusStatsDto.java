package com.example.miapp.dto.dashboard;

import com.example.miapp.entity.Appointment.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for appointment status statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatusStatsDto {
    private AppointmentStatus status;
    private long count;
    private double percentage;
}