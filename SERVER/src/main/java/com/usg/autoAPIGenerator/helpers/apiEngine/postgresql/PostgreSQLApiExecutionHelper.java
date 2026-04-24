package com.usg.autoAPIGenerator.helpers.apiEngine.postgresql;

import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.*;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.*;
import com.usg.autoAPIGenerator.helpers.BaseApiExecutionHelper;
import com.usg.autoAPIGenerator.helpers.apiEngine.ApiConversionHelper;
import com.usg.autoAPIGenerator.helpers.apiEngine.ApiResponseHelper;
import com.usg.autoAPIGenerator.utils.LoggerUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.DatabaseParameterGeneratorUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.executor.CustomQueryExecutionHelper;
import com.usg.autoAPIGenerator.utils.apiEngine.executor.postgresql.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component
public class PostgreSQLApiExecutionHelper extends BaseApiExecutionHelper {

    private final PostgreSQLTableExecutorUtil postgreSQLTableExecutorUtil;
    private final PostgreSQLViewExecutorUtil postgreSQLViewExecutorUtil;
    private final PostgreSQLProcedureExecutorUtil postgreSQLProcedureExecutorUtil;
    private final PostgreSQLFunctionExecutorUtil postgreSQLFunctionExecutorUtil;
    private final CustomQueryExecutionHelper customQueryExecutionHelper;

    // SOAP Action to SQL operation mapping
    private static final Map<String, String> SOAP_ACTION_TO_SQL_OPERATION = Map.ofEntries(
            Map.entry("SELECT", "SELECT"),
            Map.entry("SELECT_ONE", "SELECT"),
            Map.entry("SEARCH", "SELECT"),
            Map.entry("COUNT", "SELECT"),
            Map.entry("AGGREGATE", "SELECT"),
            Map.entry("EXPORT", "SELECT"),
            Map.entry("PAGINATE", "SELECT"),
            Map.entry("GET", "SELECT"),
            Map.entry("QUERY", "SELECT"),
            Map.entry("ANALYZE", "SELECT"),
            Map.entry("EXPLAIN", "SELECT"),
            Map.entry("EXISTS", "SELECT"),

            Map.entry("INSERT", "INSERT"),
            Map.entry("CREATE", "INSERT"),
            Map.entry("BULK_INSERT", "INSERT"),
            Map.entry("ADD", "INSERT"),

            Map.entry("UPDATE", "UPDATE"),
            Map.entry("MODIFY", "UPDATE"),
            Map.entry("UPSERT", "UPDATE"),
            Map.entry("BULK_UPDATE", "UPDATE"),
            Map.entry("EDIT", "UPDATE"),
            Map.entry("CHANGE", "UPDATE"),

            Map.entry("DELETE", "DELETE"),
            Map.entry("REMOVE", "DELETE"),
            Map.entry("PURGE", "DELETE"),

            Map.entry("EXECUTE", "CALL"),
            Map.entry("VALIDATE", "CALL"),
            Map.entry("PROCESS", "CALL"),
            Map.entry("RUN", "CALL"),
            Map.entry("EXECUTE_PROCEDURE", "CALL"),
            Map.entry("EXECUTE_FUNCTION", "CALL")
    );

    // GraphQL Operation Type to SQL operation mapping
    private static final Map<String, String> GRAPHQL_OP_TYPE_TO_SQL = Map.ofEntries(
            Map.entry("query", "SELECT"),
            Map.entry("mutation", "CALL"),
            Map.entry("subscription", "SELECT")
    );

    @Autowired
    @Qualifier("postgresqlJdbcTemplate")
    private JdbcTemplate postgresqlJdbcTemplate;

