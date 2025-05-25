package com.example.miapp.service.medicalrecord;

import com.example.miapp.dto.medicalrecord.MedicalRecordDto;
import com.example.miapp.dto.medicalrecord.MedicalRecordEntryDto;
import com.example.miapp.entity.MedicalRecord;
import com.example.miapp.entity.MedicalRecordEntry;
import com.example.miapp.mapper.MedicalRecordMapper;
import com.example.miapp.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service responsible for medical record queries (Single Responsibility)
 * Implements Repository Pattern for data access abstraction
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordQueryService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalRecordMapper medicalRecordMapper;

    /**
     * Finds medical record by ID
     */
    public MedicalRecordDto getMedicalRecord(Long recordId) {
        MedicalRecord record = findMedicalRecordById(recordId);
        return medicalRecordMapper.toDto(record);
    }

    /**
     * Finds medical record entity by ID (internal use)
     */
    public MedicalRecord findMedicalRecordById(Long recordId) {
        return medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found with ID: " + recordId));
    }

    /**
     * Finds medical record by patient ID
     */
    public Optional<MedicalRecordDto> findMedicalRecordByPatientId(Long patientId) {
        log.info("Finding medical record for patient: {}", patientId);
        
        return medicalRecordRepository.findByPatientId(patientId)
                .map(medicalRecordMapper::toDto);
    }

    /**
     * Gets patient's complete medical history
     */
    public MedicalRecordDto getPatientMedicalHistory(Long patientId) {
        log.info("Getting complete medical history for patient: {}", patientId);
        
        MedicalRecord record = medicalRecordRepository.findByPatientId(patientId)
                .orElseThrow(() -> new RuntimeException("Medical record not found for patient: " + patientId));
        
        return medicalRecordMapper.toDto(record);
    }

    /**
     * Gets patient's medical history visible to patient (filtered)
     */
    public MedicalRecordDto getPatientVisibleMedicalHistory(Long patientId) {
        log.info("Getting patient-visible medical history for patient: {}", patientId);
        
        MedicalRecord record = medicalRecordRepository.findByPatientId(patientId)
                .orElseThrow(() -> new RuntimeException("Medical record not found for patient: " + patientId));
        
        // Filter entries visible to patient
        MedicalRecordDto dto = medicalRecordMapper.toDto(record);
        if (dto.getEntries() != null) {
            List<MedicalRecordEntryDto> visibleEntries = dto.getEntries().stream()
                    .filter(MedicalRecordEntryDto::isVisibleToPatient)
                    .collect(Collectors.toList());
            dto.setEntries(visibleEntries);
        }
        
        return dto;
    }

    /**
     * Finds medical records by allergies content
     */
    public Page<MedicalRecordDto> findByAllergiesContaining(String allergyPattern, Pageable pageable) {
        log.info("Finding medical records by allergy pattern: {}", allergyPattern);
        
        Page<MedicalRecord> records = medicalRecordRepository.findByAllergiesContaining(allergyPattern, pageable);
        return records.map(medicalRecordMapper::toDto);
    }

    /**
     * Finds medical records by chronic conditions
     */
    public Page<MedicalRecordDto> findByChronicConditionsContaining(String conditionPattern, Pageable pageable) {
        log.info("Finding medical records by chronic condition pattern: {}", conditionPattern);
        
        Page<MedicalRecord> records = medicalRecordRepository.findByChronicConditionsContaining(conditionPattern, pageable);
        return records.map(medicalRecordMapper::toDto);
    }

    /**
     * Finds medical records by current medications
     */
    public Page<MedicalRecordDto> findByCurrentMedicationsContaining(String medicationPattern, Pageable pageable) {
        log.info("Finding medical records by medication pattern: {}", medicationPattern);
        
        Page<MedicalRecord> records = medicalRecordRepository.findByCurrentMedicationsContaining(medicationPattern, pageable);
        return records.map(medicalRecordMapper::toDto);
    }

    /**
     * Finds medical records updated within date range
     */
    public Page<MedicalRecordDto> findByUpdatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.info("Finding medical records updated between {} and {}", startDate, endDate);
        
        Page<MedicalRecord> records = medicalRecordRepository.findByUpdatedAtBetween(startDate, endDate, pageable);
        return records.map(medicalRecordMapper::toDto);
    }

    /**
     * Finds medical records by doctor ID
     */
    public Page<MedicalRecordDto> findByDoctorId(Long doctorId, Pageable pageable) {
        log.info("Finding medical records by doctor ID: {}", doctorId);
        
        Page<MedicalRecord> records = medicalRecordRepository.findByDoctorId(doctorId, pageable);
        return records.map(medicalRecordMapper::toDto);
    }

    /**
     * Finds medical records by entry type
     */
    public Page<MedicalRecordDto> findByEntryType(MedicalRecordEntry.EntryType entryType, Pageable pageable) {
        log.info("Finding medical records by entry type: {}", entryType);
        
        Page<MedicalRecord> records = medicalRecordRepository.findByEntryType(entryType, pageable);
        return records.map(medicalRecordMapper::toDto);
    }

    /**
     * Finds medical records by entry content
     */
    public Page<MedicalRecordDto> findByEntryContentContaining(String contentPattern, Pageable pageable) {
        log.info("Finding medical records by entry content pattern: {}", contentPattern);
        
        Page<MedicalRecord> records = medicalRecordRepository.findByEntryContentContaining(contentPattern, pageable);
        return records.map(medicalRecordMapper::toDto);
    }

    /**
     * Finds medical records with entries created within date range
     */
    public Page<MedicalRecordDto> findByEntryDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.info("Finding medical records with entries between {} and {}", startDate, endDate);
        
        Page<MedicalRecord> records = medicalRecordRepository.findByEntryDateBetween(startDate, endDate, pageable);
        return records.map(medicalRecordMapper::toDto);
    }

    /**
     * Advanced search for medical records
     */
    public Page<MedicalRecordDto> searchMedicalRecords(String allergyPattern, String conditionPattern, 
                                                     String medicationPattern, MedicalRecordEntry.EntryType entryType, 
                                                     String contentPattern, Pageable pageable) {
        log.info("Advanced search for medical records with multiple criteria");
        
        Page<MedicalRecord> records = medicalRecordRepository.searchMedicalRecords(
                allergyPattern, conditionPattern, medicationPattern, entryType, contentPattern, pageable);
        
        return records.map(medicalRecordMapper::toDto);
    }

    /**
     * Finds medical records with no entries
     */
    public Page<MedicalRecordDto> findMedicalRecordsWithNoEntries(Pageable pageable) {
        log.info("Finding medical records with no entries");
        
        Page<MedicalRecord> records = medicalRecordRepository.findMedicalRecordsWithNoEntries(pageable);
        return records.map(medicalRecordMapper::toDto);
    }

    /**
     * Finds medical records with minimum number of entries
     */
    public Page<MedicalRecordDto> findMedicalRecordsWithMinimumEntries(long minEntries, Pageable pageable) {
        log.info("Finding medical records with at least {} entries", minEntries);
        
        Page<MedicalRecord> records = medicalRecordRepository.findMedicalRecordsWithMinimumEntries(minEntries, pageable);
        return records.map(medicalRecordMapper::toDto);
    }

    /**
     * Gets medical record statistics (entry counts)
     */
    public List<Object[]> getMedicalRecordEntryStats() {
        return medicalRecordRepository.countEntriesByMedicalRecord();
    }

    /**
     * Gets all medical records with basic info
     */
    public Page<MedicalRecordRepository.MedicalRecordBasicInfo> getAllMedicalRecordsBasicInfo(Pageable pageable) {
        return medicalRecordRepository.findAllBasicInfo(pageable);
    }

    /**
     * Gets all medical records as DTOs
     */
    public Page<MedicalRecordDto> getAllMedicalRecords(Pageable pageable) {
        Page<MedicalRecord> records = medicalRecordRepository.findAll(pageable);
        return records.map(medicalRecordMapper::toDto);
    }

    /**
     * Gets recent medical record entries for a patient
     */
    public List<MedicalRecordEntryDto> getRecentEntriesForPatient(Long patientId, int limit) {
        log.info("Getting {} recent entries for patient: {}", limit, patientId);
        
        Optional<MedicalRecord> recordOpt = medicalRecordRepository.findByPatientId(patientId);
        if (recordOpt.isEmpty()) {
            return List.of();
        }
        
        MedicalRecord record = recordOpt.get();
        if (record.getEntries() == null || record.getEntries().isEmpty()) {
            return List.of();
        }
        
        return record.getEntries().stream()
                .sorted((e1, e2) -> e2.getEntryDate().compareTo(e1.getEntryDate())) // Most recent first
                .limit(limit)
                .map(medicalRecordMapper::toEntryDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets entries by type for a patient
     */
    public List<MedicalRecordEntryDto> getEntriesByTypeForPatient(Long patientId, MedicalRecordEntry.EntryType entryType) {
        log.info("Getting {} entries for patient: {}", entryType, patientId);
        
        Optional<MedicalRecord> recordOpt = medicalRecordRepository.findByPatientId(patientId);
        if (recordOpt.isEmpty()) {
            return List.of();
        }
        
        MedicalRecord record = recordOpt.get();
        if (record.getEntries() == null || record.getEntries().isEmpty()) {
            return List.of();
        }
        
        return record.getEntries().stream()
                .filter(entry -> entry.getType() == entryType)
                .sorted((e1, e2) -> e2.getEntryDate().compareTo(e1.getEntryDate()))
                .map(medicalRecordMapper::toEntryDto)
                .collect(Collectors.toList());
    }

    /**
     * Checks if patient has specific allergy
     */
    public boolean hasAllergy(Long patientId, String allergyName) {
        Optional<MedicalRecord> recordOpt = medicalRecordRepository.findByPatientId(patientId);
        if (recordOpt.isEmpty()) {
            return false;
        }
        
        MedicalRecord record = recordOpt.get();
        return record.getAllergies() != null && 
               record.getAllergies().toLowerCase().contains(allergyName.toLowerCase());
    }

    /**
     * Checks if patient has specific chronic condition
     */
    public boolean hasChronicCondition(Long patientId, String conditionName) {
        Optional<MedicalRecord> recordOpt = medicalRecordRepository.findByPatientId(patientId);
        if (recordOpt.isEmpty()) {
            return false;
        }
        
        MedicalRecord record = recordOpt.get();
        return record.getChronicConditions() != null && 
               record.getChronicConditions().toLowerCase().contains(conditionName.toLowerCase());
    }
}