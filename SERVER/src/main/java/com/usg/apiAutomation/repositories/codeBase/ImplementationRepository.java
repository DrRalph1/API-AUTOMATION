package com.usg.apiAutomation.repositories.codeBase;

import com.usg.apiAutomation.entities.codeBase.ImplementationEntity;
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
public interface ImplementationRepository extends JpaRepository<ImplementationEntity, String> {

    // ========== Basic find operations ==========
    @Query("SELECT i FROM ImplementationEntity i WHERE i.request.id = :requestId")
    List<ImplementationEntity> findByRequestId(@Param("requestId") String requestId);

    @Query("SELECT i FROM ImplementationEntity i WHERE i.request.id = :requestId")
    Page<ImplementationEntity> findByRequestId(@Param("requestId") String requestId, Pageable pageable);

    @Query("SELECT i FROM ImplementationEntity i WHERE i.request.id = :requestId AND i.language = :language AND i.component = :component")
    Optional<ImplementationEntity> findByRequestIdAndLanguageAndComponent(
            @Param("requestId") String requestId,
            @Param("language") String language,
            @Param("component") String component);

    // ========== Find by request and language ==========
    @Query("SELECT i FROM ImplementationEntity i WHERE i.request.id = :requestId AND i.language = :language")
    List<ImplementationEntity> findByRequestIdAndLanguage(
            @Param("requestId") String requestId,
            @Param("language") String language);

    // ========== Find languages by request ID ==========
    @Query("SELECT DISTINCT i.language FROM ImplementationEntity i WHERE i.request.id = :requestId ORDER BY i.language")
    List<String> findLanguagesByRequestId(@Param("requestId") String requestId);

    // ========== Find distinct languages ==========
    @Query("SELECT DISTINCT i.language FROM ImplementationEntity i ORDER BY i.language")
    List<String> findDistinctLanguages();

    // ========== Find top by language ==========
    @Query("SELECT i FROM ImplementationEntity i WHERE i.language = :language ORDER BY i.createdAt DESC")
    List<ImplementationEntity> findTopByLanguage(@Param("language") String language, Pageable pageable);

    // ========== Count by language ==========
    @Query("SELECT COUNT(i) FROM ImplementationEntity i WHERE i.language = :language")
    int countByLanguage(@Param("language") String language);

    // ========== Find by language ==========
    List<ImplementationEntity> findByLanguage(String language);

    Page<ImplementationEntity> findByLanguage(String language, Pageable pageable);

    // ========== Find by request ordered ==========
    @Query("SELECT i FROM ImplementationEntity i WHERE i.request.id = :requestId ORDER BY i.createdAt DESC")
    List<ImplementationEntity> findByRequestIdOrderByCreatedAtDesc(@Param("requestId") String requestId);

    // ========== Find by collection ==========
    @Query("SELECT i FROM ImplementationEntity i WHERE i.request.collection.id = :collectionId")
    List<ImplementationEntity> findByCollectionId(@Param("collectionId") String collectionId);

    @Query("SELECT COUNT(i) FROM ImplementationEntity i WHERE i.request.collection.id = :collectionId")
    long countByCollectionId(@Param("collectionId") String collectionId);

    // ========== Find by component ==========
    List<ImplementationEntity> findByComponent(String component);

    // ========== Find by validation status ==========
    List<ImplementationEntity> findByIsValidatedTrue();

    List<ImplementationEntity> findByIsValidatedFalse();

    @Query("SELECT i FROM ImplementationEntity i WHERE i.validationScore < :threshold")
    List<ImplementationEntity> findByLowValidationScore(@Param("threshold") int threshold);

    // ========== Find by framework ==========
    List<ImplementationEntity> findByFramework(String framework);

    // ========== Find by test status ==========
    List<ImplementationEntity> findByLastTestStatus(String status);

    @Query("SELECT i FROM ImplementationEntity i WHERE i.lastTestedAt < :since")
    List<ImplementationEntity> findNotTestedSince(@Param("since") LocalDateTime since);

    // ========== Find by usage ==========
    List<ImplementationEntity> findByUsageCountGreaterThanOrderByUsageCountDesc(int minUsage);

    @Query("SELECT i FROM ImplementationEntity i ORDER BY i.usageCount DESC")
    List<ImplementationEntity> findMostUsedImplementations(Pageable pageable);

