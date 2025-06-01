package com.example.miapp.controller;

import com.example.miapp.dto.auth.MessageResponse;
import com.example.miapp.dto.doctor.CreateDoctorRequest;
import com.example.miapp.dto.doctor.DoctorDto;
import com.example.miapp.dto.doctor.DoctorScheduleDto;
import com.example.miapp.dto.doctor.DoctorSearchCriteria;
import com.example.miapp.exception.DoctorValidationException;
import com.example.miapp.exception.ResourceNotFoundException;
import com.example.miapp.service.doctor.DoctorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

/**
 * Controlador para operaciones de doctores
 */
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Slf4j
public class DoctorController {

    private final DoctorService doctorService;

    /**
     * Endpoint para listar todos los doctores
     * Accesible por administradores, doctores y pacientes
     */
    @GetMapping
    public ResponseEntity<?> getAllDoctors(
            @PageableDefault(size = 10, sort = "lastName") Pageable pageable) {
        
        log.info("Solicitud para listar todos los doctores");
        
        try {
            Page<DoctorDto> doctors = doctorService.getAllDoctors(pageable);
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            log.error("Error al obtener doctores: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener doctores: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener un doctor por ID
     * Accesible por administradores, doctores y pacientes
     */
    @GetMapping("/{doctorId}")
    public ResponseEntity<?> getDoctorById(@PathVariable Long doctorId) {
        
        log.info("Solicitud para obtener doctor con ID: {}", doctorId);
        
        try {
            DoctorDto doctor = doctorService.getDoctor(doctorId);
            return ResponseEntity.ok(doctor);
        } catch (ResourceNotFoundException e) {
            log.warn("Doctor no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener doctor {}: {}", doctorId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener doctor: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para crear un nuevo doctor
     * Solo accesible por administradores
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        
        log.info("Solicitud para crear nuevo doctor: {} {}", request.getFirstName(), request.getLastName());
        
        try {
            DoctorDto createdDoctor = doctorService.createDoctor(request);
            return ResponseEntity.ok(createdDoctor);
        } catch (DoctorValidationException e) {
            log.warn("Error de validación al crear doctor: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al crear doctor: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al crear doctor: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para actualizar un doctor existente
     * Solo accesible por administradores
     */
    @PutMapping("/{doctorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateDoctor(
            @PathVariable Long doctorId,
            @Valid @RequestBody CreateDoctorRequest request) {
        
        log.info("Solicitud para actualizar doctor con ID: {}", doctorId);
        
        try {
            DoctorDto updatedDoctor = doctorService.updateDoctor(doctorId, request);
            return ResponseEntity.ok(updatedDoctor);
        } catch (ResourceNotFoundException e) {
            log.warn("Doctor no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (DoctorValidationException e) {
            log.warn("Error de validación al actualizar doctor: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al actualizar doctor {}: {}", doctorId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al actualizar doctor: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para eliminar un doctor
     * Solo accesible por administradores
     */
    @DeleteMapping("/{doctorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long doctorId) {
        
        log.info("Solicitud para eliminar doctor con ID: {}", doctorId);
        
        try {
            doctorService.deleteDoctor(doctorId);
            return ResponseEntity.ok(new MessageResponse("Doctor eliminado exitosamente"));
        } catch (ResourceNotFoundException e) {
            log.warn("Doctor no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (DoctorValidationException e) {
            log.warn("Error de validación al eliminar doctor: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al eliminar doctor {}: {}", doctorId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al eliminar doctor: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para buscar doctores por nombre
     * Accesible para todos
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchDoctorsByName(
            @RequestParam String name,
            @PageableDefault(size = 10, sort = "lastName") Pageable pageable) {
        
        log.info("Solicitud para buscar doctores por nombre: {}", name);
        
        try {
            Page<DoctorDto> doctors = doctorService.searchDoctorsByName(name, pageable);
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            log.error("Error al buscar doctores por nombre: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al buscar doctores: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para búsqueda avanzada de doctores
     * Accesible para todos
     */
    @PostMapping("/advanced-search")
    public ResponseEntity<?> advancedSearch(
            @RequestBody DoctorSearchCriteria criteria,
            @PageableDefault(size = 10, sort = "lastName") Pageable pageable) {
        
        log.info("Solicitud para búsqueda avanzada de doctores: {}", criteria);
        
        try {
            Page<DoctorDto> doctors = doctorService.searchDoctors(criteria, pageable);
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            log.error("Error en búsqueda avanzada de doctores: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al buscar doctores: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener doctores por especialidad
     * Accesible para todos
     */
    @GetMapping("/by-specialty/{specialtyId}")
    public ResponseEntity<?> getDoctorsBySpecialty(
            @PathVariable Long specialtyId,
            @PageableDefault(size = 10, sort = "lastName") Pageable pageable) {
        
        log.info("Solicitud para obtener doctores por especialidad ID: {}", specialtyId);
        
        try {
            Page<DoctorDto> doctors = doctorService.findDoctorsBySpecialty(specialtyId, pageable);
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            log.error("Error al obtener doctores por especialidad: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener doctores: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener doctores disponibles en un día y horario específicos
     * Accesible para todos
     */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableDoctors(
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @PageableDefault(size = 10, sort = "lastName") Pageable pageable) {
        
        log.info("Solicitud para obtener doctores disponibles en {} desde {} hasta {}", 
                dayOfWeek, startTime, endTime);
        
        try {
            Page<DoctorDto> doctors = doctorService.findAvailableDoctors(dayOfWeek, startTime, endTime, pageable);
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            log.error("Error al obtener doctores disponibles: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener doctores disponibles: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener doctores por rango de tarifa de consulta
     * Accesible para todos
     */
    @GetMapping("/by-fee")
    public ResponseEntity<?> getDoctorsByFeeRange(
            @RequestParam Double minFee,
            @RequestParam Double maxFee,
            @PageableDefault(size = 10, sort = "consultationFee") Pageable pageable) {
        
        log.info("Solicitud para obtener doctores por rango de tarifa: {} - {}", minFee, maxFee);
        
        try {
            Page<DoctorDto> doctors = doctorService.findDoctorsByConsultationFeeRange(minFee, maxFee, pageable);
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            log.error("Error al obtener doctores por rango de tarifa: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener doctores: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para actualizar la tarifa de consulta de un doctor
     * Solo accesible por administradores
     */
    @PatchMapping("/{doctorId}/fee")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateConsultationFee(
            @PathVariable Long doctorId,
            @RequestParam Double consultationFee) {
        
        log.info("Solicitud para actualizar tarifa de consulta del doctor {} a {}", 
                doctorId, consultationFee);
        
        try {
            doctorService.updateConsultationFee(doctorId, consultationFee);
            return ResponseEntity.ok(new MessageResponse("Tarifa de consulta actualizada exitosamente"));
        } catch (ResourceNotFoundException e) {
            log.warn("Doctor no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (DoctorValidationException e) {
            log.warn("Error de validación al actualizar tarifa: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al actualizar tarifa del doctor {}: {}", doctorId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al actualizar tarifa: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para actualizar la biografía de un doctor
     * Accesible por administradores y el propio doctor
     */
    @PatchMapping("/{doctorId}/biography")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentDoctor(#doctorId)")
    public ResponseEntity<?> updateBiography(
            @PathVariable Long doctorId,
            @RequestParam String biography) {
        
        log.info("Solicitud para actualizar biografía del doctor {}", doctorId);
        
        try {
            doctorService.updateBiography(doctorId, biography);
            return ResponseEntity.ok(new MessageResponse("Biografía actualizada exitosamente"));
        } catch (ResourceNotFoundException e) {
            log.warn("Doctor no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al actualizar biografía del doctor {}: {}", doctorId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al actualizar biografía: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para agregar una especialidad a un doctor
     * Solo accesible por administradores
     */
    @PostMapping("/{doctorId}/specialties/{specialtyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addSpecialtyToDoctor(
            @PathVariable Long doctorId,
            @PathVariable Long specialtyId,
            @RequestParam(required = false, defaultValue = "Junior") String experienceLevel,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date certificationDate) {
        
        log.info("Solicitud para agregar especialidad {} al doctor {}", specialtyId, doctorId);
        
        try {
            if (certificationDate == null) {
                certificationDate = new Date(); // Fecha actual por defecto
            }
            
            doctorService.addSpecialtyToDoctor(doctorId, specialtyId, experienceLevel, certificationDate);
            return ResponseEntity.ok(new MessageResponse("Especialidad agregada exitosamente"));
        } catch (ResourceNotFoundException e) {
            log.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (DoctorValidationException e) {
            log.warn("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al agregar especialidad {} al doctor {}: {}", 
                    specialtyId, doctorId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al agregar especialidad: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para eliminar una especialidad de un doctor
     * Solo accesible por administradores
     */
    @DeleteMapping("/{doctorId}/specialties/{specialtyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeSpecialtyFromDoctor(
            @PathVariable Long doctorId,
            @PathVariable Long specialtyId) {
        
        log.info("Solicitud para eliminar especialidad {} del doctor {}", specialtyId, doctorId);
        
        try {
            doctorService.removeSpecialtyFromDoctor(doctorId, specialtyId);
            return ResponseEntity.ok(new MessageResponse("Especialidad eliminada exitosamente"));
        } catch (ResourceNotFoundException e) {
            log.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (DoctorValidationException e) {
            log.warn("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al eliminar especialidad {} del doctor {}: {}", 
                    specialtyId, doctorId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al eliminar especialidad: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para agregar un horario de trabajo a un doctor
     * Accesible por administradores y el propio doctor
     */
    @PostMapping("/{doctorId}/schedules")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentDoctor(#doctorId)")
    public ResponseEntity<?> addWorkSchedule(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorScheduleDto scheduleDto) {
        
        log.info("Solicitud para agregar horario al doctor {} para el día {}", 
                doctorId, scheduleDto.getDayOfWeek());
        
        try {
            doctorService.addWorkSchedule(doctorId, scheduleDto);
            return ResponseEntity.ok(new MessageResponse("Horario agregado exitosamente"));
        } catch (ResourceNotFoundException e) {
            log.warn("Doctor no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (DoctorValidationException e) {
            log.warn("Error de validación al agregar horario: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al agregar horario al doctor {}: {}", doctorId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al agregar horario: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para actualizar un horario de trabajo de un doctor
     * Accesible por administradores y el propio doctor
     */
    @PutMapping("/{doctorId}/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentDoctor(#doctorId)")
    public ResponseEntity<?> updateWorkSchedule(
            @PathVariable Long doctorId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody DoctorScheduleDto scheduleDto) {
        
        log.info("Solicitud para actualizar horario {} del doctor {}", scheduleId, doctorId);
        
        try {
            scheduleDto.setId(scheduleId); // Asegurar que el ID coincida
            doctorService.updateWorkSchedule(doctorId, scheduleDto);
            return ResponseEntity.ok(new MessageResponse("Horario actualizado exitosamente"));
        } catch (ResourceNotFoundException e) {
            log.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (DoctorValidationException e) {
            log.warn("Error de validación al actualizar horario: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al actualizar horario {} del doctor {}: {}", 
                    scheduleId, doctorId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al actualizar horario: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para eliminar un horario de trabajo de un doctor
     * Accesible por administradores y el propio doctor
     */
    @DeleteMapping("/{doctorId}/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentDoctor(#doctorId)")
    public ResponseEntity<?> removeWorkSchedule(
            @PathVariable Long doctorId,
            @PathVariable Long scheduleId) {
        
        log.info("Solicitud para eliminar horario {} del doctor {}", scheduleId, doctorId);
        
        try {
            doctorService.removeWorkSchedule(doctorId, scheduleId);
            return ResponseEntity.ok(new MessageResponse("Horario eliminado exitosamente"));
        } catch (ResourceNotFoundException e) {
            log.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al eliminar horario {} del doctor {}: {}", 
                    scheduleId, doctorId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al eliminar horario: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener estadísticas de citas por doctor
     * Solo accesible por administradores
     */
    @GetMapping("/appointment-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAppointmentStatsByDoctor(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Solicitud para obtener estadísticas de citas por doctor desde {} hasta {}", 
                startDate, endDate);
        
        try {
            List<Object[]> stats = doctorService.getAppointmentStatsByDoctor(startDate, endDate);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas de citas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener estadísticas: " + e.getMessage()));
        }
    }
}