package com.example.miapp.services;

import com.example.miapp.dto.PatientDto;
import com.example.miapp.entity.Patient;
import com.example.miapp.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing patient-related operations.
 */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    /**
     * Retrieves all patients from the database.
     *
     * @return List of {@link PatientDto} containing patient details.
     */
    @Transactional(readOnly = true)
    public List<PatientDto> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a patient by its ID.
     *
     * @param id The ID of the patient.
     * @return {@link PatientDto} containing patient details.
     * @throws EntityNotFoundException If no patient is found with the given ID.
     */
    @Transactional(readOnly = true)
    public PatientDto getPatientById(Long id) {
        Patient patient = findPatientById(id);
        return convertToDto(patient);
    }

    /**
     * Saves a new patient in the database.
     *
     * @param patientDto The {@link PatientDto} containing the new patient details.
     * @return The saved {@link PatientDto}.
     */
    @Transactional
    public PatientDto savePatient(PatientDto patientDto) {
        Patient patient = convertToEntity(patientDto);
        return convertToDto(patientRepository.save(patient));
    }

    /**
     * Updates an existing patient's information.
     *
     * @param id         The ID of the patient to be updated.
     * @param patientDto The updated {@link PatientDto} data.
     * @return The updated {@link PatientDto}.
     * @throws EntityNotFoundException If the patient does not exist.
     */
    @Transactional
    public PatientDto updatePatient(Long id, PatientDto patientDto) {
        Patient existingPatient = findPatientById(id);

        existingPatient = existingPatient.toBuilder()
                .firstName(patientDto.getFirstName())
                .lastName(patientDto.getLastName())
                .birthDate(patientDto.getBirthDate())
                .phone(patientDto.getPhone())
                .address(patientDto.getAddress())
                .build();

        return convertToDto(patientRepository.save(existingPatient));
    }

    /**
     * Deletes a patient by its ID.
     *
     * @param id The ID of the patient to be deleted.
     * @throws EntityNotFoundException If the patient does not exist.
     */
    @Transactional
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new EntityNotFoundException("Patient not found with ID: " + id);
        }
        patientRepository.deleteById(id);
    }

    /**
     * Finds a patient by ID and throws an exception if not found.
     *
     * @param id The ID of the patient.
     * @return The {@link Patient} entity.
     * @throws EntityNotFoundException If the patient does not exist.
     */
    private Patient findPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with ID: " + id));
    }

    /**
     * Converts a {@link Patient} entity to a {@link PatientDto}.
     *
     * @param patient The entity to convert.
     * @return The corresponding {@link PatientDto}.
     */
    private PatientDto convertToDto(Patient patient) {
        return PatientDto.builder()
                .id(patient.getId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .birthDate(patient.getBirthDate())
                .phone(patient.getPhone())
                .address(patient.getAddress())
                .build();
    }

    /**
     * Converts a {@link PatientDto} to a {@link Patient} entity.
     *
     * @param dto The DTO to convert.
     * @return The corresponding {@link Patient} entity.
     */
    private Patient convertToEntity(PatientDto dto) {
        return Patient.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .birthDate(dto.getBirthDate())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .build();
    }
}
