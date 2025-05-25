package com.example.miapp.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for patient-specific dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDashboardDto {
    private Long patientId;
    private String patientName;
    private int age;
    private String bloodType;
    private List<AppointmentSummaryDto> upcomingAppointments;
    private List<MedicalEntrySummaryDto> recentMedicalEntries;
    private List<PrescriptionSummaryDto> activePrescriptions;
    private AppointmentHistorySummaryDto appointmentHistory;
    private LocalDateTime nextAppointment;
    private String preferredDoctor;
    private List<String> chronicConditions;
    private List<String> allergies;
    private String insuranceProvider;
    private String emergencyContact;
}