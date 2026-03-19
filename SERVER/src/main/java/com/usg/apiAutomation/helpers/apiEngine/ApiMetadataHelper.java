package com.usg.apiAutomation.helpers.apiEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiAnalyticsDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiDetailsResponseDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.GeneratedApiResponseDTO;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.ApiExecutionLogEntity;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.apiAutomation.repositories.oracle.OracleSchemaRepository;
import com.usg.apiAutomation.repositories.postgres.apiGenerationEngine.ApiExecutionLogRepository;
import com.usg.apiAutomation.services.OracleSchemaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ApiMetadataHelper {

    private final OracleSchemaRepository oracleSchemaRepository;
    private final JdbcTemplate oracleJdbcTemplate;

    public ApiMetadataHelper(OracleSchemaRepository oracleSchemaRepository, JdbcTemplate oracleJdbcTemplate) {
        this.oracleSchemaRepository = oracleSchemaRepository;
        this.oracleJdbcTemplate = oracleJdbcTemplate;
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


    public Map<String, Object> getSourceObjectDetails(OracleSchemaService oracleSchemaService,
                                                      ApiSourceObjectDTO sourceObject) {
        Map<String, Object> details = new HashMap<>();

        try {
            String targetType = sourceObject.getTargetType() != null ?
                    sourceObject.getTargetType() : sourceObject.getObjectType();
            String targetName = sourceObject.getTargetName() != null ?
                    sourceObject.getTargetName() : sourceObject.getObjectName();
            String targetOwner = sourceObject.getTargetOwner() != null ?
                    sourceObject.getTargetOwner() : sourceObject.getOwner();

            // If owner is not provided, try to determine it
            if (targetOwner == null || targetOwner.isEmpty()) {
                targetOwner = resolveObjectOwner(targetName, targetType);
            }

            log.info("Getting details for {}.{} ({})", targetOwner, targetName, targetType);

            switch (targetType.toUpperCase()) {
                case "TABLE":
                    getTableDetails(details, targetName, targetOwner);
                    break;

                case "VIEW":
                    getViewDetails(details, targetName, targetOwner);
                    break;

                case "PROCEDURE":
                    getProcedureDetails(details, targetName, targetOwner);
                    break;

                case "FUNCTION":
                    getFunctionDetails(details, targetName, targetOwner);
                    break;

                case "PACKAGE":
                    getPackageDetails(details, targetName, targetOwner);
                    break;

                case "SEQUENCE":
                    getSequenceDetails(details, targetName, targetOwner);
                    break;

                case "TRIGGER":
                    getTriggerDetails(details, targetName, targetOwner);
                    break;

                case "SYNONYM":
                    getSynonymDetails(details, targetName, targetOwner);
                    break;

                case "INDEX":
                    getIndexDetails(details, targetName, targetOwner);
                    break;

                case "TYPE":
                    getTypeDetails(details, targetName, targetOwner);
                    break;

                case "MATERIALIZED VIEW":
                case "MATERIALIZED_VIEW":
                    getMaterializedViewDetails(details, targetName, targetOwner);
                    break;

                case "DATABASE LINK":
                case "DB_LINK":
                    getDatabaseLinkDetails(details, targetName, targetOwner);
                    break;

                default:
                    // For any other object type, get basic info
                    getGenericObjectDetails(details, targetName, targetType, targetOwner);
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

    /**
     * Resolve object owner if not provided
     */
    private String resolveObjectOwner(String objectName, String objectType) {
        try {
            String sql = "SELECT owner FROM all_objects " +
                    "WHERE UPPER(object_name) = UPPER(?) " +
                    "AND UPPER(object_type) = UPPER(?) AND ROWNUM = 1";
            return oracleJdbcTemplate.queryForObject(sql, String.class, objectName, objectType);
        } catch (Exception e) {
            try {
                // Try without object type
                String sql = "SELECT owner FROM all_objects " +
                        "WHERE UPPER(object_name) = UPPER(?) AND ROWNUM = 1";
                return oracleJdbcTemplate.queryForObject(sql, String.class, objectName);
            } catch (Exception ex) {
                return getCurrentUser();
            }
        }
    }

    /**
     * Get current Oracle user
     */
    private String getCurrentUser() {
        try {
            return oracleJdbcTemplate.queryForObject("SELECT USER FROM DUAL", String.class);
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * Get table details
     */
    private void getTableDetails(Map<String, Object> details, String tableName, String owner) {
        try {
            Map<String, Object> tableDetails = oracleSchemaRepository.getTableDetails(owner, tableName);

            if (tableDetails != null && !tableDetails.isEmpty()) {
                // Get columns
                List<Map<String, Object>> columns = oracleSchemaRepository.getTableColumns(owner, tableName);
                details.put("columns", transformColumns(columns));

                // Get primary key
                List<Map<String, Object>> constraints = oracleSchemaRepository.getTableConstraints(owner, tableName);
                Map<String, Object> primaryKey = findPrimaryKey(constraints);
                if (primaryKey != null) {
                    details.put("primaryKey", primaryKey);
                }

                // Get row count
                try {
                    String countSql = "SELECT COUNT(*) FROM " + (owner != null ? owner + "." : "") + tableName;
                    Long rowCount = oracleJdbcTemplate.queryForObject(countSql, Long.class);
                    details.put("rowCount", rowCount);
                } catch (Exception e) {
                    details.put("rowCount", tableDetails.get("num_rows"));
                }

                // Get indexes
                List<Map<String, Object>> indexes = oracleSchemaRepository.getTableIndexes(owner, tableName);
                if (!indexes.isEmpty()) {
                    details.put("indexes", indexes);
                }

                // Get table comment
                String comment = getTableComment(owner, tableName);
                if (comment != null && !comment.isEmpty()) {
                    details.put("comment", comment);
                }

                // Copy other details
                copyCommonDetails(details, tableDetails);
            }
        } catch (Exception e) {
            log.warn("Error getting table details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }

    /**
     * Get view details
     */
    private void getViewDetails(Map<String, Object> details, String viewName, String owner) {
        try {
            Map<String, Object> viewDetails = oracleSchemaRepository.getViewDetails(owner, viewName);

            if (viewDetails != null && !viewDetails.isEmpty()) {
                // Get columns
                List<Map<String, Object>> columns = oracleSchemaRepository.getViewColumns(owner, viewName);
                details.put("columns", transformColumns(columns));

                // Get view text/source
                if (viewDetails.containsKey("text")) {
                    details.put("sourceCode", viewDetails.get("text"));
                }

                // Get column count
                if (viewDetails.containsKey("column_count")) {
                    details.put("columnCount", viewDetails.get("column_count"));
                } else if (columns != null) {
                    details.put("columnCount", columns.size());
                }

                // Copy other details
                copyCommonDetails(details, viewDetails);
            }
        } catch (Exception e) {
            log.warn("Error getting view details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }

    /**
     * Get procedure details - Enhanced with source parsing
     */
    private void getProcedureDetails(Map<String, Object> details, String procedureName, String owner) {
        try {
            // Try to get from database first
            Map<String, Object> procDetails = oracleSchemaRepository.getProcedureDetails(owner, procedureName);

            if (procDetails != null && !procDetails.isEmpty()) {
                List<Map<String, Object>> parameters = (List<Map<String, Object>>) procDetails.get("parameters");

                // If parameters are missing or suspicious, parse from source
                if (parameters == null || parameters.isEmpty()) {
                    log.info("Database parameters missing for {}.{}, parsing from source", owner, procedureName);
                    parameters = parseProcedureParametersFromSource(procedureName, owner);
                }

                if (parameters != null && !parameters.isEmpty()) {
                    // Count IN, OUT, and IN/OUT parameters
                    int inCount = 0, outCount = 0, inOutCount = 0;

                    for (Map<String, Object> param : parameters) {
                        String inOut = (String) param.get("in_out");
                        if (inOut == null) inOut = (String) param.get("IN_OUT");

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

                // Get source code if not already present
                if (!procDetails.containsKey("source") && !procDetails.containsKey("sourceCode")) {
                    String source = getSourceFromAllSource(procedureName, owner, "PROCEDURE");
                    if (source != null) {
                        details.put("sourceCode", source);
                    }
                }

                // Copy other details
                copyCommonDetails(details, procDetails);

                // Check if it's a package procedure
                if (procDetails.containsKey("package_name")) {
                    details.put("packageName", procDetails.get("package_name"));
                    details.put("isPackageProcedure", true);
                }
            }
        } catch (Exception e) {
            log.warn("Error getting procedure details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }







    /**
     * Get function details - Fixed version that properly parses parameters from source
     * and includes return type as a parameter
     */
    private void getFunctionDetails(Map<String, Object> details, String functionName, String owner) {
        try {
            // Try to get from source directly - this should always work
            String source = getSourceFromAllSource(functionName, owner, "FUNCTION");

            if (source != null && !source.isEmpty()) {
                log.info("Found source for function {}.{}, length: {}", owner, functionName, source.length());

                // Remove comments
                String sourceWithoutComments = removeComments(source);

                // Try multiple patterns to find function signature
                String[] patterns = {
                        "FUNCTION\\s+" + functionName + "\\s*\\(([\\s\\S]*?)\\)\\s*RETURN",
                        "FUNCTION\\s+" + functionName + "\\s*\\(([\\s\\S]*?)\\)",
                        "function\\s+" + functionName + "\\s*\\(([\\s\\S]*?)\\)\\s*return",
                        functionName + "\\s*\\(([\\s\\S]*?)\\)\\s*RETURN"
                };

                String paramsSection = null;

                for (String patternStr : patterns) {
                    Pattern p = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                    Matcher m = p.matcher(sourceWithoutComments);

                    if (m.find()) {
                        paramsSection = m.group(1).trim();
                        log.info("Found parameter section for {} using pattern: {}", functionName, patternStr);
                        log.info("Parameter section: '{}'", paramsSection);
                        break;
                    }
                }

                // Parse regular parameters
                List<Map<String, Object>> parameters = new ArrayList<>();

                if (paramsSection != null && !paramsSection.isEmpty()) {
                    // Split parameters by comma, respecting parentheses
                    List<String> paramDefs = splitParameters(paramsSection);
                    log.info("Found {} parameter definitions", paramDefs.size());

                    int position = 1;
                    for (String paramDef : paramDefs) {
                        log.info("Processing parameter definition: '{}'", paramDef);
                        Map<String, Object> param = parseFunctionParameter(paramDef, position);
                        if (param != null && !param.isEmpty()) {
                            parameters.add(param);
                            position++;
                            log.info("Successfully parsed parameter: {}", param);
                        }
                    }
                }

                // Find return type
                Pattern returnPattern = Pattern.compile(
                        "RETURN\\s+(\\w+(?:\\([^)]*\\))?)",
                        Pattern.CASE_INSENSITIVE
                );
                Matcher returnMatcher = returnPattern.matcher(sourceWithoutComments);
                if (returnMatcher.find()) {
                    String returnTypeStr = returnMatcher.group(1).trim();

                    // Create return type as a parameter
                    Map<String, Object> returnParam = new HashMap<>();
                    returnParam.put("argument_name", "RETURN");
                    returnParam.put("position", 0);
                    returnParam.put("sequence", 1);
                    returnParam.put("data_type", returnTypeStr);
                    returnParam.put("in_out", "OUT");

                    // Extract length/precision if present
                    if (returnTypeStr.contains("(")) {
                        returnParam.put("data_length", extractLength(returnTypeStr));
                        returnParam.put("data_precision", extractPrecision(returnTypeStr));
                        returnParam.put("data_scale", extractScale(returnTypeStr));
                    }

                    returnParam.put("defaulted", "N");
                    returnParam.put("is_return", true);

                    // Add return type as the first parameter
                    parameters.add(0, returnParam);

                    log.info("Added return type as parameter for {}: {}", functionName, returnTypeStr);
                }

                if (!parameters.isEmpty()) {
                    details.put("parameters", parameters);
                    details.put("parameterCount", parameters.size());

                    // Count IN/OUT/IN OUT parameters
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

                    log.info("Parsed {} parameters for function {} (including return type)", parameters.size(), functionName);
                } else {
                    log.warn("No parameters parsed for function {}", functionName);
                }

                // Add source code
                details.put("sourceCode", source);
            } else {
                log.warn("No source found for function {}.{}", owner, functionName);
            }

            // Try to get from repository as fallback (but don't override parameters we already found)
            Map<String, Object> funcDetails = oracleSchemaRepository.getFunctionDetails(owner, functionName);
            if (funcDetails != null && !funcDetails.isEmpty()) {
                copyCommonDetails(details, funcDetails);

                // Only add parameters from repository if we didn't find any from source
                if (!details.containsKey("parameters") || ((List)details.get("parameters")).isEmpty()) {
                    if (funcDetails.containsKey("parameters")) {
                        List<Map<String, Object>> repoParams = (List<Map<String, Object>>) funcDetails.get("parameters");

                        // If repository parameters exist, make sure return type is included as first parameter
                        boolean hasReturnParam = false;
                        for (Map<String, Object> param : repoParams) {
                            if (param.get("argument_name") != null && "RETURN".equals(param.get("argument_name"))) {
                                hasReturnParam = true;
                                break;
                            }
                        }

                        if (!hasReturnParam && funcDetails.containsKey("returnType")) {
                            // Add return type as parameter if missing
                            Map<String, Object> returnParam = new HashMap<>();
                            returnParam.put("argument_name", "RETURN");
                            returnParam.put("position", 0);
                            returnParam.put("data_type", funcDetails.get("returnType"));
                            returnParam.put("in_out", "OUT");
                            returnParam.put("is_return", true);
                            repoParams.add(0, returnParam);
                        }

                        details.put("parameters", repoParams);
                        details.put("parameterCount", repoParams.size());
                    }
                }
            }

        } catch (Exception e) {
            log.warn("Error getting function details: {}", e.getMessage(), e);
            details.put("error", e.getMessage());
        }
    }




    /**
     * Specialized parameter parser for functions that handles the exact format: p_trans_code IN varchar2
     */
    private Map<String, Object> parseFunctionParameter(String paramDef, int position) {
        Map<String, Object> param = new HashMap<>();

        // Clean up the parameter definition
        paramDef = paramDef.replaceAll("\\s+", " ").trim();

        // Remove inline comments
        int commentIdx = paramDef.indexOf("--");
        if (commentIdx > 0) {
            paramDef = paramDef.substring(0, commentIdx).trim();
        }

        if (paramDef.isEmpty()) {
            return null;
        }

        log.debug("Parsing function parameter: '{}' at position {}", paramDef, position);

        // Pattern specifically for function parameters: name IN/OUT type
        // This matches: p_trans_code IN varchar2
        Pattern pattern = Pattern.compile(
                "^\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s+" +  // parameter name (p_trans_code)
                        "(IN|OUT|IN\\s+OUT)\\s+" +              // direction (IN)
                        "([a-zA-Z_][a-zA-Z0-9_]*" +             // data type (varchar2)
                        "(?:\\s*\\(" +
                        "[^)]*" +
                        "\\))?)" +                               // optional size/precision
                        "(?:\\s+(?:DEFAULT|:=)\\s+(.+))?",      // default value (optional)
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(paramDef);

        if (matcher.find()) {
            String paramName = matcher.group(1);
            String direction = matcher.group(2);
            String dataType = matcher.group(3);
            String defaultValue = matcher.group(4);

            // Clean up data type
            dataType = dataType.trim().toUpperCase();

            param.put("argument_name", paramName);
            param.put("position", position);
            param.put("sequence", position);
            param.put("data_type", dataType);
            param.put("in_out", direction.toUpperCase().replace(" ", "_"));

            // Extract length/precision if present
            if (dataType.contains("(")) {
                param.put("data_length", extractLength(dataType));
                param.put("data_precision", extractPrecision(dataType));
                param.put("data_scale", extractScale(dataType));
            }

            param.put("defaulted", defaultValue != null ? "YES" : "NO");

            log.info("Successfully parsed function parameter: name={}, direction={}, type={}",
                    paramName, direction, dataType);
        } else {
            // Try alternative pattern for cases where direction might be missing (though your sample has it)
            Pattern altPattern = Pattern.compile(
                    "^\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s+" +  // parameter name
                            "([a-zA-Z_][a-zA-Z0-9_]*" +             // data type
                            "(?:\\s*\\(" +
                            "[^)]*" +
                            "\\))?)" +
                            "(?:\\s+(?:DEFAULT|:=)\\s+(.+))?",
                    Pattern.CASE_INSENSITIVE
            );

            Matcher altMatcher = altPattern.matcher(paramDef);
            if (altMatcher.find()) {
                String paramName = altMatcher.group(1);
                String dataType = altMatcher.group(2);
                String defaultValue = altMatcher.group(3);

                param.put("argument_name", paramName);
                param.put("position", position);
                param.put("sequence", position);
                param.put("data_type", dataType.trim().toUpperCase());
                param.put("in_out", "IN"); // Default to IN
                param.put("data_length", extractLength(dataType));
                param.put("data_precision", extractPrecision(dataType));
                param.put("data_scale", extractScale(dataType));
                param.put("defaulted", defaultValue != null ? "YES" : "NO");

                log.info("Parsed function parameter with alt pattern: name={}, type={}", paramName, dataType);
            } else {
                log.warn("Could not parse function parameter: '{}'", paramDef);
            }
        }

        return param.isEmpty() ? null : param;
    }


    /**
     * Get package details
     */
    private void getPackageDetails(Map<String, Object> details, String packageName, String owner) {
        try {
            Map<String, Object> pkgDetails = oracleSchemaRepository.getPackageDetails(owner, packageName);

            if (pkgDetails != null && !pkgDetails.isEmpty()) {
                // Get procedures
                List<Map<String, Object>> procedures = (List<Map<String, Object>>) pkgDetails.get("procedures");
                if (procedures != null) {
                    details.put("procedures", procedures);
                    details.put("procedureCount", procedures.size());
                }

                // Get functions
                List<Map<String, Object>> functions = (List<Map<String, Object>>) pkgDetails.get("functions");
                if (functions != null) {
                    details.put("functions", functions);
                    details.put("functionCount", functions.size());
                }

                // Get package specification
                if (pkgDetails.containsKey("specSource")) {
                    details.put("specSource", pkgDetails.get("specSource"));
                }

                // Get package body
                if (pkgDetails.containsKey("bodySource")) {
                    details.put("bodySource", pkgDetails.get("bodySource"));
                }

                // Get full source
                String fullSource = getPackageSource(owner, packageName);
                if (fullSource != null) {
                    details.put("sourceCode", fullSource);
                }

                // Copy other details
                copyCommonDetails(details, pkgDetails);
            }
        } catch (Exception e) {
            log.warn("Error getting package details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }

    /**
     * Get sequence details
     */
    private void getSequenceDetails(Map<String, Object> details, String sequenceName, String owner) {
        try {
            Map<String, Object> seqDetails = oracleSchemaRepository.getSequenceDetails(owner, sequenceName);

            if (seqDetails != null && !seqDetails.isEmpty()) {
                details.put("minValue", seqDetails.get("min_value"));
                details.put("maxValue", seqDetails.get("max_value"));
                details.put("incrementBy", seqDetails.get("increment_by"));
                details.put("cycleFlag", seqDetails.get("cycle_flag"));
                details.put("orderFlag", seqDetails.get("order_flag"));
                details.put("cacheSize", seqDetails.get("cache_size"));
                details.put("lastNumber", seqDetails.get("last_number"));

                // Get DDL
                String ddl = getSequenceDDL(owner, sequenceName);
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

    /**
     * Get trigger details
     */
    private void getTriggerDetails(Map<String, Object> details, String triggerName, String owner) {
        try {
            Map<String, Object> triggerDetails = oracleSchemaRepository.getTriggerDetails(owner, triggerName);

            if (triggerDetails != null && !triggerDetails.isEmpty()) {
                details.put("triggerType", triggerDetails.get("trigger_type"));
                details.put("triggeringEvent", triggerDetails.get("triggering_event"));
                details.put("tableName", triggerDetails.get("table_name"));
                details.put("tableOwner", triggerDetails.get("table_owner"));
                details.put("triggerBody", triggerDetails.get("trigger_body"));
                details.put("description", triggerDetails.get("description"));
                details.put("whenClause", triggerDetails.get("when_clause"));

                if (triggerDetails.containsKey("referenced_columns")) {
                    details.put("referencedColumns", triggerDetails.get("referenced_columns"));
                }

                copyCommonDetails(details, triggerDetails);
            }
        } catch (Exception e) {
            log.warn("Error getting trigger details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }

    /**
     * Get synonym details
     */
    private void getSynonymDetails(Map<String, Object> details, String synonymName, String owner) {
        try {
            Map<String, Object> synonymDetails = oracleSchemaRepository.getSynonymDetails(synonymName);

            if (synonymDetails != null && !synonymDetails.isEmpty()) {
                details.put("targetOwner", synonymDetails.get("target_owner"));
                details.put("targetName", synonymDetails.get("target_name"));
                details.put("targetType", synonymDetails.get("target_type"));
                details.put("dbLink", synonymDetails.get("db_link"));
                details.put("isRemote", synonymDetails.get("db_link") != null);

                if (synonymDetails.containsKey("targetStatus")) {
                    details.put("targetStatus", synonymDetails.get("target_status"));
                }

                copyCommonDetails(details, synonymDetails);
            }
        } catch (Exception e) {
            log.warn("Error getting synonym details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }

    /**
     * Get index details
     */
    private void getIndexDetails(Map<String, Object> details, String indexName, String owner) {
        try {
            Map<String, Object> indexDetails = oracleSchemaRepository.getIndexDetails(owner, indexName);

            if (indexDetails != null && !indexDetails.isEmpty()) {
                details.put("tableName", indexDetails.get("table_name"));
                details.put("tableOwner", indexDetails.get("table_owner"));
                details.put("indexType", indexDetails.get("index_type"));
                details.put("uniqueness", indexDetails.get("uniqueness"));
                details.put("tablespace", indexDetails.get("tablespace_name"));
                details.put("columns", indexDetails.get("columns"));
                details.put("columnCount", indexDetails.get("column_count"));
                details.put("visibility", indexDetails.get("visibility"));
                details.put("distinctKeys", indexDetails.get("distinct_keys"));
                details.put("leafBlocks", indexDetails.get("leaf_blocks"));
                details.put("clusteringFactor", indexDetails.get("clustering_factor"));

                copyCommonDetails(details, indexDetails);
            }
        } catch (Exception e) {
            log.warn("Error getting index details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }

    /**
     * Get type details
     */
    private void getTypeDetails(Map<String, Object> details, String typeName, String owner) {
        try {
            Map<String, Object> typeDetails = oracleSchemaRepository.getTypeDetails(owner, typeName);

            if (typeDetails != null && !typeDetails.isEmpty()) {
                details.put("typecode", typeDetails.get("typecode"));
                details.put("attributes", typeDetails.get("attributes"));
                details.put("methods", typeDetails.get("methods"));
                details.put("final", typeDetails.get("final"));
                details.put("instantiable", typeDetails.get("instantiable"));

                if (typeDetails.containsKey("attributeDetails")) {
                    details.put("attributeDetails", typeDetails.get("attributeDetails"));
                }

                if (typeDetails.containsKey("methodDetails")) {
                    details.put("methodDetails", typeDetails.get("methodDetails"));
                }

                copyCommonDetails(details, typeDetails);
            }
        } catch (Exception e) {
            log.warn("Error getting type details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }

    /**
     * Get materialized view details
     */
    private void getMaterializedViewDetails(Map<String, Object> details, String mvName, String owner) {
        try {
            Map<String, Object> mvDetails = oracleSchemaRepository.getMaterializedViewDetails(owner, mvName);

            if (mvDetails != null && !mvDetails.isEmpty()) {
                details.put("containerName", mvDetails.get("container_name"));
                details.put("refreshMethod", mvDetails.get("refresh_method"));
                details.put("refreshMode", mvDetails.get("refresh_mode"));
                details.put("buildMode", mvDetails.get("build_mode"));
                details.put("fastRefreshable", mvDetails.get("fast_refreshable"));
                details.put("lastRefreshType", mvDetails.get("last_refresh_type"));
                details.put("lastRefreshDate", mvDetails.get("last_refresh_date"));
                details.put("staleness", mvDetails.get("staleness"));

                // Get columns
                List<Map<String, Object>> columns = getMaterializedViewColumns(owner, mvName);
                if (!columns.isEmpty()) {
                    details.put("columns", transformColumns(columns));
                }

                // Get query
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

    /**
     * Get database link details
     */
    private void getDatabaseLinkDetails(Map<String, Object> details, String dbLinkName, String owner) {
        try {
            Map<String, Object> dbLinkDetails = oracleSchemaRepository.getDatabaseLinkDetails(owner, dbLinkName);

            if (dbLinkDetails != null && !dbLinkDetails.isEmpty()) {
                details.put("username", dbLinkDetails.get("username"));
                details.put("host", dbLinkDetails.get("host"));
                details.put("created", dbLinkDetails.get("created"));

                copyCommonDetails(details, dbLinkDetails);
            }
        } catch (Exception e) {
            log.warn("Error getting database link details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }

    /**
     * Get generic object details for any other object type
     */
    private void getGenericObjectDetails(Map<String, Object> details, String objectName,
                                         String objectType, String owner) {
        try {
            String sql = "SELECT owner, object_name, object_type, status, created, last_ddl_time " +
                    "FROM all_objects WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?)";

            Map<String, Object> objectInfo = oracleJdbcTemplate.queryForMap(sql, owner, objectName);

            copyCommonDetails(details, objectInfo);

            // Try to get source if applicable
            if (isSourceBasedObject(objectType)) {
                String source = getSourceFromAllSource(objectName, owner, objectType);
                if (source != null) {
                    details.put("sourceCode", source);
                }
            }

        } catch (Exception e) {
            log.warn("Error getting generic object details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }
    }

    /**
     * Check if object type can have source code
     */
    private boolean isSourceBasedObject(String objectType) {
        if (objectType == null) return false;
        String upperType = objectType.toUpperCase();
        return upperType.contains("PROCEDURE") ||
                upperType.contains("FUNCTION") ||
                upperType.contains("PACKAGE") ||
                upperType.contains("TRIGGER") ||
                upperType.contains("TYPE") ||
                upperType.contains("VIEW") ||
                upperType.contains("JAVA");
    }

    /**
     * Copy common details from source map to destination
     */
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
        if (source.containsKey("last_ddl_time")) {
            dest.put("lastModified", source.get("last_ddl_time"));
        }
        if (source.containsKey("lastModified")) {
            dest.put("lastModified", source.get("lastModified"));
        }
        if (source.containsKey("temporary")) {
            dest.put("temporary", source.get("temporary"));
        }
        if (source.containsKey("generated")) {
            dest.put("generated", source.get("generated"));
        }
        if (source.containsKey("source") && !dest.containsKey("sourceCode")) {
            dest.put("sourceCode", source.get("source"));
        }
        if (source.containsKey("sourceCode") && !dest.containsKey("sourceCode")) {
            dest.put("sourceCode", source.get("sourceCode"));
        }
    }

    /**
     * Transform columns to standard format
     */
    private List<Map<String, Object>> transformColumns(List<Map<String, Object>> columns) {
        if (columns == null) return new ArrayList<>();

        return columns.stream().map(col -> {
            Map<String, Object> transformed = new HashMap<>();
            transformed.put("name", col.get("column_name"));
            transformed.put("dataType", col.get("data_type"));
            transformed.put("nullable", col.get("nullable"));
            transformed.put("position", col.get("column_id"));
            transformed.put("dataLength", col.get("data_length"));
            transformed.put("dataPrecision", col.get("data_precision"));
            transformed.put("dataScale", col.get("data_scale"));
            transformed.put("defaultValue", col.get("data_default"));
            return transformed;
        }).collect(Collectors.toList());
    }

    /**
     * Find primary key from constraints
     */
    private Map<String, Object> findPrimaryKey(List<Map<String, Object>> constraints) {
        if (constraints == null) return null;

        return constraints.stream()
                .filter(c -> "P".equals(c.get("constraint_type")))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get table comment
     */
    private String getTableComment(String owner, String tableName) {
        try {
            String sql = "SELECT comments FROM all_tab_comments " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";
            return oracleJdbcTemplate.queryForObject(sql, String.class, owner, tableName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get materialized view columns
     */
    private List<Map<String, Object>> getMaterializedViewColumns(String owner, String mvName) {
        try {
            String sql = "SELECT column_id, column_name, data_type, nullable, data_length, " +
                    "data_precision, data_scale FROM all_tab_columns " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?) " +
                    "ORDER BY column_id";
            return oracleJdbcTemplate.queryForList(sql, owner, mvName);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Get package source (spec + body)
     */
    private String getPackageSource(String owner, String packageName) {
        try {
            StringBuilder fullSource = new StringBuilder();

            // Get package spec
            String specSql = "SELECT text FROM all_source " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(name) = UPPER(?) " +
                    "AND UPPER(type) = 'PACKAGE' ORDER BY line";

            List<String> specLines = oracleJdbcTemplate.queryForList(specSql, String.class, owner, packageName);
            if (!specLines.isEmpty()) {
                fullSource.append("-- PACKAGE SPECIFICATION\n");
                specLines.forEach(fullSource::append);
                fullSource.append("\n/\n\n");
            }

            // Get package body
            String bodySql = "SELECT text FROM all_source " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(name) = UPPER(?) " +
                    "AND UPPER(type) = 'PACKAGE BODY' ORDER BY line";

            List<String> bodyLines = oracleJdbcTemplate.queryForList(bodySql, String.class, owner, packageName);
            if (!bodyLines.isEmpty()) {
                fullSource.append("-- PACKAGE BODY\n");
                bodyLines.forEach(fullSource::append);
                fullSource.append("\n/");
            }

            return fullSource.length() > 0 ? fullSource.toString() : null;
        } catch (Exception e) {
            log.warn("Error getting package source: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get sequence DDL
     */
    private String getSequenceDDL(String owner, String sequenceName) {
        try {
            String sql = "SELECT 'CREATE SEQUENCE ' || " +
                    "CASE WHEN UPPER(sequence_owner) != UPPER(USER) THEN sequence_owner || '.' END || " +
                    "sequence_name || " +
                    "' MINVALUE ' || min_value || " +
                    "' MAXVALUE ' || max_value || " +
                    "' INCREMENT BY ' || increment_by || " +
                    "CASE WHEN cycle_flag = 'Y' THEN ' CYCLE' ELSE ' NOCYCLE' END || " +
                    "CASE WHEN order_flag = 'Y' THEN ' ORDER' ELSE ' NOORDER' END || " +
                    "CASE WHEN cache_size > 0 THEN ' CACHE ' || cache_size ELSE ' NOCACHE' END as ddl " +
                    "FROM all_sequences WHERE UPPER(sequence_owner) = UPPER(?) AND UPPER(sequence_name) = UPPER(?)";

            return oracleJdbcTemplate.queryForObject(sql, String.class, owner, sequenceName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse procedure parameters from source
     */
    private List<Map<String, Object>> parseProcedureParametersFromSource(String procedureName, String owner) {
        List<Map<String, Object>> params = new ArrayList<>();

        try {
            String source = getSourceFromAllSource(procedureName, owner, "PROCEDURE");
            if (source == null || source.isEmpty()) {
                return params;
            }

            // Remove comments
            source = removeComments(source);

            // Find procedure signature
            String pattern = "PROCEDURE\\s+" + procedureName + "\\s*\\(([\\s\\S]*?)\\)\\s*(?:IS|AS)";
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(source);

            if (m.find()) {
                String paramsSection = m.group(1).trim();
                params = parseParameterSection(paramsSection);
            }
        } catch (Exception e) {
            log.warn("Error parsing procedure parameters: {}", e.getMessage());
        }

        return params;
    }

    /**
     * Parse function parameters from source
     */
    private List<Map<String, Object>> parseFunctionParametersFromSource(String functionName, String owner) {
        List<Map<String, Object>> params = new ArrayList<>();

        try {
            String source = getSourceFromAllSource(functionName, owner, "FUNCTION");
            if (source == null || source.isEmpty()) {
                return params;
            }

            // Remove comments
            source = removeComments(source);

            // Find function signature
            String pattern = "FUNCTION\\s+" + functionName + "\\s*\\(([\\s\\S]*?)\\)\\s*RETURN";
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(source);

            if (m.find()) {
                String paramsSection = m.group(1).trim();
                params = parseParameterSection(paramsSection);
            }
        } catch (Exception e) {
            log.warn("Error parsing function parameters: {}", e.getMessage());
        }

        return params;
    }

    /**
     * Parse parameter section into parameter list
     */
    private List<Map<String, Object>> parseParameterSection(String paramsSection) {
        List<Map<String, Object>> params = new ArrayList<>();

        if (paramsSection == null || paramsSection.isEmpty()) {
            return params;
        }

        List<String> paramDefs = splitParameters(paramsSection);
        int position = 1;

        for (String paramDef : paramDefs) {
            Map<String, Object> param = parseSingleParameter(paramDef, position++);
            if (param != null && !param.isEmpty()) {
                params.add(param);
            }
        }

        return params;
    }

    /**
     * Split parameters by comma, respecting parentheses
     */
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

    /**
     * Parse a single parameter definition
     */
    private Map<String, Object> parseSingleParameter(String paramDef, int position) {
        Map<String, Object> param = new HashMap<>();

        paramDef = paramDef.replaceAll("\\s+", " ").trim();

        int commentIdx = paramDef.indexOf("--");
        if (commentIdx > 0) {
            paramDef = paramDef.substring(0, commentIdx).trim();
        }

        if (paramDef.isEmpty()) {
            return null;
        }

        // Pattern with direction
        Pattern pattern1 = Pattern.compile(
                "^(\\w+)\\s+(IN\\s+OUT|IN|OUT)\\s+(.+?)(?:\\s+(?:DEFAULT|:=)\\s+(.+))?$",
                Pattern.CASE_INSENSITIVE
        );

        // Pattern without direction
        Pattern pattern2 = Pattern.compile(
                "^(\\w+)\\s+([^\\s]+(?:\\s*\\()?[^)]*\\)?)(?:\\s+(?:DEFAULT|:=)\\s+(.+))?$",
                Pattern.CASE_INSENSITIVE
        );

        Matcher m = pattern1.matcher(paramDef);
        if (m.find()) {
            param.put("argument_name", m.group(1));
            param.put("in_out", m.group(2).toUpperCase().replace(" ", "_"));
            param.put("data_type", m.group(3).replaceAll("[,;]$", "").trim());
            param.put("position", position);
            param.put("defaulted", m.group(4) != null ? "Y" : "N");
        } else {
            m = pattern2.matcher(paramDef);
            if (m.find()) {
                param.put("argument_name", m.group(1));

                String inOut = "IN";
                if (paramDef.toUpperCase().contains(" OUT")) {
                    inOut = paramDef.toUpperCase().contains("IN OUT") ? "IN_OUT" : "OUT";
                }
                param.put("in_out", inOut);
                param.put("data_type", m.group(2).replaceAll("[,;]$", "").trim());
                param.put("position", position);
                param.put("defaulted", m.groupCount() >= 3 && m.group(3) != null ? "Y" : "N");
            }
        }

        return param.isEmpty() ? null : param;
    }

    /**
     * Get source from ALL_SOURCE
     */
    private String getSourceFromAllSource(String objectName, String owner, String type) {
        try {
            String sql = "SELECT text FROM all_source " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(name) = UPPER(?) " +
                    "AND UPPER(type) = UPPER(?) ORDER BY line";

            List<String> lines = oracleJdbcTemplate.queryForList(sql, String.class, owner, objectName, type);

            if (lines.isEmpty()) {
                sql = "SELECT text FROM all_source WHERE UPPER(owner) = UPPER(?) AND UPPER(name) = UPPER(?) ORDER BY line";
                lines = oracleJdbcTemplate.queryForList(sql, String.class, owner, objectName);
            }

            return lines.isEmpty() ? null : String.join("", lines);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Remove comments from source code
     */
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





    /**
     * Extract length from data type (e.g., VARCHAR2(100) -> 100)
     */
    private Integer extractLength(String dataType) {
        if (dataType == null) return null;
        Pattern pattern = Pattern.compile("\\((\\d+)\\)");
        Matcher matcher = pattern.matcher(dataType);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    /**
     * Extract precision from data type (e.g., NUMBER(10,2) -> 10)
     */
    private Integer extractPrecision(String dataType) {
        if (dataType == null) return null;
        Pattern pattern = Pattern.compile("\\((\\d+),\\s*\\d+\\)");
        Matcher matcher = pattern.matcher(dataType);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    /**
     * Extract scale from data type (e.g., NUMBER(10,2) -> 2)
     */
    private Integer extractScale(String dataType) {
        if (dataType == null) return null;
        Pattern pattern = Pattern.compile("\\(\\d+,\\s*(\\d+)\\)");
        Matcher matcher = pattern.matcher(dataType);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
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