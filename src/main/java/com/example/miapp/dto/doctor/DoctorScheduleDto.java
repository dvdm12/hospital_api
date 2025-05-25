package com.example.miapp.dto.doctor;

import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * DTO for Doctor Schedule
 */
@Data
public class DoctorScheduleDto {
    private Long id;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotDurationMinutes;
    private boolean active;
    private String location;
}