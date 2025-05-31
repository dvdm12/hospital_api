package com.example.miapp.service.appointment;

import com.example.miapp.dto.appointment.AppointmentDto;
import com.example.miapp.entity.Appointment;
import com.example.miapp.exception.ResourceNotFoundException;
import com.example.miapp.mapper.ManualAppointmentMapper;
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

/**
 * Servicio responsable de consultas de citas
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AppointmentQueryService {

    private final AppointmentRepository appointmentRepository;
    private final ManualAppointmentMapper appointmentMapper;

    /**
     * Obtiene todas las citas del sistema
     */
    public Page<AppointmentDto> getAllAppointments(Pageable pageable) {
        log.info("Obteniendo todas las citas del sistema");
        
        try {
            Page<Appointment> appointments = appointmentRepository.findAll(pageable);
            log.debug("Encontradas {} citas", appointments.getTotalElements());
            
            return appointmentMapper.toDtoPage(appointments);
        } catch (Exception e) {
            log.error("Error al obtener citas: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Obtiene las citas de un día específico
     */
    public Page<AppointmentDto> getAppointmentsByDay(LocalDate date, Pageable pageable) {
        log.info("Obteniendo citas para el día: {}", date);
        
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            
            Page<Appointment> appointments = appointmentRepository.findByDateBetween(
                    startOfDay, endOfDay, pageable);
            
            log.debug("Encontradas {} citas para el día {}", appointments.getTotalElements(), date);
            
            return appointmentMapper.toDtoPage(appointments);
        } catch (Exception e) {
            log.error("Error al obtener citas del día {}: {}", date, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Encuentra una cita por su ID
     */
    public Appointment findAppointmentById(Long appointmentId) {
        log.info("Buscando cita con ID: {}", appointmentId);
        
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> {
                    log.error("Cita no encontrada con ID: {}", appointmentId);
                    return new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId);
                });
    }
}