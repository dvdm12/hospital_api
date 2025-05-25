package com.example.miapp.service.medicalrecord;

import com.example.miapp.dto.medicalrecord.CreateMedicalEntryRequest;
import com.example.miapp.dto.medicalrecord.MedicalRecordDto;
import com.example.miapp.dto.medicalrecord.MedicalRecordEntryDto;
import com.example.miapp.entity.MedicalRecordEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Interface Segregation Principle - Clean contract for medical record operations
 * Dependency Inversion Principle - High-level modules depend on abstractions
 */
public interface MedicalRecordService {

    // Medical Record CRUD Operations
    MedicalRecordDto createMedicalRecord(Long patientId);
    void deleteMedicalRecord(Long recordId);

    // Medical Record Entry Operations
    MedicalRecordEntryDto addMedicalEntry(Long patientId, CreateMedicalEntryRequest request);
    MedicalRecordEntryDto updateMedicalEntry(Long entryId, CreateMedicalEntryRequest request);
    void deleteMedicalEntry(Long entryId, Long requestingDoctorId);

    // Query Operations
    MedicalRecordDto getMedicalRecord(Long recordId);
    Optional<MedicalRecordDto> findMedicalRecordByPatientId(Long patientId);
    MedicalRecordDto getPatientMedicalHistory(Long patientId);
    MedicalRecordDto getPatientVisibleMedicalHistory(Long patientId);
    List<MedicalRecordEntryDto> getRecentEntriesForPatient(Long patientId, int limit);
    List<MedicalRecordEntryDto> getEntriesByTypeForPatient(Long patientId, MedicalRecordEntry.EntryType entryType);

    // Search Operations
    Page<MedicalRecordDto> searchMedicalRecords(String allergyPattern, String conditionPattern, 
                                               String medicationPattern, MedicalRecordEntry.EntryType entryType, 
                                               String contentPattern, Pageable pageable);
    Page<MedicalRecordDto> findByAllergiesContaining(String allergyPattern, Pageable pageable);
    Page<MedicalRecordDto> findByChronicConditionsContaining(String conditionPattern, Pageable pageable);
    Page<MedicalRecordDto> findByCurrentMedicationsContaining(String medicationPattern, Pageable pageable);

    // Medical Information Queries
    boolean hasAllergy(Long patientId, String allergyName);
    boolean hasChronicCondition(Long patientId, String conditionName);

    // Management Operations
    void updateAllergies(Long recordId, String allergies);
    void updateChronicConditions(Long recordId, String chronicConditions);
    void updateCurrentMedications(Long recordId, String currentMedications);
    void updateMedicalRecordFields(Long recordId, String allergies, String chronicConditions, 
                                 String currentMedications, String surgicalHistory, 
                                 String familyHistory, String notes);
    void updateEntryVisibility(Long entryId, boolean visibleToPatient);
    void mergeMedicalRecords(Long primaryRecordId, Long secondaryRecordId, String reason);

    // Statistics and Reporting
    List<Object[]> getMedicalRecordEntryStats();
}