    // ========== Find recent ==========
    @Query("SELECT i FROM ImplementationEntity i WHERE i.createdAt > :since ORDER BY i.createdAt DESC")
    List<ImplementationEntity> findRecentImplementations(@Param("since") LocalDateTime since, Pageable pageable);

    // ========== Find by git info ==========
    List<ImplementationEntity> findByGitBranch(String gitBranch);

    List<ImplementationEntity> findByGitCommit(String gitCommit);

    // ========== Search implementations ==========
    @Query("SELECT i FROM ImplementationEntity i WHERE " +
            "(:language IS NULL OR i.language = :language) AND " +
            "(:component IS NULL OR i.component LIKE %:component%) AND " +
            "(:isValidated IS NULL OR i.isValidated = :isValidated) AND " +
            "(:framework IS NULL OR i.framework = :framework) AND " +
            "(:requestId IS NULL OR i.request.id = :requestId)")
    List<ImplementationEntity> searchImplementations(
            @Param("requestId") String requestId,
            @Param("language") String language,
            @Param("component") String component,
            @Param("isValidated") Boolean isValidated,
            @Param("framework") String framework);

    // ========== Search in code ==========
    @Query("SELECT i FROM ImplementationEntity i WHERE i.code LIKE %:searchTerm%")
    List<ImplementationEntity> searchInCode(@Param("searchTerm") String searchTerm);

    // ========== Language statistics ==========
    @Query("SELECT i.language, COUNT(i), AVG(i.linesOfCode), AVG(i.validationScore), SUM(i.usageCount) " +
            "FROM ImplementationEntity i GROUP BY i.language")
    List<Object[]> getLanguageStatistics();

    @Query("SELECT i.language, AVG(i.validationScore) FROM ImplementationEntity i " +
            "WHERE i.isValidated = true GROUP BY i.language")
    List<Object[]> getAverageValidationScoreByLanguage();

    @Query("SELECT i.language, AVG(i.linesOfCode) FROM ImplementationEntity i GROUP BY i.language")
    List<Object[]> getAverageLinesOfCodeByLanguage();

    // ========== Component statistics ==========
    @Query("SELECT i.component, COUNT(i) FROM ImplementationEntity i GROUP BY i.component")
    List<Object[]> getComponentStatistics();

    // ========== Framework statistics ==========
    @Query("SELECT i.framework, COUNT(i) FROM ImplementationEntity i WHERE i.framework IS NOT NULL GROUP BY i.framework")
    List<Object[]> getFrameworkStatistics();

    // ========== Test statistics ==========
    @Query("SELECT i.lastTestStatus, COUNT(i) FROM ImplementationEntity i WHERE i.lastTestStatus IS NOT NULL GROUP BY i.lastTestStatus")
    List<Object[]> getTestStatusStatistics();

    @Query("SELECT AVG(i.testCoverage) FROM ImplementationEntity i WHERE i.testCoverage IS NOT NULL")
    Double getAverageTestCoverage();

    // ========== Version history ==========
    @Query("SELECT i FROM ImplementationEntity i WHERE i.request.id = :requestId AND i.language = :language ORDER BY i.version DESC")
    List<ImplementationEntity> findVersionHistory(
            @Param("requestId") String requestId,
            @Param("language") String language);

    // FIXED: Removed Pageable parameter since we want a single Optional result
    @Query("SELECT i FROM ImplementationEntity i WHERE i.request.id = :requestId AND i.language = :language ORDER BY i.version DESC")
    Optional<ImplementationEntity> findLatestVersion(
            @Param("requestId") String requestId,
            @Param("language") String language);

    // ALTERNATIVE: If you need pagination, use Page return type
    @Query("SELECT i FROM ImplementationEntity i WHERE i.request.id = :requestId AND i.language = :language ORDER BY i.version DESC")
    Page<ImplementationEntity> findLatestVersionPaginated(
            @Param("requestId") String requestId,
            @Param("language") String language,
            Pageable pageable);

    // ========== Check existence ==========
    @Query("SELECT COUNT(i) > 0 FROM ImplementationEntity i WHERE " +
            "i.request.id = :requestId AND i.language = :language AND i.component = :component")
    boolean existsByRequestIdAndLanguageAndComponent(
            @Param("requestId") String requestId,
            @Param("language") String language,
            @Param("component") String component);

