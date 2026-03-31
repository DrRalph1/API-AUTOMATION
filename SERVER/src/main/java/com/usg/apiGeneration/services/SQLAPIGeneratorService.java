package com.usg.apiGeneration.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiGeneration.dtos.apiGenerationEngine.*;
import com.usg.apiGeneration.entities.postgres.apiGenerationEngine.*;
import com.usg.apiGeneration.helpers.apiEngine.ApiConversionHelper;
import com.usg.apiGeneration.repositories.apiGenerationEngine.GeneratedAPIRepository;
import com.usg.apiGeneration.utils.apiEngine.SQLParserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SQLAPIGeneratorService {

    private final SQLParserUtil sqlParser;
    private final GeneratedAPIRepository generatedAPIRepository;
    private final ApiConversionHelper conversionHelper;
    private final ObjectMapper objectMapper;
    private final AutomationEngineService automationService;

    /**
     * Generate API from SQL statement
     */
    @Transactional
    public GeneratedApiResponseDTO generateFromSQL(String requestId, String performedBy,
                                                   GenerateSQLApiRequestDTO request) {
        long startTime = System.currentTimeMillis();

        try {
            String databaseType = request.getDatabaseType();
            if (databaseType == null || databaseType.isEmpty()) {
                databaseType = "oracle";
            }

            // Parse SQL to extract structure
            SQLParserUtil.ParsedSQL parsedSQL = sqlParser.parseSQL(request.getSql(), databaseType);

            // Validate the SQL
            validateSQL(parsedSQL, request);

            // Create source object DTO from parsed SQL
            ApiSourceObjectDTO sourceObjectDTO = createSourceObjectFromSQL(parsedSQL, request);

            // Create API parameters from extracted SQL parameters
            List<ApiParameterDTO> parameters = convertSQLParametersToApiParameters(parsedSQL.getParameters());

            // Build the API generation request
            GenerateApiRequestDTO apiRequest = buildApiRequestFromSQL(request, parsedSQL, parameters);

            // Use existing generation logic
            GeneratedApiResponseDTO response = automationService.generateApi(requestId, performedBy, apiRequest);

            // Update with SQL-specific fields
            GeneratedApiEntity api = generatedAPIRepository.findById(response.getId())
                    .orElseThrow(() -> new RuntimeException("API not found"));
            api.setSourceSql(request.getSql());
            api.setSqlOperationType(parsedSQL.getOperationType());
            api.setSqlParsedStructure(objectMapper.writeValueAsString(parsedSQL));
            generatedAPIRepository.save(api);

            log.info("Successfully generated SQL-based API: {} in {}ms",
                    api.getApiCode(), System.currentTimeMillis() - startTime);

            return conversionHelper.mapToResponse(api);

        } catch (Exception e) {
            log.error("Error generating SQL-based API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate API from SQL: " + e.getMessage(), e);
        }
    }

    private void validateSQL(SQLParserUtil.ParsedSQL parsedSQL, GenerateSQLApiRequestDTO request) {
        // Validate operation type
        String operationType = parsedSQL.getOperationType();
        if (!Arrays.asList("SELECT", "INSERT", "UPDATE", "DELETE").contains(operationType)) {
            throw new IllegalArgumentException("Unsupported SQL operation: " + operationType);
        }

        // For SELECT statements, ensure we can access the table
        if ("SELECT".equals(operationType) && parsedSQL.getObjectName() == null) {
            throw new IllegalArgumentException("SELECT statement must specify a FROM table");
        }

        // For DML operations, validate parameters
        if (parsedSQL.getParameters().isEmpty() &&
                ("UPDATE".equals(operationType) || "DELETE".equals(operationType))) {
            throw new IllegalArgumentException("UPDATE/DELETE statements must have at least one WHERE condition parameter");
        }
    }

    private ApiSourceObjectDTO createSourceObjectFromSQL(SQLParserUtil.ParsedSQL parsedSQL,
                                                         GenerateSQLApiRequestDTO request) {
        ApiSourceObjectDTO sourceObject = new ApiSourceObjectDTO();

        // Set the basic object info using existing fields
        sourceObject.setObjectType(parsedSQL.getOperationType().toUpperCase());
        sourceObject.setObjectName(parsedSQL.getObjectName());
        sourceObject.setOwner(parsedSQL.getSchema() != null ? parsedSQL.getSchema() : request.getDefaultSchema());
        sourceObject.setDatabaseType(request.getDatabaseType());
        sourceObject.setOperation(parsedSQL.getOperationType().toUpperCase());

        // Use existing fields for SQL-specific metadata
        sourceObject.setSchemaName(parsedSQL.getSchema());

        // Use comments field to store SQL source info
        sourceObject.setComments(String.format("SQL Source: %s | Parameterized: %s",
                request.getSql().length() > 100 ? request.getSql().substring(0, 100) + "..." : request.getSql(),
                sqlParser.toParameterizedSQL(request.getSql(), parsedSQL.getParameters())));

        // If you have column information from parsed SQL, set column count
        if (parsedSQL.getSelectedColumns() != null) {
            sourceObject.setColumnCount(parsedSQL.getSelectedColumns().size());
        }

        if (parsedSQL.getParameters() != null) {
            sourceObject.setParameterCount(parsedSQL.getParameters().size());
        }

        // Set status based on SQL validation
        sourceObject.setStatus("ACTIVE");

        // Configure based on operation type
        if ("SELECT".equals(parsedSQL.getOperationType())) {
            sourceObject.setEnablePagination(true);
            sourceObject.setDefaultPageSize(20);
        }

        // For UPDATE/DELETE operations, use primary key as path param
        if ("UPDATE".equals(parsedSQL.getOperationType()) || "DELETE".equals(parsedSQL.getOperationType())) {
            sourceObject.setUsePrimaryKeyAsPathParam(true);
        }

        return sourceObject;
    }


    private List<ApiParameterDTO> convertSQLParametersToApiParameters(List<SQLParserUtil.SQLParameter> sqlParams) {
        List<ApiParameterDTO> apiParams = new ArrayList<>();

        for (SQLParserUtil.SQLParameter sqlParam : sqlParams) {
            ApiParameterDTO param = new ApiParameterDTO();
            param.setKey(sqlParam.getName());
            param.setApiType(sqlParam.getDataType());
            param.setParameterType(sqlParam.getParameterType());
            param.setParameterLocation(sqlParam.getParameterLocation());
            param.setRequired(sqlParam.isRequired());
            param.setDescription(sqlParam.getDescription() != null ?
                    sqlParam.getDescription() : "Parameter from SQL query");
            param.setExample(sqlParam.getExample() != null ?
                    sqlParam.getExample() : "example_" + sqlParam.getName());

            apiParams.add(param);
        }

        return apiParams;
    }

    private GenerateApiRequestDTO buildApiRequestFromSQL(GenerateSQLApiRequestDTO request,
                                                         SQLParserUtil.ParsedSQL parsedSQL,
                                                         List<ApiParameterDTO> parameters) {
        GenerateApiRequestDTO apiRequest = new GenerateApiRequestDTO();

        // Set API metadata
        apiRequest.setApiCode(request.getApiCode());
        apiRequest.setApiName(request.getApiName());
        apiRequest.setDescription(request.getDescription());
        apiRequest.setVersion(request.getVersion());
        apiRequest.setDatabaseType(request.getDatabaseType());

        // Set HTTP method based on operation type
        String httpMethod = mapSQLOperationToHttpMethod(parsedSQL.getOperationType());
        apiRequest.setHttpMethod(httpMethod);

        // Set endpoint path
        String endpointPath = buildEndpointPathFromSQL(parsedSQL, parameters);
        apiRequest.setEndpointPath(endpointPath);

        // Set parameters
        apiRequest.setParameters(parameters);

        // Set source object
        Map<String, Object> sourceObject = new HashMap<>();
        sourceObject.put("objectType", parsedSQL.getOperationType().toUpperCase());
        sourceObject.put("objectName", parsedSQL.getObjectName());
        sourceObject.put("owner", parsedSQL.getSchema());
        sourceObject.put("sourceType", "SQL");
        sourceObject.put("sql", request.getSql());
        apiRequest.setSourceObject(sourceObject);

        // Set collection info
        CollectionInfoDTO collectionInfo = new CollectionInfoDTO();
        collectionInfo.setCollectionId(request.getCollectionId());
        collectionInfo.setCollectionName(request.getCollectionName());
        collectionInfo.setFolderId(request.getFolderId());
        collectionInfo.setFolderName(request.getFolderName());
        apiRequest.setCollectionInfo(collectionInfo);

        // Set response mapping for SELECT statements
        if ("SELECT".equals(parsedSQL.getOperationType()) && !parsedSQL.getSelectedColumns().isEmpty()) {
            List<ApiResponseMappingDTO> responseMappings = new ArrayList<>();
            for (String column : parsedSQL.getSelectedColumns()) {
                ApiResponseMappingDTO mapping = new ApiResponseMappingDTO();
                mapping.setApiField(column);
                mapping.setDbColumn(column);
                mapping.setIncludeInResponse(true);
                responseMappings.add(mapping);
            }
            apiRequest.setResponseMappings(responseMappings);
        }

        return apiRequest;
    }

    private String mapSQLOperationToHttpMethod(String sqlOperation) {
        switch (sqlOperation.toUpperCase()) {
            case "SELECT":
                return "GET";
            case "INSERT":
                return "POST";
            case "UPDATE":
                return "PUT";
            case "DELETE":
                return "DELETE";
            default:
                return "POST";
        }
    }

    private String buildEndpointPathFromSQL(SQLParserUtil.ParsedSQL parsedSQL,
                                            List<ApiParameterDTO> parameters) {
        String basePath = "/api/v1/sql";

        // Use object name in path
        if (parsedSQL.getObjectName() != null) {
            basePath = "/api/v1/" + parsedSQL.getObjectName().toLowerCase();
        }

        // Add parameters as path variables for GET/DELETE
        if (("SELECT".equals(parsedSQL.getOperationType()) ||
                "DELETE".equals(parsedSQL.getOperationType())) &&
                !parameters.isEmpty()) {

            // Find path parameters (IDs) and add them to the path
            Optional<ApiParameterDTO> idParam = parameters.stream()
                    .filter(p -> p.getKey().toLowerCase().contains("id"))
                    .findFirst();

            if (idParam.isPresent()) {
                basePath = basePath + "/{" + idParam.get().getKey() + "}";
            }
        }

        return basePath;
    }

}