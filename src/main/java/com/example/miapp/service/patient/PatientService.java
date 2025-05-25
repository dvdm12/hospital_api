package com.example.miapp.service.patient;

import com.example.miapp.dto.patient.CreatePatientRequest;
import com.example.miapp.dto.patient.PatientDto;
import com.example.miapp.dto.patient.PatientSearchCriteria;
import com.example.miapp.entity.Patient;
import com.example.miapp.entity.Patient.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Interface Segregation Principle - Clean contract for patient operations
 * Dependency Inversion Principle - High-level modules depend on abstractions
 */
public interface PatientService {

    // CRUD Operations
    PatientDto createPatient(CreatePatientRequest request);
    PatientDto updatePatient(Long patientId, CreatePatientRequest request);
    void deletePatient(Long patientId);
    void archivePatient(Long patientId, String reason);

    // Query Operations
    PatientDto getPatient(Long patientId);
    Patient findPatientById(Long patientId); // For internal service use
    Optional<PatientDto> findPatientByEmail(String email);
    Optional<PatientDto> findPatientByPhone(String phone);
    Page<PatientDto> searchPatientsByName(String namePattern, Pageable pageable);
    Page<PatientDto> findPatientsByGender(Gender gender, Pageable pageable);
    Page<PatientDto> findPatientsByAgeRange(int minAge, int maxAge, Pageable pageable);
    Page<PatientDto> findPatientsByInsuranceProvider(String insuranceProvider, Pageable pageable);
    Page<PatientDto> findPatientsByDoctor(Long doctorId, Pageable pageable);
    Page<PatientDto> searchPatients(PatientSearchCriteria criteria, Pageable pageable);
    Page<PatientDto> getAllPatients(Pageable pageable);

    // Specialized Queries
    Page<PatientDto> findPatientsWithoutRecentAppointments(String interval, Pageable pageable);
    Page<PatientDto> findNewPatients(String interval, Pageable pageable);

    // Management Operations
    void updateAddress(Long patientId, String address);
    void updatePhone(Long patientId, String phone);
    void updateInsuranceInfo(Long patientId, String insuranceProvider, String insurancePolicyNumber);
    void updateEmergencyContact(Long patientId, String emergencyContactName, String emergencyContactPhone);
    void updatePatientProfile(Long patientId, String firstName, String lastName, String phone, String address);
    void mergePatientRecords(Long keepPatientId, Long mergePatientId, String reason);

    // Statistics and Reporting
    List<Object[]> getPatientStatsByGender();
    List<Object[]> getPatientStatsByAgeGroup(int interval);
}