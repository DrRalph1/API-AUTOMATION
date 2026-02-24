package com.usg.apiAutomation.repositories.postgres.codeBase;

import com.usg.apiAutomation.entities.postgres.codeBase.TestResultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestResultRepository extends JpaRepository<TestResultEntity, String> {

    // Basic find operations
    List<TestResultEntity> findByRequestIdOrderByTestedAtDesc(String requestId);

    Page<TestResultEntity> findByRequestId(String requestId, Pageable pageable);

    List<TestResultEntity> findByCollectionIdOrderByTestedAtDesc(String collectionId);

    List<TestResultEntity> findByLanguageOrderByTestedAtDesc(String language);

    List<TestResultEntity> findByTestedByOrderByTestedAtDesc(String testedBy);

    // Find latest test results
    Optional<TestResultEntity> findFirstByRequestIdAndLanguageOrderByTestedAtDesc(
            @Param("requestId") String requestId,
            @Param("language") String language);

    Optional<TestResultEntity> findFirstByRequestIdOrderByTestedAtDesc(String requestId);

    // Find by status
    List<TestResultEntity> findByStatusOrderByTestedAtDesc(String status);

    Page<TestResultEntity> findByStatus(String status, Pageable pageable);

    // Find by test type
    List<TestResultEntity> findByRequestIdAndTestTypeOrderByTestedAtDesc(
            @Param("requestId") String requestId,
            @Param("testType") String testType);

    // Find by date range
    List<TestResultEntity> findByTestedAtBetweenOrderByTestedAtDesc(
            LocalDateTime startDate,
            LocalDateTime endDate);

    // Find failed tests
    @Query("SELECT t FROM TestResultEntityCodeBase t WHERE t.testsFailed > 0 ORDER BY t.testedAt DESC")
    List<TestResultEntity> findFailedTests();

    @Query("SELECT t FROM TestResultEntityCodeBase t WHERE t.requestId = :requestId AND t.testsFailed > 0")
    List<TestResultEntity> findFailedTestsByRequestId(@Param("requestId") String requestId);

    // Statistical queries
    @Query("SELECT AVG(t.successRate) FROM TestResultEntityCodeBase t WHERE t.requestId = :requestId")
    Double getAverageSuccessRateByRequestId(@Param("requestId") String requestId);

    @Query("SELECT AVG(t.testsPassed * 100.0 / t.totalTests) FROM TestResultEntityCodeBase t " +
            "WHERE t.requestId = :requestId AND t.language = :language")
    Double getAverageSuccessRateByRequestAndLanguage(
            @Param("requestId") String requestId,
            @Param("language") String language);

    @Query("SELECT t.language, AVG(t.successRate) FROM TestResultEntityCodeBase t " +
            "WHERE t.requestId = :requestId GROUP BY t.language")
    List<Object[]> getAverageSuccessRateByLanguage(@Param("requestId") String requestId);

    // Trend analysis
    @Query("SELECT DATE(t.testedAt), AVG(t.successRate) FROM TestResultEntityCodeBase t " +
            "WHERE t.requestId = :requestId AND t.testedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(t.testedAt) ORDER BY DATE(t.testedAt)")
    List<Object[]> getSuccessRateTrend(
            @Param("requestId") String requestId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Performance metrics

    // Baseline comparisons
    @Query("SELECT t FROM TestResultEntityCodeBase t WHERE t.requestId = :requestId AND t.isBaseline = true")
    Optional<TestResultEntity> findBaselineByRequestId(@Param("requestId") String requestId);

    @Query("SELECT t FROM TestResultEntityCodeBase t WHERE t.baselineId = :baselineId ORDER BY t.testedAt DESC")
    List<TestResultEntity> findTestRunsComparedToBaseline(@Param("baselineId") String baselineId);

    // Flaky tests
    @Query("SELECT t FROM TestResultEntityCodeBase t WHERE t.flakyTests > 0 ORDER BY t.flakyTests DESC")
    List<TestResultEntity> findTestRunsWithFlakyTests();

    @Query("SELECT t.requestId, AVG(t.flakyTests) FROM TestResultEntityCodeBase t " +
            "WHERE t.flakyTests > 0 GROUP BY t.requestId")
    List<Object[]> getAverageFlakyTestsByRequest();

    // Coverage analysis
    @Query("SELECT t FROM TestResultEntityCodeBase t WHERE t.coverage IS NOT NULL " +
            "AND t.requestId = :requestId ORDER BY t.testedAt DESC")
    List<TestResultEntity> findCoverageReportsByRequestId(@Param("requestId") String requestId);

    @Query("SELECT MAX(CAST(SUBSTRING(t.coverage, 1, LENGTH(t.coverage)-1) AS double)) " +
            "FROM TestResultEntityCodeBase t WHERE t.requestId = :requestId")
    Double getMaxCoverageByRequestId(@Param("requestId") String requestId);

    // Time-based queries
    @Query("SELECT t FROM TestResultEntityCodeBase t WHERE t.testedAt > :since " +
            "AND t.testsFailed > 0 ORDER BY t.testedAt DESC")
    List<TestResultEntity> findRecentFailures(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(t) FROM TestResultEntityCodeBase t WHERE t.testedAt > :since")
    long countTestsRunSince(@Param("since") LocalDateTime since);

    // Aggregation queries
    @Query("SELECT t.status, COUNT(t) FROM TestResultEntityCodeBase t GROUP BY t.status")
    List<Object[]> getTestResultStatistics();

    @Query("SELECT t.testType, COUNT(t), AVG(t.successRate) FROM TestResultEntityCodeBase t " +
            "WHERE t.testedAt > :since GROUP BY t.testType")
    List<Object[]> getTestTypeStatistics(@Param("since") LocalDateTime since);

    // Search with multiple criteria
    @Query("SELECT t FROM TestResultEntityCodeBase t WHERE " +
            "(:requestId IS NULL OR t.requestId = :requestId) AND " +
            "(:language IS NULL OR t.language = :language) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:testType IS NULL OR t.testType = :testType) AND " +
            "(:minSuccessRate IS NULL OR t.successRate >= :minSuccessRate) AND " +
            "t.testedAt BETWEEN :startDate AND :endDate")
    List<TestResultEntity> searchTestResults(
            @Param("requestId") String requestId,
            @Param("language") String language,
            @Param("status") String status,
            @Param("testType") String testType,
            @Param("minSuccessRate") Double minSuccessRate,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Delete old test results
    @Modifying
    @Transactional
    @Query("DELETE FROM TestResultEntityCodeBase t WHERE t.testedAt < :cutoffDate AND t.isBaseline = false")
    int deleteOldTestResults(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Update operations
    @Modifying
    @Transactional
    @Query("UPDATE TestResultEntityCodeBase t SET t.isBaseline = false " +
            "WHERE t.requestId = :requestId AND t.id != :currentBaselineId")
    int clearOtherBaselines(
            @Param("requestId") String requestId,
            @Param("currentBaselineId") String currentBaselineId);

    // Most/Least stable requests
    @Query("SELECT t.requestId, t.requestName, AVG(t.successRate) as avgRate " +
            "FROM TestResultEntityCodeBase t GROUP BY t.requestId, t.requestName " +
            "ORDER BY avgRate DESC")
    List<Object[]> findMostStableRequests(Pageable pageable);

    @Query("SELECT t.requestId, t.requestName, AVG(t.successRate) as avgRate " +
            "FROM TestResultEntityCodeBase t GROUP BY t.requestId, t.requestName " +
            "ORDER BY avgRate ASC")
    List<Object[]> findLeastStableRequests(Pageable pageable);

    // Performance percentiles
    @Query("SELECT PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY t.executionTime) " +
            "FROM TestResultEntityCodeBase t WHERE t.requestId = :requestId")
    Double getMedianExecutionTime(@Param("requestId") String requestId);

    @Query("SELECT PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY t.executionTime) " +
            "FROM TestResultEntityCodeBase t WHERE t.requestId = :requestId")
    Double getP95ExecutionTime(@Param("requestId") String requestId);

    // Recent test runs with details
    @Query("SELECT t FROM TestResultEntityCodeBase t WHERE t.requestId IN :requestIds " +
            "AND t.testedAt IN (SELECT MAX(t2.testedAt) FROM TestResultEntityCodeBase t2 " +
            "WHERE t2.requestId = t.requestId GROUP BY t2.requestId)")
    List<TestResultEntity> findLatestTestResultsForRequests(@Param("requestIds") List<String> requestIds);

    // Count by date for reporting
    @Query("SELECT DATE(t.testedAt), COUNT(t), AVG(t.successRate) " +
            "FROM TestResultEntityCodeBase t WHERE t.testedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(t.testedAt) ORDER BY DATE(t.testedAt)")
    List<Object[]> getDailyTestStats(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}