    // ========== Delete operations ==========
    @Modifying
    @Transactional
    @Query("DELETE FROM ImplementationEntity i WHERE i.request.id = :requestId")
    void deleteByRequestId(@Param("requestId") String requestId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ImplementationEntity i WHERE i.request.id = :requestId AND i.language = :language")
    void deleteByRequestIdAndLanguage(@Param("requestId") String requestId, @Param("language") String language);

    @Modifying
    @Transactional
    @Query("DELETE FROM ImplementationEntity i WHERE i.request.id = :requestId AND i.language = :language AND i.component = :component")
    void deleteByRequestIdAndLanguageAndComponent(
            @Param("requestId") String requestId,
            @Param("language") String language,
            @Param("component") String component);

    // ========== Update operations ==========
    @Modifying
    @Transactional
    @Query("UPDATE ImplementationEntity i SET i.usageCount = i.usageCount + 1 WHERE i.id = :id")
    void incrementUsageCount(@Param("id") String id);

    @Modifying
    @Transactional
    @Query("UPDATE ImplementationEntity i SET i.isValidated = :isValidated, i.validationScore = :score WHERE i.id = :id")
    void updateValidationStatus(@Param("id") String id, @Param("isValidated") Boolean isValidated, @Param("score") Integer score);

    @Modifying
    @Transactional
    @Query("UPDATE ImplementationEntity i SET i.lastTestStatus = :status, i.testCoverage = :coverage, i.lastTestedAt = :testedAt WHERE i.id = :id")
    void updateTestStatus(
            @Param("id") String id,
            @Param("status") String status,
            @Param("coverage") Double coverage,
            @Param("testedAt") LocalDateTime testedAt);

    // ========== Batch operations ==========
    @Modifying
    @Transactional
    @Query("UPDATE ImplementationEntity i SET i.isValidated = false WHERE i.request.id = :requestId")
    void invalidateAllForRequest(@Param("requestId") String requestId);

    @Modifying
    @Transactional
    @Query("UPDATE ImplementationEntity i SET i.usageCount = i.usageCount + 1 WHERE i.id IN :ids")
    void batchIncrementUsageCount(@Param("ids") List<String> ids);

    // ========== Metadata queries ==========
    @Query("SELECT i FROM ImplementationEntity i WHERE JSONB_EXTRACT_PATH_TEXT(i.metadata, :key) = :value")
    List<ImplementationEntity> findByMetadataKeyValue(
            @Param("key") String key,
            @Param("value") String value);

    // ========== Time-based queries ==========
    List<ImplementationEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<ImplementationEntity> findByUpdatedAtAfter(LocalDateTime since);

    @Query("SELECT i FROM ImplementationEntity i WHERE i.createdAt < :date")
    List<ImplementationEntity> findOlderThan(@Param("date") LocalDateTime date);

    // ========== Generator info ==========
    List<ImplementationEntity> findByGeneratedBy(String generatedBy);

    @Query("SELECT i.generatedBy, COUNT(i) FROM ImplementationEntity i WHERE i.isGenerated = true GROUP BY i.generatedBy")
    List<Object[]> getGenerationStatisticsByUser();

    // ========== Complex aggregations ==========
    @Query("SELECT " +
            "MAX(i.linesOfCode), " +
            "MIN(i.linesOfCode), " +
            "AVG(i.linesOfCode), " +
            "MAX(i.validationScore), " +
            "MIN(i.validationScore), " +
            "AVG(i.validationScore) " +
            "FROM ImplementationEntity i WHERE i.language = :language")
    List<Object[]> getImplementationMetricsByLanguage(@Param("language") String language);

    @Query("SELECT DATE(i.createdAt), COUNT(i) FROM ImplementationEntity i " +
            "WHERE i.createdAt BETWEEN :start AND :end " +
            "GROUP BY DATE(i.createdAt) ORDER BY DATE(i.createdAt)")
    List<Object[]> getDailyImplementationCount(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // ========== Top implementations ==========
    @Query("SELECT i FROM ImplementationEntity i WHERE i.language = :language ORDER BY i.usageCount DESC")
    List<ImplementationEntity> findTopImplementationsByLanguage(
            @Param("language") String language,
            Pageable pageable);

    @Query("SELECT i FROM ImplementationEntity i WHERE i.isValidated = true ORDER BY i.validationScore DESC")
    List<ImplementationEntity> findTopValidatedImplementations(Pageable pageable);
}