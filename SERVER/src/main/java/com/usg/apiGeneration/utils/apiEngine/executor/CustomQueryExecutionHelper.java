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
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomQueryExecutionHelper {

    private final CustomQueryParserUtil queryParserUtil;

    // Pattern to remove SQL single-line comments (-- comment)
    private static final Pattern SQL_COMMENT_PATTERN = Pattern.compile("--[^\n\r]*");
    // Pattern to remove Oracle hints (/*+ ... */) - preserve as they might be needed
    private static final Pattern ORACLE_HINT_PATTERN = Pattern.compile("/\\*\\+.*?\\*/");
    // Pattern to remove multi-line comments (/* ... */) but preserve hints
    private static final Pattern MULTI_LINE_COMMENT_PATTERN = Pattern.compile("/\\*[^*].*?\\*/", Pattern.DOTALL);

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
        log.debug("Original Query: {}", selectStatement);

        try {
            // Clean the SQL query before execution
            String cleanedQuery = cleanSqlQuery(selectStatement);
            log.debug("Cleaned Query: {}", cleanedQuery);

            // Validate the SELECT statement
            queryParserUtil.validateSelectStatement(cleanedQuery);

            // Create named parameter template for easier parameter handling
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

            // Prepare parameters from request
            MapSqlParameterSource parameterSource = buildParameterSource(executeRequest, configuredParamDTOs);

            // Execute the query
            List<Map<String, Object>> results = namedTemplate.queryForList(cleanedQuery, parameterSource);

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
     * Clean SQL query by removing comments and trailing semicolons
     *
     * @param sql The original SQL query
     * @return Cleaned SQL query ready for execution
     */
    private String cleanSqlQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        String cleaned = sql;

        // First, preserve Oracle hints (/*+ ... */) by temporarily replacing them
        // This is optional - if you want to keep Oracle hints
        java.util.Map<String, String> hintPlaceholders = new java.util.HashMap<>();
        java.util.regex.Matcher hintMatcher = ORACLE_HINT_PATTERN.matcher(cleaned);
        int hintCounter = 0;
        while (hintMatcher.find()) {
            String hint = hintMatcher.group();
            String placeholder = "__HINT_" + (hintCounter++) + "__";
            hintPlaceholders.put(placeholder, hint);
            cleaned = cleaned.replace(hint, placeholder);
        }

        // Remove single-line comments (-- comment)
        cleaned = SQL_COMMENT_PATTERN.matcher(cleaned).replaceAll("");

        // Remove multi-line comments (/* ... */)
        cleaned = MULTI_LINE_COMMENT_PATTERN.matcher(cleaned).replaceAll("");

        // Restore Oracle hints
        for (java.util.Map.Entry<String, String> entry : hintPlaceholders.entrySet()) {
            cleaned = cleaned.replace(entry.getKey(), entry.getValue());
        }

        // Remove trailing semicolon if present
        cleaned = cleaned.trim();
        if (cleaned.endsWith(";")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
        }

        // Clean up any extra whitespace and newlines that might cause issues
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        return cleaned;
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