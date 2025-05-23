package com.example.miapp.services;

import com.example.miapp.dto.DoctorSpecialtyDto;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.DoctorSpecialty;
import com.example.miapp.entity.Specialty;
import com.example.miapp.repository.DoctorRepository;
import com.example.miapp.repository.DoctorSpecialtyRepository;
import com.example.miapp.repository.SpecialtyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing doctor-specialty assignments.
 */
@Service
@RequiredArgsConstructor
public class DoctorSpecialtyService {

    private final DoctorSpecialtyRepository doctorSpecialtyRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;

    /**
     * Retrieves all doctor-specialty assignments from the database.
     *
     * @return List of {@link DoctorSpecialtyDto} containing all assignments.
     */
    @Transactional(readOnly = true)
    public List<DoctorSpecialtyDto> getAllDoctorSpecialties() {
        return doctorSpecialtyRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a doctor-specialty assignment by its ID.
     *
     * @param id The ID of the assignment.
     * @return {@link DoctorSpecialtyDto} containing the requested assignment details.
     * @throws EntityNotFoundException If the assignment does not exist.
     */
    @Transactional(readOnly = true)
    public DoctorSpecialtyDto getDoctorSpecialtyById(Long id) {
        DoctorSpecialty doctorSpecialty = findDoctorSpecialtyById(id);
        return convertToDto(doctorSpecialty);
    }

    /**
     * Saves a new doctor-specialty assignment in the database.
     *
     * @param doctorSpecialtyDto The {@link DoctorSpecialtyDto} containing the new assignment details.
     * @return The saved {@link DoctorSpecialtyDto}.
     * @throws EntityNotFoundException If the associated doctor or specialty does not exist.
     */
    @Transactional
    public DoctorSpecialtyDto saveDoctorSpecialty(DoctorSpecialtyDto doctorSpecialtyDto) {
        Doctor doctor = findDoctorById(doctorSpecialtyDto.getDoctorId());
        Specialty specialty = findSpecialtyById(doctorSpecialtyDto.getSpecialtyId());

        DoctorSpecialty doctorSpecialty = convertToEntity(doctorSpecialtyDto);
        doctorSpecialty.setDoctor(doctor);
        doctorSpecialty.setSpecialty(specialty);

        return convertToDto(doctorSpecialtyRepository.save(doctorSpecialty));
    }

    /**
     * Updates an existing doctor-specialty assignment.
     *
     * @param id                  The ID of the assignment to be updated.
     * @param doctorSpecialtyDto   The updated {@link DoctorSpecialtyDto} data.
     * @return The updated {@link DoctorSpecialtyDto}.
     * @throws EntityNotFoundException If the assignment, doctor, or specialty does not exist.
     */
    @Transactional
    public DoctorSpecialtyDto updateDoctorSpecialty(Long id, DoctorSpecialtyDto doctorSpecialtyDto) {
        DoctorSpecialty existingDoctorSpecialty = findDoctorSpecialtyById(id);

        Doctor doctor = findDoctorById(doctorSpecialtyDto.getDoctorId());
        Specialty specialty = findSpecialtyById(doctorSpecialtyDto.getSpecialtyId());

        existingDoctorSpecialty = existingDoctorSpecialty.toBuilder()
                .doctor(doctor)
                .specialty(specialty)
                .certificationDate(doctorSpecialtyDto.getCertificationDate())
                .experienceLevel(doctorSpecialtyDto.getExperienceLevel())
                .build();

        return convertToDto(doctorSpecialtyRepository.save(existingDoctorSpecialty));
    }

    /**
     * Deletes a doctor-specialty assignment by its ID.
     *
     * @param id The ID of the assignment to be deleted.
     * @throws EntityNotFoundException If the assignment does not exist.
     */
    @Transactional
    public void deleteDoctorSpecialty(Long id) {
        if (!doctorSpecialtyRepository.existsById(id)) {
            throw new EntityNotFoundException("DoctorSpecialty not found with ID: " + id);
        }
        doctorSpecialtyRepository.deleteById(id);
    }

    /**
     * Finds a doctor-specialty assignment by ID and throws an exception if not found.
     *
     * @param id The ID of the assignment.
     * @return The {@link DoctorSpecialty} entity.
     * @throws EntityNotFoundException If the assignment does not exist.
     */
    private DoctorSpecialty findDoctorSpecialtyById(Long id) {
        return doctorSpecialtyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DoctorSpecialty not found with ID: " + id));
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
     * Converts a {@link DoctorSpecialty} entity to a {@link DoctorSpecialtyDto}.
     *
     * @param doctorSpecialty The entity to convert.
     * @return The corresponding {@link DoctorSpecialtyDto}.
     */
    private DoctorSpecialtyDto convertToDto(DoctorSpecialty doctorSpecialty) {
        return DoctorSpecialtyDto.builder()
                .id(doctorSpecialty.getId())
                .doctorId(doctorSpecialty.getDoctor().getId())
                .specialtyId(doctorSpecialty.getSpecialty().getId())
                .certificationDate(doctorSpecialty.getCertificationDate())
                .experienceLevel(doctorSpecialty.getExperienceLevel())
                .build();
    }

    /**
     * Converts a {@link DoctorSpecialtyDto} to a {@link DoctorSpecialty} entity.
     *
     * @param dto The DTO to convert.
     * @return The corresponding {@link DoctorSpecialty} entity.
     */
    private DoctorSpecialty convertToEntity(DoctorSpecialtyDto dto) {
        return DoctorSpecialty.builder()
                .id(dto.getId())
                .certificationDate(dto.getCertificationDate())
                .experienceLevel(dto.getExperienceLevel())
                .build();
    }
}
