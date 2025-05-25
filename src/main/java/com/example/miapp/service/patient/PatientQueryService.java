package com.example.miapp.service.patient;

import com.example.miapp.dto.patient.PatientDto;
import com.example.miapp.dto.patient.PatientSearchCriteria;
import com.example.miapp.entity.Patient;
import com.example.miapp.entity.Patient.Gender;
import com.example.miapp.mapper.PatientMapper;
import com.example.miapp.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service responsible for patient queries (Single Responsibility)
 * Implements Repository Pattern for data access abstraction
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PatientQueryService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    /**
     * Finds patient by ID
     */
    public PatientDto getPatient(Long patientId) {
        Patient patient = findPatientById(patientId);
        return patientMapper.toDto(patient);
    }

    /**
     * Finds patient entity by ID (internal use)
     */
    public Patient findPatientById(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));
    }

    /**
     * Finds patient by email
     */
    public Optional<PatientDto> findPatientByEmail(String email) {
        return patientRepository.findByEmail(email)
                .map(patientMapper::toDto);
    }

    /**
     * Finds patient by phone number
     */
    public Optional<PatientDto> findPatientByPhone(String phone) {
        return patientRepository.findByPhone(phone)
                .map(patientMapper::toDto);
    }

    /**
     * Finds patient by user ID
     */
    public Optional<PatientDto> findPatientByUserId(Long userId) {
        return patientRepository.findByUserId(userId)
                .map(patientMapper::toDto);
    }

    /**
     * Searches patients by name pattern
     */
    public Page<PatientDto> searchPatientsByName(String namePattern, Pageable pageable) {
        log.info("Searching patients by name pattern: {}", namePattern);
        
        Page<Patient> patients = patientRepository.findByNameContaining(namePattern, pageable);
        return patients.map(patientMapper::toDto);
    }

    /**
     * Finds patients by full name
     */
    public Page<PatientDto> findPatientsByFullName(String firstName, String lastName, Pageable pageable) {
        log.info("Finding patients by full name: {} {}", firstName, lastName);
        
        Page<Patient> patients = patientRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(
                firstName, lastName, pageable);
        return patients.map(patientMapper::toDto);
    }

    /**
     * Finds patients by gender
     */
    public Page<PatientDto> findPatientsByGender(Gender gender, Pageable pageable) {
        log.info("Finding patients by gender: {}", gender);
        
        Page<Patient> patients = patientRepository.findByGender(gender, pageable);
        return patients.map(patientMapper::toDto);
    }

    /**
     * Finds patients by age range
     */
    public Page<PatientDto> findPatientsByAgeRange(int minAge, int maxAge, Pageable pageable) {
        log.info("Finding patients by age range: {} - {}", minAge, maxAge);
        
        // Calculate date range for age
        java.util.Date startDate = calculateDateForAge(maxAge);
        java.util.Date endDate = calculateDateForAge(minAge);
        
        Page<Patient> patients = patientRepository.findByAgeRange(startDate, endDate, pageable);
        return patients.map(patientMapper::toDto);
    }

    /**
     * Finds patients by insurance provider
     */
    public Page<PatientDto> findPatientsByInsuranceProvider(String insuranceProvider, Pageable pageable) {
        log.info("Finding patients by insurance provider: {}", insuranceProvider);
        
        Page<Patient> patients = patientRepository.findByInsuranceProviderContainingIgnoreCase(
                insuranceProvider, pageable);
        return patients.map(patientMapper::toDto);
    }

    /**
     * Finds patients who have appointments with a specific doctor
     */
    public Page<PatientDto> findPatientsByDoctor(Long doctorId, Pageable pageable) {
        log.info("Finding patients by doctor ID: {}", doctorId);
        
        Page<Patient> patients = patientRepository.findByDoctorId(doctorId, pageable);
        return patients.map(patientMapper::toDto);
    }

    /**
     * Finds patients by medication name
     */
    public Page<PatientDto> findPatientsByMedication(String medicationName, Pageable pageable) {
        log.info("Finding patients by medication: {}", medicationName);
        
        Page<Patient> patients = patientRepository.findByMedicationName(medicationName, pageable);
        return patients.map(patientMapper::toDto);
    }

    /**
     * Finds patients by chronic condition
     */
    public Page<PatientDto> findPatientsByChronicCondition(String condition, Pageable pageable) {
        log.info("Finding patients by chronic condition: {}", condition);
        
        Page<Patient> patients = patientRepository.findByChronicCondition(condition, pageable);
        return patients.map(patientMapper::toDto);
    }

    /**
     * Finds patients by allergy
     */
    public Page<PatientDto> findPatientsByAllergy(String allergy, Pageable pageable) {
        log.info("Finding patients by allergy: {}", allergy);
        
        Page<Patient> patients = patientRepository.findByAllergy(allergy, pageable);
        return patients.map(patientMapper::toDto);
    }

    /**
     * Advanced search with multiple criteria (Strategy Pattern)
     */
    public Page<PatientDto> searchPatients(PatientSearchCriteria criteria, Pageable pageable) {
        log.info("Advanced search for patients with criteria: {}", criteria);
        
        Page<Patient> patients = patientRepository.searchPatients(
                criteria.getName(),
                criteria.getGender(),
                criteria.getMinAge(),
                criteria.getMaxAge(),
                criteria.getInsuranceProvider(),
                criteria.getCondition(),
                pageable
        );
        
        return patients.map(patientMapper::toDto);
    }

    /**
     * Finds patients without recent appointments
     */
    public Page<PatientDto> findPatientsWithoutRecentAppointments(String interval, Pageable pageable) {
        log.info("Finding patients without recent appointments (interval: {})", interval);
        
        Page<Patient> patients = patientRepository.findPatientsWithoutRecentAppointments(interval, pageable);
        return patients.map(patientMapper::toDto);
    }

    /**
     * Finds new patients registered within a time period
     */
    public Page<PatientDto> findNewPatients(String interval, Pageable pageable) {
        log.info("Finding new patients registered in interval: {}", interval);
        
        Page<Patient> patients = patientRepository.findNewPatients(interval, pageable);
        return patients.map(patientMapper::toDto);
    }

    /**
     * Gets patient statistics by gender
     */
    public List<Object[]> getPatientStatsByGender() {
        return patientRepository.countPatientsByGender();
    }

    /**
     * Gets patient statistics by age group
     */
    public List<Object[]> getPatientStatsByAgeGroup(int interval) {
        return patientRepository.countPatientsByAgeGroup(interval);
    }

    /**
     * Gets all patients with basic info (for dropdowns/lists)
     */
    public Page<PatientRepository.PatientBasicInfo> getAllPatientsBasicInfo(Pageable pageable) {
        return patientRepository.findAllBasicInfo(pageable);
    }

    /**
     * Gets all patients as DTOs
     */
    public Page<PatientDto> getAllPatients(Pageable pageable) {
        Page<Patient> patients = patientRepository.findAll(pageable);
        return patients.map(patientMapper::toDto);
    }

    // Private helper methods

    private java.util.Date calculateDateForAge(int age) {
        java.time.LocalDate date = java.time.LocalDate.now().minusYears(age);
        return java.util.Date.from(date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
    }
}