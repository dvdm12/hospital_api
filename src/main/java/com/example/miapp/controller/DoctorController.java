package com.example.miapp.controller;

import com.example.miapp.dto.DoctorDto;
import com.example.miapp.services.DoctorService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing doctor-related operations.
 */
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    /**
     * Retrieves all doctors.
     *
     * @return List of {@link DoctorDto} containing all doctors.
     */
    @GetMapping
    public ResponseEntity<List<DoctorDto>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    /**
     * Retrieves a doctor by its ID.
     *
     * @param id The ID of the doctor.
     * @return {@link DoctorDto} containing the requested doctor details.
     * @throws EntityNotFoundException If the doctor does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorDto> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    /**
     * Creates a new doctor.
     *
     * @param doctorDto The data of the new doctor.
     * @return The created {@link DoctorDto}.
     */
    @PostMapping
    public ResponseEntity<DoctorDto> createDoctor(@Valid @RequestBody DoctorDto doctorDto) {
        return ResponseEntity.ok(doctorService.saveDoctor(doctorDto));
    }

    /**
     * Updates an existing doctor.
     *
     * @param id        The ID of the doctor to update.
     * @param doctorDto The new data for the doctor.
     * @return The updated {@link DoctorDto}.
     * @throws EntityNotFoundException If the doctor does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DoctorDto> updateDoctor(@PathVariable Long id, @Valid @RequestBody DoctorDto doctorDto) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, doctorDto));
    }

    /**
     * Deletes a doctor by its ID.
     *
     * @param id The ID of the doctor to delete.
     * @return Response with status 204 (No Content) if deletion is successful.
     * @throws EntityNotFoundException If the doctor does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
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
