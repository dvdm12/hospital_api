package com.example.miapp.repository;

import com.example.miapp.entity.*;
import com.example.miapp.entity.Appointment.AppointmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentRepositoryTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    private Patient patient;
    private Doctor doctor;
    private User patientUser;
    private User doctorUser;
    private Appointment appointment1;
    private Appointment appointment2;
    private List<Appointment> appointmentList;
    private Page<Appointment> appointmentPage;
    private Pageable pageable;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // Crear usuarios
        patientUser = User.builder()
                .id(1L)
                .username("patient1")
                .email("patient1@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        
        doctorUser = User.builder()
                .id(2L)
                .username("doctor1")
                .email("doctor1@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        
        adminUser = User.builder()
                .id(3L)
                .username("admin1")
                .email("admin1@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        
        // Crear paciente
        patient = Patient.builder()
                .id(1L)
                .firstName("Maria")
                .lastName("Garcia")
                .phone("123456789")
                .address("123 Main St")
                .user(patientUser)
                .build();
        
        // Crear doctor
        doctor = Doctor.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phone("987654321")
                .licenseNumber("MED12345")
                .user(doctorUser)
                .build();
        
        // Crear citas
        LocalDateTime now = LocalDateTime.now();
        
        appointment1 = Appointment.builder()
                .id(1L)
                .patient(patient)
                .doctor(doctor)
                .date(now.plusDays(1))
                .endTime(now.plusDays(1).plusHours(1))
                .reason("Regular check-up")
                .status(AppointmentStatus.SCHEDULED)
                .confirmed(false)
                .createdAt(now.minusDays(1))
                .createdBy(adminUser)
                .location("Room 101")
                .build();
        
        appointment2 = Appointment.builder()
                .id(2L)
                .patient(patient)
                .doctor(doctor)
                .date(now.plusDays(3))
                .endTime(now.plusDays(3).plusHours(1))
                .reason("Follow-up consultation")
                .status(AppointmentStatus.CONFIRMED)
                .confirmed(true)
                .confirmationDate(now)
                .createdAt(now.minusDays(2))
                .createdBy(adminUser)
                .location("Room 102")
                .build();
        
        // Crear lista de citas
        appointmentList = new ArrayList<>();
        appointmentList.add(appointment1);
        appointmentList.add(appointment2);
        
        // Configurar paginación
        pageable = PageRequest.of(0, 10);
        appointmentPage = new PageImpl<>(appointmentList, pageable, appointmentList.size());
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por paciente y doctor")
    class PatientDoctorSearchTests {
        
        @Test
        @DisplayName("Debería encontrar citas por ID de paciente")
        void shouldFindByPatientId() {
            // Given
            when(appointmentRepository.findByPatientId(1L, pageable)).thenReturn(appointmentPage);
            
            // When
            Page<Appointment> result = appointmentRepository.findByPatientId(1L, pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            assertEquals(1L, result.getContent().get(0).getPatient().getId());
            assertEquals(1L, result.getContent().get(1).getPatient().getId());
            verify(appointmentRepository).findByPatientId(1L, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar citas por ID de doctor")
        void shouldFindByDoctorId() {
            // Given
            when(appointmentRepository.findByDoctorId(1L, pageable)).thenReturn(appointmentPage);
            
            // When
            Page<Appointment> result = appointmentRepository.findByDoctorId(1L, pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            assertEquals(1L, result.getContent().get(0).getDoctor().getId());
            assertEquals(1L, result.getContent().get(1).getDoctor().getId());
            verify(appointmentRepository).findByDoctorId(1L, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar citas por ID de paciente y estado")
        void shouldFindByPatientIdAndStatus() {
            // Given
            List<Appointment> scheduledAppointments = List.of(appointment1);
            Page<Appointment> scheduledPage = new PageImpl<>(scheduledAppointments, pageable, scheduledAppointments.size());
            
            when(appointmentRepository.findByPatientIdAndStatus(1L, AppointmentStatus.SCHEDULED, pageable))
                    .thenReturn(scheduledPage);
            
            // When
            Page<Appointment> result = appointmentRepository.findByPatientIdAndStatus(
                    1L, AppointmentStatus.SCHEDULED, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals(AppointmentStatus.SCHEDULED, result.getContent().get(0).getStatus());
            verify(appointmentRepository).findByPatientIdAndStatus(1L, AppointmentStatus.SCHEDULED, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar citas por ID de doctor y estado")
        void shouldFindByDoctorIdAndStatus() {
            // Given
            List<Appointment> confirmedAppointments = List.of(appointment2);
            Page<Appointment> confirmedPage = new PageImpl<>(confirmedAppointments, pageable, confirmedAppointments.size());
            
            when(appointmentRepository.findByDoctorIdAndStatus(1L, AppointmentStatus.CONFIRMED, pageable))
                    .thenReturn(confirmedPage);
            
            // When
            Page<Appointment> result = appointmentRepository.findByDoctorIdAndStatus(
                    1L, AppointmentStatus.CONFIRMED, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals(AppointmentStatus.CONFIRMED, result.getContent().get(0).getStatus());
            verify(appointmentRepository).findByDoctorIdAndStatus(1L, AppointmentStatus.CONFIRMED, pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por fecha y estado")
    class DateStatusSearchTests {
        
        @Test
        @DisplayName("Debería encontrar citas por estado")
        void shouldFindByStatus() {
            // Given
            List<Appointment> scheduledAppointments = List.of(appointment1);
            Page<Appointment> scheduledPage = new PageImpl<>(scheduledAppointments, pageable, scheduledAppointments.size());
            
            when(appointmentRepository.findByStatus(AppointmentStatus.SCHEDULED, pageable))
                    .thenReturn(scheduledPage);
            
            // When
            Page<Appointment> result = appointmentRepository.findByStatus(AppointmentStatus.SCHEDULED, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals(AppointmentStatus.SCHEDULED, result.getContent().get(0).getStatus());
            verify(appointmentRepository).findByStatus(AppointmentStatus.SCHEDULED, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar citas en un rango de fechas")
        void shouldFindByDateBetween() {
            // Given
            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = startDate.plusDays(5);
            
            when(appointmentRepository.findByDateBetween(startDate, endDate, pageable))
                    .thenReturn(appointmentPage);
            
            // When
            Page<Appointment> result = appointmentRepository.findByDateBetween(startDate, endDate, pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(appointmentRepository).findByDateBetween(startDate, endDate, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar citas por ID de doctor en un rango de fechas")
        void shouldFindByDoctorIdAndDateBetween() {
            // Given
            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = startDate.plusDays(5);
            
            when(appointmentRepository.findByDoctorIdAndDateBetween(1L, startDate, endDate, pageable))
                    .thenReturn(appointmentPage);
            
            // When
            Page<Appointment> result = appointmentRepository.findByDoctorIdAndDateBetween(
                    1L, startDate, endDate, pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(appointmentRepository).findByDoctorIdAndDateBetween(1L, startDate, endDate, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar citas por ID de paciente en un rango de fechas")
        void shouldFindByPatientIdAndDateBetween() {
            // Given
            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = startDate.plusDays(5);
            
            when(appointmentRepository.findByPatientIdAndDateBetween(1L, startDate, endDate, pageable))
                    .thenReturn(appointmentPage);
            
            // When
            Page<Appointment> result = appointmentRepository.findByPatientIdAndDateBetween(
                    1L, startDate, endDate, pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(appointmentRepository).findByPatientIdAndDateBetween(1L, startDate, endDate, pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de próxima cita")
    class NextAppointmentTests {
        
        @Test
        @DisplayName("Debería encontrar la próxima cita para un paciente")
        void shouldFindNextAppointmentForPatient() {
            // Given
            LocalDateTime currentDate = LocalDateTime.now();
            
            when(appointmentRepository.findNextAppointmentForPatient(1L, currentDate))
                    .thenReturn(Optional.of(appointment1));
            
            // When
            Optional<Appointment> result = appointmentRepository.findNextAppointmentForPatient(1L, currentDate);
            
            // Then
            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
            verify(appointmentRepository).findNextAppointmentForPatient(1L, currentDate);
        }
        
        @Test
        @DisplayName("Debería retornar vacío si no hay próximas citas")
        void shouldReturnEmptyWhenNoUpcomingAppointments() {
            // Given
            LocalDateTime currentDate = LocalDateTime.now();
            
            when(appointmentRepository.findNextAppointmentForPatient(2L, currentDate))
                    .thenReturn(Optional.empty());
            
            // When
            Optional<Appointment> result = appointmentRepository.findNextAppointmentForPatient(2L, currentDate);
            
            // Then
            assertFalse(result.isPresent());
            verify(appointmentRepository).findNextAppointmentForPatient(2L, currentDate);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de operaciones de actualización")
    class UpdateOperationTests {
        
        @Test
        @DisplayName("Debería actualizar el estado de una cita")
        void shouldUpdateStatus() {
            // Given
            when(appointmentRepository.updateStatus(1L, AppointmentStatus.COMPLETED, 3L))
                    .thenReturn(1);
            
            // When
            int affectedRows = appointmentRepository.updateStatus(1L, AppointmentStatus.COMPLETED, 3L);
            
            // Then
            assertEquals(1, affectedRows);
            verify(appointmentRepository).updateStatus(1L, AppointmentStatus.COMPLETED, 3L);
        }
        
        @Test
        @DisplayName("Debería actualizar la confirmación de una cita")
        void shouldUpdateConfirmation() {
            // Given
            when(appointmentRepository.updateConfirmation(1L, true))
                    .thenReturn(1);
            
            // When
            int affectedRows = appointmentRepository.updateConfirmation(1L, true);
            
            // Then
            assertEquals(1, affectedRows);
            verify(appointmentRepository).updateConfirmation(1L, true);
        }
        
        @Test
        @DisplayName("Debería actualizar las notas de una cita")
        void shouldUpdateNotes() {
            // Given
            String newNotes = "Patient reported feeling better";
            when(appointmentRepository.updateNotes(1L, newNotes, 3L))
                    .thenReturn(1);
            
            // When
            int affectedRows = appointmentRepository.updateNotes(1L, newNotes, 3L);
            
            // Then
            assertEquals(1, affectedRows);
            verify(appointmentRepository).updateNotes(1L, newNotes, 3L);
        }
        
        @Test
        @DisplayName("Debería reprogramar una cita")
        void shouldRescheduleAppointment() {
            // Given
            LocalDateTime newDate = LocalDateTime.now().plusDays(7);
            LocalDateTime newEndTime = newDate.plusHours(1);
            
            when(appointmentRepository.rescheduleAppointment(1L, newDate, newEndTime, 3L))
                    .thenReturn(1);
            
            // When
            int affectedRows = appointmentRepository.rescheduleAppointment(1L, newDate, newEndTime, 3L);
            
            // Then
            assertEquals(1, affectedRows);
            verify(appointmentRepository).rescheduleAppointment(1L, newDate, newEndTime, 3L);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda de conflictos")
    class ConflictSearchTests {
        
        @Test
        @DisplayName("Debería encontrar citas superpuestas")
        void shouldFindOverlappingAppointments() {
            // Given
            LocalDateTime startDate = LocalDateTime.now().plusDays(1);
            LocalDateTime endDate = startDate.plusHours(1);
            
            when(appointmentRepository.findOverlappingAppointments(1L, startDate, endDate, null))
                    .thenReturn(List.of(appointment1));
            
            // When
            List<Appointment> result = appointmentRepository.findOverlappingAppointments(
                    1L, startDate, endDate, null);
            
            // Then
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getId());
            verify(appointmentRepository).findOverlappingAppointments(1L, startDate, endDate, null);
        }
        
        @Test
        @DisplayName("Debería excluir una cita específica al buscar superposiciones")
        void shouldExcludeAppointmentWhenFindingOverlaps() {
            // Given
            LocalDateTime startDate = LocalDateTime.now().plusDays(1);
            LocalDateTime endDate = startDate.plusHours(1);
            
            when(appointmentRepository.findOverlappingAppointments(1L, startDate, endDate, 1L))
                    .thenReturn(List.of());
            
            // When
            List<Appointment> result = appointmentRepository.findOverlappingAppointments(
                    1L, startDate, endDate, 1L);
            
            // Then
            assertTrue(result.isEmpty());
            verify(appointmentRepository).findOverlappingAppointments(1L, startDate, endDate, 1L);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda de citas para marcar como no-show")
    class NoShowSearchTests {
        
        @Test
        @DisplayName("Debería encontrar citas para marcar como no-show")
        void shouldFindAppointmentsToMarkAsNoShow() {
            // Given
            // Crear un tiempo límite para las citas no asistidas
            LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(30);
            
            when(appointmentRepository.findAppointmentsToMarkAsNoShow(thresholdTime, pageable))
                    .thenReturn(new PageImpl<>(List.of(appointment1), pageable, 1));
            
            // When
            Page<Appointment> result = appointmentRepository.findAppointmentsToMarkAsNoShow(
                    thresholdTime, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            verify(appointmentRepository).findAppointmentsToMarkAsNoShow(thresholdTime, pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de estadísticas")
    class StatisticsTests {
        
        @Test
        @DisplayName("Debería contar citas por estado")
        void shouldCountAppointmentsByStatus() {
            // Given
            List<Object[]> statusCounts = List.of(
                    new Object[]{AppointmentStatus.SCHEDULED, 1L},
                    new Object[]{AppointmentStatus.CONFIRMED, 1L}
            );
            
            when(appointmentRepository.countAppointmentsByStatus())
                    .thenReturn(statusCounts);
            
            // When
            List<Object[]> result = appointmentRepository.countAppointmentsByStatus();
            
            // Then
            assertEquals(2, result.size());
            assertEquals(AppointmentStatus.SCHEDULED, result.get(0)[0]);
            assertEquals(1L, result.get(0)[1]);
            assertEquals(AppointmentStatus.CONFIRMED, result.get(1)[0]);
            assertEquals(1L, result.get(1)[1]);
            verify(appointmentRepository).countAppointmentsByStatus();
        }
        
        @Test
        @DisplayName("Debería contar citas por día de la semana")
        void shouldCountAppointmentsByDayOfWeek() {
            // Given
            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = startDate.plusDays(7);
            
            List<Object[]> dayOfWeekCounts = List.of(
                    new Object[]{1, 1L},  // Domingo
                    new Object[]{2, 1L}   // Lunes
            );
            
            when(appointmentRepository.countAppointmentsByDayOfWeek(startDate, endDate))
                    .thenReturn(dayOfWeekCounts);
            
            // When
            List<Object[]> result = appointmentRepository.countAppointmentsByDayOfWeek(startDate, endDate);
            
            // Then
            assertEquals(2, result.size());
            assertEquals(1, result.get(0)[0]);
            assertEquals(1L, result.get(0)[1]);
            assertEquals(2, result.get(1)[0]);
            assertEquals(1L, result.get(1)[1]);
            verify(appointmentRepository).countAppointmentsByDayOfWeek(startDate, endDate);
        }
        
        @Test
        @DisplayName("Debería contar citas por hora del día")
        void shouldCountAppointmentsByHourOfDay() {
            // Given
            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = startDate.plusDays(7);
            
            List<Object[]> hourOfDayCounts = List.of(
                    new Object[]{9, 1L},  // 9 AM
                    new Object[]{14, 1L}  // 2 PM
            );
            
            when(appointmentRepository.countAppointmentsByHourOfDay(startDate, endDate))
                    .thenReturn(hourOfDayCounts);
            
            // When
            List<Object[]> result = appointmentRepository.countAppointmentsByHourOfDay(startDate, endDate);
            
            // Then
            assertEquals(2, result.size());
            assertEquals(9, result.get(0)[0]);
            assertEquals(1L, result.get(0)[1]);
            assertEquals(14, result.get(1)[0]);
            assertEquals(1L, result.get(1)[1]);
            verify(appointmentRepository).countAppointmentsByHourOfDay(startDate, endDate);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda avanzada")
    class AdvancedSearchTests {
        
        @Test
        @DisplayName("Debería realizar búsqueda avanzada de citas")
        void shouldSearchAppointments() {
            // Given
            Long doctorId = 1L;
            Long patientId = 1L;
            AppointmentStatus status = AppointmentStatus.SCHEDULED;
            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = startDate.plusDays(7);
            String reasonPattern = "check";
            
            when(appointmentRepository.searchAppointments(
                    doctorId, patientId, status, startDate, endDate, reasonPattern, pageable))
                    .thenReturn(new PageImpl<>(List.of(appointment1), pageable, 1));
            
            // When
            Page<Appointment> result = appointmentRepository.searchAppointments(
                    doctorId, patientId, status, startDate, endDate, reasonPattern, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals(1L, result.getContent().get(0).getId());
            verify(appointmentRepository).searchAppointments(
                    doctorId, patientId, status, startDate, endDate, reasonPattern, pageable);
        }
        
        @Test
        @DisplayName("Debería realizar búsqueda con criterios parciales")
        void shouldSearchWithPartialCriteria() {
            // Given
            when(appointmentRepository.searchAppointments(
                    null, null, null, null, null, "consultation", pageable))
                    .thenReturn(new PageImpl<>(List.of(appointment2), pageable, 1));
            
            // When
            Page<Appointment> result = appointmentRepository.searchAppointments(
                    null, null, null, null, null, "consultation", pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals(2L, result.getContent().get(0).getId());
            verify(appointmentRepository).searchAppointments(
                    null, null, null, null, null, "consultation", pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de proyecciones")
    class ProjectionTests {
        
        @Test
        @DisplayName("Debería encontrar información básica de todas las citas")
        void shouldFindAllBasicInfo() {
            // Given
            List<AppointmentRepository.AppointmentBasicInfo> basicInfoList = List.of(
                    createBasicAppointmentInfo(1L, "Regular check-up", AppointmentStatus.SCHEDULED),
                    createBasicAppointmentInfo(2L, "Follow-up consultation", AppointmentStatus.CONFIRMED)
            );
            
            Page<AppointmentRepository.AppointmentBasicInfo> basicInfoPage = 
                    new PageImpl<>(basicInfoList, pageable, basicInfoList.size());
                    
            when(appointmentRepository.findAllBasicInfo(pageable)).thenReturn(basicInfoPage);
            
            // When
            Page<AppointmentRepository.AppointmentBasicInfo> result = appointmentRepository.findAllBasicInfo(pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            assertEquals("Regular check-up", result.getContent().get(0).getReason());
            assertEquals("Follow-up consultation", result.getContent().get(1).getReason());
            verify(appointmentRepository).findAllBasicInfo(pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar información básica por ID de doctor")
        void shouldFindBasicInfoByDoctorId() {
            // Given
            List<AppointmentRepository.AppointmentBasicInfo> basicInfoList = List.of(
                    createBasicAppointmentInfo(1L, "Regular check-up", AppointmentStatus.SCHEDULED),
                    createBasicAppointmentInfo(2L, "Follow-up consultation", AppointmentStatus.CONFIRMED)
            );
            
            Page<AppointmentRepository.AppointmentBasicInfo> basicInfoPage = 
                    new PageImpl<>(basicInfoList, pageable, basicInfoList.size());
                    
            when(appointmentRepository.findBasicInfoByDoctorId(1L, pageable)).thenReturn(basicInfoPage);
            
            // When
            Page<AppointmentRepository.AppointmentBasicInfo> result = appointmentRepository.findBasicInfoByDoctorId(1L, pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(appointmentRepository).findBasicInfoByDoctorId(1L, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar información básica por ID de paciente")
        void shouldFindBasicInfoByPatientId() {
            // Given
            List<AppointmentRepository.AppointmentBasicInfo> basicInfoList = List.of(
                    createBasicAppointmentInfo(1L, "Regular check-up", AppointmentStatus.SCHEDULED),
                    createBasicAppointmentInfo(2L, "Follow-up consultation", AppointmentStatus.CONFIRMED)
            );
            
            Page<AppointmentRepository.AppointmentBasicInfo> basicInfoPage = 
                    new PageImpl<>(basicInfoList, pageable, basicInfoList.size());
                    
            when(appointmentRepository.findBasicInfoByPatientId(1L, pageable)).thenReturn(basicInfoPage);
            
            // When
            Page<AppointmentRepository.AppointmentBasicInfo> result = appointmentRepository.findBasicInfoByPatientId(1L, pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(appointmentRepository).findBasicInfoByPatientId(1L, pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de métodos de disponibilidad")
    class AvailabilityTests {
        
        @Test
        @DisplayName("Debería encontrar franjas horarias disponibles")
        void shouldFindAvailableTimeSlots() {
            // Given
            LocalDateTime date = LocalDateTime.now().plusDays(1);
            List<Object[]> availableSlots = List.of(
                    new Object[]{"09:00:00", "09:30:00"},
                    new Object[]{"10:00:00", "10:30:00"}
            );
            
            when(appointmentRepository.findAvailableTimeSlots(1L, date))
                    .thenReturn(availableSlots);
            
            // When
            List<Object[]> result = appointmentRepository.findAvailableTimeSlots(1L, date);
            
            // Then
            assertEquals(2, result.size());
            assertEquals("09:00:00", result.get(0)[0]);
            assertEquals("09:30:00", result.get(0)[1]);
            verify(appointmentRepository).findAvailableTimeSlots(1L, date);
        }
        
        @Test
        @DisplayName("Debería encontrar citas por doctor y día de la semana")
        void shouldFindByDoctorIdAndDayOfWeek() {
            // Given
            int dayOfWeek = 2; // Lunes
            
            when(appointmentRepository.findByDoctorIdAndDayOfWeek(1L, dayOfWeek, pageable))
                    .thenReturn(appointmentPage);
            
            // When
            Page<Appointment> result = appointmentRepository.findByDoctorIdAndDayOfWeek(
                    1L, dayOfWeek, pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(appointmentRepository).findByDoctorIdAndDayOfWeek(1L, dayOfWeek, pageable);
        }
    }
    
    // Método auxiliar para crear objetos AppointmentBasicInfo
    private AppointmentRepository.AppointmentBasicInfo createBasicAppointmentInfo(
            Long id, String reason, AppointmentStatus status) {
        return new AppointmentRepository.AppointmentBasicInfo() {
            @Override
            public Long getId() {
                return id;
            }
            
            @Override
            public LocalDateTime getDate() {
                return LocalDateTime.now().plusDays(id);
            }
            
            @Override
            public LocalDateTime getEndTime() {
                return LocalDateTime.now().plusDays(id).plusHours(1);
            }
            
            @Override
            public String getDoctorName() {
                return "John Smith";
            }
            
            @Override
            public String getPatientName() {
                return "Maria Garcia";
            }
            
            @Override
            public String getReason() {
                return reason;
            }
            
            @Override
            public AppointmentStatus getStatus() {
                return status;
            }
            
            @Override
            public boolean isConfirmed() {
                return status == AppointmentStatus.CONFIRMED;
            }
        };
    }
}