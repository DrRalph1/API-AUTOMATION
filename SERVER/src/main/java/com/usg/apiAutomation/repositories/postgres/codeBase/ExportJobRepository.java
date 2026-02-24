package com.usg.apiAutomation.repositories.postgres.codeBase;

import com.usg.apiAutomation.entities.postgres.codeBase.ExportJobEntity;
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
public interface ExportJobRepository extends JpaRepository<ExportJobEntity, String> {

    /**
     * Find export jobs by status ordered by creation date descending
     */
    List<ExportJobEntity> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Find export jobs by status with pagination
     */
    Page<ExportJobEntity> findByStatus(String status, Pageable pageable);

    /**
     * Find export job by download URL
     */
    Optional<ExportJobEntity> findByDownloadUrl(String downloadUrl);

    /**
     * Find export jobs created before a specific date
     */
    List<ExportJobEntity> findByCreatedAtBefore(LocalDateTime dateTime);

    /**
     * Find export jobs that expire before a specific date
     */
    List<ExportJobEntity> findByExpiresAtBefore(LocalDateTime dateTime);

    /**
     * Find export jobs by language
     */
    List<ExportJobEntity> findByLanguage(String language);

    /**
     * Find export jobs by request ID
     */
    List<ExportJobEntity> findByRequestId(String requestId);

    /**
     * Find export jobs by collection ID
     */
    List<ExportJobEntity> findByCollectionId(String collectionId);

    /**
     * Find export jobs by language and status
     */
    List<ExportJobEntity> findByLanguageAndStatus(String language, String status);

    /**
     * Find recent export jobs by user (assuming performedBy is stored in metadata or added as field)
     * Note: You may need to add performedBy field to ExportJobEntity if you want to track users
     */
    @Query("SELECT e FROM ExportJobEntity e WHERE e.createdAt > :since ORDER BY e.createdAt DESC")
    List<ExportJobEntity> findRecentExports(@Param("since") LocalDateTime since);

    /**
     * Count export jobs by status
     */
    @Query("SELECT COUNT(e) FROM ExportJobEntity e WHERE e.status = :status")
    long countByStatus(@Param("status") String status);

    /**
     * Get export job statistics by status
     */
    @Query("SELECT e.status, COUNT(e) FROM ExportJobEntity e GROUP BY e.status")
    List<Object[]> getExportStatistics();

    /**
     * Find expired export jobs
     */
    @Query("SELECT e FROM ExportJobEntity e WHERE e.expiresAt < :now AND e.status = 'READY'")
    List<ExportJobEntity> findExpiredJobs(@Param("now") LocalDateTime now);

    /**
     * Delete export jobs by status and creation date
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ExportJobEntity e WHERE e.status = :status AND e.createdAt < :cutoffDate")
    int deleteOldJobsByStatus(@Param("status") String status, @Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Update job status
     */
    @Modifying
    @Transactional
    @Query("UPDATE ExportJobEntity e SET e.status = :status, e.completedAt = :completedAt WHERE e.id = :id")
    int updateJobStatus(@Param("id") String id, @Param("status") String status, @Param("completedAt") LocalDateTime completedAt);

    /**
     * Update job with download URL and file size
     */
    @Modifying
    @Transactional
    @Query("UPDATE ExportJobEntity e SET e.downloadUrl = :downloadUrl, e.fileSize = :fileSize, e.status = 'READY', e.completedAt = :completedAt WHERE e.id = :id")
    int updateJobWithDownloadInfo(@Param("id") String id,
                                  @Param("downloadUrl") String downloadUrl,
                                  @Param("fileSize") String fileSize,
                                  @Param("completedAt") LocalDateTime completedAt);

    /**
     * Find jobs that have been processing for too long (stuck jobs)
     */
    @Query("SELECT e FROM ExportJobEntity e WHERE e.status = 'PROCESSING' AND e.createdAt < :timeout")
    List<ExportJobEntity> findStuckJobs(@Param("timeout") LocalDateTime timeout);

    /**
     * Search export jobs by various criteria
     */
    @Query("SELECT e FROM ExportJobEntity e WHERE " +
            "(:language IS NULL OR e.language = :language) AND " +
            "(:status IS NULL OR e.status = :status) AND " +
            "(:format IS NULL OR e.format = :format) AND " +
            "(:requestId IS NULL OR e.requestId = :requestId) AND " +
            "(:collectionId IS NULL OR e.collectionId = :collectionId) AND " +
            "e.createdAt BETWEEN :startDate AND :endDate")
    List<ExportJobEntity> searchExportJobs(
            @Param("language") String language,
            @Param("status") String status,
            @Param("format") String format,
            @Param("requestId") String requestId,
            @Param("collectionId") String collectionId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get total size of exports by language
     */
    @Query("SELECT e.language, SUM(CAST(SUBSTRING(e.fileSize, 1, LENGTH(e.fileSize)-2) AS double)) FROM ExportJobEntity e " +
            "WHERE e.status = 'READY' GROUP BY e.language")
    List<Object[]> getTotalExportSizeByLanguage();

    /**
     * Find duplicate export jobs (same parameters created recently)
     */
    @Query("SELECT e FROM ExportJobEntity e WHERE " +
            "e.language = :language AND " +
            "e.format = :format AND " +
            "((:requestId IS NULL AND e.requestId IS NULL) OR e.requestId = :requestId) AND " +
            "((:collectionId IS NULL AND e.collectionId IS NULL) OR e.collectionId = :collectionId) AND " +
            "e.createdAt > :since")
    List<ExportJobEntity> findDuplicateExports(
            @Param("language") String language,
            @Param("format") String format,
            @Param("requestId") String requestId,
            @Param("collectionId") String collectionId,
            @Param("since") LocalDateTime since);

    /**
     * Get export job count by day for reporting
     */
    @Query("SELECT DATE(e.createdAt), COUNT(e) FROM ExportJobEntity e " +
            "WHERE e.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(e.createdAt) ORDER BY DATE(e.createdAt)")
    List<Object[]> getExportCountByDay(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Update multiple jobs status in batch
     */
    @Modifying
    @Transactional
    @Query("UPDATE ExportJobEntity e SET e.status = :status WHERE e.id IN :ids")
    int batchUpdateStatus(@Param("ids") List<String> ids, @Param("status") String status);

    /**
     * Delete all expired jobs
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ExportJobEntity e WHERE e.expiresAt < :now")
    int deleteAllExpired(@Param("now") LocalDateTime now);
}