package com.usg.apiGeneration.repositories.schemaBrowser.postgresql;

import com.usg.apiGeneration.enums.PostgreSQLSqlStatementTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;
import java.util.Date;

@Slf4j
@Repository
public class PostgreSQLExecuteRepository extends PostgreSQLRepository {

    // ============================================================
    // QUERY EXECUTION METHODS - POSTGRESQL VERSION
    // ============================================================

    public Map<String, Object> executeQuery(String query, int timeoutSeconds, boolean readOnly) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            log.info("Executing query, timeout: {}, readOnly: {}", timeoutSeconds, readOnly);

            long startTime = System.currentTimeMillis();

            conn = getJdbcTemplate().getDataSource().getConnection();
            if (readOnly) {
                conn.setReadOnly(true);
            }

            stmt = conn.createStatement();
            if (timeoutSeconds > 0) {
                stmt.setQueryTimeout(timeoutSeconds);
            }

            boolean isResultSet = stmt.execute(query);
            List<Map<String, Object>> rows = new ArrayList<>();
            List<Map<String, String>> columns = new ArrayList<>();

            if (isResultSet) {
                rs = stmt.getResultSet();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                // Extract column information
                for (int i = 1; i <= columnCount; i++) {
                    Map<String, String> col = new HashMap<>();
                    col.put("name", metaData.getColumnName(i));
                    col.put("type", metaData.getColumnTypeName(i));
                    col.put("className", metaData.getColumnClassName(i));
                    columns.add(col);
                }

                // Extract data
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);

                        // Handle PostgreSQL-specific types
                        if (value != null) {
                            String typeName = metaData.getColumnTypeName(i);
                            if ("json".equalsIgnoreCase(typeName) || "jsonb".equalsIgnoreCase(typeName)) {
                                value = rs.getString(i);
                            } else if ("geometry".equalsIgnoreCase(typeName) || "geography".equalsIgnoreCase(typeName)) {
                                value = rs.getString(i);
                            }
                        }

