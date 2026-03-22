// factories/ParameterGeneratorFactory.java
package com.usg.apiAutomation.factories;

import com.usg.apiAutomation.enums.DatabaseType;
import com.usg.apiAutomation.utils.apiEngine.DatabaseParameterGeneratorUtil;
import com.usg.apiAutomation.utils.apiEngine.OracleParameterGeneratorUtil;
import com.usg.apiAutomation.utils.apiEngine.PostgreSQLParameterGeneratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Component
public class ParameterGeneratorFactory {

    private final Map<DatabaseType, DatabaseParameterGeneratorUtil> generators = new EnumMap<>(DatabaseType.class);

    public ParameterGeneratorFactory(
            OracleParameterGeneratorUtil oracleParameterGenerator,
            PostgreSQLParameterGeneratorUtil postgreSQLParameterGenerator) {
        generators.put(DatabaseType.ORACLE, oracleParameterGenerator);
        generators.put(DatabaseType.POSTGRESQL, postgreSQLParameterGenerator);
        log.info("ParameterGeneratorFactory initialized with {} generators", generators.size());
    }

    public DatabaseParameterGeneratorUtil getGenerator(DatabaseType type) {
        DatabaseParameterGeneratorUtil generator = generators.get(type);
        if (generator == null) {
            log.error("No parameter generator found for database type: {}", type);
            throw new IllegalArgumentException("Unsupported database type for parameter generation: " + type);
        }
        return generator;
    }

    public DatabaseParameterGeneratorUtil getGenerator(String typeStr) {
        return getGenerator(DatabaseType.fromString(typeStr));
    }
}