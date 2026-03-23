package com.usg.apiAutomation.repositories.apiGenerationEngine;

import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.ApiRequestEntity;
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
public interface ApiRequestRepository extends JpaRepository<ApiRequestEntity, String> {

    // =====================================================
    // Basic Find Operations
    // =====================================================

    Optional<ApiRequestEntity> findByCorrelationId(String correlationId);

    List<ApiRequestEntity> findByRequestStatus(String requestStatus);

    Page<ApiRequestEntity> findByRequestStatus(String requestStatus, Pageable pageable);

    List<ApiRequestEntity> findByHttpMethod(String httpMethod);

    Page<ApiRequestEntity> findByHttpMethod(String httpMethod, Pageable pageable);

    List<ApiRequestEntity> findByResponseStatusCode(Integer statusCode);

    Page<ApiRequestEntity> findByResponseStatusCode(Integer statusCode, Pageable pageable);

    // =====================================================
    // API References
    // =====================================================

    List<ApiRequestEntity> findByGeneratedApiId(String apiId);

    Page<ApiRequestEntity> findByGeneratedApiId(String apiId, Pageable pageable);

    List<ApiRequestEntity> findByGeneratedApiApiCode(String apiCode);

    long countByGeneratedApiId(String apiId);

    void deleteByGeneratedApiId(String apiId);

    // =====================================================
    // Time-based Queries
    // =====================================================

    List<ApiRequestEntity> findByRequestTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<ApiRequestEntity> findByRequestTimestampAfter(LocalDateTime since);

    List<ApiRequestEntity> findByRequestTimestampBefore(LocalDateTime before);

    List<ApiRequestEntity> findByResponseTimestampAfter(LocalDateTime since);

    @Query("SELECT r FROM ApiRequestEntity r WHERE CAST(r.requestTimestamp AS date) = CURRENT_DATE")
    List<ApiRequestEntity> findTodaysRequests();

    @Query("SELECT r FROM ApiRequestEntity r WHERE r.requestTimestamp >= :since ORDER BY r.requestTimestamp DESC")
    List<ApiRequestEntity> findRecentRequests(@Param("since") LocalDateTime since, Pageable pageable);

    // =====================================================
    // Performance and Duration Queries
    // =====================================================

    List<ApiRequestEntity> findByExecutionDurationMsGreaterThan(Long threshold);

    List<ApiRequestEntity> findByExecutionDurationMsBetween(Long min, Long max);

    @Query("SELECT r FROM ApiRequestEntity r WHERE r.executionDurationMs > :threshold ORDER BY r.executionDurationMs DESC")
    List<ApiRequestEntity> findSlowRequests(@Param("threshold") Long threshold);

    @Query("SELECT AVG(r.executionDurationMs) FROM ApiRequestEntity r WHERE r.generatedApi.id = :apiId")
    Double getAverageResponseTimeByApiId(@Param("apiId") String apiId);

    @Query("SELECT MAX(r.executionDurationMs) FROM ApiRequestEntity r WHERE r.generatedApi.id = :apiId")
    Long getMaxResponseTimeByApiId(@Param("apiId") String apiId);

    @Query("SELECT MIN(r.executionDurationMs) FROM ApiRequestEntity r WHERE r.generatedApi.id = :apiId")
    Long getMinResponseTimeByApiId(@Param("apiId") String apiId);

    // =====================================================
    // Search Operations
    // =====================================================

    @Query("SELECT r FROM ApiRequestEntity r WHERE " +
            "LOWER(r.requestName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.url) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.correlationId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.errorMessage) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<ApiRequestEntity> searchRequests(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT r FROM ApiRequestEntity r WHERE " +
            "r.generatedApi.id = :apiId AND (" +
            "LOWER(r.requestName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.correlationId) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<ApiRequestEntity> searchRequestsByApiId(
            @Param("apiId") String apiId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);


