package com.example.miapp.service.appointment;

import com.example.miapp.dto.appointment.AppointmentDto;
import com.example.miapp.dto.appointment.CreateAppointmentRequest;
import com.example.miapp.entity.Appointment;
import com.example.miapp.entity.Appointment.AppointmentStatus;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.Patient;
import com.example.miapp.mapper.AppointmentMapper;
import com.example.miapp.repository.AppointmentRepository;
import com.example.miapp.service.doctor.DoctorService;
import com.example.miapp.service.patient.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentBusinessServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    
    @Mock
    private AppointmentMapper appointmentMapper;
    
    @Mock
    private AppointmentValidationService validationService;
    
    @Mock
    private AppointmentStateService stateService;
    
    @Mock
    private AppointmentQueryService queryService;
    
    @Mock
    private DoctorService doctorService;
    
    @Mock
    private PatientService patientService;
    
    @InjectMocks
    private AppointmentBusinessService businessService;
    
    @Captor
    private ArgumentCaptor<Appointment> appointmentCaptor;
    
    private CreateAppointmentRequest appointmentRequest;
    private Doctor doctor;
    private Patient patient;
    private Appointment appointment;
    private AppointmentDto appointmentDto;
    private LocalDateTime futureDateTime;
    private LocalDateTime futureEndTime;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        futureDateTime = LocalDateTime.now().plusDays(1);
        futureEndTime = futureDateTime.plusMinutes(30);
        
        // Create appointment request
        appointmentRequest = new CreateAppointmentRequest();
        appointmentRequest.setDoctorId(1L);
        appointmentRequest.setPatientId(2L);
        appointmentRequest.setDate(futureDateTime);
        appointmentRequest.setEndTime(futureEndTime);
        appointmentRequest.setReason("Annual checkup");
        appointmentRequest.setNotes("Patient requested morning appointment");
        appointmentRequest.setLocation("Room 101");
        
        // Create entities
        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setFirstName("John");
        doctor.setLastName("Doe");
        
        patient = new Patient();
        patient.setId(2L);
        patient.setFirstName("Jane");
        patient.setLastName("Smith");
        
        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setDate(futureDateTime);
        appointment.setEndTime(futureEndTime);
        appointment.setReason("Annual checkup");
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        
        // Create DTO
        appointmentDto = new AppointmentDto();
        appointmentDto.setId(1L);
        appointmentDto.setDoctorName("John Doe");
        appointmentDto.setPatientName("Jane Smith");
        appointmentDto.setDate(futureDateTime);
        appointmentDto.setEndTime(futureEndTime);
        appointmentDto.setReason("Annual checkup");
        appointmentDto.setStatus(AppointmentStatus.SCHEDULED);
    }
    
    @Test
    void scheduleAppointment_ShouldCreateNewAppointment() {
        // Given
        when(doctorService.findDoctorById(anyLong())).thenReturn(doctor);
        when(patientService.findPatientById(anyLong())).thenReturn(patient);
        doNothing().when(validationService).validateAppointmentCreation(any(), any());
        when(appointmentMapper.toEntity(any())).thenReturn(appointment);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toDto(any(Appointment.class))).thenReturn(appointmentDto);
        
        // When
        AppointmentDto result = businessService.scheduleAppointment(appointmentRequest);
        
        // Then
        assertNotNull(result);
        assertEquals(appointmentDto.getId(), result.getId());
        assertEquals(appointmentDto.getDoctorName(), result.getDoctorName());
        
        // Verify interactions
        verify(doctorService).findDoctorById(appointmentRequest.getDoctorId());
        verify(patientService).findPatientById(appointmentRequest.getPatientId());
        verify(validationService).validateAppointmentCreation(eq(appointmentRequest), eq(doctor));
        verify(appointmentRepository).save(any(Appointment.class));
        verify(appointmentMapper).toDto(appointment);
        
        // Verify appointment properties
        verify(appointmentRepository).save(appointmentCaptor.capture());
        Appointment savedAppointment = appointmentCaptor.getValue();
        assertEquals(doctor, savedAppointment.getDoctor());
        assertEquals(patient, savedAppointment.getPatient());
    }
    
    @Test
    void confirmAppointment_ShouldValidateAndConfirm() {
        // Given
        Long appointmentId = 1L;
        when(queryService.findAppointmentById(appointmentId)).thenReturn(appointment);
        doNothing().when(validationService).validateAppointmentConfirmation(appointment);
        doNothing().when(stateService).confirmAppointment(appointmentId);
        
        // When
        businessService.confirmAppointment(appointmentId);
        
        // Then
        verify(queryService).findAppointmentById(appointmentId);
        verify(validationService).validateAppointmentConfirmation(appointment);
        verify(stateService).confirmAppointment(appointmentId);
    }
    
    @Test
    void cancelAppointment_ShouldValidateAndCancel() {
        // Given
        Long appointmentId = 1L;
        String reason = "Patient request";
        when(queryService.findAppointmentById(appointmentId)).thenReturn(appointment);
        doNothing().when(validationService).validateAppointmentCancellation(appointment);
        doNothing().when(stateService).cancelAppointment(appointmentId, reason);
        
        // When
        businessService.cancelAppointment(appointmentId, reason);
        
        // Then
        verify(queryService).findAppointmentById(appointmentId);
        verify(validationService).validateAppointmentCancellation(appointment);
        verify(stateService).cancelAppointment(appointmentId, reason);
    }
    
    @Test
    void rescheduleAppointment_ShouldValidateAndReschedule() {
        // Given
        Long appointmentId = 1L;
        LocalDateTime newDate = LocalDateTime.now().plusDays(2);
        LocalDateTime newEndTime = newDate.plusMinutes(30);
        
        when(queryService.findAppointmentById(appointmentId)).thenReturn(appointment);
        doNothing().when(validationService).validateAppointmentCancellation(appointment);
        doNothing().when(validationService).validateAppointmentReschedule(
                eq(appointmentId), eq(newDate), eq(newEndTime), eq(doctor.getId()));
        doNothing().when(stateService).rescheduleAppointment(appointmentId, newDate, newEndTime);
        when(queryService.getAppointment(appointmentId)).thenReturn(appointmentDto);
        
        // When
        AppointmentDto result = businessService.rescheduleAppointment(appointmentId, newDate, newEndTime);
        
        // Then
        assertNotNull(result);
        verify(queryService).findAppointmentById(appointmentId);
        verify(validationService).validateAppointmentCancellation(appointment);
        verify(validationService).validateAppointmentReschedule(
                eq(appointmentId), eq(newDate), eq(newEndTime), eq(doctor.getId()));
        verify(stateService).rescheduleAppointment(appointmentId, newDate, newEndTime);
        verify(queryService).getAppointment(appointmentId);
    }
    
    @Test
    void completeAppointment_ShouldValidateAndComplete() {
        // Given
        Long appointmentId = 1L;
        String notes = "Treatment completed successfully";
        when(queryService.findAppointmentById(appointmentId)).thenReturn(appointment);
        doNothing().when(validationService).validateAppointmentCompletion(appointment);
        doNothing().when(stateService).completeAppointment(appointmentId, notes);
        
        // When
        businessService.completeAppointment(appointmentId, notes);
        
        // Then
        verify(queryService).findAppointmentById(appointmentId);
        verify(validationService).validateAppointmentCompletion(appointment);
        verify(stateService).completeAppointment(appointmentId, notes);
    }
    
    @Test
    void markAsNoShow_ShouldMarkAppointment() {
        // Given
        Long appointmentId = 1L;
        doNothing().when(stateService).markAsNoShow(appointmentId);
        
        // When
        businessService.markAsNoShow(appointmentId);
        
        // Then
        verify(stateService).markAsNoShow(appointmentId);
    }
    
    @Test
    void processNoShowAppointments_ShouldProcessAllOverdueAppointments() {
        // Given
        int gracePeriodMinutes = 30;
        
        // Create test appointments
        Appointment appointment1 = new Appointment();
        appointment1.setId(1L);
        appointment1.setDate(LocalDateTime.now().minusHours(2));
        appointment1.setStatus(AppointmentStatus.SCHEDULED);
        
        Appointment appointment2 = new Appointment();
        appointment2.setId(2L);
        appointment2.setDate(LocalDateTime.now().minusHours(1));
        appointment2.setStatus(AppointmentStatus.SCHEDULED);
        
        List<Appointment> overdueAppointments = List.of(appointment1, appointment2);
        Page<Appointment> appointmentPage = new PageImpl<>(overdueAppointments);
        
        // Captura el tiempo umbral calculado para verificación posterior
        ArgumentCaptor<LocalDateTime> thresholdTimeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        
        // Configure mock behavior para el nuevo método con thresholdTime
        when(appointmentRepository.findAppointmentsToMarkAsNoShow(
                thresholdTimeCaptor.capture(), any(Pageable.class)))
                .thenReturn(appointmentPage);
                
        doNothing().when(stateService).markAsNoShow(anyLong());
        
        // When
        businessService.processNoShowAppointments(gracePeriodMinutes);
        
        // Then
        // Verify repository was called with correct threshold time
        verify(appointmentRepository).findAppointmentsToMarkAsNoShow(
                any(LocalDateTime.class), any(Pageable.class));
                
        // Verify the threshold time is approximately now - gracePeriodMinutes
        LocalDateTime capturedThresholdTime = thresholdTimeCaptor.getValue();
        LocalDateTime expectedThresholdTime = LocalDateTime.now().minusMinutes(gracePeriodMinutes);
        long secondsDifference = Math.abs(java.time.Duration.between(capturedThresholdTime, expectedThresholdTime).getSeconds());
        assertTrue(secondsDifference < 5, "The threshold time should be approximately 'now - gracePeriodMinutes'");
        
        // Verify each appointment was processed correctly
        verify(stateService).markAsNoShow(1L);
        verify(stateService).markAsNoShow(2L);
        
        // Verify exactly 2 calls were made (no more, no less)
        verify(stateService, times(2)).markAsNoShow(anyLong());
        
        // No other interactions with stateService
        verifyNoMoreInteractions(stateService);
    }
    
    @Test
    void processNoShowAppointments_ShouldHandleEmptyResult() {
        // Given
        int gracePeriodMinutes = 30;
        Page<Appointment> emptyPage = new PageImpl<>(Collections.emptyList());
        
        // Updated mock behavior for the new method signature
        when(appointmentRepository.findAppointmentsToMarkAsNoShow(
                any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(emptyPage);
        
        // When
        businessService.processNoShowAppointments(gracePeriodMinutes);
        
        // Then
        // Verify repository was called with correct threshold time
        verify(appointmentRepository).findAppointmentsToMarkAsNoShow(
                any(LocalDateTime.class), any(Pageable.class));
        verify(stateService, never()).markAsNoShow(anyLong());
    }
    
    @Test
    void createAppointmentEntity_ShouldSetEndTimeIfNotProvided() {
        // Given
        CreateAppointmentRequest requestWithoutEndTime = new CreateAppointmentRequest();
        requestWithoutEndTime.setDoctorId(1L);
        requestWithoutEndTime.setPatientId(2L);
        requestWithoutEndTime.setDate(futureDateTime);
        requestWithoutEndTime.setEndTime(null); // No end time provided
        requestWithoutEndTime.setReason("Annual checkup");
        
        Appointment appointmentWithoutEndTime = new Appointment();
        appointmentWithoutEndTime.setReason("Annual checkup");
        
        when(doctorService.findDoctorById(anyLong())).thenReturn(doctor);
        when(patientService.findPatientById(anyLong())).thenReturn(patient);
        doNothing().when(validationService).validateAppointmentCreation(any(), any());
        when(appointmentMapper.toEntity(any())).thenReturn(appointmentWithoutEndTime);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointmentWithoutEndTime);
        when(appointmentMapper.toDto(any(Appointment.class))).thenReturn(appointmentDto);
        
        // When
        businessService.scheduleAppointment(requestWithoutEndTime);
        
        // Then
        verify(appointmentRepository).save(appointmentCaptor.capture());
        Appointment savedAppointment = appointmentCaptor.getValue();
        
        // Should have automatically set end time to start time + 30 minutes
        assertNotNull(savedAppointment.getEndTime());
        assertEquals(futureDateTime.plusMinutes(30), savedAppointment.getEndTime());
    }
}