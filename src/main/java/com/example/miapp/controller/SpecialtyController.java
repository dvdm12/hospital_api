package com.example.miapp.controller;

import com.example.miapp.dto.auth.MessageResponse;
import com.example.miapp.dto.specialty.CreateSpecialtyRequest;
import com.example.miapp.dto.specialty.SpecialtyDto;
import com.example.miapp.exception.ResourceNotFoundException;
import com.example.miapp.service.specialty.SpecialtyService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de especialidades médicas.
 */
@RestController
@RequestMapping("/api/specialties")
@RequiredArgsConstructor
@Slf4j
@Validated
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    /**
     * Obtiene todas las especialidades.
     *
     * @param pageable Información de paginación
     * @return ResponseEntity con página de especialidades
     */
    @GetMapping
    public ResponseEntity<?> getAllSpecialties(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        
        log.info("Solicitud para listar todas las especialidades");
        
        try {
            Page<SpecialtyDto> specialties = specialtyService.getAllSpecialties(pageable);
            return ResponseEntity.ok(specialties);
        } catch (Exception e) {
            log.error("Error al obtener especialidades: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener especialidades: " + e.getMessage()));
        }
    }

    /**
     * Obtiene una especialidad por su ID.
     *
     * @param id ID de la especialidad
     * @return ResponseEntity con la especialidad
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSpecialtyById(@PathVariable Long id) {
        log.info("Solicitud para obtener especialidad con ID: {}", id);
        
        try {
            SpecialtyDto specialty = specialtyService.getSpecialtyById(id);
            return ResponseEntity.ok(specialty);
        } catch (ResourceNotFoundException e) {
            log.warn("Especialidad no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener especialidad: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener especialidad: " + e.getMessage()));
        }
    }

    /**
     * Busca especialidades por nombre.
     *
     * @param name Nombre o parte del nombre a buscar
     * @param pageable Información de paginación
     * @return ResponseEntity con página de especialidades
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchSpecialties(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        
        log.info("Solicitud para buscar especialidades con nombre que contenga: {}", name);
        
        try {
            Page<SpecialtyDto> specialties = specialtyService.searchSpecialtiesByName(name, pageable);
            return ResponseEntity.ok(specialties);
        } catch (Exception e) {
            log.error("Error al buscar especialidades: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al buscar especialidades: " + e.getMessage()));
        }
    }

   /**
 * Crea una nueva especialidad.
 * Solo accesible por administradores.
 *
 * @param request DTO con datos de la especialidad
 * @return ResponseEntity con la especialidad creada
 */
@PostMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> createSpecialty(@Valid @RequestBody CreateSpecialtyRequest request) {
    log.info("Solicitud para crear nueva especialidad: {}", request.getName());
    
    try {
        SpecialtyDto createdSpecialty = specialtyService.createSpecialty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSpecialty);
    } catch (IllegalArgumentException e) {
        log.warn("Error de validación al crear especialidad: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(new MessageResponse(e.getMessage()));
    } catch (Exception e) {
        log.error("Error al crear especialidad: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError()
                .body(new MessageResponse("Error al crear especialidad: " + e.getMessage()));
    }
}

/**
 * Actualiza una especialidad existente.
 * Solo accesible por administradores.
 *
 * @param id ID de la especialidad
 * @param request DTO con datos actualizados
 * @return ResponseEntity con la especialidad actualizada
 */
@PutMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> updateSpecialty(
        @PathVariable Long id,
        @Valid @RequestBody CreateSpecialtyRequest request) {
    
    log.info("Solicitud para actualizar especialidad con ID: {}", id);
    
    try {
        SpecialtyDto updatedSpecialty = specialtyService.updateSpecialty(id, request);
        return ResponseEntity.ok(updatedSpecialty);
    } catch (ResourceNotFoundException e) {
        log.warn("Especialidad no encontrada: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(e.getMessage()));
    } catch (IllegalArgumentException e) {
        log.warn("Error de validación al actualizar especialidad: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(new MessageResponse(e.getMessage()));
    } catch (Exception e) {
        log.error("Error al actualizar especialidad: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError()
                .body(new MessageResponse("Error al actualizar especialidad: " + e.getMessage()));
    }
}

    /**
     * Elimina una especialidad.
     * Solo accesible por administradores.
     *
     * @param id ID de la especialidad
     * @return ResponseEntity con mensaje de resultado
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSpecialty(@PathVariable Long id) {
        log.info("Solicitud para eliminar especialidad con ID: {}", id);
        
        try {
            specialtyService.deleteSpecialty(id);
            return ResponseEntity.ok(new MessageResponse("Especialidad eliminada exitosamente"));
        } catch (ResourceNotFoundException e) {
            log.warn("Especialidad no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("No se puede eliminar la especialidad: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al eliminar especialidad: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al eliminar especialidad: " + e.getMessage()));
        }
    }

    /**
     * Obtiene las especialidades más populares.
     *
     * @param limit Número máximo de especialidades a retornar
     * @return ResponseEntity con lista de especialidades
     */
    @GetMapping("/popular")
    public ResponseEntity<?> getMostPopularSpecialties(
            @RequestParam(defaultValue = "5") @Min(1) int limit) {
        
        log.info("Solicitud para obtener las {} especialidades más populares", limit);
        
        try {
            List<SpecialtyDto> specialties = specialtyService.getMostPopularSpecialties(limit);
            return ResponseEntity.ok(specialties);
        } catch (Exception e) {
            log.error("Error al obtener especialidades populares: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener especialidades populares: " + e.getMessage()));
        }
    }

    /**
     * Obtiene especialidades relacionadas con una especialidad dada.
     *
     * @param id ID de la especialidad de referencia
     * @param limit Número máximo de especialidades a retornar
     * @return ResponseEntity con lista de especialidades relacionadas
     */
    @GetMapping("/{id}/related")
    public ResponseEntity<?> getRelatedSpecialties(
            @PathVariable Long id,
            @RequestParam(defaultValue = "3") @Min(1) int limit) {
        
        log.info("Solicitud para obtener especialidades relacionadas con ID: {}", id);
        
        try {
            List<SpecialtyDto> relatedSpecialties = specialtyService.getRelatedSpecialties(id, limit);
            return ResponseEntity.ok(relatedSpecialties);
        } catch (ResourceNotFoundException e) {
            log.warn("Especialidad no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener especialidades relacionadas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener especialidades relacionadas: " + e.getMessage()));
        }
    }

    /**
     * Obtiene especialidades sin doctores asignados.
     * Solo accesible por administradores.
     *
     * @return ResponseEntity con lista de especialidades
     */
    @GetMapping("/unused")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUnusedSpecialties() {
        log.info("Solicitud para obtener especialidades sin doctores");
        
        try {
            List<SpecialtyDto> unusedSpecialties = specialtyService.getUnusedSpecialties();
            return ResponseEntity.ok(unusedSpecialties);
        } catch (Exception e) {
            log.error("Error al obtener especialidades sin uso: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener especialidades sin uso: " + e.getMessage()));
        }
    }

    /**
     * Obtiene estadísticas de doctores por especialidad.
     * Solo accesible por administradores.
     *
     * @return ResponseEntity con estadísticas
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSpecialtyStatistics() {
        log.info("Solicitud para obtener estadísticas de especialidades");
        
        try {
            List<Object[]> statistics = specialtyService.getDoctorCountBySpecialty();
            
            // Convertir a formato más amigable para JSON
            List<Map<String, Object>> formattedStats = statistics.stream()
                    .map(stat -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("specialtyId", stat[0]);
                        item.put("specialtyName", stat[1]);
                        item.put("doctorCount", stat[2]);
                        return item;
                    })
                    .toList();
            
            return ResponseEntity.ok(formattedStats);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas de especialidades: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener estadísticas: " + e.getMessage()));
        }
    }
}