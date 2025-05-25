package com.example.miapp.service.appointment;

import com.example.miapp.dto.appointment.CreateAppointmentRequest;
import com.example.miapp.entity.Appointment;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.Appointment.AppointmentStatus;
import com.example.miapp.exception.AppointmentValidationException;
import com.example.miapp.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentValidationServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentValidationService validationService;

    private Doctor doctor;
    private Appointment appointment;
    private LocalDateTime futureDateTime;
    private LocalDateTime futureEndTime;

    @BeforeEach
    void setUp() {
        // Setup base test data - only objects, no mocking configurations
        futureDateTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        futureEndTime = futureDateTime.plusMinutes(30);
        
        // Setup doctor as mock without configuration
        doctor = mock(Doctor.class);
        
        // Setup appointment
        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDate(futureDateTime);
        appointment.setEndTime(futureEndTime);
    }

    @Nested
    @DisplayName("Validate Appointment Creation Tests")
    class ValidateAppointmentCreationTests {
        
        @Test
        @DisplayName("Should validate valid appointment creation")
        void shouldValidateValidAppointmentCreation() {
            // Given valid request and doctor availability
            CreateAppointmentRequest validRequest = createValidRequest();
            
            // Configure doctor for this specific test - only configure what's actually used
            // Removed doctor.getId() as it's not needed in this test
            when(doctor.isAvailable(eq(futureDateTime.getDayOfWeek()), eq(futureDateTime.toLocalTime())))
                .thenReturn(true);
                
            when(appointmentRepository.findOverlappingAppointments(
                    eq(1L), eq(futureDateTime), eq(futureEndTime), isNull()))
                .thenReturn(Collections.emptyList());
            
            // When validateAppointmentCreation is called
            // Then no exception should be thrown
            assertDoesNotThrow(() -> validationService.validateAppointmentCreation(validRequest, doctor));
            
            // Verify interactions
            verify(doctor).isAvailable(eq(futureDateTime.getDayOfWeek()), eq(futureDateTime.toLocalTime()));
            verify(appointmentRepository).findOverlappingAppointments(
                    eq(1L), eq(futureDateTime), eq(futureEndTime), isNull());
        }
        
        @Test
        @DisplayName("Should reject appointment in past")
        void shouldRejectAppointmentInPast() {
            // Given a request with past date
            CreateAppointmentRequest pastRequest = new CreateAppointmentRequest();
            pastRequest.setDate(LocalDateTime.now().minusDays(1));
            pastRequest.setEndTime(LocalDateTime.now().minusDays(1).plusMinutes(30));
            
            // When validateAppointmentCreation is called
            // Then should throw AppointmentValidationException
            AppointmentValidationException exception = assertThrows(
                AppointmentValidationException.class,
                () -> validationService.validateAppointmentCreation(pastRequest, doctor)
            );
            
            // Verify message
            assertEquals("Appointment date cannot be in the past", exception.getMessage());
            
            // Verify no unnecessary interactions
            verify(appointmentRepository, never()).findOverlappingAppointments(any(), any(), any(), any());
        }
        
        @Test
        @DisplayName("Should reject appointment with end time before start time")
        void shouldRejectAppointmentWithEndTimeBeforeStartTime() {
            // Given a request with end time before start time
            CreateAppointmentRequest invalidRequest = new CreateAppointmentRequest();
            invalidRequest.setDate(futureDateTime);
            invalidRequest.setEndTime(futureDateTime.minusMinutes(10)); // End time before start
            
            // When validateAppointmentCreation is called
            // Then should throw AppointmentValidationException
            AppointmentValidationException exception = assertThrows(
                AppointmentValidationException.class,
                () -> validationService.validateAppointmentCreation(invalidRequest, doctor)
            );
            
            // Verify message
            assertEquals("End time cannot be before start time", exception.getMessage());
            
            // Verify no unnecessary interactions
            verify(appointmentRepository, never()).findOverlappingAppointments(any(), any(), any(), any());
        }
        
        @Test
        @DisplayName("Should reject appointment when doctor is not available")
        void shouldRejectAppointmentWhenDoctorNotAvailable() {
            // Given doctor is not available at requested time
            LocalDateTime unavailableTime = LocalDateTime.now().plusDays(1).withHour(15).withMinute(0);
            CreateAppointmentRequest requestWithUnavailableTime = new CreateAppointmentRequest();
            requestWithUnavailableTime.setDate(unavailableTime);
            requestWithUnavailableTime.setEndTime(unavailableTime.plusMinutes(30));
            
            // Configure doctor for this specific test
            when(doctor.isAvailable(eq(unavailableTime.getDayOfWeek()), eq(unavailableTime.toLocalTime())))
                .thenReturn(false);
            
            // When validateAppointmentCreation is called
            // Then should throw AppointmentValidationException
            AppointmentValidationException exception = assertThrows(
                AppointmentValidationException.class,
                () -> validationService.validateAppointmentCreation(requestWithUnavailableTime, doctor)
            );
            
            // Verify message contains day and time
            assertTrue(exception.getMessage().contains("Doctor is not available"));
            assertTrue(exception.getMessage().contains(unavailableTime.getDayOfWeek().toString()));
            
            // Verify no unnecessary interactions
            verify(appointmentRepository, never()).findOverlappingAppointments(any(), any(), any(), any());
        }
        
        @Test
        @DisplayName("Should reject appointment with conflict")
        void shouldRejectAppointmentWithConflict() {
            // Given a conflicting appointment exists
            CreateAppointmentRequest validRequest = createValidRequest();
            
            // Configure doctor for this specific test - only configure what's actually used
            // Removed doctor.getId() as it's not needed in this test
            when(doctor.isAvailable(eq(futureDateTime.getDayOfWeek()), eq(futureDateTime.toLocalTime())))
                .thenReturn(true);
                
            List<Appointment> conflictingAppointments = List.of(new Appointment());
            when(appointmentRepository.findOverlappingAppointments(
                    eq(1L), eq(futureDateTime), eq(futureEndTime), isNull()))
                .thenReturn(conflictingAppointments);
            
            // When validateAppointmentCreation is called
            // Then should throw AppointmentValidationException
            AppointmentValidationException exception = assertThrows(
                AppointmentValidationException.class,
                () -> validationService.validateAppointmentCreation(validRequest, doctor)
            );
            
            // Verify message
            assertEquals("Doctor is not available at the requested time. Conflicting appointments found.", 
                         exception.getMessage());
            
            // Verify interactions
            verify(doctor).isAvailable(eq(futureDateTime.getDayOfWeek()), eq(futureDateTime.toLocalTime()));
            verify(appointmentRepository).findOverlappingAppointments(
                    eq(1L), eq(futureDateTime), eq(futureEndTime), isNull());
        }
        
        @Test
        @DisplayName("Should use default end time when not provided")
        void shouldUseDefaultEndTimeWhenNotProvided() {
            // Given a request without end time
            CreateAppointmentRequest requestWithoutEndTime = new CreateAppointmentRequest();
            requestWithoutEndTime.setDoctorId(1L);
            requestWithoutEndTime.setPatientId(1L);
            requestWithoutEndTime.setDate(futureDateTime);
            requestWithoutEndTime.setEndTime(null); // No end time provided
            requestWithoutEndTime.setReason("Regular checkup");
            
            // Expected default end time (start + 30 minutes)
            LocalDateTime expectedEndTime = futureDateTime.plusMinutes(30);
            
            // Configure doctor for this specific test - only configure what's actually used
            // Removed doctor.getId() as it's not needed in this test
            when(doctor.isAvailable(eq(futureDateTime.getDayOfWeek()), eq(futureDateTime.toLocalTime())))
                .thenReturn(true);
                
            when(appointmentRepository.findOverlappingAppointments(
                    eq(1L), eq(futureDateTime), eq(expectedEndTime), isNull()))
                .thenReturn(Collections.emptyList());
            
            // When validateAppointmentCreation is called
            // Then no exception should be thrown
            assertDoesNotThrow(() -> 
                validationService.validateAppointmentCreation(requestWithoutEndTime, doctor));
            
            // Verify correct end time was used
            verify(appointmentRepository).findOverlappingAppointments(
                    eq(1L), eq(futureDateTime), eq(expectedEndTime), isNull());
        }
        
        // Helper method to create a valid request
        private CreateAppointmentRequest createValidRequest() {
            CreateAppointmentRequest request = new CreateAppointmentRequest();
            request.setDoctorId(1L);
            request.setPatientId(1L);
            request.setDate(futureDateTime);
            request.setEndTime(futureEndTime);
            request.setReason("Regular checkup");
            return request;
        }
    }
    
    @Nested
    @DisplayName("Validate Appointment Reschedule Tests")
    class ValidateAppointmentRescheduleTests {
        
        @Test
        @DisplayName("Should validate valid appointment reschedule")
        void shouldValidateValidAppointmentReschedule() {
            // Given no conflicts for new time
            LocalDateTime newDate = futureDateTime.plusDays(1);
            LocalDateTime newEndTime = newDate.plusMinutes(30);
            Long appointmentId = 1L;
            Long doctorId = 1L;
            
            when(appointmentRepository.findOverlappingAppointments(
                    eq(doctorId), eq(newDate), eq(newEndTime), eq(appointmentId)))
                .thenReturn(Collections.emptyList());
            
            // When validateAppointmentReschedule is called
            // Then no exception should be thrown
            assertDoesNotThrow(() -> 
                validationService.validateAppointmentReschedule(appointmentId, newDate, newEndTime, doctorId));
            
            // Verify interactions
            verify(appointmentRepository).findOverlappingAppointments(
                    eq(doctorId), eq(newDate), eq(newEndTime), eq(appointmentId));
        }
        
        @Test
        @DisplayName("Should reject reschedule with conflict")
        void shouldRejectRescheduleWithConflict() {
            // Given a conflicting appointment exists
            LocalDateTime newDate = futureDateTime.plusDays(1);
            LocalDateTime newEndTime = newDate.plusMinutes(30);
            Long appointmentId = 1L;
            Long doctorId = 1L;
            
            List<Appointment> conflictingAppointments = List.of(new Appointment());
            when(appointmentRepository.findOverlappingAppointments(
                    eq(doctorId), eq(newDate), eq(newEndTime), eq(appointmentId)))
                .thenReturn(conflictingAppointments);
            
            // When validateAppointmentReschedule is called
            // Then should throw AppointmentValidationException
            AppointmentValidationException exception = assertThrows(
                AppointmentValidationException.class,
                () -> validationService.validateAppointmentReschedule(appointmentId, newDate, newEndTime, doctorId)
            );
            
            // Verify message
            assertEquals("Doctor is not available at the requested time. Conflicting appointments found.", 
                         exception.getMessage());
            
            // Verify interactions
            verify(appointmentRepository).findOverlappingAppointments(
                    eq(doctorId), eq(newDate), eq(newEndTime), eq(appointmentId));
        }
    }
    
    @Nested
    @DisplayName("Validate Appointment Confirmation Tests")
    class ValidateAppointmentConfirmationTests {
        
        @Test
        @DisplayName("Should validate scheduled appointment confirmation")
        void shouldValidateScheduledAppointmentConfirmation() {
            // Given a scheduled appointment
            appointment.setStatus(AppointmentStatus.SCHEDULED);
            
            // When validateAppointmentConfirmation is called
            // Then no exception should be thrown
            assertDoesNotThrow(() -> validationService.validateAppointmentConfirmation(appointment));
        }
        
        @Test
        @DisplayName("Should reject confirmation of non-scheduled appointment")
        void shouldRejectConfirmationOfNonScheduledAppointment() {
            // Given appointments with various non-SCHEDULED statuses
            AppointmentStatus[] invalidStatuses = {
                AppointmentStatus.CONFIRMED, AppointmentStatus.COMPLETED, 
                AppointmentStatus.CANCELED, AppointmentStatus.RESCHEDULED, 
                AppointmentStatus.NO_SHOW
            };
            
            for (AppointmentStatus status : invalidStatuses) {
                appointment.setStatus(status);
                
                // When validateAppointmentConfirmation is called
                // Then should throw AppointmentValidationException
                AppointmentValidationException exception = assertThrows(
                    AppointmentValidationException.class,
                    () -> validationService.validateAppointmentConfirmation(appointment)
                );
                
                // Verify message
                String expectedMessage = "Only scheduled appointments can be confirmed. Current status: " + status;
                assertEquals(expectedMessage, exception.getMessage());
            }
        }
    }
    
    @Nested
    @DisplayName("Validate Appointment Cancellation Tests")
    class ValidateAppointmentCancellationTests {
        
        @Test
        @DisplayName("Should validate non-completed appointment cancellation")
        void shouldValidateNonCompletedAppointmentCancellation() {
            // Given appointments with various cancellable statuses
            AppointmentStatus[] validStatuses = {
                AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED, 
                AppointmentStatus.RESCHEDULED
            };
            
            for (AppointmentStatus status : validStatuses) {
                appointment.setStatus(status);
                
                // When validateAppointmentCancellation is called
                // Then no exception should be thrown
                assertDoesNotThrow(() -> validationService.validateAppointmentCancellation(appointment));
            }
        }
        
        @Test
        @DisplayName("Should reject cancellation of completed appointment")
        void shouldRejectCancellationOfCompletedAppointment() {
            // Given a completed appointment
            appointment.setStatus(AppointmentStatus.COMPLETED);
            
            // When validateAppointmentCancellation is called
            // Then should throw AppointmentValidationException
            AppointmentValidationException exception = assertThrows(
                AppointmentValidationException.class,
                () -> validationService.validateAppointmentCancellation(appointment)
            );
            
            // Verify message
            assertEquals("Cannot cancel a completed appointment", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Validate Appointment Completion Tests")
    class ValidateAppointmentCompletionTests {
        
        @Test
        @DisplayName("Should validate scheduled appointment completion")
        void shouldValidateScheduledAppointmentCompletion() {
            // Given a scheduled appointment
            appointment.setStatus(AppointmentStatus.SCHEDULED);
            
            // When validateAppointmentCompletion is called
            // Then no exception should be thrown
            assertDoesNotThrow(() -> validationService.validateAppointmentCompletion(appointment));
        }
        
        @Test
        @DisplayName("Should validate confirmed appointment completion")
        void shouldValidateConfirmedAppointmentCompletion() {
            // Given a confirmed appointment
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            
            // When validateAppointmentCompletion is called
            // Then no exception should be thrown
            assertDoesNotThrow(() -> validationService.validateAppointmentCompletion(appointment));
        }
        
        @Test
        @DisplayName("Should reject completion of non-scheduled/confirmed appointment")
        void shouldRejectCompletionOfNonScheduledConfirmedAppointment() {
            // Given appointments with various invalid statuses
            AppointmentStatus[] invalidStatuses = {
                AppointmentStatus.COMPLETED, AppointmentStatus.CANCELED, 
                AppointmentStatus.RESCHEDULED, AppointmentStatus.NO_SHOW
            };
            
            for (AppointmentStatus status : invalidStatuses) {
                appointment.setStatus(status);
                
                // When validateAppointmentCompletion is called
                // Then should throw AppointmentValidationException
                AppointmentValidationException exception = assertThrows(
                    AppointmentValidationException.class,
                    () -> validationService.validateAppointmentCompletion(appointment)
                );
                
                // Verify message
                String expectedMessage = "Only confirmed or scheduled appointments can be completed. Current status: " + status;
                assertEquals(expectedMessage, exception.getMessage());
            }
        }
    }
}