package com.usg.apiAutomation.repositories.postgres.codeBase;

import com.usg.apiAutomation.entities.postgres.codeBase.ImportJobEntity;
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
public interface ImportJobRepository extends JpaRepository<ImportJobEntity, String> {

    /**
     * Find import jobs by status ordered by creation date descending
     */
    List<ImportJobEntity> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Find import jobs by status with pagination
     */
    Page<ImportJobEntity> findByStatus(String status, Pageable pageable);

    /**
     * Find import jobs by source ordered by creation date descending
     */
    List<ImportJobEntity> findBySourceOrderByCreatedAtDesc(String source);

    /**
     * Find import jobs by source and status
     */
    List<ImportJobEntity> findBySourceAndStatus(String source, String status);

    /**
     * Find import job by collection ID
     */
    Optional<ImportJobEntity> findByCollectionId(String collectionId);

    /**
     * Find import jobs created after a specific date
     */
    List<ImportJobEntity> findByCreatedAtAfter(LocalDateTime dateTime);

    /**
     * Find import jobs by format
     */
    List<ImportJobEntity> findByFormat(String format);

    /**
     * Find successful imports with generated implementations
     */
    @Query("SELECT i FROM ImportJobEntity i WHERE i.status = 'COMPLETED' AND i.implementationsGenerated > 0")
    List<ImportJobEntity> findSuccessfulImportsWithImplementations();

    /**
     * Count import jobs by status
     */
    @Query("SELECT COUNT(i) FROM ImportJobEntity i WHERE i.status = :status")
    long countByStatus(@Param("status") String status);

    /**
     * Get import statistics by source
     */
    @Query("SELECT i.source, COUNT(i), AVG(i.endpointsImported), AVG(i.implementationsGenerated) FROM ImportJobEntity i GROUP BY i.source")
    List<Object[]> getImportStatisticsBySource();

    /**
     * Get import statistics by status
     */
    @Query("SELECT i.status, COUNT(i) FROM ImportJobEntity i GROUP BY i.status")
    List<Object[]> getImportStatisticsByStatus();

    /**
     * Find imports that took too long (potential performance issues)
     * FIXED: Using native query for PostgreSQL EXTRACT function
     */
    @Query(value = "SELECT * FROM tb_cbase_import_jobs WHERE status = 'COMPLETED' AND " +
            "EXTRACT(EPOCH FROM (completed_at - created_at)) > :thresholdSeconds",
            nativeQuery = true)
    List<ImportJobEntity> findSlowImports(@Param("thresholdSeconds") long thresholdSeconds);

    /**
     * Alternative approach using Java calculation (if you prefer JPQL)
     * This can be used in your service layer instead of the native query
     */
    @Query("SELECT i FROM ImportJobEntity i WHERE i.status = 'COMPLETED'")
    List<ImportJobEntity> findAllCompletedJobs();

    /**
     * Update import job status
     */
    @Modifying
    @Transactional
    @Query("UPDATE ImportJobEntity i SET i.status = :status, i.completedAt = :completedAt WHERE i.id = :id")
    int updateJobStatus(@Param("id") String id, @Param("status") String status, @Param("completedAt") LocalDateTime completedAt);

    /**
     * Update import job with results
     */
    @Modifying
    @Transactional
    @Query("UPDATE ImportJobEntity i SET i.status = 'COMPLETED', i.collectionId = :collectionId, " +
            "i.endpointsImported = :endpointsImported, i.implementationsGenerated = :implementationsGenerated, " +
            "i.importData = :importData, i.completedAt = :completedAt WHERE i.id = :id")
    int updateJobWithResults(
            @Param("id") String id,
            @Param("collectionId") String collectionId,
            @Param("endpointsImported") int endpointsImported,
            @Param("implementationsGenerated") int implementationsGenerated,
            @Param("importData") Object importData,
            @Param("completedAt") LocalDateTime completedAt);

