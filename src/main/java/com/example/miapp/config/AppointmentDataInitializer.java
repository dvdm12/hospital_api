package com.example.miapp.config;

import com.example.miapp.entity.Appointment;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.Patient;
import com.example.miapp.repository.AppointmentRepository;
import com.example.miapp.repository.DoctorRepository;
import com.example.miapp.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AppointmentDataInitializer {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * Inicializa datos de citas de prueba
     */
    @Bean
    @Order(2)
    public CommandLineRunner initAppointmentData() {
        return args -> {
            // Verificar si ya hay citas en la base de datos
            if (appointmentRepository.count() == 0) {
                log.info("Inicializando datos de citas de prueba...");
                
                // Obtener doctor y paciente
                List<Doctor> doctors = doctorRepository.findAll();
                List<Patient> patients = patientRepository.findAll();
                
                if (!doctors.isEmpty() && !patients.isEmpty()) {
                    Doctor doctor = doctors.get(0);
                    Patient patient = patients.get(0);
                    
                    // Crear citas de prueba
                    createSampleAppointments(doctor, patient);
                    
                    log.info("Datos de citas inicializados correctamente");
                } else {
                    log.warn("No se pudieron crear citas de prueba: no hay doctores o pacientes disponibles");
                }
            } else {
                log.info("Saltando inicialización de citas: ya existen citas en la base de datos");
            }
        };
    }
    
    /**
     * Crea citas de muestra para pruebas
     * Aseguramos que haya citas para varios días diferentes
     */
    private void createSampleAppointments(Doctor doctor, Patient patient) {
        try {
            // Fecha base - hoy más 1 hora para asegurar que está en el futuro
            LocalDateTime baseDateTime = LocalDateTime.now().plusHours(1);
            
            // Día 1 (hoy) - 3 citas
            createAppointmentForDay(doctor, patient, baseDateTime, "Consulta general", Appointment.AppointmentStatus.CONFIRMED, true);
            createAppointmentForDay(doctor, patient, baseDateTime.plusHours(2), "Chequeo de rutina", Appointment.AppointmentStatus.SCHEDULED, false);
            createAppointmentForDay(doctor, patient, baseDateTime.plusHours(4), "Revisión de exámenes", Appointment.AppointmentStatus.CANCELED, false);
            
            // Día 2 (mañana) - 2 citas
            LocalDateTime tomorrow = baseDateTime.plusDays(1);
            createAppointmentForDay(doctor, patient, tomorrow, "Seguimiento de tratamiento", Appointment.AppointmentStatus.CONFIRMED, true);
            createAppointmentForDay(doctor, patient, tomorrow.plusHours(3), "Control de medicación", Appointment.AppointmentStatus.SCHEDULED, false);
            
            // Día 3 (pasado mañana) - 1 cita
            LocalDateTime dayAfterTomorrow = baseDateTime.plusDays(2);
            createAppointmentForDay(doctor, patient, dayAfterTomorrow, "Evaluación final", Appointment.AppointmentStatus.SCHEDULED, false);
            
            // Día específico (próxima semana) - 1 cita
            LocalDateTime nextWeek = baseDateTime.plusDays(7);
            createAppointmentForDay(doctor, patient, nextWeek, "Consulta de seguimiento", Appointment.AppointmentStatus.SCHEDULED, false);
            
            log.info("Creadas 7 citas de prueba para diferentes días");
        } catch (Exception e) {
            log.error("Error al crear citas de prueba: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Método auxiliar para crear una cita en un día específico
     */
    private void createAppointmentForDay(Doctor doctor, Patient patient, LocalDateTime dateTime, 
                                        String reason, Appointment.AppointmentStatus status, boolean confirmed) {
        try {
            Appointment appointment = new Appointment();
            appointment.setDoctor(doctor);
            appointment.setPatient(patient);
            appointment.setDate(dateTime);
            appointment.setEndTime(dateTime.plusMinutes(30));
            appointment.setReason(reason);
            appointment.setStatus(status);
            appointment.setConfirmed(confirmed);
            
            if (status == Appointment.AppointmentStatus.CANCELED) {
                appointment.setNotes("Cancelada: Paciente no disponible");
            }
            
            appointmentRepository.save(appointment);
            log.debug("Creada cita para: {} - {}", dateTime.toLocalDate(), reason);
        } catch (Exception e) {
            log.error("Error al crear cita para {}: {}", dateTime, e.getMessage(), e);
        }
    }
}