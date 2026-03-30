// helpers/apiEngine/MetadataHelperFactory.java
package com.usg.apiGeneration.factories;

import com.usg.apiGeneration.enums.DatabaseTypeEnum;
import com.usg.apiGeneration.helpers.DatabaseMetadataHelper;
import com.usg.apiGeneration.helpers.apiEngine.oracle.OracleApiMetadataHelper;
import com.usg.apiGeneration.helpers.apiEngine.postgresql.PostgreSQLApiMetadataHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Component
public class MetadataHelperFactory {

    private final Map<DatabaseTypeEnum, DatabaseMetadataHelper> helpers = new EnumMap<>(DatabaseTypeEnum.class);

    @Autowired
    public MetadataHelperFactory(
            OracleApiMetadataHelper oracleMetadataHelper,
            PostgreSQLApiMetadataHelper postgreSQLMetadataHelper
            // Add more as needed
    ) {
        helpers.put(DatabaseTypeEnum.ORACLE, oracleMetadataHelper);
        helpers.put(DatabaseTypeEnum.POSTGRESQL, postgreSQLMetadataHelper);
        // helpers.put(DatabaseType.MYSQL, mySQLMetadataHelper);
        // helpers.put(DatabaseType.SQL_SERVER, sqlServerMetadataHelper);

        log.info("MetadataHelperFactory initialized with {} helpers", helpers.size());
    }

    public DatabaseMetadataHelper getHelper(DatabaseTypeEnum type) {
        DatabaseMetadataHelper helper = helpers.get(type);
        if (helper == null) {
            log.error("No metadata helper found for database type: {}", type);
            throw new IllegalArgumentException("No metadata helper found for database type: " + type);
        }
        return helper;
    }

    public DatabaseMetadataHelper getHelper(String type) {
        return getHelper(DatabaseTypeEnum.fromString(type));
    }

    public DatabaseMetadataHelper getHelperOrDefault(DatabaseTypeEnum type, DatabaseTypeEnum defaultType) {
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

    public boolean isSupported(DatabaseTypeEnum type) {
        return helpers.containsKey(type);
    }
}