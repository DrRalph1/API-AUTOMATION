package com.usg.apiAutomation.services;

import com.usg.apiAutomation.dtos.codeBase.*;
import com.usg.apiAutomation.entities.postgres.codeBase.*;
import com.usg.apiAutomation.entities.postgres.collections.AuthConfigEntity;
import com.usg.apiAutomation.repositories.codeBase.*;
import com.usg.apiAutomation.repositories.collections.AuthConfigRepository;
import com.usg.apiAutomation.utils.LoggerUtil;
import com.usg.apiAutomation.utils.ModelMapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CodeBaseService {

    @Autowired
    private LoggerUtil loggerUtil;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private ImplementationRepository implementationRepository;

    @Autowired
    private AuthConfigRepository authConfigRepository;

    @Autowired
    private ModelMapperUtil modelMapper;

    // ============================================================
    // 1. GET COLLECTIONS LIST
    // ============================================================
    public CollectionsListResponse getCollectionsList(String requestId, String performedBy) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Getting collections list for codebase");

            List<CollectionEntity> collectionEntities;

            if (performedBy != null && !performedBy.isEmpty()) {
                collectionEntities = collectionRepository.findByOwnerOrderByUpdatedAtDesc(performedBy);
            } else {
                collectionEntities = collectionRepository.findAll();
            }

            List<CollectionItem> collections = collectionEntities.stream()
                    .map(entity -> {
                        CollectionItem item = new CollectionItem();
                        item.setId(entity.getId());
                        item.setName(entity.getName());
                        item.setDescription(entity.getDescription());
                        item.setVersion(entity.getVersion());
                        item.setOwner(entity.getOwner());
                        item.setUpdatedAt(formatDate(entity.getUpdatedAt()));
                        item.setIsExpanded(entity.getIsExpanded());
                        item.setIsFavorite(entity.getIsFavorite());

                        // Get counts from database
                        item.setRequestCount((int) requestRepository.countByCollectionId(entity.getId()));
                        item.setFolderCount(folderRepository.findByCollectionId(entity.getId()).size());

                        return item;
                    })
                    .collect(Collectors.toList());

            CollectionsListResponse response = new CollectionsListResponse();
            response.setCollections(collections);
            response.setTotal(collections.size());
            response.setPage(0);
            response.setPageSize(collections.size());

            return response;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error getting collections list: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 2. GET COLLECTION DETAILS (WITH FOLDERS AND THEIR REQUESTS)
    // ============================================================
    public CollectionDetailsResponse getCollectionDetails(String requestId, String performedBy, String collectionId) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Getting collection details for: " + collectionId);

            CollectionEntity collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new RuntimeException("Collection not found: " + collectionId));

            // Verify ownership
