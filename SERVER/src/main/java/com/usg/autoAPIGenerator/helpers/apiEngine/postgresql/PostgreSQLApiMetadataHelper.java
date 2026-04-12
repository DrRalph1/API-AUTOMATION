package com.usg.autoAPIGenerator.helpers.apiEngine.postgresql;

import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiAnalyticsDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiDetailsResponseDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.GeneratedApiResponseDTO;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.ApiExecutionLogEntity;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.autoAPIGenerator.enums.DatabaseTypeEnum;
import com.usg.autoAPIGenerator.helpers.ApiAnalyticsHelper;
import com.usg.autoAPIGenerator.helpers.DatabaseMetadataHelper;
import com.usg.autoAPIGenerator.repositories.apiGenerationEngine.ApiExecutionLogRepository;
import com.usg.autoAPIGenerator.repositories.schemaBrowser.postgresql.*;
import com.usg.autoAPIGenerator.interfaces.DatabaseSchemaService;
import com.usg.autoAPIGenerator.services.schemaBrowser.PostgreSQLSchemaService;
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
public class PostgreSQLApiMetadataHelper implements DatabaseMetadataHelper, ApiAnalyticsHelper {

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


    private void getFunctionDetails(Map<String, Object> details, String functionName, String schema) {
        try {
            // First try to get from repository
            Map<String, Object> funcDetails = functionRepository.getFunctionDetails(schema, functionName);
            List<Map<String, Object>> parameters = null;

            if (funcDetails != null && !funcDetails.isEmpty()) {
                parameters = (List<Map<String, Object>>) funcDetails.get("parameters");

                // If parameters missing, parse from source
                if (parameters == null || parameters.isEmpty()) {
                    log.info("Database parameters missing for {}.{}, parsing from source", schema, functionName);
                    parameters = parseFunctionParametersFromSource(functionName, schema);
                }
            } else {
                // No repository details, try source directly
                log.info("No repository details for {}.{}, trying source", schema, functionName);
                parameters = parseFunctionParametersFromSource(functionName, schema);
            }

            // Also try to get source for the function
            String source = getSourceFromPgCatalog(functionName, schema, "FUNCTION");
            if (source != null && !source.isEmpty()) {
                details.put("sourceCode", source);
            }

            if (parameters != null && !parameters.isEmpty()) {
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

            if (funcDetails != null && !funcDetails.isEmpty()) {
                copyCommonDetails(details, funcDetails);
            } else if (source != null) {
                // Set basic info from source
                details.put("objectName", functionName);
                details.put("owner", schema);
                details.put("objectType", "FUNCTION");
                details.put("status", "VALID");
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



    // Add this method to PostgreSQLApiMetadataHelper.java

    private void getProcedureDetails(Map<String, Object> details, String procedureName, String schema) {
        try {
            Map<String, Object> procDetails = procedureRepository.getProcedureDetails(schema, procedureName);

            if (procDetails != null && !procDetails.isEmpty()) {
                List<Map<String, Object>> parameters = (List<Map<String, Object>>) procDetails.get("parameters");

                if (parameters == null || parameters.isEmpty()) {
                    log.info("Database parameters missing for {}.{}, parsing from source", schema, procedureName);
                    parameters = parseProcedureParametersFromSource(procedureName, schema);
                }

                if (parameters != null && !parameters.isEmpty()) {
                    // Transform parameters to Oracle-style uppercase fields
                    List<Map<String, Object>> transformedParams = transformParametersToOracleStyle(parameters);

                    details.put("parameters", transformedParams);
                    details.put("parameterCount", transformedParams.size());

                    // Also put in details for the validation response
                    Map<String, Object> detailsMap = new HashMap<>();
                    detailsMap.put("parameters", transformedParams);
                    detailsMap.put("parameterCount", transformedParams.size());
                    details.put("details", detailsMap);

                    // Add counts
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
                    details.put("inParameterCount", inCount);
                    details.put("outParameterCount", outCount);
                    details.put("inOutParameterCount", inOutCount);
                }

                // Get source code
                String source = getSourceFromPgCatalog(procedureName, schema, "PROCEDURE");
                if (source != null) {
                    details.put("sourceCode", source);
                    details.put("source", source);
                }

                copyCommonDetails(details, procDetails);

                // CRITICAL: Ensure these fields are set for validation
                details.put("exists", true);
                details.put("valid", true);
                details.put("objectName", procedureName);
                details.put("objectType", "PROCEDURE");
                details.put("owner", schema);

            } else {
                // Try to get from source directly
                String source = getSourceFromPgCatalog(procedureName, schema, "PROCEDURE");
                if (source != null && !source.isEmpty()) {
                    details.put("sourceCode", source);
                    details.put("exists", true);
                    details.put("valid", true);
                    details.put("objectName", procedureName);
                    details.put("objectType", "PROCEDURE");
                    details.put("owner", schema);

                    // Try to parse parameters from source
                    List<Map<String, Object>> parameters = parseProcedureParametersFromSource(procedureName, schema);
                    if (!parameters.isEmpty()) {
                        List<Map<String, Object>> transformedParams = transformParametersToOracleStyle(parameters);
                        details.put("parameters", transformedParams);
                        details.put("parameterCount", transformedParams.size());

                        Map<String, Object> detailsMap = new HashMap<>();
                        detailsMap.put("parameters", transformedParams);
                        details.put("details", detailsMap);
                    }
                } else {
                    details.put("exists", false);
                    details.put("valid", false);
                    details.put("message", "Object not found: " + procedureName);
                }
            }
        } catch (Exception e) {
            log.warn("Error getting procedure details: {}", e.getMessage());
            details.put("error", e.getMessage());
            details.put("exists", false);
            details.put("valid", false);
        }
    }


    /**
     * Transform parameters to Oracle-style uppercase fields
     */
    private List<Map<String, Object>> transformParametersToOracleStyle(List<Map<String, Object>> parameters) {
        List<Map<String, Object>> transformed = new ArrayList<>();

        for (Map<String, Object> param : parameters) {
            Map<String, Object> oracleParam = new HashMap<>();

            // Map to Oracle-style uppercase field names
            oracleParam.put("POSITION", param.get("position"));
            oracleParam.put("ARGUMENT_NAME", param.get("argument_name"));
            oracleParam.put("DATA_TYPE", param.get("data_type"));
            oracleParam.put("IN_OUT", param.get("in_out"));
            oracleParam.put("DATA_LENGTH", param.get("data_length"));
            oracleParam.put("DATA_PRECISION", param.get("data_precision"));
            oracleParam.put("DATA_SCALE", param.get("data_scale"));
            oracleParam.put("DEFAULT_VALUE", param.get("default_value"));
            oracleParam.put("DEFAULTED", param.get("defaulted"));

            // Also keep lowercase versions for compatibility
            oracleParam.put("argument_name", param.get("argument_name"));
            oracleParam.put("data_type", param.get("data_type"));
            oracleParam.put("in_out", param.get("in_out"));

            transformed.add(oracleParam);
        }

        return transformed;
    }

// Add these helper methods to PostgreSQLApiMetadataHelper.java

    /**
     * Parse procedure parameters from source code
     */
    private List<Map<String, Object>> parseProcedureParametersFromSource(String procedureName, String schema) {
        List<Map<String, Object>> params = new ArrayList<>();

        try {
            String source = getSourceFromPgCatalog(procedureName, schema, "PROCEDURE");
            if (source == null || source.isEmpty()) {
                log.warn("No source found for procedure {}.{}", schema, procedureName);
                return params;
            }

            log.info("Parsing procedure parameters from source for {}.{}", schema, procedureName);

            // Remove comments
            String sourceWithoutComments = removeComments(source);

            // PostgreSQL function/procedure signature pattern
            // Pattern: CREATE [OR REPLACE] PROCEDURE name (param1 type, param2 type, ...)
            Pattern pattern = Pattern.compile(
                    "CREATE\\s+(?:OR\\s+REPLACE\\s+)?PROCEDURE\\s+" +
                            Pattern.quote(procedureName) +
                            "\\s*\\(([\\s\\S]*?)\\)\\s*(?:AS|LANGUAGE|\\$\\$|IS)",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );

            Matcher matcher = pattern.matcher(sourceWithoutComments);

            if (matcher.find()) {
                String paramsSection = matcher.group(1).trim();
                log.info("Found parameters section: '{}'", paramsSection);

                if (!paramsSection.isEmpty()) {
                    List<String> paramDefs = splitParameters(paramsSection);
                    int position = 1;

                    for (String paramDef : paramDefs) {
                        Map<String, Object> param = parsePostgresParameter(paramDef, position, "PROCEDURE");
                        if (param != null && !param.isEmpty()) {
                            params.add(param);
                            position++;
                        }
                    }
                }
            } else {
                // Try alternative pattern for procedures with no parameters
                Pattern altPattern = Pattern.compile(
                        "CREATE\\s+(?:OR\\s+REPLACE\\s+)?PROCEDURE\\s+" +
                                Pattern.quote(procedureName) +
                                "\\s*(?:\\(\\))?\\s*(?:AS|LANGUAGE|\\$\\$|IS)",
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
                );
                Matcher altMatcher = altPattern.matcher(sourceWithoutComments);
                if (altMatcher.find()) {
                    log.info("Procedure has no parameters: {}", procedureName);
                }
            }

            log.info("Parsed {} parameters for procedure {}", params.size(), procedureName);

        } catch (Exception e) {
            log.warn("Error parsing procedure parameters: {}", e.getMessage(), e);
        }

        return params;
    }

    /**
     * Parse function parameters from source
     */
    private List<Map<String, Object>> parseFunctionParametersFromSource(String functionName, String schema) {
        List<Map<String, Object>> params = new ArrayList<>();

        try {
            String source = getSourceFromPgCatalog(functionName, schema, "FUNCTION");
            if (source == null || source.isEmpty()) {
                log.warn("No source found for function {}.{}", schema, functionName);
                return params;
            }

            log.info("Parsing function parameters from source for {}.{}", schema, functionName);

            // Remove comments
            String sourceWithoutComments = removeComments(source);

            // PostgreSQL function signature pattern
            Pattern pattern = Pattern.compile(
                    "CREATE\\s+(?:OR\\s+REPLACE\\s+)?FUNCTION\\s+" +
                            Pattern.quote(functionName) +
                            "\\s*\\(([\\s\\S]*?)\\)\\s*RETURNS\\s+",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );

            Matcher matcher = pattern.matcher(sourceWithoutComments);

            if (matcher.find()) {
                String paramsSection = matcher.group(1).trim();
                log.info("Found parameters section: '{}'", paramsSection);

                if (!paramsSection.isEmpty()) {
                    List<String> paramDefs = splitParameters(paramsSection);
                    int position = 1;

                    for (String paramDef : paramDefs) {
                        Map<String, Object> param = parsePostgresParameter(paramDef, position, "FUNCTION");
                        if (param != null && !param.isEmpty()) {
                            params.add(param);
                            position++;
                        }
                    }
                }
            } else {
                // Try alternative pattern
                Pattern altPattern = Pattern.compile(
                        "CREATE\\s+(?:OR\\s+REPLACE\\s+)?FUNCTION\\s+" +
                                Pattern.quote(functionName) +
                                "\\s*(?:\\(\\))?\\s*RETURNS",
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
                );
                Matcher altMatcher = altPattern.matcher(sourceWithoutComments);
                if (altMatcher.find()) {
                    log.info("Function has no parameters: {}", functionName);
                }
            }

            log.info("Parsed {} parameters for function {}", params.size(), functionName);

        } catch (Exception e) {
            log.warn("Error parsing function parameters: {}", e.getMessage(), e);
        }

        return params;
    }

    /**
     * Parse a single PostgreSQL parameter
     */
    private Map<String, Object> parsePostgresParameter(String paramDef, int position, String objectType) {
        Map<String, Object> param = new HashMap<>();

        // Clean up the parameter definition
        paramDef = paramDef.replaceAll("\\s+", " ").trim();

        // Remove DEFAULT clause for now
        String defaultValue = null;
        int defaultIdx = paramDef.toUpperCase().indexOf("DEFAULT");
        if (defaultIdx > 0) {
            defaultValue = paramDef.substring(defaultIdx + 7).trim();
            paramDef = paramDef.substring(0, defaultIdx).trim();
        }

        // Remove IN/OUT keywords from the definition for parsing
        String paramDefWithoutMode = paramDef;
        String mode = "IN";

        if (paramDefWithoutMode.toUpperCase().startsWith("IN ")) {
            mode = "IN";
            paramDefWithoutMode = paramDefWithoutMode.substring(3).trim();
        } else if (paramDefWithoutMode.toUpperCase().startsWith("OUT ")) {
            mode = "OUT";
            paramDefWithoutMode = paramDefWithoutMode.substring(4).trim();
        } else if (paramDefWithoutMode.toUpperCase().startsWith("INOUT ")) {
            mode = "IN_OUT";
            paramDefWithoutMode = paramDefWithoutMode.substring(6).trim();
        }

        // Parse parameter name and type
        // Pattern: name type
        Pattern pattern = Pattern.compile(
                "^(\\w+)\\s+(\\w+(?:\\([^)]*\\))?)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(paramDefWithoutMode);

        if (matcher.find()) {
            String paramName = matcher.group(1);
            String dataType = matcher.group(2).trim().toUpperCase();

            param.put("argument_name", paramName);
            param.put("position", position);
            param.put("sequence", position);
            param.put("data_type", dataType);
            param.put("in_out", mode);
            param.put("defaulted", defaultValue != null ? "YES" : "NO");

            // Extract length/precision if present
            if (dataType.contains("(")) {
                param.put("data_length", extractLength(dataType));
                param.put("data_precision", extractPrecision(dataType));
                param.put("data_scale", extractScale(dataType));
            }

            log.debug("Parsed parameter: name={}, type={}, mode={}", paramName, dataType, mode);
        } else {
            log.warn("Could not parse parameter: '{}'", paramDef);
        }

        return param.isEmpty() ? null : param;
    }

    /**
     * Extract length from data type (e.g., VARCHAR(100) -> 100)
     */
    private Integer extractLength(String dataType) {
        if (dataType == null) return null;
        Pattern pattern = Pattern.compile("\\((\\d+)\\)");
        Matcher matcher = pattern.matcher(dataType);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    /**
     * Extract precision from data type (e.g., NUMERIC(10,2) -> 10)
     */
    private Integer extractPrecision(String dataType) {
        if (dataType == null) return null;
        Pattern pattern = Pattern.compile("\\((\\d+),\\s*\\d+\\)");
        Matcher matcher = pattern.matcher(dataType);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    /**
     * Extract scale from data type (e.g., NUMERIC(10,2) -> 2)
     */
    private Integer extractScale(String dataType) {
        if (dataType == null) return null;
        Pattern pattern = Pattern.compile("\\(\\d+,\\s*(\\d+)\\)");
        Matcher matcher = pattern.matcher(dataType);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }


    @Override
    public Map<String, Object> getSourceObjectDetails(DatabaseSchemaService schemaService, ApiSourceObjectDTO sourceObject) {
        // Cast to PostgreSQLSchemaService
        if (!(schemaService instanceof PostgreSQLSchemaService)) {
            throw new IllegalArgumentException("Expected PostgreSQLSchemaService but got: " + schemaService.getClass().getSimpleName());
        }
        // Call your existing method
        return getSourceObjectDetails((PostgreSQLSchemaService) schemaService, sourceObject);
    }


    @Override
    public DatabaseTypeEnum getSupportedDatabaseType() {
        return DatabaseTypeEnum.ORACLE;
    }

    @Override
    public List<Map<String, Object>> parseParametersFromSource(String sourceCode, String objectType) {
        List<Map<String, Object>> parameters = new ArrayList<>();

        if (sourceCode == null || sourceCode.isEmpty()) {
            return parameters;
        }

        try {
            String sourceWithoutComments = removeComments(sourceCode);

            // Parse based on object type
            if ("FUNCTION".equalsIgnoreCase(objectType)) {
                // PostgreSQL function signature pattern
                Pattern pattern = Pattern.compile(
                        "CREATE\\s+(?:OR\\s+REPLACE\\s+)?FUNCTION\\s+\\w+\\s*\\(([\\s\\S]*?)\\)\\s*RETURNS\\s+",
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
                );
                Matcher matcher = pattern.matcher(sourceWithoutComments);
                if (matcher.find()) {
                    String paramsSection = matcher.group(1).trim();
                    parameters = parsePostgresParameterSection(paramsSection);
                }
            } else if ("PROCEDURE".equalsIgnoreCase(objectType)) {
                // PostgreSQL procedure signature pattern
                Pattern pattern = Pattern.compile(
                        "CREATE\\s+(?:OR\\s+REPLACE\\s+)?PROCEDURE\\s+\\w+\\s*\\(([\\s\\S]*?)\\)\\s*(?:AS|LANGUAGE|\\$\\$|IS)",
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
                );
                Matcher matcher = pattern.matcher(sourceWithoutComments);
                if (matcher.find()) {
                    String paramsSection = matcher.group(1).trim();
                    parameters = parsePostgresParameterSection(paramsSection);
                }
            }

        } catch (Exception e) {
            log.error("Error parsing parameters from source: {}", e.getMessage(), e);
        }

        return parameters;
    }

    @Override
    public Map<String, Object> transformToCommonFormat(Map<String, Object> rawData) {
        Map<String, Object> commonFormat = new HashMap<>();

        if (rawData == null) return commonFormat;

        // Transform columns to common format
        if (rawData.containsKey("columns") && rawData.get("columns") instanceof List) {
            List<Map<String, Object>> columns = (List<Map<String, Object>>) rawData.get("columns");
            List<Map<String, Object>> commonColumns = columns.stream().map(col -> {
                Map<String, Object> commonCol = new HashMap<>();
                // PostgreSQL uses lowercase field names
                commonCol.put("name", col.getOrDefault("column_name", col.get("name")));
                commonCol.put("dataType", col.getOrDefault("data_type", col.get("type")));

                // Handle nullable - PostgreSQL uses YES/NO or true/false
                Object nullable = col.getOrDefault("is_nullable", col.get("nullable"));
                boolean isNullable = false;
                if (nullable instanceof String) {
                    isNullable = "YES".equalsIgnoreCase((String) nullable);
                } else if (nullable instanceof Boolean) {
                    isNullable = (Boolean) nullable;
                }
                commonCol.put("nullable", isNullable);

                commonCol.put("position", col.getOrDefault("ordinal_position", col.get("position")));
                commonCol.put("dataLength", col.getOrDefault("character_maximum_length", col.get("data_length")));
                commonCol.put("dataPrecision", col.getOrDefault("numeric_precision", col.get("data_precision")));
                commonCol.put("dataScale", col.getOrDefault("numeric_scale", col.get("data_scale")));
                commonCol.put("defaultValue", col.getOrDefault("column_default", col.get("default_value")));
                return commonCol;
            }).collect(Collectors.toList());
            commonFormat.put("columns", commonColumns);
        }

        // Transform parameters to common format
        if (rawData.containsKey("parameters") && rawData.get("parameters") instanceof List) {
            List<Map<String, Object>> parameters = (List<Map<String, Object>>) rawData.get("parameters");
            List<Map<String, Object>> commonParams = parameters.stream().map(param -> {
                Map<String, Object> commonParam = new HashMap<>();
                // PostgreSQL uses lowercase field names
                commonParam.put("name", param.getOrDefault("argument_name", param.get("name")));
                commonParam.put("dataType", param.getOrDefault("data_type", param.get("type")));
                commonParam.put("mode", param.getOrDefault("in_out", param.get("mode")));
                commonParam.put("position", param.getOrDefault("position", param.get("sequence")));
                commonParam.put("defaultValue", param.getOrDefault("default_value", param.get("defaultValue")));
                return commonParam;
            }).collect(Collectors.toList());
            commonFormat.put("parameters", commonParams);
        }

        // Copy common fields - PostgreSQL uses lowercase field names
        commonFormat.put("objectName", rawData.getOrDefault("object_name", rawData.get("name")));
        commonFormat.put("objectType", rawData.getOrDefault("object_type", rawData.get("type")));
        commonFormat.put("owner", rawData.getOrDefault("owner", rawData.get("schema")));
        commonFormat.put("status", rawData.getOrDefault("status", "VALID"));
        commonFormat.put("created", rawData.get("created"));
        commonFormat.put("lastModified", rawData.get("lastModified"));

        return commonFormat;
    }

    /**
     * Helper method to parse PostgreSQL parameter section
     */
    private List<Map<String, Object>> parsePostgresParameterSection(String paramsSection) {
        List<Map<String, Object>> parameters = new ArrayList<>();

        if (paramsSection == null || paramsSection.isEmpty()) {
            return parameters;
        }

        List<String> paramDefs = splitParameters(paramsSection);
        int position = 1;

        for (String paramDef : paramDefs) {
            Map<String, Object> param = parsePostgresParameterDefinition(paramDef, position++);
            if (param != null && !param.isEmpty()) {
                parameters.add(param);
            }
        }

        return parameters;
    }

    /**
     * Parse a single PostgreSQL parameter definition
     */
    private Map<String, Object> parsePostgresParameterDefinition(String paramDef, int position) {
        Map<String, Object> param = new HashMap<>();

        // Clean up the parameter definition
        paramDef = paramDef.replaceAll("\\s+", " ").trim();

        // Remove DEFAULT clause for now
        String defaultValue = null;
        int defaultIdx = paramDef.toUpperCase().indexOf("DEFAULT");
        if (defaultIdx > 0) {
            defaultValue = paramDef.substring(defaultIdx + 7).trim();
            paramDef = paramDef.substring(0, defaultIdx).trim();
        }

        // Remove IN/OUT keywords from the definition for parsing
        String paramDefWithoutMode = paramDef;
        String mode = "IN";

        if (paramDefWithoutMode.toUpperCase().startsWith("IN ")) {
            mode = "IN";
            paramDefWithoutMode = paramDefWithoutMode.substring(3).trim();
        } else if (paramDefWithoutMode.toUpperCase().startsWith("OUT ")) {
            mode = "OUT";
            paramDefWithoutMode = paramDefWithoutMode.substring(4).trim();
        } else if (paramDefWithoutMode.toUpperCase().startsWith("INOUT ")) {
            mode = "INOUT";
            paramDefWithoutMode = paramDefWithoutMode.substring(6).trim();
        }

        // Parse parameter name and type
        // Pattern: name type
        Pattern pattern = Pattern.compile(
                "^(\\w+)\\s+(\\w+(?:\\([^)]*\\))?)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(paramDefWithoutMode);

        if (matcher.find()) {
            String paramName = matcher.group(1);
            String dataType = matcher.group(2).trim().toUpperCase();

            param.put("argument_name", paramName);
            param.put("position", position);
            param.put("sequence", position);
            param.put("data_type", dataType);
            param.put("in_out", mode);
            param.put("defaulted", defaultValue != null ? "YES" : "NO");
            param.put("default_value", defaultValue);

            // Extract length/precision if present
            if (dataType.contains("(")) {
                param.put("data_length", extractLength(dataType));
                param.put("data_precision", extractPrecision(dataType));
                param.put("data_scale", extractScale(dataType));
            }

            log.debug("Parsed parameter: name={}, type={}, mode={}", paramName, dataType, mode);
        } else {
            log.warn("Could not parse parameter: '{}'", paramDef);
        }

        return param.isEmpty() ? null : param;
    }


}