package com.example.miapp.repository;

import com.example.miapp.entity.Role;
import com.example.miapp.entity.Role.ERole;
import com.example.miapp.entity.User;
import com.example.miapp.entity.User.UserStatus;
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
public class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private User testUser;
    private Role adminRole;
    private Role doctorRole;
    private Role patientRole;
    private Set<Role> roles;
    private List<User> userList;
    private Page<User> userPage;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Crear roles
        adminRole = new Role(1, ERole.ROLE_ADMIN);
        doctorRole = new Role(2, ERole.ROLE_DOCTOR);
        patientRole = new Role(3, ERole.ROLE_PATIENT);
        
        roles = new HashSet<>();
        roles.add(adminRole);
        
        // Crear usuario de prueba
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("$2a$10$abcdefghijklmnopqrstuvwxyz123456789")
                .status(UserStatus.ACTIVE)
                .lastLogin(System.currentTimeMillis())
                .roles(roles)
                .firstLogin(false)
                .build();
        
        // Crear lista de usuarios para pruebas
        userList = new ArrayList<>();
        userList.add(testUser);
        
        User user2 = User.builder()
                .id(2L)
                .username("doctor1")
                .email("doctor@example.com")
                .password("$2a$10$abcdefghijklmnopqrstuvwxyz123456789")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(doctorRole)))
                .build();
        userList.add(user2);
        
        User user3 = User.builder()
                .id(3L)
                .username("patient1")
                .email("patient@example.com")
                .password("$2a$10$abcdefghijklmnopqrstuvwxyz123456789")
                .status(UserStatus.INACTIVE)
                .roles(new HashSet<>(Collections.singletonList(patientRole)))
                .build();
        userList.add(user3);
        
        // Configurar paginación
        pageable = PageRequest.of(0, 10);
        userPage = new PageImpl<>(userList, pageable, userList.size());
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda básica")
    class BasicFindTests {
        
        @Test
        @DisplayName("Debería encontrar un usuario por nombre de usuario")
        void shouldFindByUsername() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            
            // When
            Optional<User> result = userRepository.findByUsername("testuser");
            
            // Then
            assertTrue(result.isPresent());
            assertEquals("testuser", result.get().getUsername());
            verify(userRepository).findByUsername("testuser");
        }
        
        @Test
        @DisplayName("Debería retornar Optional vacío cuando el usuario no existe")
        void shouldReturnEmptyOptionalWhenUserDoesNotExist() {
            // Given
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
            
            // When
            Optional<User> result = userRepository.findByUsername("nonexistent");
            
            // Then
            assertFalse(result.isPresent());
            verify(userRepository).findByUsername("nonexistent");
        }
        
        @Test
        @DisplayName("Debería encontrar un usuario por email")
        void shouldFindByEmail() {
            // Given
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            
            // When
            Optional<User> result = userRepository.findByEmail("test@example.com");
            
            // Then
            assertTrue(result.isPresent());
            assertEquals("test@example.com", result.get().getEmail());
            verify(userRepository).findByEmail("test@example.com");
        }
    }
    
    @Nested
    @DisplayName("Pruebas de verificación de existencia")
    class ExistenceTests {
        
        @Test
        @DisplayName("Debería verificar si existe un usuario por nombre de usuario")
        void shouldCheckIfUsernameExists() {
            // Given
            when(userRepository.existsByUsername("testuser")).thenReturn(true);
            when(userRepository.existsByUsername("nonexistent")).thenReturn(false);
            
            // When
            boolean existsResult = userRepository.existsByUsername("testuser");
            boolean nonExistsResult = userRepository.existsByUsername("nonexistent");
            
            // Then
            assertTrue(existsResult);
            assertFalse(nonExistsResult);
            verify(userRepository).existsByUsername("testuser");
            verify(userRepository).existsByUsername("nonexistent");
        }
        
        @Test
        @DisplayName("Debería verificar si existe un usuario por email")
        void shouldCheckIfEmailExists() {
            // Given
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
            when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);
            
            // When
            boolean existsResult = userRepository.existsByEmail("test@example.com");
            boolean nonExistsResult = userRepository.existsByEmail("nonexistent@example.com");
            
            // Then
            assertTrue(existsResult);
            assertFalse(nonExistsResult);
            verify(userRepository).existsByEmail("test@example.com");
            verify(userRepository).existsByEmail("nonexistent@example.com");
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por estado")
    class StatusFindTests {
        
        @Test
        @DisplayName("Debería encontrar usuarios por estado")
        void shouldFindByStatus() {
            // Given
            List<User> activeUsers = List.of(testUser, userList.get(1));
            Page<User> activePage = new PageImpl<>(activeUsers, pageable, activeUsers.size());
            when(userRepository.findByStatus(UserStatus.ACTIVE, pageable)).thenReturn(activePage);
            
            // When
            Page<User> result = userRepository.findByStatus(UserStatus.ACTIVE, pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            assertEquals(UserStatus.ACTIVE, result.getContent().get(0).getStatus());
            assertEquals(UserStatus.ACTIVE, result.getContent().get(1).getStatus());
            verify(userRepository).findByStatus(UserStatus.ACTIVE, pageable);
        }
        
        @Test
        @DisplayName("Debería contar usuarios por estado")
        void shouldCountByStatus() {
            // Given
            when(userRepository.countByStatus(UserStatus.ACTIVE)).thenReturn(2L);
            when(userRepository.countByStatus(UserStatus.INACTIVE)).thenReturn(1L);
            when(userRepository.countByStatus(UserStatus.BLOCKED)).thenReturn(0L);
            
            // When
            long activeCount = userRepository.countByStatus(UserStatus.ACTIVE);
            long inactiveCount = userRepository.countByStatus(UserStatus.INACTIVE);
            long blockedCount = userRepository.countByStatus(UserStatus.BLOCKED);
            
            // Then
            assertEquals(2L, activeCount);
            assertEquals(1L, inactiveCount);
            assertEquals(0L, blockedCount);
            verify(userRepository).countByStatus(UserStatus.ACTIVE);
            verify(userRepository).countByStatus(UserStatus.INACTIVE);
            verify(userRepository).countByStatus(UserStatus.BLOCKED);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por rol")
    class RoleFindTests {
        
        @Test
        @DisplayName("Debería encontrar usuarios por nombre de rol")
        void shouldFindByRoleName() {
            // Given
            List<User> adminUsers = List.of(testUser);
            Page<User> adminPage = new PageImpl<>(adminUsers, pageable, adminUsers.size());
            when(userRepository.findByRoleName(ERole.ROLE_ADMIN, pageable)).thenReturn(adminPage);
            
            // When
            Page<User> result = userRepository.findByRoleName(ERole.ROLE_ADMIN, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("testuser", result.getContent().get(0).getUsername());
            verify(userRepository).findByRoleName(ERole.ROLE_ADMIN, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar usuarios por nombre de rol y estado")
        void shouldFindByRoleNameAndStatus() {
            // Given
            List<User> activeAdminUsers = List.of(testUser);
            Page<User> activeAdminPage = new PageImpl<>(activeAdminUsers, pageable, activeAdminUsers.size());
            when(userRepository.findByRoleNameAndStatus(ERole.ROLE_ADMIN, UserStatus.ACTIVE, pageable))
                    .thenReturn(activeAdminPage);
            
            // When
            Page<User> result = userRepository.findByRoleNameAndStatus(
                    ERole.ROLE_ADMIN, UserStatus.ACTIVE, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("testuser", result.getContent().get(0).getUsername());
            assertEquals(UserStatus.ACTIVE, result.getContent().get(0).getStatus());
            verify(userRepository).findByRoleNameAndStatus(ERole.ROLE_ADMIN, UserStatus.ACTIVE, pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de actualización")
    class UpdateTests {
        
        @Test
        @DisplayName("Debería actualizar el estado de un usuario")
        void shouldUpdateUserStatus() {
            // Given
            when(userRepository.updateUserStatus(1L, UserStatus.BLOCKED)).thenReturn(1);
            
            // When
            int affectedRows = userRepository.updateUserStatus(1L, UserStatus.BLOCKED);
            
            // Then
            assertEquals(1, affectedRows);
            verify(userRepository).updateUserStatus(1L, UserStatus.BLOCKED);
        }
        
        @Test
        @DisplayName("Debería actualizar el timestamp de último login")
        void shouldUpdateLastLogin() {
            // Given
            long timestamp = System.currentTimeMillis();
            when(userRepository.updateLastLogin(1L, timestamp)).thenReturn(1);
            
            // When
            int affectedRows = userRepository.updateLastLogin(1L, timestamp);
            
            // Then
            assertEquals(1, affectedRows);
            verify(userRepository).updateLastLogin(1L, timestamp);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda avanzada")
    class AdvancedSearchTests {
        
        @Test
        @DisplayName("Debería buscar usuarios con criterios múltiples")
        void shouldSearchUsers() {
            // Given
            List<User> filteredUsers = List.of(testUser);
            Page<User> filteredPage = new PageImpl<>(filteredUsers, pageable, filteredUsers.size());
            when(userRepository.searchUsers("test", "example.com", UserStatus.ACTIVE, pageable))
                    .thenReturn(filteredPage);
            
            // When
            Page<User> result = userRepository.searchUsers(
                    "test", "example.com", UserStatus.ACTIVE, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("testuser", result.getContent().get(0).getUsername());
            verify(userRepository).searchUsers("test", "example.com", UserStatus.ACTIVE, pageable);
        }
        
        @Test
        @DisplayName("Debería buscar usuarios con criterios parciales")
        void shouldSearchUsersWithPartialCriteria() {
            // Given
            List<User> filteredUsers = List.of(testUser);
            Page<User> filteredPage = new PageImpl<>(filteredUsers, pageable, filteredUsers.size());
            when(userRepository.searchUsers("test", null, null, pageable))
                    .thenReturn(filteredPage);
            
            // When
            Page<User> result = userRepository.searchUsers("test", null, null, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("testuser", result.getContent().get(0).getUsername());
            verify(userRepository).searchUsers("test", null, null, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar usuarios inactivos")
        void shouldFindInactiveUsers() {
            // Given
            long thresholdTimestamp = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000); // 30 días atrás
            List<User> inactiveUsers = List.of(userList.get(2)); // El usuario inactivo
            Page<User> inactivePage = new PageImpl<>(inactiveUsers, pageable, inactiveUsers.size());
            
            when(userRepository.findInactiveUsers(thresholdTimestamp, pageable))
                    .thenReturn(inactivePage);
            
            // When
            Page<User> result = userRepository.findInactiveUsers(thresholdTimestamp, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("patient1", result.getContent().get(0).getUsername());
            verify(userRepository).findInactiveUsers(thresholdTimestamp, pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de estadísticas")
    class StatisticsTests {
        
        @Test
        @DisplayName("Debería contar usuarios por estado")
        void shouldCountUsersByStatus() {
            // Given
            List<Object[]> statusCounts = List.of(
                    new Object[]{UserStatus.ACTIVE, 2L},
                    new Object[]{UserStatus.INACTIVE, 1L},
                    new Object[]{UserStatus.BLOCKED, 0L}
            );
            when(userRepository.countUsersByStatus()).thenReturn(statusCounts);
            
            // When
            List<Object[]> result = userRepository.countUsersByStatus();
            
            // Then
            assertEquals(3, result.size());
            assertEquals(UserStatus.ACTIVE, result.get(0)[0]);
            assertEquals(2L, result.get(0)[1]);
            assertEquals(UserStatus.INACTIVE, result.get(1)[0]);
            assertEquals(1L, result.get(1)[1]);
            assertEquals(UserStatus.BLOCKED, result.get(2)[0]);
            assertEquals(0L, result.get(2)[1]);
            verify(userRepository).countUsersByStatus();
        }
        
        @Test
        @DisplayName("Debería contar usuarios por rol")
        void shouldCountUsersByRole() {
            // Given
            List<Object[]> roleCounts = List.of(
                    new Object[]{ERole.ROLE_ADMIN, 1L},
                    new Object[]{ERole.ROLE_DOCTOR, 1L},
                    new Object[]{ERole.ROLE_PATIENT, 1L}
            );
            when(userRepository.countUsersByRole()).thenReturn(roleCounts);
            
            // When
            List<Object[]> result = userRepository.countUsersByRole();
            
            // Then
            assertEquals(3, result.size());
            assertEquals(ERole.ROLE_ADMIN, result.get(0)[0]);
            assertEquals(1L, result.get(0)[1]);
            assertEquals(ERole.ROLE_DOCTOR, result.get(1)[0]);
            assertEquals(1L, result.get(1)[1]);
            assertEquals(ERole.ROLE_PATIENT, result.get(2)[0]);
            assertEquals(1L, result.get(2)[1]);
            verify(userRepository).countUsersByRole();
        }
    }
    
    @Nested
    @DisplayName("Pruebas de proyecciones")
    class ProjectionTests {
        
        @Test
        @DisplayName("Debería encontrar información básica de todos los usuarios")
        void shouldFindAllBasicInfo() {
            // Given
            List<UserRepository.UserBasicInfo> basicInfoList = List.of(
                    new UserRepository.UserBasicInfo() {
                        @Override
                        public Long getId() {
                            return 1L;
                        }
                        
                        @Override
                        public String getUsername() {
                            return "testuser";
                        }
                        
                        @Override
                        public String getEmail() {
                            return "test@example.com";
                        }
                        
                        @Override
                        public UserStatus getStatus() {
                            return UserStatus.ACTIVE;
                        }
                    },
                    new UserRepository.UserBasicInfo() {
                        @Override
                        public Long getId() {
                            return 2L;
                        }
                        
                        @Override
                        public String getUsername() {
                            return "doctor1";
                        }
                        
                        @Override
                        public String getEmail() {
                            return "doctor@example.com";
                        }
                        
                        @Override
                        public UserStatus getStatus() {
                            return UserStatus.ACTIVE;
                        }
                    }
            );
            
            Page<UserRepository.UserBasicInfo> basicInfoPage = 
                    new PageImpl<>(basicInfoList, pageable, basicInfoList.size());
                    
            when(userRepository.findAllBasicInfo(pageable)).thenReturn(basicInfoPage);
            
            // When
            Page<UserRepository.UserBasicInfo> result = userRepository.findAllBasicInfo(pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            assertEquals("testuser", result.getContent().get(0).getUsername());
            assertEquals("test@example.com", result.getContent().get(0).getEmail());
            assertEquals(UserStatus.ACTIVE, result.getContent().get(0).getStatus());
            verify(userRepository).findAllBasicInfo(pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar información básica por rol")
        void shouldFindByRoleNameBasicInfo() {
            // Given
            List<UserRepository.UserBasicInfo> adminBasicInfoList = List.of(
                    new UserRepository.UserBasicInfo() {
                        @Override
                        public Long getId() {
                            return 1L;
                        }
                        
                        @Override
                        public String getUsername() {
                            return "testuser";
                        }
                        
                        @Override
                        public String getEmail() {
                            return "test@example.com";
                        }
                        
                        @Override
                        public UserStatus getStatus() {
                            return UserStatus.ACTIVE;
                        }
                    }
            );
            
            Page<UserRepository.UserBasicInfo> adminBasicInfoPage = 
                    new PageImpl<>(adminBasicInfoList, pageable, adminBasicInfoList.size());
                    
            when(userRepository.findByRoleNameBasicInfo(ERole.ROLE_ADMIN, pageable))
                    .thenReturn(adminBasicInfoPage);
            
            // When
            Page<UserRepository.UserBasicInfo> result = 
                    userRepository.findByRoleNameBasicInfo(ERole.ROLE_ADMIN, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("testuser", result.getContent().get(0).getUsername());
            assertEquals(UserStatus.ACTIVE, result.getContent().get(0).getStatus());
            verify(userRepository).findByRoleNameBasicInfo(ERole.ROLE_ADMIN, pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de otros métodos de búsqueda")
    class OtherFindTests {
        
        @Test
        @DisplayName("Debería encontrar usuarios por primer login")
        void shouldFindByFirstLoginTrue() {
            // Given
            List<User> firstLoginUsers = List.of(
                    User.builder()
                            .id(4L)
                            .username("newuser")
                            .email("new@example.com")
                            .status(UserStatus.ACTIVE)
                            .firstLogin(true)
                            .build()
            );
            
            Page<User> firstLoginPage = new PageImpl<>(firstLoginUsers, pageable, firstLoginUsers.size());
            when(userRepository.findByFirstLoginTrue(pageable)).thenReturn(firstLoginPage);
            
            // When
            Page<User> result = userRepository.findByFirstLoginTrue(pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("newuser", result.getContent().get(0).getUsername());
            assertTrue(result.getContent().get(0).isFirstLogin());
            verify(userRepository).findByFirstLoginTrue(pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar usuarios por patrón de username y estado")
        void shouldFindByUsernameContainingAndStatus() {
            // Given
            List<User> filteredUsers = List.of(testUser);
            Page<User> filteredPage = new PageImpl<>(filteredUsers, pageable, filteredUsers.size());
            
            when(userRepository.findByUsernameContainingAndStatus("test", UserStatus.ACTIVE, pageable))
                    .thenReturn(filteredPage);
            
            // When
            Page<User> result = userRepository.findByUsernameContainingAndStatus(
                    "test", UserStatus.ACTIVE, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("testuser", result.getContent().get(0).getUsername());
            assertEquals(UserStatus.ACTIVE, result.getContent().get(0).getStatus());
            verify(userRepository).findByUsernameContainingAndStatus("test", UserStatus.ACTIVE, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar usuarios por múltiples estados")
        void shouldFindByStatusIn() {
            // Given
            Set<UserStatus> statuses = Set.of(UserStatus.ACTIVE, UserStatus.INACTIVE);
            
            when(userRepository.findByStatusIn(statuses, pageable)).thenReturn(userPage);
            
            // When
            Page<User> result = userRepository.findByStatusIn(statuses, pageable);
            
            // Then
            assertEquals(3, result.getTotalElements());
            verify(userRepository).findByStatusIn(statuses, pageable);
        }
    }
}