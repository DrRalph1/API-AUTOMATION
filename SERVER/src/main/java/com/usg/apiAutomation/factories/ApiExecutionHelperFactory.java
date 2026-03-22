package com.usg.apiAutomation.factories;

import com.usg.apiAutomation.helpers.BaseApiExecutionHelper;
import com.usg.apiAutomation.helpers.apiEngine.oracle.OracleApiExecutionHelper;
import com.usg.apiAutomation.helpers.apiEngine.postgresql.PostgreSQLApiExecutionHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiExecutionHelperFactory {

    private final OracleApiExecutionHelper oracleExecutionHelper;
    private final PostgreSQLApiExecutionHelper postgresqlExecutionHelper;

    public BaseApiExecutionHelper getExecutionHelper(String databaseType) {
        if (databaseType == null || databaseType.isEmpty()) {
            log.warn("Database type is null or empty, defaulting to Oracle");
            return oracleExecutionHelper;
        }

        switch (databaseType.toLowerCase()) {
            case "postgresql":
            case "postgres":
                log.info("Using PostgreSQL execution helper");
                return postgresqlExecutionHelper;
            case "oracle":
            default:
                log.info("Using Oracle execution helper");
                return oracleExecutionHelper;
        }
    }
}