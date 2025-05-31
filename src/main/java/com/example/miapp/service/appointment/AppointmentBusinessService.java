package com.example.miapp.service.appointment;

import com.example.miapp.dto.appointment.AppointmentDto;
import com.example.miapp.exception.AppointmentValidationException;
import com.example.miapp.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Servicio principal que orquesta operaciones de citas
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AppointmentBusinessService {

    private final AppointmentStateService stateService;
    private final AppointmentQueryService queryService;

    /**
     * Lista todas las citas del sistema
     */
    @Transactional(readOnly = true)
    public Page<AppointmentDto> getAllAppointments(Pageable pageable) {
        log.info("Solicitando listado de todas las citas");
        
        Page<AppointmentDto> appointments = queryService.getAllAppointments(pageable);
        
        if (appointments.isEmpty()) {
            log.warn("No se encontraron citas en el sistema");
            throw new ResourceNotFoundException("No hay citas registradas en el sistema");
        }
        
        return appointments;
    }
    
    /**
     * Lista las citas de un día específico
     */
    @Transactional(readOnly = true)
    public Page<AppointmentDto> getAppointmentsByDay(LocalDate date, Pageable pageable) {
        // Si no se proporciona fecha, usar la fecha actual
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        
        log.info("Solicitando listado de citas para el día: {}", targetDate);
        
        Page<AppointmentDto> appointments = queryService.getAppointmentsByDay(targetDate, pageable);
        
        if (appointments.isEmpty()) {
            log.warn("No se encontraron citas para el día: {}", targetDate);
            throw new ResourceNotFoundException("No hay citas programadas para el día " + targetDate);
        }
        
        return appointments;
    }

    /**
     * Cancela una cita con notificación a los usuarios involucrados
     */
    public void cancelAppointment(Long appointmentId, String reason) {
        log.info("Solicitando cancelación de cita: {} con motivo: {}", appointmentId, reason);
        
        try {
            stateService.cancelAppointment(appointmentId, reason);
        } catch (ResourceNotFoundException | AppointmentValidationException e) {
            // Estas excepciones ya tienen mensajes apropiados, solo las propagamos
            throw e;
        } catch (Exception e) {
            // Cualquier otra excepción la envolvemos para dar un mensaje más claro
            log.error("Error inesperado al cancelar cita {}: {}", appointmentId, e.getMessage(), e);
            throw new RuntimeException("Error al cancelar la cita: " + e.getMessage(), e);
        }
    }
}