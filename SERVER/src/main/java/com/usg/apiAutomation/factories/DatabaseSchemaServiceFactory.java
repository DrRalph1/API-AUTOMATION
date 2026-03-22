// services/schemaBrowser/DatabaseSchemaServiceFactory.java
package com.usg.apiAutomation.factories;

import com.usg.apiAutomation.enums.DatabaseType;
import com.usg.apiAutomation.services.schemaBrowser.DatabaseSchemaService;
import com.usg.apiAutomation.services.schemaBrowser.OracleSchemaService;
import com.usg.apiAutomation.services.schemaBrowser.PostgreSQLSchemaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Component
public class DatabaseSchemaServiceFactory {

    private final Map<DatabaseType, DatabaseSchemaService> services = new EnumMap<>(DatabaseType.class);

    @Autowired
    public DatabaseSchemaServiceFactory(
            OracleSchemaService oracleSchemaService,
            PostgreSQLSchemaService postgreSQLSchemaService
            // Add more services as needed:
            // MySQLSchemaService mySQLSchemaService,
            // SQLServerSchemaService sqlServerSchemaService,
            // MongoDBService mongoDBService
    ) {
        services.put(DatabaseType.ORACLE, oracleSchemaService);
        services.put(DatabaseType.POSTGRESQL, postgreSQLSchemaService);
        // services.put(DatabaseType.MYSQL, mySQLSchemaService);
        // services.put(DatabaseType.SQL_SERVER, sqlServerSchemaService);
        // services.put(DatabaseType.MONGODB, mongoDBService);

        log.info("DatabaseSchemaServiceFactory initialized with {} services", services.size());
    }

    public DatabaseSchemaService getService(DatabaseType type) {
        DatabaseSchemaService service = services.get(type);
        if (service == null) {
            log.error("No schema service found for database type: {}", type);
            throw new IllegalArgumentException("No schema service found for database type: " + type);
        }
        return service;
    }

    public DatabaseSchemaService getService(String type) {
        return getService(DatabaseType.fromString(type));
    }

    public DatabaseSchemaService getServiceOrDefault(DatabaseType type, DatabaseType defaultType) {
        DatabaseSchemaService service = services.get(type);
        if (service == null) {
            log.warn("No schema service found for database type: {}, falling back to: {}", type, defaultType);
            service = services.get(defaultType);
        }
        if (service == null) {
            throw new IllegalArgumentException("No schema service found for database type: " + type);
        }
        return service;
    }

    public boolean isSupported(DatabaseType type) {
        return services.containsKey(type);
    }

    public boolean isSupported(String type) {
        return isSupported(DatabaseType.fromString(type));
    }

    public Map<DatabaseType, DatabaseSchemaService> getAllServices() {
        return new EnumMap<>(services);
    }
}