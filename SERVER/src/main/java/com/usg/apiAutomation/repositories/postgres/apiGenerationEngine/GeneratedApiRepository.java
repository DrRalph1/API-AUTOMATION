package com.usg.apiAutomation.repositories.postgres.apiGenerationEngine;

import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GeneratedApiRepository extends JpaRepository<GeneratedApiEntity, String> {

    Optional<GeneratedApiEntity> findByApiCode(String apiCode);

    List<GeneratedApiEntity> findByOwnerOrderByCreatedAtDesc(String owner);

    Page<GeneratedApiEntity> findByOwner(String owner, Pageable pageable);

    Page<GeneratedApiEntity> findByStatus(String status, Pageable pageable);

    @Query("SELECT a FROM GeneratedApiEntity a WHERE " +
            "LOWER(a.apiName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.apiCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<GeneratedApiEntity> searchApis(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT a FROM GeneratedApiEntity a WHERE a.isActive = true")
    List<GeneratedApiEntity> findAllActive();

    @Query("SELECT COUNT(a) FROM GeneratedApiEntity a WHERE a.createdBy = :userId")
    long countByCreatedBy(@Param("userId") String userId);

    @Query("SELECT a FROM GeneratedApiEntity a WHERE a.lastCalledAt > :since")
    List<GeneratedApiEntity> findRecentlyCalled(@Param("since") LocalDateTime since);

    @Query("SELECT a.owner, COUNT(a) FROM GeneratedApiEntity a GROUP BY a.owner")
    List<Object[]> getApiStatsByOwner();

    @Query("SELECT a.status, COUNT(a) FROM GeneratedApiEntity a GROUP BY a.status")
    List<Object[]> getApiStatsByStatus();

    boolean existsByApiCode(String apiCode);
}