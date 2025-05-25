package com.example.miapp.service.doctor;

import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.DoctorSchedule;
import com.example.miapp.entity.DoctorSpecialty;
import com.example.miapp.entity.Specialty;
import com.example.miapp.repository.DoctorRepository;
import com.example.miapp.repository.SpecialtyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorManagementServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private SpecialtyRepository specialtyRepository;

    @InjectMocks
    private DoctorManagementService doctorManagementService;

    private Doctor testDoctor;
    private Specialty testSpecialty;
    private DoctorSchedule testSchedule;
    private final Long DOCTOR_ID = 1L;
    private final Long SPECIALTY_ID = 1L;
    private final Long SCHEDULE_ID = 1L;

    @BeforeEach
    void setUp() {
        // Initialize test doctor
        testDoctor = Doctor.builder()
                .id(DOCTOR_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .licenseNumber("MED12345")
                .consultationFee(100.0)
                .doctorSpecialties(new ArrayList<>())
                .workSchedules(new HashSet<>())
                .build();

        // Initialize test specialty
        testSpecialty = Specialty.builder()
                .id(SPECIALTY_ID)
                .name("Cardiology")
                .description("Heart specialist")
                .build();

        // Initialize test schedule
        testSchedule = DoctorSchedule.builder()
                .id(SCHEDULE_ID)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .slotDurationMinutes(30)
                .active(true)
                .location("Room 101")
                .build();
    }

    @Test
    void updateConsultationFee_Success() {
        // Arrange
        Double newFee = 150.0;
        when(doctorRepository.updateConsultationFee(DOCTOR_ID, newFee)).thenReturn(1);

        // Act
        doctorManagementService.updateConsultationFee(DOCTOR_ID, newFee);

        // Assert
        verify(doctorRepository, times(1)).updateConsultationFee(DOCTOR_ID, newFee);
    }

    @Test
    void updateConsultationFee_FailedUpdate() {
        // Arrange
        Double newFee = 150.0;
        when(doctorRepository.updateConsultationFee(DOCTOR_ID, newFee)).thenReturn(0);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorManagementService.updateConsultationFee(DOCTOR_ID, newFee);
        });

        assertEquals("Failed to update consultation fee for doctor: " + DOCTOR_ID, exception.getMessage());
        verify(doctorRepository, times(1)).updateConsultationFee(DOCTOR_ID, newFee);
    }

    @Test
    void updateBiography_Success() {
        // Arrange
        String newBiography = "New professional biography";
        when(doctorRepository.updateBiography(DOCTOR_ID, newBiography)).thenReturn(1);

        // Act
        doctorManagementService.updateBiography(DOCTOR_ID, newBiography);

        // Assert
        verify(doctorRepository, times(1)).updateBiography(DOCTOR_ID, newBiography);
    }

    @Test
    void updateBiography_FailedUpdate() {
        // Arrange
        String newBiography = "New professional biography";
        when(doctorRepository.updateBiography(DOCTOR_ID, newBiography)).thenReturn(0);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorManagementService.updateBiography(DOCTOR_ID, newBiography);
        });

        assertEquals("Failed to update biography for doctor: " + DOCTOR_ID, exception.getMessage());
        verify(doctorRepository, times(1)).updateBiography(DOCTOR_ID, newBiography);
    }

    @Test
    void addSpecialtyToDoctor_Success() {
        // Arrange
        String experienceLevel = "INTERMEDIATE";
        Date certificationDate = new Date();
        
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(testDoctor));
        when(specialtyRepository.findById(SPECIALTY_ID)).thenReturn(Optional.of(testSpecialty));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);

        // Act
        doctorManagementService.addSpecialtyToDoctor(DOCTOR_ID, SPECIALTY_ID, experienceLevel, certificationDate);

        // Assert
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(specialtyRepository, times(1)).findById(SPECIALTY_ID);
        verify(doctorRepository, times(1)).save(testDoctor);
        
        assertEquals(1, testDoctor.getDoctorSpecialties().size());
        DoctorSpecialty addedSpecialty = testDoctor.getDoctorSpecialties().get(0);
        assertEquals(testDoctor, addedSpecialty.getDoctor());
        assertEquals(testSpecialty, addedSpecialty.getSpecialty());
        assertEquals(experienceLevel, addedSpecialty.getExperienceLevel());
        assertEquals(certificationDate, addedSpecialty.getCertificationDate());
    }

    @Test
    void addSpecialtyToDoctor_DoctorNotFound() {
        // Arrange
        String experienceLevel = "INTERMEDIATE";
        Date certificationDate = new Date();
        
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorManagementService.addSpecialtyToDoctor(DOCTOR_ID, SPECIALTY_ID, experienceLevel, certificationDate);
        });

        assertEquals("Doctor not found: " + DOCTOR_ID, exception.getMessage());
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(specialtyRepository, never()).findById(any());
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void addSpecialtyToDoctor_SpecialtyNotFound() {
        // Arrange
        String experienceLevel = "INTERMEDIATE";
        Date certificationDate = new Date();
        
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(testDoctor));
        when(specialtyRepository.findById(SPECIALTY_ID)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorManagementService.addSpecialtyToDoctor(DOCTOR_ID, SPECIALTY_ID, experienceLevel, certificationDate);
        });

        assertEquals("Specialty not found: " + SPECIALTY_ID, exception.getMessage());
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(specialtyRepository, times(1)).findById(SPECIALTY_ID);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void addSpecialtyToDoctor_AlreadyHasSpecialty() {
        // Arrange
        String experienceLevel = "INTERMEDIATE";
        Date certificationDate = new Date();
        
        // Add the specialty to the doctor's list
        DoctorSpecialty existingDoctorSpecialty = DoctorSpecialty.builder()
                .doctor(testDoctor)
                .specialty(testSpecialty)
                .build();
        testDoctor.getDoctorSpecialties().add(existingDoctorSpecialty);
        
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(testDoctor));
        when(specialtyRepository.findById(SPECIALTY_ID)).thenReturn(Optional.of(testSpecialty));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorManagementService.addSpecialtyToDoctor(DOCTOR_ID, SPECIALTY_ID, experienceLevel, certificationDate);
        });

        assertEquals("Doctor already has specialty: " + testSpecialty.getName(), exception.getMessage());
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(specialtyRepository, times(1)).findById(SPECIALTY_ID);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void removeSpecialtyFromDoctor_Success() {
        // Arrange
        // Add two specialties to the doctor
        Specialty specialtyToRemove = testSpecialty;
        Specialty specialtyToKeep = Specialty.builder()
                .id(2L)
                .name("Neurology")
                .build();
        
        DoctorSpecialty doctorSpecialtyToRemove = DoctorSpecialty.builder()
                .doctor(testDoctor)
                .specialty(specialtyToRemove)
                .build();
        
        DoctorSpecialty doctorSpecialtyToKeep = DoctorSpecialty.builder()
                .doctor(testDoctor)
                .specialty(specialtyToKeep)
                .build();
        
        testDoctor.getDoctorSpecialties().add(doctorSpecialtyToRemove);
        testDoctor.getDoctorSpecialties().add(doctorSpecialtyToKeep);
        
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(testDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);

        // Act
        doctorManagementService.removeSpecialtyFromDoctor(DOCTOR_ID, SPECIALTY_ID);

        // Assert
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, times(1)).save(testDoctor);
        
        assertEquals(1, testDoctor.getDoctorSpecialties().size());
        assertEquals(specialtyToKeep.getId(), testDoctor.getDoctorSpecialties().get(0).getSpecialty().getId());
    }

    @Test
    void removeSpecialtyFromDoctor_DoctorNotFound() {
        // Arrange
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorManagementService.removeSpecialtyFromDoctor(DOCTOR_ID, SPECIALTY_ID);
        });

        assertEquals("Doctor not found: " + DOCTOR_ID, exception.getMessage());
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void removeSpecialtyFromDoctor_OnlyOneSpecialty() {
        // Arrange
        // Add only one specialty to the doctor
        DoctorSpecialty doctorSpecialty = DoctorSpecialty.builder()
                .doctor(testDoctor)
                .specialty(testSpecialty)
                .build();
        
        testDoctor.getDoctorSpecialties().add(doctorSpecialty);
        
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(testDoctor));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorManagementService.removeSpecialtyFromDoctor(DOCTOR_ID, SPECIALTY_ID);
        });

        assertEquals("Doctor must have at least one specialty", exception.getMessage());
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void removeSpecialtyFromDoctor_SpecialtyNotFound() {
        // Arrange
        // Add two specialties to the doctor but not the one we're trying to remove
        Specialty specialty1 = Specialty.builder().id(2L).name("Neurology").build();
        Specialty specialty2 = Specialty.builder().id(3L).name("Pediatrics").build();
        
        DoctorSpecialty doctorSpecialty1 = DoctorSpecialty.builder()
                .doctor(testDoctor)
                .specialty(specialty1)
                .build();
        
        DoctorSpecialty doctorSpecialty2 = DoctorSpecialty.builder()
                .doctor(testDoctor)
                .specialty(specialty2)
                .build();
        
        testDoctor.getDoctorSpecialties().add(doctorSpecialty1);
        testDoctor.getDoctorSpecialties().add(doctorSpecialty2);
        
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(testDoctor));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorManagementService.removeSpecialtyFromDoctor(DOCTOR_ID, SPECIALTY_ID);
        });

        assertEquals("Doctor does not have specialty: " + SPECIALTY_ID, exception.getMessage());
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void addWorkSchedule_Success() {
        // Arrange
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(testDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);

        // Act
        doctorManagementService.addWorkSchedule(DOCTOR_ID, testSchedule);

        // Assert
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, times(1)).save(testDoctor);
        
        assertEquals(1, testDoctor.getWorkSchedules().size());
        assertTrue(testDoctor.getWorkSchedules().contains(testSchedule));
        assertEquals(testDoctor, testSchedule.getDoctor());
    }

    @Test
    void addWorkSchedule_DoctorNotFound() {
        // Arrange
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorManagementService.addWorkSchedule(DOCTOR_ID, testSchedule);
        });

        assertEquals("Doctor not found: " + DOCTOR_ID, exception.getMessage());
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void addWorkSchedule_AlreadyHasScheduleForDay() {
        // Arrange
        // Add a schedule for the same day
        DoctorSchedule existingSchedule = DoctorSchedule.builder()
                .id(2L)
                .dayOfWeek(testSchedule.getDayOfWeek())  // Same day as testSchedule
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .active(true)
                .build();
        
        testDoctor.getWorkSchedules().add(existingSchedule);
        
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(testDoctor));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorManagementService.addWorkSchedule(DOCTOR_ID, testSchedule);
        });

        assertEquals("Doctor already has active schedule for " + testSchedule.getDayOfWeek(), exception.getMessage());
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void updateWorkSchedule_Success() {
        // Arrange
        // Add the schedule to the doctor's list
        testSchedule.setDoctor(testDoctor);
        testDoctor.getWorkSchedules().add(testSchedule);
        
        // Create updated schedule with same ID
        DoctorSchedule updatedSchedule = DoctorSchedule.builder()
                .id(SCHEDULE_ID)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))  // Changed
                .endTime(LocalTime.of(18, 0))    // Changed
                .slotDurationMinutes(45)         // Changed
                .active(true)
                .location("Room 202")            // Changed
                .build();
        
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(testDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);

        // Act
        doctorManagementService.updateWorkSchedule(DOCTOR_ID, updatedSchedule);

        // Assert
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, times(1)).save(testDoctor);
        
        // Verify the schedule was updated
        DoctorSchedule resultSchedule = testDoctor.getWorkSchedules().iterator().next();
        assertEquals(LocalTime.of(10, 0), resultSchedule.getStartTime());
        assertEquals(LocalTime.of(18, 0), resultSchedule.getEndTime());
        assertEquals(45, resultSchedule.getSlotDurationMinutes());
        assertEquals("Room 202", resultSchedule.getLocation());
    }

    @Test
    void updateWorkSchedule_DoctorNotFound() {
        // Arrange
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorManagementService.updateWorkSchedule(DOCTOR_ID, testSchedule);
        });

        assertEquals("Doctor not found: " + DOCTOR_ID, exception.getMessage());
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void updateWorkSchedule_ScheduleNotFound() {
        // Arrange
        // Doctor has no schedules
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(testDoctor));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorManagementService.updateWorkSchedule(DOCTOR_ID, testSchedule);
        });

        assertEquals("Schedule not found: " + SCHEDULE_ID, exception.getMessage());
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void removeWorkSchedule_Success() {
        // Arrange
        // Add the schedule to the doctor's list
        testSchedule.setDoctor(testDoctor);
        testDoctor.getWorkSchedules().add(testSchedule);
        
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(testDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);

        // Act
        doctorManagementService.removeWorkSchedule(DOCTOR_ID, SCHEDULE_ID);

        // Assert
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, times(1)).save(testDoctor);
        
        assertTrue(testDoctor.getWorkSchedules().isEmpty());
    }

    @Test
    void removeWorkSchedule_DoctorNotFound() {
        // Arrange
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorManagementService.removeWorkSchedule(DOCTOR_ID, SCHEDULE_ID);
        });

        assertEquals("Doctor not found: " + DOCTOR_ID, exception.getMessage());
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void removeWorkSchedule_ScheduleNotFound() {
        // Arrange
        // Doctor has no schedules
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(testDoctor));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorManagementService.removeWorkSchedule(DOCTOR_ID, SCHEDULE_ID);
        });

        assertEquals("Schedule not found: " + SCHEDULE_ID, exception.getMessage());
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void toggleScheduleStatus_Success() {
        // Arrange
        // Add the schedule to the doctor's list
        testSchedule.setDoctor(testDoctor);
        testDoctor.getWorkSchedules().add(testSchedule);
        
        boolean newActiveStatus = false;
        
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(testDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);

        // Act
        doctorManagementService.toggleScheduleStatus(DOCTOR_ID, SCHEDULE_ID, newActiveStatus);

        // Assert
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, times(1)).save(testDoctor);
        
        DoctorSchedule resultSchedule = testDoctor.getWorkSchedules().iterator().next();
        assertEquals(newActiveStatus, resultSchedule.isActive());
    }

    @Test
    void deleteDoctor_Success() {
        // Arrange
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(testDoctor));
        doNothing().when(doctorRepository).delete(testDoctor);

        // Act
        doctorManagementService.deleteDoctor(DOCTOR_ID);

        // Assert
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, times(1)).delete(testDoctor);
    }

    @Test
    void deleteDoctor_DoctorNotFound() {
        // Arrange
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorManagementService.deleteDoctor(DOCTOR_ID);
        });

        assertEquals("Doctor not found: " + DOCTOR_ID, exception.getMessage());
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, never()).delete(any());
    }

    @Test
    void updateDoctorProfile_Success() {
        // Arrange
        String newFirstName = "Jane";
        String newLastName = "Smith";
        String newPhone = "9876543210";
        String newProfilePicture = "http://example.com/new-profile.jpg";
        
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(testDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);

        // Act
        doctorManagementService.updateDoctorProfile(DOCTOR_ID, newFirstName, newLastName, 
                                                  newPhone, newProfilePicture);

        // Assert
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, times(1)).save(testDoctor);
        
        assertEquals(newFirstName, testDoctor.getFirstName());
        assertEquals(newLastName, testDoctor.getLastName());
        assertEquals(newPhone, testDoctor.getPhone());
        assertEquals(newProfilePicture, testDoctor.getProfilePicture());
    }

    @Test
    void updateDoctorProfile_PartialUpdate() {
        // Arrange
        String newFirstName = "Jane";
        // Other fields null - should not be updated
        
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(testDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);

        // Record original values
        String originalLastName = testDoctor.getLastName();
        String originalPhone = testDoctor.getPhone();
        String originalProfilePicture = testDoctor.getProfilePicture();

        // Act
        doctorManagementService.updateDoctorProfile(DOCTOR_ID, newFirstName, null, null, null);

        // Assert
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, times(1)).save(testDoctor);
        
        assertEquals(newFirstName, testDoctor.getFirstName());
        // Other fields should remain unchanged
        assertEquals(originalLastName, testDoctor.getLastName());
        assertEquals(originalPhone, testDoctor.getPhone());
        assertEquals(originalProfilePicture, testDoctor.getProfilePicture());
    }

    @Test
    void updateDoctorProfile_DoctorNotFound() {
        // Arrange
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorManagementService.updateDoctorProfile(DOCTOR_ID, "Jane", "Smith", "9876543210", null);
        });

        assertEquals("Doctor not found: " + DOCTOR_ID, exception.getMessage());
        verify(doctorRepository, times(1)).findById(DOCTOR_ID);
        verify(doctorRepository, never()).save(any());
    }
}