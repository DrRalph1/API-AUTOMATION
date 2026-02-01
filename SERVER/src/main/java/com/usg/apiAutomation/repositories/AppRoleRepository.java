package com.usg.apiAutomation.repositories;

import com.usg.apiAutomation.entities.AppRoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppRoleRepository extends JpaRepository<AppRoleEntity, UUID>, JpaSpecificationExecutor<AppRoleEntity> {

    // Basic CRUD operations are inherited from JpaRepository

    // Existence check with case-insensitive search
    boolean existsByRoleNameIgnoreCase(String roleName);

    // Find by role name with case-insensitive search
    Optional<AppRoleEntity> findByRoleNameIgnoreCase(String roleName);

    // Find all roles with pagination (inherited but we can add custom queries)
    Page<AppRoleEntity> findAll(Pageable pageable);

    // Custom query to find roles by name containing (case-insensitive)
    @Query("SELECT r FROM AppRoleEntity r WHERE LOWER(r.roleName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<AppRoleEntity> findByRoleNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    // Custom query to find roles by description containing (case-insensitive)
    @Query("SELECT r FROM AppRoleEntity r WHERE LOWER(r.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    Page<AppRoleEntity> findByDescriptionContainingIgnoreCase(@Param("description") String description, Pageable pageable);

    // Find multiple roles by their IDs
    List<AppRoleEntity> findByRoleIdIn(List<UUID> roleIds);

    // Check if any of the given role names exist
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM AppRoleEntity r WHERE LOWER(r.roleName) IN :roleNames")
    boolean existsByRoleNamesIgnoreCase(@Param("roleNames") List<String> roleNames);

    // Find roles by exact name match (case-insensitive) - multiple
    List<AppRoleEntity> findByRoleNameIgnoreCaseIn(List<String> roleNames);

    // Count total number of roles
    @Query("SELECT COUNT(r) FROM AppRoleEntity r")
    long countAllRoles();

    // Get all roles ordered by name
    List<AppRoleEntity> findAllByOrderByRoleNameAsc();

    // Check if role exists by ID (inherited from JpaRepository but we can add custom)
    boolean existsById(UUID roleId);

    // Find roles created within a date range (if you have created_date field)
    // @Query("SELECT r FROM AppRoleEntity r WHERE r.createdDate BETWEEN :startDate AND :endDate")
    // List<AppRoleEntity> findRolesByDateRange(@Param("startDate") LocalDateTime startDate,
    //                                           @Param("endDate") LocalDateTime endDate);
}