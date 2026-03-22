// services/schemaBrowser/DatabaseSchemaService.java
package com.usg.apiAutomation.services.schemaBrowser;

import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiAutomation.enums.DatabaseType;
import java.util.Map;

/**
 * Interface for database schema services
 * Each database type (Oracle, PostgreSQL, MySQL, etc.) should implement this
 */
public interface DatabaseSchemaService {

    /**
     * Check if an object exists in the database
     * @param owner Schema/owner name
     * @param objectName Object name
     * @param objectType Object type (TABLE, VIEW, PROCEDURE, etc.)
     * @return true if object exists
     */
    boolean objectExists(String owner, String objectName, String objectType);

    /**
     * Get object details for API generation
     * @param sourceObject Source object DTO
     * @return Map containing object details (columns, parameters, etc.)
     */
    Map<String, Object> getSourceObjectDetails(ApiSourceObjectDTO sourceObject);

    /**
     * Get the database type
     * @return DatabaseType enum
     */
    DatabaseType getDatabaseType();

    /**
     * Get database version
     * @return Database version string
     */
    String getDatabaseVersion();

    /**
     * Check if the database connection is active
     * @return true if connected
     */
    boolean isConnected();

    /**
     * Get object DDL
     * @param objectName Object name
     * @param objectType Object type
     * @param owner Owner/schema
     * @return DDL string
     */
    String getObjectDDL(String objectName, String objectType, String owner);

    /**
     * Get object statistics
     * @param objectName Object name
     * @param objectType Object type
     * @param owner Owner/schema
     * @return Statistics map
     */
    Map<String, Object> getObjectStatistics(String objectName, String objectType, String owner);
}