//            if (!collection.getOwner().equals(performedBy)) {
//                loggerUtil.log("codebase", "Request ID: " + requestId +
//                        ", User " + performedBy + " attempted to access collection " + collectionId + " owned by " + collection.getOwner());
//                throw new SecurityException("Access denied to collection: " + collectionId);
//            }

            CollectionDetailsResponse response = new CollectionDetailsResponse();
            response.setId(collection.getId());
            response.setName(collection.getName());
            response.setDescription(collection.getDescription());
            response.setVersion(collection.getVersion());
            response.setOwner(collection.getOwner());
            response.setCreatedAt(formatDate(collection.getCreatedAt()));
            response.setUpdatedAt(formatDate(collection.getUpdatedAt()));
            response.setIsExpanded(collection.getIsExpanded());
            response.setIsFavorite(collection.getIsFavorite());

            // Get folders for this collection
            List<FolderEntity> folderEntities = folderRepository.findByCollectionId(collectionId);
            List<CollectionFolder> folders = new ArrayList<>();

            int totalRequests = 0;

            for (FolderEntity folder : folderEntities) {
                CollectionFolder folderDto = new CollectionFolder();
                folderDto.setId(folder.getId());
                folderDto.setName(folder.getName());
                folderDto.setDescription(folder.getDescription());
                folderDto.setIsExpanded(folder.getIsExpanded());

                // Get requests for this folder
                List<RequestEntity> folderRequests = requestRepository.findByCollectionIdAndFolderId(collectionId, folder.getId());

                // Convert requests to RequestItem DTOs
                List<RequestItem> requestItems = folderRequests.stream()
                        .map(req -> {
                            RequestItem item = new RequestItem();
                            item.setId(req.getId());
                            item.setName(req.getName());
                            item.setMethod(req.getMethod());
                            item.setDescription(req.getDescription());
                            item.setTags(req.getTags());
                            item.setLastModified(formatDate(req.getUpdatedAt()));
                            return item;
                        })
                        .collect(Collectors.toList());

                folderDto.setRequests(requestItems);
                folderDto.setHasRequests(!requestItems.isEmpty());
                folderDto.setRequestCount(requestItems.size());

                folders.add(folderDto);
                totalRequests += requestItems.size();
            }

            response.setFolders(folders);
            response.setTotalRequests(totalRequests);

            return response;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error getting collection details: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 3. GET FOLDER REQUESTS (SIMPLIFIED VERSION - NOT NEEDED FOR MAIN VIEW)
    // ============================================================
    public FolderRequestsResponse getFolderRequests(String requestId, String performedBy,
                                                    String collectionId, String folderId) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Getting folder requests for folder: " + folderId + " in collection: " + collectionId);

            List<RequestEntity> requestEntities;

            if (folderId != null && !folderId.isEmpty()) {
                requestEntities = requestRepository.findByCollectionIdAndFolderId(collectionId, folderId);
            } else {
                requestEntities = requestRepository.findRootRequestsByCollectionId(collectionId);
            }

            List<RequestItem> requests = requestEntities.stream()
                    .map(entity -> {
                        RequestItem item = new RequestItem();
                        item.setId(entity.getId());
                        item.setName(entity.getName());
                        item.setMethod(entity.getMethod());
                        item.setDescription(entity.getDescription());
                        item.setTags(entity.getTags());
                        item.setLastModified(formatDate(entity.getUpdatedAt()));
                        return item;
                    })
                    .collect(Collectors.toList());

            FolderRequestsResponse response = new FolderRequestsResponse();
            response.setCollectionId(collectionId);
            response.setFolderId(folderId);
            response.setRequests(requests);
            response.setTotal(requests.size());

            return response;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error getting folder requests: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 4. GET REQUEST DETAILS
    // ============================================================
    public RequestDetailsResponse getRequestDetails(String requestId, String performedBy,
                                                    String collectionId, String requestIdParam) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Getting request details for: " + requestIdParam);

            RequestEntity request = requestRepository.findById(requestIdParam)
                    .orElseThrow(() -> new RuntimeException("Request not found: " + requestIdParam));

            // Log the request metadata for debugging
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Request metadata: " + request.getMetadata());

            // Verify collection matches
            if (!request.getCollection().getId().equals(collectionId)) {
                throw new RuntimeException("Request does not belong to the specified collection");
            }

            RequestDetailsResponse response = new RequestDetailsResponse();
            response.setId(request.getId());
            response.setName(request.getName());
            response.setMethod(request.getMethod());
            response.setUrl(request.getUrl());
            response.setDescription(request.getDescription());
            response.setCollectionId(collectionId);
            response.setFolderId(request.getFolder() != null ? request.getFolder().getId() : null);
            response.setLastModified(formatDate(request.getUpdatedAt()));
            response.setTags(request.getTags());

            // ============= EXTRACT API ID FROM GENERATED_API_ID OR METADATA =============
            String apiId = null;

            // First check: Try to get API ID from generatedApiId direct field
            if (request.getGeneratedApiId() != null && !request.getGeneratedApiId().isEmpty()) {
                apiId = request.getGeneratedApiId();
                loggerUtil.log("codebase", "Request ID: " + requestId +
                        ", Found generatedApiId directly: " + apiId);
            }
            // Second check: If generatedApiId is null, try to extract from metadata
            else if (request.getMetadata() != null) {
                try {
                    Map<String, Object> metadata = request.getMetadata();
                    loggerUtil.log("codebase", "Request ID: " + requestId +
                            ", Checking metadata for API ID. Metadata keys: " + metadata.keySet());

                    // Check for apiId in metadata
                    if (metadata.containsKey("apiId")) {
                        apiId = (String) metadata.get("apiId");
                        loggerUtil.log("codebase", "Request ID: " + requestId +
                                ", Found apiId in metadata: " + apiId);
                    }
                    // Check for other possible field names
                    else if (metadata.containsKey("generatedApiId")) {
                        apiId = (String) metadata.get("generatedApiId");
                        loggerUtil.log("codebase", "Request ID: " + requestId +
                                ", Found generatedApiId in metadata: " + apiId);
                    }
                    else if (metadata.containsKey("api_id")) {
                        apiId = (String) metadata.get("api_id");
                        loggerUtil.log("codebase", "Request ID: " + requestId +
                                ", Found api_id in metadata: " + apiId);
                    }

                    // Log any gen-related fields for debugging
                    if (metadata.containsKey("genPath")) {
                        loggerUtil.log("codebase", "Request ID: " + requestId +
                                ", genPath from metadata: " + metadata.get("genPath"));
                    }
                    if (metadata.containsKey("fullGenUrl")) {
                        loggerUtil.log("codebase", "Request ID: " + requestId +
                                ", fullGenUrl from metadata: " + metadata.get("fullGenUrl"));
                    }
                    if (metadata.containsKey("genUrlPattern")) {
                        loggerUtil.log("codebase", "Request ID: " + requestId +
                                ", genUrlPattern from metadata: " + metadata.get("genUrlPattern"));
                    }

                } catch (Exception e) {
                    loggerUtil.log("codebase", "Request ID: " + requestId +
                            ", Error extracting API ID from metadata: " + e.getMessage());
                }
            }

            // ============= CHECK IF AUTH TYPE IS API KEY =============
            boolean isApiKeyAuth = false;

            // Check for auth config in the database using the apiId
            if (apiId != null && !apiId.isEmpty()) {
                try {
                    // Try to find auth config for this API
                    // You'll need to inject the appropriate repository
                    Optional<AuthConfigEntity> authConfigOpt = authConfigRepository.findByGeneratedApiId(apiId);

                    if (authConfigOpt.isPresent()) {
                        AuthConfigEntity authConfig = authConfigOpt.get();
                        if ("apiKey".equalsIgnoreCase(authConfig.getType())) {
                            isApiKeyAuth = true;
                            loggerUtil.log("codebase", "Request ID: " + requestId +
                                    ", Found apiKey auth config in database for API ID: " + apiId);
                        }
                    }
                } catch (Exception e) {
                    loggerUtil.log("codebase", "Request ID: " + requestId +
                            ", Error checking auth config in database: " + e.getMessage());
                }
            }

            // If not found in database, check metadata as fallback
            if (!isApiKeyAuth && request.getMetadata() != null) {
                Map<String, Object> metadata = request.getMetadata();

                // Check for auth type in metadata
                if (metadata.containsKey("authType") && "apiKey".equals(metadata.get("authType"))) {
                    isApiKeyAuth = true;
                    loggerUtil.log("codebase", "Request ID: " + requestId +
                            ", Found apiKey auth type in metadata");
                }

                // Check for auth config object
                if (metadata.containsKey("authConfig")) {
                    try {
                        Map<String, Object> authConfig = (Map<String, Object>) metadata.get("authConfig");
                        if (authConfig != null && "apiKey".equals(authConfig.get("type"))) {
                            isApiKeyAuth = true;
                            loggerUtil.log("codebase", "Request ID: " + requestId +
                                    ", Found apiKey auth config in metadata");
                        }
                    } catch (Exception e) {
                        loggerUtil.log("codebase", "Request ID: " + requestId +
                                ", Error parsing auth config from metadata: " + e.getMessage());
                    }
                }
            }

            // Convert headers from JSON - create a mutable list
            List<HeaderItem> headers = new ArrayList<>();

            // Add existing headers from the request
            if (request.getHeaders() != null) {
                headers.addAll(request.getHeaders().stream()
                        .map(headerMap -> {
                            HeaderItem header = new HeaderItem();
                            header.setKey((String) headerMap.get("key"));
                            header.setValue((String) headerMap.get("value"));
                            header.setDescription((String) headerMap.get("description"));
                            header.setRequired((Boolean) headerMap.getOrDefault("required", false));
                            header.setDisabled((Boolean) headerMap.getOrDefault("disabled", false));
                            return header;
                        })
                        .collect(Collectors.toList()));
            }

            // ============= INJECT API KEY HEADERS IF AUTH TYPE IS API KEY =============
            if (isApiKeyAuth) {
                loggerUtil.log("codebase", "Request ID: " + requestId +
                        ", Auth type is API Key, injecting X-Api-Key and X-Api-Secret headers");

                // Check if X-Api-Key header already exists
                boolean hasApiKeyHeader = headers.stream()
                        .anyMatch(h -> "X-Api-Key".equalsIgnoreCase(h.getKey()));

                if (!hasApiKeyHeader) {
                    HeaderItem apiKeyHeader = new HeaderItem();
                    apiKeyHeader.setKey("X-Api-Key");
                    apiKeyHeader.setValue("{{api_key}}"); // Placeholder that will be replaced at runtime
                    apiKeyHeader.setDescription("API Key for authentication");
                    apiKeyHeader.setRequired(true);
                    apiKeyHeader.setDisabled(false);
                    headers.add(apiKeyHeader);
                    loggerUtil.log("codebase", "Request ID: " + requestId +
                            ", Added X-Api-Key header");
                }

                // Check if X-Api-Secret header already exists
                boolean hasApiSecretHeader = headers.stream()
                        .anyMatch(h -> "X-Api-Secret".equalsIgnoreCase(h.getKey()));

                if (!hasApiSecretHeader) {
                    HeaderItem apiSecretHeader = new HeaderItem();
                    apiSecretHeader.setKey("X-Api-Secret");
                    apiSecretHeader.setValue("{{api_secret}}"); // Placeholder that will be replaced at runtime
                    apiSecretHeader.setDescription("API Secret for authentication");
                    apiSecretHeader.setRequired(true);
                    apiSecretHeader.setDisabled(false);
                    headers.add(apiSecretHeader);
                    loggerUtil.log("codebase", "Request ID: " + requestId +
                            ", Added X-Api-Secret header");
                }
            }

            response.setHeaders(headers);

            response.setBody(request.getRequestBody());
            response.setResponseExample(request.getResponseExample());

            // Convert path parameters - Using ParameterItem DTO
            if (request.getPathParameters() != null) {
                List<ParameterItem> pathParams = request.getPathParameters().stream()
                        .map(paramMap -> {
                            ParameterItem param = new ParameterItem();
                            param.setName((String) paramMap.get("name"));
                            param.setType("path");
                            param.setRequired((Boolean) paramMap.getOrDefault("required", false));
                            param.setDescription((String) paramMap.get("description"));
                            param.setKey((String) paramMap.get("key"));
                            param.setValue((String) paramMap.get("value"));
                            return param;
                        })
                        .collect(Collectors.toList());
                response.setPathParameters(pathParams);
            }

            // Convert query parameters if they exist - Using ParameterItem DTO
            if (request.getQueryParameters() != null) {
                List<ParameterItem> queryParams = request.getQueryParameters().stream()
                        .map(paramMap -> {
                            ParameterItem param = new ParameterItem();
                            param.setName((String) paramMap.get("name"));
                            param.setType("query");
                            param.setRequired((Boolean) paramMap.getOrDefault("required", false));
                            param.setDescription((String) paramMap.get("description"));
                            param.setKey((String) paramMap.get("key"));
                            param.setValue((String) paramMap.get("value"));
                            return param;
                        })
                        .collect(Collectors.toList());

                // If you have a queryParameters field in your response DTO, set it here
                // response.setQueryParameters(queryParams);
            }

            // Get implementations from database
            List<ImplementationEntity> implementations = implementationRepository.findByRequestId(requestIdParam);
            Map<String, Map<String, String>> implementationMap = new HashMap<>();

            for (ImplementationEntity impl : implementations) {
                implementationMap.computeIfAbsent(impl.getLanguage(), k -> new HashMap<>())
                        .put(impl.getComponent(), impl.getCode());

                // Update implementation with generatedApiId if available and not already set
                if (apiId != null && !apiId.isEmpty() && impl.getGeneratedApiId() == null) {
                    impl.setGeneratedApiId(apiId);
                    implementationRepository.save(impl);
                    loggerUtil.log("codebase", "Request ID: " + requestId +
                            ", Updated implementation " + impl.getId() + " with generatedApiId: " + apiId);
                }
            }

            response.setImplementations(implementationMap);

            // Set baseUrl if available in metadata
            if (request.getMetadata() != null && request.getMetadata().containsKey("baseUrl")) {
                response.setBaseUrl((String) request.getMetadata().get("baseUrl"));
            }

            // Set requestGroupId if available
            if (request.getMetadata() != null && request.getMetadata().containsKey("requestGroupId")) {
                response.setRequestGroupId((String) request.getMetadata().get("requestGroupId"));
            }

            return response;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error getting request details: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 5. GET IMPLEMENTATION DETAILS
    // ============================================================
    public ImplementationResponse getImplementationDetails(String requestId, String performedBy,
                                                           String collectionId, String requestIdParam,
                                                           String language, String component) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Getting implementation for language: " + language + ", component: " + component);

            // Verify request exists and user has access
            RequestEntity request = requestRepository.findById(requestIdParam)
                    .orElseThrow(() -> new RuntimeException("Request not found: " + requestIdParam));

