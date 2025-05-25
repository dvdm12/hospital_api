package com.example.miapp.service.medicalrecord;

import com.example.miapp.entity.MedicalRecord;
import com.example.miapp.entity.MedicalRecordEntry;
import com.example.miapp.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for medical record entity management operations (Single Responsibility)
 * Implements Command Pattern for operations
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordManagementService {

    private final MedicalRecordRepository medicalRecordRepository;

    /**
     * Updates allergies information
     */
    public void updateAllergies(Long recordId, String allergies) {
        log.info("Updating allergies for medical record {}", recordId);
        
        int updatedRows = medicalRecordRepository.updateAllergies(recordId, allergies);
        
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update allergies for medical record: " + recordId);
        }
        
        log.info("Successfully updated allergies for medical record {}", recordId);
    }

    /**
     * Updates chronic conditions information
     */
    public void updateChronicConditions(Long recordId, String chronicConditions) {
        log.info("Updating chronic conditions for medical record {}", recordId);
        
        int updatedRows = medicalRecordRepository.updateChronicConditions(recordId, chronicConditions);
        
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update chronic conditions for medical record: " + recordId);
        }
        
        log.info("Successfully updated chronic conditions for medical record {}", recordId);
    }

    /**
     * Updates current medications information
     */
    public void updateCurrentMedications(Long recordId, String currentMedications) {
        log.info("Updating current medications for medical record {}", recordId);
        
        int updatedRows = medicalRecordRepository.updateCurrentMedications(recordId, currentMedications);
        
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update current medications for medical record: " + recordId);
        }
        
        log.info("Successfully updated current medications for medical record {}", recordId);
    }

    /**
     * Updates family history information
     */
    public void updateFamilyHistory(Long recordId, String familyHistory) {
        log.info("Updating family history for medical record {}", recordId);
        
        int updatedRows = medicalRecordRepository.updateFamilyHistory(recordId, familyHistory);
        
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update family history for medical record: " + recordId);
        }
        
        log.info("Successfully updated family history for medical record {}", recordId);
    }

    /**
     * Updates surgical history information
     */
    public void updateSurgicalHistory(Long recordId, String surgicalHistory) {
        log.info("Updating surgical history for medical record {}", recordId);
        
        int updatedRows = medicalRecordRepository.updateSurgicalHistory(recordId, surgicalHistory);
        
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update surgical history for medical record: " + recordId);
        }
        
        log.info("Successfully updated surgical history for medical record {}", recordId);
    }

    /**
     * Updates notes information
     */
    public void updateNotes(Long recordId, String notes) {
        log.info("Updating notes for medical record {}", recordId);
        
        int updatedRows = medicalRecordRepository.updateNotes(recordId, notes);
        
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update notes for medical record: " + recordId);
        }
        
        log.info("Successfully updated notes for medical record {}", recordId);
    }

    /**
     * Adds a new entry to medical record
     */
    public void addEntryToMedicalRecord(MedicalRecord record, MedicalRecordEntry entry) {
        log.info("Adding entry {} to medical record {}", entry.getType(), record.getId());
        
        record.addEntry(entry);
        medicalRecordRepository.save(record);
        
        log.info("Successfully added entry to medical record {}", record.getId());
    }

    /**
     * Removes entry from medical record
     */
    public void removeEntryFromMedicalRecord(MedicalRecord record, MedicalRecordEntry entry) {
        log.info("Removing entry {} from medical record {}", entry.getId(), record.getId());
        
        record.removeEntry(entry);
        medicalRecordRepository.save(record);
        
        log.info("Successfully removed entry from medical record {}", record.getId());
    }

    /**
     * Updates multiple medical record fields at once
     */
    public void updateMedicalRecordFields(Long recordId, String allergies, String chronicConditions, 
                                        String currentMedications, String surgicalHistory, 
                                        String familyHistory, String notes) {
        log.info("Updating multiple fields for medical record {}", recordId);
        
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found: " + recordId));
        
        boolean updated = false;
        
        if (allergies != null) {
            record.setAllergies(allergies.trim().isEmpty() ? null : allergies.trim());
            updated = true;
        }
        
        if (chronicConditions != null) {
            record.setChronicConditions(chronicConditions.trim().isEmpty() ? null : chronicConditions.trim());
            updated = true;
        }
        
        if (currentMedications != null) {
            record.setCurrentMedications(currentMedications.trim().isEmpty() ? null : currentMedications.trim());
            updated = true;
        }
        
        if (surgicalHistory != null) {
            record.setSurgicalHistory(surgicalHistory.trim().isEmpty() ? null : surgicalHistory.trim());
            updated = true;
        }
        
        if (familyHistory != null) {
            record.setFamilyHistory(familyHistory.trim().isEmpty() ? null : familyHistory.trim());
            updated = true;
        }
        
        if (notes != null) {
            record.setNotes(notes.trim().isEmpty() ? null : notes.trim());
            updated = true;
        }
        
        if (updated) {
            medicalRecordRepository.save(record);
            log.info("Successfully updated multiple fields for medical record {}", recordId);
        } else {
            log.info("No updates needed for medical record {}", recordId);
        }
    }

    /**
     * Updates entry visibility to patient
     */
    public void updateEntryVisibility(MedicalRecordEntry entry, boolean visibleToPatient) {
        log.info("Updating visibility of entry {} to {}", entry.getId(), visibleToPatient);
        
        entry.setVisibleToPatient(visibleToPatient);
        
        // Save through the medical record to maintain relationships
        MedicalRecord record = entry.getMedicalRecord();
        medicalRecordRepository.save(record);
        
        log.info("Successfully updated entry visibility for entry {}", entry.getId());
    }

    /**
     * Archives medical record (soft delete alternative)
     */
    public void archiveMedicalRecord(Long recordId, String reason) {
        log.info("Archiving medical record {} with reason: {}", recordId, reason);
        
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found: " + recordId));
        
        // In a real implementation, you might add an 'archived' flag and archive_reason
        // record.setArchived(true);
        // record.setArchiveReason(reason);
        // record.setArchivedAt(LocalDateTime.now());
        
        // For now, we'll add a note to indicate archival
        String archiveNote = record.getNotes() != null ? 
            record.getNotes() + "\n\n[ARCHIVED: " + reason + "]" : 
            "[ARCHIVED: " + reason + "]";
        
        record.setNotes(archiveNote);
        medicalRecordRepository.save(record);
        
        log.info("Successfully archived medical record {}", recordId);
    }

    /**
     * Deletes medical record (hard delete - use with extreme caution)
     */
    public void deleteMedicalRecord(Long recordId) {
        log.info("Deleting medical record {}", recordId);
        
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found: " + recordId));
        
        // Additional validations should be performed by validation service
        medicalRecordRepository.delete(record);
        
        log.info("Successfully deleted medical record {}", recordId);
    }

    /**
     * Merges medical records from different sources
     */
    public void mergeMedicalRecords(MedicalRecord primaryRecord, MedicalRecord secondaryRecord, String reason) {
        log.info("Merging medical record {} into {} with reason: {}", 
                secondaryRecord.getId(), primaryRecord.getId(), reason);
        
        // Merge basic information if primary record is missing data
        if (primaryRecord.getAllergies() == null && secondaryRecord.getAllergies() != null) {
            primaryRecord.setAllergies(secondaryRecord.getAllergies());
        } else if (primaryRecord.getAllergies() != null && secondaryRecord.getAllergies() != null) {
            // Combine allergies if both exist
            String combinedAllergies = primaryRecord.getAllergies() + "; " + secondaryRecord.getAllergies();
            primaryRecord.setAllergies(combinedAllergies);
        }
        
        if (primaryRecord.getChronicConditions() == null && secondaryRecord.getChronicConditions() != null) {
            primaryRecord.setChronicConditions(secondaryRecord.getChronicConditions());
        } else if (primaryRecord.getChronicConditions() != null && secondaryRecord.getChronicConditions() != null) {
            String combinedConditions = primaryRecord.getChronicConditions() + "; " + secondaryRecord.getChronicConditions();
            primaryRecord.setChronicConditions(combinedConditions);
        }
        
        if (primaryRecord.getCurrentMedications() == null && secondaryRecord.getCurrentMedications() != null) {
            primaryRecord.setCurrentMedications(secondaryRecord.getCurrentMedications());
        }
        
        if (primaryRecord.getSurgicalHistory() == null && secondaryRecord.getSurgicalHistory() != null) {
            primaryRecord.setSurgicalHistory(secondaryRecord.getSurgicalHistory());
        }
        
        if (primaryRecord.getFamilyHistory() == null && secondaryRecord.getFamilyHistory() != null) {
            primaryRecord.setFamilyHistory(secondaryRecord.getFamilyHistory());
        }
        
        // Merge entries from secondary record
        if (secondaryRecord.getEntries() != null && !secondaryRecord.getEntries().isEmpty()) {
            for (MedicalRecordEntry entry : secondaryRecord.getEntries()) {
                // Add note about the merge
                String mergedContent = entry.getContent() + 
                    "\n[Merged from record " + secondaryRecord.getId() + ": " + reason + "]";
                entry.setContent(mergedContent);
                primaryRecord.addEntry(entry);
            }
        }
        
        // Add merge note
        String mergeNote = primaryRecord.getNotes() != null ? 
            primaryRecord.getNotes() + "\n\n[MERGED with record " + secondaryRecord.getId() + ": " + reason + "]" : 
            "[MERGED with record " + secondaryRecord.getId() + ": " + reason + "]";
        primaryRecord.setNotes(mergeNote);
        
        // Save primary record with merged data
        medicalRecordRepository.save(primaryRecord);
        
        // Archive secondary record
        archiveMedicalRecord(secondaryRecord.getId(), "Merged into record " + primaryRecord.getId());
        
        log.info("Successfully merged medical record {} into {}", secondaryRecord.getId(), primaryRecord.getId());
    }

    /**
     * Bulk updates entry visibility for a medical record
     */
    public void bulkUpdateEntryVisibility(Long recordId, boolean visibleToPatient) {
        log.info("Bulk updating entry visibility for medical record {} to {}", recordId, visibleToPatient);
        
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found: " + recordId));
        
        if (record.getEntries() != null) {
            for (MedicalRecordEntry entry : record.getEntries()) {
                entry.setVisibleToPatient(visibleToPatient);
            }
            
            medicalRecordRepository.save(record);
            
            log.info("Successfully updated visibility for {} entries in medical record {}", 
                    record.getEntries().size(), recordId);
        }
    }
}