package com.example.miapp.dto.appointment;

import com.example.miapp.entity.Appointment.AppointmentStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for Appointment entity responses
 */
@Data
public class AppointmentDto {
    private Long id;
    private LocalDateTime date;
    private LocalDateTime endTime;
    private String doctorName;
    private String patientName;
    private String reason;
    private AppointmentStatus status;
    private String notes;
    private boolean confirmed;
    private LocalDateTime confirmationDate;
    private String location;
    private long durationMinutes;
    private boolean overdue;
}