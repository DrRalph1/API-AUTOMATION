// factories/ParameterGeneratorFactory.java
package com.usg.apiGeneration.factories;

import com.usg.apiGeneration.enums.DatabaseTypeEnum;
import com.usg.apiGeneration.utils.apiEngine.DatabaseParameterGeneratorUtil;
import com.usg.apiGeneration.utils.apiEngine.OracleParameterGeneratorUtil;
import com.usg.apiGeneration.utils.apiEngine.PostgreSQLParameterGeneratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Component
public class ParameterGeneratorFactory {

    private final Map<DatabaseTypeEnum, DatabaseParameterGeneratorUtil> generators = new EnumMap<>(DatabaseTypeEnum.class);

    public ParameterGeneratorFactory(
            OracleParameterGeneratorUtil oracleParameterGenerator,
            PostgreSQLParameterGeneratorUtil postgreSQLParameterGenerator) {
        generators.put(DatabaseTypeEnum.ORACLE, oracleParameterGenerator);
        generators.put(DatabaseTypeEnum.POSTGRESQL, postgreSQLParameterGenerator);
        log.info("ParameterGeneratorFactory initialized with {} generators", generators.size());
    }

    public DatabaseParameterGeneratorUtil getGenerator(DatabaseTypeEnum type) {
        DatabaseParameterGeneratorUtil generator = generators.get(type);
        if (generator == null) {
            log.error("No parameter generator found for database type: {}", type);
            throw new IllegalArgumentException("Unsupported database type for parameter generation: " + type);
        }
        return generator;
    }

    public DatabaseParameterGeneratorUtil getGenerator(String typeStr) {
        return getGenerator(DatabaseTypeEnum.fromString(typeStr));
    }
}