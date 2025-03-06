package com.example.miapp.services;

import com.example.miapp.dto.DoctorDto;
import com.example.miapp.models.Doctor;
import com.example.miapp.repository.DoctorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing doctor-related operations.
 */
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    /**
     * Retrieves all doctors from the database.
     *
     * @return List of {@link DoctorDto} containing doctor details.
     */
    @Transactional(readOnly = true)
    public List<DoctorDto> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a doctor by its ID.
     *
     * @param id The ID of the doctor.
     * @return {@link DoctorDto} containing doctor details.
     * @throws EntityNotFoundException If no doctor is found with the given ID.
     */
    @Transactional(readOnly = true)
    public DoctorDto getDoctorById(Long id) {
        Doctor doctor = findDoctorById(id);
        return convertToDto(doctor);
    }

    /**
     * Saves a new doctor in the database.
     *
     * @param doctorDto The {@link DoctorDto} containing the new doctor details.
     * @return The saved {@link DoctorDto}.
     */
    @Transactional
    public DoctorDto saveDoctor(DoctorDto doctorDto) {
        Doctor doctor = convertToEntity(doctorDto);
        return convertToDto(doctorRepository.save(doctor));
    }

    /**
     * Updates an existing doctor's information.
     *
     * @param id        The ID of the doctor to be updated.
     * @param doctorDto The updated {@link DoctorDto} data.
     * @return The updated {@link DoctorDto}.
     * @throws EntityNotFoundException If the doctor does not exist.
     */
    @Transactional
    public DoctorDto updateDoctor(Long id, DoctorDto doctorDto) {
        Doctor existingDoctor = findDoctorById(id);

        existingDoctor = existingDoctor.toBuilder()
                .firstName(doctorDto.getFirstName())
                .lastName(doctorDto.getLastName())
                .phone(doctorDto.getPhone())
                .email(doctorDto.getEmail())
                .build();

        return convertToDto(doctorRepository.save(existingDoctor));
    }

    /**
     * Deletes a doctor by its ID.
     *
     * @param id The ID of the doctor to be deleted.
     * @throws EntityNotFoundException If the doctor does not exist.
     */
    @Transactional
    public void deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new EntityNotFoundException("Doctor not found with ID: " + id);
        }
        doctorRepository.deleteById(id);
    }

    /**
     * Finds a doctor by ID and throws an exception if not found.
     *
     * @param id The ID of the doctor.
     * @return The {@link Doctor} entity.
     * @throws EntityNotFoundException If the doctor does not exist.
     */
    private Doctor findDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with ID: " + id));
    }

    /**
     * Converts a {@link Doctor} entity to a {@link DoctorDto}.
     *
     * @param doctor The entity to convert.
     * @return The corresponding {@link DoctorDto}.
     */
    private DoctorDto convertToDto(Doctor doctor) {
        return DoctorDto.builder()
                .id(doctor.getId())
                .firstName(doctor.getFirstName())
                .lastName(doctor.getLastName())
                .phone(doctor.getPhone())
                .email(doctor.getEmail())
                .build();
    }

    /**
     * Converts a {@link DoctorDto} to a {@link Doctor} entity.
     *
     * @param dto The DTO to convert.
     * @return The corresponding {@link Doctor} entity.
     */
    private Doctor convertToEntity(DoctorDto dto) {
        return Doctor.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .build();
    }
}
