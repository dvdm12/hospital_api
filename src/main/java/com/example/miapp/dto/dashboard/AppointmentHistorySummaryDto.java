package com.example.miapp.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for appointment history summary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentHistorySummaryDto {
    private int totalAppointments;
    private int completedAppointments;
    private int canceledAppointments;
    private int noShowAppointments;
    private double completionRate;
    private double noShowRate;
    private double cancellationRate;
    private LocalDateTime lastAppointment;
    private LocalDateTime nextAppointment;
    private String mostVisitedSpecialty;
    private String preferredDoctor;
    private int appointmentsThisYear;
    private int appointmentsLastYear;
    private String patientType; // NEW, REGULAR, VIP, FREQUENT
}