                        row.put(columnName, value);
                    }
                    rows.add(row);
                }
            }

            long executionTime = System.currentTimeMillis() - startTime;

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
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    public Map<String, Object> executeQueryWithParams(String query, Map<String, Object> params, int timeoutSeconds) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            log.info("Executing parameterized query, timeout: {}", timeoutSeconds);

            long startTime = System.currentTimeMillis();

            conn = getJdbcTemplate().getDataSource().getConnection();
            pstmt = conn.prepareStatement(query);

            if (timeoutSeconds > 0) {
                pstmt.setQueryTimeout(timeoutSeconds);
            }

            // Set parameters
            int paramIndex = 1;
            for (Object value : params.values()) {
                setParameter(pstmt, paramIndex++, value);
            }

            boolean isResultSet = pstmt.execute();
            List<Map<String, Object>> rows = new ArrayList<>();
            List<Map<String, String>> columns = new ArrayList<>();

            if (isResultSet) {
                rs = pstmt.getResultSet();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                // Extract column information
                for (int i = 1; i <= columnCount; i++) {
                    Map<String, String> col = new HashMap<>();
                    col.put("name", metaData.getColumnName(i));
                    col.put("type", metaData.getColumnTypeName(i));
                    col.put("className", metaData.getColumnClassName(i));
                    columns.add(col);
                }

                // Extract data
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);

                        // Handle PostgreSQL-specific types
                        if (value != null) {
                            String typeName = metaData.getColumnTypeName(i);
                            if ("json".equalsIgnoreCase(typeName) || "jsonb".equalsIgnoreCase(typeName)) {
                                value = rs.getString(i);
                            } else if ("geometry".equalsIgnoreCase(typeName) || "geography".equalsIgnoreCase(typeName)) {
                                value = rs.getString(i);
                            }
                        }

                        row.put(columnName, value);
                    }
                    rows.add(row);
                }
            }

            long executionTime = System.currentTimeMillis() - startTime;

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
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    public Map<String, Object> executeUpdate(String sql, Map<String, Object> params) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            log.info("Executing update: {}", sql);

            long startTime = System.currentTimeMillis();

            conn = getJdbcTemplate().getDataSource().getConnection();
            pstmt = conn.prepareStatement(sql);

            if (params != null && !params.isEmpty()) {
                int paramIndex = 1;
                for (Object value : params.values()) {
                    setParameter(pstmt, paramIndex++, value);
                }
            }

            int rowsAffected = pstmt.executeUpdate();

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
        } finally {
            closeResources(null, pstmt, conn);
        }
    }

    public Map<String, Object> executeBatch(List<String> sqlStatements) {
        Connection conn = null;
        Statement stmt = null;

        try {
            log.info("Executing batch of {} statements", sqlStatements.size());

            long startTime = System.currentTimeMillis();

            conn = getJdbcTemplate().getDataSource().getConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();

            int totalRowsAffected = 0;
            List<Map<String, Object>> errors = new ArrayList<>();
            int[] results = new int[sqlStatements.size()];

            for (int i = 0; i < sqlStatements.size(); i++) {
                String sql = sqlStatements.get(i);
                try {
                    stmt.addBatch(sql);
                } catch (Exception e) {
                    results[i] = -1;
                    Map<String, Object> error = new HashMap<>();
                    error.put("statementIndex", i);
                    error.put("sql", sql);
                    error.put("error", e.getMessage());
                    errors.add(error);
                    log.error("Error adding batch statement {}: {}", i, e.getMessage());
                }
            }

            // Execute batch
            int[] batchResults = stmt.executeBatch();
            for (int i = 0; i < batchResults.length; i++) {
                int rowsAffected = batchResults[i];
                results[i] = rowsAffected;
                if (rowsAffected > 0) {
                    totalRowsAffected += rowsAffected;
                }
            }

            conn.commit();

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
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                log.error("Error rolling back batch: {}", ex.getMessage());
            }
            throw new RuntimeException("Failed to execute batch: " + e.getMessage(), e);
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    public Map<String, Object> explainPlan(String sql) {
        try {
            log.info("Generating explain plan for query");

            long startTime = System.currentTimeMillis();

            // PostgreSQL uses EXPLAIN ANALYZE or just EXPLAIN
            String explainSql = "EXPLAIN (FORMAT JSON, ANALYZE false) " + sql;
            List<Map<String, Object>> planRows = getJdbcTemplate().queryForList(explainSql);

            long executionTime = System.currentTimeMillis() - startTime;

            // Format the plan for better readability
            List<String> formattedPlan = new ArrayList<>();
            for (Map<String, Object> row : planRows) {
                Object planObj = row.get("QUERY PLAN");
                if (planObj != null) {
                    if (planObj instanceof String) {
                        formattedPlan.add((String) planObj);
                    } else {
                        formattedPlan.add(planObj.toString());
                    }
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("plan", planRows);
            result.put("formattedPlan", formattedPlan);
            result.put("rowCount", planRows.size());
            result.put("executionTime", executionTime);
            result.put("message", "Explain plan generated successfully");

            return result;

        } catch (Exception e) {
            log.error("Error generating explain plan: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate explain plan: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> validateQuery(String sql) {
        Map<String, Object> result = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            log.info("Validating query: {}", sql);

            conn = getJdbcTemplate().getDataSource().getConnection();
            pstmt = conn.prepareStatement(sql);

            // Try to get metadata without executing
            // For SELECT statements, we can get the ResultSetMetaData
            if (sql.toUpperCase().trim().startsWith("SELECT")) {
                ResultSetMetaData metaData = pstmt.getMetaData();
                if (metaData != null) {
                    // Query is valid
                    result.put("valid", true);
                    result.put("message", "Query syntax is valid");
                    result.put("columnCount", metaData.getColumnCount());

                    List<Map<String, Object>> columns = new ArrayList<>();
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        Map<String, Object> column = new HashMap<>();
                        column.put("name", metaData.getColumnName(i));
                        column.put("type", metaData.getColumnTypeName(i));
                        column.put("className", metaData.getColumnClassName(i));
                        columns.add(column);
                    }
                    result.put("columns", columns);
                } else {
                    result.put("valid", true);
                    result.put("message", "Query syntax appears valid (no metadata available)");
                }
            } else {
                // For non-SELECT statements, just check if it parses
                pstmt.executeQuery(); // This will throw if invalid
                result.put("valid", true);
                result.put("message", "Query syntax is valid");
            }

        } catch (SQLException e) {
            result.put("valid", false);
            result.put("message", "Query validation failed: " + e.getMessage());
            result.put("error", e.getMessage());
            result.put("sqlState", e.getSQLState());
            result.put("errorCode", e.getErrorCode());
            log.warn("Query validation failed: {}", e.getMessage());
        } catch (Exception e) {
            result.put("valid", false);
            result.put("message", "Query validation failed: " + e.getMessage());
            result.put("error", e.getMessage());
            log.warn("Query validation failed: {}", e.getMessage());
        } finally {
            closeResources(null, pstmt, conn);
        }

        return result;
    }

    public Map<String, Object> getQueryMetadata(String sql) {
        Map<String, Object> result = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            log.info("Getting query metadata");

            result.put("sql", sql);
            result.put("originalSql", sql);

            conn = getJdbcTemplate().getDataSource().getConnection();

            // Determine query type
            String upperSql = sql.trim().toUpperCase();
            if (upperSql.startsWith("SELECT")) {
                result.put("type", "SELECT");
                pstmt = conn.prepareStatement(sql);
                ResultSetMetaData metaData = pstmt.getMetaData();

                if (metaData != null) {
                    List<Map<String, Object>> columns = new ArrayList<>();
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        Map<String, Object> column = new HashMap<>();
                        column.put("name", metaData.getColumnName(i));
                        column.put("label", metaData.getColumnLabel(i));
                        column.put("type", metaData.getColumnTypeName(i));
                        column.put("typeCode", metaData.getColumnType(i));
                        column.put("className", metaData.getColumnClassName(i));
                        column.put("precision", metaData.getPrecision(i));
                        column.put("scale", metaData.getScale(i));
                        column.put("isNullable", metaData.isNullable(i) == ResultSetMetaData.columnNullable);
                        columns.add(column);
                    }
                    result.put("columns", columns);
                    result.put("columnCount", columns.size());
                }
            } else if (upperSql.startsWith("INSERT")) {
                result.put("type", "INSERT");
            } else if (upperSql.startsWith("UPDATE")) {
                result.put("type", "UPDATE");
            } else if (upperSql.startsWith("DELETE")) {
                result.put("type", "DELETE");
            } else if (upperSql.startsWith("WITH")) {
                result.put("type", "CTE");
            } else {
                result.put("type", "UNKNOWN");
            }

            // Extract table names (simplified)
            List<String> tables = extractTableNames(sql);
            result.put("tables", tables);
            result.put("tableCount", tables.size());

            result.put("message", "Metadata retrieved successfully");

        } catch (Exception e) {
            log.error("Error getting query metadata: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
            result.put("message", "Failed to get query metadata: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return result;
    }

    public Map<String, Object> executeStoredProcedure(String procedureName, Map<String, Object> params) {
        Connection conn = null;
        CallableStatement cstmt = null;

        try {
            log.info("Executing stored procedure: {}", procedureName);

            long startTime = System.currentTimeMillis();

            conn = getJdbcTemplate().getDataSource().getConnection();

            // Build the call string for PostgreSQL
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
            cstmt = conn.prepareCall(callSql);

            // Set parameters
            if (params != null && !params.isEmpty()) {
                int paramIndex = 1;
                for (Object value : params.values()) {
                    setParameter(cstmt, paramIndex++, value);
                }
            }

            // Execute the stored procedure
            boolean isResultSet = cstmt.execute();

            // Handle any result sets
            List<Map<String, Object>> resultSets = new ArrayList<>();
            int updateCount = -1;
            int resultSetNumber = 0;

            // Process all result sets
            while (isResultSet) {
                ResultSet rs = cstmt.getResultSet();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnName(i), rs.getObject(i));
                    }
                    rows.add(row);
                }

                // Create a map for this result set
                Map<String, Object> resultSetWrapper = new HashMap<>();
                resultSetWrapper.put("resultSet", rows);
                resultSetWrapper.put("rowCount", rows.size());
                resultSetWrapper.put("columns", getColumnMetadata(metaData));
                resultSetWrapper.put("resultSetNumber", ++resultSetNumber);
                resultSets.add(resultSetWrapper);

                rs.close();

                // Check for more result sets
                isResultSet = cstmt.getMoreResults();
            }

            // Get update count if there's no result set
            if (resultSets.isEmpty()) {
                updateCount = cstmt.getUpdateCount();
            }

            long executionTime = System.currentTimeMillis() - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("procedureName", procedureName);
            result.put("params", params);
            result.put("executionTime", executionTime);
            result.put("success", true);
            result.put("resultSets", resultSets);
            result.put("resultSetCount", resultSets.size());
            result.put("updateCount", updateCount);
            result.put("message", "Stored procedure executed successfully");

            return result;

        } catch (Exception e) {
            log.error("Error executing stored procedure {}: {}", procedureName, e.getMessage(), e);
            throw new RuntimeException("Failed to execute stored procedure: " + e.getMessage(), e);
        } finally {
            closeResources(null, cstmt, conn);
        }
    }



    private List<Map<String, Object>> getColumnMetadata(ResultSetMetaData metaData) throws SQLException {
        List<Map<String, Object>> columns = new ArrayList<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            Map<String, Object> column = new HashMap<>();
            column.put("name", metaData.getColumnName(i));
            column.put("label", metaData.getColumnLabel(i));
            column.put("type", metaData.getColumnTypeName(i));
            column.put("typeCode", metaData.getColumnType(i));
            column.put("className", metaData.getColumnClassName(i));
            columns.add(column);
        }
        return columns;
    }



    public Map<String, Object> executeFunction(String functionName, Map<String, Object> params, String returnType) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            log.info("Executing function: {}", functionName);

            long startTime = System.currentTimeMillis();

            conn = getJdbcTemplate().getDataSource().getConnection();

            // PostgreSQL function call syntax: SELECT function_name(params)
            StringBuilder callBuilder = new StringBuilder("SELECT ");
            callBuilder.append(functionName).append("(");

            if (params != null && !params.isEmpty()) {
                StringJoiner joiner = new StringJoiner(",");
                for (int i = 0; i < params.size(); i++) {
                    joiner.add("?");
                }
                callBuilder.append(joiner.toString());
            }

            callBuilder.append(") as result");

            String callSql = callBuilder.toString();
            pstmt = conn.prepareStatement(callSql);

            // Set parameters
            if (params != null && !params.isEmpty()) {
                int paramIndex = 1;
                for (Object value : params.values()) {
                    setParameter(pstmt, paramIndex++, value);
                }
            }

            // Execute the function
            rs = pstmt.executeQuery();

            Object returnValue = null;
            if (rs.next()) {
                returnValue = rs.getObject(1);
            }

            long executionTime = System.currentTimeMillis() - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("functionName", functionName);
            result.put("params", params);
            result.put("returnType", returnType);
            result.put("result", returnValue);
            result.put("executionTime", executionTime);
            result.put("success", true);
            result.put("message", "Function executed successfully");

            return result;

        } catch (Exception e) {
            log.error("Error executing function {}: {}", functionName, e.getMessage(), e);
            throw new RuntimeException("Failed to execute function: " + e.getMessage(), e);
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    public Map<String, Object> getSessionInfo() {
        Map<String, Object> sessionInfo = new HashMap<>();
        try {
            String sql = "SELECT " +
                    "    pid as sid, " +
                    "    usename as username, " +
                    "    application_name as program, " +
                    "    client_addr as machine, " +
                    "    client_hostname as terminal, " +
                    "    backend_start as logon_time, " +
                    "    state as status, " +
                    "    query as current_query " +
                    "FROM pg_stat_activity " +
                    "WHERE pid = pg_backend_pid()";

            Map<String, Object> session = getJdbcTemplate().queryForMap(sql);
            sessionInfo.putAll(session);
            sessionInfo.put("success", true);
            sessionInfo.put("sessionId", session.get("sid"));

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
            String nameSql = "SELECT current_database() as name";
            String dbName = getJdbcTemplate().queryForObject(nameSql, String.class);
            dbInfo.put("name", dbName);

            // Database version
            dbInfo.put("version", getDatabaseVersion());

            // PostgreSQL version details
            String versionNumSql = "SHOW server_version";
            String versionNum = getJdbcTemplate().queryForObject(versionNumSql, String.class);
            dbInfo.put("serverVersion", versionNum);

            // Data directory
            try {
                String dataDirSql = "SHOW data_directory";
                String dataDir = getJdbcTemplate().queryForObject(dataDirSql, String.class);
                dbInfo.put("dataDirectory", dataDir);
            } catch (Exception e) {
                dbInfo.put("dataDirectory", "N/A");
            }

            // Connection info
            String portSql = "SHOW port";
            String port = getJdbcTemplate().queryForObject(portSql, String.class);
            dbInfo.put("port", port);

            // Max connections
            String maxConnSql = "SHOW max_connections";
            String maxConnections = getJdbcTemplate().queryForObject(maxConnSql, String.class);
            dbInfo.put("maxConnections", maxConnections);

            // Current connections
            String currentConnSql = "SELECT COUNT(*) as current_connections FROM pg_stat_activity";
            Integer currentConnections = getJdbcTemplate().queryForObject(currentConnSql, Integer.class);
            dbInfo.put("currentConnections", currentConnections);

            // Database size
            String sizeSql = "SELECT pg_database_size(current_database()) as size_bytes";
            Long sizeBytes = getJdbcTemplate().queryForObject(sizeSql, Long.class);
            dbInfo.put("sizeBytes", sizeBytes);
            dbInfo.put("sizeFormatted", formatBytes(sizeBytes));

            // Is primary or replica
            String roleSql = "SELECT pg_is_in_recovery() as is_replica";
            Boolean isReplica = getJdbcTemplate().queryForObject(roleSql, Boolean.class);
            dbInfo.put("isReplica", isReplica);
            dbInfo.put("role", isReplica ? "REPLICA" : "PRIMARY");

            // Transaction isolation level
            String isolationSql = "SHOW transaction_isolation";
            String isolationLevel = getJdbcTemplate().queryForObject(isolationSql, String.class);
            dbInfo.put("transactionIsolation", isolationLevel);

            dbInfo.put("success", true);

        } catch (Exception e) {
            log.error("Error getting database info: {}", e.getMessage(), e);
            dbInfo.put("error", e.getMessage());
            dbInfo.put("success", false);
        }
        return dbInfo;
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private void setParameter(PreparedStatement pstmt, int index, Object value) throws SQLException {
        if (value == null) {
            pstmt.setNull(index, Types.NULL);
        } else if (value instanceof String) {
            pstmt.setString(index, (String) value);
        } else if (value instanceof Integer) {
            pstmt.setInt(index, (Integer) value);
        } else if (value instanceof Long) {
            pstmt.setLong(index, (Long) value);
        } else if (value instanceof Double) {
            pstmt.setDouble(index, (Double) value);
        } else if (value instanceof Float) {
            pstmt.setFloat(index, (Float) value);
        } else if (value instanceof Boolean) {
            pstmt.setBoolean(index, (Boolean) value);
        } else if (value instanceof Date) {
            pstmt.setDate(index, new java.sql.Date(((Date) value).getTime()));
        } else if (value instanceof Timestamp) {
            pstmt.setTimestamp(index, (Timestamp) value);
        } else if (value instanceof byte[]) {
            pstmt.setBytes(index, (byte[]) value);
        } else {
            pstmt.setObject(index, value);
        }
    }

    private void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            log.debug("Error closing ResultSet: {}", e.getMessage());
        }
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            log.debug("Error closing Statement: {}", e.getMessage());
        }
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            log.debug("Error closing Connection: {}", e.getMessage());
        }
    }

    private List<String> extractTableNames(String sql) {
        List<String> tables = new ArrayList<>();
        // Simplified table name extraction - in production you'd use a proper SQL parser
        String upperSql = sql.toUpperCase();
        String[] keywords = {"FROM", "JOIN", "UPDATE", "INTO"};

        for (String keyword : keywords) {
            int index = upperSql.indexOf(keyword);
            while (index >= 0) {
                int start = index + keyword.length();
                int end = findNextToken(sql, start);
                if (end > start) {
                    String table = sql.substring(start, end).trim();
                    // Remove aliases and schema prefixes
                    table = table.split("\\s+")[0];
                    if (table.contains(".")) {
                        table = table.substring(table.lastIndexOf(".") + 1);
                    }
                    if (!table.isEmpty() && !tables.contains(table)) {
                        tables.add(table);
                    }
                }
                index = upperSql.indexOf(keyword, index + 1);
            }
        }

        return tables;
    }

    private int findNextToken(String sql, int start) {
        boolean inQuote = false;
        for (int i = start; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '\'') {
                inQuote = !inQuote;
            } else if (!inQuote && (c == ' ' || c == '\t' || c == '\n' || c == ',' || c == ';' || c == '(')) {
                return i;
            }
        }
        return sql.length();
    }



    /**
     * Execute DDL statements with proper error handling
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

        } catch (BadSqlGrammarException e) {
            // Extract root cause and format user-friendly error
            Throwable rootCause = e;
            while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                rootCause = rootCause.getCause();
            }

            String errorMessage = rootCause.getMessage();
            String cleanErrorMessage = extractPostgreSQLError(errorMessage);

            // Check for insufficient privileges
            if (errorMessage != null && (errorMessage.contains("permission denied") ||
                    errorMessage.contains("ERROR: permission denied"))) {
                String userFriendlyError = "Insufficient privileges: You don't have permission to execute this DDL statement.\n\n" +
                        "Please contact your database administrator to grant the required privileges.\n" +
                        "Required privileges may include: CREATE, ALTER, DROP on the schema or database.";
                throw new RuntimeException(userFriendlyError, e);
            }
            // Check for syntax error
            else if (errorMessage != null && (errorMessage.contains("syntax error") ||
                    errorMessage.contains("ERROR: syntax error"))) {
                String userFriendlyError = "Syntax error: " + cleanErrorMessage + "\n\n" +
                        "Please check your SQL syntax. Common issues:\n" +
                        "- Missing or extra commas\n" +
                        "- Invalid data type\n" +
                        "- Missing required keywords\n" +
                        "- Schema name doesn't exist or you don't have permission";
                throw new RuntimeException(userFriendlyError, e);
            }
            // Check for relation already exists
            else if (errorMessage != null && (errorMessage.contains("already exists") ||
                    errorMessage.contains("ERROR: relation") && errorMessage.contains("already exists"))) {
                String userFriendlyError = "Object already exists: " + cleanErrorMessage;
                throw new RuntimeException(userFriendlyError, e);
            }
            // Check for relation does not exist
            else if (errorMessage != null && (errorMessage.contains("does not exist") ||
                    errorMessage.contains("ERROR: relation") && errorMessage.contains("does not exist"))) {
                String userFriendlyError = "Table or view does not exist: " + cleanErrorMessage;
                throw new RuntimeException(userFriendlyError, e);
            }
            // Check for duplicate key
            else if (errorMessage != null && errorMessage.contains("duplicate key")) {
                String userFriendlyError = "Duplicate key violates unique constraint: " + cleanErrorMessage;
                throw new RuntimeException(userFriendlyError, e);
            }
            // Check for foreign key violation
            else if (errorMessage != null && errorMessage.contains("foreign key")) {
                String userFriendlyError = "Foreign key violation: " + cleanErrorMessage;
                throw new RuntimeException(userFriendlyError, e);
            }
            // Check for not null violation
            else if (errorMessage != null && errorMessage.contains("null value") && errorMessage.contains("violates not-null")) {
                String userFriendlyError = "Not null violation: " + cleanErrorMessage;
                throw new RuntimeException(userFriendlyError, e);
            }
            // Check for check constraint violation
            else if (errorMessage != null && errorMessage.contains("violates check constraint")) {
                String userFriendlyError = "Check constraint violation: " + cleanErrorMessage;
                throw new RuntimeException(userFriendlyError, e);
            }
            // Generic PostgreSQL error
            else if (errorMessage != null && (errorMessage.contains("ERROR:") || errorMessage.contains("PG::"))) {
                String userFriendlyError = "PostgreSQL error: " + cleanErrorMessage;
                throw new RuntimeException(userFriendlyError, e);
            }
            // Unknown error
            else {
                throw new RuntimeException("Failed to execute DDL: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Error executing DDL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute DDL: " + e.getMessage(), e);
        }
    }

    /**
     * Execute stored program (procedure/function) with proper error handling
     */
    public Map<String, Object> executeStoredProgram(String program, PostgreSQLSqlStatementTypeEnum programType, int timeoutSeconds) {
        try {
            log.info("Executing stored program: {}, type: {}", program, programType);
            long startTime = System.currentTimeMillis();

            Connection conn = getJdbcTemplate().getDataSource().getConnection();

            if (timeoutSeconds > 0) {
                conn.setNetworkTimeout(null, timeoutSeconds * 1000);
            }

            // For CREATE PROCEDURE/FUNCTION, execute as DDL
            if (programType == PostgreSQLSqlStatementTypeEnum.PROCEDURE ||
                    programType == PostgreSQLSqlStatementTypeEnum.FUNCTION) {
                getJdbcTemplate().execute(program);
            } else {
                // For existing procedures/functions, call them
                String executableCall = program;

                // If it's a procedure call without CALL keyword
                if (!program.toUpperCase().startsWith("CALL") &&
                        programType == PostgreSQLSqlStatementTypeEnum.PROCEDURE) {
                    executableCall = "CALL " + program;
                }

                // If it's a function call without SELECT
                if (!program.toUpperCase().startsWith("SELECT") &&
                        programType == PostgreSQLSqlStatementTypeEnum.FUNCTION) {
                    executableCall = "SELECT " + program;
                }

                getJdbcTemplate().execute(executableCall);
            }

            long executionTime = System.currentTimeMillis() - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("executionTime", executionTime);
            result.put("success", true);
            result.put("message", programType + " executed successfully");
            result.put("rowCount", 0);

            return result;

        } catch (BadSqlGrammarException e) {
            // Extract root cause and format user-friendly error
            Throwable rootCause = e;
            while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                rootCause = rootCause.getCause();
            }

            String errorMessage = rootCause.getMessage();
            String cleanErrorMessage = extractPostgreSQLError(errorMessage);

            // Check for insufficient privileges
            if (errorMessage != null && (errorMessage.contains("permission denied") ||
                    errorMessage.contains("ERROR: permission denied"))) {
                String userFriendlyError = "Insufficient privileges (Permission denied): You don't have permission to execute this operation.\n\n" +
                        "Please contact your database administrator to grant the required privileges.\n" +
                        "Required privileges may include: EXECUTE on the function/procedure, CREATE on the schema.";
                throw new RuntimeException(userFriendlyError, e);
            }
            // Check for function/procedure does not exist
            else if (errorMessage != null && (errorMessage.contains("does not exist") ||
                    errorMessage.contains("function") && errorMessage.contains("does not exist"))) {
                String userFriendlyError = "Function or procedure does not exist: " + cleanErrorMessage;
                throw new RuntimeException(userFriendlyError, e);
            }
            // Check for argument mismatch
            else if (errorMessage != null && (errorMessage.contains("function") && errorMessage.contains("does not match"))) {
                String userFriendlyError = "Function/procedure argument mismatch: " + cleanErrorMessage + "\n\n" +
                        "Please check the number and types of parameters.";
                throw new RuntimeException(userFriendlyError, e);
            }
            // Generic PostgreSQL error
            else if (errorMessage != null && (errorMessage.contains("ERROR:") || errorMessage.contains("PG::"))) {
                String userFriendlyError = "PostgreSQL error: " + cleanErrorMessage;
                throw new RuntimeException(userFriendlyError, e);
            }
            // Unknown error
            else {
                throw new RuntimeException("Failed to execute stored program: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Error executing stored program: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute stored program: " + e.getMessage(), e);
        }
    }

    /**
     * Execute PL/pgSQL block with proper error handling
     */
    public Map<String, Object> executePlPgSqlBlock(String plSqlBlock, int timeoutSeconds, boolean readOnly) {
        try {
            log.info("Executing PL/pgSQL block, timeout: {}, readOnly: {}", timeoutSeconds, readOnly);

            long startTime = System.currentTimeMillis();

            Connection conn = getJdbcTemplate().getDataSource().getConnection();
            if (readOnly) {
                conn.setReadOnly(true);
            }
            if (timeoutSeconds > 0) {
                conn.setNetworkTimeout(null, timeoutSeconds * 1000);
            }

            // Format the PL/pgSQL block for PostgreSQL
            String executableBlock = plSqlBlock.trim();

            // If it's a DO block, use as-is
            if (!executableBlock.toUpperCase().startsWith("DO") &&
                    !executableBlock.toUpperCase().startsWith("BEGIN")) {
                executableBlock = "DO $$\n" + executableBlock + "\n$$ LANGUAGE plpgsql;";
            }

            // Execute the PL/pgSQL block
            getJdbcTemplate().execute(executableBlock);

            long executionTime = System.currentTimeMillis() - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("rows", new ArrayList<>());
            result.put("columns", new ArrayList<>());
            result.put("rowCount", 0);
            result.put("executionTime", executionTime);
            result.put("message", "PL/pgSQL block executed successfully");
            result.put("isPlSql", true);

            return result;

        } catch (BadSqlGrammarException e) {
            // Extract root cause and format user-friendly error
            Throwable rootCause = e;
            while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                rootCause = rootCause.getCause();
            }

            String errorMessage = rootCause.getMessage();
            String cleanErrorMessage = extractPostgreSQLError(errorMessage);

            // Check for syntax error in PL/pgSQL
            if (errorMessage != null && (errorMessage.contains("syntax error") ||
                    errorMessage.contains("ERROR: syntax error"))) {
                String userFriendlyError = "Syntax error in PL/pgSQL block: " + cleanErrorMessage + "\n\n" +
                        "Please check your PL/pgSQL syntax. Common issues:\n" +
                        "- Missing semicolons at end of statements\n" +
                        "- Invalid variable declarations\n" +
                        "- Missing BEGIN/END blocks\n" +
                        "- Incorrect use of language features";
                throw new RuntimeException(userFriendlyError, e);
            }
            // Check for undefined variable
            else if (errorMessage != null && errorMessage.contains("column") && errorMessage.contains("does not exist")) {
                String userFriendlyError = "Undefined variable or column: " + cleanErrorMessage;
                throw new RuntimeException(userFriendlyError, e);
            }
            // Generic PostgreSQL error
            else if (errorMessage != null && (errorMessage.contains("ERROR:") || errorMessage.contains("PG::"))) {
                String userFriendlyError = "PostgreSQL error: " + cleanErrorMessage;
                throw new RuntimeException(userFriendlyError, e);
            }
            // Unknown error
            else {
                throw new RuntimeException("Failed to execute PL/pgSQL block: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Error executing PL/pgSQL block: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute PL/pgSQL block: " + e.getMessage(), e);
        }
    }

    /**
     * Execute direct CALL or EXEC statement with proper error handling
     */
    public Map<String, Object> executeDirectCall(String callStatement, int timeoutSeconds) {
        try {
            log.info("Executing direct call: {}", callStatement);
            long startTime = System.currentTimeMillis();

            Connection conn = getJdbcTemplate().getDataSource().getConnection();
            if (timeoutSeconds > 0) {
                conn.setNetworkTimeout(null, timeoutSeconds * 1000);
            }

            // Format the call properly for PostgreSQL
            String executableCall = callStatement;
            if (callStatement.toUpperCase().startsWith("EXEC") ||
                    callStatement.toUpperCase().startsWith("EXECUTE")) {
                String procedureCall = callStatement.substring(4).trim();
                executableCall = "CALL " + procedureCall;
            }

            getJdbcTemplate().execute(executableCall);

            long executionTime = System.currentTimeMillis() - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("executionTime", executionTime);
            result.put("success", true);
            result.put("message", "Call executed successfully");
            result.put("rowCount", 0);

            return result;

        } catch (BadSqlGrammarException e) {
            // Extract root cause and format user-friendly error
            Throwable rootCause = e;
            while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                rootCause = rootCause.getCause();
            }

            String errorMessage = rootCause.getMessage();
            String cleanErrorMessage = extractPostgreSQLError(errorMessage);

            // Check for procedure does not exist
            if (errorMessage != null && (errorMessage.contains("does not exist") ||
                    errorMessage.contains("procedure") && errorMessage.contains("does not exist"))) {
                String userFriendlyError = "Procedure does not exist: " + cleanErrorMessage;
                throw new RuntimeException(userFriendlyError, e);
            }
            // Check for argument mismatch
            else if (errorMessage != null && (errorMessage.contains("procedure") && errorMessage.contains("does not match"))) {
                String userFriendlyError = "Procedure argument mismatch: " + cleanErrorMessage;
                throw new RuntimeException(userFriendlyError, e);
            }
            // Generic PostgreSQL error
            else if (errorMessage != null && (errorMessage.contains("ERROR:") || errorMessage.contains("PG::"))) {
                String userFriendlyError = "PostgreSQL error: " + cleanErrorMessage;
                throw new RuntimeException(userFriendlyError, e);
            }
            // Unknown error
            else {
                throw new RuntimeException("Failed to execute call: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Error executing direct call: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute call: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to extract clean PostgreSQL error message
     */
    private String extractPostgreSQLError(String errorMessage) {
        if (errorMessage == null) {
            return "";
        }

        // Extract the first line of the error
        int newlineIndex = errorMessage.indexOf('\n');
        String firstLine = newlineIndex > 0 ? errorMessage.substring(0, newlineIndex) : errorMessage;

        // Remove the "ERROR:" prefix if present
        if (firstLine.startsWith("ERROR:")) {
            firstLine = firstLine.substring(6).trim();
        }

        // Remove the SQL state if present
        int colonIndex = firstLine.indexOf(':');
        if (colonIndex > 0 && colonIndex < 20) {
            firstLine = firstLine.substring(colonIndex + 1).trim();
        }

        return firstLine;
    }

    /**
     * Execute stored procedure with OUT parameters (PostgreSQL 14+)
     */
    public Map<String, Object> executeStoredProcedureWithParams(String procedureName,
                                                                Map<String, Object> inParams,
                                                                List<String> outParamNames) {
        Connection conn = null;
        CallableStatement cstmt = null;

        try {
            log.info("Executing stored procedure with OUT parameters: {}", procedureName);

            long startTime = System.currentTimeMillis();

            conn = getJdbcTemplate().getDataSource().getConnection();

            // Build the call string with placeholders for OUT parameters
            StringBuilder callBuilder = new StringBuilder("{call ");
            callBuilder.append(procedureName).append("(");

            int totalParams = (inParams != null ? inParams.size() : 0) + (outParamNames != null ? outParamNames.size() : 0);
            for (int i = 0; i < totalParams; i++) {
                if (i > 0) callBuilder.append(",");
                callBuilder.append("?");
            }
            callBuilder.append(")}");

            String callSql = callBuilder.toString();
            cstmt = conn.prepareCall(callSql);

            // Set IN parameters
            if (inParams != null && !inParams.isEmpty()) {
                int paramIndex = 1;
                for (Object value : inParams.values()) {
                    setParameter(cstmt, paramIndex++, value);
                }
            }

            // Register OUT parameters
            int outParamStartIndex = (inParams != null ? inParams.size() : 0) + 1;
            for (int i = 0; i < (outParamNames != null ? outParamNames.size() : 0); i++) {
                cstmt.registerOutParameter(outParamStartIndex + i, Types.VARCHAR);
            }

            // Execute the stored procedure
            cstmt.execute();

            // Collect OUT parameter values
            Map<String, Object> outParams = new HashMap<>();
            if (outParamNames != null) {
                for (int i = 0; i < outParamNames.size(); i++) {
                    outParams.put(outParamNames.get(i), cstmt.getObject(outParamStartIndex + i));
                }
            }

            long executionTime = System.currentTimeMillis() - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("outParams", outParams);
            result.put("executionTime", executionTime);
            result.put("success", true);
            result.put("message", "Stored procedure executed successfully");
            result.put("rowCount", 0);

            return result;

        } catch (Exception e) {
            log.error("Error executing stored procedure {}: {}", procedureName, e.getMessage(), e);
            throw new RuntimeException("Failed to execute stored procedure: " + e.getMessage(), e);
        } finally {
            closeResources(null, cstmt, conn);
        }
    }
}