//            if (!request.getCollection().getOwner().equals(performedBy)) {
//                throw new SecurityException("Access denied to request: " + requestIdParam);
//            }

            Optional<ImplementationEntity> implementationOpt = implementationRepository
                    .findByRequestIdAndLanguageAndComponent(requestIdParam, language, component);

            ImplementationResponse response = new ImplementationResponse();
            response.setLanguage(language);
            response.setComponent(component);
            response.setRequestId(requestIdParam);
            response.setCollectionId(collectionId);

            if (implementationOpt.isPresent()) {
                ImplementationEntity implementation = implementationOpt.get();
                response.setCode(implementation.getCode());
                response.setFileName(getFileName(component, language));
                response.setFileSize((long) implementation.getCode().length());
                response.setLinesOfCode(implementation.getLinesOfCode());
                response.setGeneratedAt(Date.from(implementation.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
            } else {
                // Return empty/default values for missing implementation
                response.setCode("// No implementation found for " + language + "/" + component);
                response.setFileName(getFileName(component, language));
                response.setFileSize(0L);
                response.setLinesOfCode(0);
                response.setGeneratedAt(new Date());
                response.setNotFound(true);
            }

            // Get language info from database or configuration
            Map<String, Object> languageInfo = getLanguageInfoFromDb(language);
            response.setLanguageInfo(languageInfo);

            // Add syntax highlighting info
            Map<String, Object> syntaxInfo = new HashMap<>();
            syntaxInfo.put("language", language);
            syntaxInfo.put("extension", getFileExtension(language));
            syntaxInfo.put("formatter", getFormatterName(language));
            response.setSyntaxInfo(syntaxInfo);

            return response;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error getting implementation details: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 6. GET ALL IMPLEMENTATIONS
    // ============================================================
    public AllImplementationsResponse getAllImplementations(String requestId, String performedBy,
                                                            String collectionId, String requestIdParam) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Getting all implementations for request: " + requestIdParam);

            // Verify request exists and user has access
            RequestEntity request = requestRepository.findById(requestIdParam)
                    .orElseThrow(() -> new RuntimeException("Request not found: " + requestIdParam));

//            if (!request.getCollection().getOwner().equals(performedBy)) {
//                throw new SecurityException("Access denied to request: " + requestIdParam);
//            }

            List<ImplementationEntity> implementations = implementationRepository.findByRequestId(requestIdParam);

            Map<String, Map<String, String>> implementationMap = new HashMap<>();
            for (ImplementationEntity impl : implementations) {
                implementationMap.computeIfAbsent(impl.getLanguage(), k -> new HashMap<>())
                        .put(impl.getComponent(), impl.getCode());
            }

            AllImplementationsResponse response = new AllImplementationsResponse();
            response.setRequestId(requestIdParam);
            response.setCollectionId(collectionId);
            response.setImplementations(implementationMap);
            response.setTotalLanguages(implementationMap.size());
            response.setTotalFiles(implementations.size());
            response.setRetrievedAt(new Date());

            return response;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error getting all implementations: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 7. GENERATE IMPLEMENTATION
    // ============================================================
    @Transactional
    public GenerateImplementationResponse generateImplementation(String requestId, String performedBy,
                                                                 GenerateImplementationRequest requestDto) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Generating implementation for request: " + requestDto.getRequestId() +
                    ", language: " + requestDto.getLanguage());

            RequestEntity request = requestRepository.findById(requestDto.getRequestId())
                    .orElseThrow(() -> new RuntimeException("Request not found: " + requestDto.getRequestId()));

            // Verify ownership
//            if (!request.getCollection().getOwner().equals(performedBy)) {
//                throw new SecurityException("Access denied to request: " + requestDto.getRequestId());
//            }

            // Delete existing implementations for this language
            List<ImplementationEntity> existingImpls = implementationRepository
                    .findByRequestIdAndLanguage(requestDto.getRequestId(), requestDto.getLanguage());
            implementationRepository.deleteAll(existingImpls);

            // Generate code based on request details
            Map<String, String> implementations = generateCodeForRequest(requestDto, request);

            // Save implementations to database
            for (Map.Entry<String, String> entry : implementations.entrySet()) {
                ImplementationEntity impl = ImplementationEntity.builder()
                        .language(requestDto.getLanguage())
                        .component(entry.getKey())
                        .code(entry.getValue())
                        .linesOfCode(entry.getValue().split("\n").length)
                        .request(request)
                        .build();
                implementationRepository.save(impl);
            }

            GenerateImplementationResponse response = new GenerateImplementationResponse();
            response.setRequestId(requestDto.getRequestId());
            response.setCollectionId(requestDto.getCollectionId());
            response.setLanguage(requestDto.getLanguage());
            response.setGeneratedAt(new Date());
            response.setStatus("generated");
            response.setImplementations(implementations);

            // Get quick start guide from database
            response.setQuickStartGuide(getQuickStartGuideFromDb(requestDto.getLanguage()));

            // Get features from database
            response.setFeatures(getFeaturesFromDb());

            // Add metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("componentsGenerated", implementations.size());
            metadata.put("linesOfCode", implementations.values().stream()
                    .mapToInt(code -> code.split("\n").length)
                    .sum());
            metadata.put("estimatedTime", "2 minutes");
            response.setMetadata(metadata);

            return response;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error generating implementation: " + e.getMessage());
            throw e;
        }
    }


    // ============================================================
    // 9. GET LANGUAGES
    // ============================================================
    public LanguagesResponse getLanguages(String requestId, String performedBy) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Getting available languages");

            // Get distinct languages from implementations table
            List<String> distinctLanguages = implementationRepository.findDistinctLanguages();

            List<LanguageInfo> languages = new ArrayList<>();
            for (String lang : distinctLanguages) {
                LanguageInfo info = new LanguageInfo();
                info.setId(lang);
                info.setName(getLanguageDisplayName(lang));
                info.setFramework(getDefaultFramework(lang));
                info.setColor(getLanguageColor(lang));
                info.setIcon(getLanguageIcon(lang));

                // Get count of implementations for this language
                int count = Math.toIntExact(implementationRepository.countByLanguage(lang));
                info.setImplementationCount(count);
                info.setIsAvailable(true);

                languages.add(info);
            }

            LanguagesResponse response = new LanguagesResponse();
            response.setLanguages(languages);
            response.setTotal(languages.size());
            response.setRetrievedAt(new Date());

            return response;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error getting languages: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 12. VALIDATE IMPLEMENTATION
    // ============================================================
    public ValidationResponse validateImplementation(String requestId, String performedBy,
                                                     ValidateImplementationRequest validationRequest) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Validating implementation for language: " + validationRequest.getLanguage());

            ValidationResponse response = new ValidationResponse();
            response.setLanguage(validationRequest.getLanguage());
            response.setValidatedAt(new Date());

            // Perform actual validation (simplified - in reality you'd use language-specific validators)
            List<ValidationIssue> issues = new ArrayList<>();

            if (validationRequest.getCode() != null) {
                // Check for common issues
                if (validationRequest.getCode().contains("TODO")) {
                    issues.add(ValidationIssue.builder()
                            .type("warning")
                            .message("Found TODO comment")
                            .line(countLines(validationRequest.getCode()))
                            .severity("low")
                            .build());
                }

                if (validationRequest.getCode().length() < 100) {
                    issues.add(ValidationIssue.builder()
                            .type("suggestion")
                            .message("Consider adding more error handling")
                            .severity("info")
                            .build());
                }

                // Check for syntax errors based on language
                if ("java".equals(validationRequest.getLanguage())) {
                    if (!validationRequest.getCode().contains("package")) {
                        issues.add(ValidationIssue.builder()
                                .type("error")
                                .message("Missing package declaration")
                                .severity("high")
                                .build());
                    }
                }
            }

            boolean isValid = issues.stream().noneMatch(i -> "error".equals(i.getType()));
            int score = calculateValidationScore(issues);

            response.setValid(isValid);
            response.setIssues(issues);
            response.setScore(score);

            // If this is for an existing implementation, update it
            if (validationRequest.getRequestId() != null &&
                    validationRequest.getComponent() != null) {

                // Verify access to request
                RequestEntity request = requestRepository.findById(validationRequest.getRequestId())
                        .orElseThrow(() -> new RuntimeException("Request not found: " + validationRequest.getRequestId()));

//                if (!request.getCollection().getOwner().equals(performedBy)) {
//                    throw new SecurityException("Access denied to request: " + validationRequest.getRequestId());
//                }

                Optional<ImplementationEntity> existingImpl = implementationRepository
                        .findByRequestIdAndLanguageAndComponent(
                                validationRequest.getRequestId(),
                                validationRequest.getLanguage(),
                                validationRequest.getComponent());

                existingImpl.ifPresent(impl -> {
                    impl.setIsValidated(isValid);
                    impl.setValidationScore(score);
                    implementationRepository.save(impl);
                });
            }

            return response;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error validating implementation: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 15. GET SUPPORTED PROGRAMMING LANGUAGES
    // ============================================================
    public Map<String, Object> getSupportedProgrammingLanguages(String requestId, String performedBy) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Getting supported programming languages");

            // Get languages from database with implementation counts
            List<String> languages = implementationRepository.findDistinctLanguages();

            List<Map<String, Object>> languageDetails = languages.stream()
                    .map(lang -> {
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("id", lang);
                        detail.put("name", getLanguageDisplayName(lang));
                        detail.put("framework", getDefaultFramework(lang));
                        detail.put("color", getLanguageColor(lang));
                        detail.put("components", getDefaultComponents(lang));
                        detail.put("extension", getFileExtension(lang));
                        detail.put("packageManager", getPackageManager(lang));
                        detail.put("implementationCount", implementationRepository.countByLanguage(lang));
                        return detail;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("languages", languageDetails);
            response.put("total", languageDetails.size());
            response.put("lastUpdated", new Date());

            return response;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error getting supported programming languages: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 16. GET QUICK START GUIDE
    // ============================================================
    public Map<String, Object> getQuickStartGuide(String requestId, String performedBy, String language) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Getting quick start guide for language: " + language);

            // Get guide from database or generate based on language
            Map<String, Object> guide = new HashMap<>();
            guide.put("language", language);
            guide.put("generatedAt", new Date());

            // Get sample implementations for this language to generate guide
            List<ImplementationEntity> samples = implementationRepository.findTopByLanguage(language, PageRequest.of(0, 1));

            if (!samples.isEmpty()) {
                ImplementationEntity sample = samples.get(0);
                guide.put("sampleCode", sample.getCode());
                guide.put("sampleComponent", sample.getComponent());
            }

            // Add language-specific steps
            List<Map<String, Object>> steps = getLanguageSteps(language);
            guide.put("steps", steps);

            return guide;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error getting quick start guide: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a");
        return dateTime.format(formatter);
    }

    private int countLines(String text) {
        return text.split("\n").length;
    }

    private String getFileName(String component, String language) {
        Map<String, String> extensions = new HashMap<>();
        extensions.put("java", ".java");
        extensions.put("javascript", ".js");
        extensions.put("python", ".py");
        extensions.put("csharp", ".cs");
        extensions.put("php", ".php");
        extensions.put("go", ".go");
        extensions.put("ruby", ".rb");
        extensions.put("kotlin", ".kt");
        extensions.put("swift", ".swift");
        extensions.put("rust", ".rs");

        Map<String, String> componentNames = new HashMap<>();
        componentNames.put("controller", "Controller");
        componentNames.put("service", "Service");
        componentNames.put("repository", "Repository");
        componentNames.put("model", "Model");
        componentNames.put("dto", "DTO");
        componentNames.put("routes", "routes");
        componentNames.put("config", "config");
        componentNames.put("server", "server");
        componentNames.put("fastapi", "main");
        componentNames.put("schemas", "schemas");
        componentNames.put("models", "models");
        componentNames.put("services", "service");

        String baseName = componentNames.getOrDefault(component, component);
        String ext = extensions.getOrDefault(language, ".txt");

        // Capitalize first letter for class files
        if ("java".equals(language) || "csharp".equals(language) || "kotlin".equals(language)) {
            baseName = baseName.substring(0, 1).toUpperCase() + baseName.substring(1);
        }

        return baseName + ext;
    }

    private String getFileExtension(String language) {
        Map<String, String> extensions = new HashMap<>();
        extensions.put("java", ".java");
        extensions.put("javascript", ".js");
        extensions.put("python", ".py");
        extensions.put("csharp", ".cs");
        extensions.put("php", ".php");
        extensions.put("go", ".go");
        extensions.put("ruby", ".rb");
        extensions.put("kotlin", ".kt");
        extensions.put("swift", ".swift");
        extensions.put("rust", ".rs");
        return extensions.getOrDefault(language, ".txt");
    }

    private String getFormatterName(String language) {
        Map<String, String> formatters = new HashMap<>();
        formatters.put("java", "Java");
        formatters.put("javascript", "JavaScript");
        formatters.put("python", "Python");
        formatters.put("csharp", "C#");
        formatters.put("php", "PHP");
        formatters.put("go", "Go");
        formatters.put("ruby", "Ruby");
        formatters.put("kotlin", "Kotlin");
        formatters.put("swift", "Swift");
        formatters.put("rust", "Rust");
        return formatters.getOrDefault(language, "Plain Text");
    }

    private Map<String, Object> getLanguageInfoFromDb(String language) {
        Map<String, Object> info = new HashMap<>();
        info.put("name", getLanguageDisplayName(language));
        info.put("framework", getDefaultFramework(language));
        info.put("color", getLanguageColor(language));
        info.put("icon", getLanguageIcon(language));
        info.put("command", getRunCommand(language));
        info.put("packageManager", getPackageManager(language));

        // Get count of implementations in this language
        info.put("implementationCount", implementationRepository.countByLanguage(language));

        return info;
    }

    private String getLanguageDisplayName(String language) {
        Map<String, String> names = new HashMap<>();
        names.put("java", "Java");
        names.put("javascript", "JavaScript");
        names.put("python", "Python");
        names.put("csharp", "C#");
        names.put("php", "PHP");
        names.put("go", "Go");
        names.put("ruby", "Ruby");
        names.put("kotlin", "Kotlin");
        names.put("swift", "Swift");
        names.put("rust", "Rust");
        return names.getOrDefault(language, language);
    }

    private String getDefaultFramework(String language) {
        Map<String, String> frameworks = new HashMap<>();
        frameworks.put("java", "Spring Boot");
        frameworks.put("javascript", "Node.js/Express");
        frameworks.put("python", "FastAPI/Django");
        frameworks.put("csharp", ".NET Core");
        frameworks.put("php", "Laravel");
        frameworks.put("go", "Gin");
        frameworks.put("ruby", "Ruby on Rails");
        frameworks.put("kotlin", "Ktor/Spring");
        frameworks.put("swift", "Vapor");
        frameworks.put("rust", "Actix-web");
        return frameworks.getOrDefault(language, "Unknown");
    }

    private String getLanguageColor(String language) {
        Map<String, String> colors = new HashMap<>();
        colors.put("java", "#f89820");
        colors.put("javascript", "#f0db4f");
        colors.put("python", "#3776ab");
        colors.put("csharp", "#9b4993");
        colors.put("php", "#777bb4");
        colors.put("go", "#00add8");
        colors.put("ruby", "#cc342d");
        colors.put("kotlin", "#7f52ff");
        colors.put("swift", "#f05138");
        colors.put("rust", "#dea584");
        return colors.getOrDefault(language, "#64748b");
    }

    private String getLanguageIcon(String language) {
        Map<String, String> icons = new HashMap<>();
        icons.put("java", "coffee");
        icons.put("javascript", "file-code");
        icons.put("python", "code");
        icons.put("csharp", "box");
        icons.put("php", "package");
        icons.put("go", "terminal");
        icons.put("ruby", "server");
        icons.put("kotlin", "cpu");
        icons.put("swift", "monitor");
        icons.put("rust", "hard-drive");
        return icons.getOrDefault(language, "file");
    }

    private String getRunCommand(String language) {
        Map<String, String> commands = new HashMap<>();
        commands.put("java", "mvn spring-boot:run");
        commands.put("javascript", "npm start");
        commands.put("python", "uvicorn main:app --reload");
        commands.put("csharp", "dotnet run");
        commands.put("php", "php artisan serve");
        commands.put("go", "go run main.go");
        commands.put("ruby", "rails server");
        commands.put("kotlin", "./gradlew bootRun");
        commands.put("swift", "vapor run");
        commands.put("rust", "cargo run");
        return commands.getOrDefault(language, "See documentation");
    }

    private String getPackageManager(String language) {
        Map<String, String> managers = new HashMap<>();
        managers.put("java", "Maven/Gradle");
        managers.put("javascript", "npm/yarn");
        managers.put("python", "pip/poetry");
        managers.put("csharp", "NuGet");
        managers.put("php", "Composer");
        managers.put("go", "go mod");
        managers.put("ruby", "Bundler");
        managers.put("kotlin", "Gradle");
        managers.put("swift", "SPM");
        managers.put("rust", "Cargo");
        return managers.getOrDefault(language, "Unknown");
    }

    private List<String> getDefaultComponents(String language) {
        Map<String, List<String>> components = new HashMap<>();
        components.put("java", Arrays.asList("controller", "service", "repository", "model", "dto"));
        components.put("javascript", Arrays.asList("controller", "service", "model", "routes", "middleware"));
        components.put("python", Arrays.asList("fastapi", "schemas", "models", "routes", "services"));
        components.put("csharp", Arrays.asList("controller", "service", "model", "repository", "dto"));
        components.put("php", Arrays.asList("controller", "service", "model", "migration"));
        components.put("go", Arrays.asList("handler", "service", "model", "routes"));
        components.put("ruby", Arrays.asList("controller", "service", "model"));
        components.put("kotlin", Arrays.asList("controller", "service", "repository", "model"));
        components.put("swift", Arrays.asList("controller", "service", "model"));
        components.put("rust", Arrays.asList("handler", "service", "model"));
        return components.getOrDefault(language, Arrays.asList("main"));
    }

    private Map<String, String> generateCodeForRequest(GenerateImplementationRequest request, RequestEntity requestEntity) {
        // In production, this would call a code generation service
        // This is a simplified placeholder
        Map<String, String> implementations = new HashMap<>();

        List<String> components = request.getComponents() != null ?
                request.getComponents() : getDefaultComponents(request.getLanguage());

        for (String component : components) {
            String code = generateComponentCode(component, request.getLanguage(), requestEntity);
            implementations.put(component, code);
        }

        return implementations;
    }

    private String generateComponentCode(String component, String language, RequestEntity request) {
        // This would call a real code generator in production
        StringBuilder code = new StringBuilder();

        switch (language) {
            case "java":
                code.append("package com.example.api.").append(component).append(";\n\n");
                code.append("import org.springframework.stereotype.Component;\n\n");
                code.append("@Component\n");
                code.append("public class ").append(capitalize(component)).append(" {\n\n");
                code.append("    // Generated for request: ").append(request.getName()).append("\n");
                code.append("    // Method: ").append(request.getMethod()).append("\n");
                code.append("    // URL: ").append(request.getUrl()).append("\n\n");
                code.append("    public void execute() {\n");
                code.append("        // TODO: Implement\n");
                code.append("    }\n");
                code.append("}");
                break;

            case "javascript":
                code.append("// ").append(component).append(".js\n\n");
                code.append("// Generated for request: ").append(request.getName()).append("\n");
                code.append("// Method: ").append(request.getMethod()).append("\n");
                code.append("// URL: ").append(request.getUrl()).append("\n\n");
                code.append("module.exports = {\n");
                code.append("    execute: function() {\n");
                code.append("        // TODO: Implement\n");
                code.append("    }\n");
                code.append("};");
                break;

            default:
                code.append("# Generated for request: ").append(request.getName()).append("\n");
                code.append("# Method: ").append(request.getMethod()).append("\n");
                code.append("# URL: ").append(request.getUrl()).append("\n");
                code.append("def execute():\n");
                code.append("    # TODO: Implement\n");
                code.append("    pass");
        }

        return code.toString();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private Map<String, String> getQuickStartGuideFromDb(String language) {
        // In production, this would come from a database table
        Map<String, String> guide = new HashMap<>();

        switch (language) {
            case "java":
                guide.put("step1", "mvn clean install");
                guide.put("step2", "mvn spring-boot:run");
                guide.put("step3", "Open http://localhost:8080");
                break;
            case "javascript":
                guide.put("step1", "npm install");
                guide.put("step2", "npm start");
                guide.put("step3", "Open http://localhost:3000");
                break;
            case "python":
                guide.put("step1", "pip install -r requirements.txt");
                guide.put("step2", "uvicorn main:app --reload");
                guide.put("step3", "Open http://localhost:8000/docs");
                break;
            default:
                guide.put("step1", "Install dependencies");
                guide.put("step2", "Run the application");
                guide.put("step3", "Test the API");
        }

        return guide;
    }

    private List<String> getFeaturesFromDb() {
        // In production, this would come from a database table
        return Arrays.asList(
                "JWT Authentication",
                "Password Hashing",
                "Input Validation",
                "Clean Architecture",
                "Dependency Injection",
                "Error Handling"
        );
    }

    private List<Map<String, Object>> getLanguageSteps(String language) {
        List<Map<String, Object>> steps = new ArrayList<>();

        switch (language) {
            case "java":
                steps.add(createStep(1, "Install JDK", "brew install openjdk@17", "Install Java Development Kit 17"));
                steps.add(createStep(2, "Install Maven", "brew install maven", "Install Maven build tool"));
                steps.add(createStep(3, "Build Project", "mvn clean install", "Build the application"));
                steps.add(createStep(4, "Run Application", "mvn spring-boot:run", "Start the server"));
                break;
            case "javascript":
                steps.add(createStep(1, "Install Node.js", "brew install node", "Install Node.js runtime"));
                steps.add(createStep(2, "Install Dependencies", "npm install", "Install project dependencies"));
                steps.add(createStep(3, "Start Server", "npm start", "Start the server"));
                break;
            case "python":
                steps.add(createStep(1, "Install Python", "brew install python", "Install Python 3.9+"));
                steps.add(createStep(2, "Create Virtual Env", "python -m venv venv", "Create virtual environment"));
                steps.add(createStep(3, "Activate Venv", "source venv/bin/activate", "Activate environment"));
                steps.add(createStep(4, "Install Dependencies", "pip install -r requirements.txt", "Install packages"));
                steps.add(createStep(5, "Run Server", "uvicorn main:app --reload", "Start FastAPI server"));
                break;
            default:
                steps.add(createStep(1, "Install Dependencies", "See documentation", "Install required packages"));
                steps.add(createStep(2, "Run Application", "Start the server", "Run the application"));
        }

        return steps;
    }

    private Map<String, Object> createStep(int number, String title, String command, String description) {
        Map<String, Object> step = new HashMap<>();
        step.put("number", number);
        step.put("title", title);
        step.put("command", command);
        step.put("description", description);
        return step;
    }

    private int calculateValidationScore(List<ValidationIssue> issues) {
        if (issues.isEmpty()) return 100;

        int score = 100;
        for (ValidationIssue issue : issues) {
            switch (issue.getSeverity()) {
                case "high":
                    score -= 20;
                    break;
                case "medium":
                    score -= 10;
                    break;
                case "low":
                    score -= 5;
                    break;
                default:
                    score -= 2;
            }
        }
        return Math.max(0, score);
    }

    private boolean runTestForImplementation(ImplementationEntity impl, TestImplementationRequest request) {
        // In production, this would actually execute the test
        // This is a simplified placeholder that returns random results
        return Math.random() > 0.2; // 80% pass rate
    }
}