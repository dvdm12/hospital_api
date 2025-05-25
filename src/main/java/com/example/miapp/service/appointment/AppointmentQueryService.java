package com.example.miapp.service.appointment;

import com.example.miapp.dto.appointment.AppointmentDto;
import com.example.miapp.dto.appointment.AppointmentSearchCriteria;
import com.example.miapp.dto.appointment.AvailableSlotDto;
import com.example.miapp.entity.Appointment;
import com.example.miapp.mapper.AppointmentMapper;
import com.example.miapp.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for appointment queries (Single Responsibility)
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AppointmentQueryService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    /**
     * Gets appointment by ID
     */
    public AppointmentDto getAppointment(Long appointmentId) {
        Appointment appointment = findAppointmentById(appointmentId);
        return appointmentMapper.toDto(appointment);
    }

    /**
     * Gets available time slots for a doctor
     */
    public List<AvailableSlotDto> getAvailableSlots(Long doctorId, LocalDate date) {
        log.info("Finding available slots for doctor {} on {}", doctorId, date);
        
        List<Object[]> results = appointmentRepository.findAvailableTimeSlots(doctorId, date.atStartOfDay());
        return appointmentMapper.mapToAvailableSlots(results);
    }

    /**
     * Gets appointments for a patient
     */
    public Page<AppointmentDto> getPatientAppointments(Long patientId, Pageable pageable) {
        log.info("Fetching appointments for patient {}", patientId);
        
        Page<Appointment> appointments = appointmentRepository.findByPatientId(patientId, pageable);
        return appointments.map(appointmentMapper::toDto);
    }

    /**
     * Gets appointments for a doctor
     */
    public Page<AppointmentDto> getDoctorAppointments(Long doctorId, Pageable pageable) {
        log.info("Fetching appointments for doctor {}", doctorId);
        
        Page<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId, pageable);
        return appointments.map(appointmentMapper::toDto);
    }

    /**
     * Gets today's appointments for a doctor
     */
    public List<AppointmentDto> getTodayAppointments(Long doctorId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        Page<Appointment> appointments = appointmentRepository.findByDoctorIdAndDateBetween(
                doctorId, startOfDay, endOfDay, Pageable.unpaged());

        return appointments.map(appointmentMapper::toDto).getContent();
    }

    /**
     * Searches appointments with criteria
     */
    public Page<AppointmentDto> searchAppointments(AppointmentSearchCriteria criteria, Pageable pageable) {
        log.info("Searching appointments with criteria: {}", criteria);

        Page<Appointment> appointments = appointmentRepository.searchAppointments(
                criteria.getDoctorId(),
                criteria.getPatientId(),
                criteria.getStatus(),
                criteria.getStartDate(),
                criteria.getEndDate(),
                criteria.getReasonPattern(),
                pageable
        );

        return appointments.map(appointmentMapper::toDto);
    }

    /**
     * Gets next appointment for a patient
     */
    public Optional<AppointmentDto> getNextAppointment(Long patientId) {
        Optional<Appointment> nextAppointment = appointmentRepository.findNextAppointmentForPatient(
                patientId, LocalDateTime.now());

        return nextAppointment.map(appointmentMapper::toDto);
    }

    /**
     * Gets appointment statistics by status
     */
    public List<Object[]> getAppointmentStatusStats() {
        return appointmentRepository.countAppointmentsByStatus();
    }

    /**
     * Finds appointment by ID (internal use)
     */
    public Appointment findAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + appointmentId));
    }
}