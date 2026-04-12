// services/schemaBrowser/DatabaseSchemaServiceFactory.java
package com.usg.autoAPIGenerator.factories;

import com.usg.autoAPIGenerator.enums.DatabaseTypeEnum;
import com.usg.autoAPIGenerator.interfaces.DatabaseSchemaService;
import com.usg.autoAPIGenerator.services.schemaBrowser.OracleSchemaService;
import com.usg.autoAPIGenerator.services.schemaBrowser.PostgreSQLSchemaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Component
public class DatabaseSchemaServiceFactory {

    private final Map<DatabaseTypeEnum, DatabaseSchemaService> services = new EnumMap<>(DatabaseTypeEnum.class);

    @Autowired
    public DatabaseSchemaServiceFactory(
            OracleSchemaService oracleSchemaService,
            PostgreSQLSchemaService postgreSQLSchemaService
            // Add more services as needed:
            // MySQLSchemaService mySQLSchemaService,
            // SQLServerSchemaService sqlServerSchemaService,
            // MongoDBService mongoDBService
    ) {
        services.put(DatabaseTypeEnum.ORACLE, oracleSchemaService);
        services.put(DatabaseTypeEnum.POSTGRESQL, postgreSQLSchemaService);
        // services.put(DatabaseType.MYSQL, mySQLSchemaService);
        // services.put(DatabaseType.SQL_SERVER, sqlServerSchemaService);
        // services.put(DatabaseType.MONGODB, mongoDBService);

        log.info("DatabaseSchemaServiceFactory initialized with {} services", services.size());
    }

    public DatabaseSchemaService getService(DatabaseTypeEnum type) {
        DatabaseSchemaService service = services.get(type);
        if (service == null) {
            log.error("No schema service found for database type: {}", type);
            throw new IllegalArgumentException("No schema service found for database type: " + type);
        }
        return service;
    }

    public DatabaseSchemaService getService(String type) {
        return getService(DatabaseTypeEnum.fromString(type));
    }

    public DatabaseSchemaService getServiceOrDefault(DatabaseTypeEnum type, DatabaseTypeEnum defaultType) {
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

    public boolean isSupported(DatabaseTypeEnum type) {
        return services.containsKey(type);
    }

    public boolean isSupported(String type) {
        return isSupported(DatabaseTypeEnum.fromString(type));
    }

    public Map<DatabaseTypeEnum, DatabaseSchemaService> getAllServices() {
        return new EnumMap<>(services);
    }
}