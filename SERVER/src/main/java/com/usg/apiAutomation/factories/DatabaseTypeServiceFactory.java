// factories/DatabaseTypeServiceFactory.java
package com.usg.apiAutomation.factories;

import com.usg.apiAutomation.enums.DatabaseTypeEnum;
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
    private final Map<DatabaseTypeEnum, DatabaseValidationHelper> validationHelpers = new EnumMap<>(DatabaseTypeEnum.class);

    // Metadata Helpers
    private final Map<DatabaseTypeEnum, DatabaseMetadataHelper> metadataHelpers = new EnumMap<>(DatabaseTypeEnum.class);

    // Analytics Helpers
    private final Map<DatabaseTypeEnum, ApiAnalyticsHelper> analyticsHelpers = new EnumMap<>(DatabaseTypeEnum.class);

    // Schema Services
    private final Map<DatabaseTypeEnum, Object> schemaServices = new EnumMap<>(DatabaseTypeEnum.class);

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
        validationHelpers.put(DatabaseTypeEnum.ORACLE, oracleValidationHelper);
        metadataHelpers.put(DatabaseTypeEnum.ORACLE, oracleMetadataHelper);
        analyticsHelpers.put(DatabaseTypeEnum.ORACLE, oracleMetadataHelper);
        schemaServices.put(DatabaseTypeEnum.ORACLE, oracleSchemaService);

        // Register PostgreSQL services
        validationHelpers.put(DatabaseTypeEnum.POSTGRESQL, postgresValidationHelper);
        metadataHelpers.put(DatabaseTypeEnum.POSTGRESQL, postgresMetadataHelper);
        analyticsHelpers.put(DatabaseTypeEnum.POSTGRESQL, postgresMetadataHelper);
        schemaServices.put(DatabaseTypeEnum.POSTGRESQL, postgreSQLSchemaService);

        log.info("DatabaseTypeServiceFactory initialized with {} database types", validationHelpers.size());
    }

    public DatabaseValidationHelper getValidationHelper(DatabaseTypeEnum type) {
        DatabaseValidationHelper helper = validationHelpers.get(type);
        if (helper == null) {
            log.error("No validation helper found for database type: {}", type);
            throw new IllegalArgumentException("Unsupported database type: " + type);
        }
        return helper;
    }

    public DatabaseValidationHelper getValidationHelper(String typeStr) {
        return getValidationHelper(DatabaseTypeEnum.fromString(typeStr));
    }

    public DatabaseMetadataHelper getMetadataHelper(DatabaseTypeEnum type) {
        DatabaseMetadataHelper helper = metadataHelpers.get(type);
        if (helper == null) {
            log.error("No metadata helper found for database type: {}", type);
            throw new IllegalArgumentException("Unsupported database type: " + type);
        }
        return helper;
    }

    public DatabaseMetadataHelper getMetadataHelper(String typeStr) {
        return getMetadataHelper(DatabaseTypeEnum.fromString(typeStr));
    }

    public ApiAnalyticsHelper getAnalyticsHelper(DatabaseTypeEnum type) {
        ApiAnalyticsHelper helper = analyticsHelpers.get(type);
        if (helper == null) {
            log.error("No analytics helper found for database type: {}", type);
            throw new IllegalArgumentException("Unsupported database type: " + type);
        }
        return helper;
    }

    public ApiAnalyticsHelper getAnalyticsHelper(String typeStr) {
        return getAnalyticsHelper(DatabaseTypeEnum.fromString(typeStr));
    }

    public Object getSchemaService(DatabaseTypeEnum type) {
        Object service = schemaServices.get(type);
        if (service == null) {
            log.error("No schema service found for database type: {}", type);
            throw new IllegalArgumentException("Unsupported database type: " + type);
        }
        return service;
    }

    public Object getSchemaService(String typeStr) {
        return getSchemaService(DatabaseTypeEnum.fromString(typeStr));
    }

    public DatabaseTypeEnum getDatabaseType(String typeStr) {
        return DatabaseTypeEnum.fromString(typeStr);
    }
}