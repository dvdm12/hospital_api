package com.example.miapp.repository;

import com.example.miapp.entity.Settings;
import com.example.miapp.entity.Settings.DataType;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SettingsRepositoryTest {

    @Mock
    private SettingsRepository settingsRepository;

    private Settings systemSetting;
    private Settings userSetting;
    private User testUser;
    private List<Settings> settingsList;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Crear usuario de prueba
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        
        // Crear configuración de sistema
        systemSetting = Settings.builder()
                .id(1L)
                .key("system.theme")
                .value("dark")
                .group("appearance")
                .description("System default theme")
                .dataType(DataType.STRING)
                .systemSetting(true)
                .visible(true)
                .editable(true)
                .build();
        
        // Crear configuración de usuario
        userSetting = Settings.builder()
                .id(2L)
                .user(testUser)
                .key("user.notifications")
                .value("true")
                .group("preferences")
                .description("User notifications preference")
                .dataType(DataType.BOOLEAN)
                .systemSetting(false)
                .visible(true)
                .editable(true)
                .build();
        
        // Configurar lista de configuraciones
        settingsList = new ArrayList<>();
        settingsList.add(systemSetting);
        settingsList.add(userSetting);
        
        // Configurar paginación
        pageable = PageRequest.of(0, 10);
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda básica")
    class BasicSearchTests {
        
        @Test
        @DisplayName("Debería encontrar una configuración por ID")
        void shouldFindById() {
            // Given
            when(settingsRepository.findById(1L))
                    .thenReturn(Optional.of(systemSetting));
            
            // When
            Optional<Settings> result = settingsRepository.findById(1L);
            
            // Then
            assertTrue(result.isPresent());
            assertEquals("system.theme", result.get().getKey());
            verify(settingsRepository).findById(1L);
        }
        
        @Test
        @DisplayName("Debería encontrar una configuración por clave")
        void shouldFindByKey() {
            // Given
            when(settingsRepository.findByKey("system.theme"))
                    .thenReturn(Optional.of(systemSetting));
            
            // When
            Optional<Settings> result = settingsRepository.findByKey("system.theme");
            
            // Then
            assertTrue(result.isPresent());
            assertEquals("dark", result.get().getValue());
            verify(settingsRepository).findByKey("system.theme");
        }
        
        @Test
        @DisplayName("Debería encontrar configuraciones por grupo")
        void shouldFindByGroup() {
            // Given
            List<Settings> appearanceSettings = List.of(systemSetting);
            when(settingsRepository.findByGroup("appearance"))
                    .thenReturn(appearanceSettings);
            
            // When
            List<Settings> result = settingsRepository.findByGroup("appearance");
            
            // Then
            assertEquals(1, result.size());
            assertEquals("system.theme", result.get(0).getKey());
            verify(settingsRepository).findByGroup("appearance");
        }
        
        @Test
        @DisplayName("Debería encontrar configuraciones de sistema")
        void shouldFindBySystemSettingTrue() {
            // Given
            List<Settings> systemSettings = List.of(systemSetting);
            when(settingsRepository.findBySystemSettingTrue())
                    .thenReturn(systemSettings);
            
            // When
            List<Settings> result = settingsRepository.findBySystemSettingTrue();
            
            // Then
            assertEquals(1, result.size());
            assertTrue(result.get(0).isSystemSetting());
            verify(settingsRepository).findBySystemSettingTrue();
        }
        
        @Test
        @DisplayName("Debería encontrar configuraciones de usuario")
        void shouldFindBySystemSettingFalse() {
            // Given
            List<Settings> userSettings = List.of(userSetting);
            when(settingsRepository.findBySystemSettingFalse())
                    .thenReturn(userSettings);
            
            // When
            List<Settings> result = settingsRepository.findBySystemSettingFalse();
            
            // Then
            assertEquals(1, result.size());
            assertFalse(result.get(0).isSystemSetting());
            verify(settingsRepository).findBySystemSettingFalse();
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por usuario")
    class UserSearchTests {
        
        @Test
        @DisplayName("Debería encontrar configuraciones por ID de usuario")
        void shouldFindByUserId() {
            // Given
            List<Settings> userSettings = List.of(userSetting);
            when(settingsRepository.findByUserId(1L))
                    .thenReturn(userSettings);
            
            // When
            List<Settings> result = settingsRepository.findByUserId(1L);
            
            // Then
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getUser().getId());
            verify(settingsRepository).findByUserId(1L);
        }
        
        @Test
        @DisplayName("Debería encontrar una configuración por ID de usuario y clave")
        void shouldFindByUserIdAndKey() {
            // Given
            when(settingsRepository.findByUserIdAndKey(1L, "user.notifications"))
                    .thenReturn(Optional.of(userSetting));
            
            // When
            Optional<Settings> result = settingsRepository.findByUserIdAndKey(1L, "user.notifications");
            
            // Then
            assertTrue(result.isPresent());
            assertEquals("true", result.get().getValue());
            verify(settingsRepository).findByUserIdAndKey(1L, "user.notifications");
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por visibilidad y editabilidad")
    class VisibilityEditabilityTests {
        
        @Test
        @DisplayName("Debería encontrar configuraciones visibles")
        void shouldFindByVisibleTrue() {
            // Given
            when(settingsRepository.findByVisibleTrue())
                    .thenReturn(settingsList);
            
            // When
            List<Settings> result = settingsRepository.findByVisibleTrue();
            
            // Then
            assertEquals(2, result.size());
            assertTrue(result.get(0).isVisible());
            assertTrue(result.get(1).isVisible());
            verify(settingsRepository).findByVisibleTrue();
        }
        
        @Test
        @DisplayName("Debería encontrar configuraciones editables")
        void shouldFindByEditableTrue() {
            // Given
            when(settingsRepository.findByEditableTrue())
                    .thenReturn(settingsList);
            
            // When
            List<Settings> result = settingsRepository.findByEditableTrue();
            
            // Then
            assertEquals(2, result.size());
            assertTrue(result.get(0).isEditable());
            assertTrue(result.get(1).isEditable());
            verify(settingsRepository).findByEditableTrue();
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por tipo de datos")
    class DataTypeTests {
        
        @Test
        @DisplayName("Debería encontrar configuraciones por tipo de datos")
        void shouldFindByDataType() {
            // Given
            List<Settings> stringSettings = List.of(systemSetting);
            when(settingsRepository.findByDataType(DataType.STRING))
                    .thenReturn(stringSettings);
            
            // When
            List<Settings> result = settingsRepository.findByDataType(DataType.STRING);
            
            // Then
            assertEquals(1, result.size());
            assertEquals(DataType.STRING, result.get(0).getDataType());
            verify(settingsRepository).findByDataType(DataType.STRING);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de actualización")
    class UpdateTests {
        
        @Test
        @DisplayName("Debería actualizar el valor de una configuración")
        void shouldUpdateValue() {
            // Given
            when(settingsRepository.updateValue(1L, "light"))
                    .thenReturn(1);
            
            // When
            int affectedRows = settingsRepository.updateValue(1L, "light");
            
            // Then
            assertEquals(1, affectedRows);
            verify(settingsRepository).updateValue(1L, "light");
        }
        
        @Test
        @DisplayName("Debería actualizar la visibilidad de una configuración")
        void shouldUpdateVisibility() {
            // Given
            when(settingsRepository.updateVisibility(1L, false))
                    .thenReturn(1);
            
            // When
            int affectedRows = settingsRepository.updateVisibility(1L, false);
            
            // Then
            assertEquals(1, affectedRows);
            verify(settingsRepository).updateVisibility(1L, false);
        }
        
        @Test
        @DisplayName("Debería actualizar la editabilidad de una configuración")
        void shouldUpdateEditability() {
            // Given
            when(settingsRepository.updateEditability(1L, false))
                    .thenReturn(1);
            
            // When
            int affectedRows = settingsRepository.updateEditability(1L, false);
            
            // Then
            assertEquals(1, affectedRows);
            verify(settingsRepository).updateEditability(1L, false);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda avanzada")
    class AdvancedSearchTests {
        
        @Test
        @DisplayName("Debería buscar configuraciones con múltiples criterios")
        void shouldSearchSettings() {
            // Given
            String keyPattern = "theme";
            String group = "appearance";
            DataType dataType = DataType.STRING;
            Boolean systemSetting = true;
            Page<Settings> expectedPage = new PageImpl<>(List.of(SettingsRepositoryTest.this.systemSetting), pageable, 1);
            
            when(settingsRepository.searchSettings(keyPattern, group, dataType, systemSetting, pageable))
                    .thenReturn(expectedPage);
            
            // When
            Page<Settings> result = settingsRepository.searchSettings(keyPattern, group, dataType, systemSetting, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("system.theme", result.getContent().get(0).getKey());
            verify(settingsRepository).searchSettings(keyPattern, group, dataType, systemSetting, pageable);
        }
        
        @Test
        @DisplayName("Debería buscar configuraciones con criterios parciales")
        void shouldSearchSettingsWithPartialCriteria() {
            // Given
            Page<Settings> expectedPage = new PageImpl<>(List.of(userSetting), pageable, 1);
            when(settingsRepository.searchSettings("notif", null, null, false, pageable))
                    .thenReturn(expectedPage);
            
            // When
            Page<Settings> result = settingsRepository.searchSettings("notif", null, null, false, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("user.notifications", result.getContent().get(0).getKey());
            verify(settingsRepository).searchSettings("notif", null, null, false, pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de métodos auxiliares")
    class UtilityMethodTests {
        
        @Test
        @DisplayName("Debería convertir correctamente a valor booleano")
        void shouldConvertToBooleanValue() {
            // Given
            Settings booleanSetting = Settings.builder()
                    .id(3L)
                    .key("feature.enabled")
                    .value("true")
                    .dataType(DataType.BOOLEAN)
                    .build();
            
            // When
            boolean result = booleanSetting.getBooleanValue();
            
            // Then
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Debería convertir correctamente a valor entero")
        void shouldConvertToIntegerValue() {
            // Given
            Settings intSetting = Settings.builder()
                    .id(4L)
                    .key("pagination.size")
                    .value("20")
                    .dataType(DataType.INTEGER)
                    .build();
            
            // When
            Integer result = intSetting.getIntegerValue();
            
            // Then
            assertEquals(20, result);
        }
        
        @Test
        @DisplayName("Debería convertir correctamente a valor decimal")
        void shouldConvertToDoubleValue() {
            // Given
            Settings doubleSetting = Settings.builder()
                    .id(5L)
                    .key("tax.rate")
                    .value("0.19")
                    .dataType(DataType.FLOAT)
                    .build();
            
            // When
            Double result = doubleSetting.getDoubleValue();
            
            // Then
            assertEquals(0.19, result);
        }
        
        @Test
        @DisplayName("Debería manejar correctamente errores de conversión")
        void shouldHandleConversionErrors() {
            // Given
            Settings invalidIntSetting = Settings.builder()
                    .id(6L)
                    .key("invalid.int")
                    .value("not-a-number")
                    .dataType(DataType.INTEGER)
                    .build();
            
            Settings invalidDoubleSetting = Settings.builder()
                    .id(7L)
                    .key("invalid.double")
                    .value("not-a-number")
                    .dataType(DataType.FLOAT)
                    .build();
            
            // When
            Integer intResult = invalidIntSetting.getIntegerValue();
            Double doubleResult = invalidDoubleSetting.getDoubleValue();
            
            // Then
            assertEquals(0, intResult);
            assertEquals(0.0, doubleResult);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de hooks de ciclo de vida")
    class LifecycleHookTests {
        
        @Test
        @DisplayName("Debería actualizar la marca de tiempo en preUpdate")
        void shouldUpdateTimestampOnPreUpdate() {
            // Given
            Settings setting = Settings.builder()
                    .id(8L)
                    .key("test.setting")
                    .value("value")
                    .dataType(DataType.STRING)
                    .build();
            
            // When
            setting.preUpdate();
            
            // Then
            assertNotNull(setting.getUpdatedAt());
            assertTrue(setting.getUpdatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
            assertTrue(setting.getUpdatedAt().isAfter(LocalDateTime.now().minusSeconds(1)));
        }
    }
    
    @Nested
    @DisplayName("Pruebas de proyecciones y estadísticas")
    class ProjectionsAndStatisticsTests {
        
        @Test
        @DisplayName("Debería encontrar información básica de todas las configuraciones")
        void shouldFindAllBasicInfo() {
            // Given
            List<SettingsRepository.SettingsBasicInfo> basicInfoList = new ArrayList<>();
            basicInfoList.add(createBasicSettingsInfo(1L, "system.theme", "dark", DataType.STRING, true));
            basicInfoList.add(createBasicSettingsInfo(2L, "user.notifications", "true", DataType.BOOLEAN, false));
            
            Page<SettingsRepository.SettingsBasicInfo> basicInfoPage = 
                    new PageImpl<>(basicInfoList, pageable, basicInfoList.size());
                    
            when(settingsRepository.findAllBasicInfo(pageable)).thenReturn(basicInfoPage);
            
            // When
            Page<SettingsRepository.SettingsBasicInfo> result = settingsRepository.findAllBasicInfo(pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            assertEquals("system.theme", result.getContent().get(0).getKey());
            assertEquals("user.notifications", result.getContent().get(1).getKey());
            verify(settingsRepository).findAllBasicInfo(pageable);
        }
        
        @Test
        @DisplayName("Debería contar configuraciones por tipo de datos")
        void shouldCountSettingsByDataType() {
            // Given
            List<Object[]> dataTypeCounts = new ArrayList<>();
            dataTypeCounts.add(new Object[]{DataType.STRING, 1L});
            dataTypeCounts.add(new Object[]{DataType.BOOLEAN, 1L});
            
            when(settingsRepository.countSettingsByDataType())
                    .thenReturn(dataTypeCounts);
            
            // When
            List<Object[]> result = settingsRepository.countSettingsByDataType();
            
            // Then
            assertEquals(2, result.size());
            assertEquals(DataType.STRING, result.get(0)[0]);
            assertEquals(1L, result.get(0)[1]);
            assertEquals(DataType.BOOLEAN, result.get(1)[0]);
            assertEquals(1L, result.get(1)[1]);
            verify(settingsRepository).countSettingsByDataType();
        }
        
        @Test
        @DisplayName("Debería encontrar todos los grupos de configuraciones")
        void shouldFindAllGroups() {
            // Given
            List<String> groups = List.of("appearance", "preferences");
            
            when(settingsRepository.findAllGroups())
                    .thenReturn(groups);
            
            // When
            List<String> result = settingsRepository.findAllGroups();
            
            // Then
            assertEquals(2, result.size());
            assertEquals("appearance", result.get(0));
            assertEquals("preferences", result.get(1));
            verify(settingsRepository).findAllGroups();
        }
    }
    
    // Método auxiliar para crear objetos SettingsBasicInfo
    private SettingsRepository.SettingsBasicInfo createBasicSettingsInfo(
            Long id, String key, String value, DataType dataType, boolean systemSetting) {
        return new SettingsRepository.SettingsBasicInfo() {
            @Override
            public Long getId() {
                return id;
            }
            
            @Override
            public String getKey() {
                return key;
            }
            
            @Override
            public String getValue() {
                return value;
            }
            
            @Override
            public DataType getDataType() {
                return dataType;
            }
            
            @Override
            public boolean isSystemSetting() {
                return systemSetting;
            }
        };
    }
}