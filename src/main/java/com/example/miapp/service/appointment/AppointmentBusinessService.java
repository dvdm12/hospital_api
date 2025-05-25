package com.example.miapp.service.appointment;

import com.example.miapp.dto.appointment.CreateAppointmentRequest;
import com.example.miapp.dto.appointment.AppointmentDto;
import com.example.miapp.entity.Appointment;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.Patient;
import com.example.miapp.mapper.AppointmentMapper;
import com.example.miapp.repository.AppointmentRepository;
import com.example.miapp.service.doctor.DoctorService;
import com.example.miapp.service.patient.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Main business service that orchestrates appointment operations (Facade Pattern)
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AppointmentBusinessService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentValidationService validationService;
    private final AppointmentStateService stateService;
    private final AppointmentQueryService queryService;
    private final DoctorService doctorService;
    private final PatientService patientService;

    /**
     * Schedules a new appointment with complete validation
     */
    public AppointmentDto scheduleAppointment(CreateAppointmentRequest request) {
        log.info("Scheduling appointment for patient {} with doctor {} at {}", 
                request.getPatientId(), request.getDoctorId(), request.getDate());

        // Get entities through dedicated services
        Doctor doctor = doctorService.findDoctorById(request.getDoctorId());
        Patient patient = patientService.findPatientById(request.getPatientId());

        // Validate appointment creation
        validationService.validateAppointmentCreation(request, doctor);

        // Create and save appointment
        Appointment appointment = createAppointmentEntity(request, doctor, patient);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        log.info("Appointment scheduled successfully with ID: {}", savedAppointment.getId());
        return appointmentMapper.toDto(savedAppointment);
    }

    /**
     * Confirms an appointment
     */
    public void confirmAppointment(Long appointmentId) {
        Appointment appointment = queryService.findAppointmentById(appointmentId);
        validationService.validateAppointmentConfirmation(appointment);
        stateService.confirmAppointment(appointmentId);
    }

    /**
     * Cancels an appointment
     */
    public void cancelAppointment(Long appointmentId, String reason) {
        Appointment appointment = queryService.findAppointmentById(appointmentId);
        validationService.validateAppointmentCancellation(appointment);
        stateService.cancelAppointment(appointmentId, reason);
    }

    /**
     * Reschedules an appointment
     */
    public AppointmentDto rescheduleAppointment(Long appointmentId, LocalDateTime newDate, LocalDateTime newEndTime) {
        Appointment appointment = queryService.findAppointmentById(appointmentId);
        
        validationService.validateAppointmentCancellation(appointment); // Can't reschedule completed
        validationService.validateAppointmentReschedule(appointmentId, newDate, newEndTime, appointment.getDoctor().getId());
        
        stateService.rescheduleAppointment(appointmentId, newDate, newEndTime);
        
        return queryService.getAppointment(appointmentId);
    }

    /**
     * Completes an appointment
     */
    public void completeAppointment(Long appointmentId, String notes) {
        Appointment appointment = queryService.findAppointmentById(appointmentId);
        validationService.validateAppointmentCompletion(appointment);
        stateService.completeAppointment(appointmentId, notes);
    }

    /**
     * Marks appointment as no-show
     */
    public void markAsNoShow(Long appointmentId) {
        stateService.markAsNoShow(appointmentId);
    }

    /**
     * Processes no-show appointments (scheduled job)
     */
    public void processNoShowAppointments(int gracePeriodMinutes) {
        log.info("Processing no-show appointments with grace period of {} minutes", gracePeriodMinutes);

        // Calcular el tiempo límite restando el período de gracia del tiempo actual
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime thresholdTime = currentTime.minusMinutes(gracePeriodMinutes);
        
        // Usar el método de repositorio modificado con el tiempo límite calculado
        Page<Appointment> overdueAppointments = appointmentRepository.findAppointmentsToMarkAsNoShow(
                thresholdTime, Pageable.unpaged());

        for (Appointment appointment : overdueAppointments) {
            stateService.markAsNoShow(appointment.getId());
        }

        log.info("Processed {} no-show appointments", overdueAppointments.getTotalElements());
    }

    // Private helper methods

    private Appointment createAppointmentEntity(CreateAppointmentRequest request, Doctor doctor, Patient patient) {
        LocalDateTime endTime = request.getEndTime() != null ? 
                request.getEndTime() : request.getDate().plusMinutes(30);

        Appointment appointment = appointmentMapper.toEntity(request);
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setEndTime(endTime);

        return appointment;
    }
}