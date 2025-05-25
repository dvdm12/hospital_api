package com.example.miapp.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for doctor statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStatsDto {
    private Long doctorId;
    private String doctorName;
    private long appointmentCount;
    private long completedAppointments;
    private long canceledAppointments;
    private double completionRate;
    private double revenue;
}