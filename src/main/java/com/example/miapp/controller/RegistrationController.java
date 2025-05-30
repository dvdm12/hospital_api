package com.example.miapp.controller;

import com.example.miapp.dto.doctor.CreateDoctorRequest;
import com.example.miapp.dto.patient.CreatePatientRequest;
import com.example.miapp.dto.user.UserDto;
import com.example.miapp.service.registration.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Controlador REST para el registro de diferentes tipos de usuarios en el sistema.
 * Solo administradores pueden crear usuarios.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {

    private final RegistrationService registrationService;

    /**
     * Endpoint para registrar un nuevo administrador.
     * Solo accesible para usuarios con rol ADMIN.
     *
     * @param request Mapa con los datos del administrador
     * @return ResponseEntity con los datos del usuario creado
     */
    @PostMapping("/api/admin/register/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerAdmin(@RequestBody Map<String, String> request) {
        log.info("Solicitud recibida para registrar administrador: {}", request.get("username"));
        
        // Validar campos requeridos
        if (!request.containsKey("username") || !request.containsKey("email") || !request.containsKey("password")) {
            log.warn("Solicitud de registro de administrador con campos faltantes");
            return ResponseEntity
                    .badRequest()
                    .body(createErrorResponse("Se requieren los campos username, email y password"));
        }
        
        try {
            UserDto userDto = registrationService.registerAdmin(
                    request.get("username"),
                    request.get("email"),
                    request.get("password"));
            
            log.info("Administrador registrado exitosamente: {}", userDto.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
        } catch (Exception e) {
            log.error("Error al registrar administrador: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Endpoint para registrar un nuevo paciente.
     * Solo accesible para usuarios con rol ADMIN.
     *
     * @param request DTO con los datos del paciente
     * @return ResponseEntity con los datos del usuario creado
     */
    @PostMapping("/api/admin/register/patient")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerPatient(@Valid @RequestBody CreatePatientRequest request) {
        log.info("Solicitud recibida para registrar paciente: {}", request.getUsername());
        
        try {
            UserDto userDto = registrationService.registerPatient(request);
            
            log.info("Paciente registrado exitosamente: {}", userDto.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
        } catch (Exception e) {
            log.error("Error al registrar paciente: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Endpoint para registrar un nuevo doctor.
     * Solo accesible para usuarios con rol ADMIN.
     *
     * @param request DTO con los datos del doctor
     * @return ResponseEntity con los datos del usuario creado
     */
    @PostMapping("/api/admin/register/doctor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerDoctor(
            @Valid @RequestBody CreateDoctorRequest request,
            @RequestParam(required = false) Set<Long> specialtyIds) {
        
        log.info("Solicitud recibida para registrar doctor: {}", request.getUsername());
        
        try {
            UserDto userDto = registrationService.registerDoctor(request, specialtyIds);
            
            log.info("Doctor registrado exitosamente: {}", userDto.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al registrar doctor: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al registrar doctor: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Crea un mapa de respuesta de error.
     *
     * @param message Mensaje de error
     * @return Mapa con el mensaje de error
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return errorResponse;
    }
}