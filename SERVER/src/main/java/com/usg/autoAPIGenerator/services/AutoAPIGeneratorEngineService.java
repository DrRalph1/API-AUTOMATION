package com.usg.autoAPIGenerator.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.*;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.*;
import com.usg.autoAPIGenerator.entities.postgres.apiSecurity.IPWhitelistEntryEntity;
import com.usg.autoAPIGenerator.entities.postgres.codeBase.ImplementationEntity;
import com.usg.autoAPIGenerator.entities.postgres.collections.*;
import com.usg.autoAPIGenerator.factories.ApiExecutionHelperFactory;
import com.usg.autoAPIGenerator.factories.ApiMetadataHelperFactory;
import com.usg.autoAPIGenerator.factories.DatabaseTypeServiceFactory;
import com.usg.autoAPIGenerator.factories.ParameterGeneratorFactory;
import com.usg.autoAPIGenerator.helpers.*;
import com.usg.autoAPIGenerator.helpers.apiEngine.oracle.OracleApiValidationHelper;
import com.usg.autoAPIGenerator.helpers.apiEngine.postgresql.PostgreSQLApiValidationHelper;
import com.usg.autoAPIGenerator.repositories.apiGenerationEngine.*;
import com.usg.autoAPIGenerator.repositories.apiSecurity.IPWhitelistEntryRepository;
import com.usg.autoAPIGenerator.repositories.codeBase.*;
import com.usg.autoAPIGenerator.repositories.codeBase.FolderRepository;
import com.usg.autoAPIGenerator.repositories.collections.AuthConfigRepository;
import com.usg.autoAPIGenerator.repositories.collections.HeaderRepository;
import com.usg.autoAPIGenerator.repositories.collections.ParameterRepository;
import com.usg.autoAPIGenerator.repositories.documentation.*;
import com.usg.autoAPIGenerator.helpers.apiEngine.*;
import com.usg.autoAPIGenerator.interfaces.DatabaseSchemaService;
import com.usg.autoAPIGenerator.services.schemaBrowser.OracleSchemaService;
import com.usg.autoAPIGenerator.services.schemaBrowser.PostgreSQLSchemaService;
import com.usg.autoAPIGenerator.utils.apiEngine.*;
import com.usg.autoAPIGenerator.utils.LoggerUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.executor.oracle.*;
import com.usg.autoAPIGenerator.utils.apiEngine.executor.postgresql.PostgreSQLFunctionExecutorUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.executor.postgresql.PostgreSQLProcedureExecutorUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.executor.postgresql.PostgreSQLTableExecutorUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.executor.postgresql.PostgreSQLViewExecutorUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.generator.CodeBaseGeneratorUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.generator.CollectionsGeneratorUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.generator.CustomQueryParserUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.generator.DocumentationGeneratorUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.OracleObjectResolverUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.OracleParameterGeneratorUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.OracleParameterValidatorUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.OracleTypeMapperUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoAPIGeneratorEngineService {

    // ==================== REPOSITORIES ====================
    private final GeneratedAPIRepository generatedAPIRepository;
    private final ApiExecutionLogRepository executionLogRepository;
    private final ApiTestRepository apiTestRepository;
    private final CollectionRepository codeBaseCollectionRepository;
    private final FolderRepository codeBaseFolderRepository;
    private final RequestRepository codeBaseRequestRepository;
    private final ImplementationRepository implementationRepository;
    private final com.usg.autoAPIGenerator.repositories.collections.CollectionRepository collectionsCollectionRepository;
    private final com.usg.autoAPIGenerator.repositories.collections.FolderRepository collectionsFolderRepository;
    private final com.usg.autoAPIGenerator.repositories.collections.RequestRepository collectionsRequestRepository;
    private final HeaderRepository collectionsHeaderRepository;
    private final ParameterRepository collectionsParameterRepository;
    private final AuthConfigRepository collectionsAuthConfigRepository;
    private final APICollectionRepository docCollectionRepository;
    private final com.usg.autoAPIGenerator.repositories.documentation.FolderRepository docFolderRepository;
    private final APIEndpointRepository endpointRepository;
    private final com.usg.autoAPIGenerator.repositories.documentation.HeaderRepository docHeaderRepository;
    private final com.usg.autoAPIGenerator.repositories.documentation.ParameterRepository docParameterRepository;
    private final ResponseExampleRepository responseExampleRepository;
    private final CodeExampleRepository codeExampleRepository;
    private final ChangelogRepository changelogRepository;

    // ==================== HELPERS (Business Logic) ====================
    private final OracleApiValidationHelper oracleValidationHelper;
    private final PostgreSQLApiValidationHelper postgresValidationHelper;
    private final ApiConversionHelper conversionHelper;
    private final ApiResponseHelper responseHelper;
    private final CustomQueryParserUtil customQueryParserUtil;
    // REMOVE THIS: private final OracleApiExecutionHelper executionHelper;
    private final ApiComponentHelper componentHelper;
    // REMOVE THESE: private final OracleApiMetadataHelper oracleMetadataHelper;
    // REMOVE THESE: private final PostgreSQLApiMetadataHelper postgresMetadataHelper;

    // ==================== UTILS (Technical Utilities) ====================
    private final GenUrlBuilderUtil genUrlBuilder;
    private final OracleTypeMapperUtil typeMapper;
    private final OracleObjectResolverUtil objectResolver;
    private final OracleParameterValidatorUtil parameterValidator;
    private final AuthenticationServiceUtil authenticationService;

    // ==================== EXECUTORS ====================
    private final OracleTableExecutorUtil oracleTableExecutorUtil;
    private final OracleViewExecutorUtil oracleViewExecutorUtil;
    private final OracleProcedureExecutorUtil oracleProcedureExecutorUtil;
    private final OracleFunctionExecutorUtil oracleFunctionExecutorUtil;
    private final OraclePackageExecutorUtil oraclePackageExecutorUtil;
    private final PostgreSQLTableExecutorUtil postgreSQLTableExecutorUtil;
    private final PostgreSQLViewExecutorUtil postgreSQLViewExecutorUtil;
    private final PostgreSQLProcedureExecutorUtil postgreSQLProcedureExecutorUtil;
    private final PostgreSQLFunctionExecutorUtil postgreSQLFunctionExecutorUtil;

    // ==================== GENERATORS ====================
    private final CodeBaseGeneratorUtil codeBaseGeneratorUtil;
    private final CollectionsGeneratorUtil collectionsGeneratorUtil;
    private final DocumentationGeneratorUtil documentationGeneratorUtil;
    private final CodeLanguageGeneratorUtil codeLanguageGeneratorUtil;
    private final OracleParameterGeneratorUtil oracleParameterGeneratorUtil;
    private final PostgreSQLParameterGeneratorUtil postgreSQLParameterGeneratorUtil;

    // ==================== FACTORIES ====================
    private final ParameterGeneratorFactory parameterGeneratorFactory;
    private final DatabaseTypeServiceFactory databaseTypeFactory;
    private final ApiExecutionHelperFactory executionHelperFactory;
    private final ApiMetadataHelperFactory metadataHelperFactory;

    // ==================== API REQUEST LOGGING ====================
    private final ApiRequestService apiRequestService;

    // ==================== EXTERNAL DEPENDENCIES ====================
    private final ObjectMapper objectMapper;
    private final LoggerUtil loggerUtil;
    private final ApiValidatorHelper validatorService;
    private final OracleSchemaService oracleSchemaService;
    private final PostgreSQLSchemaService postgreSQLSchemaService;
    private final EntityManager entityManager;

    private final IPWhitelistEntryRepository ipWhitelistRepository;

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    // ==================== PUBLIC API METHODS ====================

    @Transactional
    public GeneratedApiResponseDTO generateApi(String requestId, String performedBy, GenerateApiRequestDTO request) {
        long startTime = System.currentTimeMillis();
        try {
            String databaseType = request.getDatabaseType();
            if (databaseType == null || databaseType.isEmpty()) {
                if (request.getSourceObject() != null && request.getSourceObject().containsKey("databaseType")) {
                    databaseType = (String) request.getSourceObject().get("databaseType");
                }
            }
            if (databaseType == null || databaseType.isEmpty()) {
                databaseType = "oracle";
            }

            loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                    ", Generating API: " + request.getApiName() +
                    " for database: " + databaseType + " by: " + performedBy);

            // Log if this is a custom query
            if (Boolean.TRUE.equals(request.getUseCustomQuery()) ||
                    (request.getCustomSelectStatement() != null && !request.getCustomSelectStatement().trim().isEmpty())) {
                loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                        ", This is a CUSTOM QUERY API generation");
            }

            DatabaseValidationHelper validationHelper = databaseTypeFactory.getValidationHelper(databaseType);
            validationHelper.validateApiCodeUniqueness(generatedAPIRepository, request.getApiCode());

            Object schemaService = databaseTypeFactory.getSchemaService(databaseType);
            ApiSourceObjectDTO sourceObjectDTO = convertAndValidateSourceObjectForGeneration(request, schemaService, databaseType);
            CollectionInfoDTO collectionInfo = validationHelper.validateAndGetCollectionInfo(request.getCollectionInfo());
            String endpointPath = buildEndpointPathFromRequest(request);
            DatabaseParameterGeneratorUtil parameterGenerator = parameterGeneratorFactory.getGenerator(databaseType);

            BaseApiExecutionHelper executionHelper = executionHelperFactory.getExecutionHelper(databaseType);

            GeneratedApiEntity savedApi = executionHelper.createAndSaveApiEntity(
                    request, sourceObjectDTO, collectionInfo, endpointPath, performedBy, null,
                    generatedAPIRepository, objectMapper, parameterGenerator, conversionHelper, databaseType);

            // ============ CORRECTION: SAVE PROTOCOL-SPECIFIC FIELDS ============
            savedApi.setProtocolType(request.getProtocolType() != null ? request.getProtocolType() : "rest");

            if (request.getSoapConfig() != null) {
                savedApi.setSoapConfig(request.getSoapConfig());
            }

            if (request.getGraphqlConfig() != null) {
                savedApi.setGraphqlConfig(request.getGraphqlConfig());
            }

            if (request.getFileUploadConfig() != null) {
                savedApi.setFileUploadConfig(request.getFileUploadConfig());
            }

            savedApi = generatedAPIRepository.save(savedApi);
            // ============ END CORRECTION ============

            GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(savedApi);
            String codeBaseRequestId = codeBaseGeneratorUtil.generate(savedApi, performedBy, request, collectionInfo);

            Map<String, String> collectionResult = collectionsGeneratorUtil.generateWithDetails(
                    savedApi, performedBy, request, collectionInfo);
            String collectionId = collectionResult.get("collectionId");
            String collectionsRequestId = collectionResult.get("requestId");

            savedApi.setSourceRequestId(collectionsRequestId);
            generatedAPIRepository.save(savedApi);

            String docCollectionId = documentationGeneratorUtil.generate(savedApi, performedBy, request,
                    codeBaseRequestId, collectionId, collectionInfo);

            GeneratedApiResponseDTO response = responseHelper.buildGenerateApiResponse(
                    savedApi, genUrlInfo, codeBaseRequestId, collectionId, docCollectionId,
                    collectionInfo, genUrlBuilder, conversionHelper,
                    (api) -> generateApiCode(api));

            loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                    ", API generated successfully with ID: " + savedApi.getId() +
                    ", Database: " + databaseType +
                    " in " + (System.currentTimeMillis() - startTime) + "ms");

            return response;

        } catch (Exception e) {
            loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                    ", Error generating API: " + e.getMessage());
            log.error("Error generating API", e);
            throw new RuntimeException("Failed to generate API: " + e.getMessage(), e);
        }
    }


    /**
     * Clear ALL protocol-specific configurations when switching protocols
     * This completely resets the API configuration for the target protocol
     */
    private void clearProtocolSpecificConfigurations(GeneratedApiEntity api, String targetProtocolType) {
        String oldProtocol = api.getProtocolType();
        log.info("🔄 Switching protocol from {} to {} - Clearing ALL previous configurations",
                oldProtocol, targetProtocolType);

        // ============ CLEAR ALL PROTOCOL CONFIGURATIONS ============

        // Clear SOAP config
        api.setSoapConfig(null);

        // Clear GraphQL config
        api.setGraphqlConfig(null);

        // Clear FileUpload config
        api.setFileUploadConfig(null);

        // ============ CLEAR ALL PARAMETERS ============
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            log.info("Clearing {} parameters", api.getParameters().size());
            api.getParameters().clear();
        }

        // ============ CLEAR ALL HEADERS ============
        if (api.getHeaders() != null && !api.getHeaders().isEmpty()) {
            log.info("Clearing {} headers", api.getHeaders().size());
            api.getHeaders().clear();
        }

        // ============ CLEAR RESPONSE MAPPINGS ============
        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
            log.info("Clearing {} response mappings", api.getResponseMappings().size());
            api.getResponseMappings().clear();
        }

        // ============ RESET REQUEST CONFIGURATION ============
        if (api.getRequestConfig() != null) {
            api.getRequestConfig().setBodyType(getDefaultBodyTypeForProtocol(targetProtocolType));
            api.getRequestConfig().setSample(null);
            api.getRequestConfig().setRequiredFields(null);
            log.info("Reset request config - body type: {}", api.getRequestConfig().getBodyType());
        }

        // ============ RESET RESPONSE CONFIGURATION ============
        if (api.getResponseConfig() != null) {
            api.getResponseConfig().setSuccessSchema(null);
            api.getResponseConfig().setErrorSchema(null);
            log.info("Reset response config");
        }

        // ============ SET PROTOCOL-SPECIFIC DEFAULTS ============
        switch (targetProtocolType.toLowerCase()) {
            case "soap":
                // SOAP always uses POST
                api.setHttpMethod("POST");

                // Create default SOAP config
                SoapConfigDTO defaultSoapConfig = new SoapConfigDTO();
                defaultSoapConfig.setVersion("1.1");
                defaultSoapConfig.setBindingStyle("document");
                defaultSoapConfig.setEncodingStyle("literal");
                defaultSoapConfig.setSoapAction(api.getApiCode() != null ? api.getApiCode() : "ProcessRequest");
                defaultSoapConfig.setNamespace("http://tempuri.org/");
                defaultSoapConfig.setServiceName(api.getApiName() != null ? api.getApiName().replaceAll("\\s+", "") : "ApiService");
                api.setSoapConfig(defaultSoapConfig);

                // Set endpoint path for SOAP
                api.setEndpointPath("/soap/" + (api.getApiCode() != null ? api.getApiCode().toLowerCase() : "service"));

                log.info("✅ SOAP configuration initialized - All previous data cleared");
                break;

            case "graphql":
                // GraphQL always uses POST
                api.setHttpMethod("POST");

                // Create default GraphQL config
                GraphQLConfigDTO defaultGraphqlConfig = new GraphQLConfigDTO();
                defaultGraphqlConfig.setOperationType("query");
                defaultGraphqlConfig.setOperationName(api.getApiCode() != null ? api.getApiCode().toLowerCase() : "query");
                defaultGraphqlConfig.setEnableIntrospection(true);
                defaultGraphqlConfig.setMaxQueryDepth(10);
                api.setGraphqlConfig(defaultGraphqlConfig);

                // Set endpoint path for GraphQL
                api.setEndpointPath("/graphql");

                log.info("✅ GraphQL configuration initialized - All previous data cleared");
                break;

            case "rest":
            default:
                // REST default HTTP method
                api.setHttpMethod("GET");

                // Set default base path
                if (api.getBasePath() == null || api.getBasePath().isEmpty()) {
                    api.setBasePath("/api/v1");
                }

                // Set endpoint path for REST
                api.setEndpointPath("/" + (api.getApiCode() != null ?
                        api.getApiCode().toLowerCase().replace("_", "-") : "endpoint"));

                // Set default body type based on HTTP method
                if (api.getRequestConfig() != null) {
                    api.getRequestConfig().setBodyType("none");
                }

                log.info("✅ REST configuration initialized - All previous data cleared");
                break;
        }

        log.info("🎯 Protocol switch complete - Target: {}, HTTP Method: {}, Endpoint: {}, Parameters: 0, Headers: 0",
                targetProtocolType, api.getHttpMethod(), api.getEndpointPath());
    }

    /**
     * Helper method to get default body type for protocol
     */
    private String getDefaultBodyTypeForProtocol(String protocolType) {
        switch (protocolType.toLowerCase()) {
            case "soap":
                return "soap";
            case "graphql":
                return "graphql";
            case "rest":
            default:
                return "none";
        }
    }


    @Transactional
    public GeneratedApiResponseDTO updateApi(String requestId, String apiId, String performedBy,
                                             GenerateApiRequestDTO request) {
        try {
            loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                    ", Updating API: " + apiId + " by: " + performedBy);

            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // ============ CRITICAL FIX: Get database type from request first for custom queries ============
            String databaseType = request.getDatabaseType();

            // If not in request, check sourceObject
            if (databaseType == null || databaseType.isEmpty()) {
                if (request.getSourceObject() != null && request.getSourceObject().containsKey("databaseType")) {
                    databaseType = (String) request.getSourceObject().get("databaseType");
                }
            }

            // For custom queries, detect from syntax if still not determined
            if (databaseType == null || databaseType.isEmpty()) {
                String customQuery = request.getCustomSelectStatement();
                if (customQuery != null && !customQuery.trim().isEmpty()) {
                    // Detect PostgreSQL-specific syntax
                    if (customQuery.contains("::jsonb") ||
                            customQuery.contains("ILIKE") ||
                            customQuery.contains("->>") ||
                            customQuery.matches(".*\\$\\d+.*")) {
                        databaseType = "postgresql";
                        log.info("Detected PostgreSQL syntax in custom query, setting databaseType to: postgresql");
                    } else {
                        databaseType = "oracle";
                    }
                } else {
                    // Fall back to existing API's database type
                    databaseType = api.getDatabaseType();
                }
            }

            // Final fallback
            if (databaseType == null || databaseType.isEmpty()) {
                databaseType = "oracle";
            }

            // CRITICAL: Set the database type on the API entity
            api.setDatabaseType(databaseType);

            log.info("Updating API: {} on database: {}", apiId, databaseType);
            // ============ END FIX ============

            // ============ CHECK IF PROTOCOL IS CHANGING ============
            String currentProtocol = api.getProtocolType();
            String newProtocol = request.getProtocolType();
            boolean isProtocolChanging = currentProtocol != null && newProtocol != null &&
                    !currentProtocol.equalsIgnoreCase(newProtocol);

            if (isProtocolChanging) {
                log.info("⚠️ PROTOCOL CHANGE DETECTED: {} -> {}", currentProtocol, newProtocol);
                log.info("Clearing ALL existing configuration (parameters, headers, mappings, etc.)");
                clearProtocolSpecificConfigurations(api, newProtocol);
            }
            // ============ END PROTOCOL CHANGE CHECK ============

            BaseApiExecutionHelper executionHelper = executionHelperFactory.getExecutionHelper(databaseType);
            String originalSourceRequestId = api.getSourceRequestId();

            DatabaseValidationHelper validationHelper = databaseTypeFactory.getValidationHelper(databaseType);
            validationHelper.validateApiCodeUniquenessOnUpdate(
                    generatedAPIRepository, api.getApiCode(), request.getApiCode());

            Object schemaService = databaseTypeFactory.getSchemaService(databaseType);

            // CRITICAL: For custom queries, pass the custom SELECT statement through with the correct database type
            ApiSourceObjectDTO sourceObjectDTO = convertAndValidateSourceObjectForUpdate(request, schemaService, databaseType);

            CollectionInfoDTO collectionInfo = validationHelper.validateAndGetCollectionInfo(request.getCollectionInfo());

            executionHelper.updateApiEntity(api, request, sourceObjectDTO, collectionInfo, performedBy);
            executionHelper.clearApiRelationships(api);

            DatabaseParameterGeneratorUtil parameterGenerator = parameterGeneratorFactory.getGenerator(databaseType);
            executionHelper.recreateApiRelationships(api, request, sourceObjectDTO,
                    parameterGenerator, conversionHelper);

            api.setSourceRequestId(originalSourceRequestId);
            // CRITICAL: Ensure database type is set again after recreation
            api.setDatabaseType(databaseType);

            // ============ UPDATE PROTOCOL-SPECIFIC FIELDS ============
            if (request.getProtocolType() != null) {
                api.setProtocolType(request.getProtocolType());
            }

            // Only set configs if they were provided in the request
            if (request.getSoapConfig() != null) {
                api.setSoapConfig(request.getSoapConfig());
            }
            // If protocol is SOAP but no config provided and we didn't already set default, create default
            else if ("soap".equalsIgnoreCase(api.getProtocolType()) && api.getSoapConfig() == null) {
                SoapConfigDTO defaultSoapConfig = new SoapConfigDTO();
                defaultSoapConfig.setVersion("1.1");
                defaultSoapConfig.setBindingStyle("document");
                defaultSoapConfig.setEncodingStyle("literal");
                defaultSoapConfig.setSoapAction(api.getApiCode() != null ? api.getApiCode() : "ProcessRequest");
                defaultSoapConfig.setNamespace("http://tempuri.org/");
                defaultSoapConfig.setServiceName(api.getApiName() != null ? api.getApiName().replaceAll("\\s+", "") : "ApiService");
                api.setSoapConfig(defaultSoapConfig);
            }

            if (request.getGraphqlConfig() != null) {
                api.setGraphqlConfig(request.getGraphqlConfig());
            }
            else if ("graphql".equalsIgnoreCase(api.getProtocolType()) && api.getGraphqlConfig() == null) {
                GraphQLConfigDTO defaultGraphqlConfig = new GraphQLConfigDTO();
                defaultGraphqlConfig.setOperationType("query");
                defaultGraphqlConfig.setOperationName(api.getApiCode() != null ? api.getApiCode().toLowerCase() : "query");
                defaultGraphqlConfig.setEnableIntrospection(true);
                defaultGraphqlConfig.setMaxQueryDepth(10);
                api.setGraphqlConfig(defaultGraphqlConfig);
            }

            if (request.getFileUploadConfig() != null) {
                api.setFileUploadConfig(request.getFileUploadConfig());
            }
            // ============ END UPDATE ============

            GeneratedApiEntity savedApi = generatedAPIRepository.save(api);
            entityManager.flush();

            componentHelper.updateComponents(
                    savedApi, performedBy, request, collectionInfo,
                    shouldRegenerateComponents(request),
                    codeBaseGeneratorUtil, collectionsGeneratorUtil, documentationGeneratorUtil,
                    (apiEntity, user, req, collInfo) -> updateCodeBase(apiEntity, user, req, collInfo),
                    (apiEntity, user, req, collInfo, originalId) ->
                            updateCollections(apiEntity, user, req, collInfo, originalId),
                    (apiEntity, user, req, collInfo, codeBaseId, collectionId) ->
                            updateDocumentation(apiEntity, user, req, collInfo, codeBaseId, collectionId),
                    (apiEntity) -> getCodeBaseRequestId(apiEntity),
                    (apiEntity) -> getCollectionsCollectionId(apiEntity),
                    (apiEntity) -> generateApiCode(apiEntity)
            );

            loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                    ", API updated successfully: " + savedApi.getId() +
                    " on database: " + databaseType);

            return conversionHelper.mapToResponse(savedApi);

        } catch (Exception e) {
            loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                    ", Error updating API: " + e.getMessage());
            log.error("Error updating API on database: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update API: " + e.getMessage(), e);
        }
    }



    // Helper method to convert and validate source object based on database type
    private ApiSourceObjectDTO convertAndValidateSourceObject(GenerateApiRequestDTO request, String databaseType) {
        if ("postgresql".equalsIgnoreCase(databaseType)) {
            // For PostgreSQL, use PostgreSQL-specific conversion if needed
            // Your existing conversion logic, but with PostgreSQL in mind
            return conversionHelper.convertAndValidateSourceObject(
                    request.getSourceObject(), null, objectMapper);
        } else {
            // Default to Oracle
            return conversionHelper.convertAndValidateSourceObject(
                    request.getSourceObject(), oracleSchemaService, objectMapper);
        }
    }


    /**
     * Helper method to convert and validate source object for generation
     */
    private ApiSourceObjectDTO convertAndValidateSourceObjectForGeneration(GenerateApiRequestDTO request,
                                                                           Object schemaService,
                                                                           String databaseType) {

        // Check for custom query FIRST
        if (Boolean.TRUE.equals(request.getUseCustomQuery()) ||
                (request.getCustomSelectStatement() != null && !request.getCustomSelectStatement().trim().isEmpty())) {

            log.info("Processing custom SELECT statement for API generation");

            // CRITICAL FIX: Use the databaseType from the request, not defaulting to oracle
            String actualDatabaseType = request.getDatabaseType();
            if (actualDatabaseType == null || actualDatabaseType.isEmpty()) {
                actualDatabaseType = databaseType; // Use the passed databaseType
            }
            if (actualDatabaseType == null || actualDatabaseType.isEmpty()) {
                actualDatabaseType = "postgresql"; // Default to postgresql for custom queries with PostgreSQL syntax
            }

            log.info("Custom query database type: {}", actualDatabaseType);

            Map<String, Object> sourceObjectMap = request.getSourceObject();
            if (sourceObjectMap == null) {
                sourceObjectMap = new HashMap<>();
            }
            sourceObjectMap.put("customSelectStatement", request.getCustomSelectStatement());
            sourceObjectMap.put("objectType", "CUSTOM_QUERY");
            sourceObjectMap.put("operation", "SELECT");
            sourceObjectMap.put("databaseType", actualDatabaseType); // Use the actual database type
            sourceObjectMap.put("isCustomQuery", true);
            sourceObjectMap.put("useCustomQuery", true);

            return conversionHelper.convertAndValidateSourceObjectForCustomQuery(
                    sourceObjectMap, objectMapper, actualDatabaseType, customQueryParserUtil
            );
        }

        // Regular database object flow
        if ("postgresql".equalsIgnoreCase(databaseType)) {
            return conversionHelper.convertAndValidateSourceObject(
                    request.getSourceObject(), null, objectMapper);
        } else {
            if (!(schemaService instanceof OracleSchemaService)) {
                throw new IllegalArgumentException("Expected OracleSchemaService for Oracle database type");
            }
            return conversionHelper.convertAndValidateSourceObject(
                    request.getSourceObject(), (OracleSchemaService) schemaService, objectMapper);
        }
    }




    /**
     * Helper method to convert and validate source object for update based on database type
     */
    private ApiSourceObjectDTO convertAndValidateSourceObjectForUpdate(GenerateApiRequestDTO request,
                                                                       Object schemaService,
                                                                       String databaseType) {
        // Check for custom query FIRST
        if (Boolean.TRUE.equals(request.getUseCustomQuery()) ||
                (request.getCustomSelectStatement() != null && !request.getCustomSelectStatement().trim().isEmpty())) {

            log.info("Processing custom SELECT statement for API update");

            // CRITICAL FIX: Use the databaseType from the parameter
            String actualDatabaseType = databaseType;

            // If still null, detect from syntax
            if (actualDatabaseType == null || actualDatabaseType.isEmpty()) {
                String customQuery = request.getCustomSelectStatement();
                if (customQuery != null && !customQuery.trim().isEmpty()) {
                    if (customQuery.contains("::jsonb") ||
                            customQuery.contains("ILIKE") ||
                            customQuery.contains("->>") ||
                            customQuery.matches(".*\\$\\d+.*")) {
                        actualDatabaseType = "postgresql";
                    } else {
                        actualDatabaseType = "oracle";
                    }
                } else {
                    actualDatabaseType = "oracle";
                }
            }

            log.info("Custom query database type for update: {}", actualDatabaseType);

            Map<String, Object> sourceObjectMap = request.getSourceObject();
            if (sourceObjectMap == null) {
                sourceObjectMap = new HashMap<>();
            }
            sourceObjectMap.put("customSelectStatement", request.getCustomSelectStatement());
            sourceObjectMap.put("objectType", "CUSTOM_QUERY");
            sourceObjectMap.put("operation", "SELECT");
            sourceObjectMap.put("databaseType", actualDatabaseType);
            sourceObjectMap.put("isCustomQuery", true);
            sourceObjectMap.put("useCustomQuery", true);

            return conversionHelper.convertAndValidateSourceObjectForCustomQuery(
                    sourceObjectMap, objectMapper, actualDatabaseType, customQueryParserUtil
            );
        }

        // Regular database object flow
        if ("postgresql".equalsIgnoreCase(databaseType)) {
            return conversionHelper.convertAndValidateSourceObject(
                    request.getSourceObject(), null, objectMapper);
        } else {
            if (!(schemaService instanceof OracleSchemaService)) {
                throw new IllegalArgumentException("Expected OracleSchemaService for Oracle database type");
            }
            return conversionHelper.convertAndValidateSourceObject(
                    request.getSourceObject(), (OracleSchemaService) schemaService, objectMapper);
        }
    }

    /**
     * Get the appropriate parameter generator based on database type
     */
    private DatabaseParameterGeneratorUtil getParameterGenerator(String databaseType) {
        return parameterGeneratorFactory.getGenerator(databaseType);
    }


    // Add this overloaded updateCollections method that accepts the original sourceRequestId
    private void updateCollections(GeneratedApiEntity api, String performedBy,
                                   GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo,
                                   String originalSourceRequestId) {
        try {
            log.info("Updating collections for API: {} with original request ID: {}",
                    api.getId(), originalSourceRequestId);

            // If we have an original sourceRequestId, try to update the existing request
            if (originalSourceRequestId != null && !originalSourceRequestId.isEmpty()) {
                log.info("Attempting to update existing collections request with ID: {}", originalSourceRequestId);

                // Check if the request still exists
                Optional<com.usg.autoAPIGenerator.entities.postgres.collections.RequestEntity> existingRequestOpt =
                        collectionsRequestRepository.findById(originalSourceRequestId);

                if (existingRequestOpt.isPresent()) {
                    var existingRequest = existingRequestOpt.get();

                    // Store current IDs BEFORE any changes
                    String currentCollectionId = existingRequest.getCollection() != null ?
                            existingRequest.getCollection().getId() : null;
                    String currentFolderId = existingRequest.getFolder() != null ?
                            existingRequest.getFolder().getId() : null;

                    String newCollectionId = collectionInfo != null ? collectionInfo.getCollectionId() : null;
                    String newFolderId = collectionInfo != null ? collectionInfo.getFolderId() : null;

                    boolean collectionChanged = (newCollectionId != null && !newCollectionId.equals(currentCollectionId));
                    boolean folderChanged = (newFolderId != null && !newFolderId.equals(currentFolderId));

                    log.info("Collection changed: {} -> {}, Folder changed: {} -> {}",
                            currentCollectionId, newCollectionId, currentFolderId, newFolderId);

                    if (collectionChanged || folderChanged) {
                        log.info("Collection or folder changed, moving request to new location");

                        // Get or create the new collection
                        CollectionEntity newCollection;
                        if (newCollectionId != null) {
                            Optional<CollectionEntity> existingCollectionOpt = collectionsCollectionRepository.findById(newCollectionId);
                            if (existingCollectionOpt.isPresent()) {
                                newCollection = existingCollectionOpt.get();
                            } else {
                                // Create new collection
                                CollectionEntity newColl = new CollectionEntity();
                                newColl.setId(newCollectionId);
                                newColl.setGeneratedApiId(api.getId());
                                newColl.setName(collectionInfo.getCollectionName());
                                newColl.setDescription("Collection for " + collectionInfo.getCollectionName());
                                newColl.setOwner(performedBy);
                                newColl.setExpanded(false);
                                newColl.setEditing(false);
                                newColl.setFavorite(false);
                                newColl.setLastActivity(LocalDateTime.now());
                                newColl.setColor(getRandomColor());
                                newColl.setCreatedAt(LocalDateTime.now());
                                newColl.setUpdatedAt(LocalDateTime.now());
                                newCollection = collectionsCollectionRepository.save(newColl);
                            }
                        } else {
                            newCollection = existingRequest.getCollection();
                        }

                        // Get or create the new folder
                        FolderEntity newFolder;
                        if (newFolderId != null) {
                            Optional<FolderEntity> existingFolderOpt = collectionsFolderRepository.findById(newFolderId);
                            if (existingFolderOpt.isPresent()) {
                                newFolder = existingFolderOpt.get();
                            } else {
                                // Create new folder
                                FolderEntity newFldr = new FolderEntity();
                                newFldr.setId(newFolderId);
                                newFldr.setGeneratedApiId(api.getId());
                                newFldr.setName(collectionInfo.getFolderName());
                                newFldr.setDescription("Folder for " + collectionInfo.getFolderName());
                                newFldr.setExpanded(false);
                                newFldr.setEditing(false);
                                newFldr.setRequestCount(0);
                                newFldr.setCollection(newCollection);
                                newFldr.setCreatedAt(LocalDateTime.now());
                                newFldr.setUpdatedAt(LocalDateTime.now());
                                newFolder = collectionsFolderRepository.save(newFldr);
                            }
                        } else {
                            newFolder = existingRequest.getFolder();
                        }

                        // CRITICAL: Clear all relationships from the existing request
                        clearAllRequestRelationships(existingRequest);

                        // Update the request with new collection and folder
                        existingRequest.setCollection(newCollection);
                        existingRequest.setFolder(newFolder);

                        // Update the request content
                        updateRequestContent(existingRequest, api, performedBy, request);

                        // Save the updated request
                        collectionsRequestRepository.save(existingRequest);

                        // CRITICAL: Flush and clear the EntityManager to ensure all changes are persisted
                        entityManager.flush();
                        entityManager.clear();

                        log.info("Successfully moved request to new collection/folder");
                        return;
                    }

                    // If collection/folder hasn't changed, just update the content
                    log.info("No collection/folder change, just updating content");
                    clearAllRequestRelationships(existingRequest);
                    updateRequestContent(existingRequest, api, performedBy, request);
                    log.info("Successfully updated existing collections request: {}", originalSourceRequestId);
                    return;
                } else {
                    log.warn("Existing collections request with ID {} not found. Will generate new one.",
                            originalSourceRequestId);
                }
            }

            // If no existing request or it was deleted, generate new ones
            log.info("No existing collections request found, generating new ones for API: {}", api.getId());
            Map<String, String> collectionResult = collectionsGeneratorUtil.generateWithDetails(
                    api, performedBy, request, collectionInfo);

            String collectionsRequestId = collectionResult.get("requestId");

            // CRITICAL: Update the API with the new collections request ID
            if (collectionsRequestId != null) {
                api.setSourceRequestId(collectionsRequestId);
                generatedAPIRepository.save(api);
                log.info("Generated new collections request with ID: {} and updated API", collectionsRequestId);
            } else {
                log.error("Failed to generate new collections request - requestId is null");
            }

        } catch (Exception e) {
            log.error("Error updating collections: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update collections: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to completely clear all relationships from a request
     * This is critical to avoid duplicate key constraint violations
     */
    private void clearAllRequestRelationships(com.usg.autoAPIGenerator.entities.postgres.collections.RequestEntity request) {
        try {
            log.debug("Clearing all relationships for request: {}", request.getId());

            // Clear headers
            if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
                List<HeaderEntity> headersToRemove = new ArrayList<>(request.getHeaders());
                request.getHeaders().clear();
                if (!headersToRemove.isEmpty()) {
                    collectionsHeaderRepository.deleteAll(headersToRemove);
                    log.debug("Deleted {} headers for request: {}", headersToRemove.size(), request.getId());
                }
            }

            // Clear parameters
            if (request.getParams() != null && !request.getParams().isEmpty()) {
                List<ParameterEntity> paramsToRemove = new ArrayList<>(request.getParams());
                request.getParams().clear();
                if (!paramsToRemove.isEmpty()) {
                    collectionsParameterRepository.deleteAll(paramsToRemove);
                    log.debug("Deleted {} parameters for request: {}", paramsToRemove.size(), request.getId());
                }
            }

            // CRITICAL: Clear auth config properly
            if (request.getAuthConfig() != null) {
                AuthConfigEntity authConfig = request.getAuthConfig();
                log.debug("Deleting existing auth config with ID: {}", authConfig.getId());
                request.setAuthConfig(null);
                collectionsAuthConfigRepository.delete(authConfig);
                log.debug("Deleted auth config for request: {}", request.getId());
            }

            // Flush to ensure all deletions are processed immediately
            collectionsHeaderRepository.flush();
            collectionsParameterRepository.flush();
            collectionsAuthConfigRepository.flush();

            log.debug("Successfully cleared all relationships for request: {}", request.getId());

        } catch (Exception e) {
            log.error("Error clearing relationships for request {}: {}", request.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to clear request relationships", e);
        }
    }

    /**
     * Helper method to update request content (without parent structure changes)
     */
    private void updateRequestContent(com.usg.autoAPIGenerator.entities.postgres.collections.RequestEntity request,
                                      GeneratedApiEntity api, String performedBy,
                                      GenerateApiRequestDTO generateRequest) {
        try {
            GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);

            // Update basic info
            request.setName(api.getApiName() + " - " + api.getHttpMethod());
            request.setMethod(api.getHttpMethod());
            request.setUrl(genUrlInfo.getFullUrl());
            request.setDescription(api.getDescription());
            request.setLastModified(LocalDateTime.now());
            request.setUpdatedAt(LocalDateTime.now());

            // Set request body
            if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                request.setBody(api.getRequestConfig().getSample());
            }

            // Set auth type
            if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
                request.setAuthType(api.getAuthConfig().getAuthType().toLowerCase());
            }

            // Save the request first
            request = collectionsRequestRepository.save(request);

            // Create and add new headers
            List<HeaderEntity> newHeaders = createHeadersForRequest(api, request, api.getId());
            for (HeaderEntity header : newHeaders) {
                request.addHeader(header);
            }
            if (!newHeaders.isEmpty()) {
                collectionsHeaderRepository.saveAll(newHeaders);
            }

            // Create and add new parameters
            List<ParameterEntity> newParams = createParametersForRequest(api, request, api.getId());
            for (ParameterEntity param : newParams) {
                request.addParameter(param);
            }
            if (!newParams.isEmpty()) {
                collectionsParameterRepository.saveAll(newParams);
            }

            // Create new auth config (old one was already deleted in clearAllRequestRelationships)
            createAuthConfig(api, request, api.getId());

            // Final save
            collectionsRequestRepository.save(request);

            log.info("Successfully updated request content for: {}", request.getId());

        } catch (Exception e) {
            log.error("Error updating request content: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update request content", e);
        }
    }

    // Helper method to update folder request counts
    public void updateFolderRequestCounts(String oldFolderId, String newFolderId) {
        try {
            // Decrement count for old folder if it exists and is different
            if (oldFolderId != null && !oldFolderId.equals(newFolderId)) {
                collectionsFolderRepository.findById(oldFolderId).ifPresent(oldFolder -> {
                    int currentCount = Optional.ofNullable(oldFolder.getRequestCount()).orElse(0);
                    int newCount = Math.max(0, currentCount - 1);
                    oldFolder.setRequestCount(newCount);
                    collectionsFolderRepository.save(oldFolder);
                    log.debug("Decremented request count for old folder: {} from {} to {}",
                            oldFolderId, currentCount, newCount);
                });
            }

            // Increment count for new folder if it exists and is different
            if (newFolderId != null && !newFolderId.equals(oldFolderId)) {
                collectionsFolderRepository.findById(newFolderId).ifPresent(newFolder -> {
                    int currentCount = Optional.ofNullable(newFolder.getRequestCount()).orElse(0);
                    int newCount = currentCount + 1;
                    newFolder.setRequestCount(newCount);
                    collectionsFolderRepository.save(newFolder);
                    log.debug("Incremented request count for new folder: {} from {} to {}",
                            newFolderId, currentCount, newCount);
                });
            }
        } catch (Exception e) {
            log.warn("Failed to update folder request counts: {}", e.getMessage());
        }
    }

    // Helper method to create headers for a request
    private List<HeaderEntity> createHeadersForRequest(GeneratedApiEntity api,
                                                       com.usg.autoAPIGenerator.entities.postgres.collections.RequestEntity request,
                                                       String generatedApiId) {
        List<HeaderEntity> newHeaders = new ArrayList<>();

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity apiHeader : api.getHeaders()) {
                if (Boolean.TRUE.equals(apiHeader.getIsRequestHeader())) {
                    HeaderEntity header = new HeaderEntity();
                    header.setId(UUID.randomUUID().toString());
                    header.setGeneratedApiId(generatedApiId);
                    header.setKey(apiHeader.getKey());
                    header.setValue(apiHeader.getValue() != null ? apiHeader.getValue() : "");
                    header.setDescription(apiHeader.getDescription());
                    header.setEnabled(apiHeader.getRequired() != null ? apiHeader.getRequired() : true);
                    header.setRequest(request);
                    newHeaders.add(header);
                }
            }
        }

        if (api.getParameters() != null) {
            for (ApiParameterEntity apiParam : api.getParameters()) {
                if ("header".equals(apiParam.getParameterType())) {
                    HeaderEntity header = new HeaderEntity();
                    header.setId(UUID.randomUUID().toString());
                    header.setGeneratedApiId(generatedApiId);
                    header.setKey(apiParam.getKey());
                    header.setValue(apiParam.getExample() != null ? apiParam.getExample() : "");
                    header.setDescription(apiParam.getDescription());
                    header.setEnabled(apiParam.getRequired() != null ? apiParam.getRequired() : true);
                    header.setRequest(request);
                    newHeaders.add(header);
                }
            }
        }

        return newHeaders;
    }

    // Helper method to create parameters for a request
    private List<ParameterEntity> createParametersForRequest(GeneratedApiEntity api,
                                                             com.usg.autoAPIGenerator.entities.postgres.collections.RequestEntity request,
                                                             String generatedApiId) {
        List<ParameterEntity> newParams = new ArrayList<>();

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            for (ApiParameterEntity apiParam : api.getParameters()) {
                ParameterEntity param = new ParameterEntity();
                param.setId(UUID.randomUUID().toString());
                param.setGeneratedApiId(generatedApiId);
                param.setKey(apiParam.getKey());
                param.setValue(apiParam.getExample());
                param.setDescription(apiParam.getDescription());
                param.setEnabled(apiParam.getRequired() != null ? apiParam.getRequired() : true);
                param.setDbColumn(apiParam.getDbColumn());
                param.setDbParameter(apiParam.getDbParameter());
                param.setParameterType(apiParam.getParameterType());
                param.setOracleType(apiParam.getOracleType());
                param.setApiType(apiParam.getApiType());
                param.setParameterLocation(apiParam.getParameterLocation());
                param.setRequired(apiParam.getRequired());
                param.setValidationPattern(apiParam.getValidationPattern());
                param.setDefaultValue(apiParam.getDefaultValue());
                param.setInBody(apiParam.getInBody());
                param.setIsPrimaryKey(apiParam.getIsPrimaryKey());
                param.setParamMode(apiParam.getParamMode() != null ? apiParam.getParamMode() : "IN");
                param.setPosition(apiParam.getPosition() != null ? apiParam.getPosition() : 0);
                param.setRequest(request);
                newParams.add(param);
            }
        }

        return newParams;
    }

    // Helper method to create auth config
    private void createAuthConfig(GeneratedApiEntity api,
                                  com.usg.autoAPIGenerator.entities.postgres.collections.RequestEntity request,
                                  String generatedApiId) {
        if (api.getAuthConfig() == null || "NONE".equals(api.getAuthConfig().getAuthType())) {
            return;
        }

        AuthConfigEntity authConfig = new AuthConfigEntity();
        authConfig.setId(UUID.randomUUID().toString());
        authConfig.setGeneratedApiId(generatedApiId);
        authConfig.setRequest(request);
        authConfig.setType(api.getAuthConfig().getAuthType());

        switch (api.getAuthConfig().getAuthType()) {
            case "API_KEY":
                authConfig.setKey(api.getAuthConfig().getApiKeyHeader() != null ?
                        api.getAuthConfig().getApiKeyHeader() : "X-API-Key");
                authConfig.setValue("{{apiKey}}");
                authConfig.setAddTo("header");
                break;
            case "BEARER":
            case "JWT":
                authConfig.setType("bearer");
                authConfig.setToken("{{jwtToken}}");
                authConfig.setAddTo("header");
                break;
            case "BASIC":
                authConfig.setUsername("{{username}}");
                authConfig.setPassword("{{password}}");
                authConfig.setAddTo("header");
                break;
            case "ORACLE_ROLES":
                authConfig.setKey("X-Oracle-Session");
                authConfig.setValue("{{oracleSessionId}}");
                authConfig.setAddTo("header");
                break;
        }

        collectionsAuthConfigRepository.save(authConfig);
        request.setAuthConfig(authConfig);
    }

    // Helper method to get random color
    private String getRandomColor() {
        String[] colors = {"#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6",
                "#ec4899", "#06b6d4", "#84cc16", "#f97316", "#6366f1"};
        return colors[new Random().nextInt(colors.length)];
    }


    /**
     * Update documentation for an API
     * Handles moving endpoints between collections and folders WITHOUT deleting endpoints
     */
    private void updateDocumentation(GeneratedApiEntity api, String performedBy,
                                     GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo,
                                     String codeBaseRequestId, String collectionsCollectionId) {
        try {
            log.info("Updating documentation for API: {} with codeBaseRequestId: {} and collectionId: {}",
                    api.getId(), codeBaseRequestId, collectionsCollectionId);

            // Call the new update method instead of generate
            documentationGeneratorUtil.update(api, performedBy, request,
                    codeBaseRequestId, collectionsCollectionId, collectionInfo);

        } catch (Exception e) {
            log.error("Error updating documentation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update documentation: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to build endpoint URL
     */
    private String buildEndpointUrl(GeneratedApiEntity api) {
        GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);
        return genUrlInfo.getFullUrl();
    }

    /**
     * Helper method to update documentation collection
     */
    private void updateDocumentationCollection(
            com.usg.autoAPIGenerator.entities.postgres.documentation.APICollectionEntity collection,
            GeneratedApiEntity api, String performedBy,
            GenerateApiRequestDTO request,
            CollectionInfoDTO collectionInfo) {
        try {
            if (collection == null) return;

            if (collectionInfo != null && collectionInfo.getCollectionName() != null) {
                collection.setName(collectionInfo.getCollectionName());
            } else if (api.getApiName() != null) {
                collection.setName(api.getApiName() + " Documentation");
            }

            collection.setDescription("Documentation for " + api.getApiName());
            collection.setUpdatedAt(LocalDateTime.now());
            collection.setUpdatedBy(performedBy);

            // Update version
            if (api.getVersion() != null) {
                collection.setVersion(api.getVersion());
            }

            // Update metadata
            if (collection.getMetadata() == null) {
                collection.setMetadata(new java.util.HashMap<>());
            }
            collection.getMetadata().put("apiCode", api.getApiCode());
            collection.getMetadata().put("apiVersion", api.getVersion() != null ? api.getVersion() : "1.0");
            collection.getMetadata().put("lastUpdated", LocalDateTime.now().toString());
            collection.getMetadata().put("updatedBy", performedBy);

            // Update total endpoints count if endpoints list exists
            if (collection.getEndpoints() != null) {
                collection.setTotalEndpoints(collection.getEndpoints().size());
            }

            // Update total folders count
            if (collection.getFolders() != null) {
                collection.setTotalFolders(collection.getFolders().size());
            }

            docCollectionRepository.save(collection);

        } catch (Exception e) {
            log.error("Error updating documentation collection: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update documentation collection", e);
        }
    }

    /**
     * Helper method to clear endpoint relationships
     */
    private void clearEndpointRelationships(
            com.usg.autoAPIGenerator.entities.postgres.documentation.APIEndpointEntity endpoint) {
        try {
            if (endpoint.getHeaders() != null && !endpoint.getHeaders().isEmpty()) {
                docHeaderRepository.deleteAll(endpoint.getHeaders());
                endpoint.getHeaders().clear();
            }

            if (endpoint.getParameters() != null && !endpoint.getParameters().isEmpty()) {
                docParameterRepository.deleteAll(endpoint.getParameters());
                endpoint.getParameters().clear();
            }

            if (endpoint.getResponseExamples() != null && !endpoint.getResponseExamples().isEmpty()) {
                responseExampleRepository.deleteAll(endpoint.getResponseExamples());
                endpoint.getResponseExamples().clear();
            }

            docHeaderRepository.flush();
            docParameterRepository.flush();
            responseExampleRepository.flush();

        } catch (Exception e) {
            log.error("Error clearing endpoint relationships: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to clear endpoint relationships", e);
        }
    }

    /**
     * Helper method to create documentation headers
     */
    private List<com.usg.autoAPIGenerator.entities.postgres.documentation.HeaderEntity> createDocumentationHeaders(
            GeneratedApiEntity api,
            com.usg.autoAPIGenerator.entities.postgres.documentation.APIEndpointEntity endpoint) {

        List<com.usg.autoAPIGenerator.entities.postgres.documentation.HeaderEntity> headers = new ArrayList<>();

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity apiHeader : api.getHeaders()) {
                com.usg.autoAPIGenerator.entities.postgres.documentation.HeaderEntity header =
                        new com.usg.autoAPIGenerator.entities.postgres.documentation.HeaderEntity();
                header.setId(UUID.randomUUID().toString());
                header.setGeneratedApiId(api.getId());
                header.setKey(apiHeader.getKey());
                header.setValue(apiHeader.getValue() != null ? apiHeader.getValue() : "");
                header.setDescription(apiHeader.getDescription());
                header.setRequired(apiHeader.getRequired() != null ? apiHeader.getRequired() : false);
                header.setEndpoint(endpoint);
                headers.add(header);
            }
        }

        return headers;
    }

    /**
     * Helper method to create documentation parameters
     */
    private List<com.usg.autoAPIGenerator.entities.postgres.documentation.ParameterEntity> createDocumentationParameters(
            GeneratedApiEntity api,
            com.usg.autoAPIGenerator.entities.postgres.documentation.APIEndpointEntity endpoint) {

        List<com.usg.autoAPIGenerator.entities.postgres.documentation.ParameterEntity> parameters = new ArrayList<>();

        if (api.getParameters() != null) {
            for (ApiParameterEntity apiParam : api.getParameters()) {
                com.usg.autoAPIGenerator.entities.postgres.documentation.ParameterEntity param =
                        new com.usg.autoAPIGenerator.entities.postgres.documentation.ParameterEntity();
                param.setId(UUID.randomUUID().toString());
                param.setGeneratedApiId(api.getId());
                param.setName(apiParam.getKey());
                param.setKey(apiParam.getKey());
                param.setDbColumn(apiParam.getDbColumn());
                param.setDbParameter(apiParam.getDbParameter());
                param.setParameterType(apiParam.getParameterType());
                param.setOracleType(apiParam.getOracleType());
                param.setApiType(apiParam.getApiType());
                param.setParameterLocation(apiParam.getParameterLocation());
                param.setRequired(apiParam.getRequired());
                param.setDescription(apiParam.getDescription());
                param.setDefaultValue(apiParam.getDefaultValue());
                param.setExample(apiParam.getExample());
                param.setValidationPattern(apiParam.getValidationPattern());
                param.setInBody(apiParam.getInBody());
                param.setIsPrimaryKey(apiParam.getIsPrimaryKey());
                param.setParamMode(apiParam.getParamMode());
                param.setPosition(apiParam.getPosition());
                param.setEndpoint(endpoint);
                parameters.add(param);
            }
        }

        return parameters;
    }

    /**
     * Helper method to create response examples using ApiResponseConfigEntity
     */
    private List<com.usg.autoAPIGenerator.entities.postgres.documentation.ResponseExampleEntity> createResponseExamples(
            GeneratedApiEntity api,
            com.usg.autoAPIGenerator.entities.postgres.documentation.APIEndpointEntity endpoint) {

        List<com.usg.autoAPIGenerator.entities.postgres.documentation.ResponseExampleEntity> responses = new ArrayList<>();
        ApiResponseConfigEntity responseConfig = api.getResponseConfig();

        if (responseConfig != null) {
            // Create success response example from successSchema
            if (responseConfig.getSuccessSchema() != null && !responseConfig.getSuccessSchema().isEmpty()) {
                com.usg.autoAPIGenerator.entities.postgres.documentation.ResponseExampleEntity successResponse =
                        new com.usg.autoAPIGenerator.entities.postgres.documentation.ResponseExampleEntity();
                successResponse.setId(UUID.randomUUID().toString());
                successResponse.setGeneratedApiId(api.getId());
                successResponse.setStatusCode(200);
                successResponse.setDescription("Success response");
                successResponse.setContentType(responseConfig.getContentType() != null ?
                        responseConfig.getContentType() : "application/json");

                // Parse successSchema to Map if it's JSON
                try {
                    String successSchema = responseConfig.getSuccessSchema();
                    if (successSchema != null) {
                        Map<String, Object> exampleMap = objectMapper.readValue(successSchema, Map.class);
                        successResponse.setExample(exampleMap);
                    }
                } catch (Exception e) {
                    log.warn("Could not parse successSchema as JSON: {}", e.getMessage());
                    Map<String, Object> fallbackMap = new HashMap<>();
                    fallbackMap.put("response", responseConfig.getSuccessSchema());
                    successResponse.setExample(fallbackMap);
                }

                successResponse.setEndpoint(endpoint);
                responses.add(successResponse);
            }

            // Create error response example from errorSchema
            if (responseConfig.getErrorSchema() != null && !responseConfig.getErrorSchema().isEmpty()) {
                com.usg.autoAPIGenerator.entities.postgres.documentation.ResponseExampleEntity errorResponse =
                        new com.usg.autoAPIGenerator.entities.postgres.documentation.ResponseExampleEntity();
                errorResponse.setId(UUID.randomUUID().toString());
                errorResponse.setGeneratedApiId(api.getId());
                errorResponse.setStatusCode(400);
                errorResponse.setDescription("Error response");
                errorResponse.setContentType(responseConfig.getContentType() != null ?
                        responseConfig.getContentType() : "application/json");

                // Parse errorSchema to Map if it's JSON
                try {
                    String errorSchema = responseConfig.getErrorSchema();
                    if (errorSchema != null) {
                        Map<String, Object> exampleMap = objectMapper.readValue(errorSchema, Map.class);
                        errorResponse.setExample(exampleMap);
                    }
                } catch (Exception e) {
                    log.warn("Could not parse errorSchema as JSON: {}", e.getMessage());
                    Map<String, Object> fallbackMap = new HashMap<>();
                    fallbackMap.put("error", "Bad Request");
                    fallbackMap.put("message", responseConfig.getErrorSchema());
                    errorResponse.setExample(fallbackMap);
                }

                errorResponse.setEndpoint(endpoint);
                responses.add(errorResponse);
            }

            // If no specific schemas, add default response
            if (responses.isEmpty()) {
                com.usg.autoAPIGenerator.entities.postgres.documentation.ResponseExampleEntity defaultResponse =
                        new com.usg.autoAPIGenerator.entities.postgres.documentation.ResponseExampleEntity();
                defaultResponse.setId(UUID.randomUUID().toString());
                defaultResponse.setGeneratedApiId(api.getId());
                defaultResponse.setStatusCode(200);
                defaultResponse.setDescription("Response");
                defaultResponse.setContentType("application/json");
                Map<String, Object> defaultMap = new HashMap<>();
                defaultMap.put("message", "API response");
                defaultResponse.setExample(defaultMap);
                defaultResponse.setEndpoint(endpoint);
                responses.add(defaultResponse);
            }
        } else {
            // No response config, add default response
            com.usg.autoAPIGenerator.entities.postgres.documentation.ResponseExampleEntity defaultResponse =
                    new com.usg.autoAPIGenerator.entities.postgres.documentation.ResponseExampleEntity();
            defaultResponse.setId(UUID.randomUUID().toString());
            defaultResponse.setGeneratedApiId(api.getId());
            defaultResponse.setStatusCode(200);
            defaultResponse.setDescription("Response");
            defaultResponse.setContentType("application/json");
            Map<String, Object> defaultMap = new HashMap<>();
            defaultMap.put("message", "API response");
            defaultResponse.setExample(defaultMap);
            defaultResponse.setEndpoint(endpoint);
            responses.add(defaultResponse);
        }

        return responses;
    }


    /**
     * Extract parameters from GraphQL request body
     * Supports both standard variables field AND inline parameters
     */
    private Map<String, Object> extractGraphQLVariables(Map<String, Object> bodyMap) {
        Map<String, Object> extractedParams = new HashMap<>();

        if (bodyMap == null) {
            return extractedParams;
        }

        // PRIORITY 1: Check for standard variables field
        if (bodyMap.containsKey("variables") && bodyMap.get("variables") instanceof Map) {
            Map<String, Object> variables = (Map<String, Object>) bodyMap.get("variables");
            extractedParams.putAll(variables);
            log.info("Extracted GraphQL variables from 'variables' field: {}", variables.keySet());
            return extractedParams; // Standard format, no need to parse query
        }

        // PRIORITY 2: Check for inline parameters in query (for backward compatibility)
        if (bodyMap.containsKey("query") && bodyMap.get("query") instanceof String) {
            String query = (String) bodyMap.get("query");
            Map<String, Object> inlineParams = extractInlineGraphQLParameters(query);
            if (!inlineParams.isEmpty()) {
                extractedParams.putAll(inlineParams);
                log.info("Extracted inline GraphQL parameters from query string: {}", inlineParams.keySet());
            }
        }

        return extractedParams;
    }

    /**
     * Extract inline parameters from GraphQL query string (backward compatibility)
     * Example: query { getUser(id: "123", name: "John") { ... } }
     */
    private Map<String, Object> extractInlineGraphQLParameters(String query) {
        Map<String, Object> parameters = new HashMap<>();

        if (query == null || query.trim().isEmpty()) {
            return parameters;
        }

        try {
            // Pattern to match field with parameters: getTbsysuserss(user_id: "value", total_logins: "value")
            Pattern pattern = Pattern.compile(
                    "(\\w+)\\s*\\(([^)]+)\\)\\s*\\{",
                    Pattern.CASE_INSENSITIVE
            );

            Matcher matcher = pattern.matcher(query);

            while (matcher.find()) {
                String paramsSection = matcher.group(2);
                if (paramsSection != null && !paramsSection.trim().isEmpty()) {
                    Map<String, Object> extracted = parseParameterString(paramsSection);
                    parameters.putAll(extracted);
                }
            }

            log.debug("Extracted {} inline parameters from query", parameters.size());

        } catch (Exception e) {
            log.error("Error extracting inline GraphQL parameters: {}", e.getMessage(), e);
        }

        return parameters;
    }

    /**
     * Parse a parameter string like: user_id: "value", total_logins: "value"
     */
    private Map<String, Object> parseParameterString(String paramsSection) {
        Map<String, Object> parameters = new HashMap<>();

        // Pattern for parameter: name: value
        Pattern paramPattern = Pattern.compile(
                "(\\w+)\\s*:\\s*(?:" +
                        "\"([^\"]*)\"|" +      // Double-quoted string
                        "'([^']*)'|" +         // Single-quoted string
                        "(\\d+(?:\\.\\d+)?)|" + // Number
                        "(true|false)|" +      // Boolean
                        "null|" +              // Null
                        "\\$\\w+)"              // Variable reference (skip)
        );

        Matcher matcher = paramPattern.matcher(paramsSection);

        while (matcher.find()) {
            String paramName = matcher.group(1);
            Object paramValue = null;

            if (matcher.group(2) != null) {
                paramValue = matcher.group(2); // Double-quoted string
            } else if (matcher.group(3) != null) {
                paramValue = matcher.group(3); // Single-quoted string
            } else if (matcher.group(4) != null) {
                String numStr = matcher.group(4);
                if (numStr.contains(".")) {
                    paramValue = Double.parseDouble(numStr);
                } else {
                    paramValue = Long.parseLong(numStr);
                }
            } else if (matcher.group(5) != null) {
                paramValue = Boolean.parseBoolean(matcher.group(5));
            }

            if (paramValue != null) {
                parameters.put(paramName, paramValue);
                log.debug("Extracted parameter: {} = {}", paramName, paramValue);
            }
        }

        return parameters;
    }


    /**
     * Execute API based on the database type stored in the API entity
     * Supports multiple database types: Oracle, PostgreSQL, etc.
     * NOW WITH PROPER PROTOCOL-BASED RESPONSE FORMATTING FOR ERRORS
     */
    @Transactional
    public ExecuteApiResponseDTO executeApi(String requestId, String performedBy,
                                            String apiId, ExecuteApiRequestDTO executeRequest,
                                            String clientIp, String userAgent,
                                            HttpServletRequest httpServletRequest) {

        long startTime = System.currentTimeMillis();
        String capturedRequestId = null;
        ApiRequestResponseDTO capturedRequest = null;
        String databaseType = "oracle";
        String protocolType = "rest";
        ExecuteApiResponseDTO finalResponse = null;

        // Store the EXACT raw request and response as they are
        String rawRequestBody = null;
        String rawResponseBody = null;

        try {
            loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                    ", Executing API: " + apiId + " by: " + performedBy);

            // 1. Get the API entity
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // 2. Get the database type from the API entity
            databaseType = api.getDatabaseType();
            if (databaseType == null || databaseType.isEmpty()) {
                databaseType = "oracle";
            }

            // 3. Get protocol type for response formatting
            protocolType = api.getProtocolType();
            if (protocolType == null || protocolType.isEmpty()) {
                protocolType = "rest";
            }

            log.info("Executing API: {} on database: {} with protocol: {}", apiId, databaseType, protocolType);

            // 4. Get the appropriate execution helper
            BaseApiExecutionHelper executionHelper = executionHelperFactory.getExecutionHelper(databaseType);

            // 5. Validate request structure
            if (executeRequest == null) {
                executeRequest = new ExecuteApiRequestDTO();
                executeRequest.setRequestId(UUID.randomUUID().toString());
            }

            // 6. Extract HTTP method
            String httpMethod = null;
            if (httpServletRequest != null) {
                httpMethod = httpServletRequest.getMethod();
                log.info("HTTP method from HttpServletRequest: {}", httpMethod);
                executeRequest.setHttpMethod(httpMethod);
            }

            // 7. Prepare and validate the request
            ExecuteApiRequestDTO validatedRequest = executionHelper.prepareValidatedRequest(api, executeRequest);

            if (validatedRequest.getHttpMethod() == null && httpMethod != null) {
                validatedRequest.setHttpMethod(httpMethod);
                log.info("Re-set HTTP method in validatedRequest: {}", httpMethod);
            }

            // 8. Get actual client IP and request path
            String actualClientIp = getClientIpAddress(httpServletRequest);
            String fullRequestPath = httpServletRequest != null ? httpServletRequest.getRequestURI() : "";
            String endpointPath = api.getEndpointPath();

            // ============ CAPTURE EXACT RAW REQUEST BODY ============
            try {
                // Capture the raw request body exactly as it was sent
                rawRequestBody = getRawRequestBody(validatedRequest, protocolType);

                ApiRequestDTO requestDTO = convertExecuteRequestToApiRequestDTO(validatedRequest, api);
                requestDTO.setClientIpAddress(actualClientIp);
                requestDTO.setUserAgent(userAgent);
                requestDTO.setRequestedBy(performedBy);
                requestDTO.setCorrelationId(executeRequest.getRequestId());

                // Pass the raw request body to be stored as-is
                requestDTO.setRawRequestBody(rawRequestBody);

                capturedRequest = apiRequestService.captureRequest(
                        requestId, apiId, requestDTO, performedBy, httpServletRequest);
                capturedRequestId = capturedRequest.getId();
                log.info("Request captured successfully with ID: {}", capturedRequestId);
                log.info("Raw request body stored ({} chars)", rawRequestBody != null ? rawRequestBody.length() : 0);
            } catch (Exception e) {
                log.error("Failed to capture request: {}", e.getMessage());
            }
            // ============ END CAPTURE ============

            // ============ IP WHITELIST VALIDATION ============
            try {
                validateIpWhitelist(actualClientIp, fullRequestPath, endpointPath);
            } catch (RuntimeException e) {
                String technicalError = e.getMessage();
                String userFriendlyMessage = "Unable to process your request. Please contact support for assistance.";
                int statusCode = 403;

                log.warn("IP whitelist validation failed: {}", technicalError);

                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("code", "IP_WHITELIST_ERROR");
                errorMap.put("technicalMessage", technicalError);
                errorMap.put("clientIp", actualClientIp);
                errorMap.put("timestamp", LocalDateTime.now().toString());

                finalResponse = buildProtocolSpecificErrorResponse(
                        protocolType, statusCode, userFriendlyMessage, errorMap, api);

                // Get raw error response body
                rawResponseBody = getRawErrorResponseBody(finalResponse, protocolType);

                if (capturedRequestId != null) {
                    try {
                        apiRequestService.updateRequestWithRawResponse(
                                requestId, capturedRequestId, statusCode,
                                userFriendlyMessage, System.currentTimeMillis() - startTime, rawResponseBody);
                    } catch (Exception ex) {
                        log.error("Failed to update captured request with error: {}", ex.getMessage());
                    }
                }

                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, statusCode, System.currentTimeMillis() - startTime,
                        performedBy, actualClientIp, userAgent, technicalError, objectMapper);

                return finalResponse;
            }
            // ============ END IP WHITELIST VALIDATION ============

            // 9. Check API status
            String apiStatus = api.getStatus() != null ? api.getStatus().toUpperCase() : "UNKNOWN";

            if (!"ACTIVE".equals(apiStatus)) {
                String userFriendlyMessage;
                String errorCode;
                int statusCode;

                switch (apiStatus) {
                    case "ARCHIVED":
                        userFriendlyMessage = "This service is no longer available. Please contact support for alternatives.";
                        errorCode = "API_ARCHIVED";
                        statusCode = 410;
                        break;
                    case "DEPRECATED":
                        userFriendlyMessage = "This version of the service has been retired. Please upgrade to the latest version.";
                        errorCode = "API_DEPRECATED";
                        statusCode = 410;
                        break;
                    case "DRAFT":
                        userFriendlyMessage = "This service is not yet available. Please check back later.";
                        errorCode = "API_DRAFT";
                        statusCode = 403;
                        break;
                    case "INACTIVE":
                        userFriendlyMessage = "This service is currently unavailable. Please contact support for assistance.";
                        errorCode = "API_INACTIVE";
                        statusCode = 403;
                        break;
                    case "PENDING":
                        userFriendlyMessage = "This service is being set up and will be available soon.";
                        errorCode = "API_PENDING";
                        statusCode = 403;
                        break;
                    case "SUSPENDED":
                        userFriendlyMessage = "This service has been temporarily suspended. Please contact support for more information.";
                        errorCode = "API_SUSPENDED";
                        statusCode = 403;
                        break;
                    default:
                        userFriendlyMessage = "This service is currently unavailable. Please contact support for assistance.";
                        errorCode = "API_INVALID_STATUS";
                        statusCode = 403;
                        break;
                }

                log.warn("API execution blocked - Status: {}, API ID: {}, Database: {}", apiStatus, apiId, databaseType);

                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("code", errorCode);
                errorMap.put("technicalMessage", String.format("API status is '%s'", apiStatus));
                errorMap.put("apiId", apiId);
                errorMap.put("apiName", api.getApiName());
                errorMap.put("apiCode", api.getApiCode());
                errorMap.put("currentStatus", apiStatus);
                errorMap.put("timestamp", LocalDateTime.now().toString());

                finalResponse = buildProtocolSpecificErrorResponse(protocolType, statusCode, userFriendlyMessage, errorMap, api);
                rawResponseBody = getRawErrorResponseBody(finalResponse, protocolType);

                if (capturedRequestId != null) {
                    try {
                        apiRequestService.updateRequestWithRawResponse(
                                requestId, capturedRequestId, statusCode,
                                userFriendlyMessage, System.currentTimeMillis() - startTime, rawResponseBody);
                    } catch (Exception ex) {
                        log.error("Failed to update captured request with error: {}", ex.getMessage());
                    }
                }

                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, statusCode, System.currentTimeMillis() - startTime,
                        performedBy, actualClientIp, userAgent, apiStatus, objectMapper);

                return finalResponse;
            }

            // 10. Check isActive flag
            if (!api.getIsActive()) {
                String userFriendlyMessage = "This service is currently unavailable. Please contact support for assistance.";
                log.warn("API execution blocked - isActive flag is false for API ID: {}", apiId);

                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("code", "API_INACTIVE");
                errorMap.put("technicalMessage", "API is marked as inactive");
                errorMap.put("apiId", apiId);
                errorMap.put("apiName", api.getApiName());
                errorMap.put("apiCode", api.getApiCode());
                errorMap.put("isActive", false);
                errorMap.put("timestamp", LocalDateTime.now().toString());

                finalResponse = buildProtocolSpecificErrorResponse(protocolType, 403, userFriendlyMessage, errorMap, api);
                rawResponseBody = getRawErrorResponseBody(finalResponse, protocolType);

                if (capturedRequestId != null) {
                    try {
                        apiRequestService.updateRequestWithRawResponse(
                                requestId, capturedRequestId, 403,
                                userFriendlyMessage, System.currentTimeMillis() - startTime, rawResponseBody);
                    } catch (Exception ex) {
                        log.error("Failed to update captured request with error: {}", ex.getMessage());
                    }
                }

                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, 403, System.currentTimeMillis() - startTime,
                        performedBy, actualClientIp, userAgent, "API inactive", objectMapper);

                return finalResponse;
            }

            // 11. Validate HTTP method
            if (!validateHttpMethod(api, validatedRequest.getHttpMethod())) {
                String userFriendlyMessage = "Invalid request type. Please check your request and try again.";

                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("code", "HTTP_METHOD_NOT_ALLOWED");
                errorMap.put("technicalMessage", String.format("Expected: %s, Actual: %s",
                        api.getHttpMethod(), validatedRequest.getHttpMethod()));
                errorMap.put("expectedMethod", api.getHttpMethod());
                errorMap.put("actualMethod", validatedRequest.getHttpMethod());
                errorMap.put("apiId", apiId);
                errorMap.put("timestamp", LocalDateTime.now().toString());

                finalResponse = buildProtocolSpecificErrorResponse(protocolType, 405, userFriendlyMessage, errorMap, api);
                rawResponseBody = getRawErrorResponseBody(finalResponse, protocolType);

                if (capturedRequestId != null) {
                    try {
                        apiRequestService.updateRequestWithRawResponse(
                                requestId, capturedRequestId, 405,
                                userFriendlyMessage, System.currentTimeMillis() - startTime, rawResponseBody);
                    } catch (Exception ex) {
                        log.error("Failed to update captured request with error: {}", ex.getMessage());
                    }
                }

                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, 405, System.currentTimeMillis() - startTime,
                        performedBy, actualClientIp, userAgent, "Invalid HTTP method", objectMapper);

                return finalResponse;
            }

            // 12. Validate authentication
            try {
                AuthenticationServiceUtil.AuthenticationResult authResult =
                        authenticationService.validateAuthentication(api, validatedRequest);

                if (!authResult.isAuthenticated()) {
                    String userFriendlyMessage = "Unable to verify your credentials. Please check your API key and try again.";

                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("code", "AUTHENTICATION_FAILED");
                    errorMap.put("technicalMessage", authResult.getReason());
                    errorMap.put("authenticationType", api.getAuthConfig() != null ? api.getAuthConfig().getAuthType() : "UNKNOWN");
                    errorMap.put("apiId", apiId);
                    errorMap.put("timestamp", LocalDateTime.now().toString());

                    finalResponse = buildProtocolSpecificErrorResponse(protocolType, 401, userFriendlyMessage, errorMap, api);
                    rawResponseBody = getRawErrorResponseBody(finalResponse, protocolType);

                    if (capturedRequestId != null) {
                        try {
                            apiRequestService.updateRequestWithRawResponse(
                                    requestId, capturedRequestId, 401,
                                    userFriendlyMessage, System.currentTimeMillis() - startTime, rawResponseBody);
                        } catch (Exception ex) {
                            log.error("Failed to update captured request with error: {}", ex.getMessage());
                        }
                    }

                    executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                            null, 401, System.currentTimeMillis() - startTime,
                            performedBy, actualClientIp, userAgent, authResult.getReason(), objectMapper);

                    return finalResponse;
                }
            } catch (Exception e) {
                log.error("Authentication validation error: {}", e.getMessage(), e);

                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("code", "AUTHENTICATION_SYSTEM_ERROR");
                errorMap.put("technicalMessage", e.getMessage());
                errorMap.put("errorType", e.getClass().getSimpleName());
                errorMap.put("apiId", apiId);
                errorMap.put("timestamp", LocalDateTime.now().toString());

                finalResponse = buildProtocolSpecificErrorResponse(protocolType, 500,
                        "Unable to process your request at this time. Please try again later.", errorMap, api);
                rawResponseBody = getRawErrorResponseBody(finalResponse, protocolType);

                if (capturedRequestId != null) {
                    try {
                        apiRequestService.updateRequestWithRawResponse(
                                requestId, capturedRequestId, 500,
                                finalResponse.getMessage(), System.currentTimeMillis() - startTime, rawResponseBody);
                    } catch (Exception ex) {
                        log.error("Failed to update captured request with error: {}", ex.getMessage());
                    }
                }

                return finalResponse;
            }

            // 13. Get all API parameters
            List<ApiParameterEntity> apiParameters = api.getParameters();
            log.info("API parameter definitions for database: {}", databaseType);
            apiParameters.forEach(p ->
                    log.info("  - {}: type={}, location={}, required={}",
                            p.getKey(), p.getParameterType(), p.getParameterLocation(), p.getRequired()));

            // 14. Create consolidated params
            Map<String, Object> consolidatedParams = createConsolidatedParamsWithHeaders(validatedRequest, api);

            if ("graphql".equalsIgnoreCase(protocolType)) {
                // Get the extracted GraphQL variables
                if (validatedRequest.getBody() instanceof Map) {
                    Map<String, Object> bodyMap = (Map<String, Object>) validatedRequest.getBody();
                    Map<String, Object> graphqlVars = extractGraphQLVariables(bodyMap);

                    if (!graphqlVars.isEmpty()) {
                        // IMPORTANT: Add extracted variables to the request body
                        bodyMap.putAll(graphqlVars);

                        // Also add to validatedRequest's body as a Map for consistency
                        if (validatedRequest.getBody() instanceof Map) {
                            ((Map<String, Object>) validatedRequest.getBody()).putAll(graphqlVars);
                        }

                        // Also store them as query params if they're query parameters
                        for (Map.Entry<String, Object> entry : graphqlVars.entrySet()) {
                            String paramKey = entry.getKey();
                            Object paramValue = entry.getValue();

                            // Check if this parameter is defined as a query parameter
                            boolean isQueryParam = api.getParameters().stream()
                                    .anyMatch(p -> p.getKey().equals(paramKey) &&
                                            "query".equalsIgnoreCase(p.getParameterLocation()));

                            if (isQueryParam) {
                                if (validatedRequest.getQueryParams() == null) {
                                    validatedRequest.setQueryParams(new HashMap<>());
                                }
                                validatedRequest.getQueryParams().put(paramKey, paramValue);
                                log.info("Added GraphQL variable '{}' to query params: {}", paramKey, paramValue);
                            }
                        }

                        log.info("Added GraphQL variables to request body: {}", graphqlVars.keySet());
                    }
                }
            }

            // 15. Parse XML body if present (for SOAP) - but keep original for storage
            if (validatedRequest.getBody() instanceof String) {
                String rawBody = (String) validatedRequest.getBody();
                if (rawBody.trim().startsWith("<")) {
                    log.info("XML body detected, parsing to extract parameters");
                    Map<String, Object> xmlParams = parseXmlParameters(rawBody, api.getParameters());

                    if (validatedRequest.getBody() == null) {
                        validatedRequest.setBody(new HashMap<>());
                    }

                    if (validatedRequest.getBody() instanceof Map) {
                        ((Map<String, Object>) validatedRequest.getBody()).putAll(xmlParams);
                        log.info("Added {} extracted XML parameters to request body", xmlParams.size());
                    } else {
                        Map<String, Object> newBody = new HashMap<>();
                        newBody.putAll(xmlParams);
                        validatedRequest.setBody(newBody);
                        log.info("Replaced body with extracted XML parameters: {}", xmlParams.keySet());
                    }

                    consolidatedParams.putAll(xmlParams);
                }
            }

            // 16. Auto-add Content-Type header if missing
            boolean contentTypeRequired = apiParameters.stream()
                    .anyMatch(p -> "Content-Type".equalsIgnoreCase(p.getKey()) && p.getRequired());

            boolean contentTypeMissing = !consolidatedParams.containsKey("Content-Type") &&
                    !consolidatedParams.containsKey("content-type");

            if (contentTypeRequired && contentTypeMissing) {
                String contentTypeValue = "application/json";
                consolidatedParams.put("Content-Type", contentTypeValue);
                if (validatedRequest.getHeaders() == null) {
                    validatedRequest.setHeaders(new HashMap<>());
                }
                validatedRequest.getHeaders().put("Content-Type", contentTypeValue);
            }

            // 17. Validate required parameters
            Map<String, String> validationErrors = validateRequiredParametersEnhanced(api, consolidatedParams, validatedRequest);

            if (!validationErrors.isEmpty()) {
                String missingParams = String.join(", ", validationErrors.keySet());
                String userFriendlyMessage = "Missing required information: " + missingParams + ". Please provide all required fields and try again.";

                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("code", "MISSING_REQUIRED_PARAMETERS");
                errorMap.put("technicalMessage", String.format("Required parameters missing: %s", missingParams));
                errorMap.put("missingParameters", validationErrors);
                errorMap.put("apiId", apiId);
                errorMap.put("timestamp", LocalDateTime.now().toString());

                finalResponse = buildProtocolSpecificErrorResponse(protocolType, 400, userFriendlyMessage, errorMap, api);
                rawResponseBody = getRawErrorResponseBody(finalResponse, protocolType);

                if (capturedRequestId != null) {
                    try {
                        apiRequestService.updateRequestWithRawResponse(
                                requestId, capturedRequestId, 400,
                                userFriendlyMessage, System.currentTimeMillis() - startTime, rawResponseBody);
                    } catch (Exception ex) {
                        log.error("Failed to update captured request with error: {}", ex.getMessage());
                    }
                }

                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, 400, System.currentTimeMillis() - startTime,
                        performedBy, actualClientIp, userAgent, "Missing parameters: " + missingParams, objectMapper);

                return finalResponse;
            }

            // 18. Authorization check
            if (!validatorService.validateAuthorization(api, performedBy)) {
                String userFriendlyMessage = "You don't have permission to access this service. Please contact your administrator.";

                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("code", "AUTHORIZATION_FAILED");
                errorMap.put("technicalMessage", String.format("User '%s' not authorized to access API '%s'", performedBy, apiId));
                errorMap.put("userId", performedBy);
                errorMap.put("apiId", apiId);
                errorMap.put("apiName", api.getApiName());
                errorMap.put("apiCode", api.getApiCode());
                errorMap.put("owner", api.getOwner());
                errorMap.put("timestamp", LocalDateTime.now().toString());

                finalResponse = buildProtocolSpecificErrorResponse(protocolType, 403, userFriendlyMessage, errorMap, api);
                rawResponseBody = getRawErrorResponseBody(finalResponse, protocolType);

                if (capturedRequestId != null) {
                    try {
                        apiRequestService.updateRequestWithRawResponse(
                                requestId, capturedRequestId, 403,
                                userFriendlyMessage, System.currentTimeMillis() - startTime, rawResponseBody);
                    } catch (Exception ex) {
                        log.error("Failed to update captured request with error: {}", ex.getMessage());
                    }
                }

                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, 403, System.currentTimeMillis() - startTime,
                        performedBy, actualClientIp, userAgent, "Authorization failed", objectMapper);

                return finalResponse;
            }

            // 19. Rate limiting check
            Integer rateLimit = null;
            if (rateLimit != null && rateLimit > 0) {
                if (!validatorService.checkRateLimit(api, actualClientIp)) {
                    String userFriendlyMessage = "Too many requests. Please wait a moment and try again.";

                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("code", "RATE_LIMIT_EXCEEDED");
                    errorMap.put("technicalMessage", String.format("Rate limit exceeded for IP: %s", actualClientIp));
                    errorMap.put("clientIp", actualClientIp);
                    errorMap.put("apiId", apiId);
                    errorMap.put("rateLimit", rateLimit);
                    errorMap.put("timestamp", LocalDateTime.now().toString());

                    finalResponse = buildProtocolSpecificErrorResponse(protocolType, 429, userFriendlyMessage, errorMap, api);
                    rawResponseBody = getRawErrorResponseBody(finalResponse, protocolType);

                    if (capturedRequestId != null) {
                        try {
                            apiRequestService.updateRequestWithRawResponse(
                                    requestId, capturedRequestId, 429,
                                    userFriendlyMessage, System.currentTimeMillis() - startTime, rawResponseBody);
                        } catch (Exception ex) {
                            log.error("Failed to update captured request with error: {}", ex.getMessage());
                        }
                    }

                    executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                            null, 429, System.currentTimeMillis() - startTime,
                            performedBy, actualClientIp, userAgent, "Rate limit exceeded", objectMapper);

                    return finalResponse;
                }
            }

            // 20. Extract source object from API
            ApiSourceObjectDTO sourceObject = conversionHelper.extractSourceObject(api, objectMapper);

            // 21. Convert parameters to DTOs for execution
            List<ApiParameterDTO> configuredParamDTOs = conversionHelper.convertParametersToDTOs(api.getParameters());

            // 22. Execute against the appropriate database
            Object result;
            long executionTime;

            try {
                log.info("Executing against database: {}", databaseType);

                result = executionHelper.executeAgainstDatabase(api, sourceObject, validatedRequest, configuredParamDTOs);

                executionTime = System.currentTimeMillis() - startTime;

                // ============ BUILD RESPONSE AND CAPTURE RAW BODY ============

                if ("soap".equalsIgnoreCase(protocolType)) {
                    // Build raw SOAP XML response
                    rawResponseBody = buildRawSoapResponse(result, api);

                    finalResponse = ExecuteApiResponseDTO.builder()
                            .responseCode(200)
                            .success(true)
                            .message("Success")
                            .data(rawResponseBody)  // Return raw XML directly
                            .contentType("application/xml")
                            .protocolType(protocolType)
                            .build();

                    log.info("SOAP API - captured raw XML response ({} chars)", rawResponseBody.length());

                } else if ("graphql".equalsIgnoreCase(protocolType)) {
                    // Build raw GraphQL JSON response
                    rawResponseBody = buildRawGraphQLResponse(result, api);

                    finalResponse = ExecuteApiResponseDTO.builder()
                            .responseCode(200)
                            .success(true)
                            .message("Success")
                            .data(rawResponseBody)  // Return raw JSON directly
                            .contentType("application/json")
                            .protocolType(protocolType)
                            .build();

                    log.info("GraphQL API - captured raw JSON response ({} chars)", rawResponseBody.length());

                } else {
                    // REST API - build raw JSON response exactly as user will see
                    rawResponseBody = buildRawRestResponse(result, api, 200, "Success");

                    Object formattedData = responseHelper.formatResponse(api, result);
                    finalResponse = ExecuteApiResponseDTO.builder()
                            .responseCode(200)
                            .success(true)
                            .message("Success")
                            .data(formattedData)
                            .contentType("application/json")
                            .protocolType(protocolType)
                            .build();

                    log.info("REST API - captured raw JSON response ({} chars)", rawResponseBody.length());
                }

                // Update captured request with EXACT raw request and response bodies
                if (capturedRequestId != null) {
                    try {
                        apiRequestService.updateRequestWithRawRequestAndResponse(
                                requestId, capturedRequestId,
                                rawRequestBody,   // Exact raw request as sent
                                rawResponseBody,  // Exact raw response as received
                                200, "Success", executionTime);
                    } catch (Exception e) {
                        log.error("Failed to update captured request with raw data: {}", e.getMessage(), e);
                    }
                }

                executionHelper.updateApiStats(api, generatedAPIRepository);
                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        result, 200, executionTime, performedBy,
                        actualClientIp, userAgent, null, objectMapper);

                loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                        ", API executed successfully: " + apiId +
                        " on database: " + databaseType +
                        " with protocol: " + protocolType +
                        " - Time: " + executionTime + "ms");

                return finalResponse;

            } catch (Exception e) {
                executionTime = System.currentTimeMillis() - startTime;
                log.error("Database execution failed for {}: ", databaseType, e);

                String technicalError = extractDatabaseError(e, databaseType);
                String userFriendlyMessage = "Unable to process your request at this time. Please try again later.";

                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("code", "DATABASE_EXECUTION_ERROR");
                errorMap.put("technicalMessage", technicalError);
                errorMap.put("databaseType", databaseType);
                errorMap.put("apiId", apiId);
                errorMap.put("sqlOperationType", api.getSqlOperationType());
                errorMap.put("executionTimeMs", executionTime);
                errorMap.put("timestamp", LocalDateTime.now().toString());

                finalResponse = buildProtocolSpecificErrorResponse(protocolType, 500, userFriendlyMessage, errorMap, api);
                rawResponseBody = getRawErrorResponseBody(finalResponse, protocolType);

                if (capturedRequestId != null) {
                    try {
                        apiRequestService.updateRequestWithRawResponse(
                                requestId, capturedRequestId, 500,
                                userFriendlyMessage, executionTime, rawResponseBody);
                    } catch (Exception updateError) {
                        log.error("Failed to update captured request: {}", updateError.getMessage());
                    }
                }

                try {
                    String logErrorMessage = truncateErrorMessage(technicalError, 1000);
                    executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                            null, 500, executionTime, performedBy, actualClientIp, userAgent,
                            logErrorMessage, objectMapper);
                } catch (Exception logError) {
                    log.error("Failed to log execution error: {}", logError.getMessage());
                }

                return finalResponse;
            }

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                    ", Error executing API: " + e.getMessage());
            log.error("Error executing API: ", e);

            String technicalError = extractDatabaseError(e, databaseType);
            String userFriendlyMessage = "An unexpected error occurred. Please try again later or contact support.";

            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("code", "GENERAL_ERROR");
            errorMap.put("technicalMessage", technicalError);
            errorMap.put("errorType", e.getClass().getSimpleName());
            errorMap.put("apiId", apiId);
            errorMap.put("timestamp", LocalDateTime.now().toString());

            GeneratedApiEntity api = null;
            try {
                api = generatedAPIRepository.findById(apiId).orElse(null);
            } catch (Exception ex) {
                // Ignore
            }

            String finalProtocolType = protocolType;
            if (api != null && api.getProtocolType() != null) {
                finalProtocolType = api.getProtocolType();
            }

            finalResponse = buildProtocolSpecificErrorResponse(finalProtocolType, 500, userFriendlyMessage, errorMap, api);
            rawResponseBody = getRawErrorResponseBody(finalResponse, finalProtocolType);

            if (capturedRequestId != null) {
                try {
                    apiRequestService.updateRequestWithRawResponse(
                            requestId, capturedRequestId, 500,
                            userFriendlyMessage, executionTime, rawResponseBody);
                } catch (Exception updateError) {
                    log.error("Failed to update captured request: {}", updateError.getMessage());
                }
            }

            return finalResponse;
        }
    }

    /**
     * Get raw request body from validated request - PRESERVE EXACTLY AS SENT
     */
    private String getRawRequestBody(ExecuteApiRequestDTO validatedRequest, String protocolType) {
        if (validatedRequest.getBody() == null) {
            return null;
        }

        // For SOAP - XML is stored as String, return EXACTLY as received
        if (validatedRequest.getBody() instanceof String) {
            String rawBody = (String) validatedRequest.getBody();
            log.info("Capturing raw SOAP request body ({} chars)", rawBody.length());
            return rawBody;
        }

        // For GraphQL or REST JSON
        try {
            String jsonBody = objectMapper.writeValueAsString(validatedRequest.getBody());
            log.info("Capturing raw JSON request body ({} chars)", jsonBody.length());
            return jsonBody;
        } catch (Exception e) {
            log.warn("Failed to convert request body to JSON string: {}", e.getMessage());
            return validatedRequest.getBody().toString();
        }
    }


    /**
     * Get raw error response body based on protocol
     * This returns the COMPLETE error response exactly as the user will see it
     */
    private String getRawErrorResponseBody(ExecuteApiResponseDTO response, String protocolType) {
        try {
            if ("soap".equalsIgnoreCase(protocolType)) {
                // For SOAP, the data field contains the raw XML
                if (response.getData() instanceof String) {
                    return (String) response.getData();
                }
                return response.getData() != null ? response.getData().toString() : "";

            } else if ("graphql".equalsIgnoreCase(protocolType)) {
                // For GraphQL, build the complete error response
                Map<String, Object> graphqlError = new LinkedHashMap<>();
                List<Map<String, Object>> errors = new ArrayList<>();
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("message", response.getMessage());
                if (response.getError() != null) {
                    Map<String, Object> extensions = new LinkedHashMap<>();
                    extensions.putAll(response.getError());
                    error.put("extensions", extensions);
                }
                error.put("locations", new ArrayList<>());
                error.put("path", new ArrayList<>());
                errors.add(error);
                graphqlError.put("errors", errors);
                graphqlError.put("data", null);
                return objectMapper.writeValueAsString(graphqlError);

            } else {
                // REST API - build the COMPLETE error response object with ALL fields
                Map<String, Object> restError = new LinkedHashMap<>();
                restError.put("data", null);  // IMPORTANT: Add data field with null
                restError.put("success", response.getSuccess());
                restError.put("message", response.getMessage());
                restError.put("responseCode", response.getResponseCode());
                restError.put("contentType", "application/json");
                restError.put("protocolType", "rest");
                if (response.getError() != null) {
                    restError.put("error", response.getError());
                } else {
                    restError.put("error", null);
                }
                return objectMapper.writeValueAsString(restError);
            }
        } catch (Exception e) {
            log.warn("Failed to serialize error response: {}", e.getMessage());
            return String.format("{\"data\":null,\"success\":false,\"message\":\"%s\",\"responseCode\":%d,\"contentType\":\"application/json\",\"protocolType\":\"rest\",\"error\":null}",
                    response.getMessage(), response.getResponseCode());
        }
    }

    /**
     * Build protocol-specific error response
     * - SOAP: Returns XML SOAP envelope (entire response in data field)
     * - GraphQL: Returns GraphQL error format (entire response in data field)
     * - REST: Returns the ExecuteApiResponseDTO directly (no extra nesting)
     */
    private ExecuteApiResponseDTO buildProtocolSpecificErrorResponse(
            String protocolType, int statusCode, String message,
            Map<String, Object> errorDetails, GeneratedApiEntity api) {

        if ("soap".equalsIgnoreCase(protocolType)) {
            String soapEnvelope = buildSoapErrorResponse(message, errorDetails, api);
            log.info("SOAP error response - returning SOAP envelope in data field");

            return ExecuteApiResponseDTO.builder()
                    .responseCode(statusCode)
                    .success(false)
                    .message(message)
                    .data(soapEnvelope)
                    .error(errorDetails)
                    .protocolType(protocolType)
                    .contentType("application/xml")
                    .build();

        } else if ("graphql".equalsIgnoreCase(protocolType)) {
            Map<String, Object> graphqlError = buildGraphQLErrorResponse(message, errorDetails);
            log.info("GraphQL error response - returning GraphQL error in data field");

            return ExecuteApiResponseDTO.builder()
                    .responseCode(statusCode)
                    .success(false)
                    .message(message)
                    .data(graphqlError)
                    .error(errorDetails)
                    .protocolType(protocolType)
                    .contentType("application/json")
                    .build();

        } else {
            // REST API - return the ExecuteApiResponseDTO directly as the response
            // No extra wrapping - the ExecuteApiResponseDTO itself is the response
            log.info("REST error response - returning ExecuteApiResponseDTO directly");

            return ExecuteApiResponseDTO.builder()
                    .responseCode(statusCode)
                    .success(false)
                    .message(message)
                    .data(null)  // data is null for REST errors
                    .error(errorDetails)
                    .protocolType(protocolType)
                    .contentType("application/json")
                    .build();
        }
    }

    /**
     * Build SOAP error response envelope
     */
    private String buildSoapErrorResponse(String message, Map<String, Object> errorDetails, GeneratedApiEntity api) {
        try {
            SoapConfigDTO soapConfig = api != null ? api.getSoapConfig() : null;
            String soapVersion = soapConfig != null && soapConfig.getVersion() != null ? soapConfig.getVersion() : "1.1";
            String soapNamespace = soapConfig != null && soapConfig.getNamespace() != null ? soapConfig.getNamespace() : "http://tempuri.org/";
            String operationName = api != null && api.getApiCode() != null ? api.getApiCode() : "ErrorResponse";

            StringBuilder soapResponse = new StringBuilder();

            soapResponse.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

            if ("1.2".equals(soapVersion)) {
                soapResponse.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n");
            } else {
                soapResponse.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n");
            }

            soapResponse.append("  <soap:Header/>\n");
            soapResponse.append("  <soap:Body>\n");
            soapResponse.append("    <soap:Fault>\n");
            soapResponse.append("      <faultcode>soap:Client</faultcode>\n");
            soapResponse.append("      <faultstring>").append(escapeXml(message)).append("</faultstring>\n");
            soapResponse.append("      <detail>\n");
            soapResponse.append("        <").append(operationName).append("Error xmlns=\"").append(soapNamespace).append("\">\n");

            if (errorDetails != null) {
                for (Map.Entry<String, Object> entry : errorDetails.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    soapResponse.append("          <").append(key).append(">");
                    if (value instanceof Map) {
                        soapResponse.append("\n");
                        for (Map.Entry<String, Object> subEntry : ((Map<String, Object>) value).entrySet()) {
                            soapResponse.append("            <").append(subEntry.getKey()).append(">");
                            soapResponse.append(escapeXml(String.valueOf(subEntry.getValue())));
                            soapResponse.append("</").append(subEntry.getKey()).append(">\n");
                        }
                        soapResponse.append("          ");
                    } else {
                        soapResponse.append(escapeXml(String.valueOf(value)));
                    }
                    soapResponse.append("</").append(key).append(">\n");
                }
            }

            soapResponse.append("        </").append(operationName).append("Error>\n");
            soapResponse.append("      </detail>\n");
            soapResponse.append("    </soap:Fault>\n");
            soapResponse.append("  </soap:Body>\n");
            soapResponse.append("</soap:Envelope>");

            return soapResponse.toString();

        } catch (Exception e) {
            log.error("Error building SOAP error response: {}", e.getMessage(), e);
            // Fallback simple SOAP error
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                    "  <soap:Body>\n" +
                    "    <soap:Fault>\n" +
                    "      <faultcode>soap:Server</faultcode>\n" +
                    "      <faultstring>" + escapeXml(message) + "</faultstring>\n" +
                    "    </soap:Fault>\n" +
                    "  </soap:Body>\n" +
                    "</soap:Envelope>";
        }
    }

    /**
     * Build GraphQL error response
     */
    private Map<String, Object> buildGraphQLErrorResponse(String message, Map<String, Object> errorDetails) {
        Map<String, Object> graphqlResponse = new LinkedHashMap<>();

        List<Map<String, Object>> errors = new ArrayList<>();
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("message", message);
        error.put("locations", new ArrayList<>());
        error.put("path", new ArrayList<>());

        if (errorDetails != null) {
            Map<String, Object> extensions = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : errorDetails.entrySet()) {
                extensions.put(entry.getKey(), entry.getValue());
            }
            error.put("extensions", extensions);
        }

        errors.add(error);
        graphqlResponse.put("errors", errors);
        graphqlResponse.put("data", null);

        return graphqlResponse;
    }

    /**
     * Build REST error response (JSON) - data is null, error details in error field only
     */
    private Map<String, Object> buildRestErrorResponse(int statusCode, String message, Map<String, Object> errorDetails) {
        Map<String, Object> response = new LinkedHashMap<>();

        // For REST, data is always null on error
        response.put("data", null);
        response.put("success", false);
        response.put("message", message);
        response.put("responseCode", statusCode);
        response.put("contentType", "application/json");
        response.put("protocolType", "rest");

        if (errorDetails != null && !errorDetails.isEmpty()) {
            response.put("error", errorDetails);
        }

        return response;
    }


    /**
     * Format response as SOAP XML
     */
    private Object formatAsSoapResponse(Object result, GeneratedApiEntity api) {
        try {
            SoapConfigDTO soapConfig = api.getSoapConfig();
            String soapVersion = soapConfig != null && soapConfig.getVersion() != null ? soapConfig.getVersion() : "1.1";
            String soapNamespace = soapConfig != null && soapConfig.getNamespace() != null ? soapConfig.getNamespace() : "http://tempuri.org/";

            // Get the operation name from the API
            String operationName = api.getApiCode() != null ? api.getApiCode() : "Response";

            StringBuilder soapResponse = new StringBuilder();

            // SOAP Envelope header
            soapResponse.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

            if ("1.2".equals(soapVersion)) {
                soapResponse.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n");
            } else {
                soapResponse.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n");
            }

            soapResponse.append("  <soap:Header/>\n");
            soapResponse.append("  <soap:Body>\n");
            soapResponse.append("    <").append(operationName).append("Response xmlns=\"").append(soapNamespace).append("\">\n");

            // Convert result to XML
            String xmlContent = convertToXml(result, api);
            soapResponse.append(xmlContent);

            soapResponse.append("    </").append(operationName).append("Response>\n");
            soapResponse.append("  </soap:Body>\n");
            soapResponse.append("</soap:Envelope>");

            log.info("Generated SOAP XML response of length: {} characters", soapResponse.length());
            return soapResponse.toString();

        } catch (Exception e) {
            log.error("Error formatting SOAP response: {}", e.getMessage(), e);

            // Return error SOAP response
            StringBuilder errorResponse = new StringBuilder();
            errorResponse.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            errorResponse.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n");
            errorResponse.append("  <soap:Body>\n");
            errorResponse.append("    <soap:Fault>\n");
            errorResponse.append("      <faultcode>soap:Server</faultcode>\n");
            errorResponse.append("      <faultstring>").append(escapeXml(e.getMessage())).append("</faultstring>\n");
            errorResponse.append("    </soap:Fault>\n");
            errorResponse.append("  </soap:Body>\n");
            errorResponse.append("</soap:Envelope>");
            return errorResponse.toString();
        }
    }

    /**
     * Format response as GraphQL
     */
    private Object formatAsGraphQLResponse(Object result, GeneratedApiEntity api) {
        Map<String, Object> graphqlResponse = new LinkedHashMap<>();

        try {
            GraphQLConfigDTO graphqlConfig = api.getGraphqlConfig();

            // Check if there's an error
            if (result instanceof Map && ((Map<?, ?>) result).containsKey("error")) {
                Map<String, Object> errorMap = new LinkedHashMap<>();
                errorMap.put("message", ((Map<?, ?>) result).get("error"));
                errorMap.put("locations", new ArrayList<>());
                errorMap.put("path", new ArrayList<>());
                graphqlResponse.put("errors", Collections.singletonList(errorMap));
            } else {
                // Success response
                Map<String, Object> data = new LinkedHashMap<>();
                String operationName = graphqlConfig != null && graphqlConfig.getOperationName() != null ?
                        graphqlConfig.getOperationName() : "data";

                // Extract data from result
                if (result instanceof Map) {
                    Map<String, Object> resultMap = (Map<String, Object>) result;

                    // Check if result already has a 'data' field
                    if (resultMap.containsKey("data")) {
                        data.put(operationName, resultMap.get("data"));
                    } else if (resultMap.containsKey("result")) {
                        data.put(operationName, resultMap.get("result"));
                    } else {
                        data.put(operationName, resultMap);
                    }
                } else if (result instanceof List) {
                    data.put(operationName, result);
                } else {
                    data.put(operationName, result);
                }

                graphqlResponse.put("data", data);
            }

            return graphqlResponse;

        } catch (Exception e) {
            log.error("Error formatting GraphQL response: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new LinkedHashMap<>();
            Map<String, Object> errorMap = new LinkedHashMap<>();
            errorMap.put("message", e.getMessage());
            errorMap.put("locations", new ArrayList<>());
            errorMap.put("path", new ArrayList<>());
            errorResponse.put("errors", Collections.singletonList(errorMap));
            return errorResponse;
        }
    }

    /**
     * Convert result object to XML format
     */
    private String convertToXml(Object result, GeneratedApiEntity api) {
        StringBuilder xml = new StringBuilder();

        try {
            if (result == null) {
                xml.append("      <result>null</result>\n");
                return xml.toString();
            }

            if (result instanceof Map) {
                Map<String, Object> resultMap = (Map<String, Object>) result;
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    // Skip internal fields
                    if ("responseCode".equals(key) || "code".equals(key) || "status".equals(key) ||
                            "message".equals(key) || "msg".equals(key)) {
                        continue;
                    }

                    xml.append("      <").append(key).append(">");
                    if (value instanceof Map || value instanceof List) {
                        xml.append("\n");
                        xml.append(convertToXml(value, api));
                        xml.append("      ");
                    } else {
                        xml.append(escapeXml(String.valueOf(value)));
                    }
                    xml.append("</").append(key).append(">\n");
                }
            } else if (result instanceof List) {
                List<?> resultList = (List<?>) result;
                for (Object item : resultList) {
                    if (item instanceof Map) {
                        xml.append("      <item>\n");
                        for (Map.Entry<String, Object> entry : ((Map<String, Object>) item).entrySet()) {
                            String key = entry.getKey();
                            Object value = entry.getValue();
                            xml.append("        <").append(key).append(">");
                            xml.append(escapeXml(String.valueOf(value)));
                            xml.append("</").append(key).append(">\n");
                        }
                        xml.append("      </item>\n");
                    } else {
                        xml.append("      <item>").append(escapeXml(String.valueOf(item))).append("</item>\n");
                    }
                }
            } else {
                xml.append("      <result>").append(escapeXml(String.valueOf(result))).append("</result>\n");
            }

        } catch (Exception e) {
            log.error("Error converting to XML: {}", e.getMessage(), e);
            xml.append("      <error>").append(escapeXml(e.getMessage())).append("</error>\n");
        }

        return xml.toString();
    }

    /**
     * Escape XML special characters
     */
    private String escapeXml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }


    /**
     * Execute PostgreSQL table operation
     */
    private Object executePostgreSQLTable(String tableName, String schema, String operation,
                                          Map<String, Object> params, GeneratedApiEntity api,
                                          List<ApiParameterDTO> configuredParamDTOs) {

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


    /**
     * Build raw SOAP XML response exactly as it should be returned to the client
     */
    private String buildRawSoapResponse(Object result, GeneratedApiEntity api) {
        try {
            SoapConfigDTO soapConfig = api.getSoapConfig();
            String soapVersion = soapConfig != null && soapConfig.getVersion() != null ? soapConfig.getVersion() : "1.1";
            String soapNamespace = soapConfig != null && soapConfig.getNamespace() != null ? soapConfig.getNamespace() : "http://tempuri.org/";
            String operationName = api.getApiCode() != null ? api.getApiCode() : "Response";

            StringBuilder soapResponse = new StringBuilder();

            // XML Declaration
            soapResponse.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

            // SOAP Envelope
            if ("1.2".equals(soapVersion)) {
                soapResponse.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n");
            } else {
                soapResponse.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n");
            }

            // SOAP Header
            soapResponse.append("  <soap:Header/>\n");

            // SOAP Body
            soapResponse.append("  <soap:Body>\n");
            soapResponse.append("    <").append(operationName).append("Response xmlns=\"").append(soapNamespace).append("\">\n");

            // Convert result to raw XML
            String xmlContent = convertResultToRawXml(result);
            soapResponse.append(xmlContent);

            soapResponse.append("    </").append(operationName).append("Response>\n");
            soapResponse.append("  </soap:Body>\n");
            soapResponse.append("</soap:Envelope>");

            return soapResponse.toString();

        } catch (Exception e) {
            log.error("Error building raw SOAP response: {}", e.getMessage(), e);
            // Return error SOAP response
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                    "  <soap:Body>\n" +
                    "    <soap:Fault>\n" +
                    "      <faultcode>soap:Server</faultcode>\n" +
                    "      <faultstring>" + escapeXml(e.getMessage()) + "</faultstring>\n" +
                    "    </soap:Fault>\n" +
                    "  </soap:Body>\n" +
                    "</soap:Envelope>";
        }
    }

    /**
     * Convert result object to raw XML string (preserves exact structure)
     */
    private String convertResultToRawXml(Object result) {
        StringBuilder xml = new StringBuilder();

        try {
            if (result == null) {
                xml.append("        <result>null</result>\n");
                return xml.toString();
            }

            if (result instanceof Map) {
                Map<String, Object> resultMap = (Map<String, Object>) result;
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    xml.append("        <").append(key).append(">");
                    if (value instanceof Map || value instanceof List) {
                        xml.append("\n");
                        xml.append(convertResultToRawXml(value));
                        xml.append("        ");
                    } else if (value != null) {
                        xml.append(escapeXml(value.toString()));
                    }
                    xml.append("</").append(key).append(">\n");
                }
            } else if (result instanceof List) {
                List<?> resultList = (List<?>) result;
                for (Object item : resultList) {
                    xml.append("        <item>\n");
                    if (item instanceof Map) {
                        for (Map.Entry<String, Object> entry : ((Map<String, Object>) item).entrySet()) {
                            String key = entry.getKey();
                            Object value = entry.getValue();
                            xml.append("          <").append(key).append(">");
                            xml.append(escapeXml(String.valueOf(value)));
                            xml.append("</").append(key).append(">\n");
                        }
                    } else {
                        xml.append("          <value>").append(escapeXml(String.valueOf(item))).append("</value>\n");
                    }
                    xml.append("        </item>\n");
                }
            } else {
                xml.append("        <result>").append(escapeXml(String.valueOf(result))).append("</result>\n");
            }

        } catch (Exception e) {
            log.error("Error converting result to raw XML: {}", e.getMessage(), e);
            xml.append("        <error>").append(escapeXml(e.getMessage())).append("</error>\n");
        }

        return xml.toString();
    }


    /**
     * Build raw REST response exactly as it should be returned to the client
     * This returns the COMPLETE response object, not just the data field
     */
    private String buildRawRestResponse(Object result, GeneratedApiEntity api, int statusCode, String message) {
        try {
            Map<String, Object> fullResponse = new LinkedHashMap<>();

            // Format the data part
            Object formattedData = responseHelper.formatResponse(api, result);

            // Build the complete response object exactly as the user will see it
            fullResponse.put("responseCode", statusCode);
            fullResponse.put("success", statusCode >= 200 && statusCode < 300);
            fullResponse.put("message", message);
            fullResponse.put("data", formattedData);
            fullResponse.put("contentType", "application/json");
            fullResponse.put("protocolType", "rest");
            fullResponse.put("error", null);

            // Return as JSON string
            return objectMapper.writeValueAsString(fullResponse);

        } catch (Exception e) {
            log.error("Error building raw REST response: {}", e.getMessage(), e);
            return String.format("{\"responseCode\":%d,\"success\":false,\"message\":\"%s\",\"data\":null,\"contentType\":\"application/json\",\"protocolType\":\"rest\",\"error\":null}",
                    statusCode, escapeJson(message));
        }
    }


    /**
     * Escape JSON special characters
     */
    private String escapeJson(String input) {
        if (input == null) return "";
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }



    /**
     * Build raw GraphQL JSON response exactly as it should be returned to the client
     */
    private String buildRawGraphQLResponse(Object result, GeneratedApiEntity api) {
        try {
            Map<String, Object> graphqlResponse = new LinkedHashMap<>();
            GraphQLConfigDTO graphqlConfig = api.getGraphqlConfig();

            Map<String, Object> data = new LinkedHashMap<>();
            String operationName = graphqlConfig != null && graphqlConfig.getOperationName() != null ?
                    graphqlConfig.getOperationName() : "data";

            // Extract data from result
            if (result instanceof Map) {
                Map<String, Object> resultMap = (Map<String, Object>) result;
                if (resultMap.containsKey("data")) {
                    data.put(operationName, resultMap.get("data"));
                } else if (resultMap.containsKey("result")) {
                    data.put(operationName, resultMap.get("result"));
                } else {
                    data.put(operationName, resultMap);
                }
            } else if (result instanceof List) {
                data.put(operationName, result);
            } else {
                data.put(operationName, result);
            }

            graphqlResponse.put("data", data);

            // Return as raw JSON string
            return objectMapper.writeValueAsString(graphqlResponse);

        } catch (Exception e) {
            log.error("Error building raw GraphQL response: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new LinkedHashMap<>();
            List<Map<String, Object>> errors = new ArrayList<>();
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("message", e.getMessage());
            error.put("locations", new ArrayList<>());
            error.put("path", new ArrayList<>());
            errors.add(error);
            errorResponse.put("errors", errors);
            errorResponse.put("data", null);

            try {
                return objectMapper.writeValueAsString(errorResponse);
            } catch (Exception ex) {
                return "{\"errors\":[{\"message\":\"" + e.getMessage() + "\"}]}";
            }
        }
    }


    /**
     * Extracts the actual database error without modification
     * This returns the raw error message that PostgreSQL or Oracle returns
     */
    private String extractDatabaseError(Exception e, String databaseType) {
        Throwable cause = e;
        while (cause != null) {
            String message = cause.getMessage();
            if (message != null) {
                // PostgreSQL-specific errors - return the raw error
                if (databaseType.equalsIgnoreCase("postgresql") || databaseType.equalsIgnoreCase("postgres")) {
                    if (message.contains("ERROR:")) {
                        // Return the complete PostgreSQL error line
                        Pattern pattern = Pattern.compile("ERROR:[^\\n]*");
                        Matcher matcher = pattern.matcher(message);
                        if (matcher.find()) {
                            String rawError = matcher.group();
                            log.info("Returning raw PostgreSQL error: {}", rawError);
                            return rawError;
                        }
                        return message;
                    }
                    // Handle PostgreSQL specific exceptions by checking class name
                    if (cause.getClass().getName().contains("PSQLException")) {
                        return cause.getMessage();
                    }
                }

                // Oracle-specific errors - return raw ORA error
                if (databaseType.equalsIgnoreCase("oracle") && message.contains("ORA-")) {
                    Pattern pattern = Pattern.compile("ORA-[0-9]{5}:[^\\n]*");
                    Matcher matcher = pattern.matcher(message);
                    if (matcher.find()) {
                        return matcher.group();
                    }
                    return message;
                }

                // Generic SQL Exception
                if (cause instanceof java.sql.SQLException) {
                    return cause.getMessage();
                }
            }
            cause = cause.getCause();
        }

        // If no specific error found, return the original message
        return e.getMessage() != null ? e.getMessage() : "Unknown database error";
    }


    /**
     * Extracts the full database error with all details
     */
    private String extractFullDatabaseError(Exception e) {
        StringBuilder fullError = new StringBuilder();
        Throwable cause = e;
        while (cause != null) {
            if (cause.getMessage() != null) {
                if (fullError.length() > 0) {
                    fullError.append("; ");
                }
                fullError.append(cause.getMessage());
            }
            cause = cause.getCause();
        }
        return fullError.toString();
    }

    /**
     * Extracts the detailed Oracle error message from exception chain
     */
    private String extractDetailedOracleError(Exception e) {
        Throwable cause = e;
        while (cause != null) {
            String message = cause.getMessage();
            if (message != null) {
                // Look for ORA-xxxxx pattern
                if (message.contains("ORA-")) {
                    return message;  // Return the full Oracle error
                }
                // Check for SQLException with Oracle error
                if (cause instanceof java.sql.SQLException) {
                    return message;
                }
            }
            cause = cause.getCause();
        }

        // Fallback to original message if no ORA error found
        return e.getMessage() != null ? e.getMessage() : "Unknown database error";
    }

    /**
     * Truncate error message to fit database column limits
     */
    private String truncateErrorMessage(String message, int maxLength) {
        if (message == null) {
            return null;
        }
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength - 3) + "...";
    }

    /**
     * Helper method to update captured request with error (with truncation for database)
     */
    private void updateCapturedRequestWithError(String requestId, String capturedRequestId,
                                                int statusCode, String errorMessage,
                                                long executionDurationMs) {
        try {
            // Truncate error message for database storage (255 chars max)
            String dbErrorMessage = truncateErrorMessage(errorMessage, 250);

            apiRequestService.updateRequestWithError(
                    requestId,
                    capturedRequestId,
                    statusCode,
                    dbErrorMessage,
                    executionDurationMs
            );
        } catch (Exception e) {
            log.error("Failed to update captured request with error: {}", e.getMessage());
            // Don't rethrow - this is a non-critical operation
        }
    }

    /**
     * Convert ExecuteApiRequestDTO to ApiRequestDTO
     */
    private ApiRequestDTO convertExecuteRequestToApiRequestDTO(ExecuteApiRequestDTO executeRequest, GeneratedApiEntity api) {
        ApiRequestDTO dto = new ApiRequestDTO();

        dto.setRequestName("Execution: " + api.getApiName());
        dto.setDescription("API execution request");
        dto.setHttpMethod(executeRequest.getHttpMethod());
        dto.setUrl(executeRequest.getUrl());
        dto.setBasePath(api.getBasePath());
        dto.setEndpointPath(api.getEndpointPath());
        dto.setRequestTimeoutSeconds(executeRequest.getTimeoutSeconds());

        // Set request components
        dto.setPathParameters(executeRequest.getPathParams());
        dto.setQueryParameters(executeRequest.getQueryParams());
        dto.setHeaders(executeRequest.getHeaders());
        dto.setRequestBody(executeRequest.getBody() instanceof Map ?
                (Map<String, Object>) executeRequest.getBody() : null);

        // Set correlation ID
        dto.setCorrelationId(executeRequest.getRequestId());

        // Set metadata
        dto.setMetadata(executeRequest.getMetadata());

        // Set auth type from API (will be populated from request if available)
        if (api.getAuthConfig() != null) {
            dto.setAuthType(api.getAuthConfig().getAuthType());
        }

        return dto;
    }

    /**
     * Validate HTTP method matches API configuration
     */
    private boolean validateHttpMethod(GeneratedApiEntity api, String requestMethod) {
        if (api == null || api.getHttpMethod() == null) {
            log.warn("API or HTTP method not configured");
            return false;
        }

        if (requestMethod == null || requestMethod.trim().isEmpty()) {
            log.warn("Request method is null or empty");
            return false;
        }

        String configuredMethod = api.getHttpMethod().toUpperCase().trim();
        String actualMethod = requestMethod.toUpperCase().trim();

        boolean isValid = configuredMethod.equals(actualMethod);

        if (!isValid) {
            log.warn("HTTP method mismatch. Configured: {}, Actual: {}", configuredMethod, actualMethod);
        } else {
            log.debug("HTTP method validation passed: {}", actualMethod);
        }

        return isValid;
    }



    @Transactional
    public ApiTestResultDTO testApi(String requestId, String performedBy,
                                    String apiId, ApiTestRequestDTO testRequest, HttpServletRequest req) {
        try {
            loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                    ", Testing API: " + apiId + " with test: " + testRequest.getTestName());

            // Get the API entity first
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // Get the database type
            String databaseType = api.getDatabaseType();
            if (databaseType == null || databaseType.isEmpty()) {
                databaseType = "oracle";
            }

            // Get the appropriate execution helper
            BaseApiExecutionHelper executionHelper = executionHelperFactory.getExecutionHelper(databaseType);

            long startTime = System.currentTimeMillis();

            ExecuteApiRequestDTO executeRequest = conversionHelper.createExecuteRequest(testRequest, requestId);

            ExecuteApiResponseDTO executionResult = executeApi(requestId, performedBy,
                    apiId, executeRequest, "127.0.0.1", "API-Test", req);

            long executionTime = System.currentTimeMillis() - startTime;

            boolean passed = responseHelper.compareResponses(executionResult, testRequest.getExpectedResponse(), objectMapper);

            // Save test result
            ApiTestEntity testEntity = executionHelper.saveTestResult(
                    apiTestRepository, api, testRequest, executionResult,
                    passed, executionTime, performedBy, objectMapper);

            return responseHelper.buildTestResult(testRequest, passed, executionTime,
                    executionResult.getResponseCode(), executionResult.getData());

        } catch (Exception e) {
            loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                    ", Error testing API: " + e.getMessage());
            throw new RuntimeException("Failed to test API: " + e.getMessage(), e);
        }
    }


    public GeneratedApiResponseDTO getApiDetails(String requestId, String apiId) {
        try {
            // Get the API entity first
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // Get the database type
            String databaseType = api.getDatabaseType();
            if (databaseType == null || databaseType.isEmpty()) {
                databaseType = "oracle";
            }

            // Get the appropriate execution helper
            BaseApiExecutionHelper executionHelper = executionHelperFactory.getExecutionHelper(databaseType);

            GeneratedApiResponseDTO response = conversionHelper.mapToResponse(api);

            response.setTotalCalls(api.getTotalCalls());
            response.setLastCalledAt(api.getLastCalledAt());

            Double avgTime = executionLogRepository.getAverageExecutionTime(apiId);
            if (avgTime != null) {
                ApiAnalyticsHelper analyticsHelper = databaseTypeFactory.getAnalyticsHelper(databaseType);
                analyticsHelper.addAverageExecutionTime(response, avgTime);
            }

            return response;

        } catch (Exception e) {
            loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                    ", Error getting API details: " + e.getMessage());
            throw new RuntimeException("Failed to get API details: " + e.getMessage(), e);
        }
    }



    public GeneratedAPIDTO getGeneratedApiDetails(String requestId, String apiId) {
        try {
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            String databaseType = api.getDatabaseType();
            if (databaseType == null || databaseType.isEmpty()) {
                databaseType = "oracle";
            }

            BaseApiExecutionHelper executionHelper = executionHelperFactory.getExecutionHelper(databaseType);
            GeneratedApiEntity apiEntity = executionHelper.getApiEntity(generatedAPIRepository, apiId);

            GeneratedAPIDTO response = conversionHelper.mapToGeneratedAPIDTO(apiEntity, objectMapper);

            // ============ CRITICAL: Add protocol fields ============
            response.setProtocolType(apiEntity.getProtocolType());

            // Convert SOAP config to Map if present
            if (apiEntity.getSoapConfig() != null) {
                response.setSoapConfig(convertSoapConfigToMap(apiEntity.getSoapConfig()));
            }

            // Convert GraphQL config to Map if present
            if (apiEntity.getGraphqlConfig() != null) {
                response.setGraphqlConfig(convertGraphQLConfigToMap(apiEntity.getGraphqlConfig()));
            }

            // Convert FileUpload config to Map if present
            if (apiEntity.getFileUploadConfig() != null) {
                response.setFileUploadConfig(convertFileUploadConfigToMap(apiEntity.getFileUploadConfig()));
            }
            // ============ END CRITICAL ============

            response.setTotalCalls(apiEntity.getTotalCalls());
            response.setLastCalledAt(apiEntity.getLastCalledAt());

            Double avgTime = executionLogRepository.getAverageExecutionTime(apiId);

            return response;

        } catch (Exception e) {
            loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                    ", Error getting API details: " + e.getMessage());
            throw new RuntimeException("Failed to get API details: " + e.getMessage(), e);
        }
    }

    // Helper methods to convert config objects to Map
    private Map<String, Object> convertSoapConfigToMap(SoapConfigDTO config) {
        if (config == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("version", config.getVersion());
        map.put("bindingStyle", config.getBindingStyle());
        map.put("encodingStyle", config.getEncodingStyle());
        map.put("soapAction", config.getSoapAction());
        map.put("wsdlUrl", config.getWsdlUrl());
        map.put("namespace", config.getNamespace());
        map.put("serviceName", config.getServiceName());
        map.put("portName", config.getPortName());
        map.put("useAsyncPattern", config.getUseAsyncPattern());
        map.put("includeMtom", config.getIncludeMtom());
        map.put("soapHeaderElements", config.getSoapHeaderElements());
        return map;
    }

    private Map<String, Object> convertGraphQLConfigToMap(GraphQLConfigDTO config) {
        if (config == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("operationType", config.getOperationType());
        map.put("operationName", config.getOperationName());
        map.put("schema", config.getSchema());
        map.put("enableIntrospection", config.getEnableIntrospection());
        map.put("enablePersistedQueries", config.getEnablePersistedQueries());
        map.put("maxQueryDepth", config.getMaxQueryDepth());
        map.put("enableBatching", config.getEnableBatching());
        map.put("subscriptionsEnabled", config.getSubscriptionsEnabled());
        map.put("customDirectives", config.getCustomDirectives());
        return map;
    }

    private Map<String, Object> convertFileUploadConfigToMap(FileUploadConfigDTO config) {
        if (config == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("maxFileSize", config.getMaxFileSize());
        map.put("allowedFileTypes", config.getAllowedFileTypes());
        map.put("multipleFiles", config.getMultipleFiles());
        map.put("fileParameterName", config.getFileParameterName());
        return map;
    }


    public ApiDetailsResponseDTO getCompleteApiDetails(String requestId, String apiId) {
        try {
            loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                    ", Fetching complete API details for: " + apiId);

            GeneratedApiEntity api = generatedAPIRepository.findByIdWithConfigs(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // Load all related entities
            List<ApiParameterEntity> parameters = generatedAPIRepository.findParametersByApiId(apiId);
            List<ApiResponseMappingEntity> responseMappings = generatedAPIRepository.findResponseMappingsByApiId(apiId);
            List<ApiHeaderEntity> headers = generatedAPIRepository.findHeadersByApiId(apiId);
            List<ApiTestEntity> tests = generatedAPIRepository.findTestsByApiId(apiId);

            api.setParameters(parameters);
            api.setResponseMappings(responseMappings);
            api.setHeaders(headers);
            api.setTests(tests);

            ApiDetailsResponseDTO response = conversionHelper.mapToApiDetailsResponse(api, objectMapper);

            // ============ CRITICAL: Add protocol fields ============
            if (api.getProtocolType() != null) {
                response.setProtocolType(api.getProtocolType());
            } else {
                response.setProtocolType("rest"); // Default
            }

            if (api.getSoapConfig() != null) {
                response.setSoapConfig(api.getSoapConfig());
            }

            if (api.getGraphqlConfig() != null) {
                response.setGraphqlConfig(api.getGraphqlConfig());
            }

            if (api.getFileUploadConfig() != null) {
                response.setFileUploadConfig(api.getFileUploadConfig());
            }
            // ============ END CRITICAL ============

            response.setGeneratedFiles(generateApiCode(api));
            response.setTotalCalls(api.getTotalCalls() != null ? api.getTotalCalls() : 0L);
            response.setLastCalledAt(api.getLastCalledAt());

            Double avgTime = executionLogRepository.getAverageExecutionTime(apiId);
            if (avgTime != null) {
                ApiAnalyticsHelper analyticsHelper = databaseTypeFactory.getAnalyticsHelper(api.getDatabaseType());
                analyticsHelper.addAverageExecutionTimeToDetails(response, avgTime);
            }

            return response;

        } catch (Exception e) {
            loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                    ", Error fetching complete API details: " + e.getMessage());
            throw new RuntimeException("Failed to fetch complete API details: " + e.getMessage(), e);
        }
    }



    public List<ApiExecutionLogDTO> getExecutionLogs(String apiId, LocalDateTime fromDate,
                                                     LocalDateTime toDate, int limit) {
        try {
            List<ApiExecutionLogEntity> logs;

            if (fromDate != null && toDate != null) {
                logs = executionLogRepository.findByGeneratedApiIdAndExecutedAtBetween(
                        apiId, fromDate, toDate);
            } else {
                PageRequest pageRequest = PageRequest.of(0, limit);
                logs = executionLogRepository.findByGeneratedApiIdOrderByExecutedAtDesc(apiId, pageRequest)
                        .getContent();
            }

            return logs.stream()
                    .map(conversionHelper::mapToExecutionLogDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting execution logs: {}", e.getMessage());
            throw new RuntimeException("Failed to get execution logs: " + e.getMessage(), e);
        }
    }


    public List<ApiTestResultDTO> getTestResults(String apiId) {
        try {
            List<ApiTestEntity> tests = apiTestRepository.findByGeneratedApiIdOrderByExecutedAtDesc(apiId);
            return tests.stream()
                    .map(conversionHelper::mapToTestResultDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting test results: {}", e.getMessage());
            throw new RuntimeException("Failed to get test results: " + e.getMessage(), e);
        }
    }


    @Transactional
    public GeneratedApiResponseDTO partialUpdateApi(String requestId, String apiId, String performedBy,
                                                    Map<String, Object> updates) {
        try {
            loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                    ", Partially updating API: " + apiId + " by: " + performedBy);

            // Get existing API
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // Apply partial updates
            if (updates.containsKey("apiName")) {
                api.setApiName((String) updates.get("apiName"));
            }
            if (updates.containsKey("description")) {
                api.setDescription((String) updates.get("description"));
            }
            if (updates.containsKey("version")) {
                api.setVersion((String) updates.get("version"));
            }
            if (updates.containsKey("status")) {
                String newStatus = (String) updates.get("status");
                List<String> validStatuses = Arrays.asList("DRAFT", "ACTIVE", "DEPRECATED", "ARCHIVED");
                if (!validStatuses.contains(newStatus)) {
                    throw new RuntimeException("Invalid status: " + newStatus);
                }
                api.setStatus(newStatus);
            }
            if (updates.containsKey("category")) {
                api.setCategory((String) updates.get("category"));
            }
            if (updates.containsKey("tags")) {
                api.setTags((List<String>) updates.get("tags"));
            }

            api.setUpdatedAt(LocalDateTime.now());
            api.setUpdatedBy(performedBy);

            // Save updated API
            GeneratedApiEntity savedApi = generatedAPIRepository.save(api);

            loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                    ", API partially updated successfully: " + savedApi.getId());

            return conversionHelper.mapToResponse(savedApi);

        } catch (Exception e) {
            loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                    ", Error partially updating API: " + e.getMessage());
            throw new RuntimeException("Failed to partially update API: " + e.getMessage(), e);
        }
    }


    @Transactional
    public GeneratedApiResponseDTO updateApiStatus(String apiId, String status, String performedBy) {
        try {
            // Get the API entity first
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // Get the database type
            String databaseType = api.getDatabaseType();
            if (databaseType == null || databaseType.isEmpty()) {
                databaseType = "oracle";
            }

            // Get the appropriate execution helper (not really needed for status update, but for consistency)
            BaseApiExecutionHelper executionHelper = executionHelperFactory.getExecutionHelper(databaseType);

            // Get the appropriate validation helper based on the API's database type
            DatabaseValidationHelper validationHelper = databaseTypeFactory.getValidationHelper(databaseType);

            validationHelper.validateApiStatus(status);

            api.setStatus(status);
            api.setUpdatedAt(LocalDateTime.now());
            api.setUpdatedBy(performedBy);

            GeneratedApiEntity updatedApi = generatedAPIRepository.save(api);

            loggerUtil.log("autoAPIGenerator", "API status updated: " + apiId +
                    " to " + status + " by: " + performedBy);

            return conversionHelper.mapToResponse(updatedApi);

        } catch (Exception e) {
            log.error("Error updating API status: {}", e.getMessage());
            throw new RuntimeException("Failed to update API status: " + e.getMessage(), e);
        }
    }



    public ApiAnalyticsDTO getApiAnalytics(String requestId, String apiId,
                                           LocalDateTime startDate, LocalDateTime endDate) {
        try {
            // Get the API entity first
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // Get the database type
            String databaseType = api.getDatabaseType();
            if (databaseType == null || databaseType.isEmpty()) {
                databaseType = "oracle";
            }

            ApiAnalyticsHelper analyticsHelper = databaseTypeFactory.getAnalyticsHelper(databaseType);
            return analyticsHelper.buildApiAnalytics(
                    executionLogRepository, apiId, startDate, endDate);
        } catch (Exception e) {
            loggerUtil.log("autoAPIGenerator", "Request ID: " + requestId +
                    ", Error getting API analytics: " + e.getMessage());
            throw new RuntimeException("Failed to get API analytics: " + e.getMessage(), e);
        }
    }


    /**
     * Validates source object for API generation
     * Supports multiple database types: Oracle, PostgreSQL, etc.
     */
    public Map<String, Object> validateSourceObject(ApiSourceObjectDTO sourceObject) {
        // Get database type from source object
        String databaseType = sourceObject.getDatabaseType();

        // If not set, try to determine from context
        if (databaseType == null || databaseType.isEmpty()) {
            databaseType = determineDatabaseType(sourceObject);
            sourceObject.setDatabaseType(databaseType);
        }

        log.info("Validating source object: {}.{} ({}) on database: {}",
                sourceObject.getOwner(), sourceObject.getObjectName(),
                sourceObject.getObjectType(), databaseType);

        // Get the appropriate validation helper
        DatabaseValidationHelper validationHelper = databaseTypeFactory.getValidationHelper(databaseType);

        // Get the appropriate schema service and cast to DatabaseSchemaService
        DatabaseSchemaService schemaService = (DatabaseSchemaService) databaseTypeFactory.getSchemaService(databaseType);

        // Get the appropriate metadata helper
        DatabaseMetadataHelper metadataHelper = databaseTypeFactory.getMetadataHelper(databaseType);

        // Validate using the helper
        return validationHelper.validateSourceObject(
                schemaService,
                sourceObject,
                (source) -> metadataHelper.getSourceObjectDetails(schemaService, source)
        );
    }

    /**
     * Validate Oracle source object
     */
    private Map<String, Object> validateOracleSourceObject(ApiSourceObjectDTO sourceObject) {
        return oracleValidationHelper.validateSourceObject(
                oracleSchemaService,
                sourceObject,
                this::getOracleSourceObjectDetails
        );
    }

    /**
     * Validate PostgreSQL source object
     */
    private Map<String, Object> validatePostgreSQLSourceObject(ApiSourceObjectDTO sourceObject) {
        return postgresValidationHelper.validateSourceObject(
                postgreSQLSchemaService,
                sourceObject,
                this::getPostgreSQLSourceObjectDetails
        );
    }

    /**
     * Get Oracle source object details using metadata helper factory
     */
    private Map<String, Object> getOracleSourceObjectDetails(ApiSourceObjectDTO sourceObject) {
        DatabaseMetadataHelper metadataHelper = metadataHelperFactory.getMetadataHelper("oracle");
        return metadataHelper.getSourceObjectDetails(oracleSchemaService, sourceObject);
    }

    /**
     * Get PostgreSQL source object details using metadata helper factory
     */
    private Map<String, Object> getPostgreSQLSourceObjectDetails(ApiSourceObjectDTO sourceObject) {
        DatabaseMetadataHelper metadataHelper = metadataHelperFactory.getMetadataHelper("postgresql");
        return metadataHelper.getSourceObjectDetails(postgreSQLSchemaService, sourceObject);
    }


    /**
     * Determine database type from source object context
     */
    private String determineDatabaseType(ApiSourceObjectDTO sourceObject) {
        // First, check if database type is explicitly set in the DTO
        if (sourceObject.getDatabaseType() != null && !sourceObject.getDatabaseType().isEmpty()) {
            return sourceObject.getDatabaseType();
        }

        // Check owner - PostgreSQL often uses 'public' schema
        if ("public".equalsIgnoreCase(sourceObject.getOwner())) {
            return "postgresql";
        }

        // Check if the object name uses PostgreSQL naming conventions (lowercase with underscores)
        String objectName = sourceObject.getObjectName();
        if (objectName != null && objectName.matches("^[a-z0-9_]+$")) {
            return "postgresql";
        }

        // Check if the object type is PostgreSQL-specific
        String objectType = sourceObject.getObjectType();
        if (objectType != null) {
            String upperType = objectType.toUpperCase();
            if (upperType.equals("MATERIALIZED VIEW") ||
                    upperType.equals("SEQUENCE") ||
                    upperType.equals("TYPE")) {
                return "postgresql";
            }
        }

        // Check if the object has target info (for synonyms - Oracle specific)
        if (sourceObject.getIsSynonym() != null && sourceObject.getIsSynonym()) {
            return "oracle";
        }

        // Default to Oracle
        return "oracle";
    }


    @Transactional
    public void syncGeneratedComponents(GeneratedApiEntity api, String performedBy) {
        try {
            log.info("Syncing generated components for API: {}", api.getId());

            GenerateApiRequestDTO request = conversionHelper.convertEntityToRequestDTO(api, objectMapper);
            CollectionInfoDTO collectionInfo = conversionHelper.extractCollectionInfo(api, objectMapper);

            updateCodeBase(api, performedBy, request, collectionInfo);
            updateCollections(api, performedBy, request, collectionInfo);
            updateDocumentation(api, performedBy, request, collectionInfo,
                    getCodeBaseRequestId(api), getCollectionsCollectionId(api));

            log.info("Successfully synced all components for API: {}", api.getId());

        } catch (Exception e) {
            log.error("Failed to sync components: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to sync components: " + e.getMessage(), e);
        }
    }



    /**
     * Update codebase components when API is moved between collections/folders
     * This method delegates to CodeBaseGeneratorUtil.update() for the actual update logic
     */
    private void updateCodeBase(GeneratedApiEntity api, String performedBy,
                                GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo) {
        try {
            log.info("Updating codebase for API: {} with collection: {}",
                    api.getId(), collectionInfo.getCollectionName());

            // Call the new update method in CodeBaseGeneratorUtil instead of handling it here
            codeBaseGeneratorUtil.update(api, performedBy, request, null, collectionInfo);

            // Note: The sourceRequestId is updated inside CodeBaseGeneratorUtil.update()
            // to maintain consistency with the new request

            log.info("Codebase update completed for API: {}", api.getId());

        } catch (Exception e) {
            log.error("Error updating codebase: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update codebase: " + e.getMessage(), e);
        }
    }




    public GeneratedApiEntity getApiEntity(String apiId) {
        // First get the API entity to know its database type
        GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

        // Get the database type from the API
        String databaseType = api.getDatabaseType();
        if (databaseType == null || databaseType.isEmpty()) {
            databaseType = "oracle";
        }

        // Get the appropriate execution helper based on database type
        BaseApiExecutionHelper executionHelper = executionHelperFactory.getExecutionHelper(databaseType);
        return executionHelper.getApiEntity(generatedAPIRepository, apiId);
    }

    // ==================== DELEGATED METHODS ====================

    public Map<String, String> generateApiCode(GeneratedApiEntity api) {
        return componentHelper.generateApiCode(api, genUrlBuilder, this::generatePlSqlPackage,
                this::generateGenInfoFile, this::generateOpenApiSpec, this::generatePostmanCollection);
    }

    private String generatePlSqlPackage(GeneratedApiEntity api) {
        return componentHelper.generatePlSqlPackage(api);
    }

    private String generateGenInfoFile(GeneratedApiEntity api, GenUrlBuilderUtil.GenUrlInfo genUrlInfo) {
        return componentHelper.generateGenInfoFile(api, genUrlInfo);
    }

    private String generateOpenApiSpec(GeneratedApiEntity api) {
        return componentHelper.generateOpenApiSpec(api, objectMapper, typeMapper);
    }

    private String generatePostmanCollection(GeneratedApiEntity api) {
        return componentHelper.generatePostmanCollection(api, objectMapper);
    }

    private Object generateSampleResponse(GeneratedApiEntity api) {
        return responseHelper.generateSampleResponse(api);
    }

    private Map<String, Object> getSourceObjectDetails(ApiSourceObjectDTO sourceObject) {
        // This method is called from validation - we need to know the database type
        String databaseType = sourceObject.getDatabaseType();
        if (databaseType == null || databaseType.isEmpty()) {
            databaseType = determineDatabaseType(sourceObject);
            sourceObject.setDatabaseType(databaseType);
        }

        // Get the appropriate metadata helper
        DatabaseMetadataHelper metadataHelper = databaseTypeFactory.getMetadataHelper(databaseType);

        // Get the appropriate schema service and cast to DatabaseSchemaService
        DatabaseSchemaService schemaService = (DatabaseSchemaService) databaseTypeFactory.getSchemaService(databaseType);

        return metadataHelper.getSourceObjectDetails(schemaService, sourceObject);
    }

    private boolean shouldRegenerateComponents(GenerateApiRequestDTO request) {
        return false;
        // return request.getRegenerateComponents() != null && request.getRegenerateComponents();
    }

    private String getCodeBaseRequestId(GeneratedApiEntity api) {
        ApiAnalyticsHelper analyticsHelper = databaseTypeFactory.getAnalyticsHelper(api.getDatabaseType());
        return analyticsHelper.getCodeBaseRequestId(api);
    }

    private String getCollectionsCollectionId(GeneratedApiEntity api) {
        ApiAnalyticsHelper analyticsHelper = databaseTypeFactory.getAnalyticsHelper(api.getDatabaseType());
        return analyticsHelper.getCollectionsCollectionId(api);
    }

    private String getDocumentationCollectionId(GeneratedApiEntity api) {
        ApiAnalyticsHelper analyticsHelper = databaseTypeFactory.getAnalyticsHelper(api.getDatabaseType());
        return analyticsHelper.getDocumentationCollectionId(api);
    }

    /**
     * Helper method to completely clear all relationships from a codebase request
     * This is critical to avoid constraint violations
     */
    private void clearAllCodeBaseRequestRelationships(
            com.usg.autoAPIGenerator.entities.postgres.codeBase.RequestEntity request) {
        try {
            log.debug("Clearing all relationships for codebase request: {}", request.getId());

            // CRITICAL: Clear and delete all implementations
            if (request.getImplementations() != null && !request.getImplementations().isEmpty()) {
                List<ImplementationEntity> implsToRemove = new ArrayList<>(request.getImplementations());
                request.getImplementations().clear();
                if (!implsToRemove.isEmpty()) {
                    implementationRepository.deleteAll(implsToRemove);
                    log.debug("Deleted {} implementations for request: {}", implsToRemove.size(), request.getId());
                }
            }

            // Clear collections and lists
            if (request.getHeaders() != null) {
                request.getHeaders().clear();
            }
            if (request.getPathParameters() != null) {
                request.getPathParameters().clear();
            }
            if (request.getQueryParameters() != null) {
                request.getQueryParameters().clear();
            }
            if (request.getTags() != null) {
                request.getTags().clear();
            }

            // Flush to ensure all deletions are processed immediately
            implementationRepository.flush();

            log.debug("Successfully cleared all relationships for codebase request: {}", request.getId());

        } catch (Exception e) {
            log.error("Error clearing relationships for codebase request {}: {}",
                    request.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to clear codebase request relationships", e);
        }
    }

    /**
     * Helper method to update codebase request content
     */
    private void updateCodeBaseRequestContent(
            com.usg.autoAPIGenerator.entities.postgres.codeBase.RequestEntity request,
            GeneratedApiEntity api, String performedBy,
            GenerateApiRequestDTO generateRequest) {
        try {
            GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);

            // Update basic info
            request.setName(api.getApiName());
            request.setMethod(api.getHttpMethod());
            request.setUrl(genUrlInfo.getFullUrl());
            request.setDescription(api.getDescription());
            request.setUpdatedAt(LocalDateTime.now());

            // Set request body from API request config
            if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("sample", api.getRequestConfig().getSample());
                request.setRequestBody(bodyMap);
            }

            // Set headers
            if (api.getHeaders() != null && !api.getHeaders().isEmpty()) {
                List<Map<String, Object>> headers = new ArrayList<>();
                for (ApiHeaderEntity apiHeader : api.getHeaders()) {
                    Map<String, Object> header = new HashMap<>();
                    header.put("key", apiHeader.getKey());
                    header.put("value", apiHeader.getValue());
                    header.put("description", apiHeader.getDescription());
                    header.put("required", apiHeader.getRequired());
                    headers.add(header);
                }
                request.setHeaders(headers);
            }

            // Set path parameters
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                List<Map<String, Object>> pathParams = new ArrayList<>();
                List<Map<String, Object>> queryParams = new ArrayList<>();

                for (ApiParameterEntity apiParam : api.getParameters()) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("name", apiParam.getKey());
                    param.put("type", apiParam.getOracleType());
                    param.put("required", apiParam.getRequired());
                    param.put("description", apiParam.getDescription());
                    param.put("example", apiParam.getExample());

                    if ("path".equalsIgnoreCase(apiParam.getParameterLocation())) {
                        pathParams.add(param);
                    } else if ("query".equalsIgnoreCase(apiParam.getParameterLocation())) {
                        queryParams.add(param);
                    }
                }

                request.setPathParameters(pathParams);
                request.setQueryParameters(queryParams);
            }

            // Set metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("apiId", api.getId());
            metadata.put("apiCode", api.getApiCode());
            metadata.put("databaseType", api.getDatabaseType());
            metadata.put("status", api.getStatus());
            metadata.put("version", api.getVersion());
            metadata.put("updatedBy", performedBy);
            request.setMetadata(metadata);

            // Set tags
            if (api.getTags() != null && !api.getTags().isEmpty()) {
                request.setTags(new ArrayList<>(api.getTags()));
            }

            // Save the request first
            request = codeBaseRequestRepository.save(request);

            // Create implementations (this is the critical part for codebase)
            createImplementationsForRequest(request, api, performedBy);

            log.info("Successfully updated codebase request content for: {}", request.getId());

        } catch (Exception e) {
            log.error("Error updating codebase request content: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update codebase request content", e);
        }
    }

    /**
     * Helper method to create implementations for a request
     */
    private void createImplementationsForRequest(
            com.usg.autoAPIGenerator.entities.postgres.codeBase.RequestEntity request,
            GeneratedApiEntity api, String performedBy) {
        try {
            log.info("Creating implementations for request: {}", request.getId());

            // Generate code for all languages
            Map<String, String> generatedCode = generateApiCode(api);

            if (generatedCode != null && !generatedCode.isEmpty()) {
                List<ImplementationEntity> newImplementations = new ArrayList<>();

                for (Map.Entry<String, String> codeEntry : generatedCode.entrySet()) {
                    String language = codeEntry.getKey();
                    String code = codeEntry.getValue();

                    ImplementationEntity impl = new ImplementationEntity();
                    impl.setId(UUID.randomUUID().toString());
                    impl.setGeneratedApiId(api.getId());
                    impl.setLanguage(language);
                    impl.setComponent("api_" + api.getApiCode().toLowerCase());
                    impl.setCode(code);
                    impl.setIsGenerated(true);
                    impl.setGeneratedBy(performedBy);
                    impl.setVersion(1);
                    impl.setIsValidated(false);
                    impl.setUsageCount(0);

                    // Set metadata
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("apiName", api.getApiName());
                    metadata.put("apiCode", api.getApiCode());
                    metadata.put("httpMethod", api.getHttpMethod());
                    metadata.put("endpointPath", api.getEndpointPath());
                    metadata.put("generatedAt", LocalDateTime.now().toString());
                    impl.setMetadata(metadata);

                    // Update file name and calculate lines
                    impl.updateFileName();
                    impl.calculateLinesOfCode();

                    newImplementations.add(impl);
                }

                // Save all implementations
                if (!newImplementations.isEmpty()) {
                    implementationRepository.saveAll(newImplementations);

                    // Add implementations to request
                    for (ImplementationEntity impl : newImplementations) {
                        request.addImplementation(impl);
                    }

                    log.info("Created {} implementations for request: {}",
                            newImplementations.size(), request.getId());
                }
            } else {
                log.warn("No code generated for API: {}", api.getId());
            }

        } catch (Exception e) {
            log.error("Error creating implementations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create implementations", e);
        }
    }

    /**
     * Helper method to update folder request flags for codebase
     * Note: FolderEntity uses hasRequests boolean, not requestCount
     */
    private void updateCodeBaseFolderRequestCounts(String oldFolderId, String newFolderId) {
        try {
            // Update old folder if it exists and is different from new folder
            if (oldFolderId != null && !oldFolderId.equals(newFolderId)) {
                codeBaseFolderRepository.findById(oldFolderId).ifPresent(oldFolder -> {
                    // Check if this folder still has any other requests
                    // You might need to count remaining requests in this folder
                    boolean hasOtherRequests = checkIfFolderHasRequests(oldFolder.getId());
                    oldFolder.setHasRequests(hasOtherRequests);
                    codeBaseFolderRepository.save(oldFolder);
                    log.debug("Updated hasRequests flag for old codebase folder: {} to {}",
                            oldFolderId, hasOtherRequests);
                });
            }

            // Update new folder if it exists and is different from old folder
            if (newFolderId != null && !newFolderId.equals(oldFolderId)) {
                codeBaseFolderRepository.findById(newFolderId).ifPresent(newFolder -> {
                    newFolder.setHasRequests(true);
                    codeBaseFolderRepository.save(newFolder);
                    log.debug("Set hasRequests flag to true for new codebase folder: {}", newFolderId);
                });
            }
        } catch (Exception e) {
            log.warn("Failed to update codebase folder request flags: {}", e.getMessage());
        }
    }

    /**
     * Helper method to check if a folder has any requests
     */
    private boolean checkIfFolderHasRequests(String folderId) {
        try {
            // Query to count requests in this folder
            // You might need to add this method to your RequestRepository
            long requestCount = codeBaseRequestRepository.countByFolderId(folderId);
            return requestCount > 0;
        } catch (Exception e) {
            log.error("Error checking if folder has requests: {}", e.getMessage());
            return true; // Assume true to be safe
        }
    }



    private void updateCollections(GeneratedApiEntity api, String performedBy,
                                   GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo) {
        componentHelper.updateCollections(api, performedBy, request, collectionInfo,
                collectionsGeneratorUtil, collectionsCollectionRepository, collectionsFolderRepository,
                collectionsRequestRepository, entityManager);
    }




    private void regenerateComponents(GeneratedApiEntity api, String performedBy,
                                      GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo) {
        componentHelper.regenerateComponents(api, performedBy, request, collectionInfo,
                codeBaseGeneratorUtil, collectionsGeneratorUtil, documentationGeneratorUtil,
                this::generateApiCode);
    }

    private CollectionInfoDTO extractCollectionInfo(GeneratedApiEntity api) {
        return conversionHelper.extractCollectionInfo(api, objectMapper);
    }



    public boolean isApiCodeAvailable(String apiCode) {
        try {
            // Check if an API with this code already exists
            return !generatedAPIRepository.existsByApiCode(apiCode);
        } catch (Exception e) {
            log.error("Error checking API code availability: {}", e.getMessage());
            // Return false in case of error to be safe
            return false;
        }
    }


    // Add this method to AutoAPIGeneratorEngineService
    private String buildEndpointPathFromRequest(GenerateApiRequestDTO request) {
        String baseEndpoint = request.getEndpointPath();
        if (baseEndpoint == null || baseEndpoint.isEmpty()) {
            baseEndpoint = "/api/v1/" + request.getApiCode().toLowerCase();
        }

        log.info("Building endpoint path from request. Base: {}", baseEndpoint);

        // Use the parameters DIRECTLY from the request
        List<ApiParameterDTO> pathParams = request.getParameters().stream()
                .filter(p -> "path".equalsIgnoreCase(p.getParameterLocation()))
                .sorted(Comparator.comparing(ApiParameterDTO::getPosition,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        log.info("Found {} path parameters from request: {}", pathParams.size(),
                pathParams.stream().map(ApiParameterDTO::getKey).collect(Collectors.joining(", ")));

        StringBuilder endpointBuilder = new StringBuilder(baseEndpoint);

        // Remove trailing slash if present
        if (endpointBuilder.length() > 0 && endpointBuilder.charAt(endpointBuilder.length() - 1) == '/') {
            endpointBuilder.setLength(endpointBuilder.length() - 1);
        }

        // Add each path parameter as a placeholder
        for (ApiParameterDTO param : pathParams) {
            endpointBuilder.append("/{").append(param.getKey()).append("}");
        }

        String fullEndpoint = endpointBuilder.toString();
        log.info("Final endpoint path with placeholders: {}", fullEndpoint);

        return fullEndpoint;
    }


    // Add this helper method near the other helper methods
    /**
     * Generate current timestamp in format YYYYMMDDHHMMSS
     * Used for AUTOGENERATE data type parameters
     */
    private String getCurrentTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%04d%02d%02d%02d%02d%02d",
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute(),
                now.getSecond());
    }


    /**
     * Create consolidated parameters map that INCLUDES HEADERS for validation
     * Also handles AUTOGENERATE data types by auto-populating with current timestamp
     * NOW WITH COMPLETE FILE UPLOAD SUPPORT
     */
    private Map<String, Object> createConsolidatedParamsWithHeaders(ExecuteApiRequestDTO executeRequest, GeneratedApiEntity api) {
        Map<String, Object> allParams = new HashMap<>();

        log.info("=== Creating Consolidated Params with Headers ===");
        log.info("Request has file: {}", executeRequest.getFile() != null ? "YES - " + executeRequest.getFile().getOriginalFilename() : "NO");
        log.info("Request has multiple files: {}", executeRequest.getFiles() != null ? executeRequest.getFiles().size() : "NO");
        log.info("Request has fileMap: {}", executeRequest.getFileMap() != null ? executeRequest.getFileMap().size() : "NO");

        // ============ 1. ADD PATH PARAMETERS ============
        if (executeRequest.getPathParams() != null && !executeRequest.getPathParams().isEmpty()) {
            allParams.putAll(executeRequest.getPathParams());
            log.info("Added path params to consolidated map: {}", executeRequest.getPathParams().keySet());
        }

        // ============ 2. ADD QUERY PARAMETERS ============
        if (executeRequest.getQueryParams() != null && !executeRequest.getQueryParams().isEmpty()) {
            allParams.putAll(executeRequest.getQueryParams());
            log.info("Added query params to consolidated map: {}", executeRequest.getQueryParams().keySet());
        }

        // ============ 3. ADD HEADERS (CRITICAL FIX) ============
        if (executeRequest.getHeaders() != null && !executeRequest.getHeaders().isEmpty()) {
            // Add headers as-is (original case)
            allParams.putAll(executeRequest.getHeaders());

            // Also add lowercase versions for case-insensitive matching
            executeRequest.getHeaders().forEach((key, value) -> {
                allParams.put(key.toLowerCase(), value);
            });

            log.info("Added headers to consolidated map: {}", executeRequest.getHeaders().keySet());

            // Specifically log if ac_no header is present
            if (executeRequest.getHeaders().containsKey("ac_no")) {
                log.info("Found ac_no header with value: {}", executeRequest.getHeaders().get("ac_no"));
            } else if (executeRequest.getHeaders().containsKey("AC_NO")) {
                log.info("Found AC_NO header with value: {}", executeRequest.getHeaders().get("AC_NO"));
            }
        } else {
            log.warn("No headers found in the request!");
        }

        // ============ 4. ADD BODY PARAMETERS ============
        if (executeRequest.getBody() != null) {
            if (executeRequest.getBody() instanceof Map) {
                Map<String, Object> bodyMap = (Map<String, Object>) executeRequest.getBody();
                allParams.putAll(bodyMap);
                log.info("Added body params to consolidated map: {}", bodyMap.keySet());

                // CRITICAL: Extract GraphQL variables if this is a GraphQL request
                if ("graphql".equalsIgnoreCase(api.getProtocolType())) {
                    Map<String, Object> graphqlVars = extractGraphQLVariables(bodyMap);
                    if (!graphqlVars.isEmpty()) {
                        allParams.putAll(graphqlVars);
                        log.info("✅ GraphQL parameters extracted successfully: {}", graphqlVars);

                        // Log specifically for debugging
                        for (Map.Entry<String, Object> entry : graphqlVars.entrySet()) {
                            log.info("  GraphQL param '{}' = '{}'", entry.getKey(), entry.getValue());
                        }
                    } else {
                        log.warn("⚠️ No GraphQL parameters found in request");
                    }
                }
            } else {
                // For non-map bodies, wrap in a special key
                allParams.put("_body", executeRequest.getBody());
                log.info("Added raw body to consolidated map");
            }
        }

        // ============ 5. HANDLE FILE UPLOADS FROM FILE MAP (PREFERRED) ============
        // This handles named file parameters sent via multipart/form-data
        if (executeRequest.getFileMap() != null && !executeRequest.getFileMap().isEmpty()) {
            log.info("Processing named file map: {} entries", executeRequest.getFileMap().size());

            for (Map.Entry<String, MultipartFile> entry : executeRequest.getFileMap().entrySet()) {
                String paramName = entry.getKey();
                MultipartFile file = entry.getValue();

                log.info("  Processing named file: {} -> {}", paramName, file.getOriginalFilename());

                try {
                    byte[] fileBytes = file.getBytes();
                    String databaseType = api.getDatabaseType() != null ? api.getDatabaseType() : "oracle";

                    // Store the file in multiple formats for flexibility
                    allParams.put(paramName, fileBytes);           // Raw bytes
                    allParams.put(paramName + "_file", file);      // Original MultipartFile
                    allParams.put(paramName + "_name", file.getOriginalFilename());
                    allParams.put(paramName + "_size", file.getSize());
                    allParams.put(paramName + "_content_type", file.getContentType());

                    // Database-specific storage
                    if ("postgresql".equalsIgnoreCase(databaseType)) {
                        allParams.put(paramName + "_bytea", fileBytes);
                        log.info("Stored file for parameter '{}' as bytea ({} bytes) for PostgreSQL", paramName, fileBytes.length);
                    } else if ("oracle".equalsIgnoreCase(databaseType)) {
                        allParams.put(paramName + "_blob", fileBytes);
                        log.info("Stored file for parameter '{}' as BLOB ({} bytes) for Oracle", paramName, fileBytes.length);
                    }

                    // Also add to body if body is a Map (for consistency)
                    if (executeRequest.getBody() instanceof Map) {
                        ((Map<String, Object>) executeRequest.getBody()).put(paramName, file);
                        ((Map<String, Object>) executeRequest.getBody()).put(paramName + "_data", fileBytes);
                        ((Map<String, Object>) executeRequest.getBody()).put(paramName + "_name", file.getOriginalFilename());
                    }

                    log.info("Successfully stored file for parameter: {}", paramName);

                } catch (IOException e) {
                    log.error("Failed to read uploaded file for parameter {}: {}", paramName, e.getMessage(), e);
                    throw new RuntimeException("Failed to process file upload for parameter '" + paramName + "': " + e.getMessage(), e);
                }
            }
        }

        // ============ 6. HANDLE SINGLE FILE UPLOAD (LEGACY/STANDARD) ============
        // This handles cases where a single file is sent without a parameter name
        if (executeRequest.getFile() != null && !executeRequest.getFile().isEmpty()) {
            MultipartFile file = executeRequest.getFile();
            log.info("Processing single file upload: {}", file.getOriginalFilename());

            try {
                // Find which parameter should receive this file
                ApiParameterEntity fileParameter = findFileParameter(api);
                byte[] fileBytes = file.getBytes();
                String databaseType = api.getDatabaseType() != null ? api.getDatabaseType() : "oracle";

                if (fileParameter != null) {
                    String paramKey = fileParameter.getKey();
                    log.info("Mapping single file to parameter: {} for database: {}", paramKey, databaseType);

                    // Store file data based on database type
                    allParams.put(paramKey, fileBytes);
                    allParams.put(paramKey + "_file", file);
                    allParams.put(paramKey + "_name", file.getOriginalFilename());
                    allParams.put(paramKey + "_size", file.getSize());
                    allParams.put(paramKey + "_content_type", file.getContentType());

                    if ("postgresql".equalsIgnoreCase(databaseType)) {
                        allParams.put(paramKey + "_bytea", fileBytes);
                        log.info("Stored file as bytea ({} bytes) for PostgreSQL", fileBytes.length);
                    } else if ("oracle".equalsIgnoreCase(databaseType)) {
                        allParams.put(paramKey + "_blob", fileBytes);
                        log.info("Stored file as BLOB ({} bytes) for Oracle", fileBytes.length);
                    }

                    // Also add to body if body is a Map (for consistency)
                    if (executeRequest.getBody() instanceof Map) {
                        ((Map<String, Object>) executeRequest.getBody()).put(paramKey, file);
                        ((Map<String, Object>) executeRequest.getBody()).put(paramKey + "_data", fileBytes);
                        ((Map<String, Object>) executeRequest.getBody()).put(paramKey + "_name", file.getOriginalFilename());
                    }

                } else {
                    // No specific file parameter found, store with default key
                    log.warn("No file parameter configured for API, storing with default key 'file'");
                    allParams.put("file", fileBytes);
                    allParams.put("file_name", file.getOriginalFilename());
                    allParams.put("file_content_type", file.getContentType());
                    allParams.put("file_size", file.getSize());

                    if ("postgresql".equalsIgnoreCase(databaseType)) {
                        allParams.put("file_bytea", fileBytes);
                    } else if ("oracle".equalsIgnoreCase(databaseType)) {
                        allParams.put("file_blob", fileBytes);
                    }
                }

            } catch (IOException e) {
                log.error("Failed to read uploaded file: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process file upload: " + e.getMessage(), e);
            }
        }

        // ============ 7. HANDLE MULTIPLE FILES UPLOAD ============
        if (executeRequest.getFiles() != null && !executeRequest.getFiles().isEmpty()) {
            log.info("Processing multiple file upload: {} files", executeRequest.getFiles().size());

            List<Map<String, Object>> fileList = new ArrayList<>();
            Map<String, Object> filesByParam = new HashMap<>();

            for (int i = 0; i < executeRequest.getFiles().size(); i++) {
                MultipartFile file = executeRequest.getFiles().get(i);
                log.info("  Processing file {}: {}", i, file.getOriginalFilename());

                try {
                    byte[] fileBytes = file.getBytes();
                    String databaseType = api.getDatabaseType() != null ? api.getDatabaseType() : "oracle";

                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("index", i);
                    fileInfo.put("data", fileBytes);
                    fileInfo.put("name", file.getOriginalFilename());
                    fileInfo.put("contentType", file.getContentType());
                    fileInfo.put("size", file.getSize());

                    if ("oracle".equalsIgnoreCase(databaseType)) {
                        fileInfo.put("oracle_blob", fileBytes);
                    }

                    fileList.add(fileInfo);

                    // Try to map to parameters if multiple file parameters exist
                    List<ApiParameterEntity> fileParameters = findFileParameters(api);
                    if (i < fileParameters.size()) {
                        String paramKey = fileParameters.get(i).getKey();
                        filesByParam.put(paramKey, fileBytes);
                        filesByParam.put(paramKey + "_metadata", fileInfo);
                        filesByParam.put(paramKey + "_name", file.getOriginalFilename());
                        log.info("  Mapped file {} to parameter: {}", file.getOriginalFilename(), paramKey);
                    } else {
                        // Store with index if no specific mapping
                        filesByParam.put("file_" + i, fileBytes);
                        filesByParam.put("file_" + i + "_name", file.getOriginalFilename());
                    }

                } catch (IOException e) {
                    log.error("Failed to read uploaded file {}: {}", i, e.getMessage(), e);
                    throw new RuntimeException("Failed to process file upload: " + e.getMessage(), e);
                }
            }

            // Add to consolidated params
            allParams.put("files", fileList);
            allParams.putAll(filesByParam);

            log.info("Added {} files to consolidated params", fileList.size());
        }

        // ============ 8. HANDLE BASE64 ENCODED FILES (from JSON body) ============
        if (executeRequest.getBody() instanceof Map) {
            Map<String, Object> bodyMap = (Map<String, Object>) executeRequest.getBody();

            // Check for base64 encoded files in the body
            for (Map.Entry<String, Object> entry : bodyMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof String) {
                    String strValue = (String) value;
                    // Check if it looks like a base64 encoded file
                    if (strValue.startsWith("data:") && strValue.contains(";base64,")) {
                        log.info("Detected base64 encoded file in field: {}", key);

                        try {
                            // Parse data URL: data:image/png;base64,actualbase64data
                            String[] parts = strValue.split(",");
                            if (parts.length == 2) {
                                String metadata = parts[0];
                                String base64Data = parts[1];

                                // Extract content type
                                String contentType = metadata.substring(5, metadata.indexOf(';'));
                                byte[] fileBytes = java.util.Base64.getDecoder().decode(base64Data);

                                // Store the decoded file
                                allParams.put(key, fileBytes);
                                allParams.put(key + "_content_type", contentType);
                                allParams.put(key + "_size", fileBytes.length);
                                allParams.put(key + "_is_base64", true);
                                allParams.put(key + "_file_name", key + "_base64_decoded");

                                log.info("Decoded base64 file for {}: {} bytes, type: {}", key, fileBytes.length, contentType);
                            }
                        } catch (Exception e) {
                            log.warn("Failed to decode base64 file for field {}: {}", key, e.getMessage());
                        }
                    }
                }
            }
        }

        // ============ 9. HANDLE AUTOGENERATE PARAMETERS ============
        if (api != null && api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if ("AUTOGENERATE".equalsIgnoreCase(param.getOracleType())) {
                    String paramKey = param.getKey();
                    String timestamp = getCurrentTimestamp();

                    // Check if this parameter already has a meaningful value
                    boolean hasExistingValue = false;
                    Object existingValue = null;

                    for (Map.Entry<String, Object> entry : allParams.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(paramKey)) {
                            existingValue = entry.getValue();
                            // Treat null OR empty string as "no value"
                            if (existingValue != null && !existingValue.toString().trim().isEmpty()) {
                                hasExistingValue = true;
                                log.info("AUTOGENERATE parameter [{}] already has value: {}, keeping existing value",
                                        paramKey, existingValue);
                            } else {
                                log.info("AUTOGENERATE parameter [{}] has empty value, will auto-populate", paramKey);
                            }
                            break;
                        }
                    }

                    // If no meaningful existing value, auto-populate with timestamp
                    if (!hasExistingValue) {
                        allParams.put(paramKey, timestamp);
                        log.info("🔧 AUTOGENERATE parameter [{}] auto-populated with timestamp: {}", paramKey, timestamp);

                        // Also add to the appropriate location in the request
                        String location = param.getParameterLocation();
                        if ("path".equalsIgnoreCase(location) && executeRequest.getPathParams() != null) {
                            executeRequest.getPathParams().put(paramKey, timestamp);
                        } else if ("query".equalsIgnoreCase(location) && executeRequest.getQueryParams() != null) {
                            executeRequest.getQueryParams().put(paramKey, timestamp);
                        } else if ("header".equalsIgnoreCase(location) && executeRequest.getHeaders() != null) {
                            executeRequest.getHeaders().put(paramKey, timestamp);
                        } else if ("body".equalsIgnoreCase(location) && executeRequest.getBody() instanceof Map) {
                            ((Map<String, Object>) executeRequest.getBody()).put(paramKey, timestamp);
                        }
                    }
                }
            }
        }

        // ============ 10. LOG SUMMARY ============
        log.info("=== Consolidated Params Summary ===");
        log.info("Total parameters: {}", allParams.size());
        log.info("Parameter keys: {}", allParams.keySet());

        // Log file-related parameters specifically
        allParams.keySet().stream()
                .filter(key -> key.toLowerCase().contains("file") ||
                        key.toLowerCase().contains("blob") ||
                        key.toLowerCase().contains("byte") ||
                        key.toLowerCase().contains("_name") ||
                        key.toLowerCase().contains("_size"))
                .forEach(key -> {
                    Object value = allParams.get(key);
                    if (value instanceof byte[]) {
                        log.info("  File param '{}' : {} bytes", key, ((byte[]) value).length);
                    } else if (value instanceof MultipartFile) {
                        MultipartFile file = (MultipartFile) value;
                        log.info("  File param '{}' : {} ({} bytes)", key, file.getOriginalFilename(), file.getSize());
                    } else {
                        log.info("  File param '{}' : {}", key, value);
                    }
                });

        // Check specifically for required parameters in consolidated params
        if (api != null && api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if (param.getRequired()) {
                    String paramKey = param.getKey();
                    boolean found = false;
                    for (String key : allParams.keySet()) {
                        if (key.equalsIgnoreCase(paramKey)) {
                            found = true;
                            log.info("Required parameter '{}' found in consolidated params as '{}'", paramKey, key);
                            break;
                        }
                    }
                    if (!found) {
                        log.warn("WARNING: Required parameter '{}' NOT found in consolidated params!", paramKey);
                    }
                }
            }
        }

        return allParams;
    }

    /**
     * Helper method to find a file parameter in the API configuration
     */
    private ApiParameterEntity findFileParameter(GeneratedApiEntity api) {
        if (api == null || api.getParameters() == null) {
            return null;
        }

        // Look for parameters that accept files
        return api.getParameters().stream()
                .filter(p -> {
                    String paramType = p.getParameterType() != null ? p.getParameterType().toUpperCase() : "";
                    String oracleType = p.getOracleType() != null ? p.getOracleType().toUpperCase() : "";
                    String apiType = p.getApiType() != null ? p.getApiType().toUpperCase() : "";

                    return "FILE".equals(paramType) ||
                            "BLOB".equals(oracleType) ||
                            "CLOB".equals(oracleType) ||
                            "BYTEA".equals(oracleType) ||
                            "BINARY".equals(apiType) ||
                            "FILE".equals(apiType);
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * Helper method to find all file parameters in the API configuration
     */
    private List<ApiParameterEntity> findFileParameters(GeneratedApiEntity api) {
        if (api == null || api.getParameters() == null) {
            return new ArrayList<>();
        }

        return api.getParameters().stream()
                .filter(p -> {
                    String paramType = p.getParameterType() != null ? p.getParameterType().toUpperCase() : "";
                    String oracleType = p.getOracleType() != null ? p.getOracleType().toUpperCase() : "";
                    String apiType = p.getApiType() != null ? p.getApiType().toUpperCase() : "";

                    return "FILE".equals(paramType) ||
                            "BLOB".equals(oracleType) ||
                            "CLOB".equals(oracleType) ||
                            "BYTEA".equals(oracleType) ||
                            "BINARY".equals(apiType) ||
                            "FILE".equals(apiType);
                })
                .collect(Collectors.toList());
    }


    /**
     * Enhanced validation that checks headers properly (without HttpServletRequest parameter)
     */
    private Map<String, String> validateRequiredParametersEnhanced(
            GeneratedApiEntity api,
            Map<String, Object> consolidatedParams,
            ExecuteApiRequestDTO executeRequest) {

        Map<String, String> errors = new HashMap<>();
        List<ApiParameterEntity> parameters = api.getParameters();

        log.info("=== Starting Enhanced Required Parameter Validation ===");
        log.info("Processing {} parameters", parameters.size());
        log.info("Consolidated params keys: {}", consolidatedParams.keySet());

        for (ApiParameterEntity param : parameters) {
            if ("AUTOGENERATE".equalsIgnoreCase(param.getOracleType())) {
                log.info("Skipping validation for AUTOGENERATE parameter: {}", param.getKey());
                continue;
            }

            if (!param.getRequired()) {
                continue;
            }

            String paramKey = param.getKey();
            String location = param.getParameterLocation();

            log.info("Checking required parameter: {} (location: {})", paramKey, location);

            Object value = null;

            // Check if this is a file parameter
            boolean isFileParam = "FILE".equals(param.getParameterType()) ||
                    "BLOB".equals(param.getOracleType()) ||
                    "BYTEA".equals(param.getOracleType());

            // For file parameters, check fileMap first
            if (isFileParam && executeRequest != null && executeRequest.getFileMap() != null) {
                MultipartFile file = executeRequest.getFileMap().get(paramKey);
                if (file != null && !file.isEmpty()) {
                    value = file;
                    log.info("Found file for parameter {}: {} ({} bytes)", paramKey, file.getOriginalFilename(), file.getSize());
                }
            }

            // Check in consolidated params
            if (value == null) {
                value = consolidatedParams.get(paramKey);
            }

            // Check case-insensitive
            if (value == null) {
                for (Map.Entry<String, Object> entry : consolidatedParams.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(paramKey)) {
                        value = entry.getValue();
                        log.info("Found case-insensitive match: {} = {}", entry.getKey(), value);
                        break;
                    }
                }
            }

            // Check for file in executeRequest if it's a file parameter
            if (value == null && isFileParam && executeRequest != null) {
                if (executeRequest.getFile() != null && paramKey.equalsIgnoreCase("file")) {
                    value = executeRequest.getFile();
                } else if (executeRequest.getFiles() != null && !executeRequest.getFiles().isEmpty()) {
                    // Check if files are mapped to this parameter
                    for (int i = 0; i < executeRequest.getFiles().size(); i++) {
                        MultipartFile file = executeRequest.getFiles().get(i);
                        // You might need additional logic here based on your mapping
                        if (file != null && !file.isEmpty()) {
                            value = file;
                            break;
                        }
                    }
                }
            }

            // Validate the value
            if (value == null) {
                log.error("Required parameter [{}] not found or empty", paramKey);
                errors.put(paramKey, "Required parameter '" + paramKey + "' is missing");
            } else if (value instanceof MultipartFile) {
                MultipartFile file = (MultipartFile) value;
                if (file.isEmpty()) {
                    log.error("Required file parameter [{}] is empty", paramKey);
                    errors.put(paramKey, "Required file parameter '" + paramKey + "' is empty");
                } else {
                    log.info("Required file parameter [{}] found with file: {} ({} bytes)",
                            paramKey, file.getOriginalFilename(), file.getSize());
                }
            } else if (value instanceof String && ((String) value).trim().isEmpty()) {
                log.error("Required parameter [{}] is empty string", paramKey);
                errors.put(paramKey, "Required parameter '" + paramKey + "' is empty");
            } else if (value instanceof byte[]) {
                byte[] bytes = (byte[]) value;
                if (bytes.length == 0) {
                    log.error("Required file parameter [{}] has empty bytes", paramKey);
                    errors.put(paramKey, "Required file parameter '" + paramKey + "' has no data");
                } else {
                    log.info("Required file parameter [{}] found with {} bytes", paramKey, bytes.length);
                }
            } else {
                log.info("Required parameter [{}] found with value: {}", paramKey, value);
            }
        }

        log.info("=== Validation complete. Errors: {} ===\n", errors.size());
        return errors;
    }



    /**
     * Sets the generated API ID for any entity that has a generatedApiId field
     * This ensures all related entities are properly linked to the main API
     */
    private void setGeneratedApiIdForEntity(Object entity, String generatedApiId) {
        if (entity == null || generatedApiId == null) return;

        try {
            // Use reflection to find and set generatedApiId field
            java.lang.reflect.Field field = getGeneratedApiIdField(entity.getClass());
            if (field != null) {
                field.setAccessible(true);
                field.set(entity, generatedApiId);
            }
        } catch (Exception e) {
            log.warn("Could not set generatedApiId for entity: {} - {}",
                    entity.getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * Recursively find the generatedApiId field in class hierarchy
     */
    private java.lang.reflect.Field getGeneratedApiIdField(Class<?> clazz) {
        while (clazz != null && clazz != Object.class) {
            try {
                java.lang.reflect.Field field = clazz.getDeclaredField("generatedApiId");
                return field;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Sets generatedApiId for a collection of entities
     */
    private <T> void setGeneratedApiIdForEntities(Collection<T> entities, String generatedApiId) {
        if (entities == null) return;
        entities.forEach(entity -> setGeneratedApiIdForEntity(entity, generatedApiId));
    }


    /**
     * Parse XML body and extract parameter values
     */
    private Map<String, Object> parseXmlParameters(String xmlBody, List<ApiParameterEntity> parameters) {
        Map<String, Object> extractedParams = new HashMap<>();

        if (xmlBody == null || xmlBody.trim().isEmpty()) {
            return extractedParams;
        }

        log.info("Parsing XML body to extract parameter values");

        try {
            // For each parameter, try to extract its value from XML
            for (ApiParameterEntity param : parameters) {
                String paramKey = param.getKey();
                if (paramKey == null || paramKey.isEmpty()) {
                    continue;
                }

                // Look for XML tags with this key (case-insensitive)
                Pattern pattern = Pattern.compile("<" + paramKey + ">(.*?)</" + paramKey + ">",
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher matcher = pattern.matcher(xmlBody);

                if (matcher.find()) {
                    String value = matcher.group(1).trim();
                    extractedParams.put(paramKey, value);
                    log.info("✅ Extracted XML parameter: {} = {}", paramKey, value);
                }
            }

            log.info("Extracted {} parameters from XML: {}", extractedParams.size(), extractedParams.keySet());

        } catch (Exception e) {
            log.error("Error parsing XML parameters: {}", e.getMessage(), e);
        }

        return extractedParams;
    }



    /**
     * Check if the requesting IP is whitelisted and has access to the endpoint
     * DENY BY DEFAULT - Only allow if explicitly whitelisted
     * @param clientIp The client IP address (automatically obtained from request)
     * @param fullRequestPath The complete request path (e.g., /plx/api/gen/{apiId}/api/v1/create-bio-data)
     * @param endpointPath The API endpoint path (e.g., /create-bio-data)
     * @throws RuntimeException with appropriate message if access is denied
     */
    private void validateIpWhitelist(String clientIp, String fullRequestPath, String endpointPath) {
        try {
            // Get all active whitelist entries (case-insensitive)
            List<IPWhitelistEntryEntity> allEntries = ipWhitelistRepository.findAll();
            List<IPWhitelistEntryEntity> whitelistEntries = allEntries.stream()
                    .filter(entry -> entry.getStatus() != null && "ACTIVE".equalsIgnoreCase(entry.getStatus().trim()))
                    .collect(Collectors.toList());

            // DENY BY DEFAULT - If no whitelist entries exist, block everything
            if (whitelistEntries.isEmpty()) {
                String errorMsg = "Access denied: No IP whitelist configuration found. Please contact system administrator.";
                log.warn("IP whitelist validation failed - No active whitelist entries found. IP: {}, FullPath: {}, Endpoint: {}",
                        clientIp, fullRequestPath, endpointPath);
                throw new RuntimeException(errorMsg);
            }

            // Check if the client IP matches any whitelist entry
            boolean ipMatches = false;
            IPWhitelistEntryEntity matchingEntry = null;

            for (IPWhitelistEntryEntity entry : whitelistEntries) {
                if (isIpInRange(clientIp, entry.getIpRange())) {
                    ipMatches = true;
                    matchingEntry = entry;
                    log.info("IP {} matched whitelist entry: {} (Range: {})", clientIp, entry.getName(), entry.getIpRange());
                    break;
                }
            }

            // DENY if IP not found in any whitelist entry
            if (!ipMatches) {
                String errorMsg = String.format("Access denied: IP address %s is not whitelisted. Please contact system administrator for access.", clientIp);
                log.warn("IP whitelist validation failed - IP: {}, FullPath: {}, Endpoint: {}", clientIp, fullRequestPath, endpointPath);
                throw new RuntimeException(errorMsg);
            }

            // Check endpoint access using the FULL REQUEST PATH
            String allowedEndpoints = matchingEntry.getEndpoints();

            // Clean the endpoints string - remove brackets, quotes, and trim
            String cleanedEndpoints = "";
            if (allowedEndpoints != null) {
                cleanedEndpoints = allowedEndpoints.trim();
                // Remove square brackets if present
                if (cleanedEndpoints.startsWith("[") && cleanedEndpoints.endsWith("]")) {
                    cleanedEndpoints = cleanedEndpoints.substring(1, cleanedEndpoints.length() - 1);
                }
                // Remove quotes if present
                cleanedEndpoints = cleanedEndpoints.replace("\"", "");
            }

            // If endpoints is empty or null or contains "/**", they can access all endpoints
            if (cleanedEndpoints == null || cleanedEndpoints.trim().isEmpty() || "/**".equals(cleanedEndpoints.trim())) {
                log.info("IP {} has access to all endpoints (allowedEndpoints: {})", clientIp, cleanedEndpoints);
                return;
            }

            // Parse allowed endpoints (comma-separated)
            String[] allowedEndpointList = cleanedEndpoints.split(",");
            boolean endpointAllowed = false;

            for (String allowedEndpoint : allowedEndpointList) {
                // Clean each endpoint pattern - remove any remaining brackets or quotes
                String trimmedEndpoint = allowedEndpoint.trim();
                trimmedEndpoint = trimmedEndpoint.replace("[", "").replace("]", "").replace("\"", "");

                if (trimmedEndpoint.isEmpty()) {
                    continue;
                }

                // Check against BOTH full request path and endpoint path
                if (isEndpointMatch(fullRequestPath, trimmedEndpoint) || isEndpointMatch(endpointPath, trimmedEndpoint)) {
                    endpointAllowed = true;
                    log.info("IP {} granted access to path: {} (matched pattern: {})", clientIp, fullRequestPath, trimmedEndpoint);
                    break;
                }
            }

            // DENY if endpoint not in allowed list
            if (!endpointAllowed) {
                String errorMsg = String.format("Access denied: IP address %s does not have access to path '%s'. Allowed paths: %s",
                        clientIp, fullRequestPath, cleanedEndpoints);
                log.warn("IP endpoint access denied - IP: {}, FullPath: {}, Allowed: {}", clientIp, fullRequestPath, cleanedEndpoints);
                throw new RuntimeException(errorMsg);
            }

            log.info("IP whitelist validation passed - IP: {}, FullPath: {}", clientIp, fullRequestPath);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during IP whitelist validation: {}", e.getMessage(), e);
            // DENY on error to be safe
            throw new RuntimeException("Access validation failed. Please contact system administrator.");
        }
    }

    /**
     * Check if a path matches an allowed pattern
     * Supports:
     * - Exact matches: "/create-bio-data"
     * - Path prefix with /** : "/plx/api/gen/**" (matches any path under that prefix)
     * - Wildcard in the middle: "/plx/api/gen/create-bio-data"
     */
    private boolean isEndpointMatch(String actualPath, String allowedPattern) {
        if (actualPath == null || allowedPattern == null) {
            return false;
        }

        actualPath = actualPath.trim();
        String pattern = allowedPattern.trim();

        // Clean the pattern - remove any brackets or quotes that might have been stored
        pattern = pattern.replace("[", "").replace("]", "").replace("\"", "");

        // Handle /** which means allow all paths
        if ("/**".equals(pattern)) {
            return true;
        }

        // Handle empty pattern
        if (pattern.isEmpty()) {
            return true;
        }

        // Exact match
        if (actualPath.equals(pattern)) {
            return true;
        }

        // Path prefix match (if pattern ends with /**)
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            // Remove trailing slash if present for cleaner matching
            if (prefix.endsWith("/")) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
            // Check if actual path starts with the prefix
            if (prefix.isEmpty()) {
                return true;
            }
            // Special handling for paths with variable segments (like API IDs)
            if (actualPath.startsWith(prefix)) {
                log.debug("Path {} matches prefix pattern {}", actualPath, pattern);
                return true;
            }
        }

        // Simple wildcard match (single *)
        if (pattern.contains("*") && !pattern.contains("/**")) {
            // Convert to simple regex - escape regex special characters except *
            String regexPattern = pattern
                    .replace(".", "\\.")
                    .replace("?", "\\?")
                    .replace("+", "\\+")
                    .replace("[", "\\[")
                    .replace("]", "\\]")
                    .replace("{", "\\{")
                    .replace("}", "\\}")
                    .replace("(", "\\(")
                    .replace(")", "\\)")
                    .replace("*", ".*");

            try {
                boolean matches = actualPath.matches(regexPattern);
                if (matches) {
                    log.debug("Path {} matches wildcard pattern {}", actualPath, pattern);
                }
                return matches;
            } catch (Exception e) {
                log.warn("Regex error for pattern {}: {}", pattern, e.getMessage());
                // Fall back to simple contains check
                return actualPath.contains(pattern.replace("*", ""));
            }
        }

        return false;
    }

    /**
     * Check if an IP address falls within a CIDR range, matches an exact IP, or matches a wildcard pattern
     * Supports:
     * - Exact IP: "192.168.1.100"
     * - CIDR notation: "192.168.1.0/24"
     * - Wildcard patterns: "192.168.1.*", "192.168.*.*", "192.*.*.*", "192.168.*"
     * - Range notation: "192.168.1.1-192.168.1.100"
     * @param ipAddress The client IP address to check
     * @param ipRange The IP range/pattern to check against
     * @return true if the IP matches the range/pattern, false otherwise
     */
    private boolean isIpInRange(String ipAddress, String ipRange) {
        if (ipAddress == null || ipRange == null) {
            return false;
        }

        ipAddress = ipAddress.trim();
        ipRange = ipRange.trim();

        // Check for exact IP match
        if (ipAddress.equals(ipRange)) {
            return true;
        }

        // Check for CIDR notation (e.g., "192.168.1.0/24")
        if (ipRange.contains("/")) {
            try {
                String[] parts = ipRange.split("/");
                String networkAddress = parts[0];
                int prefixLength = Integer.parseInt(parts[1]);

                // Convert IPs to integers for comparison
                int ipInt = ipToInt(ipAddress);
                int networkInt = ipToInt(networkAddress);

                // Calculate mask based on prefix length
                int mask = prefixLength == 0 ? 0 : (0xFFFFFFFF << (32 - prefixLength));

                // Check if IP is in the network range
                return (ipInt & mask) == (networkInt & mask);
            } catch (Exception e) {
                log.warn("Failed to parse CIDR range: {}", ipRange, e);
                return false;
            }
        }

        // Check for IP range notation (e.g., "192.168.1.1-192.168.1.100")
        if (ipRange.contains("-")) {
            try {
                String[] parts = ipRange.split("-");
                String startIp = parts[0].trim();
                String endIp = parts[1].trim();

                int startInt = ipToInt(startIp);
                int endInt = ipToInt(endIp);
                int ipInt = ipToInt(ipAddress);

                return ipInt >= startInt && ipInt <= endInt;
            } catch (Exception e) {
                log.warn("Failed to parse IP range: {}", ipRange, e);
                return false;
            }
        }

        // Check for wildcard pattern (e.g., "192.168.1.*", "192.168.*.*", "192.168.*")
        if (ipRange.contains("*")) {
            // Handle partial wildcard like "192.168.*" (meaning 192.168.x.x)
            String normalizedRange = ipRange;
            String[] rangeParts = normalizedRange.split("\\.");

            // If pattern has fewer than 4 parts, pad with *
            if (rangeParts.length < 4) {
                StringBuilder padded = new StringBuilder();
                for (int i = 0; i < rangeParts.length; i++) {
                    if (i > 0) padded.append(".");
                    padded.append(rangeParts[i]);
                }
                for (int i = rangeParts.length; i < 4; i++) {
                    padded.append(".*");
                }
                normalizedRange = padded.toString();
            }

            // Use simple wildcard matching
            return matchWildcardSimple(ipAddress, normalizedRange);
        }

        return false;
    }

    /**
     * Simple wildcard matching for IP addresses
     * Supports patterns like "192.168.1.*" and "192.168.*.*"
     */
    private boolean matchWildcardSimple(String ipAddress, String ipRange) {
        String[] ipParts = ipAddress.split("\\.");
        String[] rangeParts = ipRange.split("\\.");

        if (ipParts.length != 4 || rangeParts.length != 4) {
            return false;
        }

        for (int i = 0; i < 4; i++) {
            if (rangeParts[i].equals("*")) {
                continue; // Wildcard matches anything
            }
            if (!ipParts[i].equals(rangeParts[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Convert IP address string to integer for CIDR calculations
     */
    private int ipToInt(String ipAddress) {
        try {
            String[] parts = ipAddress.split("\\.");
            if (parts.length != 4) {
                return 0;
            }
            int result = 0;
            for (int i = 0; i < 4; i++) {
                result |= (Integer.parseInt(parts[i]) << (24 - (8 * i)));
            }
            return result;
        } catch (Exception e) {
            log.warn("Failed to convert IP to int: {}", ipAddress, e);
            return 0;
        }
    }

    /**
     * Get the actual client IP address from HttpServletRequest
     * This handles proxies and load balancers properly
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // If multiple IPs in X-Forwarded-For, take the first one (original client)
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress != null ? ipAddress : "unknown";
    }
}