package com.usg.apiAutomation.repositories.postgres.apiGenerationEngine;

import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.ApiTestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiTestRepository extends JpaRepository<ApiTestEntity, String> {

    List<ApiTestEntity> findByGeneratedApiIdOrderByExecutedAtDesc(String apiId);

    List<ApiTestEntity> findByGeneratedApiIdAndStatus(String apiId, String status);

    @Query("SELECT COUNT(t) FROM ApiTestEntity t WHERE t.generatedApi.id = :apiId AND t.status = 'PASSED'")
    long countPassedTests(@Param("apiId") String apiId);

    @Query("SELECT t.testType, COUNT(t), AVG(t.executionTimeMs) " +
            "FROM ApiTestEntity t WHERE t.generatedApi.id = :apiId GROUP BY t.testType")
    List<Object[]> getTestStatsByType(@Param("apiId") String apiId);
}