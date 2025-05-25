package com.example.miapp.service.appointment;

import com.example.miapp.dto.appointment.AppointmentDto;
import com.example.miapp.dto.appointment.AppointmentSearchCriteria;
import com.example.miapp.dto.appointment.AvailableSlotDto;
import com.example.miapp.entity.Appointment;
import com.example.miapp.entity.Appointment.AppointmentStatus;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.Patient;
import com.example.miapp.mapper.AppointmentMapper;
import com.example.miapp.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentQueryServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    
    @Mock
    private AppointmentMapper appointmentMapper;
    
    @InjectMocks
    private AppointmentQueryService queryService;
    
    private Appointment appointment;
    private AppointmentDto appointmentDto;
    private Patient patient;
    private Doctor doctor;
    private LocalDateTime now;
    private Pageable pageable;
    
    @BeforeEach
    void setUp() {
        // Configurar fecha actual para pruebas
        now = LocalDateTime.now();
        
        // Crear objetos de prueba
        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setFirstName("John");
        doctor.setLastName("Smith");
        
        patient = new Patient();
        patient.setId(2L);
        patient.setFirstName("Jane");
        patient.setLastName("Doe");
        
        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setDate(now.plusDays(1));
        appointment.setEndTime(now.plusDays(1).plusMinutes(30));
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setReason("Regular checkup");
        
        appointmentDto = new AppointmentDto();
        appointmentDto.setId(1L);
        appointmentDto.setDoctorName("John Smith");
        appointmentDto.setPatientName("Jane Doe");
        appointmentDto.setDate(now.plusDays(1));
        appointmentDto.setEndTime(now.plusDays(1).plusMinutes(30));
        appointmentDto.setStatus(AppointmentStatus.SCHEDULED);
        appointmentDto.setReason("Regular checkup");
        
        pageable = Pageable.unpaged();
    }
    
    @Test
    void shouldGetAppointmentById() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentMapper.toDto(appointment)).thenReturn(appointmentDto);
        
        // When
        AppointmentDto result = queryService.getAppointment(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Smith", result.getDoctorName());
        assertEquals("Jane Doe", result.getPatientName());
        verify(appointmentRepository).findById(1L);
        verify(appointmentMapper).toDto(appointment);
    }
    
    @Test
    void shouldThrowExceptionWhenAppointmentNotFound() {
        // Given
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
        
        // When/Then
        assertThrows(RuntimeException.class, () -> queryService.getAppointment(99L));
        verify(appointmentRepository).findById(99L);
    }
    
    @Test
    void shouldGetAvailableSlots() {
        // Given
        LocalDate date = LocalDate.now();
        List<Object[]> availableSlots = Arrays.asList(
            new Object[]{LocalTime.of(9, 0), LocalTime.of(9, 30)},
            new Object[]{LocalTime.of(10, 0), LocalTime.of(10, 30)}
        );
        
        List<AvailableSlotDto> expectedDtos = Arrays.asList(
            new AvailableSlotDto(LocalTime.of(9, 0), LocalTime.of(9, 30), true, null),
            new AvailableSlotDto(LocalTime.of(10, 0), LocalTime.of(10, 30), true, null)
        );
        
        when(appointmentRepository.findAvailableTimeSlots(eq(1L), any(LocalDateTime.class)))
            .thenReturn(availableSlots);
        when(appointmentMapper.mapToAvailableSlots(availableSlots)).thenReturn(expectedDtos);
        
        // When
        List<AvailableSlotDto> result = queryService.getAvailableSlots(1L, date);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(LocalTime.of(9, 0), result.get(0).getStartTime());
        assertEquals(LocalTime.of(9, 30), result.get(0).getEndTime());
        verify(appointmentRepository).findAvailableTimeSlots(eq(1L), any(LocalDateTime.class));
        verify(appointmentMapper).mapToAvailableSlots(availableSlots);
    }
    
    @Test
    void shouldReturnEmptyListWhenNoAvailableSlots() {
        // Given
        LocalDate date = LocalDate.now();
        List<Object[]> emptySlots = Collections.emptyList();
        List<AvailableSlotDto> emptyDtos = Collections.emptyList();
        
        when(appointmentRepository.findAvailableTimeSlots(eq(1L), any(LocalDateTime.class)))
            .thenReturn(emptySlots);
        when(appointmentMapper.mapToAvailableSlots(emptySlots)).thenReturn(emptyDtos);
        
        // When
        List<AvailableSlotDto> result = queryService.getAvailableSlots(1L, date);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(appointmentRepository).findAvailableTimeSlots(eq(1L), any(LocalDateTime.class));
        verify(appointmentMapper).mapToAvailableSlots(emptySlots);
    }
    
    @Test
