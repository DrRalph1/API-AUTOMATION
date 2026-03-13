package com.usg.apiAutomation.helpers.apiEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiAnalyticsDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiDetailsResponseDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.GeneratedApiResponseDTO;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.ApiExecutionLogEntity;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
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
                    Map<String, Object> procDetails = oracleSchemaService.getProcedureDetails(
                            UUID.randomUUID().toString(),
                            null,
                            "system",
                            targetName
                    );

                    Map<String, Object> procData = (Map<String, Object>) procDetails.get("data");
                    if (procData != null) {
                        details.put("parameters", procData.get("parameters"));
                        details.put("parameterCount", procData.get("parameterCount"));
                    }
                    break;

                case "FUNCTION":
                    Map<String, Object> funcDetails = oracleSchemaService.getFunctionDetails(
                            UUID.randomUUID().toString(),
                            null,
                            "system",
                            targetName
                    );

                    Map<String, Object> funcData = (Map<String, Object>) funcDetails.get("data");
                    if (funcData != null) {
                        details.put("parameters", funcData.get("parameters"));
                        details.put("returnType", funcData.get("returnType"));
                        details.put("parameterCount", funcData.get("parameterCount"));
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