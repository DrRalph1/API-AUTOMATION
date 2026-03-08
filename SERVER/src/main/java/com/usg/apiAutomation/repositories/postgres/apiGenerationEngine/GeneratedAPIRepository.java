package com.usg.apiAutomation.repositories.postgres.apiGenerationEngine;

import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
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
public interface GeneratedAPIRepository extends JpaRepository<GeneratedApiEntity, String> {

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

    @Query("SELECT g FROM GeneratedApiEntity g WHERE g.endpointPath = :endpointPath")
    Optional<GeneratedApiEntity> findByEndpointPath(@Param("endpointPath") String endpointPath);

    boolean existsByApiCode(String apiCode);

    // ============= JSONB QUERIES =============

    @Query(value = "SELECT * FROM tb_eng_generated_apis WHERE source_object_info @> jsonb_build_object('requestId', :requestId)", nativeQuery = true)
    Optional<GeneratedApiEntity> findBySourceObjectInfoRequestId(@Param("requestId") String requestId);

    @Query(value = "SELECT * FROM tb_eng_generated_apis WHERE collection_info @> jsonb_build_object('requestId', :requestId)", nativeQuery = true)
    Optional<GeneratedApiEntity> findByCollectionInfoRequestId(@Param("requestId") String requestId);

    @Query(value = "SELECT g.* FROM tb_eng_generated_apis g " +
            "JOIN tb_col_requests r ON r.id = :requestId " +
            "WHERE r.collection_id::text = g.collection_info ->> 'collectionId' " +
            "AND (r.folder_id::text = g.collection_info ->> 'folderId' OR " +
            "    (r.folder_id IS NULL AND g.collection_info ->> 'folderId' IS NULL)) " +
            "AND r.name LIKE '%' || (g.source_object_info ->> 'objectName') || '%'",
            nativeQuery = true)
    Optional<GeneratedApiEntity> findByRequestId(@Param("requestId") String requestId);

    @Query(value = "SELECT * FROM tb_eng_generated_apis WHERE source_object_info @> jsonb_build_object('requestId', :requestId) OR collection_info @> jsonb_build_object('requestId', :requestId)", nativeQuery = true)
    Optional<GeneratedApiEntity> findByGeneratedAPIId(@Param("requestId") String requestId);

    // ============= METHODS TO FETCH RELATED ENTITIES =============

    @Query(value = "SELECT * FROM tb_eng_parameters WHERE api_id = :apiId ORDER BY position ASC", nativeQuery = true)
    List<ApiParameterEntity> findParametersByApiId(@Param("apiId") String apiId);

    @Query("SELECT r FROM ApiResponseMappingEntity r WHERE r.generatedApi.id = :apiId ORDER BY r.position ASC")
    List<ApiResponseMappingEntity> findResponseMappingsByApiId(@Param("apiId") String apiId);

    @Query("SELECT h FROM ApiHeaderEntity h WHERE h.generatedApi.id = :apiId")
    List<ApiHeaderEntity> findHeadersByApiId(@Param("apiId") String apiId);

    @Query("SELECT t FROM ApiTestEntity t WHERE t.generatedApi.id = :apiId")
    List<ApiTestEntity> findTestsByApiId(@Param("apiId") String apiId);

    @Query("SELECT a FROM ApiAuthConfigEntity a WHERE a.generatedApi.id = :apiId")
    Optional<ApiAuthConfigEntity> findAuthConfigByApiId(@Param("apiId") String apiId);

    @Query("SELECT s FROM ApiSchemaConfigEntity s WHERE s.generatedApi.id = :apiId")
    Optional<ApiSchemaConfigEntity> findSchemaConfigByApiId(@Param("apiId") String apiId);

    @Query("SELECT r FROM ApiRequestConfigEntity r WHERE r.generatedApi.id = :apiId")
    Optional<ApiRequestConfigEntity> findRequestConfigByApiId(@Param("apiId") String apiId);

    @Query("SELECT r FROM ApiResponseConfigEntity r WHERE r.generatedApi.id = :apiId")
    Optional<ApiResponseConfigEntity> findResponseConfigByApiId(@Param("apiId") String apiId);

    @Query("SELECT s FROM ApiSettingsEntity s WHERE s.generatedApi.id = :apiId")
    Optional<ApiSettingsEntity> findSettingsByApiId(@Param("apiId") String apiId);

    @Query("SELECT g FROM GeneratedApiEntity g " +
            "LEFT JOIN FETCH g.schemaConfig " +
            "LEFT JOIN FETCH g.authConfig " +
            "LEFT JOIN FETCH g.requestConfig " +
            "LEFT JOIN FETCH g.responseConfig " +
            "LEFT JOIN FETCH g.settings " +
            "WHERE g.id = :apiId")
    Optional<GeneratedApiEntity> findByIdWithConfigs(@Param("apiId") String apiId);

    // ============= NEW METHODS FOR FINDING BY ID =============

    @Query("SELECT h FROM ApiHeaderEntity h WHERE h.id = :id")
    Optional<ApiHeaderEntity> findHeaderById(@Param("id") String id);

    @Query("SELECT p FROM ApiParameterEntity p WHERE p.id = :id")
    Optional<ApiParameterEntity> findParameterById(@Param("id") String id);

    @Query("SELECT rm FROM ApiResponseMappingEntity rm WHERE rm.id = :id")
    Optional<ApiResponseMappingEntity> findResponseMappingById(@Param("id") String id);

    // ============= NEW METHODS FOR FINDING BY BUSINESS KEY =============

    @Query("SELECT h FROM ApiHeaderEntity h WHERE h.generatedApi.id = :apiId AND h.key = :key")
    Optional<ApiHeaderEntity> findHeaderByApiIdAndKey(@Param("apiId") String apiId, @Param("key") String key);

    @Query("SELECT p FROM ApiParameterEntity p WHERE p.generatedApi.id = :apiId AND p.key = :key")
    Optional<ApiParameterEntity> findParameterByApiIdAndKey(@Param("apiId") String apiId, @Param("key") String key);

    @Query("SELECT rm FROM ApiResponseMappingEntity rm WHERE rm.generatedApi.id = :apiId AND rm.apiField = :apiField")
    Optional<ApiResponseMappingEntity> findResponseMappingByApiIdAndApiField(@Param("apiId") String apiId, @Param("apiField") String apiField);
}