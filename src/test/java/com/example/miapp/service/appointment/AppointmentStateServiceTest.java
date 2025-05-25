package com.example.miapp.service.appointment;

import com.example.miapp.entity.Appointment.AppointmentStatus;
import com.example.miapp.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentStateServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentStateService stateService;

    @Captor
    private ArgumentCaptor<AppointmentStatus> statusCaptor;

    @Captor
    private ArgumentCaptor<LocalDateTime> dateTimeCaptor;

    @Captor
    private ArgumentCaptor<String> notesCaptor;

    private final Long APPOINTMENT_ID = 1L;

    @BeforeEach
    void setUp() {
        // Configure common mock behavior if needed
    }

    @Test
    void shouldConfirmAppointment() {
        // Given an appointment ID
        // When confirmAppointment is called
        stateService.confirmAppointment(APPOINTMENT_ID);

        // Then updateConfirmation should be called with correct parameters
        verify(appointmentRepository).updateConfirmation(eq(APPOINTMENT_ID), eq(true));
        
        // And updateStatus should be called with CONFIRMED status
        verify(appointmentRepository).updateStatus(eq(APPOINTMENT_ID), eq(AppointmentStatus.CONFIRMED), isNull());
    }

    @Test
    void shouldCancelAppointment() {
        // Given an appointment ID and cancellation reason
        String reason = "Patient requested cancellation";
        
        // When cancelAppointment is called
        stateService.cancelAppointment(APPOINTMENT_ID, reason);

        // Then updateStatus should be called with CANCELED status
        verify(appointmentRepository).updateStatus(eq(APPOINTMENT_ID), eq(AppointmentStatus.CANCELED), isNull());
        
        // And updateNotes should be called with correct cancellation message
        verify(appointmentRepository).updateNotes(eq(APPOINTMENT_ID), contains(reason), isNull());
    }

    @Test
    void shouldCancelAppointmentWithNullReason() {
        // Test handling of null reason
        stateService.cancelAppointment(APPOINTMENT_ID, null);
        
        verify(appointmentRepository).updateStatus(eq(APPOINTMENT_ID), eq(AppointmentStatus.CANCELED), isNull());
        verify(appointmentRepository).updateNotes(eq(APPOINTMENT_ID), eq("Canceled: null"), isNull());
    }

    @Test
    void shouldCompleteAppointment() {
        // Given an appointment ID and completion notes
        String notes = "Patient examination completed. Follow-up in 2 weeks.";
        
        // When completeAppointment is called
        stateService.completeAppointment(APPOINTMENT_ID, notes);

        // Then updateStatus should be called with COMPLETED status
        verify(appointmentRepository).updateStatus(eq(APPOINTMENT_ID), eq(AppointmentStatus.COMPLETED), isNull());
        
        // And updateNotes should be called with the provided notes
        verify(appointmentRepository).updateNotes(eq(APPOINTMENT_ID), eq(notes), isNull());
    }

    @Test
    void shouldCompleteAppointmentWithoutNotes() {
        // Test completing without notes
        stateService.completeAppointment(APPOINTMENT_ID, null);
        
        verify(appointmentRepository).updateStatus(eq(APPOINTMENT_ID), eq(AppointmentStatus.COMPLETED), isNull());
        verify(appointmentRepository, never()).updateNotes(any(), any(), any());
    }

    @Test
    void shouldCompleteAppointmentWithEmptyNotes() {
        // Test completing with empty notes
        stateService.completeAppointment(APPOINTMENT_ID, "  ");
        
        verify(appointmentRepository).updateStatus(eq(APPOINTMENT_ID), eq(AppointmentStatus.COMPLETED), isNull());
        verify(appointmentRepository, never()).updateNotes(any(), any(), any());
    }

    @Test
    void shouldMarkAsNoShow() {
        // When markAsNoShow is called
        stateService.markAsNoShow(APPOINTMENT_ID);

        // Then updateStatus should be called with NO_SHOW status
        verify(appointmentRepository).updateStatus(eq(APPOINTMENT_ID), eq(AppointmentStatus.NO_SHOW), isNull());
    }

    @Test
    void shouldRescheduleAppointment() {
        // Given an appointment ID and new dates
        LocalDateTime newDate = LocalDateTime.now().plusDays(7);
        LocalDateTime newEndTime = newDate.plusHours(1);
        
        // When rescheduleAppointment is called
        stateService.rescheduleAppointment(APPOINTMENT_ID, newDate, newEndTime);

        // Then rescheduleAppointment repository method should be called with correct parameters
        verify(appointmentRepository).rescheduleAppointment(
            eq(APPOINTMENT_ID), 
            eq(newDate), 
            eq(newEndTime), 
            isNull()
        );
    }

    @Test
    void shouldRescheduleAppointmentWithNullEndTime() {
        // Test rescheduling with null end time
        LocalDateTime newDate = LocalDateTime.now().plusDays(7);
        
        stateService.rescheduleAppointment(APPOINTMENT_ID, newDate, null);
        
        verify(appointmentRepository).rescheduleAppointment(
            eq(APPOINTMENT_ID), 
            eq(newDate), 
            isNull(), 
            isNull()
        );
    }
}