void shouldGetPatientAppointments() {
    // Given
    List<Appointment> appointments = Collections.singletonList(appointment);
    Page<Appointment> appointmentPage = new PageImpl<>(appointments, pageable, 1);
    
    // El mapper convierte cada elemento, no la página completa
    when(appointmentRepository.findByPatientId(2L, pageable)).thenReturn(appointmentPage);
    when(appointmentMapper.toDto(appointment)).thenReturn(appointmentDto);
    
    // When
    Page<AppointmentDto> result = queryService.getPatientAppointments(2L, pageable);
    
    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(1, result.getContent().size());
    
    // Verificamos el contenido del primer elemento
    AppointmentDto actualDto = result.getContent().get(0);
    assertEquals(appointmentDto.getId(), actualDto.getId());
    assertEquals(appointmentDto.getDoctorName(), actualDto.getDoctorName());
    assertEquals(appointmentDto.getPatientName(), actualDto.getPatientName());
    assertEquals(appointmentDto.getDate(), actualDto.getDate());
    assertEquals(appointmentDto.getStatus(), actualDto.getStatus());
    
    // Verificamos las interacciones
    verify(appointmentRepository).findByPatientId(2L, pageable);
    verify(appointmentMapper).toDto(appointment);
}
    
    @Test
    void shouldGetDoctorAppointments() {
        // Given
        List<Appointment> appointments = Collections.singletonList(appointment);
        Page<Appointment> appointmentPage = new PageImpl<>(appointments);
        
        when(appointmentRepository.findByDoctorId(1L, pageable)).thenReturn(appointmentPage);
        when(appointmentMapper.toDto(appointment)).thenReturn(appointmentDto);
        
        // When
        Page<AppointmentDto> result = queryService.getDoctorAppointments(1L, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(appointmentDto, result.getContent().get(0));
        verify(appointmentRepository).findByDoctorId(1L, pageable);
        verify(appointmentMapper).toDto(appointment);
    }
    
    @Test
    void shouldGetTodayAppointments() {
        // Given
        List<Appointment> appointments = Collections.singletonList(appointment);
        Page<Appointment> appointmentPage = new PageImpl<>(appointments);
        
        when(appointmentRepository.findByDoctorIdAndDateBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
            .thenReturn(appointmentPage);
        when(appointmentMapper.toDto(appointment)).thenReturn(appointmentDto);
        
        // When
        List<AppointmentDto> result = queryService.getTodayAppointments(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(appointmentDto, result.get(0));
        verify(appointmentRepository).findByDoctorIdAndDateBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
        verify(appointmentMapper).toDto(appointment);
    }
    
    @Test
    void shouldSearchAppointments() {
        // Given
        AppointmentSearchCriteria criteria = new AppointmentSearchCriteria();
        criteria.setDoctorId(1L);
        criteria.setPatientId(2L);
        criteria.setStatus(AppointmentStatus.SCHEDULED);
        criteria.setStartDate(now);
        criteria.setEndDate(now.plusDays(7));
        criteria.setReasonPattern("checkup");
        
        List<Appointment> appointments = Collections.singletonList(appointment);
        Page<Appointment> appointmentPage = new PageImpl<>(appointments);
        
        when(appointmentRepository.searchAppointments(
                eq(1L), eq(2L), eq(AppointmentStatus.SCHEDULED), 
                eq(now), eq(now.plusDays(7)), eq("checkup"), eq(pageable)))
            .thenReturn(appointmentPage);
        when(appointmentMapper.toDto(appointment)).thenReturn(appointmentDto);
        
        // When
        Page<AppointmentDto> result = queryService.searchAppointments(criteria, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(appointmentDto, result.getContent().get(0));
        verify(appointmentRepository).searchAppointments(
                eq(1L), eq(2L), eq(AppointmentStatus.SCHEDULED), 
                eq(now), eq(now.plusDays(7)), eq("checkup"), eq(pageable));
        verify(appointmentMapper).toDto(appointment);
    }
    
    @Test
    void shouldGetNextAppointment() {
        // Given
        when(appointmentRepository.findNextAppointmentForPatient(eq(2L), any(LocalDateTime.class)))
            .thenReturn(Optional.of(appointment));
        when(appointmentMapper.toDto(appointment)).thenReturn(appointmentDto);
        
        // When
        Optional<AppointmentDto> result = queryService.getNextAppointment(2L);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(appointmentDto, result.get());
        verify(appointmentRepository).findNextAppointmentForPatient(eq(2L), any(LocalDateTime.class));
        verify(appointmentMapper).toDto(appointment);
    }
    
    @Test
    void shouldReturnEmptyWhenNoNextAppointment() {
        // Given
        when(appointmentRepository.findNextAppointmentForPatient(eq(2L), any(LocalDateTime.class)))
            .thenReturn(Optional.empty());
        
        // When
        Optional<AppointmentDto> result = queryService.getNextAppointment(2L);
        
        // Then
        assertFalse(result.isPresent());
        verify(appointmentRepository).findNextAppointmentForPatient(eq(2L), any(LocalDateTime.class));
        verify(appointmentMapper, never()).toDto(any());
    }
    
    @Test
    void shouldGetAppointmentStatusStats() {
        // Given
        List<Object[]> statsData = Arrays.asList(
            new Object[]{AppointmentStatus.SCHEDULED, 10L},
            new Object[]{AppointmentStatus.COMPLETED, 5L}
        );
        
        when(appointmentRepository.countAppointmentsByStatus()).thenReturn(statsData);
        
        // When
        List<Object[]> result = queryService.getAppointmentStatusStats();
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(AppointmentStatus.SCHEDULED, result.get(0)[0]);
        assertEquals(10L, result.get(0)[1]);
        verify(appointmentRepository).countAppointmentsByStatus();
    }
    
    @Test
    void shouldFindAppointmentById() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        
        // When
        Appointment result = queryService.findAppointmentById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(appointmentRepository).findById(1L);
    }
    
    @Test
    void shouldThrowExceptionWhenFindingNonExistentAppointment() {
        // Given
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
        
        // When/Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            queryService.findAppointmentById(99L);
        });
        
        assertTrue(exception.getMessage().contains("Appointment not found"));
        verify(appointmentRepository).findById(99L);
    }
}