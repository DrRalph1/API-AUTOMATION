package com.usg.apiGeneration.utils.apiEngine.executor;

import com.usg.apiGeneration.dtos.apiGenerationEngine.ApiParameterDTO;
import com.usg.apiGeneration.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiGeneration.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import com.usg.apiGeneration.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.apiGeneration.utils.apiEngine.generator.CustomQueryParserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomQueryExecutionHelper {

    private final CustomQueryParserUtil queryParserUtil;

    /**
     * Execute a custom SELECT statement
     * This is called ONLY when useCustomQuery is true
     */
    public Object executeCustomQuery(GeneratedApiEntity api,
                                     ApiSourceObjectDTO sourceObject,
                                     ExecuteApiRequestDTO executeRequest,
                                     List<ApiParameterDTO> configuredParamDTOs,
                                     JdbcTemplate jdbcTemplate) {

        String selectStatement = sourceObject.getCustomSelectStatement();

        if (selectStatement == null || selectStatement.trim().isEmpty()) {
            throw new RuntimeException("No SELECT statement provided for custom query API");
        }

        log.info("Executing custom SELECT query for API: {}", api.getApiCode());
        log.debug("Query: {}", selectStatement);

        try {
            // Validate the SELECT statement
            queryParserUtil.validateSelectStatement(selectStatement);

            // Create named parameter template for easier parameter handling
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

            // Prepare parameters from request
            MapSqlParameterSource parameterSource = buildParameterSource(executeRequest, configuredParamDTOs);

            // Execute the query
            List<Map<String, Object>> results = namedTemplate.queryForList(selectStatement, parameterSource);

            log.info("Custom query executed successfully, returned {} rows", results.size());

            // Apply pagination if configured
            if (Boolean.TRUE.equals(sourceObject.getEnablePagination())) {
                return applyPagination(results, executeRequest, sourceObject.getDefaultPageSize());
            }

            return results;

        } catch (Exception e) {
            log.error("Error executing custom query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute custom SELECT statement: " + e.getMessage(), e);
        }
    }

    /**
     * Build parameter source from request
     */
    private MapSqlParameterSource buildParameterSource(ExecuteApiRequestDTO executeRequest,
                                                       List<ApiParameterDTO> configuredParamDTOs) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();

        // Add query parameters
        if (executeRequest.getQueryParams() != null) {
            executeRequest.getQueryParams().forEach(parameterSource::addValue);
        }

        // Add path parameters
        if (executeRequest.getPathParams() != null) {
            executeRequest.getPathParams().forEach(parameterSource::addValue);
        }

        // Add body parameters if it's a map
        if (executeRequest.getBody() instanceof Map) {
            ((Map<String, Object>) executeRequest.getBody()).forEach(parameterSource::addValue);
        }

        // Add configured parameter default values if not provided
        if (configuredParamDTOs != null) {
            for (ApiParameterDTO param : configuredParamDTOs) {
                if (!parameterSource.hasValue(param.getKey()) && param.getDefaultValue() != null) {
                    parameterSource.addValue(param.getKey(), param.getDefaultValue());
                }
            }
        }

        return parameterSource;
    }

    /**
     * Apply pagination to results
     */
    private Map<String, Object> applyPagination(List<Map<String, Object>> results,
                                                ExecuteApiRequestDTO executeRequest,
                                                Integer defaultPageSize) {
        int page = 0;
        int size = defaultPageSize != null ? defaultPageSize : 20;

        if (executeRequest.getQueryParams() != null) {
            if (executeRequest.getQueryParams().containsKey("page")) {
                page = Integer.parseInt(executeRequest.getQueryParams().get("page").toString());
            }
            if (executeRequest.getQueryParams().containsKey("size")) {
                size = Integer.parseInt(executeRequest.getQueryParams().get("size").toString());
            }
        }

        int start = page * size;
        int end = Math.min(start + size, results.size());

        List<Map<String, Object>> pagedResults = results.subList(start, end);

        return Map.of(
                "content", pagedResults,
                "page", page,
                "size", size,
                "totalElements", results.size(),
                "totalPages", (int) Math.ceil((double) results.size() / size)
        );
    }
}