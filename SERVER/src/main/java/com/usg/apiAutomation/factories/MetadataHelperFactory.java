// helpers/apiEngine/MetadataHelperFactory.java
package com.usg.apiAutomation.factories;

import com.usg.apiAutomation.enums.DatabaseType;
import com.usg.apiAutomation.helpers.DatabaseMetadataHelper;
import com.usg.apiAutomation.helpers.apiEngine.oracle.OracleApiMetadataHelper;
import com.usg.apiAutomation.helpers.apiEngine.postgresql.PostgreSQLApiMetadataHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Component
public class MetadataHelperFactory {

    private final Map<DatabaseType, DatabaseMetadataHelper> helpers = new EnumMap<>(DatabaseType.class);

    @Autowired
    public MetadataHelperFactory(
            OracleApiMetadataHelper oracleMetadataHelper,
            PostgreSQLApiMetadataHelper postgreSQLMetadataHelper
            // Add more as needed
    ) {
        helpers.put(DatabaseType.ORACLE, oracleMetadataHelper);
        helpers.put(DatabaseType.POSTGRESQL, postgreSQLMetadataHelper);
        // helpers.put(DatabaseType.MYSQL, mySQLMetadataHelper);
        // helpers.put(DatabaseType.SQL_SERVER, sqlServerMetadataHelper);

        log.info("MetadataHelperFactory initialized with {} helpers", helpers.size());
    }

    public DatabaseMetadataHelper getHelper(DatabaseType type) {
        DatabaseMetadataHelper helper = helpers.get(type);
        if (helper == null) {
            log.error("No metadata helper found for database type: {}", type);
            throw new IllegalArgumentException("No metadata helper found for database type: " + type);
        }
        return helper;
    }

    public DatabaseMetadataHelper getHelper(String type) {
        return getHelper(DatabaseType.fromString(type));
    }

    public DatabaseMetadataHelper getHelperOrDefault(DatabaseType type, DatabaseType defaultType) {
        DatabaseMetadataHelper helper = helpers.get(type);
        if (helper == null) {
            log.warn("No metadata helper found for database type: {}, falling back to: {}", type, defaultType);
            helper = helpers.get(defaultType);
        }
        if (helper == null) {
            throw new IllegalArgumentException("No metadata helper found for database type: " + type);
        }
        return helper;
    }

    public boolean isSupported(DatabaseType type) {
        return helpers.containsKey(type);
    }
}