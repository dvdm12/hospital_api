package com.example.miapp.service.doctor;

import com.example.miapp.dto.doctor.DoctorDto;
import com.example.miapp.dto.doctor.DoctorSearchCriteria;
import com.example.miapp.entity.Doctor;
import com.example.miapp.mapper.DoctorMapper;
import com.example.miapp.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for doctor queries (Single Responsibility)
 * Implements Repository Pattern for data access abstraction
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DoctorQueryService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;

    /**
     * Finds doctor by ID
     */
    public DoctorDto getDoctor(Long doctorId) {
        Doctor doctor = findDoctorById(doctorId);
        return doctorMapper.toDto(doctor);
    }

    /**
     * Finds doctor entity by ID (internal use)
     */
    public Doctor findDoctorById(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));
    }

    /**
     * Finds doctor by email
     */
    public Optional<DoctorDto> findDoctorByEmail(String email) {
        return doctorRepository.findByEmail(email)
                .map(doctorMapper::toDto);
    }

    /**
     * Finds doctor by license number
     */
    public Optional<DoctorDto> findDoctorByLicenseNumber(String licenseNumber) {
        return doctorRepository.findByLicenseNumber(licenseNumber)
                .map(doctorMapper::toDto);
    }

    /**
     * Finds doctor by user ID
     */
    public Optional<DoctorDto> findDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId)
                .map(doctorMapper::toDto);
    }

    /**
     * Searches doctors by name pattern
     */
    public Page<DoctorDto> searchDoctorsByName(String namePattern, Pageable pageable) {
        log.info("Searching doctors by name pattern: {}", namePattern);
        
        Page<Doctor> doctors = doctorRepository.findByNameContaining(namePattern, pageable);
        return doctors.map(doctorMapper::toDto);
    }

    /**
     * Finds doctors by specialty
     */
    public Page<DoctorDto> findDoctorsBySpecialty(Long specialtyId, Pageable pageable) {
        log.info("Finding doctors by specialty ID: {}", specialtyId);
        
        Page<Doctor> doctors = doctorRepository.findBySpecialtyId(specialtyId, pageable);
        return doctors.map(doctorMapper::toDto);
    }

    /**
     * Finds doctors by multiple specialties
     */
    public Page<DoctorDto> findDoctorsBySpecialties(List<Long> specialtyIds, Pageable pageable) {
        log.info("Finding doctors by multiple specialties: {}", specialtyIds);
        
        Page<Doctor> doctors = doctorRepository.findBySpecialtyIdIn(specialtyIds, pageable);
        return doctors.map(doctorMapper::toDto);
    }

    /**
     * Finds available doctors for specific day and time
     */
    public Page<DoctorDto> findAvailableDoctors(DayOfWeek dayOfWeek, LocalTime startTime, 
                                              LocalTime endTime, Pageable pageable) {
        log.info("Finding available doctors for {} from {} to {}", dayOfWeek, startTime, endTime);
        
        Page<Doctor> doctors = doctorRepository.findAvailableDoctors(dayOfWeek, startTime, endTime, pageable);
        return doctors.map(doctorMapper::toDto);
    }

    /**
     * Finds doctors with appointments in date range
     */
    public Page<DoctorDto> findDoctorsWithAppointmentsInRange(LocalDateTime startDate, 
                                                            LocalDateTime endDate, Pageable pageable) {
        log.info("Finding doctors with appointments between {} and {}", startDate, endDate);
        
        Page<Doctor> doctors = doctorRepository.findWithAppointmentsInRange(startDate, endDate, pageable);
        return doctors.map(doctorMapper::toDto);
    }

    /**
     * Gets appointment statistics by doctor
     */
    public List<Object[]> getAppointmentStatsByDoctor(LocalDateTime startDate, LocalDateTime endDate) {
        return doctorRepository.countAppointmentsByDoctor(startDate, endDate);
    }

    /**
     * Finds doctors by consultation fee range
     */
    public Page<DoctorDto> findDoctorsByConsultationFeeRange(Double minFee, Double maxFee, Pageable pageable) {
        log.info("Finding doctors with consultation fee between {} and {}", minFee, maxFee);
        
        Page<Doctor> doctors = doctorRepository.findByConsultationFeeBetween(minFee, maxFee, pageable);
        return doctors.map(doctorMapper::toDto);
    }

    /**
     * Finds doctors with highest consultation fees
     */
    public Page<DoctorDto> findDoctorsByHighestFees(Pageable pageable) {
        Page<Doctor> doctors = doctorRepository.findByHighestConsultationFee(pageable);
        return doctors.map(doctorMapper::toDto);
    }

    /**
     * Finds doctors with lowest consultation fees
     */
    public Page<DoctorDto> findDoctorsByLowestFees(Pageable pageable) {
        Page<Doctor> doctors = doctorRepository.findByLowestConsultationFee(pageable);
        return doctors.map(doctorMapper::toDto);
    }

    /**
     * Finds doctors by experience level
     */
    public Page<DoctorDto> findDoctorsByExperienceLevel(String experienceLevel, Pageable pageable) {
        log.info("Finding doctors by experience level: {}", experienceLevel);
        
        Page<Doctor> doctors = doctorRepository.findByExperienceLevel(experienceLevel, pageable);
        return doctors.map(doctorMapper::toDto);
    }

    /**
     * Advanced search with multiple criteria (Strategy Pattern)
     */
    public Page<DoctorDto> searchDoctors(DoctorSearchCriteria criteria, Pageable pageable) {
        log.info("Advanced search for doctors with criteria: {}", criteria);
        
        Page<Doctor> doctors = doctorRepository.searchDoctors(
                criteria.getName(),
                criteria.getSpecialtyId(),
                criteria.getDayOfWeek(),
                criteria.getMinFee(),
                criteria.getMaxFee(),
                pageable
        );
        
        return doctors.map(doctorMapper::toDto);
    }

    /**
     * Finds doctors with minimum number of specialties
     */
    public Page<DoctorDto> findDoctorsWithMinimumSpecialties(long specialtyCount, Pageable pageable) {
        Page<Doctor> doctors = doctorRepository.findDoctorsWithMinimumSpecialties(specialtyCount, pageable);
        return doctors.map(doctorMapper::toDto);
    }

    /**
     * Finds doctors without specific specialty
     */
    public Page<DoctorDto> findDoctorsWithoutSpecialty(Long specialtyId, Pageable pageable) {
        Page<Doctor> doctors = doctorRepository.findDoctorsWithoutSpecialty(specialtyId, pageable);
        return doctors.map(doctorMapper::toDto);
    }

    /**
     * Gets all doctors with basic info (for dropdowns/lists)
     */
    public Page<DoctorRepository.DoctorBasicInfo> getAllDoctorsBasicInfo(Pageable pageable) {
        return doctorRepository.findAllBasicInfo(pageable);
    }

    /**
     * Gets all doctors as DTOs
     */
    public Page<DoctorDto> getAllDoctors(Pageable pageable) {
        Page<Doctor> doctors = doctorRepository.findAll(pageable);
        return doctors.map(doctorMapper::toDto);
    }
}