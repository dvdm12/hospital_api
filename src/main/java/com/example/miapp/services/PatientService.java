package com.example.miapp.services;

import com.example.miapp.dto.PatientDto;
import com.example.miapp.models.Patient;
import com.example.miapp.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing Patient operations.
 */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    /**
     * Retrieves all patients and converts them to DTOs.
     */
    public List<PatientDto> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Finds a patient by ID and converts it to DTO.
     */
    public PatientDto getPatientById(Long id) {
        return patientRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with ID: " + id));
    }

    /**
     * Saves a new patient using a DTO.
     */
    @Transactional
    public PatientDto savePatient(PatientDto patientDto) {
        Patient patient = convertToEntity(patientDto);
        return convertToDto(patientRepository.save(patient));
    }

    /**
     * Updates an existing patient.
     */
    @Transactional
    public PatientDto updatePatient(Long id, PatientDto patientDto) {
        if (!patientRepository.existsById(id)) {
            throw new EntityNotFoundException("Patient not found with ID: " + id);
        }

        Patient patient = convertToEntity(patientDto).toBuilder()
                .id(id) // Ensure the ID remains the same
                .build();

        return convertToDto(patientRepository.save(patient));
    }

    /**
     * Deletes a patient by ID.
     */
    @Transactional
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new EntityNotFoundException("Patient not found with ID: " + id);
        }
        patientRepository.deleteById(id);
    }

    /**
     * Converts a Patient entity to PatientDto.
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
     * Converts a PatientDto to a Patient entity.
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
