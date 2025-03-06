package com.example.miapp.controller;

import com.example.miapp.dto.SpecialtyDto;
import com.example.miapp.services.SpecialtyService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing specialty-related operations.
 */
@RestController
@RequestMapping("/api/specialties")
@RequiredArgsConstructor
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    /**
     * Retrieves all specialties.
     *
     * @return List of {@link SpecialtyDto} containing all specialties.
     */
    @GetMapping
    public ResponseEntity<List<SpecialtyDto>> getAllSpecialties() {
        return ResponseEntity.ok(specialtyService.getAllSpecialties());
    }

    /**
     * Retrieves a specialty by its ID.
     *
     * @param id The ID of the specialty.
     * @return {@link SpecialtyDto} containing the requested specialty details.
     * @throws EntityNotFoundException If the specialty does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SpecialtyDto> getSpecialtyById(@PathVariable Long id) {
        return ResponseEntity.ok(specialtyService.getSpecialtyById(id));
    }

    /**
     * Creates a new specialty.
     *
     * @param specialtyDto The data of the new specialty.
     * @return The created {@link SpecialtyDto}.
     */
    @PostMapping
    public ResponseEntity<SpecialtyDto> createSpecialty(@Valid @RequestBody SpecialtyDto specialtyDto) {
        return ResponseEntity.ok(specialtyService.saveSpecialty(specialtyDto));
    }

    /**
     * Updates an existing specialty.
     *
     * @param id           The ID of the specialty to update.
     * @param specialtyDto The new data for the specialty.
     * @return The updated {@link SpecialtyDto}.
     * @throws EntityNotFoundException If the specialty does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SpecialtyDto> updateSpecialty(@PathVariable Long id, @Valid @RequestBody SpecialtyDto specialtyDto) {
        return ResponseEntity.ok(specialtyService.updateSpecialty(id, specialtyDto));
    }

    /**
     * Deletes a specialty by its ID.
     *
     * @param id The ID of the specialty to delete.
     * @return Response with status 204 (No Content) if deletion is successful.
     * @throws EntityNotFoundException If the specialty does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpecialty(@PathVariable Long id) {
        specialtyService.deleteSpecialty(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Handles exceptions related to entity not found errors.
     *
     * @param ex The exception thrown.
     * @return ResponseEntity with status 404 (Not Found) and error message.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}
