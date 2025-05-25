package com.example.miapp.service.doctor;

import com.example.miapp.dto.doctor.CreateDoctorRequest;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.DoctorSchedule;
import com.example.miapp.exception.DoctorValidationException;
import com.example.miapp.repository.DoctorRepository;
import com.example.miapp.repository.SpecialtyRepository;
import com.example.miapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorValidationServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpecialtyRepository specialtyRepository;

    @InjectMocks
    private DoctorValidationService validationService;

    // Test fixtures
    private CreateDoctorRequest validRequest;
    private Doctor existingDoctor;
    private DoctorSchedule validSchedule;

    @BeforeEach
    void setUp() {
        // Setup valid doctor request
        validRequest = new CreateDoctorRequest();
        validRequest.setFirstName("John");
        validRequest.setLastName("Smith");
        validRequest.setEmail("john.smith@example.com");
        validRequest.setLicenseNumber("MED12345");
        validRequest.setUsername("jsmith");
        validRequest.setPassword("password123");
        validRequest.setPhone("+1234567890");
        validRequest.setConsultationFee(100.0);
        validRequest.setSpecialtyIds(Set.of(1L, 2L));

        // Setup existing doctor
        existingDoctor = new Doctor();
        existingDoctor.setId(1L);
        existingDoctor.setEmail("existing@example.com");
        existingDoctor.setLicenseNumber("EXISTING123");

        // Setup valid schedule
        validSchedule = DoctorSchedule.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .slotDurationMinutes(30)
                .build();
    }

    @Nested
    @DisplayName("Doctor Creation Validation Tests")
    class DoctorCreationValidationTests {

        @Test
        @DisplayName("Valid doctor creation request should not throw exception")
        void validateDoctorCreation_WithValidRequest_ShouldNotThrowException() {
            // Arrange
            when(doctorRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(doctorRepository.findByLicenseNumber(anyString())).thenReturn(Optional.empty());
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(specialtyRepository.existsById(anyLong())).thenReturn(true);
            
            // Act & Assert
            assertDoesNotThrow(() -> validationService.validateDoctorCreation(validRequest));
        }

        @Test
        @DisplayName("Doctor creation with existing email should throw exception")
        void validateDoctorCreation_WithExistingEmail_ShouldThrowException() {
            // Arrange
            when(doctorRepository.findByEmail(validRequest.getEmail())).thenReturn(Optional.of(existingDoctor));
            
            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorCreation(validRequest));
            assertEquals("Email already exists: " + validRequest.getEmail(), exception.getMessage());
        }

        @Test
        @DisplayName("Doctor creation with existing license number should throw exception")
        void validateDoctorCreation_WithExistingLicenseNumber_ShouldThrowException() {
            // Arrange
            when(doctorRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(doctorRepository.findByLicenseNumber(validRequest.getLicenseNumber())).thenReturn(Optional.of(existingDoctor));
            
            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorCreation(validRequest));
            assertEquals("License number already exists: " + validRequest.getLicenseNumber(), exception.getMessage());
        }

        @Test
        @DisplayName("Doctor creation with existing username should throw exception")
        void validateDoctorCreation_WithExistingUsername_ShouldThrowException() {
            // Arrange
            when(doctorRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(doctorRepository.findByLicenseNumber(anyString())).thenReturn(Optional.empty());
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(true);
            
            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorCreation(validRequest));
            assertEquals("Username already exists: " + validRequest.getUsername(), exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(doubles = {-50.0, -1.0, -0.01})
        @DisplayName("Doctor creation with negative consultation fee should throw exception")
        void validateDoctorCreation_WithNegativeConsultationFee_ShouldThrowException(double negativeFee) {
            // Arrange - Usando lenient() para evitar UnnecessaryStubbing en pruebas parametrizadas
            // Esta configuración no se usa en la validación de tarifas negativas, pero podría ser necesaria
            // para otras validaciones que se ejecutan primero en validateDoctorCreation
            lenient().when(specialtyRepository.existsById(anyLong())).thenReturn(true);
            
            CreateDoctorRequest request = createDoctorRequestWithFee(negativeFee);
            
            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorCreation(request));
            assertEquals("Consultation fee cannot be negative", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(doubles = {10001.0, 15000.0, 20000.0})
        @DisplayName("Doctor creation with very high consultation fee should throw exception")
        void validateDoctorCreation_WithVeryHighConsultationFee_ShouldThrowException(double highFee) {
            // Arrange - Usando lenient() para evitar UnnecessaryStubbing en pruebas parametrizadas
            lenient().when(specialtyRepository.existsById(anyLong())).thenReturn(true);
            
            CreateDoctorRequest request = createDoctorRequestWithFee(highFee);
            
            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorCreation(request));
            assertEquals("Consultation fee seems too high: " + highFee, exception.getMessage());
        }

        @Test
        @DisplayName("Doctor creation with no specialties should throw exception")
        void validateDoctorCreation_WithNoSpecialties_ShouldThrowException() {
            // Arrange
            validRequest.setSpecialtyIds(Collections.emptySet());
            
            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorCreation(validRequest));
            assertEquals("Doctor must have at least one specialty", exception.getMessage());
        }

        @Test
        @DisplayName("Doctor creation with null specialties should throw exception")
        void validateDoctorCreation_WithNullSpecialties_ShouldThrowException() {
            // Arrange
            validRequest.setSpecialtyIds(null);
            
            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorCreation(validRequest));
            assertEquals("Doctor must have at least one specialty", exception.getMessage());
        }

        @Test
        @DisplayName("Doctor creation with non-existent specialty should throw exception")
        void validateDoctorCreation_WithNonExistentSpecialty_ShouldThrowException() {
            // Arrange
            Long nonExistentSpecialtyId = 999L;
            validRequest.setSpecialtyIds(Set.of(nonExistentSpecialtyId));
            when(specialtyRepository.existsById(nonExistentSpecialtyId)).thenReturn(false);
            
            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorCreation(validRequest));
            assertEquals("Specialty not found with ID: " + nonExistentSpecialtyId, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Doctor Update Validation Tests")
    class DoctorUpdateValidationTests {

        private final Long doctorId = 1L;

        @Test
        @DisplayName("Valid doctor update should not throw exception")
        void validateDoctorUpdate_WithValidRequest_ShouldNotThrowException() {
            // Arrange
            when(doctorRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(doctorRepository.findByLicenseNumber(anyString())).thenReturn(Optional.empty());
            when(specialtyRepository.existsById(anyLong())).thenReturn(true);
            
            // Act & Assert
            assertDoesNotThrow(() -> validationService.validateDoctorUpdate(doctorId, validRequest));
        }

        @Test
        @DisplayName("Doctor update with existing email (different doctor) should throw exception")
        void validateDoctorUpdate_WithExistingEmailDifferentDoctor_ShouldThrowException() {
            // Arrange
            Doctor otherDoctor = new Doctor();
            otherDoctor.setId(2L);
            when(doctorRepository.findByEmail(validRequest.getEmail())).thenReturn(Optional.of(otherDoctor));
            
            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorUpdate(doctorId, validRequest));
            assertEquals("Email already exists: " + validRequest.getEmail(), exception.getMessage());
        }

        @Test
        @DisplayName("Doctor update with existing email (same doctor) should not throw exception")
        void validateDoctorUpdate_WithExistingEmailSameDoctor_ShouldNotThrowException() {
            // Arrange
            Doctor sameDoctor = new Doctor();
            sameDoctor.setId(doctorId);
            when(doctorRepository.findByEmail(validRequest.getEmail())).thenReturn(Optional.of(sameDoctor));
            when(doctorRepository.findByLicenseNumber(anyString())).thenReturn(Optional.empty());
            when(specialtyRepository.existsById(anyLong())).thenReturn(true);
            
            // Act & Assert
            assertDoesNotThrow(() -> validationService.validateDoctorUpdate(doctorId, validRequest));
        }

        @Test
        @DisplayName("Doctor update with existing license (different doctor) should throw exception")
        void validateDoctorUpdate_WithExistingLicenseDifferentDoctor_ShouldThrowException() {
            // Arrange
            Doctor otherDoctor = new Doctor();
            otherDoctor.setId(2L);
            when(doctorRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(doctorRepository.findByLicenseNumber(validRequest.getLicenseNumber())).thenReturn(Optional.of(otherDoctor));
            
            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorUpdate(doctorId, validRequest));
            assertEquals("License number already exists: " + validRequest.getLicenseNumber(), exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(doubles = {11000.0, 12000.0, 15000.0})
        @DisplayName("Doctor update with high consultation fee should throw exception")
        void validateDoctorUpdate_WithHighConsultationFee_ShouldThrowException(double highFee) {
            // Arrange - Usando lenient() para evitar UnnecessaryStubbing en pruebas parametrizadas
            lenient().when(specialtyRepository.existsById(anyLong())).thenReturn(true);
            
            CreateDoctorRequest request = createDoctorRequestWithFee(highFee);
            
            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorUpdate(doctorId, request));
            assertEquals("Consultation fee seems too high: " + highFee, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Doctor Schedule Validation Tests")
    class DoctorScheduleValidationTests {

        @Test
        @DisplayName("Valid schedule should not throw exception")
        void validateDoctorSchedule_WithValidSchedule_ShouldNotThrowException() {
            // Act & Assert
            assertDoesNotThrow(() -> validationService.validateDoctorSchedule(validSchedule));
        }

        @Test
        @DisplayName("Schedule with start time after end time should throw exception")
        void validateDoctorSchedule_WithStartAfterEnd_ShouldThrowException() {
            // Arrange
            DoctorSchedule invalidSchedule = DoctorSchedule.builder()
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(17, 0))
                    .endTime(LocalTime.of(9, 0))
                    .slotDurationMinutes(30)
                    .build();

            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorSchedule(invalidSchedule));
            assertEquals("Start time cannot be after end time", exception.getMessage());
        }

        @Test
        @DisplayName("Schedule with equal start and end time should throw exception")
        void validateDoctorSchedule_WithEqualStartAndEnd_ShouldThrowException() {
            // Arrange
            LocalTime sameTime = LocalTime.of(9, 0);
            DoctorSchedule invalidSchedule = DoctorSchedule.builder()
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(sameTime)
                    .endTime(sameTime)
                    .slotDurationMinutes(30)
                    .build();

            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorSchedule(invalidSchedule));
            assertEquals("Start time cannot equal end time", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -30})
        @DisplayName("Schedule with non-positive slot duration should throw exception")
        void validateDoctorSchedule_WithNonPositiveSlotDuration_ShouldThrowException(int duration) {
            // Arrange
            DoctorSchedule invalidSchedule = DoctorSchedule.builder()
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .slotDurationMinutes(duration)
                    .build();

            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorSchedule(invalidSchedule));
            assertEquals("Slot duration must be positive", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(ints = {481, 500, 600})
        @DisplayName("Schedule with excessive slot duration should throw exception")
        void validateDoctorSchedule_WithExcessiveSlotDuration_ShouldThrowException(int duration) {
            // Arrange
            DoctorSchedule invalidSchedule = DoctorSchedule.builder()
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .slotDurationMinutes(duration)
                    .build();

            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorSchedule(invalidSchedule));
            assertEquals("Slot duration cannot exceed 8 hours", exception.getMessage());
        }

        @Test
        @DisplayName("Schedule with slot duration greater than work period should throw exception")
        void validateDoctorSchedule_WithSlotDurationGreaterThanWorkPeriod_ShouldThrowException() {
            // Arrange
            DoctorSchedule invalidSchedule = DoctorSchedule.builder()
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(10, 0))
                    .slotDurationMinutes(90) // 1.5 hours
                    .build();

            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorSchedule(invalidSchedule));
            assertEquals("Slot duration cannot be longer than work period: 60 minutes", exception.getMessage());
        }

        @Test
        @DisplayName("Schedule with null day of week should throw exception")
        void validateDoctorSchedule_WithNullDayOfWeek_ShouldThrowException() {
            // Arrange
            DoctorSchedule invalidSchedule = DoctorSchedule.builder()
                    .dayOfWeek(null)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .slotDurationMinutes(30)
                    .build();

            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorSchedule(invalidSchedule));
            assertEquals("Day of week is required for schedule", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Doctor Deletion Validation Tests")
    class DoctorDeletionValidationTests {

        private Doctor doctor;

        @BeforeEach
        void setUp() {
            doctor = new Doctor();
            doctor.setId(1L);
            doctor.setFirstName("John");
            doctor.setLastName("Smith");
            
            // By default, the doctor has no appointments or prescriptions
            doctor.setAppointments(new ArrayList<>());
            doctor.setPrescriptions(new ArrayList<>());
        }

        @Test
        @DisplayName("Doctor with no active appointments or prescriptions can be deleted")
        void validateDoctorDeletion_WithNoActiveAppointmentsOrPrescriptions_ShouldNotThrowException() {
            // Act & Assert
            assertDoesNotThrow(() -> validationService.validateDoctorDeletion(doctor));
        }

        @Test
        @DisplayName("Doctor with active appointments cannot be deleted")
        void validateDoctorDeletion_WithActiveAppointments_ShouldThrowException() {
            // Arrange - Create a mock appointment with SCHEDULED status
            com.example.miapp.entity.Appointment activeAppointment = new com.example.miapp.entity.Appointment();
            activeAppointment.setStatus(com.example.miapp.entity.Appointment.AppointmentStatus.SCHEDULED);
            doctor.setAppointments(List.of(activeAppointment));

            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorDeletion(doctor));
            assertEquals("Cannot delete doctor with active appointments", exception.getMessage());
        }

        @Test
        @DisplayName("Doctor with active prescriptions cannot be deleted")
        void validateDoctorDeletion_WithActivePrescriptions_ShouldThrowException() {
            // Arrange - Create a mock prescription with ACTIVE status
            com.example.miapp.entity.Prescription activePrescription = new com.example.miapp.entity.Prescription();
            activePrescription.setStatus(com.example.miapp.entity.Prescription.PrescriptionStatus.ACTIVE);
            doctor.setPrescriptions(List.of(activePrescription));

            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateDoctorDeletion(doctor));
            assertEquals("Cannot delete doctor with active prescriptions", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Consultation Fee Update Validation Tests")
    class ConsultationFeeUpdateValidationTests {

        @Test
        @DisplayName("Valid consultation fee update should not throw exception")
        void validateConsultationFeeUpdate_WithValidFee_ShouldNotThrowException() {
            // Act & Assert
            assertDoesNotThrow(() -> validationService.validateConsultationFeeUpdate(100.0));
        }

        @ParameterizedTest
        @ValueSource(doubles = {-100.0, -50.0, -0.01})
        @DisplayName("Negative consultation fee should throw exception")
        void validateConsultationFeeUpdate_WithNegativeFee_ShouldThrowException(double negativeFee) {
            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateConsultationFeeUpdate(negativeFee));
            assertEquals("Consultation fee cannot be negative", exception.getMessage());
        }

        @Test
        @DisplayName("Zero consultation fee should not throw exception")
        void validateConsultationFeeUpdate_WithZeroFee_ShouldNotThrowException() {
            // Act & Assert
            assertDoesNotThrow(() -> validationService.validateConsultationFeeUpdate(0.0));
        }

        @ParameterizedTest
        @ValueSource(doubles = {10001.0, 15000.0, 25000.0})
        @DisplayName("Very high consultation fee should throw exception")
        void validateConsultationFeeUpdate_WithVeryHighFee_ShouldThrowException(double highFee) {
            // Act & Assert
            DoctorValidationException exception = assertThrows(DoctorValidationException.class,
                    () -> validationService.validateConsultationFeeUpdate(highFee));
            assertEquals("Consultation fee seems too high: " + highFee, exception.getMessage());
        }

        @Test
        @DisplayName("Null consultation fee should not throw exception")
        void validateConsultationFeeUpdate_WithNullFee_ShouldNotThrowException() {
            // Act & Assert
            assertDoesNotThrow(() -> validationService.validateConsultationFeeUpdate(null));
        }
    }

    // Test helper methods
    private CreateDoctorRequest createDoctorRequestWithFee(Double fee) {
        CreateDoctorRequest request = new CreateDoctorRequest();
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setEmail("john.smith@example.com");
        request.setLicenseNumber("MED12345");
        request.setUsername("jsmith");
        request.setPassword("password123");
        request.setConsultationFee(fee);
        request.setSpecialtyIds(Set.of(1L));
        return request;
    }
}