    @Query("SELECT r FROM ApiRequestEntity r WHERE " +
            "r.generatedApi.id = :apiId AND " +
            "r.requestTimestamp BETWEEN :startDate AND :endDate")
    List<ApiRequestEntity> findApiRequestsByApiIdAndDateRange(
            @Param("apiId") String apiId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // =====================================================
    // Statistics and Aggregations
    // =====================================================

    @Query("SELECT r.requestStatus, COUNT(r) FROM ApiRequestEntity r GROUP BY r.requestStatus")
    List<Object[]> getRequestStatsByStatus();

    @Query("SELECT r.responseStatusCode, COUNT(r) FROM ApiRequestEntity r GROUP BY r.responseStatusCode")
    List<Object[]> getRequestStatsByResponseCode();

    @Query("SELECT r.httpMethod, COUNT(r) FROM ApiRequestEntity r GROUP BY r.httpMethod")
    List<Object[]> getRequestStatsByHttpMethod();

    @Query("SELECT DATE(r.requestTimestamp), COUNT(r) FROM ApiRequestEntity r " +
            "WHERE r.requestTimestamp BETWEEN :start AND :end GROUP BY DATE(r.requestTimestamp)")
    List<Object[]> getRequestCountByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT r.generatedApi.id, COUNT(r) FROM ApiRequestEntity r " +
            "GROUP BY r.generatedApi.id ORDER BY COUNT(r) DESC")
    List<Object[]> getMostCalledApis(Pageable pageable);

    @Query("SELECT r.requestedBy, COUNT(r) FROM ApiRequestEntity r " +
            "WHERE r.requestedBy IS NOT NULL GROUP BY r.requestedBy ORDER BY COUNT(r) DESC")
    List<Object[]> getTopRequesters(Pageable pageable);

    // =====================================================
    // Error and Failure Queries
    // =====================================================

    List<ApiRequestEntity> findByRequestStatusIn(List<String> statuses);

    @Query("SELECT r FROM ApiRequestEntity r WHERE r.responseStatusCode >= 400")
    List<ApiRequestEntity> findFailedRequests();

    @Query("SELECT r FROM ApiRequestEntity r WHERE r.responseStatusCode >= 500")
    List<ApiRequestEntity> findServerErrorRequests();

    @Query("SELECT r FROM ApiRequestEntity r WHERE r.errorMessage IS NOT NULL")
    List<ApiRequestEntity> findRequestsWithErrors();

    @Query("SELECT r FROM ApiRequestEntity r WHERE r.retryCount > 0")
    List<ApiRequestEntity> findRetriedRequests();

    @Query("SELECT r FROM ApiRequestEntity r WHERE r.retryCount > :threshold")
    List<ApiRequestEntity> findRequestsWithHighRetries(@Param("threshold") Integer threshold);

    // =====================================================
    // JSONB Queries
    // =====================================================

    @Query(value = "SELECT * FROM tb_eng_api_requests WHERE headers @> jsonb_build_object(:key, :value)", nativeQuery = true)
    List<ApiRequestEntity> findByHeaderKeyValue(
            @Param("key") String key,
            @Param("value") String value);

    @Query(value = "SELECT * FROM tb_eng_api_requests WHERE request_body @> :jsonPath", nativeQuery = true)
    List<ApiRequestEntity> findByRequestBodyContaining(@Param("jsonPath") String jsonPath);

    @Query(value = "SELECT * FROM tb_eng_api_requests WHERE response_body @> :jsonPath", nativeQuery = true)
    List<ApiRequestEntity> findByResponseBodyContaining(@Param("jsonPath") String jsonPath);

    @Query(value = "SELECT * FROM tb_eng_api_requests WHERE jsonb_exists(query_parameters, :paramName)", nativeQuery = true)
    List<ApiRequestEntity> findByQueryParameterExists(@Param("paramName") String paramName);

    @Query(value = "SELECT * FROM tb_eng_api_requests WHERE jsonb_exists(path_parameters, :paramName)", nativeQuery = true)
    List<ApiRequestEntity> findByPathParameterExists(@Param("paramName") String paramName);

    @Query(value = "SELECT * FROM tb_eng_api_requests WHERE metadata @> jsonb_build_object(:key, :value)", nativeQuery = true)
    List<ApiRequestEntity> findByMetadataKeyValue(
            @Param("key") String key,
            @Param("value") Object value);

    // =====================================================
    // Client Information Queries
    // =====================================================

    List<ApiRequestEntity> findByClientIpAddress(String ipAddress);

    List<ApiRequestEntity> findByUserAgentContaining(String userAgent);

