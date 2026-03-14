package com.usg.apiAutomation.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.dtos.collections.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.collections.*;
import com.usg.apiAutomation.entities.postgres.collections.RequestEntity;
import com.usg.apiAutomation.repositories.postgres.apiGenerationEngine.ApiParameterRepository;
import com.usg.apiAutomation.repositories.postgres.apiGenerationEngine.GeneratedAPIRepository;
import com.usg.apiAutomation.repositories.postgres.collections.*;
import com.usg.apiAutomation.utils.LoggerUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CollectionsService {

    private final LoggerUtil loggerUtil;
    private final CollectionRepository collectionRepository;
    private final FolderRepository folderRepository;
    private final RequestRepository requestRepository;
    private final HeaderRepository headerRepository;
    private final AuthConfigRepository authConfigRepository;
    private final ParameterRepository parameterRepository;
    private final ApiParameterRepository apiParameterRepository;
    private final VariableRepository variableRepository;
    private final EnvironmentRepository environmentRepository;
    // Add this repository
    private final GeneratedAPIRepository generatedAPIRepository;

    // Add RestTemplate as a dependency
    private final RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        log.info("CollectionsService initialized with database");
    }

    // ========== PUBLIC SERVICE METHODS ==========

    public CollectionsListResponseDTO getCollectionsList(String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Getting collections list for user: {}", requestId, performedBy);
        loggerUtil.log("collections",
                "Request ID: " + requestId + ", Getting collections list for user: " + performedBy);

        // Get from database
        List<CollectionEntity> collections = collectionRepository.findAll();
        List<CollectionDTO> collectionDTOs = collections.stream()
                .map(this::mapToCollectionDTO)
                .collect(Collectors.toList());

        log.info("Request ID: {}, Retrieved {} collections from database", requestId, collectionDTOs.size());

        return new CollectionsListResponseDTO(collectionDTOs, collectionDTOs.size());
    }


    public CollectionDetailsResponseDTO getCollectionDetails(String requestId, HttpServletRequest req, String performedBy,
                                                             String collectionId) {
        log.info("Request ID: {}, Getting collection details for: {}", requestId, collectionId);

        // Get collection without fetching relationships
        CollectionEntity collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new EntityNotFoundException("Collection not found: " + collectionId));

        // Create response DTO
        CollectionDetailsResponseDTO details = new CollectionDetailsResponseDTO();
        details.setCollectionId(collection.getId());
        details.setName(collection.getName());
        details.setDescription(collection.getDescription());
        details.setCreatedAt(collection.getCreatedAt() != null ?
                collection.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        details.setUpdatedAt(collection.getUpdatedAt() != null ?
                collection.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        details.setFavorite(collection.isFavorite());
        details.setOwner(collection.getOwner());
        details.setComments(collection.getComments());
        details.setLastActivity(collection.getLastActivity() != null ?
                collection.getLastActivity().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);

        // Fetch folders
        List<FolderEntity> folders = folderRepository.findByCollectionId(collectionId);
        List<FolderDTO> folderDTOs = new ArrayList<>();

        int totalRequests = 0;

        for (FolderEntity folder : folders) {
            FolderDTO folderDTO = new FolderDTO();
            folderDTO.setId(folder.getId());
            folderDTO.setName(folder.getName());
            folderDTO.setDescription(folder.getDescription());
            folderDTO.setExpanded(folder.isExpanded());
            folderDTO.setEditing(folder.isEditing());

            // Fetch request summaries for this folder (without relationships)
            List<RequestSummaryDTO> requestSummaries = requestRepository.findRequestSummariesByFolderId(folder.getId());
            List<RequestDTO> requestDTOs = new ArrayList<>();

            for (RequestSummaryDTO summary : requestSummaries) {
                RequestDTO requestDTO = new RequestDTO();
                requestDTO.setId(summary.getId());
                requestDTO.setName(summary.getName());
                requestDTO.setMethod(summary.getMethod());
                requestDTO.setUrl(summary.getUrl());
                requestDTO.setDescription(summary.getDescription());
                requestDTO.setAuthType(summary.getAuthType());
                requestDTO.setBody(summary.getBody());
                requestDTO.setTests(summary.getTests());
                requestDTO.setPreRequestScript(summary.getPreRequestScript());
                requestDTO.setSaved(summary.isSaved());
                requestDTO.setCollectionId(collectionId);
                requestDTO.setFolderId(folder.getId());

                // IMPORTANT: Set both timestamps
                if (summary.getLastModified() != null) {
                    requestDTO.setLastModified(summary.getLastModified().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                if (summary.getCreatedAt() != null) {
                    requestDTO.setCreatedAt(summary.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }

                // Fetch headers as DTOs
                List<HeaderDTO> headers = headerRepository.findHeaderDTOsByRequestId(summary.getId());
                requestDTO.setHeaders(headers);

                // Fetch params as DTOs
                List<ParameterDTO> params = parameterRepository.findParameterDTOsByRequestId(summary.getId());
                requestDTO.setParams(params);

                // Fetch auth config as DTO
                authConfigRepository.findAuthConfigDTOByRequestId(summary.getId()).ifPresent(requestDTO::setAuth);

                requestDTOs.add(requestDTO);
            }

            folderDTO.setRequests(requestDTOs);
            folderDTO.setRequestCount(requestDTOs.size());
            folderDTOs.add(folderDTO);

            totalRequests += requestDTOs.size();
        }

        details.setFolders(folderDTOs);
        details.setTotalFolders(folders.size());
        details.setTotalRequests(totalRequests);

        // Fetch variables
        if (collection.getVariables() != null) {
            List<VariableDTO> variableDTOs = collection.getVariables().stream()
                    .map(this::mapToVariableDTO)
                    .collect(Collectors.toList());
            details.setVariables(variableDTOs);
        }

        log.info("Request ID: {}, Retrieved details for collection: {} with {} folders and {} requests",
                requestId, collectionId, folders.size(), totalRequests);

        return details;
    }


    public GenerateApiRequestDTO getRequestDetails(String requestId, HttpServletRequest req, String performedBy,
                                                   String collectionId, String requestIdParam) {
        log.info("Request ID: {}, Getting request details for: {}", requestId, requestIdParam);

        // Get basic request entity (without fetching relationships)
        RequestEntity request = requestRepository.findById(requestIdParam)
                .orElseThrow(() -> new EntityNotFoundException("Request not found: " + requestIdParam));

        System.out.println("collectionId:::::" + collectionId);
        System.out.println("requestIdParam::::::" + requestIdParam);

        // ============= FIXED: Handle non-unique result gracefully =============
        Optional<GeneratedApiEntity> generatedApiOpt = Optional.empty();
        try {
            // Try to find the associated GeneratedApiEntity using the improved repository method
            generatedApiOpt = generatedAPIRepository.findByRequestId(requestIdParam);

            // If not found, try alternative method
            if (generatedApiOpt.isEmpty()) {
                generatedApiOpt = generatedAPIRepository.findByRequestIdUsingObjectName(requestIdParam);
            }

            // If still not found, try by stored requestId
            if (generatedApiOpt.isEmpty()) {
                generatedApiOpt = generatedAPIRepository.findByStoredRequestId(requestIdParam);
            }

            // If still not found, try to find by collection and folder
            if (generatedApiOpt.isEmpty() && request.getCollection() != null) {
                List<GeneratedApiEntity> apisByCollection = generatedAPIRepository
                        .findByCollectionId(request.getCollection().getId());

                if (!apisByCollection.isEmpty()) {
                    // Try to find the most relevant one
                    generatedApiOpt = apisByCollection.stream()
                            .filter(api -> {
                                if (request.getFolder() != null) {
                                    String folderId = api.getCollectionInfo() != null ?
                                            (String) api.getCollectionInfo().get("folderId") : null;
                                    return request.getFolder().getId().equals(folderId);
                                }
                                return true;
                            })
                            .findFirst();
                }
            }
        } catch (IncorrectResultSizeDataAccessException e) {
            log.warn("Request ID: {}, Multiple results found for request: {}, using first result",
                    requestId, requestIdParam);

            // Handle the non-unique result by taking the first one
            List<GeneratedApiEntity> allMatches = generatedAPIRepository.findAll().stream()
                    .filter(api -> {
                        if (api.getCollectionInfo() != null && request.getCollection() != null) {
                            String apiCollectionId = (String) api.getCollectionInfo().get("collectionId");
                            return request.getCollection().getId().equals(apiCollectionId);
                        }
                        return false;
                    })
                    .collect(Collectors.toList());

            if (!allMatches.isEmpty()) {
                generatedApiOpt = Optional.of(allMatches.get(0));
            }
        }

        System.out.println("generatedApiOpt1111111111111::::" + generatedApiOpt);

        // Create GenerateApiRequestDTO - all fields are flattened in the DTO
        GenerateApiRequestDTO generateApiRequest = new GenerateApiRequestDTO();

        // ============= 1. SET FLATTENED API DETAILS FIELDS =============
        generateApiRequest.setApiName(request.getName());
        generateApiRequest.setApiCode(extractApiCode(request));
        generateApiRequest.setDescription(request.getDescription());

        if (generatedApiOpt.isPresent()) {
            GeneratedApiEntity generatedApi = generatedApiOpt.get();
            generateApiRequest.setVersion(generatedApi.getVersion());
            generateApiRequest.setStatus(generatedApi.getStatus());
            generateApiRequest.setTags(generatedApi.getTags() != null ? generatedApi.getTags() : List.of("default"));
            generateApiRequest.setCategory(generatedApi.getCategory() != null ? generatedApi.getCategory() : "general");
            generateApiRequest.setOwner(generatedApi.getOwner());
            generateApiRequest.setHttpMethod(generatedApi.getHttpMethod());
            generateApiRequest.setBasePath(generatedApi.getBasePath());
            generateApiRequest.setEndpointPath(generatedApi.getEndpointPath());
        } else {
            // No GeneratedApi found - this could be a manually created request
            log.debug("No GeneratedApiEntity found for request: {}", requestIdParam);
            generateApiRequest.setHttpMethod(request.getMethod());
            generateApiRequest.setVersion("1.0.0");
            generateApiRequest.setStatus("DRAFT");
            generateApiRequest.setTags(List.of("default"));
            generateApiRequest.setCategory("general");
            generateApiRequest.setOwner(performedBy);

            // Parse base path and endpoint from URL
            if (request.getUrl() != null) {
                String url = request.getUrl();
                // Remove baseUrl placeholder if present
                String cleanUrl = url.replace("{{baseUrl}}", "").replace("{{base_url}}", "");

                // Split into base path and endpoint
                int lastSlash = cleanUrl.lastIndexOf('/');
                if (lastSlash > 0) {
                    generateApiRequest.setBasePath(cleanUrl.substring(0, lastSlash));
                    generateApiRequest.setEndpointPath(cleanUrl.substring(lastSlash));
                } else {
                    generateApiRequest.setBasePath("");
                    generateApiRequest.setEndpointPath(cleanUrl);
                }
            }
        }

        // ============= 2. SCHEMA CONFIG =============
        if (generatedApiOpt.isPresent()) {
            String apiId = generatedApiOpt.get().getId();
            try {
                Optional<ApiSchemaConfigEntity> schemaConfigOpt = generatedAPIRepository.findSchemaConfigByApiId(apiId);
                if (schemaConfigOpt.isPresent()) {
                    ApiSchemaConfigEntity schemaConfigEntity = schemaConfigOpt.get();
                    ApiSchemaConfigDTO schemaConfig = new ApiSchemaConfigDTO();

                    schemaConfig.setSchemaName(schemaConfigEntity.getSchemaName());
                    schemaConfig.setObjectType(schemaConfigEntity.getObjectType());
                    schemaConfig.setObjectName(schemaConfigEntity.getObjectName());
                    schemaConfig.setOperation(schemaConfigEntity.getOperation());
                    schemaConfig.setPrimaryKeyColumn(schemaConfigEntity.getPrimaryKeyColumn());
                    schemaConfig.setSequenceName(schemaConfigEntity.getSequenceName());
                    schemaConfig.setEnablePagination(schemaConfigEntity.getEnablePagination());
                    schemaConfig.setPageSize(schemaConfigEntity.getPageSize());
                    schemaConfig.setEnableSorting(schemaConfigEntity.getEnableSorting());
                    schemaConfig.setDefaultSortColumn(schemaConfigEntity.getDefaultSortColumn());
                    schemaConfig.setDefaultSortDirection(schemaConfigEntity.getDefaultSortDirection());
                    schemaConfig.setIsSynonym(schemaConfigEntity.getIsSynonym());
                    schemaConfig.setTargetType(schemaConfigEntity.getTargetType());
                    schemaConfig.setTargetName(schemaConfigEntity.getTargetName());
                    schemaConfig.setTargetOwner(schemaConfigEntity.getTargetOwner());

                    generateApiRequest.setSchemaConfig(schemaConfig);
                }
            } catch (Exception e) {
                log.warn("Error fetching schema config for API {}: {}", apiId, e.getMessage());
            }
        }

        // ============= 3. COLLECTION INFO =============
        CollectionInfoDTO collectionInfo = new CollectionInfoDTO();
        collectionInfo.setCollectionId(request.getCollection() != null ? request.getCollection().getId() : null);
        collectionInfo.setCollectionName(request.getCollection() != null ? request.getCollection().getName() : null);
        collectionInfo.setCollectionType(extractCollectionType(request));
        collectionInfo.setFolderId(request.getFolder() != null ? request.getFolder().getId() : null);
        collectionInfo.setFolderName(request.getFolder() != null ? request.getFolder().getName() : null);
        generateApiRequest.setCollectionInfo(collectionInfo);

        // ============= 4. REQUEST PARAMETERS =============
        List<ApiParameterDTO> parameters = new ArrayList<>();

        System.out.println("requestIdParam::::::::::" + requestIdParam);

        try {
            // Try from collections parameters first
            List<ParameterEntity> paramEntities = parameterRepository.findByRequestId(requestIdParam);
            if (paramEntities != null && !paramEntities.isEmpty()) {
                for (ParameterEntity paramEntity : paramEntities) {
                    ApiParameterDTO apiParam = mapParameterEntityToApiDTO(paramEntity);
                    parameters.add(apiParam);
                }
            }

            // If no parameters from collections, try from apiParameterRepository
            if (parameters.isEmpty() && generatedApiOpt.isPresent()) {
                List<ApiParameterEntity> parameterEntities = apiParameterRepository.findByGeneratedApiId(generatedApiOpt.get().getId());
                if (parameterEntities != null && !parameterEntities.isEmpty()) {
                    for (ApiParameterEntity paramEntity : parameterEntities) {
                        ApiParameterDTO apiParam = mapApiParameterEntityToDTO(paramEntity);
                        parameters.add(apiParam);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error fetching parameters for request {}: {}", requestIdParam, e.getMessage());
        }

        generateApiRequest.setParameters(parameters.isEmpty() ? null : parameters);

        // ============= 5. RESPONSE FIELDS (MAPPINGS) =============
        List<ApiResponseMappingDTO> responseMappings = new ArrayList<>();

        if (generatedApiOpt.isPresent()) {
            try {
                String apiId = generatedApiOpt.get().getId();
                List<ApiResponseMappingEntity> mappingEntities = generatedAPIRepository.findResponseMappingsByApiId(apiId);
                if (mappingEntities != null && !mappingEntities.isEmpty()) {
                    for (ApiResponseMappingEntity mappingEntity : mappingEntities) {
                        ApiResponseMappingDTO mapping = mapResponseMappingEntityToDTO(mappingEntity);
                        responseMappings.add(mapping);
                    }
                }
            } catch (Exception e) {
                log.warn("Error fetching response mappings for API {}: {}", generatedApiOpt.get().getId(), e.getMessage());
            }
        }
        generateApiRequest.setResponseMappings(responseMappings.isEmpty() ? null : responseMappings);

        // ============= 6. HEADERS =============
        List<ApiHeaderDTO> headers = new ArrayList<>();

        // Try from collections headers first
        try {
            List<HeaderEntity> headerEntities = headerRepository.findByRequestId(requestIdParam);
            if (headerEntities != null && !headerEntities.isEmpty()) {
                for (HeaderEntity headerEntity : headerEntities) {
                    ApiHeaderDTO apiHeader = mapHeaderEntityToApiDTO(headerEntity);
                    headers.add(apiHeader);
                }
            }

            // If no headers from collections, try from GeneratedApi
            if (headers.isEmpty() && generatedApiOpt.isPresent()) {
                String apiId = generatedApiOpt.get().getId();
                List<ApiHeaderEntity> headerEntities2 = generatedAPIRepository.findHeadersByApiId(apiId);
                if (headerEntities2 != null && !headerEntities2.isEmpty()) {
                    for (ApiHeaderEntity headerEntity : headerEntities2) {
                        ApiHeaderDTO apiHeader = mapApiHeaderEntityToDTO(headerEntity);
                        headers.add(apiHeader);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error fetching headers for request {}: {}", requestIdParam, e.getMessage());
        }

        // ============= INJECT API KEY HEADERS IF AUTH TYPE IS API KEY =============
        // Check if auth config exists and is of type API Key
        boolean isApiKeyAuth = false;

        // First check if we have auth config from GeneratedApi
        if (generatedApiOpt.isPresent()) {
            try {
                String apiId = generatedApiOpt.get().getId();
                Optional<ApiAuthConfigEntity> authConfigOpt = generatedAPIRepository.findAuthConfigByApiId(apiId);
                if (authConfigOpt.isPresent() && "API_KEY".equals(authConfigOpt.get().getAuthType())) {
                    isApiKeyAuth = true;
                }
            } catch (Exception e) {
                log.warn("Error checking auth config for API Key type: {}", e.getMessage());
            }
        }

        // If not found in GeneratedApi, check collections auth config
        if (!isApiKeyAuth) {
            try {
                Optional<AuthConfigEntity> authConfigEntityOpt = authConfigRepository.findByRequestId(requestIdParam);
                System.out.println("authConfigEntityOpt:::::::::" + authConfigEntityOpt);
                if (authConfigEntityOpt.isPresent() && "apiKey".equals(authConfigEntityOpt.get().getType())) {
                    isApiKeyAuth = true;
                }
            } catch (Exception e) {
                log.warn("Error checking collections auth config for API Key type: {}", e.getMessage());
            }
        }

        // If auth type is API Key, inject X-Api-Key and X-Api-Secret headers
        if (isApiKeyAuth) {
            log.info("Auth type is API Key for request: {}, injecting X-Api-Key and X-Api-Secret headers", requestIdParam);

            // Check if X-Api-Key header already exists
            boolean hasApiKeyHeader = headers.stream()
                    .anyMatch(h -> "X-Api-Key".equalsIgnoreCase(h.getKey()));

            if (!hasApiKeyHeader) {
                ApiHeaderDTO apiKeyHeader = new ApiHeaderDTO();
                apiKeyHeader.setKey("X-Api-Key");
                apiKeyHeader.setValue("{{api_key}}"); // Placeholder that will be replaced at runtime
                apiKeyHeader.setDescription("API Key for authentication");
                apiKeyHeader.setRequired(true);
                headers.add(apiKeyHeader);
            }

            // Check if X-Api-Secret header already exists
            boolean hasApiSecretHeader = headers.stream()
                    .anyMatch(h -> "X-Api-Secret".equalsIgnoreCase(h.getKey()));

            if (!hasApiSecretHeader) {
                ApiHeaderDTO apiSecretHeader = new ApiHeaderDTO();
                apiSecretHeader.setKey("X-Api-Secret");
                apiSecretHeader.setValue("{{api_secret}}"); // Placeholder that will be replaced at runtime
                apiSecretHeader.setDescription("API Secret for authentication");
                apiSecretHeader.setRequired(true);
                headers.add(apiSecretHeader);
            }
        }

        generateApiRequest.setHeaders(headers.isEmpty() ? null : headers);

        System.out.println("generatedApiOpt22222222:::::" + generatedApiOpt);

        // ============= 7. REQUEST BODY =============
        if (generatedApiOpt.isPresent()) {
            System.out.println("adey here cool...");
            try {
                String apiId = generatedApiOpt.get().getId();
                Optional<ApiRequestConfigEntity> requestConfigOpt = generatedAPIRepository.findRequestConfigByApiId(apiId);
                if (requestConfigOpt.isPresent()) {
                    ApiRequestConfigEntity requestConfigEntity = requestConfigOpt.get();
                    ApiRequestConfigDTO requestBody = new ApiRequestConfigDTO();

                    requestBody.setBodyType(requestConfigEntity.getBodyType());
                    requestBody.setSample(requestConfigEntity.getSample());
                    requestBody.setRequiredFields(requestConfigEntity.getRequiredFields());
                    requestBody.setValidateSchema(requestConfigEntity.getValidateSchema());
                    requestBody.setMaxSize(requestConfigEntity.getMaxSize());

                    if (requestConfigEntity.getAllowedMediaTypes() != null) {
                        requestBody.setAllowedMediaTypes(Arrays.asList(requestConfigEntity.getAllowedMediaTypes().split(",")));
                    }

                    generateApiRequest.setRequestBody(requestBody);
                }
            } catch (Exception e) {
                log.warn("Error fetching request config for API {}: {}", generatedApiOpt.get().getId(), e.getMessage());
            }
        }

        System.out.println("generateApiRequest:::::" + generateApiRequest);
        System.out.println("generateApiRequest.getRequestBody() is: " + generateApiRequest.getRequestBody());
        System.out.println("request.getBody() is: " + request.getBody());
        System.out.println("request.getBody() empty? " + (request.getBody() == null || request.getBody().isEmpty()));

        // If no request config from GeneratedApi, use request body from collections module
        if (generateApiRequest.getRequestBody() == null && request.getBody() != null && !request.getBody().isEmpty()) {
            System.out.println("case eeiii...");
            ApiRequestConfigDTO requestBody = new ApiRequestConfigDTO();
            requestBody.setBodyType("json");
            requestBody.setSample(request.getBody());
            requestBody.setRequiredFields(new ArrayList<>());
            requestBody.setValidateSchema(true);
            requestBody.setMaxSize(1048576L);
            requestBody.setAllowedMediaTypes(List.of("application/json"));
            generateApiRequest.setRequestBody(requestBody);
        }

        // ============= 8. RESPONSE BODY =============
        if (generatedApiOpt.isPresent()) {
            try {
                String apiId = generatedApiOpt.get().getId();
                Optional<ApiResponseConfigEntity> responseConfigOpt = generatedAPIRepository.findResponseConfigByApiId(apiId);
                if (responseConfigOpt.isPresent()) {
                    ApiResponseConfigEntity responseConfigEntity = responseConfigOpt.get();
                    ApiResponseConfigDTO responseBody = new ApiResponseConfigDTO();

                    responseBody.setSuccessSchema(responseConfigEntity.getSuccessSchema());
                    responseBody.setErrorSchema(responseConfigEntity.getErrorSchema());
                    responseBody.setIncludeMetadata(responseConfigEntity.getIncludeMetadata());
                    responseBody.setMetadataFields(responseConfigEntity.getMetadataFields());
                    responseBody.setContentType(responseConfigEntity.getContentType());
                    responseBody.setCompression(responseConfigEntity.getCompression());

                    generateApiRequest.setResponseBody(responseBody);
                }
            } catch (Exception e) {
                log.warn("Error fetching response config for API {}: {}", generatedApiOpt.get().getId(), e.getMessage());
            }
        }

        // ============= 9. RESPONSE EXAMPLES =============
        Map<String, Object> responseExamples = new HashMap<>();

        if (generatedApiOpt.isPresent()) {
            try {
                String apiId = generatedApiOpt.get().getId();
                Optional<ApiResponseConfigEntity> responseConfigOpt = generatedAPIRepository.findResponseConfigByApiId(apiId);
                if (responseConfigOpt.isPresent()) {
                    ApiResponseConfigEntity responseConfigEntity = responseConfigOpt.get();

                    // Parse success schema if it exists
                    if (responseConfigEntity.getSuccessSchema() != null) {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            responseExamples.put("success", mapper.readValue(responseConfigEntity.getSuccessSchema(), Map.class));
                        } catch (Exception e) {
                            log.error("Failed to parse success schema", e);
                            // Create default success example from mappings
                            if (!responseMappings.isEmpty()) {
                                responseExamples.put("success", createSuccessExampleFromMappings(responseMappings));
                            }
                        }
                    }

                    // Parse error schema if it exists
                    if (responseConfigEntity.getErrorSchema() != null) {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            responseExamples.put("error", mapper.readValue(responseConfigEntity.getErrorSchema(), Map.class));
                        } catch (Exception e) {
                            log.error("Failed to parse error schema", e);
                            responseExamples.put("error", createDefaultErrorExample());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error fetching response examples for API {}: {}", generatedApiOpt.get().getId(), e.getMessage());
            }
        }

        // If no examples, create defaults
        if (responseExamples.isEmpty()) {
            if (!responseMappings.isEmpty()) {
                responseExamples.put("success", createSuccessExampleFromMappings(responseMappings));
            } else {
                Map<String, Object> defaultSuccess = new HashMap<>();
                defaultSuccess.put("success", true);
                defaultSuccess.put("message", "Request processed successfully");
                defaultSuccess.put("data", new HashMap<>());
                responseExamples.put("success", defaultSuccess);
            }
            responseExamples.put("error", createDefaultErrorExample());
        }

        generateApiRequest.setResponseExamples(responseExamples);

        // ============= 10. AUTH CONFIG =============
        if (generatedApiOpt.isPresent()) {
            try {
                String apiId = generatedApiOpt.get().getId();
                Optional<ApiAuthConfigEntity> authConfigOpt = generatedAPIRepository.findAuthConfigByApiId(apiId);
                if (authConfigOpt.isPresent()) {
                    ApiAuthConfigEntity authConfigEntity = authConfigOpt.get();
                    ApiAuthConfigDTO authConfig = mapApiAuthConfigEntityToDTO(authConfigEntity);
                    generateApiRequest.setAuthConfig(authConfig);
                }
            } catch (Exception e) {
                log.warn("Error fetching auth config for API {}: {}", generatedApiOpt.get().getId(), e.getMessage());
            }
        }

        // If no auth config from GeneratedApi, try from collections module
        if (generateApiRequest.getAuthConfig() == null) {
            try {
                Optional<AuthConfigEntity> authConfigEntityOpt = authConfigRepository.findByRequestId(requestIdParam);
                if (authConfigEntityOpt.isPresent()) {
                    AuthConfigEntity authConfigEntity = authConfigEntityOpt.get();
                    ApiAuthConfigDTO authConfig = mapAuthConfigEntityToApiDTO(authConfigEntity);
                    generateApiRequest.setAuthConfig(authConfig);
                }
            } catch (Exception e) {
                log.warn("Error fetching auth config from collections for request {}: {}", requestIdParam, e.getMessage());
            }
        }

        // ============= 11. SETTINGS =============
        if (generatedApiOpt.isPresent()) {
            try {
                String apiId = generatedApiOpt.get().getId();
                Optional<ApiSettingsEntity> settingsOpt = generatedAPIRepository.findSettingsByApiId(apiId);
                if (settingsOpt.isPresent()) {
                    ApiSettingsEntity settingsEntity = settingsOpt.get();
                    ApiSettingsDTO settings = mapApiSettingsEntityToDTO(settingsEntity);
                    generateApiRequest.setSettings(settings);
                }
            } catch (Exception e) {
                log.warn("Error fetching settings for API {}: {}", generatedApiOpt.get().getId(), e.getMessage());
            }
        }

        // ============= 12. SOURCE OBJECT INFO =============
        if (generatedApiOpt.isPresent() && generatedApiOpt.get().getSourceObjectInfo() != null) {
            generateApiRequest.setSourceObject(generatedApiOpt.get().getSourceObjectInfo());
        }

        // ============= 13. TESTS =============
        if (generatedApiOpt.isPresent()) {
            try {
                String apiId = generatedApiOpt.get().getId();
                List<ApiTestEntity> testEntities = generatedAPIRepository.findTestsByApiId(apiId);
                if (testEntities != null && !testEntities.isEmpty()) {
                    ApiTestsDTO tests = new ApiTestsDTO();
                    // Combine all test data
                    Map<String, Object> combinedTestData = new HashMap<>();
                    for (ApiTestEntity test : testEntities) {
                        if (test.getTestData() != null) {
                            combinedTestData.putAll(test.getTestData());
                        }
                    }

                    // Set common test fields
                    tests.setTestConnection((Boolean) combinedTestData.getOrDefault("testConnection", false));
                    tests.setTestObjectAccess((Boolean) combinedTestData.getOrDefault("testObjectAccess", false));
                    tests.setTestPrivileges((Boolean) combinedTestData.getOrDefault("testPrivileges", false));
                    tests.setTestDataTypes((Boolean) combinedTestData.getOrDefault("testDataTypes", false));
                    tests.setTestNullConstraints((Boolean) combinedTestData.getOrDefault("testNullConstraints", false));
                    tests.setTestUniqueConstraints((Boolean) combinedTestData.getOrDefault("testUniqueConstraints", false));
                    tests.setTestForeignKeyReferences((Boolean) combinedTestData.getOrDefault("testForeignKeyReferences", false));
                    tests.setTestQueryPerformance((Boolean) combinedTestData.getOrDefault("testQueryPerformance", false));
                    tests.setPerformanceThreshold((Integer) combinedTestData.getOrDefault("performanceThreshold", 1000));
                    tests.setTestWithSampleData((Boolean) combinedTestData.getOrDefault("testWithSampleData", false));
                    tests.setSampleDataRows((Integer) combinedTestData.getOrDefault("sampleDataRows", 10));
                    tests.setTestProcedureExecution((Boolean) combinedTestData.getOrDefault("testProcedureExecution", false));
                    tests.setTestFunctionReturn((Boolean) combinedTestData.getOrDefault("testFunctionReturn", false));
                    tests.setTestExceptionHandling((Boolean) combinedTestData.getOrDefault("testExceptionHandling", false));
                    tests.setTestSQLInjection((Boolean) combinedTestData.getOrDefault("testSQLInjection", false));
                    tests.setTestAuthentication((Boolean) combinedTestData.getOrDefault("testAuthentication", false));
                    tests.setTestAuthorization((Boolean) combinedTestData.getOrDefault("testAuthorization", false));
                    tests.setTestData(combinedTestData.get("testData") instanceof Map ?
                            (Map<String, Object>) combinedTestData.get("testData") : null);
                    tests.setTestQueries(combinedTestData.get("testQueries") instanceof List ?
                            (List<String>) combinedTestData.get("testQueries") : null);

                    generateApiRequest.setTests(tests);
                }
            } catch (Exception e) {
                log.warn("Error fetching tests for API {}: {}", generatedApiOpt.get().getId(), e.getMessage());
            }
        }

        // ============= 14. VALIDATION =============
        if (generatedApiOpt.isPresent()) {
            try {
                String apiId = generatedApiOpt.get().getId();
                Optional<ApiSchemaConfigEntity> schemaConfigOpt = generatedAPIRepository.findSchemaConfigByApiId(apiId);
                if (schemaConfigOpt.isPresent()) {
                    ApiSchemaConfigEntity schemaConfig = schemaConfigOpt.get();
                    Map<String, Object> validation = new HashMap<>();
                    validation.put("valid", true);
                    validation.put("exists", true);
                    validation.put("objectName", schemaConfig.getObjectName());
                    validation.put("objectType", schemaConfig.getObjectType());
                    validation.put("owner", schemaConfig.getSchemaName());
                    validation.put("isSynonym", schemaConfig.getIsSynonym());

                    Map<String, Object> details = new HashMap<>();
                    details.put("parameterCount", parameters != null ? parameters.size() : 0);
                    details.put("parameters", parameters);
                    validation.put("details", details);

                    generateApiRequest.setValidation(validation);
                }
            } catch (Exception e) {
                log.warn("Error creating validation for API {}: {}", generatedApiOpt.get().getId(), e.getMessage());
            }
        }

        // ============= 15. FLAGS =============
        generateApiRequest.setRegenerateComponents(null);
        generateApiRequest.setIsEditing(true);

        log.info("Request ID: {}, Retrieved details for request: {}", requestId, requestIdParam);

        return generateApiRequest;
    }

// ============= NEW HELPER METHOD TO MAP REQUEST TO API =============

    @Transactional
    public void mapRequestToApi(String requestId, String apiId, String performedBy) {
        log.info("Mapping request {} to API {}", requestId, apiId);

        RequestEntity request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found: " + requestId));

        GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                .orElseThrow(() -> new EntityNotFoundException("API not found: " + apiId));

        // Store the mapping in collection_info
        if (api.getCollectionInfo() == null) {
            api.setCollectionInfo(new HashMap<>());
        }

        Map<String, Object> collectionInfo = api.getCollectionInfo();
        collectionInfo.put("requestId", requestId);
        collectionInfo.put("collectionId", request.getCollection() != null ? request.getCollection().getId() : null);
        collectionInfo.put("folderId", request.getFolder() != null ? request.getFolder().getId() : null);
        collectionInfo.put("requestName", request.getName());
        collectionInfo.put("mappedAt", LocalDateTime.now().toString());
        collectionInfo.put("mappedBy", performedBy);

        api.setCollectionInfo(collectionInfo);
        generatedAPIRepository.save(api);

        log.info("Successfully mapped request {} to API {}", requestId, apiId);
    }

// ============= MAPPING HELPER METHODS =============

    private ApiParameterDTO mapParameterEntityToApiDTO(ParameterEntity entity) {
        ApiParameterDTO dto = new ApiParameterDTO();
        dto.setKey(entity.getKey());
        dto.setDbColumn(entity.getDbColumn());
        dto.setDbParameter(entity.getDbParameter());
        dto.setType(entity.getApiType());
        dto.setOracleType(entity.getOracleType());
        dto.setRequired(entity.getRequired());
        dto.setParameterLocation(entity.getParameterLocation());
        dto.setBodyFormat(entity.getInBody() != null && entity.getInBody() ? "json" : null);
        dto.setDescription(entity.getDescription());
        dto.setExample(entity.getExample());
        dto.setValidationPattern(entity.getValidationPattern());
        dto.setDefaultValue(entity.getDefaultValue());
        dto.setIsPrimaryKey(entity.getIsPrimaryKey());
        dto.setParamMode(entity.getParamMode());
        dto.setPosition(entity.getPosition());
        return dto;
    }

    private ApiParameterDTO mapApiParameterEntityToDTO(ApiParameterEntity entity) {
        ApiParameterDTO dto = new ApiParameterDTO();
        dto.setKey(entity.getKey());
        dto.setDbColumn(entity.getDbColumn());
        dto.setDbParameter(entity.getDbParameter());
        dto.setType(entity.getApiType());
        dto.setOracleType(entity.getOracleType());
        dto.setRequired(entity.getRequired());
        dto.setParameterLocation(entity.getParameterLocation());
        dto.setBodyFormat(entity.getInBody() != null && entity.getInBody() ? "json" : null);
        dto.setDescription(entity.getDescription());
        dto.setExample(entity.getExample());
        dto.setValidationPattern(entity.getValidationPattern());
        dto.setDefaultValue(entity.getDefaultValue());
        dto.setIsPrimaryKey(entity.getIsPrimaryKey());
        dto.setParamMode(entity.getParamMode());
        dto.setPosition(entity.getPosition());
        return dto;
    }

    private ApiResponseMappingDTO mapResponseMappingEntityToDTO(ApiResponseMappingEntity entity) {
        ApiResponseMappingDTO dto = new ApiResponseMappingDTO();
        dto.setApiField(entity.getApiField());
        dto.setDbColumn(entity.getDbColumn());
        dto.setOracleType(entity.getOracleType());
        dto.setApiType(entity.getApiType());
        dto.setFormat(entity.getFormat());
        dto.setNullable(entity.getNullable());
        dto.setIsPrimaryKey(entity.getIsPrimaryKey());
        dto.setIncludeInResponse(entity.getIncludeInResponse());
        dto.setInResponse(entity.getInResponse());
        dto.setPosition(entity.getPosition());
        return dto;
    }

    private ApiHeaderDTO mapHeaderEntityToApiDTO(HeaderEntity entity) {
        ApiHeaderDTO dto = new ApiHeaderDTO();
        dto.setKey(entity.getKey());
        dto.setValue(entity.getValue() != null ? entity.getValue() : "");
        dto.setDescription(entity.getDescription());
        dto.setRequired(entity.isEnabled());
        dto.setIsRequestHeader(true);
        dto.setIsResponseHeader(false);
        return dto;
    }

    private ApiHeaderDTO mapApiHeaderEntityToDTO(ApiHeaderEntity entity) {
        ApiHeaderDTO dto = new ApiHeaderDTO();
        dto.setKey(entity.getKey());
        dto.setValue(entity.getValue());
        dto.setDescription(entity.getDescription());
        dto.setRequired(entity.getRequired());
        dto.setIsRequestHeader(entity.getIsRequestHeader());
        dto.setIsResponseHeader(entity.getIsResponseHeader());
        return dto;
    }

    private ApiAuthConfigDTO mapApiAuthConfigEntityToDTO(ApiAuthConfigEntity entity) {
        ApiAuthConfigDTO dto = new ApiAuthConfigDTO();
        dto.setAuthType(entity.getAuthType());

        // API Key fields
        dto.setApiKeyHeader(entity.getApiKeyHeader());
        dto.setApiKeyValue(entity.getApiKeyValue());
        dto.setApiKeySecret(entity.getApiKeySecret());
        dto.setApiSecretHeader(entity.getApiSecretHeader());
        dto.setApiSecretValue(entity.getApiSecretValue());
        dto.setApiKeyLocation(entity.getApiKeyLocation());
        dto.setApiKeyPrefix(entity.getApiKeyPrefix());

        // Basic Auth fields
        dto.setBasicUsername(entity.getBasicUsername());
        dto.setBasicPassword(entity.getBasicPassword());
        dto.setBasicRealm(entity.getBasicRealm());

        // JWT fields
        dto.setJwtToken(entity.getJwtToken());
        dto.setJwtSecret(entity.getJwtSecret());
        dto.setJwtIssuer(entity.getJwtIssuer());
        dto.setJwtAudience(entity.getJwtAudience());
        dto.setJwtExpiration(entity.getJwtExpiration());
        dto.setJwtAlgorithm(entity.getJwtAlgorithm());

        // OAuth2 fields
        dto.setOauthClientId(entity.getOauthClientId());
        dto.setOauthClientSecret(entity.getOauthClientSecret());
        dto.setOauthTokenUrl(entity.getOauthTokenUrl());
        dto.setOauthAuthUrl(entity.getOauthAuthUrl());
        dto.setOauthScopes(entity.getOauthScopes());

        // Oracle Roles
        dto.setRequiredRoles(entity.getRequiredRoles());
        dto.setCustomAuthFunction(entity.getCustomAuthFunction());
        dto.setValidateSession(entity.getValidateSession());
        dto.setCheckObjectPrivileges(entity.getCheckObjectPrivileges());

        // Security settings
        dto.setIpWhitelist(entity.getIpWhitelist());
        dto.setRateLimitRequests(entity.getRateLimitRequests());
        dto.setRateLimitPeriod(entity.getRateLimitPeriod());
        dto.setEnableRateLimiting(entity.getEnableRateLimiting());
        dto.setAuditLevel(entity.getAuditLevel());

        if (entity.getCorsOrigins() != null) {
            dto.setCorsOrigins(Arrays.asList(entity.getCorsOrigins().split(",")));
        }
        dto.setCorsCredentials(entity.getCorsCredentials());

        return dto;
    }

    private ApiAuthConfigDTO mapAuthConfigEntityToApiDTO(AuthConfigEntity entity) {
        ApiAuthConfigDTO dto = new ApiAuthConfigDTO();
        dto.setAuthType(entity.getType());

        if ("api_key".equalsIgnoreCase(entity.getType())) {
            dto.setApiKeyHeader(entity.getKey());
            dto.setApiKeyValue(entity.getValue());
        } else if ("bearer".equalsIgnoreCase(entity.getType())) {
            dto.setJwtToken(entity.getToken());
        } else if ("basic".equalsIgnoreCase(entity.getType())) {
            dto.setBasicUsername(entity.getUsername());
            dto.setBasicPassword(entity.getPassword());
        }

        return dto;
    }

    private ApiSettingsDTO mapApiSettingsEntityToDTO(ApiSettingsEntity entity) {
        ApiSettingsDTO dto = new ApiSettingsDTO();
        dto.setTimeout(entity.getTimeout());
        dto.setMaxRecords(entity.getMaxRecords());
        dto.setEnableLogging(entity.getEnableLogging());
        dto.setLogLevel(entity.getLogLevel());
        dto.setEnableCaching(entity.getEnableCaching());
        dto.setCacheTtl(entity.getCacheTtl());
        dto.setEnableRateLimiting(entity.getEnableRateLimiting());
        dto.setRateLimit(entity.getRateLimit());
        dto.setRateLimitPeriod(entity.getRateLimitPeriod());
        dto.setEnableAudit(entity.getEnableAudit());
        dto.setAuditLevel(entity.getAuditLevel());
        dto.setGenerateSwagger(entity.getGenerateSwagger());
        dto.setGeneratePostman(entity.getGeneratePostman());
        dto.setGenerateClientSDK(entity.getGenerateClientSDK());
        dto.setEnableMonitoring(entity.getEnableMonitoring());
        dto.setEnableAlerts(entity.getEnableAlerts());
        dto.setAlertEmail(entity.getAlertEmail());
        dto.setEnableTracing(entity.getEnableTracing());
        dto.setCorsEnabled(entity.getCorsEnabled());

        if (entity.getCorsOrigins() != null) {
            dto.setCorsOrigins(Arrays.asList(entity.getCorsOrigins().split(",")));
        }

        return dto;
    }


    private Map<String, Object> createSuccessExampleFromMappings(List<ApiResponseMappingDTO> responseMappings) {
        Map<String, Object> successExample = new HashMap<>();
        successExample.put("success", true);

        if (responseMappings != null && !responseMappings.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            for (ApiResponseMappingDTO mapping : responseMappings) {
                // Add sample values based on type
                if (mapping.getApiType() != null) {
                    switch (mapping.getApiType().toLowerCase()) {
                        case "string":
                            data.put(mapping.getApiField(), "Sample " + mapping.getApiField());
                            break;
                        case "number":
                        case "integer":
                            data.put(mapping.getApiField(), 123);
                            break;
                        case "boolean":
                            data.put(mapping.getApiField(), true);
                            break;
                        case "date":
                            data.put(mapping.getApiField(), "2024-01-01");
                            break;
                        default:
                            data.put(mapping.getApiField(), "sample");
                    }
                } else {
                    data.put(mapping.getApiField(), "sample");
                }
            }
            successExample.put("data", data);
        } else {
            successExample.put("data", new HashMap<>());
        }

        successExample.put("message", "Request processed successfully");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("timestamp", "{{timestamp}}");
        metadata.put("apiVersion", "1.0.0");
        metadata.put("requestId", "{{requestId}}");
        successExample.put("metadata", metadata);

        return successExample;
    }

    private Map<String, Object> createDefaultErrorExample() {
        Map<String, Object> errorExample = new HashMap<>();
        errorExample.put("success", false);

        Map<String, Object> error = new HashMap<>();
        error.put("code", "ERR_001");
        error.put("message", "Error processing request");

        Map<String, Object> details = new HashMap<>();
        details.put("field", "exampleField");
        details.put("reason", "Invalid value provided");
        details.put("expectedType", "string");
        details.put("providedValue", null);
        error.put("details", details);

        errorExample.put("error", error);

        return errorExample;
    }



    // Helper methods remain the same
    private String extractApiCode(RequestEntity request) {
        return request.getName().toUpperCase().replace(" ", "_").replace("-", "_").replace("___", "_");
    }

    private List<String> extractTags(RequestEntity request) {
        return List.of("default");
    }

    private String extractCategory(RequestEntity request) {
        return "general";
    }

    private String extractCollectionType(RequestEntity request) {
        return "general";
    }



    public ExecuteRequestResponseDTO executeRequest(String requestId, HttpServletRequest req, String performedBy,
                                                    ExecuteRequestDTO requestDto) {
        log.info("Request ID: {}, Executing request for user: {}", requestId, performedBy);
        loggerUtil.log("collections",
                "Request ID: " + requestId + ", Executing request: " + requestDto.getMethod() + " " + requestDto.getUrl());

        // Try to find the API by its URL pattern
        String url = requestDto.getUrl();
        String endpointPath = extractEndpointPath(url);

        // Look up the API by endpoint path
        Optional<GeneratedApiEntity> apiOpt = generatedAPIRepository.findByEndpointPath(endpointPath);

        if (apiOpt.isPresent()) {
            GeneratedApiEntity api = apiOpt.get();
            log.info("Request ID: {}, Found API configuration for endpoint: {}", requestId, endpointPath);

            // Apply the API's authentication configuration
            applyApiAuthentication(requestDto, api);
        } else {
            log.debug("Request ID: {}, No API configuration found for endpoint: {}, falling back to passthrough",
                    requestId, endpointPath);

            // Fallback to passthrough authentication
            passthroughAuthentication(requestDto, req);
        }

        ExecuteRequestResponseDTO response = executeActualRequest(requestDto);

        log.info("Request ID: {}, Request executed successfully, status: {}",
                requestId, response.getStatusCode());

        return response;
    }

    private String extractEndpointPath(String url) {
        try {
            // Remove protocol and domain
            String withoutProtocol = url.replaceAll("^https?://[^/]+", "");
            // Remove query parameters
            return withoutProtocol.replaceAll("\\?.*$", "");
        } catch (Exception e) {
            return url;
        }
    }


    private void passthroughAuthentication(ExecuteRequestDTO requestDto, HttpServletRequest req) {
        if (req == null) return;

        if (requestDto.getHeaders() == null) {
            requestDto.setHeaders(new ArrayList<>());
        }

        // Pass through Authorization header
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && !authHeader.isEmpty()) {
            boolean hasAuth = requestDto.getHeaders().stream()
                    .anyMatch(h -> "Authorization".equalsIgnoreCase(h.getKey()));

            if (!hasAuth) {
                HeaderDTO header = new HeaderDTO();
                header.setKey("Authorization");
                header.setValue(authHeader);
                header.setEnabled(true);
                requestDto.getHeaders().add(header);
                log.info("Added passthrough Authorization header");
            }
        }

        // Pass through Cookie header
        String cookieHeader = req.getHeader("Cookie");
        if (cookieHeader != null && !cookieHeader.isEmpty()) {
            boolean hasCookie = requestDto.getHeaders().stream()
                    .anyMatch(h -> "Cookie".equalsIgnoreCase(h.getKey()));

            if (!hasCookie) {
                HeaderDTO header = new HeaderDTO();
                header.setKey("Cookie");
                header.setValue(cookieHeader);
                header.setEnabled(true);
                requestDto.getHeaders().add(header);
                log.info("Added passthrough Cookie header");
            }
        }
    }



    private void applyApiAuthentication(ExecuteRequestDTO requestDto, GeneratedApiEntity api) {
        if (api.getAuthConfig() == null || "NONE".equals(api.getAuthConfig().getAuthType())) {
            return; // No authentication needed
        }

        if (requestDto.getHeaders() == null) {
            requestDto.setHeaders(new ArrayList<>());
        }

        switch (api.getAuthConfig().getAuthType()) {
            case "API_KEY":
                String headerName = api.getAuthConfig().getApiKeyHeader() != null ?
                        api.getAuthConfig().getApiKeyHeader() : "X-API-Key";
                String headerValue = api.getAuthConfig().getApiKeyValue() != null ?
                        api.getAuthConfig().getApiKeyValue() : "";

                HeaderDTO apiKeyHeader = new HeaderDTO();
                apiKeyHeader.setKey(headerName);
                apiKeyHeader.setValue(headerValue);
                apiKeyHeader.setEnabled(true);
                requestDto.getHeaders().add(apiKeyHeader);
                break;

            case "BEARER":
            case "JWT":
                String token = api.getAuthConfig().getJwtSecret() != null ?
                        api.getAuthConfig().getJwtSecret() : "";

                HeaderDTO bearerHeader = new HeaderDTO();
                bearerHeader.setKey("Authorization");
                bearerHeader.setValue("Bearer " + token);
                bearerHeader.setEnabled(true);
                requestDto.getHeaders().add(bearerHeader);
                break;

            case "BASIC":
                String username = api.getAuthConfig().getBasicUsername() != null ?
                        api.getAuthConfig().getBasicUsername() : "";
                String password = api.getAuthConfig().getBasicPassword() != null ?
                        api.getAuthConfig().getBasicPassword() : "";
                String basicAuth = "Basic " + Base64.getEncoder().encodeToString(
                        (username + ":" + password).getBytes(StandardCharsets.UTF_8));

                HeaderDTO basicHeader = new HeaderDTO();
                basicHeader.setKey("Authorization");
                basicHeader.setValue(basicAuth);
                basicHeader.setEnabled(true);
                requestDto.getHeaders().add(basicHeader);
                break;
        }
    }


    private ExecuteRequestResponseDTO executeActualRequest(ExecuteRequestDTO requestDto) {
        try {
            // Create HTTP headers
            HttpHeaders headers = new HttpHeaders();
            if (requestDto.getHeaders() != null) {
                for (HeaderDTO header : requestDto.getHeaders()) {
                    if (header.isEnabled()) {
                        headers.add(header.getKey(), header.getValue());
                    }
                }
            }

            // Set default content type if not provided
            if (!headers.containsKey("Content-Type")) {
                headers.setContentType(MediaType.APPLICATION_JSON);
            }

            // Create HTTP entity with body
            HttpEntity<String> requestEntity;
            if (requestDto.getBody() != null && !requestDto.getBody().trim().isEmpty()) {
                requestEntity = new HttpEntity<>(requestDto.getBody(), headers);
            } else {
                requestEntity = new HttpEntity<>(headers);
            }

            // Start timing
            long startTime = System.currentTimeMillis();

            // Execute request
            ResponseEntity<String> response = restTemplate.exchange(
                    requestDto.getUrl(),
                    HttpMethod.valueOf(requestDto.getMethod()),
                    requestEntity,
                    String.class
            );

            // Calculate time taken
            long timeMs = System.currentTimeMillis() - startTime;

            // Extract response headers
            List<HeaderDTO> responseHeaders = new ArrayList<>();
            for (Map.Entry<String, List<String>> entry : response.getHeaders().entrySet()) {
                String headerName = entry.getKey();
                String headerValue = String.join(", ", entry.getValue());
                responseHeaders.add(createHeader(
                        UUID.randomUUID().toString(),
                        headerName,
                        headerValue,
                        true,
                        ""
                ));
            }

            // Calculate response size
            String responseBody = response.getBody() != null ? response.getBody() : "";
            long sizeBytes = responseBody.getBytes(StandardCharsets.UTF_8).length;

            // Get status text from HttpStatus enum
            HttpStatus status = HttpStatus.valueOf(response.getStatusCode().value());

            return new ExecuteRequestResponseDTO(
                    responseBody,
                    response.getStatusCode().value(),
                    status.getReasonPhrase(),
                    responseHeaders,
                    timeMs,
                    sizeBytes
            );

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Handle HTTP errors (4xx, 5xx)
            log.error("HTTP error executing request: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());

            List<HeaderDTO> responseHeaders = new ArrayList<>();
            if (e.getResponseHeaders() != null) {
                for (Map.Entry<String, List<String>> entry : e.getResponseHeaders().entrySet()) {
                    String headerName = entry.getKey();
                    if (headerName != null) {
                        String headerValue = String.join(", ", entry.getValue());
                        responseHeaders.add(createHeader(
                                UUID.randomUUID().toString(),
                                headerName,
                                headerValue,
                                true,
                                ""
                        ));
                    }
                }
            }

            // Get status text from HttpStatus enum
            HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());

            return new ExecuteRequestResponseDTO(
                    e.getResponseBodyAsString(),
                    e.getStatusCode().value(),
                    status.getReasonPhrase(),
                    responseHeaders,
                    0,
                    e.getResponseBodyAsString() != null ? e.getResponseBodyAsString().getBytes(StandardCharsets.UTF_8).length : 0
            );

        } catch (ResourceAccessException e) {
            log.error("Connection error: {}", e.getMessage());
            throw new RuntimeException("Connection error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error executing request: {}", e.getMessage());
            throw new RuntimeException("Failed to execute request: " + e.getMessage(), e);
        }
    }


    public SaveRequestResponseDTO saveRequest(String requestId, HttpServletRequest req, String performedBy,
                                              SaveRequestDTO requestDto) {
        log.info("Request ID: {}, Saving request for user: {}", requestId, performedBy);

        RequestDTO requestData = requestDto.getRequest();
        CollectionEntity collection = collectionRepository.findById(requestDto.getCollectionId())
                .orElseThrow(() -> new EntityNotFoundException("Collection not found: " + requestDto.getCollectionId()));

        // Verify ownership
//        if (!collection.getOwner().equals(performedBy)) {
//            throw new SecurityException("Access denied to collection: " + requestDto.getCollectionId());
//        }

        RequestEntity requestEntity;
        if (requestDto.getRequestId() != null && !requestDto.getRequestId().isEmpty()) {
            // Update existing request
            requestEntity = requestRepository.findByIdWithDetails(requestDto.getRequestId())
                    .orElseThrow(() -> new EntityNotFoundException("Request not found: " + requestDto.getRequestId()));

            // Update fields
            updateRequestEntity(requestEntity, requestData);

            // Clear and update relationships
            updateRequestRelationships(requestEntity, requestData);

        } else {
            // Create new request
            requestEntity = createNewRequest(requestData, collection, requestDto.getFolderId());
        }

        RequestEntity saved = requestRepository.save(requestEntity);

        // Update folder request count if in folder
        if (saved.getFolder() != null) {
            updateFolderRequestCount(saved.getFolder().getId());
        }

        // Update collection last activity
        collection.setLastActivity(LocalDateTime.now());
        collectionRepository.save(collection);

        log.info("Request ID: {}, Request saved successfully: {}", requestId, saved.getId());

        return new SaveRequestResponseDTO(saved.getId(), "Request saved successfully");
    }

    public CreateCollectionResponseDTO createCollection(String requestId, HttpServletRequest req, String performedBy,
                                                        CreateCollectionDTO collectionDto) {
        log.info("Request ID: {}, Creating collection for user: {}", requestId, performedBy);

        CollectionEntity collection = new CollectionEntity();
        collection.setName(collectionDto.getName());
        collection.setDescription(collectionDto.getDescription());
        collection.setOwner(performedBy);
        collection.setExpanded(false);
        collection.setEditing(false);
        collection.setFavorite(false);
        collection.setLastActivity(LocalDateTime.now());

        // Add variables if provided
        if (collectionDto.getVariables() != null) {
            for (VariableDTO varDTO : collectionDto.getVariables()) {
                VariableEntity variable = new VariableEntity();
                variable.setKey(varDTO.getKey());
                variable.setValue(varDTO.getValue());
                variable.setType(varDTO.getType());
                variable.setEnabled(varDTO.isEnabled());
                variable.setCollection(collection);
                collection.getVariables().add(variable);
            }
        }

        CollectionEntity saved = collectionRepository.save(collection);

        log.info("Request ID: {}, Collection created successfully: {}", requestId, saved.getId());

        return new CreateCollectionResponseDTO(saved.getId(), "Collection created successfully");
    }

    public CodeSnippetResponseDTO generateCodeSnippet(String requestId, HttpServletRequest req, String performedBy,
                                                      CodeSnippetRequestDTO snippetRequest) {
        log.info("Request ID: {}, Generating code snippet for language: {}",
                requestId, snippetRequest.getLanguage());

        CodeSnippetResponseDTO snippet = generateSampleCodeSnippet(snippetRequest);

        log.info("Request ID: {}, Generated code snippet for {}", requestId, snippetRequest.getLanguage());

        return snippet;
    }

    public EnvironmentsResponseDTO getEnvironments(String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Getting environments for user: {}", requestId, performedBy);

        List<EnvironmentEntity> environments = environmentRepository.findAll();

        List<EnvironmentDTO> environmentDTOs = environments.stream()
                .map(this::mapToEnvironmentDTO)
                .collect(Collectors.toList());

        log.info("Request ID: {}, Retrieved {} environments from database", requestId, environmentDTOs.size());

        return new EnvironmentsResponseDTO(environmentDTOs);
    }

    public ImportResponseDTO importCollection(String requestId, HttpServletRequest req, String performedBy,
                                              ImportRequestDTO importRequest) {
        log.info("Request ID: {}, Importing collection for user: {}", requestId, performedBy);

        // Create a new collection from import
        CollectionEntity collection = new CollectionEntity();
        collection.setName("Imported Collection");
        collection.setDescription("Imported from " + importRequest.getSource());
        collection.setOwner(performedBy);
        collection.setLastActivity(LocalDateTime.now());

        CollectionEntity saved = collectionRepository.save(collection);

        log.info("Request ID: {}, Collection imported successfully: {}", requestId, saved.getId());

        return new ImportResponseDTO(saved.getId(), "Collection imported successfully from " + importRequest.getSource());
    }

    // ========== MAPPING METHODS ==========

    private CollectionDTO mapToCollectionDTO(CollectionEntity entity) {
        CollectionDTO dto = new CollectionDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setExpanded(entity.isExpanded());
        dto.setFavorite(entity.isFavorite());
        dto.setEditing(entity.isEditing());
        dto.setOwner(entity.getOwner());
        dto.setColor(entity.getColor());
        dto.setTags(entity.getTags());

        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (entity.getUpdatedAt() != null) {
            dto.setUpdatedAt(entity.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        // Calculate requests count - need to fetch folders and requests
        List<FolderEntity> folders = folderRepository.findByCollectionId(entity.getId());
        int requestsCount = 0;
        for (FolderEntity folder : folders) {
            requestsCount += requestRepository.countByFolderId(folder.getId());
        }
        dto.setRequestsCount(requestsCount);

        dto.setFolderCount(folders.size());

        if (entity.getVariables() != null) {
            List<VariableDTO> variableDTOs = entity.getVariables().stream()
                    .map(this::mapToVariableDTO)
                    .collect(Collectors.toList());
            dto.setVariables(variableDTOs);
        }

        return dto;
    }

    private CollectionDetailsResponseDTO mapToCollectionDetailsDTO(CollectionEntity entity) {
        CollectionDetailsResponseDTO dto = new CollectionDetailsResponseDTO();
        dto.setCollectionId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());

        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (entity.getUpdatedAt() != null) {
            dto.setUpdatedAt(entity.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        // Calculate total requests
        int totalRequests = 0;
        if (entity.getFolders() != null) {
            for (FolderEntity folder : entity.getFolders()) {
                totalRequests += folder.getRequests() != null ? folder.getRequests().size() : 0;
            }
        }
        dto.setTotalRequests(totalRequests);

        dto.setTotalFolders(entity.getFolders() != null ? entity.getFolders().size() : 0);
        dto.setFavorite(entity.isFavorite());
        dto.setOwner(entity.getOwner());
        dto.setComments(entity.getComments());

        if (entity.getLastActivity() != null) {
            dto.setLastActivity(entity.getLastActivity().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        if (entity.getVariables() != null) {
            List<VariableDTO> variableDTOs = entity.getVariables().stream()
                    .map(this::mapToVariableDTO)
                    .collect(Collectors.toList());
            dto.setVariables(variableDTOs);
        }

        if (entity.getFolders() != null) {
            List<FolderDTO> folderDTOs = entity.getFolders().stream()
                    .map(this::mapToFolderDTO)
                    .collect(Collectors.toList());
            dto.setFolders(folderDTOs);
        }

        return dto;
    }

    private FolderDTO mapToFolderDTO(FolderEntity entity) {
        FolderDTO dto = new FolderDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setExpanded(entity.isExpanded());
        dto.setEditing(entity.isEditing());
        dto.setRequestCount(entity.getRequestCount());

        if (entity.getRequests() != null) {
            List<RequestDTO> requestDTOs = entity.getRequests().stream()
                    .map(this::mapToRequestDTO)
                    .collect(Collectors.toList());
            dto.setRequests(requestDTOs);
        }

        return dto;
    }

    private RequestDTO mapToRequestDTO(RequestEntity entity) {
        RequestDTO dto = new RequestDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setMethod(entity.getMethod());
        dto.setUrl(entity.getUrl());
        dto.setDescription(entity.getDescription());
        dto.setEditing(entity.isEditing());
        dto.setStatus(entity.getStatus());

        // Set both timestamps
        if (entity.getLastModified() != null) {
            dto.setLastModified(entity.getLastModified().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        dto.setBody(entity.getBody());
        dto.setTests(entity.getTests());
        dto.setPreRequestScript(entity.getPreRequestScript());
        dto.setSaved(entity.isSaved());

        if (entity.getAuthConfig() != null) {
            dto.setAuth(mapToAuthConfigDTO(entity.getAuthConfig()));
        }

        if (entity.getHeaders() != null) {
            List<HeaderDTO> headerDTOs = entity.getHeaders().stream()
                    .map(this::mapToHeaderDTO)
                    .collect(Collectors.toList());
            dto.setHeaders(headerDTOs);
        }

        if (entity.getParams() != null) {
            List<ParameterDTO> paramDTOs = entity.getParams().stream()
                    .map(this::mapToParameterDTO)
                    .collect(Collectors.toList());
            dto.setParams(paramDTOs);
        }

        if (entity.getCollection() != null) {
            dto.setCollectionId(entity.getCollection().getId());
        }

        if (entity.getFolder() != null) {
            dto.setFolderId(entity.getFolder().getId());
        }

        return dto;
    }

    private RequestDetailsResponseDTO mapToRequestDetailsDTO(RequestEntity entity) {
        RequestDetailsResponseDTO dto = new RequestDetailsResponseDTO();
        dto.setRequestId(entity.getId());
        dto.setName(entity.getName());
        dto.setMethod(entity.getMethod());
        dto.setUrl(entity.getUrl());
        dto.setDescription(entity.getDescription());
        dto.setAuthType(entity.getAuthType());

        if (entity.getHeaders() != null) {
            List<HeaderDTO> headerDTOs = entity.getHeaders().stream()
                    .map(this::mapToHeaderDTO)
                    .collect(Collectors.toList());
            dto.setHeaders(headerDTOs);
        }

        if (entity.getParams() != null) {
            List<ParameterDTO> paramDTOs = entity.getParams().stream()
                    .map(this::mapToParameterDTO)
                    .collect(Collectors.toList());
            dto.setParameters(paramDTOs);
        }

        // Create BodyDTO
        BodyDTO body = new BodyDTO();
        if (entity.getBody() != null && !entity.getBody().isEmpty()) {
            body.setType("raw");
            body.setRawType("json");
            body.setContent(entity.getBody());
        } else {
            body.setType("none");
        }
        dto.setBody(body);

        if (entity.getAuthConfig() != null) {
            dto.setAuthConfig(mapToAuthConfigDTO(entity.getAuthConfig()));
        }

        dto.setPreRequestScript(entity.getPreRequestScript());
        dto.setTests(entity.getTests());

        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (entity.getUpdatedAt() != null) {
            dto.setUpdatedAt(entity.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        if (entity.getCollection() != null) {
            dto.setCollectionId(entity.getCollection().getId());
        }

        if (entity.getFolder() != null) {
            dto.setFolderId(entity.getFolder().getId());
        }

        dto.setSaved(entity.isSaved());

        return dto;
    }

    private AuthConfigDTO mapToAuthConfigDTO(AuthConfigEntity entity) {
        AuthConfigDTO dto = new AuthConfigDTO();
        dto.setType(entity.getType());
        dto.setToken(entity.getToken());
        dto.setTokenType(entity.getTokenType());
        dto.setUsername(entity.getUsername());
        dto.setPassword(entity.getPassword());
        dto.setKey(entity.getKey());
        dto.setValue(entity.getValue());
        dto.setAddTo(entity.getAddTo());
        return dto;
    }

    private VariableDTO mapToVariableDTO(VariableEntity entity) {
        VariableDTO dto = new VariableDTO();
        dto.setId(entity.getId());
        dto.setKey(entity.getKey());
        dto.setValue(entity.getValue());
        dto.setType(entity.getType());
        dto.setEnabled(entity.isEnabled());
        return dto;
    }

    private VariableDTO mapToVariableDTO(EnvironmentVariableEntity entity) {
        VariableDTO dto = new VariableDTO();
        dto.setId(entity.getId());
        dto.setKey(entity.getKey());
        dto.setValue(entity.getValue());
        dto.setType(entity.getType());
        dto.setEnabled(entity.isEnabled());
        return dto;
    }

    private HeaderDTO mapToHeaderDTO(HeaderEntity entity) {
        HeaderDTO dto = new HeaderDTO();
        dto.setId(entity.getId());
        dto.setKey(entity.getKey());
        dto.setValue(entity.getValue());
        dto.setDescription(entity.getDescription());
        dto.setEnabled(entity.isEnabled());
        return dto;
    }

    private ParameterDTO mapToParameterDTO(ParameterEntity entity) {
        ParameterDTO dto = new ParameterDTO();
        dto.setId(entity.getId());
        dto.setKey(entity.getKey());
        dto.setValue(entity.getValue());
        dto.setDescription(entity.getDescription());
        dto.setEnabled(entity.isEnabled());
        return dto;
    }

    private EnvironmentDTO mapToEnvironmentDTO(EnvironmentEntity entity) {
        EnvironmentDTO dto = new EnvironmentDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setActive(entity.isActive());

        if (entity.getVariables() != null) {
            List<VariableDTO> variableDTOs = entity.getVariables().stream()
                    .map(this::mapToVariableDTO)
                    .collect(Collectors.toList());
            dto.setVariables(variableDTOs);
        }

        return dto;
    }

    // ========== HELPER METHODS ==========

    private RequestEntity createNewRequest(RequestDTO requestData, CollectionEntity collection, String folderId) {
        RequestEntity requestEntity = new RequestEntity();
        requestEntity.setName(requestData.getName());
        requestEntity.setMethod(requestData.getMethod());
        requestEntity.setUrl(requestData.getUrl());
        requestEntity.setDescription(requestData.getDescription());
        requestEntity.setBody(requestData.getBody());
        requestEntity.setTests(requestData.getTests());
        requestEntity.setPreRequestScript(requestData.getPreRequestScript());
        requestEntity.setSaved(true);
        requestEntity.setLastModified(LocalDateTime.now());
        requestEntity.setCollection(collection);
        requestEntity.setAuthType(requestData.getAuth() != null ? requestData.getAuth().getType() : null);

        // Set folder if provided
        if (folderId != null && !folderId.isEmpty()) {
            FolderEntity folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new EntityNotFoundException("Folder not found: " + folderId));
            requestEntity.setFolder(folder);
        }

        return requestEntity;
    }

    private void updateRequestEntity(RequestEntity entity, RequestDTO data) {
        entity.setName(data.getName());
        entity.setMethod(data.getMethod());
        entity.setUrl(data.getUrl());
        entity.setDescription(data.getDescription());
        entity.setBody(data.getBody());
        entity.setTests(data.getTests());
        entity.setPreRequestScript(data.getPreRequestScript());
        entity.setSaved(true);
        entity.setLastModified(LocalDateTime.now());
        entity.setAuthType(data.getAuth() != null ? data.getAuth().getType() : null);
    }

    private void updateRequestRelationships(RequestEntity entity, RequestDTO data) {
        // Update auth config
        if (data.getAuth() != null) {
            if (entity.getAuthConfig() == null) {
                AuthConfigEntity authConfig = new AuthConfigEntity();
                authConfig.setRequest(entity);
                entity.setAuthConfig(authConfig);
            }
            AuthConfigEntity authConfig = entity.getAuthConfig();
            authConfig.setType(data.getAuth().getType());
            authConfig.setToken(data.getAuth().getToken());
            authConfig.setTokenType(data.getAuth().getTokenType());
            authConfig.setUsername(data.getAuth().getUsername());
            authConfig.setPassword(data.getAuth().getPassword());
            authConfig.setKey(data.getAuth().getKey());
            authConfig.setValue(data.getAuth().getValue());
            authConfig.setAddTo(data.getAuth().getAddTo());
        } else if (entity.getAuthConfig() != null) {
            entity.setAuthConfig(null);
        }

        // Update headers
        entity.getHeaders().clear();
        if (data.getHeaders() != null) {
            for (HeaderDTO headerDTO : data.getHeaders()) {
                HeaderEntity header = new HeaderEntity();
                header.setKey(headerDTO.getKey());
                header.setValue(headerDTO.getValue());
                header.setDescription(headerDTO.getDescription());
                header.setEnabled(headerDTO.isEnabled());
                header.setRequest(entity);
                entity.getHeaders().add(header);
            }
        }

        // Update params
        entity.getParams().clear();
        if (data.getParams() != null) {
            for (ParameterDTO paramDTO : data.getParams()) {
                ParameterEntity param = new ParameterEntity();
                param.setKey(paramDTO.getKey());
                param.setValue(paramDTO.getValue());
                param.setDescription(paramDTO.getDescription());
                param.setEnabled(paramDTO.isEnabled());
                param.setRequest(entity);
                entity.getParams().add(param);
            }
        }
    }

    private void updateFolderRequestCount(String folderId) {
        if (folderId != null) {
            int count = requestRepository.countByFolderId(folderId);
            folderRepository.findById(folderId).ifPresent(folder -> {
                folder.setRequestCount(count);
                folderRepository.save(folder);
            });
        }
    }

    private VariableDTO createVariable(String id, String key, String value, String type, boolean enabled) {
        VariableDTO variable = new VariableDTO();
        variable.setId(id);
        variable.setKey(key);
        variable.setValue(value);
        variable.setType(type);
        variable.setEnabled(enabled);
        return variable;
    }

    private ParameterDTO createParameter(String id, String key, String value, String description, boolean enabled) {
        ParameterDTO parameter = new ParameterDTO();
        parameter.setId(id);
        parameter.setKey(key);
        parameter.setValue(value);
        parameter.setDescription(description);
        parameter.setEnabled(enabled);
        return parameter;
    }

    private HeaderDTO createHeader(String id, String key, String value, boolean enabled, String description) {
        HeaderDTO header = new HeaderDTO();
        header.setId(id);
        header.setKey(key);
        header.setValue(value);
        header.setEnabled(enabled);
        header.setDescription(description);
        return header;
    }

    // ========== EXECUTION AND CODE SNIPPET METHODS ==========

    private ExecuteRequestResponseDTO executeSampleRequest(ExecuteRequestDTO requestDto) {
        try {
            // Generate sample response based on request
            String method = requestDto.getMethod();
            String statusCode = method.equals("GET") ? "200" : method.equals("POST") ? "201" : "200";
            String statusText = statusCode.equals("200") ? "OK" : statusCode.equals("201") ? "Created" : "OK";

            String responseBody = "";
            if (requestDto.getUrl().contains("login")) {
                responseBody = "{\n" +
                        "  \"success\": true,\n" +
                        "  \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\",\n" +
                        "  \"refresh_token\": \"refresh_token_123\",\n" +
                        "  \"user\": {\n" +
                        "    \"id\": 1,\n" +
                        "    \"email\": \"user@example.com\",\n" +
                        "    \"name\": \"John Doe\"\n" +
                        "  }\n" +
                        "}";
            } else if (requestDto.getUrl().contains("products")) {
                responseBody = "{\n" +
                        "  \"success\": true,\n" +
                        "  \"data\": [\n" +
                        "    {\n" +
                        "      \"id\": 1,\n" +
                        "      \"name\": \"Product 1\",\n" +
                        "      \"price\": 99.99,\n" +
                        "      \"category\": \"electronics\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"id\": 2,\n" +
                        "      \"name\": \"Product 2\",\n" +
                        "      \"price\": 49.99,\n" +
                        "      \"category\": \"clothing\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"pagination\": {\n" +
                        "    \"page\": 1,\n" +
                        "    \"limit\": 20,\n" +
                        "    \"total\": 100\n" +
                        "  }\n" +
                        "}";
            } else {
                responseBody = "{\n" +
                        "  \"success\": true,\n" +
                        "  \"message\": \"Request processed successfully\",\n" +
                        "  \"timestamp\": \"" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\",\n" +
                        "  \"endpoint\": \"" + requestDto.getUrl() + "\",\n" +
                        "  \"method\": \"" + method + "\"\n" +
                        "}";
            }

            long timeMs = (long) (Math.random() * 200) + 100;
            long sizeBytes = responseBody.getBytes().length;

            // Generate headers
            List<HeaderDTO> headers = new ArrayList<>();
            headers.add(createHeader("res-header-1", "Content-Type", "application/json", true, ""));
            headers.add(createHeader("res-header-2", "X-RateLimit-Limit", "1000", true, ""));
            headers.add(createHeader("res-header-3", "X-RateLimit-Remaining", "999", true, ""));
            headers.add(createHeader("res-header-4", "X-Powered-By", "Express", true, ""));
            headers.add(createHeader("res-header-5", "Date", LocalDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME), true, ""));

            return new ExecuteRequestResponseDTO(
                    responseBody,
                    Integer.parseInt(statusCode),
                    statusText,
                    headers,
                    timeMs,
                    sizeBytes
            );

        } catch (Exception e) {
            log.error("Error executing sample request: {}", e.getMessage());
            throw new RuntimeException("Failed to execute request: " + e.getMessage(), e);
        }
    }

    private CodeSnippetResponseDTO generateSampleCodeSnippet(CodeSnippetRequestDTO snippetRequest) {
        String code = "";
        String language = snippetRequest.getLanguage();

        switch (language.toLowerCase()) {
            case "curl":
                code = generateCurlSnippet(snippetRequest);
                break;
            case "javascript":
                code = generateJavaScriptSnippet(snippetRequest);
                break;
            case "python":
                code = generatePythonSnippet(snippetRequest);
                break;
            case "java":
                code = generateJavaSnippet(snippetRequest);
                break;
            case "nodejs":
                code = generateNodeJsSnippet(snippetRequest);
                break;
            case "php":
                code = generatePhpSnippet(snippetRequest);
                break;
            case "ruby":
                code = generateRubySnippet(snippetRequest);
                break;
            default:
                code = generateCurlSnippet(snippetRequest);
                break;
        }

        return new CodeSnippetResponseDTO(code, language, "Code snippet generated successfully");
    }

    private String generateCurlSnippet(CodeSnippetRequestDTO request) {
        StringBuilder curl = new StringBuilder();
        curl.append("curl -X ").append(request.getMethod()).append(" \\\n");
        curl.append("  \"").append(request.getUrl()).append("\"");

        if (request.getHeaders() != null) {
            for (HeaderDTO header : request.getHeaders()) {
                if (header.isEnabled()) {
                    curl.append(" \\\n");
                    curl.append("  -H \"").append(header.getKey()).append(": ").append(header.getValue()).append("\"");
                }
            }
        }

        if (request.getBody() != null && !request.getBody().trim().isEmpty() &&
                !request.getMethod().equals("GET")) {
            curl.append(" \\\n");
            curl.append("  -d '").append(request.getBody()).append("'");
        }

        return curl.toString();
    }

    private String generateJavaScriptSnippet(CodeSnippetRequestDTO request) {
        StringBuilder js = new StringBuilder();
        js.append("fetch(\"").append(request.getUrl()).append("\", {\n");
        js.append("  method: \"").append(request.getMethod()).append("\",\n");

        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            js.append("  headers: {\n");
            for (int i = 0; i < request.getHeaders().size(); i++) {
                HeaderDTO header = request.getHeaders().get(i);
                if (header.isEnabled()) {
                    js.append("    \"").append(header.getKey()).append("\": \"").append(header.getValue()).append("\"");
                    if (i < request.getHeaders().size() - 1) js.append(",\n");
                }
            }
            js.append("\n  },\n");
        }

        if (request.getBody() != null && !request.getBody().trim().isEmpty() &&
                !request.getMethod().equals("GET")) {
            js.append("  body: JSON.stringify(").append(request.getBody()).append(")\n");
        }

        js.append("})\n");
        js.append(".then(response => response.json())\n");
        js.append(".then(data => console.log(data))\n");
        js.append(".catch(error => console.error('Error:', error));");

        return js.toString();
    }

    private String generatePythonSnippet(CodeSnippetRequestDTO request) {
        StringBuilder python = new StringBuilder();
        python.append("import requests\n\n");

        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            python.append("headers = {\n");
            for (HeaderDTO header : request.getHeaders()) {
                if (header.isEnabled()) {
                    python.append("    \"").append(header.getKey()).append("\": \"").append(header.getValue()).append("\",\n");
                }
            }
            python.append("}\n\n");
        } else {
            python.append("headers = {}\n\n");
        }

        if (request.getBody() != null && !request.getBody().trim().isEmpty() &&
                !request.getMethod().equals("GET")) {
            python.append("data = ").append(request.getBody()).append("\n\n");
            python.append("response = requests.").append(request.getMethod().toLowerCase());
            python.append("(\"").append(request.getUrl()).append("\", json=data, headers=headers)\n");
        } else {
            python.append("response = requests.").append(request.getMethod().toLowerCase());
            python.append("(\"").append(request.getUrl()).append("\", headers=headers)\n");
        }

        python.append("print(response.json())");

        return python.toString();
    }

    private String generateJavaSnippet(CodeSnippetRequestDTO request) {
        StringBuilder java = new StringBuilder();
        java.append("import java.net.HttpURLConnection;\n");
        java.append("import java.net.URL;\n");
        java.append("import java.io.BufferedReader;\n");
        java.append("import java.io.InputStreamReader;\n");
        java.append("import java.io.OutputStream;\n\n");

        java.append("public class ApiRequest {\n");
        java.append("    public static void main(String[] args) throws Exception {\n");
        java.append("        URL url = new URL(\"").append(request.getUrl()).append("\");\n");
        java.append("        HttpURLConnection conn = (HttpURLConnection) url.openConnection();\n");
        java.append("        conn.setRequestMethod(\"").append(request.getMethod()).append("\");\n");

        if (request.getHeaders() != null) {
            for (HeaderDTO header : request.getHeaders()) {
                if (header.isEnabled()) {
                    java.append("        conn.setRequestProperty(\"").append(header.getKey()).append("\", \"");
                    java.append(header.getValue()).append("\");\n");
                }
            }
        }

        if (request.getBody() != null && !request.getBody().trim().isEmpty() &&
                !request.getMethod().equals("GET")) {
            java.append("        conn.setDoOutput(true);\n");
            java.append("        try(OutputStream os = conn.getOutputStream()) {\n");
            java.append("            byte[] input = ").append(request.getBody()).append(".getBytes(\"utf-8\");\n");
            java.append("            os.write(input, 0, input.length);\n");
            java.append("        }\n");
        }

        java.append("        \n");
        java.append("        try(BufferedReader br = new BufferedReader(\n");
        java.append("            new InputStreamReader(conn.getInputStream(), \"utf-8\"))) {\n");
        java.append("            StringBuilder response = new StringBuilder();\n");
        java.append("            String responseLine;\n");
        java.append("            while ((responseLine = br.readLine()) != null) {\n");
        java.append("                response.append(responseLine.trim());\n");
        java.append("            }\n");
        java.append("            System.out.println(response.toString());\n");
        java.append("        }\n");
        java.append("    }\n");
        java.append("}");

        return java.toString();
    }

    private String generateNodeJsSnippet(CodeSnippetRequestDTO request) {
        StringBuilder node = new StringBuilder();
        node.append("const https = require('https');\n\n");

        node.append("const options = {\n");
        node.append("  hostname: '").append(extractHostname(request.getUrl())).append("',\n");
        node.append("  port: 443,\n");
        node.append("  path: '").append(extractPath(request.getUrl())).append("',\n");
        node.append("  method: '").append(request.getMethod()).append("',\n");

        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            node.append("  headers: {\n");
            for (int i = 0; i < request.getHeaders().size(); i++) {
                HeaderDTO header = request.getHeaders().get(i);
                if (header.isEnabled()) {
                    node.append("    '").append(header.getKey()).append("': '").append(header.getValue()).append("'");
                    if (i < request.getHeaders().size() - 1) node.append(",\n");
                }
            }
            node.append("\n  }\n");
        }

        node.append("};\n\n");

        node.append("const req = https.request(options, (res) => {\n");
        node.append("  let data = '';\n");
        node.append("  res.on('data', (chunk) => {\n");
        node.append("    data += chunk;\n");
        node.append("  });\n");
        node.append("  res.on('end', () => {\n");
        node.append("    console.log(JSON.parse(data));\n");
        node.append("  });\n");
        node.append("});\n\n");

        node.append("req.on('error', (error) => {\n");
        node.append("  console.error(error);\n");
        node.append("});\n\n");

        if (request.getBody() != null && !request.getBody().trim().isEmpty() &&
                !request.getMethod().equals("GET")) {
            node.append("req.write(JSON.stringify(").append(request.getBody()).append("));\n");
        }

        node.append("req.end();");

        return node.toString();
    }

    private String generatePhpSnippet(CodeSnippetRequestDTO request) {
        StringBuilder php = new StringBuilder();
        php.append("<?php\n\n");

        php.append("$ch = curl_init();\n\n");
        php.append("curl_setopt($ch, CURLOPT_URL, \"").append(request.getUrl()).append("\");\n");
        php.append("curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);\n");
        php.append("curl_setopt($ch, CURLOPT_CUSTOMREQUEST, \"").append(request.getMethod()).append("\");\n\n");

        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            php.append("$headers = [\n");
            for (HeaderDTO header : request.getHeaders()) {
                if (header.isEnabled()) {
                    php.append("    \"").append(header.getKey()).append(": ").append(header.getValue()).append("\",\n");
                }
            }
            php.append("];\n");
            php.append("curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);\n\n");
        }

        if (request.getBody() != null && !request.getBody().trim().isEmpty() &&
                !request.getMethod().equals("GET")) {
            php.append("curl_setopt($ch, CURLOPT_POSTFIELDS, ").append(request.getBody()).append(");\n\n");
        }

        php.append("$response = curl_exec($ch);\n");
        php.append("curl_close($ch);\n\n");
        php.append("echo $response;\n");
        php.append("?>");

        return php.toString();
    }

    private String generateRubySnippet(CodeSnippetRequestDTO request) {
        StringBuilder ruby = new StringBuilder();
        ruby.append("require 'net/http'\n");
        ruby.append("require 'uri'\n");
        ruby.append("require 'json'\n\n");

        ruby.append("uri = URI.parse(\"").append(request.getUrl()).append("\")\n\n");

        ruby.append("http = Net::HTTP.new(uri.host, uri.port)\n");
        ruby.append("http.use_ssl = true if uri.scheme == 'https'\n\n");

        ruby.append("request = Net::HTTP::").append(request.getMethod().charAt(0) + request.getMethod().substring(1).toLowerCase());
        ruby.append(".new(uri.request_uri)\n\n");

        if (request.getHeaders() != null) {
            for (HeaderDTO header : request.getHeaders()) {
                if (header.isEnabled()) {
                    ruby.append("request[\"").append(header.getKey()).append("\"] = \"").append(header.getValue()).append("\"\n");
                }
            }
        }

        if (request.getBody() != null && !request.getBody().trim().isEmpty() &&
                !request.getMethod().equals("GET")) {
            ruby.append("request.body = ").append(request.getBody()).append(".to_json\n\n");
        }

        ruby.append("response = http.request(request)\n");
        ruby.append("puts response.body");

        return ruby.toString();
    }

    private String extractHostname(String url) {
        try {
            String[] parts = url.replace("http://", "").replace("https://", "").split("/");
            return parts[0];
        } catch (Exception e) {
            return "api.example.com";
        }
    }

    private String extractPath(String url) {
        try {
            String[] parts = url.replace("http://", "").replace("https://", "").split("/");
            if (parts.length > 1) {
                return "/" + String.join("/", Arrays.copyOfRange(parts, 1, parts.length));
            }
            return "/";
        } catch (Exception e) {
            return "/api/v1/endpoint";
        }
    }
}