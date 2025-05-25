package com.example.miapp.service.appointment;

import com.example.miapp.dto.appointment.CreateAppointmentRequest;
import com.example.miapp.entity.Appointment;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.Appointment.AppointmentStatus;
import com.example.miapp.exception.AppointmentValidationException;
import com.example.miapp.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsible for appointment validations (Single Responsibility)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentValidationService {

    private final AppointmentRepository appointmentRepository;

    /**
     * Validates if an appointment can be scheduled
     */
    public void validateAppointmentCreation(CreateAppointmentRequest request, Doctor doctor) {
        validateAppointmentTime(request);
        validateDoctorAvailability(doctor, request.getDate());
        validateNoConflicts(request.getDoctorId(), request.getDate(), 
                          calculateEndTime(request), null);
    }

    /**
     * Validates if an appointment can be rescheduled
     */
    public void validateAppointmentReschedule(Long appointmentId, LocalDateTime newDate, 
                                            LocalDateTime newEndTime, Long doctorId) {
        validateNoConflicts(doctorId, newDate, newEndTime, appointmentId);
    }

    /**
     * Validates if an appointment can be confirmed
     */
    public void validateAppointmentConfirmation(Appointment appointment) {
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new AppointmentValidationException(
                "Only scheduled appointments can be confirmed. Current status: " + appointment.getStatus());
        }
    }

    /**
     * Validates if an appointment can be canceled
     */
    public void validateAppointmentCancellation(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new AppointmentValidationException("Cannot cancel a completed appointment");
        }
    }

    /**
     * Validates if an appointment can be completed
     */
    public void validateAppointmentCompletion(Appointment appointment) {
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED && 
            appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new AppointmentValidationException(
                "Only confirmed or scheduled appointments can be completed. Current status: " + appointment.getStatus());
        }
    }

    // Private validation methods

    private void validateAppointmentTime(CreateAppointmentRequest request) {
        if (request.getDate().isBefore(LocalDateTime.now())) {
            throw new AppointmentValidationException("Appointment date cannot be in the past");
        }
        
        LocalDateTime endTime = calculateEndTime(request);
        if (endTime.isBefore(request.getDate())) {
            throw new AppointmentValidationException("End time cannot be before start time");
        }
    }

    private void validateDoctorAvailability(Doctor doctor, LocalDateTime appointmentDateTime) {
        if (!doctor.isAvailable(appointmentDateTime.getDayOfWeek(), appointmentDateTime.toLocalTime())) {
            throw new AppointmentValidationException(
                String.format("Doctor is not available on %s at %s", 
                             appointmentDateTime.getDayOfWeek(), 
                             appointmentDateTime.toLocalTime()));
        }
    }

    private void validateNoConflicts(Long doctorId, LocalDateTime startTime, LocalDateTime endTime, Long excludeAppointmentId) {
        List<Appointment> overlappingAppointments = appointmentRepository.findOverlappingAppointments(
                doctorId, startTime, endTime, excludeAppointmentId);

        if (!overlappingAppointments.isEmpty()) {
            throw new AppointmentValidationException("Doctor is not available at the requested time. Conflicting appointments found.");
        }
    }

    private LocalDateTime calculateEndTime(CreateAppointmentRequest request) {
        return request.getEndTime() != null ? request.getEndTime() : request.getDate().plusMinutes(30);
    }
}