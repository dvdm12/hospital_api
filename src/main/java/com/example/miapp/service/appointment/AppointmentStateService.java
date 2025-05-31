package com.example.miapp.service.appointment;

import com.example.miapp.entity.Appointment;
import com.example.miapp.entity.Notification;
import com.example.miapp.entity.User;
import com.example.miapp.exception.AppointmentValidationException;
import com.example.miapp.repository.AppointmentRepository;
import com.example.miapp.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

/**
 * Servicio responsable de las operaciones de estado de citas
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AppointmentStateService {

    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final AppointmentQueryService queryService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Cancela una cita y notifica a los usuarios involucrados
     */
    public void cancelAppointment(Long appointmentId, String reason) {
        log.info("Cancelando cita con ID: {} con motivo: {}", appointmentId, reason);
        
        // Obtener la cita completa para poder acceder a doctor y paciente
        Appointment appointment = queryService.findAppointmentById(appointmentId);
        
        // Validar que la cita no esté ya completada
        if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
            throw new AppointmentValidationException("No se puede cancelar una cita ya completada");
        }
        
        // Validar que la cita no esté ya cancelada
        if (appointment.getStatus() == Appointment.AppointmentStatus.CANCELED) {
            throw new AppointmentValidationException("Esta cita ya ha sido cancelada");
        }
        
        // Actualizar estado de la cita
        appointment.setStatus(Appointment.AppointmentStatus.CANCELED);
        appointment.setNotes("Cancelada: " + reason);
        appointmentRepository.save(appointment);
        
        // Notificar al doctor
        if (appointment.getDoctor() != null && appointment.getDoctor().getUser() != null) {
            notifyUser(
                appointment.getDoctor().getUser(),
                "Cita cancelada",
                "Su cita con " + appointment.getPatient().getFullName() + 
                " programada para " + appointment.getDate().format(DATE_FORMATTER) + 
                " ha sido cancelada por un administrador. Motivo: " + reason,
                Notification.NotificationType.APPOINTMENT_CANCELLATION,
                appointment.getId()
            );
        }
        
        // Notificar al paciente
        if (appointment.getPatient() != null && appointment.getPatient().getUser() != null) {
            notifyUser(
                appointment.getPatient().getUser(),
                "Cita cancelada",
                "Su cita con " + appointment.getDoctor().getFullName() + 
                " programada para " + appointment.getDate().format(DATE_FORMATTER) + 
                " ha sido cancelada por un administrador. Motivo: " + reason,
                Notification.NotificationType.APPOINTMENT_CANCELLATION,
                appointment.getId()
            );
        }
        
        log.info("Cita {} cancelada exitosamente y usuarios notificados", appointmentId);
    }
    
    /**
     * Método auxiliar para enviar notificaciones a usuarios
     */
    private void notifyUser(User user, String title, String content, 
                          Notification.NotificationType type, Long entityId) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .type(type)
                .status(Notification.NotificationStatus.UNREAD)
                .entityType(Notification.EntityType.APPOINTMENT)
                .entityId(entityId)
                .build();
        
        notificationRepository.save(notification);
        log.debug("Notificación enviada al usuario: {}", user.getUsername());
    }
}