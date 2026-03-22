// helpers/DatabaseMetadataHelper.java
package com.usg.apiAutomation.helpers;

import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiAutomation.enums.DatabaseTypeEnum;
import com.usg.apiAutomation.services.schemaBrowser.DatabaseSchemaService;

import java.util.List;
import java.util.Map;

/**
 * Interface for database metadata helpers
 * Each database type should implement this to provide detailed object metadata
 */
public interface DatabaseMetadataHelper {

    /**
     * Get source object details for API generation
     * @param schemaService The schema service to use
     * @param sourceObject Source object DTO
     * @return Map containing object details (columns, parameters, etc.)
     */
    Map<String, Object> getSourceObjectDetails(DatabaseSchemaService schemaService, ApiSourceObjectDTO sourceObject);

    /**
     * Get the database type this helper supports
     * @return DatabaseType enum
     */
    DatabaseTypeEnum getSupportedDatabaseType();

    /**
     * Parse and extract parameters from source code
     * @param sourceCode Object source code
     * @param objectType Object type
     * @return List of parameters
     */
    List<Map<String, Object>> parseParametersFromSource(String sourceCode, String objectType);

    /**
     * Transform database-specific data to common format
     * @param rawData Raw data from database
     * @return Transformed data in common format
     */
    Map<String, Object> transformToCommonFormat(Map<String, Object> rawData);
}