    public PostgreSQLApiExecutionHelper(
            ApiResponseHelper responseHelper,
            LoggerUtil loggerUtil,
            ApiConversionHelper conversionHelper,
            org.springframework.transaction.support.TransactionTemplate transactionTemplate,
            PostgreSQLTableExecutorUtil postgreSQLTableExecutorUtil,
            PostgreSQLViewExecutorUtil postgreSQLViewExecutorUtil,
            PostgreSQLProcedureExecutorUtil postgreSQLProcedureExecutorUtil,
            PostgreSQLFunctionExecutorUtil postgreSQLFunctionExecutorUtil,
            CustomQueryExecutionHelper customQueryExecutionHelper,
            @Qualifier("postgresqlJdbcTemplate") JdbcTemplate postgresqlJdbcTemplate) {
        super(responseHelper, loggerUtil, conversionHelper, transactionTemplate);
        this.postgreSQLTableExecutorUtil = postgreSQLTableExecutorUtil;
        this.postgreSQLViewExecutorUtil = postgreSQLViewExecutorUtil;
        this.postgreSQLProcedureExecutorUtil = postgreSQLProcedureExecutorUtil;
        this.postgreSQLFunctionExecutorUtil = postgreSQLFunctionExecutorUtil;
        this.customQueryExecutionHelper = customQueryExecutionHelper;
        this.postgresqlJdbcTemplate = postgresqlJdbcTemplate;
    }

    @Override
    protected void setupApiRelationships(GeneratedApiEntity api,
                                         GenerateApiRequestDTO request,
                                         ApiSourceObjectDTO sourceObjectDTO,
                                         DatabaseParameterGeneratorUtil parameterGenerator,
                                         ApiConversionHelper conversionHelper) {

        // Ensure collections are initialized (if they're null, initialize them)
        if (api.getParameters() == null) {
            api.setParameters(new ArrayList<>());
        }
        if (api.getHeaders() == null) {
            api.setHeaders(new ArrayList<>());
        }
        if (api.getResponseMappings() == null) {
            api.setResponseMappings(new ArrayList<>());
        }

        // Create and set auth config (one-to-one - this is fine to set)
        if (request.getAuthConfig() != null) {
            ApiAuthConfigEntity authConfig = conversionHelper.createAuthConfigEntity(request.getAuthConfig());
            authConfig.setGeneratedApi(api);
            api.setAuthConfig(authConfig);
        }

        // Create and set request config (one-to-one - this is fine to set)
        if (request.getRequestBody() != null) {
            ApiRequestConfigEntity requestConfig = conversionHelper.createRequestConfigEntity(request.getRequestBody());
            requestConfig.setGeneratedApi(api);
            api.setRequestConfig(requestConfig);
        }

        // Create and set response config (one-to-one - this is fine to set)
        if (request.getResponseBody() != null) {
            ApiResponseConfigEntity responseConfig = conversionHelper.createResponseConfigEntity(request.getResponseBody());
            responseConfig.setGeneratedApi(api);
            api.setResponseConfig(responseConfig);
        }

        // Create and set schema config (one-to-one - this is fine to set)
        if (sourceObjectDTO != null) {
            ApiSchemaConfigEntity schemaConfig = conversionHelper.createSchemaConfigEntity(sourceObjectDTO);

            if (schemaConfig.getSchemaName() == null || schemaConfig.getSchemaName().isEmpty()) {
                schemaConfig.setSchemaName("public");
                log.info("Defaulting to 'public' schema for PostgreSQL table: {}", schemaConfig.getObjectName());
            }

            schemaConfig.setGeneratedApi(api);
            api.setSchemaConfig(schemaConfig);
        }

        // Create and set settings (one-to-one - this is fine to set)
        if (request.getSettings() != null) {
            ApiSettingsEntity settings = conversionHelper.createSettingsEntity(request.getSettings());
            settings.setGeneratedApi(api);
            api.setSettings(settings);
        }

        // ============ REPLACE parameters collection (CLEAR then ADD) ============
        api.getParameters().clear();

        List<ApiParameterEntity> parameters = parameterGenerator.generateParameters(
                sourceObjectDTO, request.getParameters(), api.getId());
        for (ApiParameterEntity param : parameters) {
            param.setGeneratedApi(api);
            api.getParameters().add(param);
        }
        log.info("Recreated {} parameters for API: {}", parameters.size(), api.getId());

        // ============ REPLACE headers collection (CLEAR then ADD) ============
        api.getHeaders().clear();

        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            List<ApiHeaderEntity> headers = conversionHelper.createHeaderEntities(request.getHeaders(), api);
            for (ApiHeaderEntity header : headers) {
                header.setGeneratedApi(api);
                api.getHeaders().add(header);
            }
            log.info("Recreated {} headers for API: {}", headers.size(), api.getId());
        }

