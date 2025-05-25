package com.example.miapp.service.prescription;

import com.example.miapp.entity.Prescription;
import com.example.miapp.entity.PrescriptionItem;
import com.example.miapp.entity.Prescription.PrescriptionStatus;
import com.example.miapp.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service responsible for prescription entity management operations (Single Responsibility)
 * Implements Command Pattern for operations
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PrescriptionManagementService {

    private final PrescriptionRepository prescriptionRepository;

    /**
     * Updates prescription status
     */
    public void updatePrescriptionStatus(Long prescriptionId, PrescriptionStatus status) {
        log.info("Updating prescription {} status to {}", prescriptionId, status);
        
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));
        
        prescription.setStatus(status);
        prescriptionRepository.save(prescription);
        
        log.info("Successfully updated prescription {} status to {}", prescriptionId, status);
    }

    /**
     * Marks prescription as printed
     */
    public void markPrescriptionAsPrinted(Long prescriptionId) {
        log.info("Marking prescription {} as printed", prescriptionId);
        
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));
        
        prescription.setPrinted(true);
        prescription.setPrintDate(LocalDateTime.now());
        prescriptionRepository.save(prescription);
        
        log.info("Successfully marked prescription {} as printed", prescriptionId);
    }

    /**
     * Updates prescription notes
     */
    public void updatePrescriptionNotes(Long prescriptionId, String notes) {
        log.info("Updating notes for prescription {}", prescriptionId);
        
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));
        
        prescription.setNotes(notes);
        prescriptionRepository.save(prescription);
        
        log.info("Successfully updated notes for prescription {}", prescriptionId);
    }

    /**
     * Processes refill for prescription item
     */
    public void processRefill(Long prescriptionId, Long itemId, int refillCount) {
        log.info("Processing {} refills for item {} in prescription {}", refillCount, itemId, prescriptionId);
        
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));
        
        PrescriptionItem item = prescription.getMedicationItems().stream()
                .filter(pi -> pi.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Prescription item not found: " + itemId));
        
        // Update refills used
        int newRefillsUsed = item.getRefillsUsed() + refillCount;
        item.setRefillsUsed(newRefillsUsed);
        
        prescriptionRepository.save(prescription);
        
        log.info("Successfully processed {} refills for item {} in prescription {}", 
                refillCount, itemId, prescriptionId);
    }

    /**
     * Adds medication item to existing prescription
     */
    public void addMedicationItem(Long prescriptionId, PrescriptionItem newItem) {
        log.info("Adding medication item {} to prescription {}", newItem.getMedicationName(), prescriptionId);
        
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));
        
        newItem.setPrescription(prescription);
        prescription.getMedicationItems().add(newItem);
        
        prescriptionRepository.save(prescription);
        
        log.info("Successfully added medication item {} to prescription {}", 
                newItem.getMedicationName(), prescriptionId);
    }

    /**
     * Removes medication item from prescription
     */
    public void removeMedicationItem(Long prescriptionId, Long itemId) {
        log.info("Removing medication item {} from prescription {}", itemId, prescriptionId);
        
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));
        
        boolean removed = prescription.getMedicationItems().removeIf(item -> item.getId().equals(itemId));
        
        if (!removed) {
            throw new RuntimeException("Prescription item not found: " + itemId);
        }
        
        prescriptionRepository.save(prescription);
        
        log.info("Successfully removed medication item {} from prescription {}", itemId, prescriptionId);
    }

    /**
     * Updates medication item details
     */
    public void updateMedicationItem(Long prescriptionId, Long itemId, PrescriptionItem updatedItem) {
        log.info("Updating medication item {} in prescription {}", itemId, prescriptionId);
        
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));
        
        PrescriptionItem existingItem = prescription.getMedicationItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Prescription item not found: " + itemId));
        
        // Update item fields
        existingItem.setMedicationName(updatedItem.getMedicationName());
        existingItem.setDosage(updatedItem.getDosage());
        existingItem.setFrequency(updatedItem.getFrequency());
        existingItem.setDuration(updatedItem.getDuration());
        existingItem.setInstructions(updatedItem.getInstructions());
        existingItem.setQuantity(updatedItem.getQuantity());
        existingItem.setRoute(updatedItem.getRoute());
        existingItem.setRefillable(updatedItem.isRefillable());
        existingItem.setRefillsAllowed(updatedItem.getRefillsAllowed());
        
        prescriptionRepository.save(prescription);
        
        log.info("Successfully updated medication item {} in prescription {}", itemId, prescriptionId);
    }

    /**
     * Cancels prescription
     */
    public void cancelPrescription(Long prescriptionId, String reason) {
        log.info("Canceling prescription {} with reason: {}", prescriptionId, reason);
        
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));
        
        prescription.setStatus(PrescriptionStatus.CANCELED);
        
        // Add cancellation reason to notes
        String cancellationNote = prescription.getNotes() != null ? 
            prescription.getNotes() + "\n\n[CANCELED: " + reason + "]" : 
            "[CANCELED: " + reason + "]";
        prescription.setNotes(cancellationNote);
        
        prescriptionRepository.save(prescription);
        
        log.info("Successfully canceled prescription {}", prescriptionId);
    }

    /**
     * Completes prescription (marks as filled/dispensed)
     */
    public void completePrescription(Long prescriptionId) {
        log.info("Completing prescription {}", prescriptionId);
        
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));
        
        prescription.setStatus(PrescriptionStatus.COMPLETED);
        
        prescriptionRepository.save(prescription);
        
        log.info("Successfully completed prescription {}", prescriptionId);
    }

    /**
     * Updates prescription diagnosis
     */
    public void updateDiagnosis(Long prescriptionId, String diagnosis) {
        log.info("Updating diagnosis for prescription {}", prescriptionId);
        
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));
        
        prescription.setDiagnosis(diagnosis);
        prescriptionRepository.save(prescription);
        
        log.info("Successfully updated diagnosis for prescription {}", prescriptionId);
    }

    /**
     * Renews prescription (creates a new prescription based on existing one)
     */
    public Prescription renewPrescription(Long originalPrescriptionId) {
        log.info("Renewing prescription {}", originalPrescriptionId);
        
        Prescription originalPrescription = prescriptionRepository.findById(originalPrescriptionId)
                .orElseThrow(() -> new RuntimeException("Original prescription not found: " + originalPrescriptionId));
        
        // Create new prescription based on original
        Prescription renewedPrescription = Prescription.builder()
                .doctor(originalPrescription.getDoctor())
                .patient(originalPrescription.getPatient())
                .issueDate(LocalDateTime.now())
                .diagnosis(originalPrescription.getDiagnosis())
                .notes("Renewed from prescription #" + originalPrescriptionId)
                .status(PrescriptionStatus.ACTIVE)
                .printed(false)
                .build();
        
        // Copy medication items (reset refill counts)
        for (PrescriptionItem originalItem : originalPrescription.getMedicationItems()) {
            PrescriptionItem renewedItem = PrescriptionItem.builder()
                    .prescription(renewedPrescription)
                    .medicationName(originalItem.getMedicationName())
                    .dosage(originalItem.getDosage())
                    .frequency(originalItem.getFrequency())
                    .duration(originalItem.getDuration())
                    .instructions(originalItem.getInstructions())
                    .quantity(originalItem.getQuantity())
                    .route(originalItem.getRoute())
                    .refillable(originalItem.isRefillable())
                    .refillsAllowed(originalItem.getRefillsAllowed())
                    .refillsUsed(0) // Reset refill count
                    .build();
            
            renewedPrescription.getMedicationItems().add(renewedItem);
        }
        
        Prescription savedPrescription = prescriptionRepository.save(renewedPrescription);
        
        log.info("Successfully renewed prescription {} as new prescription {}", 
                originalPrescriptionId, savedPrescription.getId());
        
        return savedPrescription;
    }

    /**
     * Archives prescription (soft delete alternative)
     */
    public void archivePrescription(Long prescriptionId, String reason) {
        log.info("Archiving prescription {} with reason: {}", prescriptionId, reason);
        
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));
        
        // In a real implementation, you might add an 'archived' flag
        // prescription.setArchived(true);
        // prescription.setArchiveReason(reason);
        // prescription.setArchivedAt(LocalDateTime.now());
        
        // For now, we'll add a note to indicate archival
        String archiveNote = prescription.getNotes() != null ? 
            prescription.getNotes() + "\n\n[ARCHIVED: " + reason + "]" : 
            "[ARCHIVED: " + reason + "]";
        
        prescription.setNotes(archiveNote);
        prescriptionRepository.save(prescription);
        
        log.info("Successfully archived prescription {}", prescriptionId);
    }

    /**
     * Deletes prescription (hard delete - use with extreme caution)
     */
    public void deletePrescription(Long prescriptionId) {
        log.info("Deleting prescription {}", prescriptionId);
        
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));
        
        // Additional validations should be performed by validation service
        prescriptionRepository.delete(prescription);
        
        log.info("Successfully deleted prescription {}", prescriptionId);
    }

    /**
     * Bulk updates prescription status
     */
    public void bulkUpdatePrescriptionStatus(java.util.List<Long> prescriptionIds, PrescriptionStatus status, String reason) {
        log.info("Bulk updating {} prescriptions to status {} with reason: {}", 
                prescriptionIds.size(), status, reason);
        
        for (Long prescriptionId : prescriptionIds) {
            try {
                updatePrescriptionStatus(prescriptionId, status);
                
                if (reason != null && !reason.trim().isEmpty()) {
                    updatePrescriptionNotes(prescriptionId, reason);
                }
            } catch (Exception e) {
                log.error("Failed to update prescription {}: {}", prescriptionId, e.getMessage());
            }
        }
        
        log.info("Completed bulk update for {} prescriptions", prescriptionIds.size());
    }

    /**
     * Updates refill information for prescription item
     */
    public void updateRefillInfo(Long prescriptionId, Long itemId, boolean refillable, Integer refillsAllowed) {
        log.info("Updating refill info for item {} in prescription {}", itemId, prescriptionId);
        
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));
        
        PrescriptionItem item = prescription.getMedicationItems().stream()
                .filter(pi -> pi.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Prescription item not found: " + itemId));
        
        item.setRefillable(refillable);
        if (refillsAllowed != null) {
            item.setRefillsAllowed(refillsAllowed);
        }
        
        prescriptionRepository.save(prescription);
        
        log.info("Successfully updated refill info for item {} in prescription {}", itemId, prescriptionId);
    }
}