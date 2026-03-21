package com.usg.apiAutomation.repositories.apiGenerationEngine;

import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.ApiParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiParameterRepository extends JpaRepository<ApiParameterEntity, String> {

    // Find parameters by the GeneratedApiEntity ID
    List<ApiParameterEntity> findByGeneratedApiId(String generatedApiId);

    // Find parameters by GeneratedApiEntity ID ordered by position
    List<ApiParameterEntity> findByGeneratedApiIdOrderByPositionAsc(String generatedApiId);

    // Find parameters by the request ID stored in the parent's JSONB fields
    @Query(value = "SELECT p.* FROM tb_eng_parameters p " +
            "INNER JOIN tb_eng_generated_apis g ON p.api_id = g.id " +
            "WHERE g.source_object_info @> jsonb_build_object('requestId', :requestId) " +
            "OR g.collection_info @> jsonb_build_object('requestId', :requestId)",
            nativeQuery = true)
    List<ApiParameterEntity> findByRequestId(@Param("requestId") String requestId);

    // Find parameters by parameter location (query, path, header, body)
    List<ApiParameterEntity> findByParameterLocation(String parameterLocation);

    // Find parameters by parameter mode (IN, OUT, IN OUT)
    List<ApiParameterEntity> findByParamMode(String paramMode);

    // Find required parameters
    List<ApiParameterEntity> findByRequiredTrue();

    // Find primary key parameters
    List<ApiParameterEntity> findByIsPrimaryKeyTrue();

    // Find parameters that are in the body
    List<ApiParameterEntity> findByInBodyTrue();

    // Find parameters by Oracle type
    List<ApiParameterEntity> findByOracleType(String oracleType);

    // Find parameters by API type
    List<ApiParameterEntity> findByApiType(String apiType);

    // Find parameters by key name
    List<ApiParameterEntity> findByKey(String key);

    // Find parameters by dbColumn name
    List<ApiParameterEntity> findByDbColumn(String dbColumn);

    // Custom query to find parameters with their parent API info (avoids N+1)
    @Query("SELECT p FROM ApiParameterEntity p JOIN FETCH p.generatedApi WHERE p.generatedApi.id = :apiId")
    List<ApiParameterEntity> findWithApiByGeneratedApiId(@Param("apiId") String apiId);

    // Custom query to find parameters by request ID with parent API info
    @Query(value = "SELECT p.* FROM tb_eng_parameters p " +
            "INNER JOIN tb_eng_generated_apis g ON p.api_id = g.id " +
            "WHERE g.source_object_info @> jsonb_build_object('requestId', :requestId) " +
            "OR g.collection_info @> jsonb_build_object('requestId', :requestId)",
            nativeQuery = true)
    List<ApiParameterEntity> findWithDetailsByRequestId(@Param("requestId") String requestId);

    // Count parameters by API ID
    Long countByGeneratedApiId(String generatedApiId);

    // Delete all parameters for a specific API
    void deleteByGeneratedApiId(String generatedApiId);

    // Find parameters by multiple criteria
    List<ApiParameterEntity> findByGeneratedApiIdAndParameterLocation(String generatedApiId, String parameterLocation);

    List<ApiParameterEntity> findByGeneratedApiIdAndParamMode(String generatedApiId, String paramMode);

    // Check if any parameters exist for an API
    boolean existsByGeneratedApiId(String generatedApiId);
}