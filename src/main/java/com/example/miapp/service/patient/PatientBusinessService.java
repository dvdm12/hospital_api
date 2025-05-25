package com.example.miapp.service.patient;

import com.example.miapp.dto.patient.CreatePatientRequest;
import com.example.miapp.dto.patient.PatientDto;
import com.example.miapp.dto.patient.PatientSearchCriteria;
import com.example.miapp.entity.MedicalRecord;
import com.example.miapp.entity.Patient;
import com.example.miapp.entity.Role;
import com.example.miapp.entity.User;
import com.example.miapp.entity.Patient.Gender;
import com.example.miapp.mapper.PatientMapper;
import com.example.miapp.repository.PatientRepository;
import com.example.miapp.repository.RoleRepository;
import com.example.miapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Main business service that orchestrates patient operations (Facade Pattern)
 * Applies SOLID principles and Design Patterns
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PatientBusinessService implements PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PatientMapper patientMapper;
    private final PasswordEncoder passwordEncoder;

    // Composed services following Single Responsibility
    private final PatientValidationService validationService;
    private final PatientQueryService queryService;
    private final PatientManagementService managementService;

    /**
     * Creates a new patient with user account (Template Method Pattern)
     */
    @Override
    public PatientDto createPatient(CreatePatientRequest request) {
        log.info("Creating new patient: {} {}", request.getFirstName(), request.getLastName());

        // Step 1: Validate request
        validationService.validatePatientCreation(request);

        // Step 2: Create user account
        User user = createPatientUser(request);

        // Step 3: Create patient entity
        Patient patient = createPatientEntity(request, user);

        // Step 4: Create initial medical record
        MedicalRecord medicalRecord = createInitialMedicalRecord(patient);
        patient.setMedicalRecord(medicalRecord);

        // Step 5: Save patient
        Patient savedPatient = patientRepository.save(patient);

        log.info("Successfully created patient with ID: {}", savedPatient.getId());
        return patientMapper.toDto(savedPatient);
    }

    /**
     * Updates existing patient information
     */
    @Override
    public PatientDto updatePatient(Long patientId, CreatePatientRequest request) {
        log.info("Updating patient with ID: {}", patientId);

        // Validate update
        validationService.validatePatientUpdate(patientId, request);

        // Get existing patient
        Patient patient = queryService.findPatientById(patientId);

        // Update patient fields
        updatePatientFields(patient, request);

        Patient updatedPatient = patientRepository.save(patient);

        log.info("Successfully updated patient with ID: {}", patientId);
        return patientMapper.toDto(updatedPatient);
    }

    /**
     * Deletes patient after validation
     */
    @Override
    public void deletePatient(Long patientId) {
        log.info("Deleting patient with ID: {}", patientId);

        Patient patient = queryService.findPatientById(patientId);
        validationService.validatePatientDeletion(patient);
        managementService.deletePatient(patientId);

        log.info("Successfully deleted patient with ID: {}", patientId);
    }

    /**
     * Archives patient instead of deleting
     */
    @Override
    public void archivePatient(Long patientId, String reason) {
        log.info("Archiving patient with ID: {}", patientId);

        Patient patient = queryService.findPatientById(patientId);
        validationService.validatePatientDeletion(patient); // Same validations apply
        managementService.archivePatient(patientId, reason);

        log.info("Successfully archived patient with ID: {}", patientId);
    }

    // Query operations - delegate to query service

    @Override
    @Transactional(readOnly = true)
    public PatientDto getPatient(Long patientId) {
        return queryService.getPatient(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public Patient findPatientById(Long patientId) {
        return queryService.findPatientById(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PatientDto> findPatientByEmail(String email) {
        return queryService.findPatientByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PatientDto> findPatientByPhone(String phone) {
        return queryService.findPatientByPhone(phone);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> searchPatientsByName(String namePattern, Pageable pageable) {
        return queryService.searchPatientsByName(namePattern, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findPatientsByGender(Gender gender, Pageable pageable) {
        return queryService.findPatientsByGender(gender, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findPatientsByAgeRange(int minAge, int maxAge, Pageable pageable) {
        return queryService.findPatientsByAgeRange(minAge, maxAge, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findPatientsByInsuranceProvider(String insuranceProvider, Pageable pageable) {
        return queryService.findPatientsByInsuranceProvider(insuranceProvider, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findPatientsByDoctor(Long doctorId, Pageable pageable) {
        return queryService.findPatientsByDoctor(doctorId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> searchPatients(PatientSearchCriteria criteria, Pageable pageable) {
        return queryService.searchPatients(criteria, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> getAllPatients(Pageable pageable) {
        return queryService.getAllPatients(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findPatientsWithoutRecentAppointments(String interval, Pageable pageable) {
        return queryService.findPatientsWithoutRecentAppointments(interval, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findNewPatients(String interval, Pageable pageable) {
        return queryService.findNewPatients(interval, pageable);
    }

    // Management operations - delegate to management service

    @Override
    public void updateAddress(Long patientId, String address) {
        managementService.updateAddress(patientId, address);
    }

    @Override
    public void updatePhone(Long patientId, String phone) {
        validationService.validateUniquePhone(phone, patientId);
        managementService.updatePhone(patientId, phone);
    }

    @Override
    public void updateInsuranceInfo(Long patientId, String insuranceProvider, String insurancePolicyNumber) {
        validationService.validateInsuranceUpdate(insuranceProvider, insurancePolicyNumber);
        managementService.updateInsuranceInfo(patientId, insuranceProvider, insurancePolicyNumber);
    }

    @Override
    public void updateEmergencyContact(Long patientId, String emergencyContactName, String emergencyContactPhone) {
        validationService.validateEmergencyContactUpdate(emergencyContactName, emergencyContactPhone);
        managementService.updateEmergencyContact(patientId, emergencyContactName, emergencyContactPhone);
    }

    @Override
    public void updatePatientProfile(Long patientId, String firstName, String lastName, String phone, String address) {
        // Validate phone if being updated
        if (phone != null && !phone.trim().isEmpty()) {
            validationService.validateUniquePhone(phone, patientId);
        }
        managementService.updatePatientProfile(patientId, firstName, lastName, phone, address);
    }

    @Override
    public void mergePatientRecords(Long keepPatientId, Long mergePatientId, String reason) {
        // Validate both patients exist
        Patient keepPatient = queryService.findPatientById(keepPatientId);
        Patient mergePatient = queryService.findPatientById(mergePatientId);
        
        if (keepPatientId.equals(mergePatientId)) {
            throw new RuntimeException("Cannot merge patient with itself");
        }
        
        // Pass the loaded entities to avoid additional database queries
        managementService.mergePatientRecords(keepPatient, mergePatient, reason);
    }

    // Statistics and reporting

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getPatientStatsByGender() {
        return queryService.getPatientStatsByGender();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getPatientStatsByAgeGroup(int interval) {
        return queryService.getPatientStatsByAgeGroup(interval);
    }

    // Private helper methods (Template Method Pattern steps)

    private User createPatientUser(CreatePatientRequest request) {
        Role patientRole = roleRepository.findByName(Role.ERole.ROLE_PATIENT)
                .orElseThrow(() -> new RuntimeException("Patient role not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(User.UserStatus.ACTIVE)
                .roles(Set.of(patientRole))
                .firstLogin(true)
                .build();

        return userRepository.save(user);
    }

    private Patient createPatientEntity(CreatePatientRequest request, User user) {
        return Patient.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .birthDate(request.getBirthDate())
                .phone(request.getPhone())
                .address(request.getAddress())
                .gender(request.getGender())
                .bloodType(request.getBloodType())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .insuranceProvider(request.getInsuranceProvider())
                .insurancePolicyNumber(request.getInsurancePolicyNumber())
                .user(user)
                .build();
    }

    private MedicalRecord createInitialMedicalRecord(Patient patient) {
        return MedicalRecord.builder()
                .patient(patient)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void updatePatientFields(Patient patient, CreatePatientRequest request) {
        if (request.getFirstName() != null) {
            patient.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            patient.setLastName(request.getLastName());
        }
        if (request.getBirthDate() != null) {
            patient.setBirthDate(request.getBirthDate());
        }
        if (request.getPhone() != null) {
            patient.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            patient.setAddress(request.getAddress());
        }
        if (request.getGender() != null) {
            patient.setGender(request.getGender());
        }
        if (request.getBloodType() != null) {
            patient.setBloodType(request.getBloodType());
        }
        if (request.getEmergencyContactName() != null) {
            patient.setEmergencyContactName(request.getEmergencyContactName());
        }
        if (request.getEmergencyContactPhone() != null) {
            patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }
        if (request.getInsuranceProvider() != null) {
            patient.setInsuranceProvider(request.getInsuranceProvider());
        }
        if (request.getInsurancePolicyNumber() != null) {
            patient.setInsurancePolicyNumber(request.getInsurancePolicyNumber());
        }
    }
}
