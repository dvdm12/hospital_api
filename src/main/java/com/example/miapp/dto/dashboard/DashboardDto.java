package com.example.miapp.dto.dashboard;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO for Hospital Dashboard statistics
 */
@Data
public class DashboardDto {
    private LocalDate date;
    private DashboardStatsDto stats;
    private List<AppointmentStatusStatsDto> appointmentStats;
    private List<SpecialtyStatsDto> specialtyStats;
    private List<DoctorStatsDto> doctorStats;
    private Map<String, Integer> appointmentsByHour;
    private Map<String, Integer> appointmentsByDay;
    private List<AlertDto> alerts;
}