        // ============ REPLACE response mappings collection (CLEAR then ADD) ============
        api.getResponseMappings().clear();

        if (request.getResponseMappings() != null && !request.getResponseMappings().isEmpty()) {
            List<ApiResponseMappingEntity> responseMappings =
                    conversionHelper.createResponseMappingEntities(request.getResponseMappings(), api.getId());
            for (ApiResponseMappingEntity mapping : responseMappings) {
                mapping.setGeneratedApi(api);
                api.getResponseMappings().add(mapping);
            }
            log.info("Recreated {} response mappings for API: {}", responseMappings.size(), api.getId());
        }

        log.debug("Setup relationships for PostgreSQL API: {}", api.getId());
    }

    @Override
    public Object executeAgainstDatabase(GeneratedApiEntity api,
                                         ApiSourceObjectDTO sourceObject,
                                         ExecuteApiRequestDTO validatedRequest,
                                         List<ApiParameterDTO> configuredParamDTOs) throws SQLException {

        log.info("Executing PostgreSQL operation for API: {}", api.getId());

        // ============ CHECK FOR CUSTOM QUERY FIRST ============
        if (sourceObject != null && sourceObject.isCustomQuery()) {
            log.info("Executing custom SELECT query for PostgreSQL API: {}", api.getApiCode());
            return customQueryExecutionHelper.executeCustomQuery(
                    api, sourceObject, validatedRequest, configuredParamDTOs, postgresqlJdbcTemplate
            );
        }

        // ============ EXISTING LOGIC FOR REGULAR DATABASE OBJECTS ============
        ApiSchemaConfigEntity schemaConfig = api.getSchemaConfig();
        if (schemaConfig == null) {
            throw new RuntimeException("Schema configuration not found for PostgreSQL API");
        }

        String objectType = schemaConfig.getObjectType();
        String objectName = schemaConfig.getObjectName();
        String schema = schemaConfig.getSchemaName();

        // ============ DETERMINE OPERATION BASED ON PROTOCOL ============
        String operation = determineOperationFromProtocol(api, schemaConfig);
        log.info("Final operation for API {}: {}", api.getApiCode(), operation);

        // Create consolidated params
        Map<String, Object> params = new HashMap<>();
        if (validatedRequest.getPathParams() != null) params.putAll(validatedRequest.getPathParams());
        if (validatedRequest.getQueryParams() != null) params.putAll(validatedRequest.getQueryParams());
        if (validatedRequest.getBody() instanceof Map) {
            params.putAll((Map<String, Object>) validatedRequest.getBody());
        }

        switch (objectType.toUpperCase()) {
            case "TABLE":
                return executeTableOperation(objectName, schema, operation, params, api, configuredParamDTOs);
            case "VIEW":
            case "MATERIALIZED VIEW":
                return postgreSQLViewExecutorUtil.execute(api, sourceObject, objectName, schema,
                        validatedRequest, configuredParamDTOs);
            case "PROCEDURE":
                return postgreSQLProcedureExecutorUtil.execute(api, sourceObject, objectName, schema,
                        validatedRequest, configuredParamDTOs);
            case "FUNCTION":
                return postgreSQLFunctionExecutorUtil.execute(api, sourceObject, objectName, schema,
                        validatedRequest, configuredParamDTOs);
            default:
                throw new RuntimeException("Unsupported PostgreSQL object type: " + objectType);
        }
    }

    /**
     * Determine the SQL operation based on the API's protocol:
     * - SOAP: Uses SOAP Action mapping
     * - GraphQL: Uses GraphQL Operation Type mapping
     * - REST: Uses the schema config operation (SELECT, INSERT, UPDATE, DELETE)
     */
    private String determineOperationFromProtocol(GeneratedApiEntity api, ApiSchemaConfigEntity schemaConfig) {
        String protocolType = api.getProtocolType();

        // ============ SOAP PROTOCOL ============
        if ("soap".equalsIgnoreCase(protocolType) && api.getSoapConfig() != null) {
            String soapAction = api.getSoapConfig().getSoapAction();
            log.info("SOAP API detected - using SOAP Action to determine operation: {}", soapAction);

            if (soapAction != null && !soapAction.trim().isEmpty()) {
                // Try exact match first
                String mappedOperation = SOAP_ACTION_TO_SQL_OPERATION.get(soapAction.toUpperCase());
                if (mappedOperation != null) {
                    log.info("SOAP Action '{}' mapped to SQL operation: {}", soapAction, mappedOperation);
                    return mappedOperation;
                }

                // Try prefix match
                for (Map.Entry<String, String> entry : SOAP_ACTION_TO_SQL_OPERATION.entrySet()) {
                    if (soapAction.toUpperCase().startsWith(entry.getKey())) {
                        log.info("SOAP Action '{}' matched prefix '{}' -> SQL operation: {}",
                                soapAction, entry.getKey(), entry.getValue());
                        return entry.getValue();
                    }
                }

                // Try contains match
                for (Map.Entry<String, String> entry : SOAP_ACTION_TO_SQL_OPERATION.entrySet()) {
                    if (soapAction.toUpperCase().contains(entry.getKey())) {
                        log.info("SOAP Action '{}' contains '{}' -> SQL operation: {}",
                                soapAction, entry.getKey(), entry.getValue());
                        return entry.getValue();
                    }
                }

                log.warn("SOAP Action '{}' not recognized, defaulting to SELECT", soapAction);
                return "SELECT";
            }

            log.warn("No SOAP Action defined, defaulting to SELECT");
            return "SELECT";
        }

        // ============ GRAPHQL PROTOCOL ============
        if ("graphql".equalsIgnoreCase(protocolType) && api.getGraphqlConfig() != null) {
            String operationType = api.getGraphqlConfig().getOperationType();
            String operationName = api.getGraphqlConfig().getOperationName();
            log.info("GraphQL API detected - using Operation Type to determine operation: {}", operationType);

            if (operationType != null && !operationType.trim().isEmpty()) {
                String mappedOperation = GRAPHQL_OP_TYPE_TO_SQL.get(operationType.toLowerCase());
                if (mappedOperation != null) {
                    // For mutation, we can refine based on operation name
                    if ("CALL".equals(mappedOperation) && operationName != null) {
                        String refined = refineFromGraphQLOperationName(operationName);
                        if (refined != null) {
                            log.info("GraphQL mutation '{}' refined from CALL to {} based on operation name",
                                    operationName, refined);
                            return refined;
                        }
                    }
                    log.info("GraphQL Operation Type '{}' mapped to SQL operation: {}", operationType, mappedOperation);
                    return mappedOperation;
                }

                log.warn("GraphQL Operation Type '{}' not recognized, defaulting to SELECT", operationType);
                return "SELECT";
            }

            log.warn("No GraphQL Operation Type defined, defaulting to SELECT");
            return "SELECT";
        }

        // ============ REST PROTOCOL (or default) ============
        String configuredOperation = schemaConfig.getOperation();
        if (configuredOperation == null || configuredOperation.isEmpty()) {
            configuredOperation = "SELECT";
        }
        log.info("REST API detected - using configured operation: {}", configuredOperation);
        return configuredOperation;
    }

    /**
     * Refine GraphQL mutation operation based on operation name patterns
     */
    private String refineFromGraphQLOperationName(String operationName) {
        if (operationName == null || operationName.trim().isEmpty()) {
            return null;
        }

        String lowerName = operationName.toLowerCase();

        if (lowerName.startsWith("create") || lowerName.startsWith("add") ||
                lowerName.startsWith("insert") || lowerName.startsWith("save") ||
                lowerName.startsWith("register")) {
            return "INSERT";
        }

        if (lowerName.startsWith("update") || lowerName.startsWith("modify") ||
                lowerName.startsWith("edit") || lowerName.startsWith("change") ||
                lowerName.startsWith("set") || lowerName.startsWith("upsert")) {
            return "UPDATE";
        }

        if (lowerName.startsWith("delete") || lowerName.startsWith("remove") ||
                lowerName.startsWith("purge") || lowerName.startsWith("destroy")) {
            return "DELETE";
        }

        if (lowerName.startsWith("execute") || lowerName.startsWith("run") ||
                lowerName.startsWith("process") || lowerName.startsWith("validate") ||
                lowerName.startsWith("calculate") || lowerName.startsWith("compute")) {
            return "CALL";
        }

        if (lowerName.contains("create") || lowerName.contains("add") || lowerName.contains("insert")) {
            return "INSERT";
        }

        if (lowerName.contains("update") || lowerName.contains("modify")) {
            return "UPDATE";
        }

        if (lowerName.contains("delete") || lowerName.contains("remove")) {
            return "DELETE";
        }

        return null;
    }

    private Object executeTableOperation(String tableName, String schema, String operation,
                                         Map<String, Object> params, GeneratedApiEntity api,
                                         List<ApiParameterDTO> configuredParamDTOs) {

        if (operation == null) {
            operation = "SELECT";
        }

        log.info("Executing {} operation on {}.{} with params: {}", operation, schema, tableName, params.keySet());

        switch (operation.toUpperCase()) {
            case "SELECT":
                return postgreSQLTableExecutorUtil.executeSelect(tableName, schema, params, api, configuredParamDTOs);
            case "INSERT":
                return postgreSQLTableExecutorUtil.executeInsert(tableName, schema, params, api, configuredParamDTOs);
            case "UPDATE":
                return postgreSQLTableExecutorUtil.executeUpdate(tableName, schema, params, api, configuredParamDTOs);
            case "DELETE":
                return postgreSQLTableExecutorUtil.executeDelete(tableName, schema, params, api, configuredParamDTOs);
            default:
                throw new RuntimeException("Unsupported table operation: " + operation);
        }
    }

    @Override
    protected boolean checkObjectExistsInDatabase(String schema, String objectName,
                                                  String objectType, String databaseType) {
        try {
            String sql = "";
            switch (objectType.toUpperCase()) {
                case "TABLE":
                    sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?";
                    break;
                case "VIEW":
                    sql = "SELECT COUNT(*) FROM information_schema.views WHERE table_schema = ? AND table_name = ?";
                    break;
                case "MATERIALIZED VIEW":
                    sql = "SELECT COUNT(*) FROM pg_matviews WHERE schemaname = ? AND matviewname = ?";
                    break;
                case "FUNCTION":
                    sql = "SELECT COUNT(*) FROM pg_proc p JOIN pg_namespace n ON p.pronamespace = n.oid WHERE n.nspname = ? AND p.proname = ?";
                    break;
                case "PROCEDURE":
                    sql = "SELECT COUNT(*) FROM pg_proc p JOIN pg_namespace n ON p.pronamespace = n.oid WHERE n.nspname = ? AND p.proname = ? AND p.prokind = 'p'";
                    break;
                default:
                    return false;
            }

            Integer count = postgresqlJdbcTemplate.queryForObject(sql, Integer.class, schema, objectName);
            return count != null && count > 0;
        } catch (Exception e) {
            log.debug("Error checking object existence in PostgreSQL: {}", e.getMessage());
            return false;
        }
    }

    @Override
    protected String resolveSchemaFromDatabase(String objectName, String defaultSchema, String databaseType) {
        try {
            String sql = "SELECT n.nspname FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE c.relname = ? AND n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                    "LIMIT 1";

            List<String> schemas = postgresqlJdbcTemplate.queryForList(sql, String.class, objectName);
            if (!schemas.isEmpty()) {
                return schemas.get(0);
            }

            sql = "SELECT n.nspname FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE p.proname = ? AND n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                    "LIMIT 1";

            schemas = postgresqlJdbcTemplate.queryForList(sql, String.class, objectName);
            if (!schemas.isEmpty()) {
                return schemas.get(0);
            }

        } catch (Exception e) {
            log.debug("Error resolving schema in PostgreSQL: {}", e.getMessage());
        }

        return defaultSchema != null ? defaultSchema : "public";
    }
}