    /**
     * Find stuck import jobs (processing for too long)
     */
    @Query("SELECT i FROM ImportJobEntity i WHERE i.status = 'PROCESSING' AND i.createdAt < :timeout")
    List<ImportJobEntity> findStuckJobs(@Param("timeout") LocalDateTime timeout);

    /**
     * Search import jobs by various criteria
     */
    @Query("SELECT i FROM ImportJobEntity i WHERE " +
            "(:source IS NULL OR i.source = :source) AND " +
            "(:status IS NULL OR i.status = :status) AND " +
            "(:format IS NULL OR i.format = :format) AND " +
            "i.createdAt BETWEEN :startDate AND :endDate")
    List<ImportJobEntity> searchImportJobs(
            @Param("source") String source,
            @Param("status") String status,
            @Param("format") String format,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get import job count by day for reporting
     */
    @Query("SELECT DATE(i.createdAt), COUNT(i) FROM ImportJobEntity i " +
            "WHERE i.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(i.createdAt) ORDER BY DATE(i.createdAt)")
    List<Object[]> getImportCountByDay(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get total endpoints imported by source
     */
    @Query("SELECT i.source, SUM(i.endpointsImported) FROM ImportJobEntity i " +
            "WHERE i.status = 'COMPLETED' GROUP BY i.source")
    List<Object[]> getTotalEndpointsImportedBySource();

    /**
     * Find imports with no endpoints imported (failed or empty imports)
     */
    @Query("SELECT i FROM ImportJobEntity i WHERE i.status = 'COMPLETED' AND i.endpointsImported = 0")
    List<ImportJobEntity> findEmptyImports();

    /**
     * Delete old import jobs
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ImportJobEntity i WHERE i.createdAt < :cutoffDate")
    int deleteOldImports(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find recent imports by source
     */
    List<ImportJobEntity> findTop10BySourceOrderByCreatedAtDesc(String source);

    /**
     * Get average endpoints per import by source
     */
    @Query("SELECT i.source, AVG(i.endpointsImported) FROM ImportJobEntity i " +
            "WHERE i.status = 'COMPLETED' GROUP BY i.source")
    List<Object[]> getAverageEndpointsBySource();

    /**
     * Find imports that generated the most implementations
     */
    @Query("SELECT i FROM ImportJobEntity i WHERE i.status = 'COMPLETED' ORDER BY i.implementationsGenerated DESC")
    List<ImportJobEntity> findTopImportingJobs(Pageable pageable);

    /**
     * Check if a collection was created by import
     */
    @Query("SELECT COUNT(i) > 0 FROM ImportJobEntity i WHERE i.collectionId = :collectionId")
    boolean isCollectionFromImport(@Param("collectionId") String collectionId);

    /**
     * Update import job failure
     */
    @Modifying
    @Transactional
    @Query("UPDATE ImportJobEntity i SET i.status = 'FAILED', i.importData = :errorData, " +
            "i.completedAt = :completedAt WHERE i.id = :id")
    int markAsFailed(
            @Param("id") String id,
            @Param("errorData") Object errorData,
            @Param("completedAt") LocalDateTime completedAt);

    /**
     * Get success rate by source
     */
    @Query("SELECT i.source, " +
            "SUM(CASE WHEN i.status = 'COMPLETED' THEN 1 ELSE 0 END) * 100.0 / COUNT(i) " +
            "FROM ImportJobEntity i GROUP BY i.source")
    List<Object[]> getSuccessRateBySource();

    /**
     * Find duplicate imports (same source and format within time window)
     */
    @Query("SELECT i FROM ImportJobEntity i WHERE " +
            "i.source = :source AND i.format = :format AND " +
            "i.createdAt > :since AND i.status = 'COMPLETED'")
    List<ImportJobEntity> findDuplicateImports(
            @Param("source") String source,
            @Param("format") String format,
            @Param("since") LocalDateTime since);
}