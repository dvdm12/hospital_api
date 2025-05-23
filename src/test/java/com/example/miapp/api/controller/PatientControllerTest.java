package com.example.miapp.api.controller;

import com.example.miapp.dto.PatientDto;
import com.example.miapp.controller.PatientController;
import com.example.miapp.services.PatientService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PatientControllerTest {

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientController patientController;

    private AutoCloseable closeable;

    private PatientDto samplePatient;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        samplePatient = PatientDto.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .birthDate(new GregorianCalendar(1990, Calendar.MARCH, 15).getTime())
                .phone("1234567890")
                .address("123 Calle Falsa")
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close(); // Evita leaks de recursos (SpotBugs friendly)
    }

    @Test
    void testGetAllPatients() {
        List<PatientDto> patients = List.of(samplePatient);
        when(patientService.getAllPatients()).thenReturn(patients);

        ResponseEntity<List<PatientDto>> response = patientController.getAllPatients();

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(samplePatient, response.getBody().get(0));
        verify(patientService, times(1)).getAllPatients();
    }

    @Test
    void testGetPatientById() {
        when(patientService.getPatientById(1L)).thenReturn(samplePatient);

        ResponseEntity<PatientDto> response = patientController.getPatientById(1L);

        assertEquals(200, response.getStatusCode());
        assertEquals(samplePatient, response.getBody());
        verify(patientService).getPatientById(1L);
    }

    @Test
    void testCreatePatient() {
        when(patientService.savePatient(samplePatient)).thenReturn(samplePatient);

        ResponseEntity<PatientDto> response = patientController.createPatient(samplePatient);

        assertEquals(200, response.getStatusCode());
        assertEquals(samplePatient, response.getBody());
        verify(patientService).savePatient(samplePatient);
    }

    @Test
    void testUpdatePatient() {
        when(patientService.updatePatient(1L, samplePatient)).thenReturn(samplePatient);

        ResponseEntity<PatientDto> response = patientController.updatePatient(1L, samplePatient);

        assertEquals(200, response.getStatusCode());
        assertEquals(samplePatient, response.getBody());
        verify(patientService).updatePatient(1L, samplePatient);
    }

    @Test
    void testDeletePatient() {
        doNothing().when(patientService).deletePatient(1L);

        ResponseEntity<Void> response = patientController.deletePatient(1L);

        assertEquals(204, response.getStatusCode());
        verify(patientService).deletePatient(1L);
    }

    @Test
    void testHandleEntityNotFoundException() {
        String errorMessage = "Patient not found";
        EntityNotFoundException exception = new EntityNotFoundException(errorMessage);

        ResponseEntity<String> response = patientController.handleEntityNotFoundException(exception);

        assertEquals(404, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }
}