    List<ApiRequestEntity> findBySourceApplication(String sourceApplication);

    List<ApiRequestEntity> findByRequestedBy(String requestedBy);

    @Query("SELECT r.clientIpAddress, COUNT(r) FROM ApiRequestEntity r GROUP BY r.clientIpAddress ORDER BY COUNT(r) DESC")
    List<Object[]> getTopClientIPs(Pageable pageable);

    // =====================================================
    // Authentication Queries
    // =====================================================

    List<ApiRequestEntity> findByAuthType(String authType);

    @Query("SELECT r.authType, COUNT(r) FROM ApiRequestEntity r GROUP BY r.authType")
    List<Object[]> getRequestStatsByAuthType();

    // =====================================================
    // Mock Request Queries
    // =====================================================

    List<ApiRequestEntity> findByIsMockRequestTrue();

    List<ApiRequestEntity> findByIsMockRequestFalse();

    // =====================================================
    // Complex Custom Queries
    // =====================================================

    @Query("SELECT r FROM ApiRequestEntity r WHERE " +
            "r.generatedApi.id = :apiId AND " +
            "r.requestStatus = :status AND " +
            "r.requestTimestamp BETWEEN :startDate AND :endDate")
    List<ApiRequestEntity> findApiRequestsByStatusAndDateRange(
            @Param("apiId") String apiId,
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM ApiRequestEntity r WHERE " +
            "r.responseStatusCode IN :statusCodes AND " +
            "r.executionDurationMs > :duration")
    List<ApiRequestEntity> findSlowFailedRequests(
            @Param("statusCodes") List<Integer> statusCodes,
            @Param("duration") Long duration);

    @Query("SELECT r FROM ApiRequestEntity r WHERE " +
            "r.generatedApi.id = :apiId AND " +
            "r.requestTimestamp = (SELECT MAX(r2.requestTimestamp) FROM ApiRequestEntity r2 WHERE r2.generatedApi.id = :apiId)")
    Optional<ApiRequestEntity> findLatestRequestByApiId(@Param("apiId") String apiId);

    @Query("SELECT DISTINCT r.correlationId FROM ApiRequestEntity r WHERE r.generatedApi.id = :apiId")
    List<String> findDistinctCorrelationIdsByApiId(@Param("apiId") String apiId);

    // =====================================================
    // Pagination and Sorting
    // =====================================================

    Page<ApiRequestEntity> findAllByOrderByRequestTimestampDesc(Pageable pageable);

    Page<ApiRequestEntity> findByGeneratedApiIdOrderByRequestTimestampDesc(String apiId, Pageable pageable);

    Page<ApiRequestEntity> findByRequestStatusOrderByRequestTimestampDesc(String status, Pageable pageable);

    // =====================================================
    // Existence Checks
    // =====================================================

    boolean existsByCorrelationId(String correlationId);

    boolean existsByGeneratedApiIdAndRequestStatus(String apiId, String status);

    // =====================================================
    // Count Operations
    // =====================================================

    long countByRequestTimestampAfter(LocalDateTime since);

    long countByResponseStatusCodeBetween(int start, int end);

    long countByGeneratedApiIdAndRequestTimestampBetween(String apiId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(r) FROM ApiRequestEntity r WHERE r.generatedApi.id = :apiId AND r.responseStatusCode = 200")
    long countSuccessfulRequestsByApiId(@Param("apiId") String apiId);

    @Query("SELECT COUNT(r) FROM ApiRequestEntity r WHERE r.generatedApi.id = :apiId AND r.responseStatusCode >= 400")
    long countFailedRequestsByApiId(@Param("apiId") String apiId);

    // =====================================================
    // Delete Operations
    // =====================================================

    long deleteByRequestTimestampBefore(LocalDateTime before);

    long deleteByRequestStatus(String status);


    /**
     * Find all requests for a specific API within a date range
     */
    List<ApiRequestEntity> findByGeneratedApiIdAndRequestTimestampBetween(
            String apiId,
            LocalDateTime start,
            LocalDateTime end);


    // Add these methods for summary calculation
    List<ApiRequestEntity> findAllByGeneratedApiId(String apiId);

    List<ApiRequestEntity> findAllByRequestStatus(String requestStatus);

}