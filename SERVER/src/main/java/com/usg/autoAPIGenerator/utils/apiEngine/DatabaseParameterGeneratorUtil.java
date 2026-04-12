// utils/apiEngine/DatabaseParameterGeneratorUtil.java
package com.usg.autoAPIGenerator.utils.apiEngine;

import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiParameterDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.ApiHeaderEntity;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.ApiParameterEntity;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.ApiResponseMappingEntity;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.autoAPIGenerator.enums.DatabaseTypeEnum;

import java.util.List;

/**
 * Interface for database-specific parameter generation
 */
public interface DatabaseParameterGeneratorUtil {

    /**
     * Generate parameters from source object
     */
    List<ApiParameterEntity> generateParametersFromSource(ApiSourceObjectDTO sourceObject, GeneratedApiEntity api);

    /**
     * Generate response mappings from source object
     */
    List<ApiResponseMappingEntity> generateResponseMappingsFromSource(ApiSourceObjectDTO sourceObject, GeneratedApiEntity api);

    /**
     * Get the database type this generator supports
     */
    DatabaseTypeEnum getSupportedDatabaseType();


    /**
     * Generate API parameter entities from DTOs and source object
     */
    List<ApiParameterEntity> generateParameters(
            ApiSourceObjectDTO sourceObjectDTO,
            List<ApiParameterDTO> parameterDTOs,
            String generatedApiId);

    /**
     * Generate response mapping entities from source object
     */
    List<ApiResponseMappingEntity> generateResponseMappings(
            ApiSourceObjectDTO sourceObjectDTO,
            String generatedApiId);

    /**
     * Generate header entities from DTOs
     */
    List<ApiHeaderEntity> generateHeaders(
            List<com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiHeaderDTO> headerDTOs,
            String generatedApiId);
}