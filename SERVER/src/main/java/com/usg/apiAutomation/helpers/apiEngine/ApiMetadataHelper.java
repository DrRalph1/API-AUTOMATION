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
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ApiMetadataHelper {

    private final OracleSchemaRepository oracleSchemaRepository;

    public ApiMetadataHelper(OracleSchemaRepository oracleSchemaRepository) {
        this.oracleSchemaRepository = oracleSchemaRepository;
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

            switch (targetType.toUpperCase()) {
                case "TABLE":
                case "VIEW":
                    Map<String, Object> tableDetails = oracleSchemaService.getTableDetailsForFrontend(
                            UUID.randomUUID().toString(),
                            null,
                            "system",
                            targetName
                    );

                    Map<String, Object> tableData = (Map<String, Object>) tableDetails.get("data");
                    if (tableData != null) {
                        details.put("columns", tableData.get("columns"));
                        details.put("primaryKey", tableData.get("primaryKey"));
                        details.put("rowCount", tableData.get("rowCount"));
                    }
                    break;

                case "PROCEDURE":
                    // Call repository directly
                    Map<String, Object> procDetails = oracleSchemaRepository.getProcedureDetails(targetName);

                    if (procDetails != null && !procDetails.isEmpty()) {
                        // Get parameters from the procedure details
                        List<Map<String, Object>> parameters = (List<Map<String, Object>>) procDetails.get("parameters");

                        if (parameters != null && !parameters.isEmpty()) {
                            // Count IN, OUT, and IN/OUT parameters
                            int inCount = 0;
                            int outCount = 0;
                            int inOutCount = 0;

                            for (Map<String, Object> param : parameters) {
                                String inOut = (String) param.get("in_out");
                                if (inOut == null) {
                                    inOut = (String) param.get("IN_OUT");
                                }

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

                            // Add source code if available
                            if (procDetails.containsKey("source")) {
                                details.put("sourceCode", procDetails.get("source"));
                            }

                            // Add procedure metadata
                            details.put("status", procDetails.get("status"));
                            details.put("created", procDetails.get("created"));
                            details.put("lastModified", procDetails.get("last_ddl_time"));

                            // Check if it's a package procedure
                            if (procDetails.containsKey("package_name")) {
                                details.put("packageName", procDetails.get("package_name"));
                                details.put("isPackageProcedure", true);
                            }
                        }
                    }
                    break;

                case "FUNCTION":
                    // Call repository directly
                    Map<String, Object> funcDetails = oracleSchemaRepository.getFunctionDetails(targetName);

                    if (funcDetails != null && !funcDetails.isEmpty()) {
                        // Get parameters from the function details
                        List<Map<String, Object>> parameters = (List<Map<String, Object>>) funcDetails.get("parameters");

                        if (parameters != null && !parameters.isEmpty()) {
                            // Count IN, OUT, and IN/OUT parameters
                            int inCount = 0;
                            int outCount = 0;
                            int inOutCount = 0;

                            for (Map<String, Object> param : parameters) {
                                String inOut = (String) param.get("in_out");
                                if (inOut == null) {
                                    inOut = (String) param.get("IN_OUT");
                                }

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

                            // Add return type
                            details.put("returnType", funcDetails.get("returnType"));

                            // Add source code if available
                            if (funcDetails.containsKey("source")) {
                                details.put("sourceCode", funcDetails.get("source"));
                            }

                            // Add function metadata
                            details.put("status", funcDetails.get("status"));
                            details.put("created", funcDetails.get("created"));
                            details.put("lastModified", funcDetails.get("last_ddl_time"));
                        }
                    }
                    break;

                case "PACKAGE":
                    // Call repository directly
                    Map<String, Object> pkgDetails = oracleSchemaRepository.getPackageDetails(targetName);

                    if (pkgDetails != null && !pkgDetails.isEmpty()) {
                        details.put("procedures", pkgDetails.get("procedures"));
                        details.put("functions", pkgDetails.get("functions"));
                        details.put("procedureCount", pkgDetails.get("procedureCount"));
                        details.put("functionCount", pkgDetails.get("functionCount"));
                        details.put("specification", pkgDetails.get("specification"));
                        details.put("body", pkgDetails.get("body"));

                        if (pkgDetails.containsKey("specSource")) {
                            details.put("sourceCode", pkgDetails.get("specSource"));
                        }
                    }
                    break;
            }

        } catch (Exception e) {
            log.warn("Could not get source object details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
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