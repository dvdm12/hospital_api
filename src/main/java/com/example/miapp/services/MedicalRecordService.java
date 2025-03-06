package com.example.miapp.services;

import com.example.miapp.dto.MedicalRecordDto;
import com.example.miapp.models.MedicalRecord;
import com.example.miapp.models.Patient;
import com.example.miapp.models.Doctor;
import com.example.miapp.repository.MedicalRecordRepository;
import com.example.miapp.repository.PatientRepository;
import com.example.miapp.repository.DoctorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing medical records.
 */
@Service
public class MedicalRecordService {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    /**
     * Retrieves all medical records from the database.
     *
     * @return List of {@link MedicalRecordDto} containing medical record details.
     */
    public List<MedicalRecordDto> getAllMedicalRecords() {
        return medicalRecordRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a medical record by its ID.
     *
     * @param id The ID of the medical record.
     * @return {@link MedicalRecordDto} containing medical record details.
     * @throws EntityNotFoundException If no medical record is found with the given ID.
     */
    public MedicalRecordDto getMedicalRecordById(Long id) {
        return medicalRecordRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new EntityNotFoundException("Medical record not found with ID: " + id));
    }

    /**
     * Saves a new medical record in the database.
     *
     * @param dto The {@link MedicalRecordDto} containing medical record details.
     * @return The saved {@link MedicalRecordDto}.
     * @throws EntityNotFoundException If the associated patient or doctor does not exist.
     */
    @Transactional
    public MedicalRecordDto saveMedicalRecord(MedicalRecordDto dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with ID: " + dto.getPatientId()));

        Doctor doctor = doctorRepository.findById(dto.getResponsibleDoctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with ID: " + dto.getResponsibleDoctorId()));

        MedicalRecord medicalRecord = MedicalRecord.builder()
                .patient(patient)
                .responsibleDoctor(doctor)
                .diagnosis(dto.getDiagnosis())
                .treatment(dto.getTreatment())
                .entryDate(dto.getEntryDate())
                .build();

        return convertToDto(medicalRecordRepository.save(medicalRecord));
    }

    /**
     * Updates an existing medical record.
     *
     * @param id  The ID of the medical record.
     * @param dto The updated {@link MedicalRecordDto} data.
     * @return The updated {@link MedicalRecordDto}.
     * @throws EntityNotFoundException If the medical record, patient, or doctor does not exist.
     */
    @Transactional
    public MedicalRecordDto updateMedicalRecord(Long id, MedicalRecordDto dto) {
        MedicalRecord existingRecord = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medical record not found with ID: " + id));

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with ID: " + dto.getPatientId()));

        Doctor doctor = doctorRepository.findById(dto.getResponsibleDoctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with ID: " + dto.getResponsibleDoctorId()));

        MedicalRecord updatedRecord = existingRecord.toBuilder()
                .patient(patient)
                .responsibleDoctor(doctor)
                .diagnosis(dto.getDiagnosis())
                .treatment(dto.getTreatment())
                .entryDate(dto.getEntryDate())
                .build();

        return convertToDto(medicalRecordRepository.save(updatedRecord));
    }

    /**
     * Deletes a medical record by its ID.
     *
     * @param id The ID of the medical record to be deleted.
     * @throws EntityNotFoundException If the medical record does not exist.
     */
    @Transactional
    public void deleteMedicalRecord(Long id) {
        if (!medicalRecordRepository.existsById(id)) {
            throw new EntityNotFoundException("Medical record not found with ID: " + id);
        }
        medicalRecordRepository.deleteById(id);
    }

    /**
     * Converts a {@link MedicalRecord} entity to a {@link MedicalRecordDto}.
     *
     * @param medicalRecord The entity to convert.
     * @return The corresponding {@link MedicalRecordDto}.
     */
    private MedicalRecordDto convertToDto(MedicalRecord medicalRecord) {
        return MedicalRecordDto.builder()
                .id(medicalRecord.getId())
                .patientId(medicalRecord.getPatient().getId())
                .responsibleDoctorId(medicalRecord.getResponsibleDoctor().getId())
                .diagnosis(medicalRecord.getDiagnosis())
                .treatment(medicalRecord.getTreatment())
                .entryDate(medicalRecord.getEntryDate())
                .build();
    }
}
