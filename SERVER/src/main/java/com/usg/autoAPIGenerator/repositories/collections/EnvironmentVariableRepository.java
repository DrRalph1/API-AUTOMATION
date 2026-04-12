package com.usg.autoAPIGenerator.repositories.collections;

import com.usg.autoAPIGenerator.entities.postgres.collections.EnvironmentVariableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvironmentVariableRepository extends JpaRepository<EnvironmentVariableEntity, String> {

    // Find all variables for a specific environment
    List<EnvironmentVariableEntity> findByEnvironmentId(String environmentId);

    // Find variables by environment and key
    Optional<EnvironmentVariableEntity> findByEnvironmentIdAndKey(String environmentId, String key);

    // Find all enabled variables for an environment
    List<EnvironmentVariableEntity> findByEnvironmentIdAndIsEnabledTrue(String environmentId);

    // Find variables by type
    List<EnvironmentVariableEntity> findByType(String type);

    // Find variables by environment and multiple keys
    @Query("SELECT e FROM EnvironmentVariablesEntityCollections e WHERE e.environment.id = :environmentId AND e.key IN :keys")
    List<EnvironmentVariableEntity> findByEnvironmentIdAndKeys(@Param("environmentId") String environmentId,
                                                               @Param("keys") List<String> keys);

    // Count variables in an environment
    long countByEnvironmentId(String environmentId);

    // Delete all variables for an environment
    @Modifying
    @Query("DELETE FROM EnvironmentVariablesEntityCollections e WHERE e.environment.id = :environmentId")
    void deleteByEnvironmentId(@Param("environmentId") String environmentId);

    // Find variables by generated API ID
    List<EnvironmentVariableEntity> findByGeneratedApiId(String generatedApiId);

    // Find variables by environment and search term in key or value
    @Query("SELECT e FROM EnvironmentVariablesEntityCollections e WHERE e.environment.id = :environmentId " +
            "AND (LOWER(e.key) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(e.value) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<EnvironmentVariableEntity> searchByEnvironmentIdAndTerm(@Param("environmentId") String environmentId,
                                                                 @Param("searchTerm") String searchTerm);

    // Check if a key exists in an environment
    @Query("SELECT COUNT(e) > 0 FROM EnvironmentVariablesEntityCollections e " +
            "WHERE e.environment.id = :environmentId AND e.key = :key")
    boolean existsByEnvironmentIdAndKey(@Param("environmentId") String environmentId,
                                        @Param("key") String key);

    // Get all keys for an environment
    @Query("SELECT e.key FROM EnvironmentVariablesEntityCollections e WHERE e.environment.id = :environmentId")
    List<String> findKeysByEnvironmentId(@Param("environmentId") String environmentId);

    // Bulk update enabled status
    @Modifying
    @Query("UPDATE EnvironmentVariablesEntityCollections e SET e.isEnabled = :enabled " +
            "WHERE e.environment.id = :environmentId")
    int updateEnabledByEnvironmentId(@Param("environmentId") String environmentId,
                                     @Param("enabled") boolean enabled);

    // Find variables by environment ordered by key
    List<EnvironmentVariableEntity> findByEnvironmentIdOrderByKeyAsc(String environmentId);

    // Find variables with specific keys across all environments
    @Query("SELECT e FROM EnvironmentVariablesEntityCollections e WHERE e.key IN :keys")
    List<EnvironmentVariableEntity> findByKeys(@Param("keys") List<String> keys);

    // Find variables by value pattern (useful for finding variables with specific values)
    @Query("SELECT e FROM EnvironmentVariablesEntityCollections e WHERE e.value LIKE %:valuePattern%")
    List<EnvironmentVariableEntity> findByValueContaining(@Param("valuePattern") String valuePattern);

    // Find duplicate keys within an environment
    @Query("SELECT e.key, COUNT(e) FROM EnvironmentVariablesEntityCollections e " +
            "WHERE e.environment.id = :environmentId GROUP BY e.key HAVING COUNT(e) > 1")
    List<Object[]> findDuplicateKeysInEnvironment(@Param("environmentId") String environmentId);

    // Find variables by environment and type with pagination (if needed)
    // This would typically be used with Pageable parameter
    // List<EnvironmentVariableEntity> findByEnvironmentIdAndType(String environmentId, String type, Pageable pageable);
}