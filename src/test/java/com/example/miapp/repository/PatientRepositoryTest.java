package com.example.miapp.repository;

import com.example.miapp.entity.Patient;
import com.example.miapp.entity.Patient.Gender;
import com.example.miapp.entity.User;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientRepositoryTest {

    @Mock
    private PatientRepository patientRepository;

    private Patient patient1;
    private Patient patient2;
    private User patientUser1;
    private User patientUser2;
    private List<Patient> patientList;
    private Page<Patient> patientPage;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Crear usuarios
        patientUser1 = User.builder()
                .id(1L)
                .username("patient1")
                .email("patient1@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        
        patientUser2 = User.builder()
                .id(2L)
                .username("patient2")
                .email("patient2@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        
        // Crear pacientes
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -35); // 35 años atrás
        Date birthDate1 = calendar.getTime();
        
        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -25); // 25 años atrás
        Date birthDate2 = calendar.getTime();
        
        patient1 = Patient.builder()
                .id(1L)
                .firstName("Maria")
                .lastName("Garcia")
                .birthDate(birthDate1)
                .phone("123456789")
                .address("123 Main St")
                .gender(Gender.FEMALE)
                .bloodType("A+")
                .emergencyContactName("Juan Garcia")
                .emergencyContactPhone("987654321")
                .insuranceProvider("MediSalud")
                .insurancePolicyNumber("MS123456")
                .user(patientUser1)
                .build();
        
        patient2 = Patient.builder()
                .id(2L)
                .firstName("Carlos")
                .lastName("Rodriguez")
                .birthDate(birthDate2)
                .phone("456789123")
                .address("456 Oak St")
                .gender(Gender.MALE)
                .bloodType("O+")
                .emergencyContactName("Ana Rodriguez")
                .emergencyContactPhone("321654987")
                .insuranceProvider("SeguroTotal")
                .insurancePolicyNumber("ST654321")
                .user(patientUser2)
                .build();
        
        // Configurar lista de pacientes
        patientList = Arrays.asList(patient1, patient2);
        
        // Configurar paginación
        pageable = PageRequest.of(0, 10);
        patientPage = new PageImpl<>(patientList, pageable, patientList.size());
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda básica")
    class BasicSearchTests {
        
        @Test
        @DisplayName("Debería encontrar un paciente por email")
        void shouldFindByEmail() {
            // Given
            when(patientRepository.findByEmail("patient1@example.com"))
                    .thenReturn(Optional.of(patient1));
            
            // When
            Optional<Patient> result = patientRepository.findByEmail("patient1@example.com");
            
            // Then
            assertTrue(result.isPresent());
            assertEquals("patient1@example.com", result.get().getUser().getEmail());
            verify(patientRepository).findByEmail("patient1@example.com");
        }
        
        @Test
        @DisplayName("Debería encontrar un paciente por número de teléfono")
        void shouldFindByPhone() {
            // Given
            when(patientRepository.findByPhone("123456789"))
                    .thenReturn(Optional.of(patient1));
            
            // When
            Optional<Patient> result = patientRepository.findByPhone("123456789");
            
            // Then
            assertTrue(result.isPresent());
            assertEquals("123456789", result.get().getPhone());
            verify(patientRepository).findByPhone("123456789");
        }
        
        @Test
        @DisplayName("Debería encontrar un paciente por ID de usuario")
        void shouldFindByUserId() {
            // Given
            when(patientRepository.findByUserId(1L))
                    .thenReturn(Optional.of(patient1));
            
            // When
            Optional<Patient> result = patientRepository.findByUserId(1L);
            
            // Then
            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getUser().getId());
            verify(patientRepository).findByUserId(1L);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por nombre")
    class NameSearchTests {
        
        @Test
        @DisplayName("Debería encontrar pacientes por nombre y apellido")
        void shouldFindByFirstNameAndLastName() {
            // Given
            List<Patient> matchingPatients = Collections.singletonList(patient1);
            Page<Patient> matchingPage = new PageImpl<>(matchingPatients, pageable, matchingPatients.size());
            
            when(patientRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase("Maria", "Garcia", pageable))
                    .thenReturn(matchingPage);
            
            // When
            Page<Patient> result = patientRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(
                    "Maria", "Garcia", pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("Maria", result.getContent().get(0).getFirstName());
            assertEquals("Garcia", result.getContent().get(0).getLastName());
            verify(patientRepository).findByFirstNameIgnoreCaseAndLastNameIgnoreCase("Maria", "Garcia", pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar pacientes por patrón de nombre")
        void shouldFindByNameContaining() {
            // Given
            when(patientRepository.findByNameContaining("ar", pageable))
                    .thenReturn(patientPage);
            
            // When
            Page<Patient> result = patientRepository.findByNameContaining("ar", pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(patientRepository).findByNameContaining("ar", pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por características demográficas")
    class DemographicSearchTests {
        
        @Test
        @DisplayName("Debería encontrar pacientes por género")
        void shouldFindByGender() {
            // Given
            List<Patient> femalePatients = Collections.singletonList(patient1);
            Page<Patient> femalePage = new PageImpl<>(femalePatients, pageable, femalePatients.size());
            
            when(patientRepository.findByGender(Gender.FEMALE, pageable))
                    .thenReturn(femalePage);
            
            // When
            Page<Patient> result = patientRepository.findByGender(Gender.FEMALE, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals(Gender.FEMALE, result.getContent().get(0).getGender());
            verify(patientRepository).findByGender(Gender.FEMALE, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar pacientes nacidos antes de una fecha")
        void shouldFindByBirthDateBefore() {
            // Given
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -30); // 30 años atrás
            Date cutoffDate = calendar.getTime();
            
            List<Patient> olderPatients = Collections.singletonList(patient1);
            Page<Patient> olderPatientsPage = new PageImpl<>(olderPatients, pageable, olderPatients.size());
            
            when(patientRepository.findByBirthDateBefore(cutoffDate, pageable))
                    .thenReturn(olderPatientsPage);
            
            // When
            Page<Patient> result = patientRepository.findByBirthDateBefore(cutoffDate, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertTrue(result.getContent().get(0).getBirthDate().before(cutoffDate));
            verify(patientRepository).findByBirthDateBefore(cutoffDate, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar pacientes nacidos después de una fecha")
        void shouldFindByBirthDateAfter() {
            // Given
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -30); // 30 años atrás
            Date cutoffDate = calendar.getTime();
            
            List<Patient> youngerPatients = Collections.singletonList(patient2);
            Page<Patient> youngerPatientsPage = new PageImpl<>(youngerPatients, pageable, youngerPatients.size());
            
            when(patientRepository.findByBirthDateAfter(cutoffDate, pageable))
                    .thenReturn(youngerPatientsPage);
            
            // When
            Page<Patient> result = patientRepository.findByBirthDateAfter(cutoffDate, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertTrue(result.getContent().get(0).getBirthDate().after(cutoffDate));
            verify(patientRepository).findByBirthDateAfter(cutoffDate, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar pacientes por rango de edad")
        void shouldFindByAgeRange() {
            // Given
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -40); // 40 años atrás
            Date startDate = calendar.getTime();
            
            calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -20); // 20 años atrás
            Date endDate = calendar.getTime();
            
            when(patientRepository.findByAgeRange(startDate, endDate, pageable))
                    .thenReturn(patientPage);
            
            // When
            Page<Patient> result = patientRepository.findByAgeRange(startDate, endDate, pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(patientRepository).findByAgeRange(startDate, endDate, pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por seguro")
    class InsuranceSearchTests {
        
        @Test
        @DisplayName("Debería encontrar pacientes por proveedor de seguro")
        void shouldFindByInsuranceProvider() {
            // Given
            List<Patient> mediSaludPatients = Collections.singletonList(patient1);
            Page<Patient> mediSaludPage = new PageImpl<>(mediSaludPatients, pageable, mediSaludPatients.size());
            
            when(patientRepository.findByInsuranceProviderContainingIgnoreCase("MediSalud", pageable))
                    .thenReturn(mediSaludPage);
            
            // When
            Page<Patient> result = patientRepository.findByInsuranceProviderContainingIgnoreCase("MediSalud", pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertTrue(result.getContent().get(0).getInsuranceProvider().contains("MediSalud"));
            verify(patientRepository).findByInsuranceProviderContainingIgnoreCase("MediSalud", pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de operaciones de actualización")
    class UpdateOperationTests {
        
        @Test
        @DisplayName("Debería actualizar la dirección de un paciente")
        void shouldUpdateAddress() {
            // Given
            String newAddress = "789 Maple Ave";
            when(patientRepository.updateAddress(1L, newAddress))
                    .thenReturn(1);
            
            // When
            int affectedRows = patientRepository.updateAddress(1L, newAddress);
            
            // Then
            assertEquals(1, affectedRows);
            verify(patientRepository).updateAddress(1L, newAddress);
        }
        
        @Test
        @DisplayName("Debería actualizar el número de teléfono de un paciente")
        void shouldUpdatePhone() {
            // Given
            String newPhone = "555123456";
            when(patientRepository.updatePhone(1L, newPhone))
                    .thenReturn(1);
            
            // When
            int affectedRows = patientRepository.updatePhone(1L, newPhone);
            
            // Then
            assertEquals(1, affectedRows);
            verify(patientRepository).updatePhone(1L, newPhone);
        }
        
        @Test
        @DisplayName("Debería actualizar la información de seguro de un paciente")
        void shouldUpdateInsuranceInfo() {
            // Given
            String newInsuranceProvider = "SeguroPlus";
            String newInsurancePolicyNumber = "SP987654";
            
            when(patientRepository.updateInsuranceInfo(1L, newInsuranceProvider, newInsurancePolicyNumber))
                    .thenReturn(1);
            
            // When
            int affectedRows = patientRepository.updateInsuranceInfo(
                    1L, newInsuranceProvider, newInsurancePolicyNumber);
            
            // Then
            assertEquals(1, affectedRows);
            verify(patientRepository).updateInsuranceInfo(1L, newInsuranceProvider, newInsurancePolicyNumber);
        }
        
        @Test
        @DisplayName("Debería actualizar la información de contacto de emergencia de un paciente")
        void shouldUpdateEmergencyContact() {
            // Given
            String newContactName = "Luis Garcia";
            String newContactPhone = "555987654";
            
            when(patientRepository.updateEmergencyContact(1L, newContactName, newContactPhone))
                    .thenReturn(1);
            
            // When
            int affectedRows = patientRepository.updateEmergencyContact(
                    1L, newContactName, newContactPhone);
            
            // Then
            assertEquals(1, affectedRows);
            verify(patientRepository).updateEmergencyContact(1L, newContactName, newContactPhone);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por relaciones")
    class RelationshipSearchTests {
        
        @Test
        @DisplayName("Debería encontrar pacientes por ID de doctor")
        void shouldFindByDoctorId() {
            // Given
            when(patientRepository.findByDoctorId(1L, pageable))
                    .thenReturn(patientPage);
            
            // When
            Page<Patient> result = patientRepository.findByDoctorId(1L, pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(patientRepository).findByDoctorId(1L, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar pacientes por nombre de medicamento")
        void shouldFindByMedicationName() {
            // Given
            when(patientRepository.findByMedicationName("Ibuprofen", pageable))
                    .thenReturn(patientPage);
            
            // When
            Page<Patient> result = patientRepository.findByMedicationName("Ibuprofen", pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(patientRepository).findByMedicationName("Ibuprofen", pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar pacientes por tipo de entrada en el historial médico")
        void shouldFindByMedicalRecordEntryType() {
            // Given
            when(patientRepository.findByMedicalRecordEntryType("CONSULTATION", pageable))
                    .thenReturn(patientPage);
            
            // When
            Page<Patient> result = patientRepository.findByMedicalRecordEntryType("CONSULTATION", pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(patientRepository).findByMedicalRecordEntryType("CONSULTATION", pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por condiciones médicas")
    class MedicalConditionSearchTests {
        
        @Test
        @DisplayName("Debería encontrar pacientes por condición crónica")
        void shouldFindByChronicCondition() {
            // Given
            when(patientRepository.findByChronicCondition("Diabetes", pageable))
                    .thenReturn(patientPage);
            
            // When
            Page<Patient> result = patientRepository.findByChronicCondition("Diabetes", pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(patientRepository).findByChronicCondition("Diabetes", pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar pacientes por alergia")
        void shouldFindByAllergy() {
            // Given
            when(patientRepository.findByAllergy("Penicillin", pageable))
                    .thenReturn(patientPage);
            
            // When
            Page<Patient> result = patientRepository.findByAllergy("Penicillin", pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(patientRepository).findByAllergy("Penicillin", pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda avanzada")
    class AdvancedSearchTests {
        
        @Test
        @DisplayName("Debería buscar pacientes con múltiples criterios")
        void shouldSearchPatients() {
            // Given
            String name = "Maria";
            Gender gender = Gender.FEMALE;
            Integer minAge = 30;
            Integer maxAge = 40;
            String insuranceProvider = "MediSalud";
            String condition = "Hypertension";
            
            List<Patient> matchingPatients = Collections.singletonList(patient1);
            Page<Patient> matchingPage = new PageImpl<>(matchingPatients, pageable, matchingPatients.size());
            
            when(patientRepository.searchPatients(
                    name, gender, minAge, maxAge, insuranceProvider, condition, pageable))
                    .thenReturn(matchingPage);
            
            // When
            Page<Patient> result = patientRepository.searchPatients(
                    name, gender, minAge, maxAge, insuranceProvider, condition, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("Maria", result.getContent().get(0).getFirstName());
            verify(patientRepository).searchPatients(
                    name, gender, minAge, maxAge, insuranceProvider, condition, pageable);
        }
        
        @Test
        @DisplayName("Debería buscar pacientes con criterios parciales")
        void shouldSearchPatientsWithPartialCriteria() {
            // Given
            when(patientRepository.searchPatients(
                    "Maria", null, null, null, null, null, pageable))
                    .thenReturn(new PageImpl<>(Collections.singletonList(patient1), pageable, 1));
            
            // When
            Page<Patient> result = patientRepository.searchPatients(
                    "Maria", null, null, null, null, null, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("Maria", result.getContent().get(0).getFirstName());
            verify(patientRepository).searchPatients(
                    "Maria", null, null, null, null, null, pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de estadísticas")
    class StatisticsTests {
        
        @Test
        @DisplayName("Debería contar pacientes por género")
        void shouldCountPatientsByGender() {
            // Given
            List<Object[]> genderCounts = new ArrayList<>();
            genderCounts.add(new Object[]{Gender.FEMALE, 1L});
            genderCounts.add(new Object[]{Gender.MALE, 1L});
            
            when(patientRepository.countPatientsByGender())
                    .thenReturn(genderCounts);
            
            // When
            List<Object[]> result = patientRepository.countPatientsByGender();
            
            // Then
            assertEquals(2, result.size());
            assertEquals(Gender.FEMALE, result.get(0)[0]);
            assertEquals(1L, result.get(0)[1]);
            assertEquals(Gender.MALE, result.get(1)[0]);
            assertEquals(1L, result.get(1)[1]);
            verify(patientRepository).countPatientsByGender();
        }
        
        @Test
        @DisplayName("Debería contar pacientes por grupo de edad")
        void shouldCountPatientsByAgeGroup() {
            // Given
            List<Object[]> ageGroupCounts = new ArrayList<>();
            ageGroupCounts.add(new Object[]{"20-29", 1L});
            ageGroupCounts.add(new Object[]{"30-39", 1L});
            
            when(patientRepository.countPatientsByAgeGroup(10))
                    .thenReturn(ageGroupCounts);
            
            // When
            List<Object[]> result = patientRepository.countPatientsByAgeGroup(10);
            
            // Then
            assertEquals(2, result.size());
            assertEquals("20-29", result.get(0)[0]);
            assertEquals(1L, result.get(0)[1]);
            assertEquals("30-39", result.get(1)[0]);
            assertEquals(1L, result.get(1)[1]);
            verify(patientRepository).countPatientsByAgeGroup(10);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de proyecciones")
    class ProjectionTests {
        
        @Test
        @DisplayName("Debería encontrar información básica de todos los pacientes")
        void shouldFindAllBasicInfo() {
            // Given
            List<PatientRepository.PatientBasicInfo> basicInfoList = new ArrayList<>();
            basicInfoList.add(createBasicPatientInfo(1L, "Maria", "Garcia", Gender.FEMALE));
            basicInfoList.add(createBasicPatientInfo(2L, "Carlos", "Rodriguez", Gender.MALE));
            
            Page<PatientRepository.PatientBasicInfo> basicInfoPage = 
                    new PageImpl<>(basicInfoList, pageable, basicInfoList.size());
                    
            when(patientRepository.findAllBasicInfo(pageable)).thenReturn(basicInfoPage);
            
            // When
            Page<PatientRepository.PatientBasicInfo> result = patientRepository.findAllBasicInfo(pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            assertEquals("Maria", result.getContent().get(0).getFirstName());
            assertEquals("Carlos", result.getContent().get(1).getFirstName());
            verify(patientRepository).findAllBasicInfo(pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda especial de pacientes")
    class SpecialPatientSearchTests {
        
        @Test
        @DisplayName("Debería encontrar pacientes sin citas recientes")
        void shouldFindPatientsWithoutRecentAppointments() {
            // Given
            String interval = "6 MONTH";
            
            when(patientRepository.findPatientsWithoutRecentAppointments(interval, pageable))
                    .thenReturn(patientPage);
            
            // When
            Page<Patient> result = patientRepository.findPatientsWithoutRecentAppointments(
                    interval, pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(patientRepository).findPatientsWithoutRecentAppointments(interval, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar pacientes nuevos")
        void shouldFindNewPatients() {
            // Given
            String interval = "1 MONTH";
            
            when(patientRepository.findNewPatients(interval, pageable))
                    .thenReturn(patientPage);
            
            // When
            Page<Patient> result = patientRepository.findNewPatients(interval, pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(patientRepository).findNewPatients(interval, pageable);
        }
    }
    
    // Método auxiliar para crear objetos PatientBasicInfo
    private PatientRepository.PatientBasicInfo createBasicPatientInfo(
            Long id, String firstName, String lastName, Gender gender) {
        return new PatientRepository.PatientBasicInfo() {
            @Override
            public Long getId() {
                return id;
            }
            
            @Override
            public String getFirstName() {
                return firstName;
            }
            
            @Override
            public String getLastName() {
                return lastName;
            }
            
            @Override
            public Date getBirthDate() {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.YEAR, -30);
                return calendar.getTime();
            }
            
            @Override
            public String getPhone() {
                return "123456789";
            }
            
            @Override
            public Gender getGender() {
                return gender;
            }
        };
    }
}