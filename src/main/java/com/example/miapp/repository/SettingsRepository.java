package com.example.miapp.repository;

import com.example.miapp.entity.Settings;
import com.example.miapp.entity.Settings.DataType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Settings} entity.
 * Provides methods for CRUD operations and custom queries related to system and user settings.
 */
@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {
    
    /**
     * Finds a setting by its key.
     *
     * @param key the key to search for
     * @return an Optional containing the setting if found, or empty if not found
     */
    Optional<Settings> findByKey(String key);
    
    /**
     * Finds settings by group.
     *
     * @param group the group to search for
     * @return a list of settings in the specified group
     */
    List<Settings> findByGroup(String group);
    
    /**
     * Finds system-wide settings.
     *
     * @return a list of system settings
     */
    List<Settings> findBySystemSettingTrue();
    
    /**
     * Finds user-specific settings.
     *
     * @return a list of user-specific settings
     */
    List<Settings> findBySystemSettingFalse();
    
    /**
     * Finds settings for a specific user.
     *
     * @param userId the ID of the user
     * @return a list of settings for the specified user
     */
    List<Settings> findByUserId(Long userId);
    
    /**
     * Finds a specific setting for a user.
     *
     * @param userId the ID of the user
     * @param key the key of the setting
     * @return an Optional containing the setting if found, or empty if not found
     */
    Optional<Settings> findByUserIdAndKey(Long userId, String key);
    
    /**
     * Finds visible settings.
     *
     * @return a list of visible settings
     */
    List<Settings> findByVisibleTrue();
    
    /**
     * Finds editable settings.
     *
     * @return a list of editable settings
     */
    List<Settings> findByEditableTrue();
    
    /**
     * Finds settings that are both visible and editable.
     *
     * @return a list of visible and editable settings
     */
    List<Settings> findByVisibleTrueAndEditableTrue();
    
    /**
     * Finds settings by data type.
     *
     * @param dataType the data type to search for
     * @return a list of settings with the specified data type
     */
    List<Settings> findByDataType(DataType dataType);
    
    /**
     * Updates the value of a setting.
     *
     * @param settingId the ID of the setting to update
     * @param value the new value
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE Settings s SET s.value = :value, s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :settingId")
    int updateValue(@Param("settingId") Long settingId, @Param("value") String value);
    
    /**
     * Updates the visibility of a setting.
     *
     * @param settingId the ID of the setting to update
     * @param visible the new visibility status
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE Settings s SET s.visible = :visible, s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :settingId")
    int updateVisibility(@Param("settingId") Long settingId, @Param("visible") boolean visible);
    
    /**
     * Updates the editability of a setting.
     *
     * @param settingId the ID of the setting to update
     * @param editable the new editability status
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE Settings s SET s.editable = :editable, s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :settingId")
    int updateEditability(@Param("settingId") Long settingId, @Param("editable") boolean editable);
    
    /**
     * Updates the description of a setting.
     *
     * @param settingId the ID of the setting to update
     * @param description the new description
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE Settings s SET s.description = :description, s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :settingId")
    int updateDescription(@Param("settingId") Long settingId, @Param("description") String description);
    
    /**
     * Advanced search for settings by multiple criteria.
     *
     * @param keyPattern optional pattern to search for in key
     * @param group optional group to filter by
     * @param dataType optional data type to filter by
     * @param systemSetting optional system setting flag to filter by
     * @param pageable pagination information
     * @return a Page of settings matching the criteria
     */
    @Query("SELECT s FROM Settings s WHERE " +
           "(:keyPattern IS NULL OR LOWER(s.key) LIKE LOWER(CONCAT('%', :keyPattern, '%'))) " +
           "AND (:group IS NULL OR s.group = :group) " +
           "AND (:dataType IS NULL OR s.dataType = :dataType) " +
           "AND (:systemSetting IS NULL OR s.systemSetting = :systemSetting)")
    Page<Settings> searchSettings(
            @Param("keyPattern") String keyPattern,
            @Param("group") String group,
            @Param("dataType") DataType dataType,
            @Param("systemSetting") Boolean systemSetting,
            Pageable pageable);
    
    /**
     * Finds settings with keys matching a pattern.
     *
     * @param keyPattern the pattern to match against keys
     * @param pageable pagination information
     * @return a Page of settings with keys matching the pattern
     */
    @Query("SELECT s FROM Settings s WHERE LOWER(s.key) LIKE LOWER(CONCAT('%', :keyPattern, '%'))")
    Page<Settings> findByKeyContainingIgnoreCase(@Param("keyPattern") String keyPattern, Pageable pageable);
    
    /**
     * Finds user settings with keys matching a pattern.
     *
     * @param userId the ID of the user
     * @param keyPattern the pattern to match against keys
     * @param pageable pagination information
     * @return a Page of user settings with keys matching the pattern
     */
    @Query("SELECT s FROM Settings s WHERE s.user.id = :userId " +
           "AND LOWER(s.key) LIKE LOWER(CONCAT('%', :keyPattern, '%'))")
    Page<Settings> findByUserIdAndKeyContainingIgnoreCase(
            @Param("userId") Long userId,
            @Param("keyPattern") String keyPattern,
            Pageable pageable);
    
    /**
     * Creates a new user setting based on a system setting template.
     *
     * @param userId the ID of the user
     * @param systemSettingKey the key of the system setting to use as template
     * @param value the value for the new user setting
     * @return the number of affected rows
     */
    @Modifying
    @Query(value = "INSERT INTO settings (user_id, `key`, value, `group`, description, data_type, system_setting, visible, editable) " +
                   "SELECT :userId, REPLACE(s.`key`, 'system.', 'user.'), :value, s.`group`, s.description, s.data_type, 0, s.visible, s.editable " +
                   "FROM settings s WHERE s.`key` = :systemSettingKey AND s.system_setting = 1", 
            nativeQuery = true)
    int createUserSettingFromTemplate(
            @Param("userId") Long userId,
            @Param("systemSettingKey") String systemSettingKey,
            @Param("value") String value);
    
    /**
     * Custom projection interface for basic settings information.
     */
    interface SettingsBasicInfo {
        Long getId();
        String getKey();
        String getValue();
        DataType getDataType();
        boolean isSystemSetting();
    }
    
    /**
     * Finds all settings and returns only basic information.
     *
     * @param pageable pagination information
     * @return a Page of SettingsBasicInfo projections
     */
    @Query("SELECT s.id as id, s.key as key, s.value as value, " +
           "s.dataType as dataType, s.systemSetting as systemSetting " +
           "FROM Settings s")
    Page<SettingsBasicInfo> findAllBasicInfo(Pageable pageable);
    
    /**
     * Finds settings by user and returns only basic information.
     *
     * @param userId the ID of the user
     * @param pageable pagination information
     * @return a Page of SettingsBasicInfo projections
     */
    @Query("SELECT s.id as id, s.key as key, s.value as value, " +
           "s.dataType as dataType, s.systemSetting as systemSetting " +
           "FROM Settings s WHERE s.user.id = :userId")
    Page<SettingsBasicInfo> findBasicInfoByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Retrieves all unique setting groups.
     *
     * @return a list of unique group names
     */
    @Query("SELECT DISTINCT s.group FROM Settings s WHERE s.group IS NOT NULL ORDER BY s.group")
    List<String> findAllGroups();
    
    /**
     * Counts settings by data type.
     *
     * @return a list of arrays containing [dataType, count]
     */
    @Query("SELECT s.dataType, COUNT(s) FROM Settings s GROUP BY s.dataType")
    List<Object[]> countSettingsByDataType();
    
    /**
     * Deletes user settings for a specific user.
     *
     * @param userId the ID of the user
     * @return the number of affected rows
     */
    @Modifying
    @Query("DELETE FROM Settings s WHERE s.user.id = :userId")
    int deleteUserSettings(@Param("userId") Long userId);
    
    /**
     * Restores default values for user settings.
     *
     * @param userId the ID of the user
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE Settings s SET s.value = " +
           "(SELECT s2.value FROM Settings s2 WHERE s2.key = REPLACE(s.key, 'user.', 'system.') AND s2.systemSetting = true), " +
           "s.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.user.id = :userId AND s.systemSetting = false")
    int restoreDefaultValues(@Param("userId") Long userId);
}