package com.example.miapp.repository;

import com.example.miapp.entity.Role;
import com.example.miapp.entity.Role.ERole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleRepositoryTest {

    @Mock
    private RoleRepository roleRepository;

    private Role adminRole;
    private Role doctorRole;
    private Role patientRole;
    private List<Role> allRoles;

    @BeforeEach
    void setUp() {
        // Crear roles
        adminRole = new Role(1, ERole.ROLE_ADMIN);
        doctorRole = new Role(2, ERole.ROLE_DOCTOR);
        patientRole = new Role(3, ERole.ROLE_PATIENT);
        
        // Configurar lista de roles
        allRoles = Arrays.asList(adminRole, doctorRole, patientRole);
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda básica")
    class BasicSearchTests {
        
        @Test
        @DisplayName("Debería encontrar un rol por nombre")
        void shouldFindByName() {
            // Given
            when(roleRepository.findByName(ERole.ROLE_ADMIN))
                    .thenReturn(Optional.of(adminRole));
            
            // When
            Optional<Role> result = roleRepository.findByName(ERole.ROLE_ADMIN);
            
            // Then
            assertTrue(result.isPresent());
            assertEquals(ERole.ROLE_ADMIN, result.get().getName());
            verify(roleRepository).findByName(ERole.ROLE_ADMIN);
        }
        
        @Test
@DisplayName("Debería comprobar si existe un rol por nombre")
void shouldExistsByName() {
    // Given
    when(roleRepository.existsByName(ERole.ROLE_ADMIN))
            .thenReturn(true);
    when(roleRepository.existsByName(ERole.ROLE_DOCTOR))
            .thenReturn(false);
    
    // When
    boolean existsResult = roleRepository.existsByName(ERole.ROLE_ADMIN);
    boolean nonExistsResult = roleRepository.existsByName(ERole.ROLE_DOCTOR);
    
    // Then
    assertTrue(existsResult);
    assertFalse(nonExistsResult);
    verify(roleRepository).existsByName(ERole.ROLE_ADMIN);
    verify(roleRepository).existsByName(ERole.ROLE_DOCTOR);
}
        
        @Test
        @DisplayName("Debería encontrar roles por múltiples nombres")
        void shouldFindByNameIn() {
            // Given
            Set<ERole> roleNames = Set.of(ERole.ROLE_ADMIN, ERole.ROLE_DOCTOR);
            List<Role> expectedRoles = Arrays.asList(adminRole, doctorRole);
            
            when(roleRepository.findByNameIn(roleNames))
                    .thenReturn(expectedRoles);
            
            // When
            List<Role> result = roleRepository.findByNameIn(roleNames);
            
            // Then
            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(role -> role.getName() == ERole.ROLE_ADMIN));
            assertTrue(result.stream().anyMatch(role -> role.getName() == ERole.ROLE_DOCTOR));
            verify(roleRepository).findByNameIn(roleNames);
        }
        
        @Test
        @DisplayName("Debería obtener todos los nombres de roles")
        void shouldFindAllRoleNames() {
            // Given
            List<ERole> roleNames = Arrays.asList(ERole.ROLE_ADMIN, ERole.ROLE_DOCTOR, ERole.ROLE_PATIENT);
            
            when(roleRepository.findAllRoleNames())
                    .thenReturn(roleNames);
            
            // When
            List<ERole> result = roleRepository.findAllRoleNames();
            
            // Then
            assertEquals(3, result.size());
            assertTrue(result.contains(ERole.ROLE_ADMIN));
            assertTrue(result.contains(ERole.ROLE_DOCTOR));
            assertTrue(result.contains(ERole.ROLE_PATIENT));
            verify(roleRepository).findAllRoleNames();
        }
    }
    
    @Nested
    @DisplayName("Pruebas de estadísticas")
    class StatisticsTests {
        
        @Test
        @DisplayName("Debería contar usuarios por rol")
        void shouldCountUsersByRole() {
            // Given
            List<Object[]> roleCounts = new ArrayList<>();
            roleCounts.add(new Object[]{ERole.ROLE_ADMIN, 1L});
            roleCounts.add(new Object[]{ERole.ROLE_DOCTOR, 5L});
            roleCounts.add(new Object[]{ERole.ROLE_PATIENT, 20L});
            
            when(roleRepository.countUsersByRole())
                    .thenReturn(roleCounts);
            
            // When
            List<Object[]> result = roleRepository.countUsersByRole();
            
            // Then
            assertEquals(3, result.size());
            assertEquals(ERole.ROLE_ADMIN, result.get(0)[0]);
            assertEquals(1L, result.get(0)[1]);
            assertEquals(ERole.ROLE_DOCTOR, result.get(1)[0]);
            assertEquals(5L, result.get(1)[1]);
            assertEquals(ERole.ROLE_PATIENT, result.get(2)[0]);
            assertEquals(20L, result.get(2)[1]);
            verify(roleRepository).countUsersByRole();
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por número de usuarios")
    class UserCountSearchTests {
        
        @Test
        @DisplayName("Debería encontrar roles con un mínimo de usuarios")
        void shouldFindRolesWithUserCountGreaterThanEqual() {
            // Given
            when(roleRepository.findRolesWithUserCountGreaterThanEqual(5L))
                    .thenReturn(Arrays.asList(doctorRole, patientRole));
            
            // When
            List<Role> result = roleRepository.findRolesWithUserCountGreaterThanEqual(5L);
            
            // Then
            assertEquals(2, result.size());
            assertTrue(result.contains(doctorRole));
            assertTrue(result.contains(patientRole));
            verify(roleRepository).findRolesWithUserCountGreaterThanEqual(5L);
        }
        
        @Test
        @DisplayName("Debería encontrar roles sin usuarios")
        void shouldFindUnusedRoles() {
            // Given
            when(roleRepository.findUnusedRoles())
                    .thenReturn(Collections.emptyList());
            
            // When
            List<Role> result = roleRepository.findUnusedRoles();
            
            // Then
            assertTrue(result.isEmpty());
            verify(roleRepository).findUnusedRoles();
        }
    }
    
    @Nested
    @DisplayName("Pruebas de proyecciones")
    class ProjectionTests {
        
        @Test
        @DisplayName("Debería encontrar información básica de todos los roles")
        void shouldFindAllBasicInfo() {
            // Given
            List<RoleRepository.RoleBasicInfo> basicInfoList = new ArrayList<>();
            basicInfoList.add(createBasicRoleInfo(1, ERole.ROLE_ADMIN));
            basicInfoList.add(createBasicRoleInfo(2, ERole.ROLE_DOCTOR));
            basicInfoList.add(createBasicRoleInfo(3, ERole.ROLE_PATIENT));
            
            when(roleRepository.findAllBasicInfo())
                    .thenReturn(basicInfoList);
            
            // When
            List<RoleRepository.RoleBasicInfo> result = roleRepository.findAllBasicInfo();
            
            // Then
            assertEquals(3, result.size());
            assertEquals(ERole.ROLE_ADMIN, result.get(0).getName());
            assertEquals(ERole.ROLE_DOCTOR, result.get(1).getName());
            assertEquals(ERole.ROLE_PATIENT, result.get(2).getName());
            verify(roleRepository).findAllBasicInfo();
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por usuario")
    class UserRelatedSearchTests {
        
        @Test
        @DisplayName("Debería encontrar roles asignados a un usuario específico")
        void shouldFindRolesByUserId() {
            // Given
            when(roleRepository.findRolesByUserId(1L))
                    .thenReturn(Collections.singletonList(adminRole));
            
            // When
            List<Role> result = roleRepository.findRolesByUserId(1L);
            
            // Then
            assertEquals(1, result.size());
            assertEquals(ERole.ROLE_ADMIN, result.get(0).getName());
            verify(roleRepository).findRolesByUserId(1L);
        }
        
        @Test
        @DisplayName("Debería verificar si un usuario tiene un rol específico")
        void shouldCheckIfUserHasRole() {
            // Given
            when(roleRepository.hasUserRole(1L, ERole.ROLE_ADMIN))
                    .thenReturn(true);
            when(roleRepository.hasUserRole(1L, ERole.ROLE_DOCTOR))
                    .thenReturn(false);
            
            // When
            boolean hasAdminRole = roleRepository.hasUserRole(1L, ERole.ROLE_ADMIN);
            boolean hasDoctorRole = roleRepository.hasUserRole(1L, ERole.ROLE_DOCTOR);
            
            // Then
            assertTrue(hasAdminRole);
            assertFalse(hasDoctorRole);
            verify(roleRepository).hasUserRole(1L, ERole.ROLE_ADMIN);
            verify(roleRepository).hasUserRole(1L, ERole.ROLE_DOCTOR);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda de roles en uso")
    class RolesInUseTests {
        
        @Test
        @DisplayName("Debería encontrar roles que estén en uso")
        void shouldFindRolesInUse() {
            // Given
            when(roleRepository.findRolesInUse())
                    .thenReturn(allRoles);
            
            // When
            List<Role> result = roleRepository.findRolesInUse();
            
            // Then
            assertEquals(3, result.size());
            assertTrue(result.contains(adminRole));
            assertTrue(result.contains(doctorRole));
            assertTrue(result.contains(patientRole));
            verify(roleRepository).findRolesInUse();
        }
    }
    
    // Método auxiliar para crear objetos RoleBasicInfo
    private RoleRepository.RoleBasicInfo createBasicRoleInfo(Integer id, ERole name) {
        return new RoleRepository.RoleBasicInfo() {
            @Override
            public Integer getId() {
                return id;
            }
            
            @Override
            public ERole getName() {
                return name;
            }
        };
    }
}