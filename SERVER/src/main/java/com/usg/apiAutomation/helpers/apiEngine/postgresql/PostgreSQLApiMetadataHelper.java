package com.usg.apiAutomation.helpers.apiEngine.postgresql;

import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiAnalyticsDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiDetailsResponseDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.GeneratedApiResponseDTO;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.ApiExecutionLogEntity;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.apiAutomation.repositories.apiGenerationEngine.ApiExecutionLogRepository;
import com.usg.apiAutomation.repositories.schemaBrowser.postgresql.*;
import com.usg.apiAutomation.services.schemaBrowser.PostgreSQLSchemaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PostgreSQLApiMetadataHelper {

    private final PostgreSQLTableRepository tableRepository;
    private final PostgreSQLViewRepository viewRepository;
    private final PostgreSQLProcedureRepository procedureRepository;
    private final PostgreSQLFunctionRepository functionRepository;
    private final PostgreSQLOtherObjectsRepository otherObjectsRepository;
    private final PostgreSQLObjectRepository objectRepository;
    private final JdbcTemplate postgresqlJdbcTemplate;

    public PostgreSQLApiMetadataHelper(
            PostgreSQLTableRepository tableRepository,
            PostgreSQLViewRepository viewRepository,
            PostgreSQLProcedureRepository procedureRepository,
            PostgreSQLFunctionRepository functionRepository,
            PostgreSQLOtherObjectsRepository otherObjectsRepository,
            PostgreSQLObjectRepository objectRepository,
            JdbcTemplate postgresqlJdbcTemplate) {
        this.tableRepository = tableRepository;
        this.viewRepository = viewRepository;
        this.procedureRepository = procedureRepository;
        this.functionRepository = functionRepository;
        this.otherObjectsRepository = otherObjectsRepository;
        this.objectRepository = objectRepository;
        this.postgresqlJdbcTemplate = postgresqlJdbcTemplate;
    }

    public void addAverageExecutionTime(GeneratedApiResponseDTO response, Double avgTime) {
        Map<String, Object> metadata = response.getMetadata() != null ?
                response.getMetadata() : new HashMap<>();
        metadata.put("averageExecutionTimeMs", avgTime);
        response.setMetadata(metadata);
    }

    public void addAverageExecutionTimeToDetails(ApiDetailsResponseDTO response, Double avgTime) {
        Map<String, Object> metadata = response.getMetadata() != null ?
                response.getMetadata() : new HashMap<>();
        metadata.put("averageExecutionTimeMs", avgTime);
        response.setMetadata(metadata);
    }

    public ApiAnalyticsDTO buildApiAnalytics(ApiExecutionLogRepository logRepository,
                                             String apiId,
                                             LocalDateTime startDate,
                                             LocalDateTime endDate) {
        List<ApiExecutionLogEntity> logs = logRepository
                .findByGeneratedApiIdAndExecutedAtBetween(apiId, startDate, endDate);

        long totalCalls = logs.size();
        double avgExecutionTime = logs.stream()
                .mapToLong(ApiExecutionLogEntity::getExecutionTimeMs)
                .average()
                .orElse(0.0);
        long totalErrors = logs.stream()
                .filter(log -> log.getResponseStatus() != null && log.getResponseStatus() >= 400)
                .count();
        double successRate = totalCalls > 0 ?
                ((totalCalls - totalErrors) * 100.0 / totalCalls) : 0.0;

        Map<Integer, Long> statusDistribution = logs.stream()
                .filter(log -> log.getResponseStatus() != null)
                .collect(Collectors.groupingBy(
                        ApiExecutionLogEntity::getResponseStatus,
                        Collectors.counting()
                ));

        List<Object[]> dailyStats = logRepository
                .getDailyCallStats(apiId, startDate, endDate);

        Map<String, Long> dailyCallStats = new HashMap<>();
        if (dailyStats != null) {
            for (Object[] stat : dailyStats) {
                if (stat.length >= 2 && stat[0] != null && stat[1] != null) {
                    dailyCallStats.put(stat[0].toString(), ((Number) stat[1]).longValue());
                }
            }
        }

        return ApiAnalyticsDTO.builder()
                .apiId(apiId)
                .startDate(startDate)
                .endDate(endDate)
                .totalCalls(totalCalls)
                .averageExecutionTimeMs(avgExecutionTime)
                .totalErrors(totalErrors)
                .successRate(successRate)
                .statusDistribution(statusDistribution)
                .dailyCallStats(dailyCallStats)
                .build();
    }

    public Map<String, Object> getSourceObjectDetails(PostgreSQLSchemaService schemaService,
                                                      ApiSourceObjectDTO sourceObject) {
        Map<String, Object> details = new HashMap<>();

        try {
            String targetType = sourceObject.getTargetType() != null ?
                    sourceObject.getTargetType() : sourceObject.getObjectType();
            String targetName = sourceObject.getTargetName() != null ?
                    sourceObject.getTargetName() : sourceObject.getObjectName();
            String targetSchema = sourceObject.getTargetOwner() != null ?
                    sourceObject.getTargetOwner() : sourceObject.getOwner();

            // If schema is not provided, try to determine it
            if (targetSchema == null || targetSchema.isEmpty()) {
                targetSchema = resolveObjectSchema(targetName, targetType);
            }

            log.info("Getting details for {}.{} ({})", targetSchema, targetName, targetType);

            switch (targetType.toUpperCase()) {
                case "TABLE":
                    getTableDetails(details, targetName, targetSchema);
                    break;

                case "VIEW":
                    getViewDetails(details, targetName, targetSchema);
                    break;

                case "MATERIALIZED VIEW":
                    getMaterializedViewDetails(details, targetName, targetSchema);
                    break;

                case "PROCEDURE":
                    getProcedureDetails(details, targetName, targetSchema);
                    break;

                case "FUNCTION":
                    getFunctionDetails(details, targetName, targetSchema);
                    break;

                case "SEQUENCE":
                    getSequenceDetails(details, targetName, targetSchema);
                    break;

                case "TRIGGER":
                    getTriggerDetails(details, targetName, targetSchema);
                    break;

                case "INDEX":
                    getIndexDetails(details, targetName, targetSchema);
                    break;

                case "TYPE":
                    getTypeDetails(details, targetName, targetSchema);
                    break;

                default:
                    getGenericObjectDetails(details, targetName, targetType, targetSchema);
                    break;
            }

        } catch (Exception e) {
            log.error("Could not get source object details for {}: {}",
                    sourceObject.getObjectName(), e.getMessage(), e);
            details.put("error", e.getMessage());
            details.put("hasError", true);
        }

        return details;
    }

    private String resolveObjectSchema(String objectName, String objectType) {
        try {
            String relKind = getRelationKind(objectType);
            String sql;

            if (relKind != null) {
                sql = "SELECT n.nspname FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE c.relname = ? AND c.relkind = ? " +
                        "AND n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                        "LIMIT 1";
                return postgresqlJdbcTemplate.queryForObject(sql, String.class, objectName, relKind);
            } else if ("FUNCTION".equalsIgnoreCase(objectType) || "PROCEDURE".equalsIgnoreCase(objectType)) {
                sql = "SELECT n.nspname FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE p.proname = ? " +
                        "AND n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                        "LIMIT 1";
                return postgresqlJdbcTemplate.queryForObject(sql, String.class, objectName);
            } else {
                sql = "SELECT n.nspname FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE c.relname = ? " +
                        "AND n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                        "LIMIT 1";
                return postgresqlJdbcTemplate.queryForObject(sql, String.class, objectName);
            }
        } catch (Exception e) {
            return getCurrentSchema();
        }
    }

    private String getCurrentSchema() {
        try {
            return postgresqlJdbcTemplate.queryForObject("SELECT current_schema()", String.class);
        } catch (Exception e) {
            return "public";
        }
    }

    private String getRelationKind(String objectType) {
        if (objectType == null) return null;
        String upperType = objectType.toUpperCase();
        switch (upperType) {
            case "TABLE": return "r";
            case "VIEW": return "v";
            case "MATERIALIZED VIEW": return "m";
            case "SEQUENCE": return "S";
            case "INDEX": return "i";
            default: return null;
        }
    }

    private void getTableDetails(Map<String, Object> details, String tableName, String schema) {
        try {
            Map<String, Object> tableDetails = tableRepository.getTableDetails(schema, tableName);

            if (tableDetails != null && !tableDetails.isEmpty()) {
                List<Map<String, Object>> columns = tableRepository.getTableColumns(schema, tableName);
                details.put("columns", transformColumns(columns));

                List<Map<String, Object>> constraints = tableRepository.getTableConstraints(schema, tableName);
                Map<String, Object> primaryKey = findPrimaryKey(constraints);
                if (primaryKey != null) {
                    details.put("primaryKey", primaryKey);
                }

                try {
                    String countSql = "SELECT COUNT(*) FROM " + (schema != null ? schema + "." : "") + tableName;
                    Long rowCount = postgresqlJdbcTemplate.queryForObject(countSql, Long.class);
                    details.put("rowCount", rowCount);
                } catch (Exception e) {
                    details.put("rowCount", tableDetails.get("num_rows"));
                }

                List<Map<String, Object>> indexes = tableRepository.getTableIndexes(schema, tableName);
                if (!indexes.isEmpty()) {
                    details.put("indexes", indexes);
                }

                String comment = getTableComment(schema, tableName);
                if (comment != null && !comment.isEmpty()) {
                    details.put("comment", comment);
                }

                copyCommonDetails(details, tableDetails);
            }
        } catch (Exception e) {
            log.warn("Error getting table details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }

    private void getViewDetails(Map<String, Object> details, String viewName, String schema) {
        try {
            Map<String, Object> viewDetails = viewRepository.getViewDetails(schema, viewName);

            if (viewDetails != null && !viewDetails.isEmpty()) {
                List<Map<String, Object>> columns = viewRepository.getViewColumns(schema, viewName);
                details.put("columns", transformColumns(columns));

                if (viewDetails.containsKey("text")) {
                    details.put("sourceCode", viewDetails.get("text"));
                }

                if (viewDetails.containsKey("column_count")) {
                    details.put("columnCount", viewDetails.get("column_count"));
                } else if (columns != null) {
                    details.put("columnCount", columns.size());
                }

                copyCommonDetails(details, viewDetails);
            }
        } catch (Exception e) {
            log.warn("Error getting view details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }

    private void getMaterializedViewDetails(Map<String, Object> details, String mvName, String schema) {
        try {
            Map<String, Object> mvDetails = viewRepository.getMaterializedViewDetails(schema, mvName);

            if (mvDetails != null && !mvDetails.isEmpty()) {
                details.put("refreshMethod", mvDetails.get("refresh_method"));
                details.put("refreshMode", mvDetails.get("refresh_mode"));
                details.put("lastRefreshDate", mvDetails.get("last_refresh_date"));

                List<Map<String, Object>> columns = viewRepository.getViewColumns(schema, mvName);
                if (!columns.isEmpty()) {
                    details.put("columns", transformColumns(columns));
                }

                if (mvDetails.containsKey("query")) {
                    details.put("sourceCode", mvDetails.get("query"));
                }

                copyCommonDetails(details, mvDetails);
            }
        } catch (Exception e) {
            log.warn("Error getting materialized view details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }

    private void getProcedureDetails(Map<String, Object> details, String procedureName, String schema) {
        try {
            Map<String, Object> procDetails = procedureRepository.getProcedureDetails(schema, procedureName);

            if (procDetails != null && !procDetails.isEmpty()) {
                List<Map<String, Object>> parameters = (List<Map<String, Object>>) procDetails.get("parameters");

                if (parameters != null && !parameters.isEmpty()) {
                    int inCount = 0, outCount = 0, inOutCount = 0;

                    for (Map<String, Object> param : parameters) {
                        String inOut = (String) param.get("in_out");
                        if (inOut != null) {
                            String upperInOut = inOut.toUpperCase();
                            if (upperInOut.contains("IN") && upperInOut.contains("OUT")) {
                                inOutCount++;
                            } else if (upperInOut.contains("OUT")) {
                                outCount++;
                            } else if (upperInOut.contains("IN")) {
                                inCount++;
                            }
                        }
                    }

                    details.put("parameters", parameters);
                    details.put("parameterCount", parameters.size());
                    details.put("inParameterCount", inCount);
                    details.put("outParameterCount", outCount);
                    details.put("inOutParameterCount", inOutCount);
                }

                if (!procDetails.containsKey("source") && !procDetails.containsKey("sourceCode")) {
                    String source = getSourceFromPgCatalog(procedureName, schema, "PROCEDURE");
                    if (source != null) {
                        details.put("sourceCode", source);
                    }
                }

                copyCommonDetails(details, procDetails);
            }
        } catch (Exception e) {
            log.warn("Error getting procedure details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }

    private void getFunctionDetails(Map<String, Object> details, String functionName, String schema) {
        try {
            String source = getSourceFromPgCatalog(functionName, schema, "FUNCTION");

            if (source != null && !source.isEmpty()) {
                log.info("Found source for function {}.{}, length: {}", schema, functionName, source.length());

                String sourceWithoutComments = removeComments(source);

                // Parse function signature
                Pattern pattern = Pattern.compile(
                        "CREATE\\s+(?:OR\\s+REPLACE\\s+)?FUNCTION\\s+" +
                                Pattern.quote(functionName) +
                                "\\s*\\(([\\s\\S]*?)\\)\\s*RETURNS\\s+",
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
                );
                Matcher matcher = pattern.matcher(sourceWithoutComments);

                List<Map<String, Object>> parameters = new ArrayList<>();

                if (matcher.find()) {
                    String paramsSection = matcher.group(1).trim();
                    if (!paramsSection.isEmpty()) {
                        List<String> paramDefs = splitParameters(paramsSection);
                        int position = 1;
                        for (String paramDef : paramDefs) {
                            Map<String, Object> param = parseFunctionParameter(paramDef, position);
                            if (param != null && !param.isEmpty()) {
                                parameters.add(param);
                                position++;
                            }
                        }
                    }
                }

                // Parse return type
                Pattern returnPattern = Pattern.compile(
                        "RETURNS\\s+(\\w+(?:\\([^)]*\\))?)",
                        Pattern.CASE_INSENSITIVE
                );
                Matcher returnMatcher = returnPattern.matcher(sourceWithoutComments);
                if (returnMatcher.find()) {
                    String returnTypeStr = returnMatcher.group(1).trim();

                    Map<String, Object> returnParam = new HashMap<>();
                    returnParam.put("argument_name", "RETURN");
                    returnParam.put("position", 0);
                    returnParam.put("sequence", 1);
                    returnParam.put("data_type", returnTypeStr);
                    returnParam.put("in_out", "OUT");
                    returnParam.put("is_return", true);

                    parameters.add(0, returnParam);
                }

                if (!parameters.isEmpty()) {
                    details.put("parameters", parameters);
                    details.put("parameterCount", parameters.size());

                    int inCount = 0, outCount = 0, inOutCount = 0;
                    for (Map<String, Object> param : parameters) {
                        String inOut = (String) param.get("in_out");
                        if (inOut != null) {
                            if (inOut.equals("IN_OUT")) {
                                inOutCount++;
                            } else if (inOut.equals("OUT")) {
                                outCount++;
                            } else if (inOut.equals("IN")) {
                                inCount++;
                            }
                        }
                    }

                    details.put("inParameterCount", inCount);
                    details.put("outParameterCount", outCount);
                    details.put("inOutParameterCount", inOutCount);
                }

                details.put("sourceCode", source);
            }

            // Fallback to repository
            Map<String, Object> funcDetails = functionRepository.getFunctionDetails(schema, functionName);
            if (funcDetails != null && !funcDetails.isEmpty()) {
                copyCommonDetails(details, funcDetails);
            }

        } catch (Exception e) {
            log.warn("Error getting function details: {}", e.getMessage(), e);
            details.put("error", e.getMessage());
        }
    }

    private Map<String, Object> parseFunctionParameter(String paramDef, int position) {
        Map<String, Object> param = new HashMap<>();

        paramDef = paramDef.replaceAll("\\s+", " ").trim();

        // Pattern: name data_type or name data_type DEFAULT value
        Pattern pattern = Pattern.compile(
                "^(\\w+)\\s+(\\w+(?:\\([^)]*\\))?)\\s*(?:DEFAULT\\s+(.+))?$",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(paramDef);
        if (matcher.find()) {
            param.put("argument_name", matcher.group(1));
            param.put("position", position);
            param.put("sequence", position);
            param.put("data_type", matcher.group(2).trim().toUpperCase());
            param.put("in_out", "IN");
            param.put("defaulted", matcher.group(3) != null ? "YES" : "NO");
        }

        return param.isEmpty() ? null : param;
    }

    private void getSequenceDetails(Map<String, Object> details, String sequenceName, String schema) {
        try {
            Map<String, Object> seqDetails = otherObjectsRepository.getSequenceDetails(schema, sequenceName);

            if (seqDetails != null && !seqDetails.isEmpty()) {
                details.put("minValue", seqDetails.get("min_value"));
                details.put("maxValue", seqDetails.get("max_value"));
                details.put("incrementBy", seqDetails.get("increment_by"));
                details.put("cycleFlag", seqDetails.get("cycle_flag"));
                details.put("cacheSize", seqDetails.get("cache_size"));
                details.put("lastNumber", seqDetails.get("last_number"));

                String ddl = getSequenceDDL(schema, sequenceName);
                if (ddl != null) {
                    details.put("sourceCode", ddl);
                }

                copyCommonDetails(details, seqDetails);
            }
        } catch (Exception e) {
            log.warn("Error getting sequence details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }

    private void getTriggerDetails(Map<String, Object> details, String triggerName, String schema) {
        try {
            Map<String, Object> triggerDetails = otherObjectsRepository.getTriggerDetails(schema, triggerName);

            if (triggerDetails != null && !triggerDetails.isEmpty()) {
                details.put("triggerType", triggerDetails.get("trigger_type"));
                details.put("triggeringEvent", triggerDetails.get("triggering_event"));
                details.put("tableName", triggerDetails.get("table_name"));
                details.put("triggerBody", triggerDetails.get("trigger_body"));
                details.put("description", triggerDetails.get("description"));

                copyCommonDetails(details, triggerDetails);
            }
        } catch (Exception e) {
            log.warn("Error getting trigger details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }

    private void getIndexDetails(Map<String, Object> details, String indexName, String schema) {
        try {
            Map<String, Object> indexDetails = objectRepository.getObjectDetails(indexName, "INDEX", schema);

            if (indexDetails != null && !indexDetails.isEmpty()) {
                details.put("tableName", indexDetails.get("table_name"));
                details.put("indexType", indexDetails.get("index_type"));
                details.put("uniqueness", indexDetails.get("uniqueness"));
                details.put("tablespace", indexDetails.get("tablespace_name"));
                details.put("columns", indexDetails.get("columns"));
                details.put("columnCount", indexDetails.get("column_count"));

                copyCommonDetails(details, indexDetails);
            }
        } catch (Exception e) {
            log.warn("Error getting index details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }

    private void getTypeDetails(Map<String, Object> details, String typeName, String schema) {
        try {
            Map<String, Object> typeDetails = otherObjectsRepository.getTypeDetails(schema, typeName);

            if (typeDetails != null && !typeDetails.isEmpty()) {
                details.put("typecode", typeDetails.get("typecode"));
                details.put("attributes", typeDetails.get("attributes"));

                if (typeDetails.containsKey("attributeDetails")) {
                    details.put("attributeDetails", typeDetails.get("attributeDetails"));
                }

                copyCommonDetails(details, typeDetails);
            }
        } catch (Exception e) {
            log.warn("Error getting type details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }

    private void getGenericObjectDetails(Map<String, Object> details, String objectName,
                                         String objectType, String schema) {
        try {
            Map<String, Object> objectInfo = objectRepository.getBasicObjectInfo(schema, objectName, objectType);

            copyCommonDetails(details, objectInfo);

            if (isSourceBasedObject(objectType)) {
                String source = getSourceFromPgCatalog(objectName, schema, objectType);
                if (source != null) {
                    details.put("sourceCode", source);
                }
            }

        } catch (Exception e) {
            log.warn("Error getting generic object details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }

    private boolean isSourceBasedObject(String objectType) {
        if (objectType == null) return false;
        String upperType = objectType.toUpperCase();
        return upperType.contains("PROCEDURE") ||
                upperType.contains("FUNCTION") ||
                upperType.contains("TRIGGER") ||
                upperType.contains("TYPE") ||
                upperType.contains("VIEW");
    }

    private void copyCommonDetails(Map<String, Object> dest, Map<String, Object> source) {
        if (source.containsKey("object_name")) {
            dest.put("objectName", source.get("object_name"));
        }
        if (source.containsKey("object_type")) {
            dest.put("objectType", source.get("object_type"));
        }
        if (source.containsKey("owner")) {
            dest.put("owner", source.get("owner"));
        }
        if (source.containsKey("status")) {
            dest.put("status", source.get("status"));
        }
        if (source.containsKey("created")) {
            dest.put("created", source.get("created"));
        }
        if (source.containsKey("lastModified")) {
            dest.put("lastModified", source.get("lastModified"));
        }
        if (source.containsKey("source") && !dest.containsKey("sourceCode")) {
            dest.put("sourceCode", source.get("source"));
        }
        if (source.containsKey("sourceCode") && !dest.containsKey("sourceCode")) {
            dest.put("sourceCode", source.get("sourceCode"));
        }
    }

    private List<Map<String, Object>> transformColumns(List<Map<String, Object>> columns) {
        if (columns == null) return new ArrayList<>();

        return columns.stream().map(col -> {
            Map<String, Object> transformed = new HashMap<>();
            transformed.put("name", col.get("column_name"));
            transformed.put("dataType", col.get("data_type"));
            transformed.put("nullable", col.get("is_nullable"));
            transformed.put("position", col.get("ordinal_position"));
            transformed.put("dataLength", col.get("character_maximum_length"));
            transformed.put("dataPrecision", col.get("numeric_precision"));
            transformed.put("dataScale", col.get("numeric_scale"));
            transformed.put("defaultValue", col.get("column_default"));
            return transformed;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> findPrimaryKey(List<Map<String, Object>> constraints) {
        if (constraints == null) return null;

        return constraints.stream()
                .filter(c -> "PRIMARY KEY".equals(c.get("constraint_type")))
                .findFirst()
                .orElse(null);
    }

    private String getTableComment(String schema, String tableName) {
        try {
            String sql = "SELECT obj_description(quote_ident(?) || '.' || quote_ident(?))";
            return postgresqlJdbcTemplate.queryForObject(sql, String.class, schema, tableName);
        } catch (Exception e) {
            return null;
        }
    }

    private String getSequenceDDL(String schema, String sequenceName) {
        try {
            String sql = "SELECT 'CREATE SEQUENCE ' || " +
                    "CASE WHEN ? != current_schema() THEN ? || '.' ELSE '' END || ? || " +
                    "' START WITH ' || s.seqstart || " +
                    "' INCREMENT BY ' || s.seqincrement || " +
                    "' MINVALUE ' || s.seqmin || " +
                    "' MAXVALUE ' || s.seqmax || " +
                    "CASE WHEN s.seqcycle THEN ' CYCLE' ELSE ' NO CYCLE' END || ';' as ddl " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "JOIN pg_sequence s ON c.oid = s.seqrelid " +
                    "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'S'";

            return postgresqlJdbcTemplate.queryForObject(sql, String.class, schema, schema, sequenceName, schema, sequenceName);
        } catch (Exception e) {
            return null;
        }
    }

    private String getSourceFromPgCatalog(String objectName, String schema, String type) {
        try {
            if ("FUNCTION".equalsIgnoreCase(type) || "PROCEDURE".equalsIgnoreCase(type)) {
                String sql = "SELECT pg_get_functiondef(p.oid) as source " +
                        "FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname = ? AND p.proname = ?";
                return postgresqlJdbcTemplate.queryForObject(sql, String.class, schema, objectName);
            } else if ("VIEW".equalsIgnoreCase(type) || "MATERIALIZED VIEW".equalsIgnoreCase(type)) {
                String relKind = "VIEW".equalsIgnoreCase(type) ? "v" : "m";
                String sql = "SELECT pg_get_viewdef(c.oid) as source " +
                        "FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = ?";
                return postgresqlJdbcTemplate.queryForObject(sql, String.class, schema, objectName, relKind);
            } else if ("TRIGGER".equalsIgnoreCase(type)) {
                String sql = "SELECT pg_get_triggerdef(t.oid) as source " +
                        "FROM pg_trigger t " +
                        "JOIN pg_class c ON t.tgrelid = c.oid " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = ? AND t.tgname = ?";
                return postgresqlJdbcTemplate.queryForObject(sql, String.class, schema, objectName);
            }
            return null;
        } catch (Exception e) {
            log.debug("Could not get source from pg_catalog: {}", e.getMessage());
            return null;
        }
    }

    private String removeComments(String source) {
        if (source == null) return "";

        StringBuilder result = new StringBuilder();
        String[] lines = source.split("\\n");

        for (String line : lines) {
            boolean inQuotes = false;
            int commentStart = -1;

            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);

                if (c == '\'') {
                    inQuotes = !inQuotes;
                } else if (!inQuotes && c == '-' && i + 1 < line.length() && line.charAt(i + 1) == '-') {
                    commentStart = i;
                    break;
                }
            }

            if (commentStart != -1) {
                result.append(line.substring(0, commentStart));
            } else {
                result.append(line);
            }
            result.append("\n");
        }

        return result.toString();
    }

    private List<String> splitParameters(String paramsSection) {
        List<String> parameters = new ArrayList<>();
        StringBuilder currentParam = new StringBuilder();
        int parenCount = 0;
        boolean inQuotes = false;

        for (int i = 0; i < paramsSection.length(); i++) {
            char c = paramsSection.charAt(i);

            if (c == '\'') {
                inQuotes = !inQuotes;
                currentParam.append(c);
            } else if (!inQuotes) {
                if (c == '(') {
                    parenCount++;
                    currentParam.append(c);
                } else if (c == ')') {
                    parenCount--;
                    currentParam.append(c);
                } else if (c == ',' && parenCount == 0) {
                    String param = currentParam.toString().trim();
                    if (!param.isEmpty()) {
                        parameters.add(param);
                    }
                    currentParam = new StringBuilder();
                } else {
                    currentParam.append(c);
                }
            } else {
                currentParam.append(c);
            }
        }

        String lastParam = currentParam.toString().trim();
        if (!lastParam.isEmpty()) {
            parameters.add(lastParam);
        }

        return parameters;
    }

    public String getCodeBaseRequestId(GeneratedApiEntity api) {
        try {
            if (api.getSourceObjectInfo() != null) {
                return (String) api.getSourceObjectInfo().get("codeBaseRequestId");
            }
        } catch (Exception e) {
            log.warn("Failed to extract code base request ID: {}", e.getMessage());
        }
        return null;
    }

    public String getCollectionsCollectionId(GeneratedApiEntity api) {
        try {
            if (api.getSourceObjectInfo() != null) {
                return (String) api.getSourceObjectInfo().get("collectionsCollectionId");
            }
        } catch (Exception e) {
            log.warn("Failed to extract collections collection ID: {}", e.getMessage());
        }
        return null;
    }

    public String getDocumentationCollectionId(GeneratedApiEntity api) {
        try {
            if (api.getSourceObjectInfo() != null) {
                return (String) api.getSourceObjectInfo().get("documentationCollectionId");
            }
        } catch (Exception e) {
            log.warn("Failed to extract documentation collection ID: {}", e.getMessage());
        }
        return null;
    }
}