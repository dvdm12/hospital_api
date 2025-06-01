package com.example.miapp.service.appointment;

import com.example.miapp.dto.appointment.AppointmentDto;
import com.example.miapp.dto.appointment.CreateAppointmentRequest;
import com.example.miapp.entity.Appointment;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.Notification;
import com.example.miapp.entity.Patient;
import com.example.miapp.exception.AppointmentValidationException;
import com.example.miapp.exception.ResourceNotFoundException;
import com.example.miapp.mapper.ManualAppointmentMapper;
import com.example.miapp.repository.AppointmentRepository;
import com.example.miapp.repository.DoctorRepository;
import com.example.miapp.repository.NotificationRepository;
import com.example.miapp.repository.PatientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio principal que orquesta operaciones de citas
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AppointmentBusinessService {

    private final AppointmentStateService stateService;
    private final AppointmentQueryService queryService;
    private final AppointmentRepository appointmentRepository;
private final DoctorRepository doctorRepository;
private final PatientRepository patientRepository;
private final NotificationRepository notificationRepository;
private final ManualAppointmentMapper appointmentMapper;

    /**
     * Lista todas las citas del sistema
     */
    @Transactional(readOnly = true)
    public Page<AppointmentDto> getAllAppointments(Pageable pageable) {
        log.info("Solicitando listado de todas las citas");
        
        Page<AppointmentDto> appointments = queryService.getAllAppointments(pageable);
        
        if (appointments.isEmpty()) {
            log.warn("No se encontraron citas en el sistema");
            throw new ResourceNotFoundException("No hay citas registradas en el sistema");
        }
        
        return appointments;
    }
    
    /**
     * Lista las citas de un día específico
     */
    @Transactional(readOnly = true)
    public Page<AppointmentDto> getAppointmentsByDay(LocalDate date, Pageable pageable) {
        // Si no se proporciona fecha, usar la fecha actual
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        
        log.info("Solicitando listado de citas para el día: {}", targetDate);
        
        Page<AppointmentDto> appointments = queryService.getAppointmentsByDay(targetDate, pageable);
        
        if (appointments.isEmpty()) {
            log.warn("No se encontraron citas para el día: {}", targetDate);
            throw new ResourceNotFoundException("No hay citas programadas para el día " + targetDate);
        }
        
        return appointments;
    }

    /**
     * Cancela una cita con notificación a los usuarios involucrados
     */
    public void cancelAppointment(Long appointmentId, String reason) {
        log.info("Solicitando cancelación de cita: {} con motivo: {}", appointmentId, reason);
        
        try {
            stateService.cancelAppointment(appointmentId, reason);
        } catch (ResourceNotFoundException | AppointmentValidationException e) {
            // Estas excepciones ya tienen mensajes apropiados, solo las propagamos
            throw e;
        } catch (Exception e) {
            // Cualquier otra excepción la envolvemos para dar un mensaje más claro
            log.error("Error inesperado al cancelar cita {}: {}", appointmentId, e.getMessage(), e);
            throw new RuntimeException("Error al cancelar la cita: " + e.getMessage(), e);
        }
    }

    /**
 * Crea una nueva cita con validaciones correspondientes
 */
@Transactional
public AppointmentDto createAppointment(CreateAppointmentRequest request) {
    log.info("Procesando solicitud de creación de cita: paciente {} con doctor {} para {}", 
            request.getPatientId(), request.getDoctorId(), request.getDate());
    
    // Validar la solicitud de cita utilizando AppointmentValidationService
    try {
        // Este método debe implementarse en el servicio de validación
        validateAppointmentRequest(request);
        
        // Crear y guardar la cita
        Appointment appointment = createAndSaveAppointment(request);
        
        // Notificar a los usuarios involucrados
        sendAppointmentNotifications(appointment);
        
        log.info("Cita creada exitosamente con ID: {}", appointment.getId());
        
        // Convertir a DTO y retornar
        return appointmentMapper.toDto(appointment);
    } catch (ResourceNotFoundException | AppointmentValidationException e) {
        // Estas excepciones ya tienen mensajes apropiados, solo las propagamos
        throw e;
    } catch (Exception e) {
        // Cualquier otra excepción la envolvemos para dar un mensaje más claro
        log.error("Error inesperado al crear cita: {}", e.getMessage(), e);
        throw new RuntimeException("Error al crear la cita: " + e.getMessage(), e);
    }
}

/**
 * Valida la solicitud de cita
 */
private void validateAppointmentRequest(CreateAppointmentRequest request) {
    // Obtener entidades necesarias para validación
    var doctor = getDoctorById(request.getDoctorId());
    
    // Delegar a AppointmentValidationService para validaciones específicas de citas
    AppointmentValidationService validationService = new AppointmentValidationService(appointmentRepository);
    validationService.validateAppointmentCreation(request, doctor);
}

/**
 * Crea y guarda la entidad Appointment
 */
private Appointment createAndSaveAppointment(CreateAppointmentRequest request) {
    // Obtener entidades relacionadas
    var doctor = getDoctorById(request.getDoctorId());
    var patient = getPatientById(request.getPatientId());
    
    // Calcular endTime si no fue proporcionado
    LocalDateTime endTime = request.getEndTime();
    if (endTime == null) {
        // Buscar la duración de slot del doctor para este día
        int slotDuration = doctor.getWorkSchedules().stream()
            .filter(ws -> ws.getDayOfWeek() == request.getDate().getDayOfWeek())
            .filter(ws -> ws.isActive())
            .map(ws -> ws.getSlotDurationMinutes())
            .findFirst()
            .orElse(30); // Valor por defecto si no se encuentra
        
        endTime = request.getDate().plusMinutes(slotDuration);
    }
    
    // Crear la entidad Appointment
    Appointment appointment = Appointment.builder()
        .doctor(doctor)
        .patient(patient)
        .date(request.getDate())
        .endTime(endTime)
        .reason(request.getReason())
        .notes(request.getNotes())
        .status(Appointment.AppointmentStatus.SCHEDULED)
        .confirmed(false)
        .location(request.getLocation())
        .createdAt(LocalDateTime.now())
        .build();
    
    // Guardar la cita
    return appointmentRepository.save(appointment);
}

/**
 * Envía notificaciones a los usuarios involucrados
 */
private void sendAppointmentNotifications(Appointment appointment) {
    // Formato para fechas en notificaciones
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    // Notificar al doctor
    if (appointment.getDoctor() != null && appointment.getDoctor().getUser() != null) {
        Notification doctorNotification = Notification.builder()
            .user(appointment.getDoctor().getUser())
            .title("Nueva cita programada")
            .content("Se ha programado una cita con " + appointment.getPatient().getFullName() + 
                    " para el " + appointment.getDate().format(formatter) + 
                    " en " + appointment.getLocation())
            .type(Notification.NotificationType.APPOINTMENT_CONFIRMATION)
            .status(Notification.NotificationStatus.UNREAD)
            .entityType(Notification.EntityType.APPOINTMENT)
            .entityId(appointment.getId())
            .createdAt(LocalDateTime.now())
            .build();
        
        notificationRepository.save(doctorNotification);
    }
    
    // Notificar al paciente
    if (appointment.getPatient() != null && appointment.getPatient().getUser() != null) {
        Notification patientNotification = Notification.builder()
            .user(appointment.getPatient().getUser())
            .title("Cita médica programada")
            .content("Se ha programado su cita con " + appointment.getDoctor().getFullName() + 
                    " para el " + appointment.getDate().format(formatter) + 
                    " en " + appointment.getLocation())
            .type(Notification.NotificationType.APPOINTMENT_CONFIRMATION)
            .status(Notification.NotificationStatus.UNREAD)
            .entityType(Notification.EntityType.APPOINTMENT)
            .entityId(appointment.getId())
            .createdAt(LocalDateTime.now())
            .build();
        
        notificationRepository.save(patientNotification);
    }
}

/**
 * Obtiene un doctor por ID
 */
private Doctor getDoctorById(Long doctorId) {
    return doctorRepository.findById(doctorId)
        .orElseThrow(() -> {
            log.error("Doctor no encontrado con ID: {}", doctorId);
            return new ResourceNotFoundException("Doctor no encontrado con ID: " + doctorId);
        });
}

/**
 * Obtiene un paciente por ID
 */
private Patient getPatientById(Long patientId) {
    return patientRepository.findById(patientId)
        .orElseThrow(() -> {
            log.error("Paciente no encontrado con ID: {}", patientId);
            return new ResourceNotFoundException("Paciente no encontrado con ID: " + patientId);
        });
}

    
}