package com.example.miapp.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO for doctor-specific dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDashboardDto {
    private Long doctorId;
    private String doctorName;
    private LocalDate date;
    private List<AppointmentSummaryDto> todayAppointments;
    private int totalAppointments;
    private int completedAppointments;
    private int canceledAppointments;
    private int noShowAppointments;
    private double utilizationRate;
    private double completionRate;
    private List<AppointmentSummaryDto> upcomingAppointments;
    private LocalTime startTime;
    private LocalTime endTime;
    private int availableSlots;
    private int bookedSlots;
    private Double dailyRevenue;
    private String mostCommonReason;
}