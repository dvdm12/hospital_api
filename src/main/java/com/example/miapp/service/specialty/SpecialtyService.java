package com.example.miapp.service.specialty;

import com.example.miapp.dto.specialty.CreateSpecialtyRequest;
import com.example.miapp.dto.specialty.SpecialtyDto;
import com.example.miapp.entity.Specialty;
import com.example.miapp.exception.ResourceNotFoundException;
import com.example.miapp.mapper.SpecialtyMapper;
import com.example.miapp.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio principal para la gestión de especialidades médicas.
 * Proporciona operaciones CRUD y búsquedas específicas para especialidades.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyMapper specialtyMapper;

    /**
     * Obtiene todas las especialidades.
     *
     * @param pageable Información de paginación
     * @return Página de DTOs de especialidades
     */
    @Transactional(readOnly = true)
    public Page<SpecialtyDto> getAllSpecialties(Pageable pageable) {
        log.info("Obteniendo todas las especialidades");
        Page<Specialty> specialties = specialtyRepository.findAll(pageable);
        return specialties.map(specialtyMapper::toDto);
    }

    /**
     * Obtiene una especialidad por su ID.
     *
     * @param id ID de la especialidad
     * @return DTO de la especialidad
     * @throws ResourceNotFoundException si la especialidad no existe
     */
    @Transactional(readOnly = true)
    public SpecialtyDto getSpecialtyById(Long id) {
        log.info("Buscando especialidad con ID: {}", id);
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Especialidad no encontrada con ID: {}", id);
                    return new ResourceNotFoundException("Especialidad no encontrada con ID: " + id);
                });
        return specialtyMapper.toDto(specialty);
    }

    /**
     * Obtiene una especialidad por su nombre.
     *
     * @param name Nombre de la especialidad
     * @return DTO de la especialidad
     * @throws ResourceNotFoundException si la especialidad no existe
     */
    @Transactional(readOnly = true)
    public SpecialtyDto getSpecialtyByName(String name) {
        log.info("Buscando especialidad con nombre: {}", name);
        Specialty specialty = specialtyRepository.findByName(name)
                .orElseThrow(() -> {
                    log.error("Especialidad no encontrada con nombre: {}", name);
                    return new ResourceNotFoundException("Especialidad no encontrada con nombre: " + name);
                });
        return specialtyMapper.toDto(specialty);
    }

    /**
     * Busca especialidades por nombre (búsqueda parcial).
     *
     * @param namePattern Patrón de nombre para buscar
     * @param pageable Información de paginación
     * @return Página de DTOs de especialidades
     */
    @Transactional(readOnly = true)
    public Page<SpecialtyDto> searchSpecialtiesByName(String namePattern, Pageable pageable) {
        log.info("Buscando especialidades que contengan: {}", namePattern);
        Page<Specialty> specialties = specialtyRepository.findByNameContainingIgnoreCase(namePattern, pageable);
        return specialties.map(specialtyMapper::toDto);
    }

/**
 * Crea una nueva especialidad.
 *
 * @param request DTO con datos de la especialidad
 * @return DTO de la especialidad creada
 * @throws IllegalArgumentException si el nombre ya existe
 */
@Transactional
public SpecialtyDto createSpecialty(CreateSpecialtyRequest request) {
    log.info("Creando nueva especialidad: {}", request.getName());
    
    // Verificar si ya existe una especialidad con ese nombre
    if (specialtyRepository.existsByName(request.getName())) {
        log.error("Ya existe una especialidad con el nombre: {}", request.getName());
        throw new IllegalArgumentException("Ya existe una especialidad con el nombre: " + request.getName());
    }
    
    Specialty specialty = new Specialty();
    specialty.setName(request.getName());
    specialty.setDescription(request.getDescription());
    
    specialty = specialtyRepository.save(specialty);
    log.info("Especialidad creada con ID: {}", specialty.getId());
    
    return specialtyMapper.toDto(specialty);
}

/**
 * Actualiza una especialidad existente.
 *
 * @param id ID de la especialidad
 * @param request DTO con datos actualizados
 * @return DTO de la especialidad actualizada
 * @throws ResourceNotFoundException si la especialidad no existe
 * @throws IllegalArgumentException si el nuevo nombre ya existe
 */
