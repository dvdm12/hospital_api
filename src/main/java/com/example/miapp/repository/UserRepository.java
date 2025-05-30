package com.example.miapp.repository;

import com.example.miapp.entity.Role;
import com.example.miapp.entity.User;
import com.example.miapp.entity.User.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for {@link User} entity.
 * Provides methods for CRUD operations and custom queries related to user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     *
     * @param email the email to search for
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user with the given username exists.
     *
     * @param username the username to check
     * @return true if a user with the username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
 * Verifica si existe un usuario con la cédula proporcionada
 * 
 * @param cc número de cédula a verificar
 * @return true si existe un usuario con esa cédula, false en caso contrario
 */
boolean existsByCc(String cc);

    /**
     * Checks if a user with the given email exists.
     *
     * @param email the email to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds all users with a specific status.
     *
     * @param status the status to filter by
     * @param pageable pagination information
     * @return a Page of users with the specified status
     */
    Page<User> findByStatus(UserStatus status, Pageable pageable);

    /**
     * Finds users that have a specific role.
     *
     * @param roleName the role name to search for
     * @param pageable pagination information
     * @return a Page of users with the specified role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") Role.ERole roleName, Pageable pageable);

    /**
     * Finds users by username pattern and status.
     *
     * @param usernamePattern the pattern to match against usernames (using SQL LIKE)
     * @param status the status to filter by
     * @param pageable pagination information
     * @return a Page of matching users
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:usernamePattern% AND u.status = :status")
    Page<User> findByUsernameContainingAndStatus(
            @Param("usernamePattern") String usernamePattern,
            @Param("status") UserStatus status,
            Pageable pageable);

    /**
     * Updates a user's status.
     *
     * @param userId the ID of the user to update
     * @param status the new status
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :userId")
    int updateUserStatus(@Param("userId") Long userId, @Param("status") UserStatus status);

    /**
     * Updates a user's last login timestamp.
     *
     * @param userId the ID of the user to update
     * @param timestamp the new last login timestamp
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :timestamp WHERE u.id = :userId")
    int updateLastLogin(@Param("userId") Long userId, @Param("timestamp") Long timestamp);

    /**
     * Finds users who haven't logged in yet (first-time users).
     *
     * @param pageable pagination information
     * @return a Page of users who haven't logged in yet
     */
    Page<User> findByFirstLoginTrue(Pageable pageable);

    /**
     * Counts users by status.
     *
     * @param status the status to count
     * @return the count of users with the specified status
     */
    long countByStatus(UserStatus status);

    /**
     * Finds users with specific statuses.
     *
     * @param statuses the set of statuses to include
     * @param pageable pagination information
     * @return a Page of users with any of the specified statuses
     */
    @Query("SELECT u FROM User u WHERE u.status IN :statuses")
    Page<User> findByStatusIn(@Param("statuses") Set<UserStatus> statuses, Pageable pageable);

    /**
     * Searches for users by username, email, or status.
     * This is a flexible search that can match any of the provided criteria.
     *
     * @param username optional username to search for (can be null)
     * @param email optional email to search for (can be null)
     * @param status optional status to filter by (can be null)
     * @param pageable pagination information
     * @return a Page of users matching any of the provided criteria
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.roles r WHERE " +
           "(:username IS NULL OR u.username LIKE %:username%) AND " +
           "(:email IS NULL OR u.email LIKE %:email%) AND " +
           "(:status IS NULL OR u.status = :status)")
    Page<User> searchUsers(
            @Param("username") String username,
            @Param("email") String email,
            @Param("status") UserStatus status,
            Pageable pageable);

    /**
     * Finds users who haven't logged in since a specific timestamp.
     *
     * @param timestamp the timestamp threshold
     * @param pageable pagination information
     * @return a Page of users who haven't logged in since the specified timestamp
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin < :timestamp OR u.lastLogin IS NULL")
    Page<User> findInactiveUsers(@Param("timestamp") Long timestamp, Pageable pageable);

    /**
     * Retrieves a summary of user counts by status.
     *
     * @return a list of arrays containing [status, count]
     */
    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    List<Object[]> countUsersByStatus();

    /**
     * Retrieves a summary of user counts by role.
     *
     * @return a list of arrays containing [roleName, count]
     */
    @Query("SELECT r.name, COUNT(DISTINCT u) FROM User u JOIN u.roles r GROUP BY r.name")
    List<Object[]> countUsersByRole();

    /**
     * Finds users with a specific role and status.
     *
     * @param roleName the role name to search for
     * @param status the status to filter by
     * @param pageable pagination information
     * @return a Page of users with the specified role and status
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.status = :status")
    Page<User> findByRoleNameAndStatus(
            @Param("roleName") Role.ERole roleName,
            @Param("status") UserStatus status,
            Pageable pageable);

    /**
     * Custom projection interface for basic user information.
     */
    interface UserBasicInfo {
        Long getId();
        String getUsername();
        String getEmail();
        UserStatus getStatus();
    }

    /**
     * Finds all users and returns only basic information.
     *
     * @param pageable pagination information
     * @return a Page of UserBasicInfo projections
     */
    @Query("SELECT u.id as id, u.username as username, u.email as email, u.status as status FROM User u")
    Page<UserBasicInfo> findAllBasicInfo(Pageable pageable);

    /**
     * Finds users by a specific role and returns basic information.
     *
     * @param roleName the role to filter by
     * @param pageable pagination information
     * @return a Page of UserBasicInfo projections
     */
    @Query("SELECT u.id as id, u.username as username, u.email as email, u.status as status " +
           "FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<UserBasicInfo> findByRoleNameBasicInfo(
            @Param("roleName") Role.ERole roleName,
            Pageable pageable);
}