package com.example.miapp.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for real-time dashboard statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeStatsDto {
    private LocalDateTime currentTime;
    private int todayAppointments;
    private int activePatients;
    private int onlineDoctors;
    private int pendingAppointments;
    private Double todayRevenue;
    private int completedAppointmentsToday;
    private int canceledAppointmentsToday;
    private int noShowAppointmentsToday;
    private double systemLoad;
    private String systemStatus;
}