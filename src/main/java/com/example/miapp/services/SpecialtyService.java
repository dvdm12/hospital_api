package com.example.miapp.services;

import com.example.miapp.dto.SpecialtyDto;
import com.example.miapp.entity.Specialty;
import com.example.miapp.repository.SpecialtyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing specialty-related operations.
 */
@Service
@RequiredArgsConstructor
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    /**
     * Retrieves all specialties from the database.
     *
     * @return List of {@link SpecialtyDto} containing specialty details.
     */
    @Transactional(readOnly = true)
    public List<SpecialtyDto> getAllSpecialties() {
        return specialtyRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specialty by its ID.
     *
     * @param id The ID of the specialty.
     * @return {@link SpecialtyDto} containing specialty details.
     * @throws EntityNotFoundException If no specialty is found with the given ID.
     */
    @Transactional(readOnly = true)
    public SpecialtyDto getSpecialtyById(Long id) {
        Specialty specialty = findSpecialtyById(id);
        return convertToDto(specialty);
    }

    /**
     * Saves a new specialty in the database.
     *
     * @param specialtyDto The {@link SpecialtyDto} containing the new specialty details.
     * @return The saved {@link SpecialtyDto}.
     */
    @Transactional
    public SpecialtyDto saveSpecialty(SpecialtyDto specialtyDto) {
        Specialty specialty = convertToEntity(specialtyDto);
        return convertToDto(specialtyRepository.save(specialty));
    }

    /**
     * Updates an existing specialty.
     *
     * @param id           The ID of the specialty to be updated.
     * @param specialtyDto The updated {@link SpecialtyDto} data.
     * @return The updated {@link SpecialtyDto}.
     * @throws EntityNotFoundException If the specialty does not exist.
     */
    @Transactional
    public SpecialtyDto updateSpecialty(Long id, SpecialtyDto specialtyDto) {
        Specialty existingSpecialty = findSpecialtyById(id);

        existingSpecialty = existingSpecialty.toBuilder()
                .name(specialtyDto.getName())
                .description(specialtyDto.getDescription())
                .build();

        return convertToDto(specialtyRepository.save(existingSpecialty));
    }

    /**
     * Deletes a specialty by its ID.
     *
     * @param id The ID of the specialty to be deleted.
     * @throws EntityNotFoundException If the specialty does not exist.
     */
    @Transactional
    public void deleteSpecialty(Long id) {
        if (!specialtyRepository.existsById(id)) {
            throw new EntityNotFoundException("Specialty not found with ID: " + id);
        }
        specialtyRepository.deleteById(id);
    }

    /**
     * Finds a specialty by ID and throws an exception if not found.
     *
     * @param id The ID of the specialty.
     * @return The {@link Specialty} entity.
     * @throws EntityNotFoundException If the specialty does not exist.
     */
    private Specialty findSpecialtyById(Long id) {
        return specialtyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Specialty not found with ID: " + id));
    }

    /**
     * Converts a {@link Specialty} entity to a {@link SpecialtyDto}.
     *
     * @param specialty The entity to convert.
     * @return The corresponding {@link SpecialtyDto}.
     */
    private SpecialtyDto convertToDto(Specialty specialty) {
        return SpecialtyDto.builder()
                .id(specialty.getId())
                .name(specialty.getName())
                .description(specialty.getDescription())
                .build();
    }

    /**
     * Converts a {@link SpecialtyDto} to a {@link Specialty} entity.
     *
     * @param dto The DTO to convert.
     * @return The corresponding {@link Specialty} entity.
     */
    private Specialty convertToEntity(SpecialtyDto dto) {
        return Specialty.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }
}