@Transactional
public SpecialtyDto updateSpecialty(Long id, CreateSpecialtyRequest request) {
    log.info("Actualizando especialidad con ID: {}", id);
    
    Specialty specialty = specialtyRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Especialidad no encontrada para actualizar, ID: {}", id);
                return new ResourceNotFoundException("Especialidad no encontrada con ID: " + id);
            });
    
    // Actualizar nombre si se proporciona y es diferente
    if (request.getName() != null && !request.getName().isEmpty() && !request.getName().equals(specialty.getName())) {
        // Verificar si el nuevo nombre ya existe
        if (specialtyRepository.existsByName(request.getName())) {
            log.error("Ya existe otra especialidad con el nombre: {}", request.getName());
            throw new IllegalArgumentException("Ya existe otra especialidad con el nombre: " + request.getName());
        }
        specialty.setName(request.getName());
    }
    
    // Actualizar descripción si se proporciona
    if (request.getDescription() != null) {
        specialty.setDescription(request.getDescription());
    }
    
    specialty = specialtyRepository.save(specialty);
    log.info("Especialidad actualizada con ID: {}", specialty.getId());
    
    return specialtyMapper.toDto(specialty);
}

    /**
     * Actualiza una especialidad existente.
     *
     * @param id ID de la especialidad
     * @param name Nuevo nombre (opcional)
     * @param description Nueva descripción (opcional)
     * @return DTO de la especialidad actualizada
     * @throws ResourceNotFoundException si la especialidad no existe
     * @throws IllegalArgumentException si el nuevo nombre ya existe
     */
    @Transactional
    public SpecialtyDto updateSpecialty(Long id, String name, String description) {
        log.info("Actualizando especialidad con ID: {}", id);
        
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Especialidad no encontrada para actualizar, ID: {}", id);
                    return new ResourceNotFoundException("Especialidad no encontrada con ID: " + id);
                });
        
        // Actualizar nombre si se proporciona y es diferente
        if (name != null && !name.isEmpty() && !name.equals(specialty.getName())) {
            // Verificar si el nuevo nombre ya existe
            if (specialtyRepository.existsByName(name)) {
                log.error("Ya existe otra especialidad con el nombre: {}", name);
                throw new IllegalArgumentException("Ya existe otra especialidad con el nombre: " + name);
            }
            specialty.setName(name);
        }
        
        // Actualizar descripción si se proporciona
        if (description != null) {
            specialty.setDescription(description);
        }
        
        specialty = specialtyRepository.save(specialty);
        log.info("Especialidad actualizada con ID: {}", specialty.getId());
        
        return specialtyMapper.toDto(specialty);
    }

    /**
     * Elimina una especialidad.
     *
     * @param id ID de la especialidad
     * @throws ResourceNotFoundException si la especialidad no existe
     * @throws IllegalStateException si la especialidad tiene doctores asociados
     */
    @Transactional
    public void deleteSpecialty(Long id) {
        log.info("Eliminando especialidad con ID: {}", id);
        
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Especialidad no encontrada para eliminar, ID: {}", id);
                    return new ResourceNotFoundException("Especialidad no encontrada con ID: " + id);
                });
        
        // Verificar si hay doctores asociados a esta especialidad
        if (specialty.getDoctorSpecialties() != null && !specialty.getDoctorSpecialties().isEmpty()) {
            log.error("No se puede eliminar la especialidad {} porque tiene doctores asociados", id);
            throw new IllegalStateException(
                    "No se puede eliminar la especialidad porque tiene " + 
                    specialty.getDoctorSpecialties().size() + " doctores asociados");
        }
        
        specialtyRepository.delete(specialty);
        log.info("Especialidad eliminada con ID: {}", id);
    }

    /**
     * Obtiene las especialidades más populares basadas en la cantidad de doctores.
     *
     * @param limit Número máximo de especialidades a retornar
     * @return Lista de DTOs de especialidades ordenadas por popularidad
     */
    @Transactional(readOnly = true)
    public List<SpecialtyDto> getMostPopularSpecialties(int limit) {
        log.info("Obteniendo las {} especialidades más populares", limit);
        
        Pageable limitPage = Pageable.ofSize(limit);
        List<Specialty> specialties = specialtyRepository.findMostPopularSpecialties(limitPage);
        
        return specialtyMapper.toDtoList(specialties);
    }

    /**
     * Obtiene especialidades relacionadas con una especialidad dada.
     *
     * @param specialtyId ID de la especialidad de referencia
     * @param limit Número máximo de especialidades relacionadas a retornar
     * @return Lista de DTOs de especialidades relacionadas
     * @throws ResourceNotFoundException si la especialidad de referencia no existe
     */
    @Transactional(readOnly = true)
    public List<SpecialtyDto> getRelatedSpecialties(Long specialtyId, int limit) {
        log.info("Buscando especialidades relacionadas con especialidad ID: {}", specialtyId);
        
        // Verificar que la especialidad de referencia existe
        if (!specialtyRepository.existsById(specialtyId)) {
            log.error("Especialidad de referencia no encontrada, ID: {}", specialtyId);
            throw new ResourceNotFoundException("Especialidad no encontrada con ID: " + specialtyId);
        }
        
        Pageable limitPage = Pageable.ofSize(limit);
        List<Specialty> relatedSpecialties = specialtyRepository.findRelatedSpecialties(specialtyId, limitPage);
        
        return specialtyMapper.toDtoList(relatedSpecialties);
    }

    /**
     * Obtiene especialidades que no tienen doctores asignados.
     *
     * @return Lista de DTOs de especialidades sin doctores
     */
    @Transactional(readOnly = true)
    public List<SpecialtyDto> getUnusedSpecialties() {
        log.info("Buscando especialidades sin doctores asignados");
        
        List<Specialty> unusedSpecialties = specialtyRepository.findSpecialtiesWithoutDoctors();
        
        return specialtyMapper.toDtoList(unusedSpecialties);
    }

    /**
     * Obtiene estadísticas de doctores por especialidad.
     *
     * @return Lista de arrays con [specialtyId, specialtyName, doctorCount]
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDoctorCountBySpecialty() {
        log.info("Obteniendo estadísticas de doctores por especialidad");
        
        return specialtyRepository.countDoctorsBySpecialty();
    }
}