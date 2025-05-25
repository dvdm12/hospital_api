package com.example.miapp.repository;

import com.example.miapp.entity.Role;
import com.example.miapp.entity.Role.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for {@link Role} entity.
 * Provides methods for CRUD operations and custom queries related to role management.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    
    /**
     * Finds a role by its name.
     *
     * @param name the role name to search for
     * @return an Optional containing the role if found, or empty if not found
     */
    Optional<Role> findByName(ERole name);
    
    /**
     * Checks if a role with the given name exists.
     *
     * @param name the role name to check
     * @return true if a role with the name exists, false otherwise
     */
    boolean existsByName(ERole name);
    
    /**
     * Finds multiple roles by their names.
     *
     * @param names set of role names to search for
     * @return list of roles matching the provided names
     */
    List<Role> findByNameIn(Set<ERole> names);
    
    /**
     * Retrieves the list of all role names.
     *
     * @return list of all role names (ERole enum values)
     */
    @Query("SELECT r.name FROM Role r")
    List<ERole> findAllRoleNames();
    
    /**
     * Counts the number of users assigned to each role.
     * Returns a list of arrays containing [roleName, userCount]
     *
     * @return list of arrays with role statistics
     */
    @Query("SELECT r.name, COUNT(u) FROM Role r LEFT JOIN User u JOIN u.roles r2 ON r.id = r2.id GROUP BY r.name")
    List<Object[]> countUsersByRole();
    
    /**
     * Finds roles that are assigned to a specific number of users or more.
     *
     * @param userCount minimum number of users assigned to the role
     * @return list of roles with at least the specified number of users
     */
    @Query("SELECT r FROM Role r JOIN User u JOIN u.roles r2 ON r.id = r2.id GROUP BY r HAVING COUNT(u) >= :userCount")
    List<Role> findRolesWithUserCountGreaterThanEqual(@Param("userCount") long userCount);
    
    /**
     * Finds roles that are not assigned to any user.
     *
     * @return list of unused roles
     */
    @Query("SELECT r FROM Role r WHERE r.id NOT IN (SELECT r2.id FROM User u JOIN u.roles r2)")
    List<Role> findUnusedRoles();
    
    /**
     * Custom projection interface for basic role information.
     */
    interface RoleBasicInfo {
        Integer getId();
        ERole getName();
    }
    
    /**
     * Finds all roles and returns only basic information.
     *
     * @return list of RoleBasicInfo projections
     */
    @Query("SELECT r.id as id, r.name as name FROM Role r")
    List<RoleBasicInfo> findAllBasicInfo();
    
    /**
     * Finds roles assigned to a specific user.
     *
     * @param userId the ID of the user
     * @return list of roles assigned to the user
     */
    @Query("SELECT r FROM User u JOIN u.roles r WHERE u.id = :userId")
    List<Role> findRolesByUserId(@Param("userId") Long userId);
    
    /**
     * Checks if a user has a specific role.
     *
     * @param userId the ID of the user
     * @param roleName the role name to check
     * @return true if the user has the role, false otherwise
     */
    @Query("SELECT COUNT(r) > 0 FROM User u JOIN u.roles r WHERE u.id = :userId AND r.name = :roleName")
    boolean hasUserRole(@Param("userId") Long userId, @Param("roleName") ERole roleName);
    
    /**
     * Retrieves the roles that are assigned to at least one user.
     *
     * @return list of roles that are in use
     */
    @Query("SELECT DISTINCT r FROM User u JOIN u.roles r")
    List<Role> findRolesInUse();
}