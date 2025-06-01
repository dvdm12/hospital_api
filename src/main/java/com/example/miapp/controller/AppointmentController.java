package com.example.miapp.controller;

import com.example.miapp.dto.appointment.AppointmentDto;
import com.example.miapp.dto.appointment.CreateAppointmentRequest;
import com.example.miapp.dto.auth.MessageResponse;
import com.example.miapp.exception.AppointmentValidationException;
import com.example.miapp.exception.ResourceNotFoundException;
import com.example.miapp.service.appointment.AppointmentBusinessService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Controlador para operaciones de citas
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentBusinessService appointmentService;

    /**
     * Endpoint para listar todas las citas
     * Solo accesible por administradores
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllAppointments(
            @PageableDefault(size = 10, sort = "date") Pageable pageable) {
        
        log.info("Solicitud para listar todas las citas del sistema");
        
        try {
            Page<AppointmentDto> appointments = appointmentService.getAllAppointments(pageable);
            return ResponseEntity.ok(appointments);
        } catch (ResourceNotFoundException e) {
            log.warn("No se encontraron citas: {}", e.getMessage());
            return ResponseEntity.ok(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener las citas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener las citas: " + e.getMessage()));
        }
    }

   @GetMapping("/day")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> getAppointmentsByDay(
        @RequestParam(required = false) String date,
        @PageableDefault(size = 10, sort = "date") Pageable pageable) {

    log.info("Solicitud para listar citas del día: {}", date != null ? date : "hoy");

    LocalDate parsedDate = null;
    try {
        if (date != null && !date.trim().isEmpty()) {
            parsedDate = LocalDate.parse(date.trim()); // Esto limpia cualquier espacio
        }
    } catch (DateTimeParseException e) {
        log.warn("Fecha inválida: '{}'", date);
        return ResponseEntity.badRequest().body(
            new MessageResponse("Formato de fecha inválido, use YYYY-MM-DD sin espacios"));
    }

    try {
        Page<AppointmentDto> appointments = appointmentService.getAppointmentsByDay(parsedDate, pageable);
        return ResponseEntity.ok(appointments);
    } catch (ResourceNotFoundException e) {
        log.warn("No se encontraron citas para el día indicado: {}", e.getMessage());
        return ResponseEntity.ok(new MessageResponse(e.getMessage()));
    } catch (Exception e) {
        log.error("Error al obtener las citas del día: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError()
                .body(new MessageResponse("Error al obtener las citas: " + e.getMessage()));
    }
}

/**
 * Endpoint para crear una nueva cita
 * Accesible por administradores y médicos
 */
@PostMapping
@PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
public ResponseEntity<?> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
    
    log.info("Solicitud para crear nueva cita: paciente {} con doctor {} para {}", 
            request.getPatientId(), request.getDoctorId(), request.getDate());
    
    try {
        AppointmentDto createdAppointment = appointmentService.createAppointment(request);
        return ResponseEntity.ok(createdAppointment);
    } catch (ResourceNotFoundException e) {
        log.warn("Recurso no encontrado: {}", e.getMessage());
        return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
    } catch (AppointmentValidationException e) {
        log.warn("Error de validación al crear cita: {}", e.getMessage());
        return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
    } catch (Exception e) {
        log.error("Error al crear cita: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError()
                .body(new MessageResponse("Error al crear la cita: " + e.getMessage()));
    }
}


    /**
     * Endpoint para cancelar una cita
     * Solo accesible por administradores
     */
    @PostMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long appointmentId,
            @RequestParam(required = false) String reason) {
        
        log.info("Solicitud para cancelar cita con ID: {}", appointmentId);
        
        try {
            String cancelReason = (reason != null && !reason.isEmpty()) 
                    ? reason 
                    : "Cancelada por administrador del sistema";
            
            appointmentService.cancelAppointment(appointmentId, cancelReason);
            
            return ResponseEntity.ok(new MessageResponse("Cita cancelada exitosamente"));
        } catch (ResourceNotFoundException e) {
            log.warn("Cita no encontrada: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (AppointmentValidationException e) {
            log.warn("Error de validación al cancelar cita: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al cancelar cita {}: {}", appointmentId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al cancelar la cita: " + e.getMessage()));
        }
    }
}