package com.example.miapp.repository;

import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.DoctorSpecialty;
import com.example.miapp.entity.Specialty;
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
public class SpecialtyRepositoryTest {

    @Mock
    private SpecialtyRepository specialtyRepository;

    private Specialty cardiology;
    private Specialty neurology;
    private Doctor doctor;
    private DoctorSpecialty doctorSpecialty;
    private List<Specialty> specialtyList;
    private Page<Specialty> specialtyPage;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Crear especialidades
        cardiology = Specialty.builder()
                .id(1L)
                .name("Cardiology")
                .description("Diagnosis and treatment of heart diseases")
                .build();
        
        neurology = Specialty.builder()
                .id(2L)
                .name("Neurology")
                .description("Diagnosis and treatment of nervous system disorders")
                .build();
        
        // Crear doctor
        User doctorUser = User.builder()
                .id(1L)
                .username("doctor1")
                .email("doctor1@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        
        doctor = Doctor.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phone("123456789")
                .licenseNumber("MED12345")
                .user(doctorUser)
                .build();
        
        // Crear relación doctor-especialidad
        doctorSpecialty = DoctorSpecialty.builder()
                .id(1L)
                .doctor(doctor)
                .specialty(cardiology)
                .experienceLevel("Senior")
                .build();
        
        // Configurar lista de doctorSpecialties para especialidad
        List<DoctorSpecialty> doctorSpecialties = new ArrayList<>();
        doctorSpecialties.add(doctorSpecialty);
        cardiology.setDoctorSpecialties(doctorSpecialties);
        neurology.setDoctorSpecialties(new ArrayList<>());
        
        // Configurar lista de especialidades
        specialtyList = Arrays.asList(cardiology, neurology);
        
        // Configurar paginación
        pageable = PageRequest.of(0, 10);
        specialtyPage = new PageImpl<>(specialtyList, pageable, specialtyList.size());
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda básica")
    class BasicSearchTests {
        
        @Test
        @DisplayName("Debería encontrar una especialidad por nombre")
        void shouldFindByName() {
            // Given
            when(specialtyRepository.findByName("Cardiology"))
                    .thenReturn(Optional.of(cardiology));
            
            // When
            Optional<Specialty> result = specialtyRepository.findByName("Cardiology");
            
            // Then
            assertTrue(result.isPresent());
            assertEquals("Cardiology", result.get().getName());
            verify(specialtyRepository).findByName("Cardiology");
        }
        
        @Test
        @DisplayName("Debería comprobar si existe una especialidad por nombre")
        void shouldExistsByName() {
            // Given
            when(specialtyRepository.existsByName("Cardiology"))
                    .thenReturn(true);
            when(specialtyRepository.existsByName("Oncology"))
                    .thenReturn(false);
            
            // When
            boolean existsResult = specialtyRepository.existsByName("Cardiology");
            boolean nonExistsResult = specialtyRepository.existsByName("Oncology");
            
            // Then
            assertTrue(existsResult);
            assertFalse(nonExistsResult);
            verify(specialtyRepository).existsByName("Cardiology");
            verify(specialtyRepository).existsByName("Oncology");
        }
        
        @Test
        @DisplayName("Debería encontrar especialidades por patrón de nombre")
        void shouldFindByNameContainingIgnoreCase() {
            // Given
            when(specialtyRepository.findByNameContainingIgnoreCase("ology", pageable))
                    .thenReturn(specialtyPage);
            
            // When
            Page<Specialty> result = specialtyRepository.findByNameContainingIgnoreCase("ology", pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(specialtyRepository).findByNameContainingIgnoreCase("ology", pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de operaciones de actualización")
    class UpdateOperationTests {
        
        @Test
        @DisplayName("Debería actualizar la descripción de una especialidad")
        void shouldUpdateDescription() {
            // Given
            String newDescription = "Updated description for cardiology";
            when(specialtyRepository.updateDescription(1L, newDescription))
                    .thenReturn(1);
            
            // When
            int affectedRows = specialtyRepository.updateDescription(1L, newDescription);
            
            // Then
            assertEquals(1, affectedRows);
            verify(specialtyRepository).updateDescription(1L, newDescription);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por doctores")
    class DoctorRelatedSearchTests {
        
        @Test
        @DisplayName("Debería encontrar especialidades con doctores")
        void shouldFindSpecialtiesWithDoctors() {
            // Given
            when(specialtyRepository.findSpecialtiesWithDoctors())
                    .thenReturn(Collections.singletonList(cardiology));
            
            // When
            List<Specialty> result = specialtyRepository.findSpecialtiesWithDoctors();
            
            // Then
            assertEquals(1, result.size());
            assertEquals("Cardiology", result.get(0).getName());
            assertFalse(result.get(0).getDoctorSpecialties().isEmpty());
            verify(specialtyRepository).findSpecialtiesWithDoctors();
        }
        
        @Test
        @DisplayName("Debería encontrar especialidades sin doctores")
        void shouldFindSpecialtiesWithoutDoctors() {
            // Given
            when(specialtyRepository.findSpecialtiesWithoutDoctors())
                    .thenReturn(Collections.singletonList(neurology));
            
            // When
            List<Specialty> result = specialtyRepository.findSpecialtiesWithoutDoctors();
            
            // Then
            assertEquals(1, result.size());
            assertEquals("Neurology", result.get(0).getName());
            assertTrue(result.get(0).getDoctorSpecialties().isEmpty());
            verify(specialtyRepository).findSpecialtiesWithoutDoctors();
        }
        
        @Test
        @DisplayName("Debería encontrar especialidades por ID de doctor")
        void shouldFindByDoctorId() {
            // Given
            when(specialtyRepository.findByDoctorId(1L))
                    .thenReturn(Collections.singletonList(cardiology));
            
            // When
            List<Specialty> result = specialtyRepository.findByDoctorId(1L);
            
            // Then
            assertEquals(1, result.size());
            assertEquals("Cardiology", result.get(0).getName());
            verify(specialtyRepository).findByDoctorId(1L);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de estadísticas")
    class StatisticsTests {
        
        @Test
        @DisplayName("Debería contar doctores por especialidad")
        void shouldCountDoctorsBySpecialty() {
            // Given
            List<Object[]> specialtyCounts = new ArrayList<>();
            specialtyCounts.add(new Object[]{1L, "Cardiology", 1L});
            specialtyCounts.add(new Object[]{2L, "Neurology", 0L});
            
            when(specialtyRepository.countDoctorsBySpecialty())
                    .thenReturn(specialtyCounts);
            
            // When
            List<Object[]> result = specialtyRepository.countDoctorsBySpecialty();
            
            // Then
            assertEquals(2, result.size());
            assertEquals(1L, result.get(0)[0]);  // ID
            assertEquals("Cardiology", result.get(0)[1]);  // Name
            assertEquals(1L, result.get(0)[2]);  // Count
            assertEquals(2L, result.get(1)[0]);  // ID
            assertEquals("Neurology", result.get(1)[1]);  // Name
            assertEquals(0L, result.get(1)[2]);  // Count
            verify(specialtyRepository).countDoctorsBySpecialty();
        }
    }
    
    @Nested
    @DisplayName("Pruebas de especialidades populares")
    class PopularityTests {
        
        @Test
        @DisplayName("Debería encontrar las especialidades más populares")
        void shouldFindMostPopularSpecialties() {
            // Given
            Pageable limitPageable = PageRequest.of(0, 5);
            
            when(specialtyRepository.findMostPopularSpecialties(limitPageable))
                    .thenReturn(Collections.singletonList(cardiology));
            
            // When
            List<Specialty> result = specialtyRepository.findMostPopularSpecialties(limitPageable);
            
            // Then
            assertEquals(1, result.size());
            assertEquals("Cardiology", result.get(0).getName());
            verify(specialtyRepository).findMostPopularSpecialties(limitPageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por experiencia")
    class ExperienceSearchTests {
        
        @Test
        @DisplayName("Debería encontrar especialidades por nivel de experiencia de los doctores")
        void shouldFindByDoctorExperienceLevel() {
            // Given
            when(specialtyRepository.findByDoctorExperienceLevel("Senior"))
                    .thenReturn(Collections.singletonList(cardiology));
            
            // When
            List<Specialty> result = specialtyRepository.findByDoctorExperienceLevel("Senior");
            
            // Then
            assertEquals(1, result.size());
            assertEquals("Cardiology", result.get(0).getName());
            verify(specialtyRepository).findByDoctorExperienceLevel("Senior");
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda avanzada")
    class AdvancedSearchTests {
        
        @Test
        @DisplayName("Debería buscar especialidades por nombre o descripción")
        void shouldSearchByNameOrDescription() {
            // Given
            when(specialtyRepository.searchByNameOrDescription("heart", pageable))
                    .thenReturn(new PageImpl<>(Collections.singletonList(cardiology), pageable, 1));
            
            // When
            Page<Specialty> result = specialtyRepository.searchByNameOrDescription("heart", pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("Cardiology", result.getContent().get(0).getName());
            assertTrue(result.getContent().get(0).getDescription().contains("heart"));
            verify(specialtyRepository).searchByNameOrDescription("heart", pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de proyecciones")
    class ProjectionTests {
        
        @Test
        @DisplayName("Debería encontrar información básica de todas las especialidades")
        void shouldFindAllBasicInfo() {
            // Given
            List<SpecialtyRepository.SpecialtyBasicInfo> basicInfoList = new ArrayList<>();
            basicInfoList.add(createBasicSpecialtyInfo(1L, "Cardiology"));
            basicInfoList.add(createBasicSpecialtyInfo(2L, "Neurology"));
            
            when(specialtyRepository.findAllBasicInfo())
                    .thenReturn(basicInfoList);
            
            // When
            List<SpecialtyRepository.SpecialtyBasicInfo> result = specialtyRepository.findAllBasicInfo();
            
            // Then
            assertEquals(2, result.size());
            assertEquals("Cardiology", result.get(0).getName());
            assertEquals("Neurology", result.get(1).getName());
            verify(specialtyRepository).findAllBasicInfo();
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda especial de especialidades")
    class SpecialSpecialtySearchTests {
        
        @Test
        @DisplayName("Debería encontrar especialidades con un mínimo de doctores")
        void shouldFindSpecialtiesWithMinimumDoctors() {
            // Given
            when(specialtyRepository.findSpecialtiesWithMinimumDoctors(1L))
                    .thenReturn(Collections.singletonList(cardiology));
            
            // When
            List<Specialty> result = specialtyRepository.findSpecialtiesWithMinimumDoctors(1L);
            
            // Then
            assertEquals(1, result.size());
            assertEquals("Cardiology", result.get(0).getName());
            verify(specialtyRepository).findSpecialtiesWithMinimumDoctors(1L);
        }
        
        @Test
        @DisplayName("Debería encontrar especialidades relacionadas")
        void shouldFindRelatedSpecialties() {
            // Given
            Pageable limitPageable = PageRequest.of(0, 3);
            Specialty dermatology = Specialty.builder()
                    .id(3L)
                    .name("Dermatology")
                    .description("Skin related disorders")
                    .build();
            
            when(specialtyRepository.findRelatedSpecialties(1L, limitPageable))
                    .thenReturn(Collections.singletonList(dermatology));
            
            // When
            List<Specialty> result = specialtyRepository.findRelatedSpecialties(1L, limitPageable);
            
            // Then
            assertEquals(1, result.size());
            assertEquals("Dermatology", result.get(0).getName());
            verify(specialtyRepository).findRelatedSpecialties(1L, limitPageable);
        }
    }
    
    // Método auxiliar para crear objetos SpecialtyBasicInfo
    private SpecialtyRepository.SpecialtyBasicInfo createBasicSpecialtyInfo(Long id, String name) {
        return new SpecialtyRepository.SpecialtyBasicInfo() {
            @Override
            public Long getId() {
                return id;
            }
            
            @Override
            public String getName() {
                return name;
            }
        };
    }
}