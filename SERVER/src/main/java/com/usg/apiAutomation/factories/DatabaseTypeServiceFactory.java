// factories/DatabaseTypeServiceFactory.java
package com.usg.apiAutomation.factories;

import com.usg.apiAutomation.enums.DatabaseType;
import com.usg.apiAutomation.helpers.ApiAnalyticsHelper;
import com.usg.apiAutomation.helpers.DatabaseMetadataHelper;
import com.usg.apiAutomation.helpers.DatabaseValidationHelper;
import com.usg.apiAutomation.helpers.apiEngine.oracle.OracleApiMetadataHelper;
import com.usg.apiAutomation.helpers.apiEngine.oracle.OracleApiValidationHelper;
import com.usg.apiAutomation.helpers.apiEngine.postgresql.PostgreSQLApiMetadataHelper;
import com.usg.apiAutomation.helpers.apiEngine.postgresql.PostgreSQLApiValidationHelper;
import com.usg.apiAutomation.services.schemaBrowser.OracleSchemaService;
import com.usg.apiAutomation.services.schemaBrowser.PostgreSQLSchemaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Component
public class DatabaseTypeServiceFactory {

    // Validation Helpers
    private final Map<DatabaseType, DatabaseValidationHelper> validationHelpers = new EnumMap<>(DatabaseType.class);

    // Metadata Helpers
    private final Map<DatabaseType, DatabaseMetadataHelper> metadataHelpers = new EnumMap<>(DatabaseType.class);

    // Analytics Helpers
    private final Map<DatabaseType, ApiAnalyticsHelper> analyticsHelpers = new EnumMap<>(DatabaseType.class);

    // Schema Services
    private final Map<DatabaseType, Object> schemaServices = new EnumMap<>(DatabaseType.class);

    public DatabaseTypeServiceFactory(
            // Oracle
            OracleApiValidationHelper oracleValidationHelper,
            OracleApiMetadataHelper oracleMetadataHelper,
            OracleSchemaService oracleSchemaService,
            // PostgreSQL
            PostgreSQLApiValidationHelper postgresValidationHelper,
            PostgreSQLApiMetadataHelper postgresMetadataHelper,
            PostgreSQLSchemaService postgreSQLSchemaService
    ) {
        // Register Oracle services
        validationHelpers.put(DatabaseType.ORACLE, oracleValidationHelper);
        metadataHelpers.put(DatabaseType.ORACLE, oracleMetadataHelper);
        analyticsHelpers.put(DatabaseType.ORACLE, oracleMetadataHelper);
        schemaServices.put(DatabaseType.ORACLE, oracleSchemaService);

        // Register PostgreSQL services
        validationHelpers.put(DatabaseType.POSTGRESQL, postgresValidationHelper);
        metadataHelpers.put(DatabaseType.POSTGRESQL, postgresMetadataHelper);
        analyticsHelpers.put(DatabaseType.POSTGRESQL, postgresMetadataHelper);
        schemaServices.put(DatabaseType.POSTGRESQL, postgreSQLSchemaService);

        log.info("DatabaseTypeServiceFactory initialized with {} database types", validationHelpers.size());
    }

    public DatabaseValidationHelper getValidationHelper(DatabaseType type) {
        DatabaseValidationHelper helper = validationHelpers.get(type);
        if (helper == null) {
            log.error("No validation helper found for database type: {}", type);
            throw new IllegalArgumentException("Unsupported database type: " + type);
        }
        return helper;
    }

    public DatabaseValidationHelper getValidationHelper(String typeStr) {
        return getValidationHelper(DatabaseType.fromString(typeStr));
    }

    public DatabaseMetadataHelper getMetadataHelper(DatabaseType type) {
        DatabaseMetadataHelper helper = metadataHelpers.get(type);
        if (helper == null) {
            log.error("No metadata helper found for database type: {}", type);
            throw new IllegalArgumentException("Unsupported database type: " + type);
        }
        return helper;
    }

    public DatabaseMetadataHelper getMetadataHelper(String typeStr) {
        return getMetadataHelper(DatabaseType.fromString(typeStr));
    }

    public ApiAnalyticsHelper getAnalyticsHelper(DatabaseType type) {
        ApiAnalyticsHelper helper = analyticsHelpers.get(type);
        if (helper == null) {
            log.error("No analytics helper found for database type: {}", type);
            throw new IllegalArgumentException("Unsupported database type: " + type);
        }
        return helper;
    }

    public ApiAnalyticsHelper getAnalyticsHelper(String typeStr) {
        return getAnalyticsHelper(DatabaseType.fromString(typeStr));
    }

    public Object getSchemaService(DatabaseType type) {
        Object service = schemaServices.get(type);
        if (service == null) {
            log.error("No schema service found for database type: {}", type);
            throw new IllegalArgumentException("Unsupported database type: " + type);
        }
        return service;
    }

    public Object getSchemaService(String typeStr) {
        return getSchemaService(DatabaseType.fromString(typeStr));
    }

    public DatabaseType getDatabaseType(String typeStr) {
        return DatabaseType.fromString(typeStr);
    }
}