package com.example.miapp.dto.appointment;

import com.example.miapp.entity.Appointment.AppointmentStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for appointment search criteria
 */
@Data
public class AppointmentSearchCriteria {
    private Long doctorId;
    private Long patientId;
    private AppointmentStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reasonPattern;
    private Boolean confirmed;
    private String location;
}