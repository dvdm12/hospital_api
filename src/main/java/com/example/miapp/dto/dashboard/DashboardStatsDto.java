package com.example.miapp.dto.dashboard;

import lombok.Data;

/**
 * DTO for main dashboard statistics
 */
@Data
public class DashboardStatsDto {
    private int totalAppointments;
    private int scheduledAppointments;
    private int completedAppointments;
    private int canceledAppointments;
    private int noShowAppointments;
    private int newPatients;
    private int totalPatients;
    private int totalDoctors;
    private int prescriptionsIssued;
    private Double averageAppointmentDuration;
    private Double totalRevenue;
}