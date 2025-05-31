package com.example.miapp.service.prescription;

import com.example.miapp.dto.prescription.CreatePrescriptionItemRequest;
import com.example.miapp.dto.prescription.CreatePrescriptionRequest;
import com.example.miapp.dto.prescription.PrescriptionDto;
import com.example.miapp.dto.prescription.PrescriptionItemDto;
import com.example.miapp.entity.Appointment;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.Patient;
import com.example.miapp.entity.Prescription;
import com.example.miapp.entity.PrescriptionItem;
import com.example.miapp.entity.Prescription.PrescriptionStatus;
import com.example.miapp.exception.ResourceNotFoundException;
import com.example.miapp.exception.ValidationException;
import com.example.miapp.mapper.PrescriptionMapper;
import com.example.miapp.repository.AppointmentRepository;
import com.example.miapp.repository.DoctorRepository;
import com.example.miapp.repository.PatientRepository;
import com.example.miapp.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de prescripciones médicas.
 * Maneja la creación, consulta y actualización de recetas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionMapper prescriptionMapper;

    /**
     * Crea una nueva prescripción médica.
     *
     * @param request DTO con la información de la prescripción
     * @return DTO con la información de la prescripción creada
     * @throws ResourceNotFoundException si no se encuentra el paciente, doctor o cita
     * @throws ValidationException si los datos no son válidos
     */
    @Transactional
    public PrescriptionDto createPrescription(CreatePrescriptionRequest request) {
        log.info("Creando nueva prescripción para paciente: {} por doctor: {}", 
                request.getPatientId(), request.getDoctorId());
        
        // Validar que haya al menos un medicamento
        if (request.getMedicationItems() == null || request.getMedicationItems().isEmpty()) {
            log.warn("Intento de crear prescripción sin medicamentos");
            throw new ValidationException("La prescripción debe tener al menos un medicamento");
        }
        
        // Verificar que el paciente existe
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> {
                    log.error("Paciente no encontrado con ID: {}", request.getPatientId());
                    return new ResourceNotFoundException("Paciente no encontrado con ID: " + request.getPatientId());
                });
        
        // Verificar que el doctor existe
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> {
                    log.error("Doctor no encontrado con ID: {}", request.getDoctorId());
                    return new ResourceNotFoundException("Doctor no encontrado con ID: " + request.getDoctorId());
                });
        
        // Crear la prescripción
        Prescription prescription = prescriptionMapper.toEntity(request);
        prescription.setDoctor(doctor);
        prescription.setPatient(patient);
        prescription.setIssueDate(LocalDateTime.now());
        prescription.setStatus(PrescriptionStatus.ACTIVE);
        
        // Si hay una cita asociada, verificar que existe
        if (request.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> {
                        log.error("Cita no encontrada con ID: {}", request.getAppointmentId());
                        return new ResourceNotFoundException("Cita no encontrada con ID: " + request.getAppointmentId());
                    });
            prescription.setAppointment(appointment);
        }
        
        // Crear y añadir los medicamentos
        List<PrescriptionItem> medicationItems = new ArrayList<>();
        for (CreatePrescriptionItemRequest itemRequest : request.getMedicationItems()) {
            PrescriptionItem item = prescriptionMapper.toItemEntity(itemRequest);
            item.setPrescription(prescription);
            medicationItems.add(item);
        }
        prescription.setMedicationItems(medicationItems);
        
        // Guardar la prescripción
        prescription = prescriptionRepository.save(prescription);
        
        log.info("Prescripción creada exitosamente con ID: {}", prescription.getId());
        
        return prescriptionMapper.toDto(prescription);
    }
    
    /**
     * Obtiene todas las prescripciones para un paciente.
     *
     * @param patientId ID del paciente
     * @param pageable información de paginación
     * @return Página de DTOs con las prescripciones del paciente
     * @throws ResourceNotFoundException si no se encuentra el paciente
     */
    @Transactional(readOnly = true)
    public Page<PrescriptionDto> getPrescriptionsByPatient(Long patientId, Pageable pageable) {
        log.info("Obteniendo prescripciones para paciente con ID: {}", patientId);
        
        // Verificar que el paciente existe
        if (!patientRepository.existsById(patientId)) {
            log.error("Paciente no encontrado con ID: {}", patientId);
            throw new ResourceNotFoundException("Paciente no encontrado con ID: " + patientId);
        }
        
        // Obtener las prescripciones
        Page<Prescription> prescriptions = prescriptionRepository.findByPatientIdOrderByIssueDateDesc(
                patientId, pageable);
        
        log.debug("Encontradas {} prescripciones para el paciente", prescriptions.getTotalElements());
        
        // Mapear a DTOs
        return prescriptions.map(prescriptionMapper::toDto);
    }
    
    /**
     * Obtiene todas las prescripciones creadas por un doctor.
     *
     * @param doctorId ID del doctor
     * @param pageable información de paginación
     * @return Página de DTOs con las prescripciones del doctor
     * @throws ResourceNotFoundException si no se encuentra el doctor
     */
    @Transactional(readOnly = true)
    public Page<PrescriptionDto> getPrescriptionsByDoctor(Long doctorId, Pageable pageable) {
        log.info("Obteniendo prescripciones para doctor con ID: {}", doctorId);
        
        // Verificar que el doctor existe
        if (!doctorRepository.existsById(doctorId)) {
            log.error("Doctor no encontrado con ID: {}", doctorId);
            throw new ResourceNotFoundException("Doctor no encontrado con ID: " + doctorId);
        }
        
        // Obtener las prescripciones
        Page<Prescription> prescriptions = prescriptionRepository.findByDoctorId(doctorId, pageable);
        
        log.debug("Encontradas {} prescripciones para el doctor", prescriptions.getTotalElements());
        
        // Mapear a DTOs
        return prescriptions.map(prescriptionMapper::toDto);
    }
    
    /**
     * Obtiene una prescripción por su ID.
     *
     * @param prescriptionId ID de la prescripción
     * @return DTO con la información de la prescripción
     * @throws ResourceNotFoundException si no se encuentra la prescripción
     */
    @Transactional(readOnly = true)
    public PrescriptionDto getPrescriptionById(Long prescriptionId) {
        log.info("Obteniendo prescripción con ID: {}", prescriptionId);
        
        // Buscar la prescripción
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> {
                    log.error("Prescripción no encontrada con ID: {}", prescriptionId);
                    return new ResourceNotFoundException("Prescripción no encontrada con ID: " + prescriptionId);
                });
        
        return prescriptionMapper.toDto(prescription);
    }
    
    /**
     * Obtiene las prescripciones para una cita específica.
     *
     * @param appointmentId ID de la cita
     * @return DTO con la información de la prescripción, o null si no hay
     * @throws ResourceNotFoundException si no se encuentra la cita
     */
    @Transactional(readOnly = true)
    public PrescriptionDto getPrescriptionByAppointment(Long appointmentId) {
        log.info("Obteniendo prescripción para cita con ID: {}", appointmentId);
        
        // Verificar que la cita existe
        if (!appointmentRepository.existsById(appointmentId)) {
            log.error("Cita no encontrada con ID: {}", appointmentId);
            throw new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId);
        }
        
        // Buscar la prescripción
        return prescriptionRepository.findByAppointmentId(appointmentId)
                .map(prescriptionMapper::toDto)
                .orElse(null);
    }
    
    /**
     * Actualiza el estado de una prescripción.
     *
     * @param prescriptionId ID de la prescripción
     * @param status nuevo estado
     * @return DTO con la información actualizada de la prescripción
     * @throws ResourceNotFoundException si no se encuentra la prescripción
     */
    @Transactional
    public PrescriptionDto updatePrescriptionStatus(Long prescriptionId, PrescriptionStatus status) {
        log.info("Actualizando estado de prescripción {} a: {}", prescriptionId, status);
        
        // Buscar la prescripción
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> {
                    log.error("Prescripción no encontrada con ID: {}", prescriptionId);
                    return new ResourceNotFoundException("Prescripción no encontrada con ID: " + prescriptionId);
                });
        
        // Actualizar el estado
        prescription.setStatus(status);
        prescription = prescriptionRepository.save(prescription);
        
        log.info("Estado de prescripción actualizado exitosamente");
        
        return prescriptionMapper.toDto(prescription);
    }
    
    /**
     * Marca una prescripción como impresa.
     *
     * @param prescriptionId ID de la prescripción
     * @return DTO con la información actualizada de la prescripción
     * @throws ResourceNotFoundException si no se encuentra la prescripción
     */
    @Transactional
    public PrescriptionDto markPrescriptionAsPrinted(Long prescriptionId) {
        log.info("Marcando prescripción {} como impresa", prescriptionId);
        
        // Buscar la prescripción
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> {
                    log.error("Prescripción no encontrada con ID: {}", prescriptionId);
                    return new ResourceNotFoundException("Prescripción no encontrada con ID: " + prescriptionId);
                });
        
        // Marcar como impresa
        prescription.setPrinted(true);
        prescription.setPrintDate(LocalDateTime.now());
        prescription = prescriptionRepository.save(prescription);
        
        log.info("Prescripción marcada como impresa exitosamente");
        
        return prescriptionMapper.toDto(prescription);
    }
    
    /**
     * Procesa una recarga de medicamento.
     *
     * @param prescriptionId ID de la prescripción
     * @param itemId ID del medicamento
     * @return DTO con la información del medicamento actualizado
     * @throws ResourceNotFoundException si no se encuentra la prescripción o medicamento
     * @throws ValidationException si el medicamento no es recargable o ha agotado recargas
     */
    @Transactional
    public PrescriptionItemDto processRefill(Long prescriptionId, Long itemId) {
        log.info("Procesando recarga para medicamento {} de prescripción {}", itemId, prescriptionId);
        
        // Buscar la prescripción
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> {
                    log.error("Prescripción no encontrada con ID: {}", prescriptionId);
                    return new ResourceNotFoundException("Prescripción no encontrada con ID: " + prescriptionId);
                });
        
        // Verificar que la prescripción está activa
        if (prescription.getStatus() != PrescriptionStatus.ACTIVE) {
            log.warn("Intento de recargar medicamento de prescripción no activa: {}", prescriptionId);
            throw new ValidationException("Solo se pueden procesar recargas de prescripciones activas");
        }
        
        // Buscar el medicamento
        PrescriptionItem item = prescription.getMedicationItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Medicamento no encontrado con ID: {} en prescripción: {}", itemId, prescriptionId);
                    return new ResourceNotFoundException("Medicamento no encontrado en la prescripción");
                });
        
        // Verificar que el medicamento es recargable
        if (!item.isRefillable()) {
            log.warn("Intento de recargar medicamento no recargable: {}", itemId);
            throw new ValidationException("Este medicamento no permite recargas");
        }
        
        // Verificar que quedan recargas disponibles
        if (item.getRefillsUsed() >= item.getRefillsAllowed()) {
            log.warn("Intento de recargar medicamento sin recargas disponibles: {}", itemId);
            throw new ValidationException("Se han agotado las recargas disponibles para este medicamento");
        }
        
        // Incrementar contador de recargas
        item.setRefillsUsed(item.getRefillsUsed() + 1);
        prescription = prescriptionRepository.save(prescription);
        
        // Obtener el item actualizado
        PrescriptionItem updatedItem = prescription.getMedicationItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(); // No debería ocurrir
        
        log.info("Recarga procesada exitosamente. Recargas utilizadas: {}/{}", 
                updatedItem.getRefillsUsed(), updatedItem.getRefillsAllowed());
        
        return prescriptionMapper.toItemDto(updatedItem);
    }
    
    /**
     * Busca prescripciones por nombre de medicamento.
     *
     * @param medicationName nombre del medicamento a buscar
     * @param pageable información de paginación
     * @return Página de DTOs con las prescripciones que contienen el medicamento
     */
    @Transactional(readOnly = true)
    public Page<PrescriptionDto> findByMedicationName(String medicationName, Pageable pageable) {
        log.info("Buscando prescripciones con medicamento: {}", medicationName);
        
        Page<Prescription> prescriptions = prescriptionRepository.findByMedicationName(medicationName, pageable);
        
        log.debug("Encontradas {} prescripciones con el medicamento", prescriptions.getTotalElements());
        
        return prescriptions.map(prescriptionMapper::toDto);
    }
    
    /**
     * Busca prescripciones por diagnóstico.
     *
     * @param diagnosisPattern patrón a buscar en el diagnóstico
     * @param pageable información de paginación
     * @return Página de DTOs con las prescripciones que coinciden con el diagnóstico
     */
    @Transactional(readOnly = true)
    public Page<PrescriptionDto> findByDiagnosis(String diagnosisPattern, Pageable pageable) {
        log.info("Buscando prescripciones con diagnóstico que contenga: {}", diagnosisPattern);
        
        Page<Prescription> prescriptions = prescriptionRepository.findByDiagnosisContaining(diagnosisPattern, pageable);
        
        log.debug("Encontradas {} prescripciones con el diagnóstico", prescriptions.getTotalElements());
        
        return prescriptions.map(prescriptionMapper::toDto);
    }
    
    /**
     * Encuentra los medicamentos más prescritos.
     *
     * @param limit número máximo de resultados
     * @return Lista de nombres de medicamentos y su conteo
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMostPrescribedMedications(int limit) {
        log.info("Obteniendo los {} medicamentos más prescritos", limit);
        
        return prescriptionRepository.findMostPrescribedMedications(Pageable.ofSize(limit))
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Verifica posibles interacciones medicamentosas para un paciente.
     * Este método busca todas las prescripciones activas del paciente
     * y verifica si hay posibles interacciones entre los medicamentos.
     *
     * @param patientId ID del paciente
     * @param newMedication nuevo medicamento a verificar (opcional)
     * @return Lista de posibles interacciones
     * @throws ResourceNotFoundException si no se encuentra el paciente
     */
    @Transactional(readOnly = true)
    public List<String> checkDrugInteractions(Long patientId, String newMedication) {
        log.info("Verificando interacciones medicamentosas para paciente: {}", patientId);
        
        // Verificar que el paciente existe
        if (!patientRepository.existsById(patientId)) {
            log.error("Paciente no encontrado con ID: {}", patientId);
            throw new ResourceNotFoundException("Paciente no encontrado con ID: " + patientId);
        }
        
        // Obtener medicamentos actuales
        List<String> currentMedications = new ArrayList<>();
        
        // Buscar en prescripciones activas
        Page<Prescription> activePrescriptions = prescriptionRepository.findByPatientIdAndStatus(
                patientId, PrescriptionStatus.ACTIVE, Pageable.unpaged());
        
        activePrescriptions.forEach(prescription -> {
            prescription.getMedicationItems().forEach(item -> {
                currentMedications.add(normalizeMedicationName(item.getMedicationName()));
            });
        });
        
        // Añadir el nuevo medicamento si se proporciona
        if (newMedication != null && !newMedication.isEmpty()) {
            currentMedications.add(normalizeMedicationName(newMedication));
        }
        
        log.debug("Medicamentos actuales del paciente: {}", currentMedications);
        
        // Lista para almacenar las interacciones encontradas
        List<String> interactions = new ArrayList<>();
        
        // Si hay menos de 2 medicamentos, no puede haber interacciones
        if (currentMedications.size() < 2) {
            log.info("El paciente tiene menos de 2 medicamentos, no hay interacciones posibles");
            return interactions;
        }
        
        // Mapa de interacciones conocidas (medicamento -> lista de [medicamento, descripción])
        Map<String, List<Pair<String, String>>> knownInteractions = buildDrugInteractionsDatabase();
        
        // Verificar cada par de medicamentos para posibles interacciones
        for (int i = 0; i < currentMedications.size(); i++) {
            String med1 = currentMedications.get(i);
            
            // Buscar interacciones conocidas para este medicamento
            if (knownInteractions.containsKey(med1)) {
                List<Pair<String, String>> potentialInteractions = knownInteractions.get(med1);
                
                // Verificar si alguno de los otros medicamentos interactúa con este
                for (int j = i + 1; j < currentMedications.size(); j++) {
                    String med2 = currentMedications.get(j);
                    
                    // Buscar si hay una interacción conocida entre med1 y med2
                    for (Pair<String, String> interaction : potentialInteractions) {
                        if (med2.equals(interaction.getFirst())) {
                            // Encontrada interacción
                            String description = interaction.getSecond();
                            String interactionMessage = String.format("%s + %s: %s", 
                                    capitalize(med1), capitalize(med2), description);
                            
                            interactions.add(interactionMessage);
                            log.debug("Interacción encontrada: {}", interactionMessage);
                        }
                    }
                }
            }
        }
        
        // Verificar también interacciones inversas (med2 -> med1) que podrían no estar en la lista anterior
        for (int i = 0; i < currentMedications.size(); i++) {
            String med1 = currentMedications.get(i);
            
            for (int j = i + 1; j < currentMedications.size(); j++) {
                String med2 = currentMedications.get(j);
                
                // Verificar si med2 tiene interacciones con med1
                if (knownInteractions.containsKey(med2)) {
                    for (Pair<String, String> interaction : knownInteractions.get(med2)) {
                        if (med1.equals(interaction.getFirst())) {
                            // Asegurarse de no duplicar interacciones ya registradas
                            String interactionMessage = String.format("%s + %s: %s", 
                                    capitalize(med2), capitalize(med1), interaction.getSecond());
                            
                            // Verificar forma inversa para evitar duplicados
                            String reverseCheck = String.format("%s + %s", capitalize(med1), capitalize(med2));
                            boolean isDuplicate = interactions.stream()
                                    .anyMatch(existing -> existing.startsWith(reverseCheck));
                            
                            if (!isDuplicate) {
                                interactions.add(interactionMessage);
                                log.debug("Interacción encontrada (dirección inversa): {}", interactionMessage);
                            }
                        }
                    }
                }
            }
        }
        
        // Verificar interacciones por grupos de medicamentos
        checkDrugGroupInteractions(currentMedications, interactions);
        
        log.info("Verificación de interacciones completada. Encontradas: {}", interactions.size());
        
        return interactions;
    }
    
    /**
     * Busca prescripciones que necesiten renovación pronto.
     * Esto puede ayudar a los médicos a gestionar renovaciones proactivamente.
     *
     * @param daysThreshold número de días para considerar "pronto"
     * @param pageable información de paginación
     * @return Página de DTOs con prescripciones que necesitan renovación
     */
    @Transactional(readOnly = true)
    public Page<PrescriptionDto> findPrescriptionsNeedingRenewal(int daysThreshold, Pageable pageable) {
        log.info("Buscando prescripciones que necesitan renovación en los próximos {} días", daysThreshold);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30 - daysThreshold);
        
        Page<Prescription> prescriptions = prescriptionRepository.findPrescriptionsNeedingRenewal(
                cutoffDate, pageable);
        
        log.debug("Encontradas {} prescripciones que necesitan renovación", prescriptions.getTotalElements());
        
        return prescriptions.map(prescriptionMapper::toDto);
    }
    
    /**
     * Clase interna para representar pares de valores.
     */
    private static class Pair<F, S> {
        private final F first;
        private final S second;
        
        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
        
        public F getFirst() {
            return first;
        }
        
        public S getSecond() {
            return second;
        }
    }
    
    /**
     * Construye una base de datos en memoria de interacciones medicamentosas conocidas.
     * En una implementación real, esta información vendría de una base de datos o servicio externo.
     *
     * @return Mapa de medicamentos y sus interacciones conocidas
     */
    private Map<String, List<Pair<String, String>>> buildDrugInteractionsDatabase() {
        Map<String, List<Pair<String, String>>> interactions = new HashMap<>();
        
        // Interacciones con Warfarina
        List<Pair<String, String>> warfarinInteractions = new ArrayList<>();
        warfarinInteractions.add(new Pair<>("aspirina", "Aumenta el riesgo de sangrado"));
        warfarinInteractions.add(new Pair<>("ibuprofeno", "Aumenta el riesgo de sangrado gastrointestinal"));
        warfarinInteractions.add(new Pair<>("naproxeno", "Aumenta el riesgo de sangrado gastrointestinal"));
        warfarinInteractions.add(new Pair<>("diclofenaco", "Aumenta el riesgo de sangrado gastrointestinal"));
        warfarinInteractions.add(new Pair<>("amiodarona", "Potencia el efecto anticoagulante, aumentando riesgo de sangrado"));
        warfarinInteractions.add(new Pair<>("levotiroxina", "Puede aumentar los efectos anticoagulantes"));
        interactions.put("warfarina", warfarinInteractions);
        
        // Interacciones con Estatinas
        List<Pair<String, String>> simvastatinInteractions = new ArrayList<>();
        simvastatinInteractions.add(new Pair<>("eritromicina", "Aumenta el riesgo de miopatía y rabdomiólisis"));
        simvastatinInteractions.add(new Pair<>("claritromicina", "Aumenta el riesgo de miopatía y rabdomiólisis"));
        simvastatinInteractions.add(new Pair<>("itraconazol", "Aumenta el riesgo de miopatía y rabdomiólisis"));
        simvastatinInteractions.add(new Pair<>("ketoconazol", "Aumenta el riesgo de miopatía y rabdomiólisis"));
        simvastatinInteractions.add(new Pair<>("gemfibrozilo", "Aumenta el riesgo de miopatía y rabdomiólisis"));
        interactions.put("simvastatina", simvastatinInteractions);
        
        // Interacciones similares para atorvastatina
        List<Pair<String, String>> atorvastatinInteractions = new ArrayList<>();
        atorvastatinInteractions.add(new Pair<>("eritromicina", "Puede aumentar el riesgo de miopatía"));
        atorvastatinInteractions.add(new Pair<>("claritromicina", "Puede aumentar el riesgo de miopatía"));
        atorvastatinInteractions.add(new Pair<>("itraconazol", "Puede aumentar el riesgo de miopatía"));
        atorvastatinInteractions.add(new Pair<>("ketoconazol", "Puede aumentar el riesgo de miopatía"));
        interactions.put("atorvastatina", atorvastatinInteractions);
        
        // Interacciones con ISRS (antidepresivos)
        List<Pair<String, String>> fluoxetineInteractions = new ArrayList<>();
        fluoxetineInteractions.add(new Pair<>("tramadol", "Riesgo de síndrome serotoninérgico"));
        fluoxetineInteractions.add(new Pair<>("sumatriptan", "Riesgo de síndrome serotoninérgico"));
        fluoxetineInteractions.add(new Pair<>("moclobemida", "Riesgo grave de síndrome serotoninérgico"));
        fluoxetineInteractions.add(new Pair<>("litio", "Aumento de efectos serotoninérgicos"));
        interactions.put("fluoxetina", fluoxetineInteractions);
        
        // Interacciones similares para otros ISRS
        interactions.put("sertralina", fluoxetineInteractions);
        interactions.put("paroxetina", fluoxetineInteractions);
        
        // Interacciones con Inhibidores de la ECA
        List<Pair<String, String>> enalaprilInteractions = new ArrayList<>();
        enalaprilInteractions.add(new Pair<>("espironolactona", "Riesgo de hiperpotasemia"));
        enalaprilInteractions.add(new Pair<>("suplementos de potasio", "Riesgo de hiperpotasemia"));
        enalaprilInteractions.add(new Pair<>("indometacina", "Reduce el efecto antihipertensivo"));
        enalaprilInteractions.add(new Pair<>("litio", "Aumento de los niveles de litio"));
        interactions.put("enalapril", enalaprilInteractions);
        
        // Interacciones similares para otros IECAs
        interactions.put("lisinopril", enalaprilInteractions);
        interactions.put("ramipril", enalaprilInteractions);
        
        // Interacciones con Digoxina
        List<Pair<String, String>> digoxinInteractions = new ArrayList<>();
        digoxinInteractions.add(new Pair<>("amiodarona", "Aumenta los niveles de digoxina, riesgo de toxicidad"));
        digoxinInteractions.add(new Pair<>("verapamilo", "Aumenta los niveles de digoxina, riesgo de toxicidad"));
        digoxinInteractions.add(new Pair<>("espironolactona", "Interfiere con la medición de digoxina sérica"));
        interactions.put("digoxina", digoxinInteractions);
        
        // Interacciones con Antidiabéticos
        List<Pair<String, String>> metforminInteractions = new ArrayList<>();
        metforminInteractions.add(new Pair<>("cimetidina", "Aumenta los niveles de metformina"));
        metforminInteractions.add(new Pair<>("contraste yodado", "Riesgo de acidosis láctica, suspender temporalmente"));
        interactions.put("metformina", metforminInteractions);
        
        // Interacciones con antibióticos
       List<Pair<String, String>> ciprofloxacinaInteractions = new ArrayList<>();
       ciprofloxacinaInteractions.add(new Pair<>("teofilina", "Aumenta los niveles de teofilina, riesgo de toxicidad"));
       ciprofloxacinaInteractions.add(new Pair<>("warfarina", "Aumenta el efecto anticoagulante"));
       ciprofloxacinaInteractions.add(new Pair<>("antiácidos", "Reduce la absorción de ciprofloxacina"));
       interactions.put("ciprofloxacina", ciprofloxacinaInteractions);
       
       // Interacciones con anticonvulsivantes
       List<Pair<String, String>> carbamacepinaInteractions = new ArrayList<>();
       carbamacepinaInteractions.add(new Pair<>("eritromicina", "Aumenta los niveles de carbamazepina"));
       carbamacepinaInteractions.add(new Pair<>("fluoxetina", "Aumenta los niveles de carbamazepina"));
       carbamacepinaInteractions.add(new Pair<>("anticonceptivos orales", "Reduce la eficacia de los anticonceptivos"));
       interactions.put("carbamazepina", carbamacepinaInteractions);
       
       // Interacciones con benzodiacepinas
       List<Pair<String, String>> diazepamInteractions = new ArrayList<>();
       diazepamInteractions.add(new Pair<>("alcohol", "Potencia la depresión del sistema nervioso central"));
       diazepamInteractions.add(new Pair<>("fluoxetina", "Aumenta los niveles de diazepam"));
       diazepamInteractions.add(new Pair<>("cimetidina", "Aumenta los niveles de diazepam"));
       interactions.put("diazepam", diazepamInteractions);
       interactions.put("alprazolam", diazepamInteractions);
       
       // Interacciones con antihipertensivos beta-bloqueantes
       List<Pair<String, String>> atenololInteractions = new ArrayList<>();
       atenololInteractions.add(new Pair<>("insulina", "Puede enmascarar síntomas de hipoglucemia"));
       atenololInteractions.add(new Pair<>("verapamilo", "Riesgo de bradicardia y bloqueo AV"));
       atenololInteractions.add(new Pair<>("diltiazem", "Riesgo de bradicardia y bloqueo AV"));
       interactions.put("atenolol", atenololInteractions);
       interactions.put("metoprolol", atenololInteractions);
       interactions.put("propranolol", atenololInteractions);
       
       // Interacciones con antiinflamatorios
       List<Pair<String, String>> ibuprofenInteractions = new ArrayList<>();
       ibuprofenInteractions.add(new Pair<>("enalapril", "Puede reducir el efecto antihipertensivo"));
       ibuprofenInteractions.add(new Pair<>("lisinopril", "Puede reducir el efecto antihipertensivo"));
       ibuprofenInteractions.add(new Pair<>("ramipril", "Puede reducir el efecto antihipertensivo"));
       ibuprofenInteractions.add(new Pair<>("losartan", "Puede reducir el efecto antihipertensivo"));
       ibuprofenInteractions.add(new Pair<>("diuréticos", "Puede reducir el efecto diurético"));
       ibuprofenInteractions.add(new Pair<>("litio", "Aumenta los niveles de litio"));
       interactions.put("ibuprofeno", ibuprofenInteractions);
       interactions.put("naproxeno", ibuprofenInteractions);
       interactions.put("diclofenaco", ibuprofenInteractions);
       
       return interactions;
   }
   
   /**
    * Verifica interacciones entre grupos de medicamentos (ej. AINE con corticosteroides).
    * 
    * @param medications Lista de medicamentos del paciente
    * @param interactions Lista donde añadir las interacciones encontradas
    */
   private void checkDrugGroupInteractions(List<String> medications, List<String> interactions) {
       // Definir grupos de medicamentos
       Set<String> nsaids = Set.of("aspirina", "ibuprofeno", "naproxeno", "diclofenaco", "meloxicam", "ketorolaco");
       Set<String> corticosteroids = Set.of("prednisona", "dexametasona", "betametasona", "hidrocortisona", "metilprednisolona");
       Set<String> anticoagulants = Set.of("warfarina", "heparina", "enoxaparina", "rivaroxaban", "apixaban", "dabigatran");
       Set<String> antiplatelets = Set.of("aspirina", "clopidogrel", "prasugrel", "ticagrelor");
       Set<String> ssris = Set.of("fluoxetina", "sertralina", "paroxetina", "escitalopram", "citalopram", "fluvoxamina");
       Set<String> maois = Set.of("fenelzina", "tranilcipromina", "isocarboxazida", "moclobemida");
       Set<String> diuretics = Set.of("hidroclorotiazida", "furosemida", "espironolactona", "indapamida", "clortalidona");
       Set<String> betaBlockers = Set.of("atenolol", "metoprolol", "propranolol", "bisoprolol", "carvedilol");
       Set<String> calciumChannelBlockers = Set.of("amlodipino", "nifedipino", "verapamilo", "diltiazem");
       Set<String> aceiArbs = Set.of("enalapril", "lisinopril", "ramipril", "losartan", "valsartan", "candesartan");
       
       // Comprobar si hay medicamentos de cada grupo
       boolean hasNsaid = medications.stream().anyMatch(nsaids::contains);
       boolean hasCorticosteroid = medications.stream().anyMatch(corticosteroids::contains);
       boolean hasAnticoagulant = medications.stream().anyMatch(anticoagulants::contains);
       boolean hasAntiplatelet = medications.stream().anyMatch(antiplatelets::contains);
       boolean hasSsri = medications.stream().anyMatch(ssris::contains);
       boolean hasMaoi = medications.stream().anyMatch(maois::contains);
       boolean hasDiuretic = medications.stream().anyMatch(diuretics::contains);
       boolean hasBetaBlocker = medications.stream().anyMatch(betaBlockers::contains);
       boolean hasCalciumChannelBlocker = medications.stream().anyMatch(calciumChannelBlockers::contains);
       boolean hasAceiArb = medications.stream().anyMatch(aceiArbs::contains);
       
       // Verificar interacciones entre grupos
       
       // AINEs + Corticosteroides
       if (hasNsaid && hasCorticosteroid) {
           interactions.add("AINEs + Corticosteroides: Aumento del riesgo de úlcera péptica y sangrado gastrointestinal");
       }
       
       // AINEs + Anticoagulantes
       if (hasNsaid && hasAnticoagulant) {
           interactions.add("AINEs + Anticoagulantes: Aumento significativo del riesgo de sangrado");
       }
       
       // Anticoagulantes + Antiplaquetarios
       if (hasAnticoagulant && hasAntiplatelet) {
           interactions.add("Anticoagulantes + Antiplaquetarios: Aumento sustancial del riesgo de sangrado mayor");
       }
       
       // ISRS + IMAOs
       if (hasSsri && hasMaoi) {
           interactions.add("ISRS + IMAOs: Riesgo potencialmente mortal de síndrome serotoninérgico");
       }
       
       // Beta-bloqueantes + Bloqueantes de canales de calcio
       if (hasBetaBlocker && hasCalciumChannelBlocker) {
           interactions.add("Beta-bloqueantes + Bloqueantes de canales de calcio: Mayor riesgo de bradicardia, hipotensión y bloqueo cardiaco");
       }
       
       // AINEs + Diuréticos
       if (hasNsaid && hasDiuretic) {
           interactions.add("AINEs + Diuréticos: Reducción de la eficacia antihipertensiva y diurética");
       }
       
       // AINEs + IECA/ARA-II
       if (hasNsaid && hasAceiArb) {
           interactions.add("AINEs + IECA/ARA-II: Reducción del efecto antihipertensivo y mayor riesgo de insuficiencia renal");
       }
       
       // Triple whammy (IECA/ARA-II + Diurético + AINE)
       if (hasAceiArb && hasDiuretic && hasNsaid) {
           interactions.add("IECA/ARA-II + Diurético + AINE: 'Triple whammy' con alto riesgo de lesión renal aguda");
       }
   }
   
   /**
    * Normaliza el nombre de un medicamento para búsquedas consistentes.
    * Elimina espacios, convierte a minúsculas y elimina caracteres especiales.
    *
    * @param medicationName Nombre del medicamento a normalizar
    * @return Nombre normalizado
    */
   private String normalizeMedicationName(String medicationName) {
       if (medicationName == null) {
           return "";
       }
       
       // Convertir a minúsculas
       String normalized = medicationName.toLowerCase();
       
       // Eliminar marcas comerciales entre paréntesis
       normalized = normalized.replaceAll("\\([^)]*\\)", "");
       
       // Eliminar caracteres especiales y espacios extra
       normalized = normalized.replaceAll("[^a-zñáéíóúü0-9]", " ")
                         .replaceAll("\\s+", " ")
                         .trim();
       
       // Normalizar nombres comunes
       Map<String, String> commonNames = new HashMap<>();
       commonNames.put("acido acetilsalicilico", "aspirina");
       commonNames.put("asa", "aspirina");
       commonNames.put("paracetamol", "acetaminofen");
       commonNames.put("acetaminofeno", "acetaminofen");
       commonNames.put("levotiroxina sodica", "levotiroxina");
       commonNames.put("aines", "antiinflamatorio no esteroideo");
       commonNames.put("acido valproico", "valproato");
       commonNames.put("aas", "aspirina");
       commonNames.put("omeprazol sodico", "omeprazol");
       commonNames.put("metroprolol", "metoprolol"); // Corrección de error común
       
       for (Map.Entry<String, String> entry : commonNames.entrySet()) {
           if (normalized.equals(entry.getKey())) {
               normalized = entry.getValue();
               break;
           }
       }
       
       return normalized;
   }
   
   /**
    * Capitaliza la primera letra de un string.
    *
    * @param str String a capitalizar
    * @return String con la primera letra en mayúscula
    */
   private String capitalize(String str) {
       if (str == null || str.isEmpty()) {
           return str;
       }
       return str.substring(0, 1).toUpperCase() + str.substring(1);
   }
}