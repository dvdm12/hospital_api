package com.example.miapp.service.medicalrecord;

import com.example.miapp.dto.medicalrecord.CreateMedicalEntryRequest;
import com.example.miapp.dto.medicalrecord.MedicalRecordDto;
import com.example.miapp.dto.medicalrecord.MedicalRecordEntryDto;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.MedicalRecord;
import com.example.miapp.entity.MedicalRecordEntry;
import com.example.miapp.entity.Patient;
import com.example.miapp.mapper.MedicalRecordMapper;
import com.example.miapp.repository.MedicalRecordRepository;
import com.example.miapp.service.doctor.DoctorService;
import com.example.miapp.service.patient.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Main business service that orchestrates medical record operations (Facade Pattern)
 * Applies SOLID principles and Design Patterns
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordBusinessService implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalRecordMapper medicalRecordMapper;
    private final DoctorService doctorService;
    private final PatientService patientService;

    // Composed services following Single Responsibility
    private final MedicalRecordValidationService validationService;
    private final MedicalRecordQueryService queryService;
    private final MedicalRecordManagementService managementService;

    /**
     * Creates initial medical record for patient (Template Method Pattern)
     */
    @Override
    public MedicalRecordDto createMedicalRecord(Long patientId) {
        log.info("Creating medical record for patient: {}", patientId);

        // Step 1: Get patient entity
        Patient patient = patientService.findPatientById(patientId);

        // Step 2: Validate creation
        validationService.validateMedicalRecordCreation(patient);

        // Step 3: Create medical record
        MedicalRecord medicalRecord = createMedicalRecordEntity(patient);

        // Step 4: Save medical record
        MedicalRecord savedRecord = medicalRecordRepository.save(medicalRecord);

        log.info("Successfully created medical record with ID: {}", savedRecord.getId());
        return medicalRecordMapper.toDto(savedRecord);
    }

    /**
     * Adds new entry to medical record
     */
    @Override
    public MedicalRecordEntryDto addMedicalEntry(Long patientId, CreateMedicalEntryRequest request) {
        log.info("Adding medical entry for patient: {}", patientId);

        // Get required entities
        MedicalRecord medicalRecord = getMedicalRecordByPatientId(patientId);
        Doctor doctor = doctorService.findDoctorById(request.getDoctorId());

        // Validate entry creation
        validationService.validateMedicalEntryCreation(request, medicalRecord, doctor);

        // Create and add entry
        MedicalRecordEntry entry = createMedicalEntryEntity(request, medicalRecord, doctor);
        managementService.addEntryToMedicalRecord(medicalRecord, entry);

        log.info("Successfully added medical entry of type {} for patient {}", request.getType(), patientId);
        return medicalRecordMapper.toEntryDto(entry);
    }

    /**
     * Updates existing medical entry
     */
    @Override
    public MedicalRecordEntryDto updateMedicalEntry(Long entryId, CreateMedicalEntryRequest request) {
        log.info("Updating medical entry: {}", entryId);

        // Find existing entry
        MedicalRecordEntry existingEntry = findMedicalEntryById(entryId);
        Doctor doctor = doctorService.findDoctorById(request.getDoctorId());

        // Validate update
        validationService.validateMedicalEntryUpdate(request, existingEntry, doctor);

        // Update entry fields
        updateMedicalEntryFields(existingEntry, request);

        // Save through medical record to maintain relationships
        medicalRecordRepository.save(existingEntry.getMedicalRecord());

        log.info("Successfully updated medical entry: {}", entryId);
        return medicalRecordMapper.toEntryDto(existingEntry);
    }

    /**
     * Deletes medical entry after validation
     */
    @Override
    public void deleteMedicalEntry(Long entryId, Long requestingDoctorId) {
        log.info("Deleting medical entry: {} by doctor: {}", entryId, requestingDoctorId);

        MedicalRecordEntry entry = findMedicalEntryById(entryId);
        Doctor requestingDoctor = doctorService.findDoctorById(requestingDoctorId);

        validationService.validateEntryDeletion(entry, requestingDoctor);

        MedicalRecord medicalRecord = entry.getMedicalRecord();
        managementService.removeEntryFromMedicalRecord(medicalRecord, entry);

        log.info("Successfully deleted medical entry: {}", entryId);
    }

    // Query operations - delegate to query service

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordDto getMedicalRecord(Long recordId) {
        return queryService.getMedicalRecord(recordId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MedicalRecordDto> findMedicalRecordByPatientId(Long patientId) {
        return queryService.findMedicalRecordByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordDto getPatientMedicalHistory(Long patientId) {
        return queryService.getPatientMedicalHistory(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordDto getPatientVisibleMedicalHistory(Long patientId) {
        validationService.validatePatientAccess(
            queryService.findMedicalRecordById(
                queryService.findMedicalRecordByPatientId(patientId)
                    .map(dto -> findMedicalRecordByDto(dto))
                    .orElseThrow(() -> new RuntimeException("Medical record not found for patient: " + patientId))
                    .getId()
            ), 
            patientId
        );
        return queryService.getPatientVisibleMedicalHistory(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordEntryDto> getRecentEntriesForPatient(Long patientId, int limit) {
        return queryService.getRecentEntriesForPatient(patientId, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordEntryDto> getEntriesByTypeForPatient(Long patientId, MedicalRecordEntry.EntryType entryType) {
        return queryService.getEntriesByTypeForPatient(patientId, entryType);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicalRecordDto> searchMedicalRecords(String allergyPattern, String conditionPattern, 
                                                     String medicationPattern, MedicalRecordEntry.EntryType entryType, 
                                                     String contentPattern, Pageable pageable) {
        return queryService.searchMedicalRecords(allergyPattern, conditionPattern, medicationPattern, entryType, contentPattern, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAllergy(Long patientId, String allergyName) {
        return queryService.hasAllergy(patientId, allergyName);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasChronicCondition(Long patientId, String conditionName) {
        return queryService.hasChronicCondition(patientId, conditionName);
    }

    // Management operations - delegate to management service

    @Override
    public void updateAllergies(Long recordId, String allergies) {
        validationService.validateMedicalRecordUpdate(allergies, null, null, null, null, null);
        managementService.updateAllergies(recordId, allergies);
    }

    @Override
    public void updateChronicConditions(Long recordId, String chronicConditions) {
        validationService.validateMedicalRecordUpdate(null, chronicConditions, null, null, null, null);
        managementService.updateChronicConditions(recordId, chronicConditions);
    }

    @Override
    public void updateCurrentMedications(Long recordId, String currentMedications) {
        validationService.validateMedicalRecordUpdate(null, null, currentMedications, null, null, null);
        managementService.updateCurrentMedications(recordId, currentMedications);
    }

    @Override
    public void updateMedicalRecordFields(Long recordId, String allergies, String chronicConditions, 
                                        String currentMedications, String surgicalHistory, 
                                        String familyHistory, String notes) {
        validationService.validateMedicalRecordUpdate(allergies, chronicConditions, currentMedications, 
                                                    surgicalHistory, familyHistory, notes);
        managementService.updateMedicalRecordFields(recordId, allergies, chronicConditions, 
                                                  currentMedications, surgicalHistory, familyHistory, notes);
    }

    @Override
    public void updateEntryVisibility(Long entryId, boolean visibleToPatient) {
        MedicalRecordEntry entry = findMedicalEntryById(entryId);
        managementService.updateEntryVisibility(entry, visibleToPatient);
    }

    @Override
    public void mergeMedicalRecords(Long primaryRecordId, Long secondaryRecordId, String reason) {
        MedicalRecord primaryRecord = queryService.findMedicalRecordById(primaryRecordId);
        MedicalRecord secondaryRecord = queryService.findMedicalRecordById(secondaryRecordId);
        
        if (primaryRecordId.equals(secondaryRecordId)) {
            throw new RuntimeException("Cannot merge medical record with itself");
        }
        
        managementService.mergeMedicalRecords(primaryRecord, secondaryRecord, reason);
    }

    @Override
    public void deleteMedicalRecord(Long recordId) {
        MedicalRecord record = queryService.findMedicalRecordById(recordId);
        validationService.validateMedicalRecordDeletion(record);
        managementService.deleteMedicalRecord(recordId);
    }

    // Statistics and reporting

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getMedicalRecordEntryStats() {
        return queryService.getMedicalRecordEntryStats();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicalRecordDto> findByAllergiesContaining(String allergyPattern, Pageable pageable) {
        return queryService.findByAllergiesContaining(allergyPattern, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicalRecordDto> findByChronicConditionsContaining(String conditionPattern, Pageable pageable) {
        return queryService.findByChronicConditionsContaining(conditionPattern, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicalRecordDto> findByCurrentMedicationsContaining(String medicationPattern, Pageable pageable) {
        return queryService.findByCurrentMedicationsContaining(medicationPattern, pageable);
    }

    // Private helper methods (Template Method Pattern steps)

    private MedicalRecord createMedicalRecordEntity(Patient patient) {
        return MedicalRecord.builder()
                .patient(patient)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private MedicalRecordEntry createMedicalEntryEntity(CreateMedicalEntryRequest request, 
                                                      MedicalRecord medicalRecord, Doctor doctor) {
        MedicalRecordEntry entry = medicalRecordMapper.toEntryEntity(request);
        entry.setMedicalRecord(medicalRecord);
        entry.setDoctor(doctor);
        entry.setEntryDate(LocalDateTime.now());
        
        // Set appointment if provided
        if (request.getAppointmentId() != null) {
            // Note: In a real implementation, you'd fetch the appointment entity
            // entry.setAppointment(appointmentService.findById(request.getAppointmentId()));
        }
        
        return entry;
    }

    private void updateMedicalEntryFields(MedicalRecordEntry entry, CreateMedicalEntryRequest request) {
        if (request.getType() != null) {
            entry.setType(request.getType());
        }
        if (request.getTitle() != null) {
            entry.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            entry.setContent(request.getContent());
        }
        if (request.getAttachments() != null) {
            entry.setAttachments(request.getAttachments());
        }
        entry.setVisibleToPatient(request.isVisibleToPatient());
    }

    private MedicalRecord getMedicalRecordByPatientId(Long patientId) {
        return medicalRecordRepository.findByPatientId(patientId)
                .orElseThrow(() -> new RuntimeException("Medical record not found for patient: " + patientId));
    }

    private MedicalRecordEntry findMedicalEntryById(Long entryId) {
        // This would typically be in a separate repository or service
        // For now, we'll find it through the medical record
        return medicalRecordRepository.findAll().stream()
                .flatMap(record -> record.getEntries().stream())
                .filter(entry -> entry.getId().equals(entryId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Medical record entry not found: " + entryId));
    }

    private MedicalRecord findMedicalRecordByDto(MedicalRecordDto dto) {
        return queryService.findMedicalRecordById(dto.getId());
    }
}