package com.example.miapp.service.prescription;

import com.example.miapp.dto.prescription.CreatePrescriptionRequest;
import com.example.miapp.dto.prescription.PrescriptionDto;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.Patient;
import com.example.miapp.entity.Prescription;
import com.example.miapp.entity.PrescriptionItem;
import com.example.miapp.entity.Prescription.PrescriptionStatus;
import com.example.miapp.mapper.PrescriptionMapper;
import com.example.miapp.repository.PrescriptionRepository;
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

/**
 * Main business service that orchestrates prescription operations (Facade Pattern)
 * Applies SOLID principles and Design Patterns
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PrescriptionBusinessService implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionMapper prescriptionMapper;
    private final DoctorService doctorService;
    private final PatientService patientService;

    // Composed services following Single Responsibility
    private final PrescriptionValidationService validationService;
    private final PrescriptionQueryService queryService;
    private final PrescriptionManagementService managementService;

    /**
     * Creates new prescription with validations (Template Method Pattern)
     */
    @Override
    public PrescriptionDto createPrescription(CreatePrescriptionRequest request) {
        log.info("Creating prescription for patient {} by doctor {}", request.getPatientId(), request.getDoctorId());

        // Step 1: Get required entities
        Doctor doctor = doctorService.findDoctorById(request.getDoctorId());
        Patient patient = patientService.findPatientById(request.getPatientId());

        // Step 2: Validate prescription creation
        validationService.validatePrescriptionCreation(request, doctor, patient);

        // Step 3: Create prescription entity
        Prescription prescription = createPrescriptionEntity(request, doctor, patient);

        // Step 4: Create and add medication items
        addMedicationItems(prescription, request);

        // Step 5: Save prescription
        Prescription savedPrescription = prescriptionRepository.save(prescription);

        log.info("Successfully created prescription with ID: {}", savedPrescription.getId());
        return prescriptionMapper.toDto(savedPrescription);
    }

    /**
     * Updates existing prescription
     */
    @Override
    public PrescriptionDto updatePrescription(Long prescriptionId, CreatePrescriptionRequest request) {
        log.info("Updating prescription: {}", prescriptionId);

        // Get existing prescription and doctor
        Prescription existingPrescription = queryService.findPrescriptionById(prescriptionId);
        Doctor doctor = doctorService.findDoctorById(request.getDoctorId());

        // Validate update
        validationService.validatePrescriptionUpdate(request, existingPrescription, doctor);

        // Update prescription fields
        updatePrescriptionFields(existingPrescription, request);

        // Update medication items
        updateMedicationItems(existingPrescription, request);

        Prescription updatedPrescription = prescriptionRepository.save(existingPrescription);

        log.info("Successfully updated prescription: {}", prescriptionId);
        return prescriptionMapper.toDto(updatedPrescription);
    }

    /**
     * Cancels prescription after validation
     */
    @Override
    public void cancelPrescription(Long prescriptionId, Long doctorId, String reason) {
        log.info("Canceling prescription {} by doctor {} with reason: {}", prescriptionId, doctorId, reason);

        Prescription prescription = queryService.findPrescriptionById(prescriptionId);
        Doctor doctor = doctorService.findDoctorById(doctorId);

        validationService.validatePrescriptionCancellation(prescription, doctor);
        managementService.cancelPrescription(prescriptionId, reason);

        log.info("Successfully canceled prescription: {}", prescriptionId);
    }

    /**
     * Processes prescription refill
     */
    @Override
    public void processRefill(Long prescriptionId, Long itemId, int refillCount, Long requestingDoctorId) {
        log.info("Processing {} refills for item {} in prescription {} by doctor {}", 
                refillCount, itemId, prescriptionId, requestingDoctorId);

        Prescription prescription = queryService.findPrescriptionById(prescriptionId);
        PrescriptionItem item = findPrescriptionItem(prescription, itemId);
        Doctor doctor = doctorService.findDoctorById(requestingDoctorId);

        // Validate refill request
        validationService.validateRefillRequest(item, refillCount);
        validationService.validateDoctorCanModifyPrescription(doctor, prescription);

        managementService.processRefill(prescriptionId, itemId, refillCount);

        log.info("Successfully processed {} refills for item {} in prescription {}", 
                refillCount, itemId, prescriptionId);
    }

    /**
     * Renews prescription (creates new prescription based on existing)
     */
    @Override
    public PrescriptionDto renewPrescription(Long originalPrescriptionId, Long doctorId) {
        log.info("Renewing prescription {} by doctor {}", originalPrescriptionId, doctorId);

        Prescription originalPrescription = queryService.findPrescriptionById(originalPrescriptionId);
        Doctor doctor = doctorService.findDoctorById(doctorId);

        // Validate doctor can renew this prescription
        validationService.validateDoctorCanModifyPrescription(doctor, originalPrescription);

        // Check for any new contraindications
        CreatePrescriptionRequest renewalRequest = createRenewalRequest(originalPrescription);
        validationService.validatePrescriptionCreation(renewalRequest, doctor, originalPrescription.getPatient());

        Prescription renewedPrescription = managementService.renewPrescription(originalPrescriptionId);

        log.info("Successfully renewed prescription {} as new prescription {}", 
                originalPrescriptionId, renewedPrescription.getId());
        return prescriptionMapper.toDto(renewedPrescription);
    }

    // Query operations - delegate to query service

    @Override
    @Transactional(readOnly = true)
    public PrescriptionDto getPrescription(Long prescriptionId) {
        return queryService.getPrescription(prescriptionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrescriptionDto> findPrescriptionsByPatient(Long patientId, Pageable pageable) {
        return queryService.findPrescriptionsByPatient(patientId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrescriptionDto> findPrescriptionsByDoctor(Long doctorId, Pageable pageable) {
        return queryService.findPrescriptionsByDoctor(doctorId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrescriptionDto> findActivePrescriptionsByPatient(Long patientId, Pageable pageable) {
        return queryService.findActivePrescriptionsByPatient(patientId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDto> getCurrentMedicationsForPatient(Long patientId) {
        return queryService.getCurrentMedicationsForPatient(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrescriptionDto> findPrescriptionsNeedingRenewal(int daysAhead, Pageable pageable) {
        return queryService.findPrescriptionsNeedingRenewal(daysAhead, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasBeenPrescribed(Long patientId, String medicationName) {
        return queryService.hasBeenPrescribed(patientId, medicationName);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPotentialDrugInteraction(Long patientId, String newMedicationName) {
        return queryService.hasPotentialDrugInteraction(patientId, newMedicationName);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrescriptionDto> searchPrescriptions(Long doctorId, Long patientId, PrescriptionStatus status,
                                                   LocalDateTime startDate, LocalDateTime endDate, 
                                                   String diagnosisPattern, String medicationName, 
                                                   Pageable pageable) {
        return queryService.searchPrescriptions(doctorId, patientId, status, startDate, endDate, 
                                              diagnosisPattern, medicationName, pageable);
    }

    // Management operations - delegate to management service

    @Override
    public void markPrescriptionAsPrinted(Long prescriptionId) {
        managementService.markPrescriptionAsPrinted(prescriptionId);
    }

    @Override
    public void completePrescription(Long prescriptionId) {
        managementService.completePrescription(prescriptionId);
    }

    @Override
    public void updatePrescriptionNotes(Long prescriptionId, String notes) {
        managementService.updatePrescriptionNotes(prescriptionId, notes);
    }

    @Override
    public void deletePrescription(Long prescriptionId, Long doctorId) {
        Prescription prescription = queryService.findPrescriptionById(prescriptionId);
        Doctor doctor = doctorService.findDoctorById(doctorId);

        validationService.validateDoctorCanModifyPrescription(doctor, prescription);
        managementService.deletePrescription(prescriptionId);
    }

    // Statistics and reporting

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getPrescriptionStatusStats() {
        return queryService.getPrescriptionStatusStats();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getPrescriptionStatsByDoctor(LocalDateTime startDate, LocalDateTime endDate) {
        return queryService.getPrescriptionStatsByDoctor(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getMostPrescribedMedications(int limit) {
        return queryService.getMostPrescribedMedications(limit);
    }

    // Private helper methods (Template Method Pattern steps)

    private Prescription createPrescriptionEntity(CreatePrescriptionRequest request, Doctor doctor, Patient patient) {
        Prescription prescription = Prescription.builder()
                .doctor(doctor)
                .patient(patient)
                .issueDate(LocalDateTime.now())
                .diagnosis(request.getDiagnosis())
                .notes(request.getNotes())
                .status(PrescriptionStatus.ACTIVE)
                .printed(false)
                .build();
        
        // Set appointment if provided
        if (request.getAppointmentId() != null) {
            // Note: In a real implementation, you'd fetch the appointment entity
            // Appointment appointment = appointmentService.findById(request.getAppointmentId());
            // prescription.setAppointment(appointment);
        }
        
        return prescription;
    }

    private void addMedicationItems(Prescription prescription, CreatePrescriptionRequest request) {
        List<PrescriptionItem> items = prescriptionMapper.toItemEntityList(request.getMedicationItems());
        
        for (PrescriptionItem item : items) {
            item.setPrescription(prescription);
            prescription.getMedicationItems().add(item);
        }
    }

    private void updatePrescriptionFields(Prescription prescription, CreatePrescriptionRequest request) {
        if (request.getDiagnosis() != null) {
            prescription.setDiagnosis(request.getDiagnosis());
        }
        if (request.getNotes() != null) {
            prescription.setNotes(request.getNotes());
        }
    }

    private void updateMedicationItems(Prescription prescription, CreatePrescriptionRequest request) {
        // Clear existing items and add new ones
        prescription.getMedicationItems().clear();
        addMedicationItems(prescription, request);
    }

    private PrescriptionItem findPrescriptionItem(Prescription prescription, Long itemId) {
        return prescription.getMedicationItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Prescription item not found: " + itemId));
    }

    private CreatePrescriptionRequest createRenewalRequest(Prescription originalPrescription) {
        CreatePrescriptionRequest request = new CreatePrescriptionRequest();
        request.setDoctorId(originalPrescription.getDoctor().getId());
        request.setPatientId(originalPrescription.getPatient().getId());
        request.setDiagnosis(originalPrescription.getDiagnosis());
        request.setNotes("Renewed from prescription #" + originalPrescription.getId());
        
        // Convert prescription items to request format
        request.setMedicationItems(
            originalPrescription.getMedicationItems().stream()
                .map(this::convertToCreateRequest)
                .toList()
        );
        
        return request;
    }

    private com.example.miapp.dto.prescription.CreatePrescriptionItemRequest convertToCreateRequest(PrescriptionItem item) {
        com.example.miapp.dto.prescription.CreatePrescriptionItemRequest request = 
            new com.example.miapp.dto.prescription.CreatePrescriptionItemRequest();
        request.setMedicationName(item.getMedicationName());
        request.setDosage(item.getDosage());
        request.setFrequency(item.getFrequency());
        request.setDuration(item.getDuration());
        request.setInstructions(item.getInstructions());
        request.setQuantity(item.getQuantity());
        request.setRoute(item.getRoute());
        request.setRefillable(item.isRefillable());
        request.setRefillsAllowed(item.getRefillsAllowed());
        
        return request;
    }
}