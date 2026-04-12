package com.usg.autoAPIGenerator.factories;

import com.usg.autoAPIGenerator.helpers.DatabaseMetadataHelper;
import com.usg.autoAPIGenerator.helpers.apiEngine.oracle.OracleApiMetadataHelper;
import com.usg.autoAPIGenerator.helpers.apiEngine.postgresql.PostgreSQLApiMetadataHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiMetadataHelperFactory {

    private final OracleApiMetadataHelper oracleMetadataHelper;
    private final PostgreSQLApiMetadataHelper postgresqlMetadataHelper;

    /**
     * Get the appropriate metadata helper based on database type
     *
     * @param databaseType The database type (oracle, postgresql, etc.)
     * @return The DatabaseMetadataHelper implementation for the specified database type
     */
    public DatabaseMetadataHelper getMetadataHelper(String databaseType) {
        if (databaseType == null || databaseType.isEmpty()) {
            log.warn("Database type is null or empty, defaulting to Oracle metadata helper");
            return oracleMetadataHelper;
        }

        switch (databaseType.toLowerCase()) {
            case "postgresql":
            case "postgres":
                log.debug("Using PostgreSQL metadata helper");
                return postgresqlMetadataHelper;
            case "oracle":
            default:
                log.debug("Using Oracle metadata helper");
                return oracleMetadataHelper;
        }
    }

    /**
     * Get Oracle metadata helper specifically
     */
    public OracleApiMetadataHelper getOracleMetadataHelper() {
        return oracleMetadataHelper;
    }

    /**
     * Get PostgreSQL metadata helper specifically
     */
    public PostgreSQLApiMetadataHelper getPostgreSQLMetadataHelper() {
        return postgresqlMetadataHelper;
    }
}