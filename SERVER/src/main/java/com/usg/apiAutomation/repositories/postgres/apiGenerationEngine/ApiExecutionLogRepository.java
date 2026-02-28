package com.usg.apiAutomation.repositories.postgres.apiGenerationEngine;

import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.ApiExecutionLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApiExecutionLogRepository extends JpaRepository<ApiExecutionLogEntity, String> {

    Page<ApiExecutionLogEntity> findByGeneratedApiIdOrderByExecutedAtDesc(String apiId, Pageable pageable);

    List<ApiExecutionLogEntity> findByGeneratedApiIdAndExecutedAtBetween(
            String apiId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(l) FROM ApiExecutionLogEntity l WHERE l.generatedApi.id = :apiId")
    long countByApiId(@Param("apiId") String apiId);

    @Query("SELECT AVG(l.executionTimeMs) FROM ApiExecutionLogEntity l WHERE l.generatedApi.id = :apiId")
    Double getAverageExecutionTime(@Param("apiId") String apiId);

    @Query("SELECT l.responseStatus, COUNT(l) FROM ApiExecutionLogEntity l " +
            "WHERE l.generatedApi.id = :apiId GROUP BY l.responseStatus")
    List<Object[]> getStatusDistribution(@Param("apiId") String apiId);

    @Query("SELECT DATE(l.executedAt), COUNT(l) FROM ApiExecutionLogEntity l " +
            "WHERE l.generatedApi.id = :apiId AND l.executedAt > :since " +
            "GROUP BY DATE(l.executedAt) ORDER BY DATE(l.executedAt)")
    List<Object[]> getDailyCallStats(@Param("apiId") String apiId, @Param("since") LocalDateTime since);
}