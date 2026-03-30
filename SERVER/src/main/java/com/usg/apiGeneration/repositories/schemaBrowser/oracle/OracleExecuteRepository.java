package com.usg.apiGeneration.repositories.schemaBrowser.oracle;

import com.usg.apiGeneration.enums.OracleSqlStatementTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
public class OracleExecuteRepository extends OracleRepository {

    // ============================================================
    // QUERY EXECUTION METHODS - ORIGINAL
    // ============================================================

    public Map<String, Object> executeQuery(String query, int timeoutSeconds, boolean readOnly) {
        try {
            log.info("Executing query, timeout: {}, readOnly: {}", timeoutSeconds, readOnly);

            long startTime = System.currentTimeMillis();

            if (timeoutSeconds > 0) {
                getJdbcTemplate().setQueryTimeout(timeoutSeconds);
            }

            List<Map<String, Object>> rows = getJdbcTemplate().queryForList(query);

            long executionTime = System.currentTimeMillis() - startTime;

            getJdbcTemplate().setQueryTimeout(-1);

            List<Map<String, String>> columns = new ArrayList<>();
            if (!rows.isEmpty()) {
                Map<String, Object> firstRow = rows.get(0);
                for (String key : firstRow.keySet()) {
                    Map<String, String> col = new HashMap<>();
                    col.put("name", key);
                    col.put("type", "VARCHAR2");
                    columns.add(col);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("rows", rows);
            result.put("columns", columns);
            result.put("rowCount", rows.size());
            result.put("executionTime", executionTime);
            result.put("message", "Query executed successfully, " + rows.size() + " rows returned");

            return result;

        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute query: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> executeQueryWithParams(String query, Map<String, Object> params, int timeoutSeconds) {
        try {
            log.info("Executing parameterized query, timeout: {}", timeoutSeconds);

            long startTime = System.currentTimeMillis();

            if (timeoutSeconds > 0) {
                getJdbcTemplate().setQueryTimeout(timeoutSeconds);
            }

            // Build parameter array from map
            Object[] paramArray = params.values().toArray();

            List<Map<String, Object>> rows = getJdbcTemplate().queryForList(query, paramArray);

            long executionTime = System.currentTimeMillis() - startTime;

            getJdbcTemplate().setQueryTimeout(-1);

            List<Map<String, String>> columns = new ArrayList<>();
            if (!rows.isEmpty()) {
                Map<String, Object> firstRow = rows.get(0);
                for (String key : firstRow.keySet()) {
                    Map<String, String> col = new HashMap<>();
                    col.put("name", key);
                    col.put("type", "VARCHAR2");
                    columns.add(col);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("rows", rows);
            result.put("columns", columns);
            result.put("rowCount", rows.size());
            result.put("executionTime", executionTime);
            result.put("params", params);
            result.put("message", "Query executed successfully, " + rows.size() + " rows returned");

            return result;

        } catch (Exception e) {
            log.error("Error executing parameterized query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute query: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> executeUpdate(String sql, Map<String, Object> params) {
        try {
            log.info("Executing update: {}", sql);

            long startTime = System.currentTimeMillis();

            Object[] paramArray = params != null ? params.values().toArray() : new Object[0];

            int rowsAffected = getJdbcTemplate().update(sql, paramArray);

            long executionTime = System.currentTimeMillis() - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("rowsAffected", rowsAffected);
            result.put("executionTime", executionTime);
            result.put("success", true);
            result.put("message", "Update executed successfully, " + rowsAffected + " rows affected");

            return result;

        } catch (Exception e) {
            log.error("Error executing update: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute update: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> executeBatch(List<String> sqlStatements) {
        try {
            log.info("Executing batch of {} statements", sqlStatements.size());

            long startTime = System.currentTimeMillis();

            int[] results = new int[sqlStatements.size()];
            int totalRowsAffected = 0;
            List<Map<String, Object>> errors = new ArrayList<>();

            for (int i = 0; i < sqlStatements.size(); i++) {
                String sql = sqlStatements.get(i);
                try {
                    int rowsAffected = getJdbcTemplate().update(sql);
                    results[i] = rowsAffected;
                    totalRowsAffected += rowsAffected;
                } catch (Exception e) {
                    results[i] = -1;
                    Map<String, Object> error = new HashMap<>();
                    error.put("statementIndex", i);
                    error.put("sql", sql);
                    error.put("error", e.getMessage());
                    errors.add(error);
                    log.error("Error executing batch statement {}: {}", i, e.getMessage());
                }
            }

            long executionTime = System.currentTimeMillis() - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("results", results);
            result.put("totalRowsAffected", totalRowsAffected);
            result.put("executionTime", executionTime);
            result.put("successfulCount", sqlStatements.size() - errors.size());
            result.put("failedCount", errors.size());
            result.put("errors", errors);
            result.put("message", "Batch executed: " + (sqlStatements.size() - errors.size()) + " successful, " + errors.size() + " failed");

            return result;

        } catch (Exception e) {
            log.error("Error executing batch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute batch: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> explainPlan(String sql) {
        try {
            log.info("Generating explain plan for query");

            // First, create the plan table if needed (simplified)
            String explainSql = "EXPLAIN PLAN SET STATEMENT_ID = 'CURRENT_QUERY' FOR " + sql;
            getJdbcTemplate().execute(explainSql);

            // Then query the plan
            String planSql = "SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY('PLAN_TABLE', 'CURRENT_QUERY', 'TYPICAL'))";
            List<Map<String, Object>> planRows = getJdbcTemplate().queryForList(planSql);

            // Clean up
            String cleanupSql = "DELETE FROM PLAN_TABLE WHERE STATEMENT_ID = 'CURRENT_QUERY'";
            getJdbcTemplate().update(cleanupSql);

            Map<String, Object> result = new HashMap<>();
            result.put("plan", planRows);
            result.put("rowCount", planRows.size());
            result.put("message", "Explain plan generated successfully");

            return result;

        } catch (Exception e) {
            log.error("Error generating explain plan: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate explain plan: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> validateQuery(String sql) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("Validating query: {}", sql);

            // Try to explain the plan to validate without executing
            String explainSql = "EXPLAIN PLAN SET STATEMENT_ID = 'VALIDATE_QUERY' FOR " + sql;
            getJdbcTemplate().execute(explainSql);

            // Clean up
            String cleanupSql = "DELETE FROM PLAN_TABLE WHERE STATEMENT_ID = 'VALIDATE_QUERY'";
            getJdbcTemplate().update(cleanupSql);

            result.put("valid", true);
            result.put("message", "Query syntax is valid");

        } catch (Exception e) {
            result.put("valid", false);
            result.put("message", "Query validation failed: " + e.getMessage());
            result.put("error", e.getMessage());
            log.warn("Query validation failed: {}", e.getMessage());
        }

        return result;
    }

    public Map<String, Object> getQueryMetadata(String sql) {
        try {
            log.info("Getting query metadata");

            // Use prepared statement to get metadata without executing
            // This is a simplified version - in production you'd use PreparedStatement.getMetaData()

            Map<String, Object> result = new HashMap<>();
            result.put("sql", sql);
            result.put("message", "Metadata retrieved successfully");

            // Try to get column information by executing with fetch size 0 (if supported)
            // For now, return basic info
            List<Map<String, Object>> columns = new ArrayList<>();

            // Attempt to get column info by analyzing the query
            String upperSql = sql.toUpperCase();
            if (upperSql.contains("SELECT")) {
                result.put("type", "SELECT");
                result.put("estimatedColumns", "Unknown - execute query to get column details");
            } else if (upperSql.contains("INSERT")) {
                result.put("type", "INSERT");
            } else if (upperSql.contains("UPDATE")) {
                result.put("type", "UPDATE");
            } else if (upperSql.contains("DELETE")) {
                result.put("type", "DELETE");
            } else {
                result.put("type", "UNKNOWN");
            }

            result.put("columns", columns);
            return result;

        } catch (Exception e) {
            log.error("Error getting query metadata: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get query metadata: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> executeStoredProcedure(String procedureName, Map<String, Object> params) {
        try {
            log.info("Executing stored procedure: {}", procedureName);

            long startTime = System.currentTimeMillis();

            // Build the call string
            StringBuilder callBuilder = new StringBuilder("{call ");
            callBuilder.append(procedureName).append("(");

            if (params != null && !params.isEmpty()) {
                StringJoiner joiner = new StringJoiner(",");
                for (int i = 0; i < params.size(); i++) {
                    joiner.add("?");
                }
                callBuilder.append(joiner.toString());
            }

            callBuilder.append(")}");

            String callSql = callBuilder.toString();
            Object[] paramArray = params != null ? params.values().toArray() : new Object[0];

            // Execute the stored procedure
            getJdbcTemplate().update(callSql, paramArray);

            long executionTime = System.currentTimeMillis() - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("procedureName", procedureName);
            result.put("params", params);
            result.put("executionTime", executionTime);
            result.put("success", true);
            result.put("message", "Stored procedure executed successfully");

            return result;

        } catch (Exception e) {
            log.error("Error executing stored procedure {}: {}", procedureName, e.getMessage(), e);
            throw new RuntimeException("Failed to execute stored procedure: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> executeFunction(String functionName, Map<String, Object> params, String returnType) {
        try {
            log.info("Executing function: {}", functionName);

            long startTime = System.currentTimeMillis();

            // Build the call string with return value
            StringBuilder callBuilder = new StringBuilder("{? = call ");
            callBuilder.append(functionName).append("(");

            if (params != null && !params.isEmpty()) {
                StringJoiner joiner = new StringJoiner(",");
                for (int i = 0; i < params.size(); i++) {
                    joiner.add("?");
                }
                callBuilder.append(joiner.toString());
            }

            callBuilder.append(")}");

            String callSql = callBuilder.toString();

            // Execute the function and get return value
            // This is a simplified version - in production you'd use CallableStatement
            Object[] paramArray = new Object[(params != null ? params.size() : 0) + 1];
            if (params != null) {
                int i = 1;
                for (Object value : params.values()) {
                    paramArray[i++] = value;
                }
            }

            // For now, we'll execute as a query and hope it returns something
            // In a real implementation, you'd use CallableStatement.registerOutParameter
            List<Map<String, Object>> resultList = getJdbcTemplate().queryForList(callSql, paramArray);

            long executionTime = System.currentTimeMillis() - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("functionName", functionName);
            result.put("params", params);
            result.put("returnType", returnType);
            result.put("result", resultList);
            result.put("executionTime", executionTime);
            result.put("success", true);
            result.put("message", "Function executed successfully");

            return result;

        } catch (Exception e) {
            log.error("Error executing function {}: {}", functionName, e.getMessage(), e);
            throw new RuntimeException("Failed to execute function: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getSessionInfo() {
        Map<String, Object> sessionInfo = new HashMap<>();
        try {
            String sql = "SELECT " +
                    "    USERNAME, " +
                    "    SID, " +
                    "    SERIAL#, " +
                    "    STATUS, " +
                    "    SCHEMANAME, " +
                    "    OSUSER, " +
                    "    MACHINE, " +
                    "    TERMINAL, " +
                    "    PROGRAM, " +
                    "    LOGON_TIME " +
                    "FROM V$SESSION " +
                    "WHERE AUDSID = USERENV('SESSIONID')";

            Map<String, Object> session = getJdbcTemplate().queryForMap(sql);
            sessionInfo.putAll(session);
            sessionInfo.put("success", true);

        } catch (Exception e) {
            log.error("Error getting session info: {}", e.getMessage(), e);
            sessionInfo.put("error", e.getMessage());
            sessionInfo.put("success", false);
        }
        return sessionInfo;
    }

    public Map<String, Object> getDatabaseInfo() {
        Map<String, Object> dbInfo = new HashMap<>();
        try {
            // Database name
            String nameSql = "SELECT NAME FROM V$DATABASE";
            String dbName = getJdbcTemplate().queryForObject(nameSql, String.class);
            dbInfo.put("name", dbName);

            // Database version
            dbInfo.put("version", getDatabaseVersion());

            // Instance name
            String instanceSql = "SELECT INSTANCE_NAME FROM V$INSTANCE";
            String instanceName = getJdbcTemplate().queryForObject(instanceSql, String.class);
            dbInfo.put("instance", instanceName);

            // Database open mode
            String openModeSql = "SELECT OPEN_MODE FROM V$DATABASE";
            String openMode = getJdbcTemplate().queryForObject(openModeSql, String.class);
            dbInfo.put("openMode", openMode);

            // Database role
            String roleSql = "SELECT DATABASE_ROLE FROM V$DATABASE";
            String role = getJdbcTemplate().queryForObject(roleSql, String.class);
            dbInfo.put("role", role);

            // Archive log mode
            String archiveSql = "SELECT LOG_MODE FROM V$DATABASE";
            String archiveMode = getJdbcTemplate().queryForObject(archiveSql, String.class);
            dbInfo.put("archiveMode", archiveMode);

            dbInfo.put("success", true);

        } catch (Exception e) {
            log.error("Error getting database info: {}", e.getMessage(), e);
            dbInfo.put("error", e.getMessage());
            dbInfo.put("success", false);
        }
        return dbInfo;
    }


    public Map<String, Object> executePlSqlBlock(String plSqlBlock, int timeoutSeconds, boolean readOnly) {
        try {
            log.info("Executing PL/SQL block, timeout: {}, readOnly: {}", timeoutSeconds, readOnly);

            long startTime = System.currentTimeMillis();

            if (timeoutSeconds > 0) {
                getJdbcTemplate().setQueryTimeout(timeoutSeconds);
            }

            // For PL/SQL blocks, we need to use execute() instead of queryForList()
            // Wrap in BEGIN-END if not already wrapped
            String executableBlock = plSqlBlock.trim();
            if (!executableBlock.toUpperCase().startsWith("BEGIN") &&
                    !executableBlock.toUpperCase().startsWith("DECLARE")) {
                executableBlock = "BEGIN\n" + executableBlock + "\nEND;";
            }

            // Execute the PL/SQL block
            getJdbcTemplate().execute(executableBlock);

            long executionTime = System.currentTimeMillis() - startTime;

            getJdbcTemplate().setQueryTimeout(-1);

            Map<String, Object> result = new HashMap<>();
            result.put("rows", new ArrayList<>()); // No rows returned for PL/SQL blocks
            result.put("columns", new ArrayList<>());
            result.put("rowCount", 0);
            result.put("executionTime", executionTime);
            result.put("message", "PL/SQL block executed successfully");
            result.put("isPlSql", true);

            return result;

        } catch (Exception e) {
            log.error("Error executing PL/SQL block: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute PL/SQL block: " + e.getMessage(), e);
        }
    }


    public Map<String, Object> executeProcedureWithOutParams(String procedureName,
                                                             Map<String, Object> inParams,
                                                             List<String> outParamNames) {
        try {
            log.info("Executing stored procedure with OUT parameters: {}", procedureName);

            long startTime = System.currentTimeMillis();

            // Build the call string with placeholders for OUT parameters
            StringBuilder callBuilder = new StringBuilder("{call ");
            callBuilder.append(procedureName).append("(");

            int totalParams = inParams.size() + outParamNames.size();
            for (int i = 0; i < totalParams; i++) {
                if (i > 0) callBuilder.append(",");
                callBuilder.append("?");
            }
            callBuilder.append(")}");

            String callSql = callBuilder.toString();

            // Use CallableStatement for OUT parameters
            Map<String, Object> result = getJdbcTemplate().execute((java.sql.Connection conn) -> {
                java.sql.CallableStatement cs = conn.prepareCall(callSql);

                // Set IN parameters
                int paramIndex = 1;
                for (Object value : inParams.values()) {
                    cs.setObject(paramIndex++, value);
                }

                // Register OUT parameters
                for (int i = 0; i < outParamNames.size(); i++) {
                    cs.registerOutParameter(paramIndex + i, java.sql.Types.VARCHAR);
                }

                cs.execute();

                // Collect OUT parameter values
                Map<String, Object> outParams = new HashMap<>();
                for (int i = 0; i < outParamNames.size(); i++) {
                    outParams.put(outParamNames.get(i), cs.getObject(paramIndex + i));
                }

                return outParams;
            });

            long executionTime = System.currentTimeMillis() - startTime;

            Map<String, Object> finalResult = new HashMap<>();
            finalResult.put("outParams", result);
            finalResult.put("executionTime", executionTime);
            finalResult.put("success", true);
            finalResult.put("message", "Stored procedure executed successfully");
            finalResult.put("rowCount", 0);

            return finalResult;

        } catch (Exception e) {
            log.error("Error executing stored procedure {}: {}", procedureName, e.getMessage(), e);
            throw new RuntimeException("Failed to execute stored procedure: " + e.getMessage(), e);
        }
    }


    /**
     * Execute DDL statements
     */
    public Map<String, Object> executeDdl(String ddl) {
        try {
            log.info("Executing DDL: {}", ddl);
            long startTime = System.currentTimeMillis();

            getJdbcTemplate().execute(ddl);

            long executionTime = System.currentTimeMillis() - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("executionTime", executionTime);
            result.put("success", true);
            result.put("message", "DDL executed successfully");
            result.put("rowCount", 0);

            return result;

        } catch (Exception e) {
            log.error("Error executing DDL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute DDL: " + e.getMessage(), e);
        }
    }

    /**
     * Execute stored program (procedure/function/package)
     */
    public Map<String, Object> executeStoredProgram(String program, OracleSqlStatementTypeEnum programType, int timeoutSeconds) {
        try {
            log.info("Executing stored program: {}, type: {}", program, programType);
            long startTime = System.currentTimeMillis();

            if (timeoutSeconds > 0) {
                getJdbcTemplate().setQueryTimeout(timeoutSeconds);
            }

            // Check if this is a DDL statement that should NOT be wrapped
            boolean isDDL = programType == OracleSqlStatementTypeEnum.CREATE_TABLE ||
                    programType == OracleSqlStatementTypeEnum.CREATE_VIEW ||
                    programType == OracleSqlStatementTypeEnum.CREATE_INDEX ||
                    programType == OracleSqlStatementTypeEnum.CREATE_TRIGGER ||
                    programType == OracleSqlStatementTypeEnum.CREATE_SEQUENCE ||
                    programType == OracleSqlStatementTypeEnum.CREATE_SYNONYM ||
                    programType == OracleSqlStatementTypeEnum.CREATE_TYPE ||
                    programType == OracleSqlStatementTypeEnum.ALTER ||
                    programType == OracleSqlStatementTypeEnum.DROP ||
                    programType == OracleSqlStatementTypeEnum.TRUNCATE ||
                    programType == OracleSqlStatementTypeEnum.GRANT ||
                    programType == OracleSqlStatementTypeEnum.REVOKE ||
                    programType == OracleSqlStatementTypeEnum.COMMENT ||
                    programType == OracleSqlStatementTypeEnum.RENAME ||
                    programType == OracleSqlStatementTypeEnum.DDL;

            // For CREATE PROCEDURE/FUNCTION/PACKAGE or DDL, execute directly
            if (programType == OracleSqlStatementTypeEnum.PROCEDURE ||
                    programType == OracleSqlStatementTypeEnum.FUNCTION ||
                    programType == OracleSqlStatementTypeEnum.PACKAGE ||
                    isDDL) {
                getJdbcTemplate().execute(program);
            } else {
                // For existing procedures/calls, wrap in anonymous block
                String executableBlock = "BEGIN\n" + program + "\nEND;";
                getJdbcTemplate().execute(executableBlock);
            }

            long executionTime = System.currentTimeMillis() - startTime;
            getJdbcTemplate().setQueryTimeout(-1);

            Map<String, Object> result = new HashMap<>();
            result.put("executionTime", executionTime);
            result.put("success", true);
            result.put("message", programType + " executed successfully");
            result.put("rowCount", 0);

            return result;

        } catch (Exception e) {
            log.error("Error executing stored program: {}", e.getMessage(), e);

            // Extract Oracle error message
            Throwable rootCause = e;
            while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                rootCause = rootCause.getCause();
            }
            String errorMessage = rootCause.getMessage();

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("executionTime", 0);

            if (errorMessage != null && errorMessage.contains("ORA-01031")) {
                errorResult.put("message", "Insufficient privileges: The database user does not have permission to execute this operation.");
                errorResult.put("error", "ORA-01031: insufficient privileges");
            } else if (errorMessage != null && errorMessage.contains("ORA-00900")) {
                errorResult.put("message", "Invalid SQL statement.");
                errorResult.put("error", errorMessage.substring(0, Math.min(errorMessage.indexOf('\n'), errorMessage.length())));
            } else if (errorMessage != null && errorMessage.contains("ORA-00942")) {
                errorResult.put("message", "Table or view does not exist.");
                errorResult.put("error", errorMessage.substring(0, Math.min(errorMessage.indexOf('\n'), errorMessage.length())));
            } else if (errorMessage != null && errorMessage.contains("ORA-00955")) {
                errorResult.put("message", "Object already exists.");
                errorResult.put("error", errorMessage.substring(0, Math.min(errorMessage.indexOf('\n'), errorMessage.length())));
            } else {
                errorResult.put("message", "Execution failed: " + e.getMessage());
                errorResult.put("error", e.getMessage());
            }

            return errorResult;
        }
    }

    /**
     * Execute direct CALL or EXEC statement
     */
    public Map<String, Object> executeDirectCall(String callStatement, int timeoutSeconds) {
        try {
            log.info("Executing direct call: {}", callStatement);
            long startTime = System.currentTimeMillis();

            if (timeoutSeconds > 0) {
                getJdbcTemplate().setQueryTimeout(timeoutSeconds);
            }

            // Convert EXEC to BEGIN-END block if needed
            String executableCall = callStatement;
            if (callStatement.toUpperCase().startsWith("EXEC") ||
                    callStatement.toUpperCase().startsWith("EXECUTE")) {
                String procedureCall = callStatement.substring(4).trim();
                executableCall = "BEGIN\n" + procedureCall + ";\nEND;";
            }

            getJdbcTemplate().execute(executableCall);

            long executionTime = System.currentTimeMillis() - startTime;
            getJdbcTemplate().setQueryTimeout(-1);

            Map<String, Object> result = new HashMap<>();
            result.put("executionTime", executionTime);
            result.put("success", true);
            result.put("message", "Call executed successfully");
            result.put("rowCount", 0);

            return result;

        } catch (Exception e) {
            log.error("Error executing direct call: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute call: " + e.getMessage(), e);
        }
    }
}