package com.example.miapp.service.medicalrecord;

import com.example.miapp.dto.medicalrecord.MedicalRecordDto;
import com.example.miapp.dto.medicalrecord.MedicalRecordEntryDto;
import com.example.miapp.dto.medicalrecord.CreateMedicalEntryRequest;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.MedicalRecord;
import com.example.miapp.entity.MedicalRecordEntry;
import com.example.miapp.entity.Patient;
import com.example.miapp.exception.ResourceNotFoundException;
import com.example.miapp.mapper.MedicalRecordMapper;
import com.example.miapp.repository.AppointmentRepository;
import com.example.miapp.repository.DoctorRepository;
import com.example.miapp.repository.MedicalRecordRepository;
import com.example.miapp.repository.PatientRepository;
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
 * Servicio principal para la gestión de historiales médicos.
 * Orquesta las operaciones de alto nivel relacionadas con historiales médicos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordMapper medicalRecordMapper;

    /**
 * Obtiene el historial médico de un paciente por su ID.
 *
 * @param patientId ID del paciente
 * @return DTO con la información del historial médico
 * @throws ResourceNotFoundException si el historial médico no existe para el paciente
 */
@Transactional(readOnly = true)
public MedicalRecordDto getMedicalRecordByPatientId(Long patientId) {
    log.info("Obteniendo historial médico para paciente con ID: {}", patientId);
    
    // Buscar el historial médico directamente
    MedicalRecord medicalRecord = medicalRecordRepository.findByPatientId(patientId)
            .orElseThrow(() -> {
                log.error("Historial médico no encontrado para paciente con ID: {}", patientId);
                return new ResourceNotFoundException("Historial médico no encontrado para el paciente con ID: " + patientId);
            });
    
    log.debug("Historial médico encontrado con {} entradas",
            medicalRecord.getEntries() != null ? medicalRecord.getEntries().size() : 0);
    
    return medicalRecordMapper.toDto(medicalRecord);
}
    
    /**
     * Obtiene todas las entradas del historial médico de un paciente.
     *
     * @param patientId ID del paciente
     * @param pageable información de paginación
     * @return Página de DTOs con las entradas del historial
     * @throws ResourceNotFoundException si el paciente o el historial no existen
     */
    @Transactional(readOnly = true)
    public List<MedicalRecordEntryDto> getMedicalRecordEntries(Long patientId) {
        log.info("Obteniendo entradas de historial médico para paciente con ID: {}", patientId);
        
        // Buscar el historial médico
        MedicalRecord medicalRecord = medicalRecordRepository.findByPatientId(patientId)
                .orElseThrow(() -> {
                    log.error("Historial médico no encontrado para paciente con ID: {}", patientId);
                    return new ResourceNotFoundException("Historial médico no encontrado para el paciente");
                });
        
        // Mapear las entradas a DTOs
        List<MedicalRecordEntryDto> entries = 
                medicalRecordMapper.toEntryDtoList(medicalRecord.getEntries());
        
        log.debug("Encontradas {} entradas de historial médico", entries.size());
        
        return entries;
    }
    
    /**
     * Crea una nueva entrada en el historial médico de un paciente.
     *
     * @param patientId ID del paciente
     * @param request DTO con la información de la nueva entrada
     * @return DTO con la información de la entrada creada
     * @throws ResourceNotFoundException si el paciente, doctor o cita no existen
     */
    @Transactional
    public MedicalRecordEntryDto addMedicalRecordEntry(Long patientId, CreateMedicalEntryRequest request) {
        log.info("Añadiendo nueva entrada de tipo {} al historial médico del paciente {}", 
                request.getType(), patientId);
        
        // Verificar que el paciente existe y obtener su historial médico
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> {
                    log.error("Paciente no encontrado con ID: {}", patientId);
                    return new ResourceNotFoundException("Paciente no encontrado con ID: " + patientId);
                });
        
        // Obtener o crear el historial médico si no existe
        MedicalRecord medicalRecord = medicalRecordRepository.findByPatientId(patientId)
                .orElseGet(() -> {
                    log.info("Creando nuevo historial médico para paciente {}", patientId);
                    MedicalRecord newRecord = MedicalRecord.builder()
                            .patient(patient)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return medicalRecordRepository.save(newRecord);
                });
        
        // Verificar que el doctor existe
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> {
                    log.error("Doctor no encontrado con ID: {}", request.getDoctorId());
                    return new ResourceNotFoundException("Doctor no encontrado con ID: " + request.getDoctorId());
                });
        
        // Crear la nueva entrada
        MedicalRecordEntry entry = medicalRecordMapper.toEntryEntity(request);
        entry.setMedicalRecord(medicalRecord);
        entry.setDoctor(doctor);
        entry.setEntryDate(LocalDateTime.now());
        
        // Si hay una cita asociada, verificar que existe
        if (request.getAppointmentId() != null) {
            entry.setAppointment(appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> {
                        log.error("Cita no encontrada con ID: {}", request.getAppointmentId());
                        return new ResourceNotFoundException("Cita no encontrada con ID: " + request.getAppointmentId());
                    }));
        }
        
        // Agregar la entrada al historial
        medicalRecord.addEntry(entry);
        medicalRecordRepository.save(medicalRecord);
        
        log.info("Entrada añadida exitosamente al historial médico del paciente {}", patientId);
        
        return medicalRecordMapper.toEntryDto(entry);
    }
    
    /**
     * Actualiza la información básica del historial médico.
     *
     * @param recordId ID del historial médico
     * @param allergies nuevas alergias (nulo para no cambiar)
     * @param chronicConditions nuevas condiciones crónicas (nulo para no cambiar)
     * @param currentMedications nuevos medicamentos actuales (nulo para no cambiar)
     * @param familyHistory nuevo historial familiar (nulo para no cambiar)
     * @param surgicalHistory nuevo historial quirúrgico (nulo para no cambiar)
     * @param notes nuevas notas (nulo para no cambiar)
     * @return DTO con la información actualizada del historial
     * @throws ResourceNotFoundException si el historial no existe
     */
    @Transactional
    public MedicalRecordDto updateMedicalRecordInfo(
            Long recordId,
            String allergies,
            String chronicConditions,
            String currentMedications,
            String familyHistory,
            String surgicalHistory,
            String notes) {
        
        log.info("Actualizando información básica del historial médico con ID: {}", recordId);
        
        // Verificar que el historial existe
        MedicalRecord medicalRecord = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> {
                    log.error("Historial médico no encontrado con ID: {}", recordId);
                    return new ResourceNotFoundException("Historial médico no encontrado con ID: " + recordId);
                });
        
        // Actualizar solo los campos proporcionados
        boolean updated = false;
        
        if (allergies != null) {
            medicalRecord.setAllergies(allergies);
            updated = true;
        }
        
        if (chronicConditions != null) {
            medicalRecord.setChronicConditions(chronicConditions);
            updated = true;
        }
        
        if (currentMedications != null) {
            medicalRecord.setCurrentMedications(currentMedications);
            updated = true;
        }
        
        if (familyHistory != null) {
            medicalRecord.setFamilyHistory(familyHistory);
            updated = true;
        }
        
        if (surgicalHistory != null) {
            medicalRecord.setSurgicalHistory(surgicalHistory);
            updated = true;
        }
        
        if (notes != null) {
            medicalRecord.setNotes(notes);
            updated = true;
        }
        
        // Si se actualizó algún campo, guardar los cambios
        if (updated) {
            medicalRecord.setUpdatedAt(LocalDateTime.now());
            medicalRecordRepository.save(medicalRecord);
            log.info("Información del historial médico actualizada exitosamente");
        } else {
            log.info("No se proporcionaron campos para actualizar");
        }
        
        return medicalRecordMapper.toDto(medicalRecord);
    }
    
    /**
     * Busca historiales médicos por condición crónica.
     *
     * @param conditionPattern patrón de búsqueda para condiciones crónicas
     * @param pageable información de paginación
     * @return Página de DTOs con los historiales médicos que coinciden
     */
    @Transactional(readOnly = true)
    public Page<MedicalRecordDto> findByChronicCondition(String conditionPattern, Pageable pageable) {
        log.info("Buscando historiales médicos con condición crónica que contenga: {}", conditionPattern);
        
        Page<MedicalRecord> records = medicalRecordRepository.findByChronicConditionsContaining(
                conditionPattern, pageable);
        
        return records.map(medicalRecordMapper::toDto);
    }
    
    /**
     * Busca historiales médicos por alergia.
     *
     * @param allergyPattern patrón de búsqueda para alergias
     * @param pageable información de paginación
     * @return Página de DTOs con los historiales médicos que coinciden
     */
    @Transactional(readOnly = true)
    public Page<MedicalRecordDto> findByAllergy(String allergyPattern, Pageable pageable) {
        log.info("Buscando historiales médicos con alergia que contenga: {}", allergyPattern);
        
        Page<MedicalRecord> records = medicalRecordRepository.findByAllergiesContaining(
                allergyPattern, pageable);
        
        return records.map(medicalRecordMapper::toDto);
    }
    
    /**
     * Busca historiales médicos por medicación actual.
     *
     * @param medicationPattern patrón de búsqueda para medicaciones
     * @param pageable información de paginación
     * @return Página de DTOs con los historiales médicos que coinciden
     */
    @Transactional(readOnly = true)
    public Page<MedicalRecordDto> findByCurrentMedication(String medicationPattern, Pageable pageable) {
        log.info("Buscando historiales médicos con medicación que contenga: {}", medicationPattern);
        
        Page<MedicalRecord> records = medicalRecordRepository.findByCurrentMedicationsContaining(
                medicationPattern, pageable);
        
        return records.map(medicalRecordMapper::toDto);
    }
    
    /**
     * Obtiene un informe resumido del historial médico para un paciente.
     * Este método proporciona una versión simplificada del historial,
     * útil para vistas rápidas o resúmenes.
     *
     * @param patientId ID del paciente
     * @return DTO con la información resumida del historial médico
     * @throws ResourceNotFoundException si el paciente o el historial no existen
     */
    @Transactional(readOnly = true)
    public MedicalRecordDto getMedicalRecordSummary(Long patientId) {
        log.info("Obteniendo resumen de historial médico para paciente con ID: {}", patientId);
        
        // Buscar el historial médico
        MedicalRecord medicalRecord = medicalRecordRepository.findByPatientId(patientId)
                .orElseThrow(() -> {
                    log.error("Historial médico no encontrado para paciente con ID: {}", patientId);
                    return new ResourceNotFoundException("Historial médico no encontrado para el paciente");
                });
        
        // Crear un DTO con la información resumida
        MedicalRecordDto summaryDto = medicalRecordMapper.toDto(medicalRecord);
        
        // Se podría implementar lógica adicional para resumir o filtrar información
        // Por ejemplo, incluir solo las entradas más recientes o relevantes
        
        log.debug("Resumen de historial médico generado exitosamente");
        
        return summaryDto;
    }
    
    /**
     * Actualiza la visibilidad de una entrada del historial médico para el paciente.
     * Algunas entradas pueden no ser apropiadas para que el paciente las vea directamente.
     *
     * @param entryId ID de la entrada del historial
     * @param visibleToPatient si la entrada debe ser visible para el paciente
     * @throws ResourceNotFoundException si la entrada no existe
     */
    @Transactional
    public void updateEntryVisibility(Long entryId, boolean visibleToPatient) {
        log.info("Actualizando visibilidad de entrada de historial médico ID {}: {}", 
                entryId, visibleToPatient ? "visible" : "no visible");
        
        // Buscar la entrada en el historial
        MedicalRecord medicalRecord = medicalRecordRepository.findAll().stream()
                .filter(record -> record.getEntries().stream()
                        .anyMatch(entry -> entry.getId().equals(entryId)))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Entrada de historial médico no encontrada con ID: {}", entryId);
                    return new ResourceNotFoundException("Entrada de historial médico no encontrada");
                });
        
        // Encontrar la entrada específica
        Optional<MedicalRecordEntry> entryOpt = medicalRecord.getEntries().stream()
                .filter(entry -> entry.getId().equals(entryId))
                .findFirst();
        
        if (entryOpt.isPresent()) {
            MedicalRecordEntry entry = entryOpt.get();
            entry.setVisibleToPatient(visibleToPatient);
            medicalRecordRepository.save(medicalRecord);
            log.info("Visibilidad de entrada actualizada exitosamente");
        } else {
            log.error("Entrada de historial médico no encontrada con ID: {}", entryId);
            throw new ResourceNotFoundException("Entrada de historial médico no encontrada");
        }
    }
    
    /**
     * Obtiene historiales médicos actualizados recientemente.
     *
     * @param days número de días hacia atrás para considerar
     * @param pageable información de paginación
     * @return Página de DTOs con los historiales médicos actualizados recientemente
     */
    @Transactional(readOnly = true)
    public Page<MedicalRecordDto> getRecentlyUpdatedRecords(int days, Pageable pageable) {
        log.info("Obteniendo historiales médicos actualizados en los últimos {} días", days);
        
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();
        
        Page<MedicalRecord> records = medicalRecordRepository.findByUpdatedAtBetween(
                startDate, endDate, pageable);
        
        return records.map(medicalRecordMapper::toDto);
    }
}