package com.example.miapp.service.appointment;

import com.example.miapp.entity.Appointment.AppointmentStatus;
import com.example.miapp.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service responsible for appointment state transitions (Single Responsibility)
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AppointmentStateService {

    private final AppointmentRepository appointmentRepository;

    /**
     * Confirms an appointment
     */
    public void confirmAppointment(Long appointmentId) {
        log.info("Confirming appointment with ID: {}", appointmentId);
        
        appointmentRepository.updateConfirmation(appointmentId, true);
        appointmentRepository.updateStatus(appointmentId, AppointmentStatus.CONFIRMED, null);
        
        log.info("Appointment {} confirmed successfully", appointmentId);
    }

    /**
     * Cancels an appointment
     */
    public void cancelAppointment(Long appointmentId, String reason) {
        log.info("Canceling appointment with ID: {} with reason: {}", appointmentId, reason);
        
        appointmentRepository.updateStatus(appointmentId, AppointmentStatus.CANCELED, null);
        appointmentRepository.updateNotes(appointmentId, "Canceled: " + reason, null);
        
        log.info("Appointment {} canceled successfully", appointmentId);
    }

    /**
     * Completes an appointment
     */
    public void completeAppointment(Long appointmentId, String notes) {
        log.info("Completing appointment with ID: {}", appointmentId);
        
        appointmentRepository.updateStatus(appointmentId, AppointmentStatus.COMPLETED, null);
        
        if (notes != null && !notes.trim().isEmpty()) {
            appointmentRepository.updateNotes(appointmentId, notes, null);
        }
        
        log.info("Appointment {} completed successfully", appointmentId);
    }

    /**
     * Marks appointment as no-show
     */
    public void markAsNoShow(Long appointmentId) {
        log.info("Marking appointment {} as no-show", appointmentId);
        
        appointmentRepository.updateStatus(appointmentId, AppointmentStatus.NO_SHOW, null);
        
        log.info("Appointment {} marked as no-show", appointmentId);
    }

    /**
     * Reschedules an appointment
     */
    public void rescheduleAppointment(Long appointmentId, LocalDateTime newDate, LocalDateTime newEndTime) {
        log.info("Rescheduling appointment {} to {}", appointmentId, newDate);
        
        appointmentRepository.rescheduleAppointment(appointmentId, newDate, newEndTime, null);
        
        log.info("Appointment {} rescheduled successfully", appointmentId);
    }
}