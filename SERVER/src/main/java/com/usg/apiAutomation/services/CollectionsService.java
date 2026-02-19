package com.usg.apiAutomation.services;

import com.usg.apiAutomation.dtos.collections.*;
import com.usg.apiAutomation.utils.LoggerUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CollectionsService {

    private final LoggerUtil loggerUtil;

    // Cache for collections data
    private final Map<String, CollectionsCache> collectionsCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes cache TTL

    @PostConstruct
    public void init() {
        log.info("CollectionsService initialized");
        preloadCollectionsCache();
    }

    // ========== PUBLIC SERVICE METHODS ==========

    public CollectionsListResponseDTO getCollectionsList(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("RequestEntity ID: {}, Getting collections list for user: {}", requestId, performedBy);
            loggerUtil.log("collections",
                    "RequestEntity ID: " + requestId + ", Getting collections list for user: " + performedBy);

            // Check cache first
            String cacheKey = "collections_list_" + performedBy;
            CollectionsCache cachedData = collectionsCache.get(cacheKey);

            if (cachedData != null && !isCacheExpired(cachedData)) {
                log.debug("RequestEntity ID: {}, Returning cached collections list", requestId);
                return (CollectionsListResponseDTO) cachedData.getData();
            }

            CollectionsListResponseDTO collections = generateStaticCollectionsList(performedBy);

            // Update cache
            collectionsCache.put(cacheKey, new CollectionsCache(collections, System.currentTimeMillis()));

            log.info("RequestEntity ID: {}, Retrieved {} collections", requestId, collections.getCollections().size());

            return collections;

        } catch (Exception e) {
            String errorMsg = "Error retrieving collections list: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
            return getFallbackCollectionsList();
        }
    }

    public CollectionDetailsResponseDTO getCollectionDetails(String requestId, HttpServletRequest req, String performedBy,
                                                             String collectionId) {
        try {
            log.info("RequestEntity ID: {}, Getting collectionEntity details for: {}", requestId, collectionId);

            // Check cache first
            String cacheKey = "collection_details_" + performedBy + "_" + collectionId;
            CollectionsCache cachedData = collectionsCache.get(cacheKey);

            if (cachedData != null && !isCacheExpired(cachedData)) {
                log.debug("RequestEntity ID: {}, Returning cached collectionEntity details", requestId);
                return (CollectionDetailsResponseDTO) cachedData.getData();
            }

            CollectionDetailsResponseDTO details = generateStaticCollectionDetails(collectionId);

            // Update cache
            collectionsCache.put(cacheKey, new CollectionsCache(details, System.currentTimeMillis()));

            log.info("RequestEntity ID: {}, Retrieved details for collectionEntity: {}", requestId, collectionId);

            return details;

        } catch (Exception e) {
            String errorMsg = "Error retrieving collectionEntity details: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
            return getFallbackCollectionDetails(collectionId);
        }
    }

    public RequestDetailsResponseDTO getRequestDetails(String requestId, HttpServletRequest req, String performedBy,
                                                       String collectionId, String requestIdParam) {
        try {
            log.info("RequestEntity ID: {}, Getting requestEntity details for: {}", requestId, requestIdParam);

            // Check cache first
            String cacheKey = "request_details_" + performedBy + "_" + collectionId + "_" + requestIdParam;
            CollectionsCache cachedData = collectionsCache.get(cacheKey);

            if (cachedData != null && !isCacheExpired(cachedData)) {
                log.debug("RequestEntity ID: {}, Returning cached requestEntity details", requestId);
                return (RequestDetailsResponseDTO) cachedData.getData();
            }

            RequestDetailsResponseDTO details = generateStaticRequestDetails(collectionId, requestIdParam);

            // Update cache
            collectionsCache.put(cacheKey, new CollectionsCache(details, System.currentTimeMillis()));

            log.info("RequestEntity ID: {}, Retrieved details for requestEntity: {}", requestId, requestIdParam);

            return details;

        } catch (Exception e) {
            String errorMsg = "Error retrieving requestEntity details: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
            return getFallbackRequestDetails(requestIdParam);
        }
    }

    public ExecuteRequestResponseDTO executeRequest(String requestId, HttpServletRequest req, String performedBy,
                                                    ExecuteRequestDTO requestDto) {
        try {
            log.info("RequestEntity ID: {}, Executing requestEntity for user: {}", requestId, performedBy);
            loggerUtil.log("collections",
                    "RequestEntity ID: " + requestId + ", Executing requestEntity: " + requestDto.getMethod() + " " + requestDto.getUrl());

            ExecuteRequestResponseDTO response = executeSampleRequest(requestDto);

            log.info("RequestEntity ID: {}, RequestEntity executed successfully, status: {}",
                    requestId, response.getStatusCode());

            return response;

        } catch (Exception e) {
            String errorMsg = "Error executing requestEntity: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
            return new ExecuteRequestResponseDTO(
                    "",
                    500,
                    "Error executing requestEntity: " + e.getMessage(),
                    Collections.emptyList(),
                    0L,
                    0L
            );
        }
    }

    public SaveRequestResponseDTO saveRequest(String requestId, HttpServletRequest req, String performedBy,
                                              SaveRequestDTO requestDto) {
        try {
            log.info("RequestEntity ID: {}, Saving requestEntity for user: {}", requestId, performedBy);

            SaveRequestResponseDTO response = saveSampleRequest(requestDto);

            log.info("RequestEntity ID: {}, RequestEntity saved successfully: {}", requestId, response.getRequestId());

            // Clear relevant cache
            clearCollectionCache(performedBy, requestDto.getCollectionId());

            return response;

        } catch (Exception e) {
            String errorMsg = "Error saving requestEntity: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
            return new SaveRequestResponseDTO("", "Error saving requestEntity: " + e.getMessage());
        }
    }

    public CreateCollectionResponseDTO createCollection(String requestId, HttpServletRequest req, String performedBy,
                                                        CreateCollectionDTO collectionDto) {
        try {
            log.info("RequestEntity ID: {}, Creating collectionEntity for user: {}", requestId, performedBy);

            CreateCollectionResponseDTO response = createSampleCollection(collectionDto);

            log.info("RequestEntity ID: {}, CollectionEntity created successfully: {}", requestId, response.getCollectionId());

            // Clear collections list cache
            clearUserCollectionsCache(performedBy);

            return response;

        } catch (Exception e) {
            String errorMsg = "Error creating collectionEntity: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
            return new CreateCollectionResponseDTO("", "Error creating collectionEntity: " + e.getMessage());
        }
    }

    public CodeSnippetResponseDTO generateCodeSnippet(String requestId, HttpServletRequest req, String performedBy,
                                                      CodeSnippetRequestDTO snippetRequest) {
        try {
            log.info("RequestEntity ID: {}, Generating code snippet for language: {}",
                    requestId, snippetRequest.getLanguage());

            CodeSnippetResponseDTO snippet = generateSampleCodeSnippet(snippetRequest);

            log.info("RequestEntity ID: {}, Generated code snippet for {}", requestId, snippetRequest.getLanguage());

            return snippet;

        } catch (Exception e) {
            String errorMsg = "Error generating code snippet: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
            return new CodeSnippetResponseDTO("", snippetRequest.getLanguage(),
                    "Error generating code snippet: " + e.getMessage());
        }
    }

    public EnvironmentsResponseDTO getEnvironments(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("RequestEntity ID: {}, Getting environments for user: {}", requestId, performedBy);

            EnvironmentsResponseDTO environments = generateStaticEnvironments();

            log.info("RequestEntity ID: {}, Retrieved {} environments", requestId, environments.getEnvironments().size());

            return environments;

        } catch (Exception e) {
            String errorMsg = "Error retrieving environments: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
            return new EnvironmentsResponseDTO(Collections.emptyList());
        }
    }

    public ImportResponseDTO importCollection(String requestId, HttpServletRequest req, String performedBy,
                                              ImportRequestDTO importRequest) {
        try {
            log.info("RequestEntity ID: {}, Importing collectionEntity for user: {}", requestId, performedBy);

            ImportResponseDTO response = importSampleCollection(importRequest);

            log.info("RequestEntity ID: {}, CollectionEntity imported successfully: {}", requestId, response.getCollectionId());

            // Clear collections list cache
            clearUserCollectionsCache(performedBy);

            return response;

        } catch (Exception e) {
            String errorMsg = "Error importing collectionEntity: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
            return new ImportResponseDTO("", "Error importing collectionEntity: " + e.getMessage());
        }
    }

    public void clearCollectionsCache(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("RequestEntity ID: {}, Clearing collections cache for user: {}", requestId, performedBy);

            int beforeSize = collectionsCache.size();

            // Clear all cache entries for this user
            collectionsCache.keySet().removeIf(key -> key.contains(performedBy));

            int afterSize = collectionsCache.size();

            log.info("RequestEntity ID: {}, Cleared {} collections cache entries", requestId, beforeSize - afterSize);
            loggerUtil.log("collections",
                    "RequestEntity ID: " + requestId + ", Cleared collections cache for user: " + performedBy);

        } catch (Exception e) {
            String errorMsg = "Error clearing collections cache: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
        }
    }

    // ========== STATIC DATA GENERATORS ==========

    private CollectionsListResponseDTO generateStaticCollectionsList(String userId) {
        List<CollectionDTO> collections = new ArrayList<>();

        // First collectionEntity - E-Commerce API (from React component)
        CollectionDTO ecommerceCollection = new CollectionDTO();
        ecommerceCollection.setId("col-1");
        ecommerceCollection.setName("E-Commerce API");
        ecommerceCollection.setDescription("Complete e-commerce platform endpoints");
        ecommerceCollection.setExpanded(true);
        ecommerceCollection.setFavorite(true);
        ecommerceCollection.setEditing(false);
        ecommerceCollection.setCreatedAt("2024-01-15T10:30:00Z");
        ecommerceCollection.setRequestsCount(12);

        List<VariableDTO> ecommerceVariables = new ArrayList<>();
        ecommerceVariables.add(createVariable("var-1", "baseUrl", "{{base_url}}", "string", true));
        ecommerceCollection.setVariables(ecommerceVariables);

        // Second collectionEntity - Social Media API (from React component)
        CollectionDTO socialMediaCollection = new CollectionDTO();
        socialMediaCollection.setId("col-2");
        socialMediaCollection.setName("Social Media API");
        socialMediaCollection.setDescription("Social media platform endpoints");
        socialMediaCollection.setExpanded(false);
        socialMediaCollection.setFavorite(false);
        socialMediaCollection.setEditing(false);
        socialMediaCollection.setCreatedAt("2024-01-10T14:20:00Z");
        socialMediaCollection.setRequestsCount(8);

        List<VariableDTO> socialMediaVariables = new ArrayList<>();
        socialMediaVariables.add(createVariable("var-2", "apiUrl", "{{api_url}}", "string", true));
        socialMediaCollection.setVariables(socialMediaVariables);

        // Set other properties
        ecommerceCollection.setOwner(userId);
        socialMediaCollection.setOwner(userId);
        ecommerceCollection.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        socialMediaCollection.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        ecommerceCollection.setTags(Arrays.asList("ecommerce", "api", "rest", "favorite"));
        socialMediaCollection.setTags(Arrays.asList("social", "api", "rest"));
        ecommerceCollection.setColor("blue");
        socialMediaCollection.setColor("green");

        collections.add(ecommerceCollection);
        collections.add(socialMediaCollection);

        return new CollectionsListResponseDTO(collections, collections.size());
    }

    private CollectionDetailsResponseDTO generateStaticCollectionDetails(String collectionId) {
        if ("col-1".equals(collectionId)) {
            return generateEcommerceCollectionDetails();
        } else if ("col-2".equals(collectionId)) {
            return generateSocialMediaCollectionDetails();
        }
        return generateEcommerceCollectionDetails(); // Default to first collectionEntity
    }

    private CollectionDetailsResponseDTO generateEcommerceCollectionDetails() {
        CollectionDetailsResponseDTO details = new CollectionDetailsResponseDTO();
        details.setCollectionId("col-1");
        details.setName("E-Commerce API");
        details.setDescription("Complete e-commerce platform endpoints");
        details.setCreatedAt("2024-01-15T10:30:00Z");
        details.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setTotalRequests(12);
        details.setTotalFolders(2);
        details.setFavorite(true);
        details.setOwner("admin");

        List<VariableDTO> variables = new ArrayList<>();
        variables.add(createVariable("var-1", "baseUrl", "{{base_url}}", "string", true));
        details.setVariables(variables);

        // Generate folderEntities with requestEntities
        List<FolderDTO> folders = new ArrayList<>();

        // Authentication folderEntity
        FolderDTO authFolder = new FolderDTO();
        authFolder.setId("folderEntity-1");
        authFolder.setName("Authentication");
        authFolder.setDescription("User authentication and authorization");
        authFolder.setExpanded(true);
        authFolder.setEditing(false);
        authFolder.setRequestCount(2);

        List<RequestDTO> authRequests = new ArrayList<>();

        // Login requestEntity
        RequestDTO loginRequest = new RequestDTO();
        loginRequest.setId("req-1");
        loginRequest.setName("Login");
        loginRequest.setMethod("POST");
        loginRequest.setUrl("http://com.example.com/api/v1/auth/login");
        loginRequest.setDescription("Authenticate user with email and password");
        loginRequest.setEditing(false);
        loginRequest.setStatus("saved");
        loginRequest.setLastModified("2024-01-15T09:45:00Z");

        AuthConfigDTO noAuth = new AuthConfigDTO();
        noAuth.setType("noauth");
        loginRequest.setAuth(noAuth);

        List<ParameterDTO> loginParams = new ArrayList<>();
        loginParams.add(createParameter("p-1", "test_param", "test_value", "Test parameter", true));
        loginRequest.setParams(loginParams);

        List<HeaderDTO> loginHeaders = new ArrayList<>();
        loginHeaders.add(createHeader("h-1", "Content-Type", "application/json", true, ""));
        loginRequest.setHeaders(loginHeaders);

        loginRequest.setBody("{\n  \"email\": \"user@example.com\",\n  \"password\": \"password123\"\n}");
        loginRequest.setTests("pm.test('Status code is 200', function() {\n  pm.response.to.have.status(200);\n});");
        loginRequest.setPreRequestScript("");
        loginRequest.setSaved(true);
        loginRequest.setCollectionId("col-1");
        loginRequest.setFolderId("folderEntity-1");
        authRequests.add(loginRequest);

        // Refresh Token requestEntity
        RequestDTO refreshRequest = new RequestDTO();
        refreshRequest.setId("req-2");
        refreshRequest.setName("Refresh Token");
        refreshRequest.setMethod("POST");
        refreshRequest.setUrl("http://com.example.com/api/v1/auth/refresh");
        refreshRequest.setDescription("Refresh access token");
        refreshRequest.setEditing(false);
        refreshRequest.setStatus("saved");
        refreshRequest.setLastModified("2024-01-14T14:20:00Z");

        AuthConfigDTO bearerAuth = new AuthConfigDTO();
        bearerAuth.setType("bearer");
        bearerAuth.setToken("{{access_token}}");
        refreshRequest.setAuth(bearerAuth);

        refreshRequest.setParams(new ArrayList<>());

        List<HeaderDTO> refreshHeaders = new ArrayList<>();
        refreshHeaders.add(createHeader("h-2", "Content-Type", "application/json", true, ""));
        refreshRequest.setHeaders(refreshHeaders);

        refreshRequest.setBody("{\n  \"refresh_token\": \"{{refresh_token}}\"\n}");
        refreshRequest.setTests("");
        refreshRequest.setPreRequestScript("");
        refreshRequest.setSaved(true);
        refreshRequest.setCollectionId("col-1");
        refreshRequest.setFolderId("folderEntity-1");
        authRequests.add(refreshRequest);

        authFolder.setRequests(authRequests);
        folders.add(authFolder);

        // Products folderEntity
        FolderDTO productsFolder = new FolderDTO();
        productsFolder.setId("folderEntity-2");
        productsFolder.setName("Products");
        productsFolder.setDescription("Product management endpoints");
        productsFolder.setExpanded(true);
        productsFolder.setEditing(false);
        productsFolder.setRequestCount(2);

        List<RequestDTO> productRequests = new ArrayList<>();

        // Get Products requestEntity
        RequestDTO getProductsRequest = new RequestDTO();
        getProductsRequest.setId("req-3");
        getProductsRequest.setName("Get Products");
        getProductsRequest.setMethod("GET");
        getProductsRequest.setUrl("http://com.example.com/api/v1/products");
        getProductsRequest.setDescription("Retrieve list of products");
        getProductsRequest.setEditing(false);
        getProductsRequest.setStatus("saved");
        getProductsRequest.setLastModified("2024-01-15T08:15:00Z");

        AuthConfigDTO bearerAuth2 = new AuthConfigDTO();
        bearerAuth2.setType("bearer");
        bearerAuth2.setToken("{{access_token}}");
        getProductsRequest.setAuth(bearerAuth2);

        List<ParameterDTO> productParams = new ArrayList<>();
        productParams.add(createParameter("p-1", "page", "1", "Page number", true));
        productParams.add(createParameter("p-2", "limit", "20", "Items per page", true));
        productParams.add(createParameter("p-3", "category", "", "Filter by category", false));
        getProductsRequest.setParams(productParams);

        List<HeaderDTO> productHeaders = new ArrayList<>();
        productHeaders.add(createHeader("h-3", "Authorization", "Bearer {{access_token}}", true, ""));
        getProductsRequest.setHeaders(productHeaders);

        getProductsRequest.setBody("");
        getProductsRequest.setTests("pm.test('Status code is 200', function() {\n  pm.response.to.have.status(200);\n});");
        getProductsRequest.setPreRequestScript("");
        getProductsRequest.setSaved(true);
        getProductsRequest.setCollectionId("col-1");
        getProductsRequest.setFolderId("folderEntity-2");
        productRequests.add(getProductsRequest);

        // Create Product requestEntity
        RequestDTO createProductRequest = new RequestDTO();
        createProductRequest.setId("req-4");
        createProductRequest.setName("Create Product");
        createProductRequest.setMethod("POST");
        createProductRequest.setUrl("http://com.example.com/api/v1/products");
        createProductRequest.setDescription("Create a new product");
        createProductRequest.setEditing(false);
        createProductRequest.setStatus("saved");
        createProductRequest.setLastModified("2024-01-14T16:45:00Z");

        AuthConfigDTO bearerAuth3 = new AuthConfigDTO();
        bearerAuth3.setType("bearer");
        bearerAuth3.setToken("{{access_token}}");
        createProductRequest.setAuth(bearerAuth3);

        createProductRequest.setParams(new ArrayList<>());

        List<HeaderDTO> createProductHeaders = new ArrayList<>();
        createProductHeaders.add(createHeader("h-4", "Authorization", "Bearer {{access_token}}", true, ""));
        createProductHeaders.add(createHeader("h-5", "Content-Type", "application/json", true, ""));
        createProductRequest.setHeaders(createProductHeaders);

        createProductRequest.setBody("{\n  \"name\": \"New Product\",\n  \"price\": 99.99,\n  \"category\": \"electronics\"\n}");
        createProductRequest.setTests("pm.test('Status code is 201', function() {\n  pm.response.to.have.status(201);\n});");
        createProductRequest.setPreRequestScript("");
        createProductRequest.setSaved(true);
        createProductRequest.setCollectionId("col-1");
        createProductRequest.setFolderId("folderEntity-2");
        productRequests.add(createProductRequest);

        productsFolder.setRequests(productRequests);
        folders.add(productsFolder);

        details.setFolders(folders);
        details.setComments("Sample e-commerce API collectionEntity for testing");
        details.setLastActivity(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return details;
    }

    private CollectionDetailsResponseDTO generateSocialMediaCollectionDetails() {
        CollectionDetailsResponseDTO details = new CollectionDetailsResponseDTO();
        details.setCollectionId("col-2");
        details.setName("Social Media API");
        details.setDescription("Social media platform endpoints");
        details.setCreatedAt("2024-01-10T14:20:00Z");
        details.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setTotalRequests(8);
        details.setTotalFolders(1);
        details.setFavorite(false);
        details.setOwner("admin");

        List<VariableDTO> variables = new ArrayList<>();
        variables.add(createVariable("var-2", "apiUrl", "{{api_url}}", "string", true));
        details.setVariables(variables);

        // Generate folderEntities with requestEntities
        List<FolderDTO> folders = new ArrayList<>();

        // Posts folderEntity
        FolderDTO postsFolder = new FolderDTO();
        postsFolder.setId("folderEntity-3");
        postsFolder.setName("Posts");
        postsFolder.setDescription("Post management endpoints");
        postsFolder.setExpanded(false);
        postsFolder.setEditing(false);
        postsFolder.setRequestCount(1);

        List<RequestDTO> postRequests = new ArrayList<>();

        // Create Post requestEntity
        RequestDTO createPostRequest = new RequestDTO();
        createPostRequest.setId("req-5");
        createPostRequest.setName("Create Post");
        createPostRequest.setMethod("POST");
        createPostRequest.setUrl("{{apiUrl}}/api/v1/posts");
        createPostRequest.setDescription("Create a new post");
        createPostRequest.setEditing(false);
        createPostRequest.setStatus("saved");
        createPostRequest.setLastModified("2024-01-12T11:30:00Z");

        AuthConfigDTO bearerAuth = new AuthConfigDTO();
        bearerAuth.setType("bearer");
        bearerAuth.setToken("{{access_token}}");
        createPostRequest.setAuth(bearerAuth);

        createPostRequest.setParams(new ArrayList<>());

        List<HeaderDTO> postHeaders = new ArrayList<>();
        postHeaders.add(createHeader("h-6", "Content-Type", "application/json", true, ""));
        createPostRequest.setHeaders(postHeaders);

        createPostRequest.setBody("{\n  \"content\": \"Hello world!\",\n  \"media_urls\": [\"https://example.com/image.jpg\"]\n}");
        createPostRequest.setTests("pm.test('Status code is 201', function() {\n  pm.response.to.have.status(201);\n});");
        createPostRequest.setPreRequestScript("");
        createPostRequest.setSaved(true);
        createPostRequest.setCollectionId("col-2");
        createPostRequest.setFolderId("folderEntity-3");
        postRequests.add(createPostRequest);

        postsFolder.setRequests(postRequests);
        folders.add(postsFolder);

        details.setFolders(folders);
        details.setComments("Sample social media API collectionEntity");
        details.setLastActivity(LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return details;
    }

    private RequestDetailsResponseDTO generateStaticRequestDetails(String collectionId, String requestId) {
        // Based on the requestId, return the appropriate requestEntity details
        switch (requestId) {
            case "req-1":
                return getLoginRequestDetails();
            case "req-2":
                return getRefreshTokenRequestDetails();
            case "req-3":
                return getProductsRequestDetails();
            case "req-4":
                return getCreateProductRequestDetails();
            case "req-5":
                return getCreatePostRequestDetails();
            default:
                return getLoginRequestDetails(); // Default to login requestEntity
        }
    }

    private RequestDetailsResponseDTO getLoginRequestDetails() {
        RequestDetailsResponseDTO details = new RequestDetailsResponseDTO();
        details.setRequestId("req-1");
        details.setName("Login");
        details.setMethod("POST");
        details.setUrl("http://com.example.com/api/v1/auth/login");
        details.setDescription("Authenticate user with email and password");
        details.setAuthType("noauth");

        // Headers
        List<HeaderDTO> headers = new ArrayList<>();
        headers.add(createHeader("h-1", "Content-Type", "application/json", true, ""));
        details.setHeaders(headers);

        // Parameters
        List<ParameterDTO> parameters = new ArrayList<>();
        parameters.add(createParameter("p-1", "test_param", "test_value", "Test parameter", true));
        details.setParameters(parameters);

        // Body
        BodyDTO body = new BodyDTO();
        body.setType("raw");
        body.setRawType("json");
        body.setContent("{\n  \"email\": \"user@example.com\",\n  \"password\": \"password123\"\n}");
        details.setBody(body);

        // Auth config
        AuthConfigDTO authConfig = new AuthConfigDTO();
        authConfig.setType("noauth");
        details.setAuthConfig(authConfig);

        details.setPreRequestScript("");
        details.setTests("pm.test('Status code is 200', function() {\n  pm.response.to.have.status(200);\n});");
        details.setCreatedAt("2024-01-15T09:45:00Z");
        details.setUpdatedAt("2024-01-15T09:45:00Z");
        details.setCollectionId("col-1");
        details.setFolderId("folderEntity-1");
        details.setSaved(true);

        return details;
    }

    private RequestDetailsResponseDTO getRefreshTokenRequestDetails() {
        RequestDetailsResponseDTO details = new RequestDetailsResponseDTO();
        details.setRequestId("req-2");
        details.setName("Refresh Token");
        details.setMethod("POST");
        details.setUrl("http://com.example.com/api/v1/auth/refresh");
        details.setDescription("Refresh access token");
        details.setAuthType("bearer");

        // Headers
        List<HeaderDTO> headers = new ArrayList<>();
        headers.add(createHeader("h-2", "Content-Type", "application/json", true, ""));
        details.setHeaders(headers);

        details.setParameters(new ArrayList<>());

        // Body
        BodyDTO body = new BodyDTO();
        body.setType("raw");
        body.setRawType("json");
        body.setContent("{\n  \"refresh_token\": \"{{refresh_token}}\"\n}");
        details.setBody(body);

        // Auth config
        AuthConfigDTO authConfig = new AuthConfigDTO();
        authConfig.setType("bearer");
        authConfig.setToken("{{access_token}}");
        details.setAuthConfig(authConfig);

        details.setPreRequestScript("");
        details.setTests("");
        details.setCreatedAt("2024-01-14T14:20:00Z");
        details.setUpdatedAt("2024-01-14T14:20:00Z");
        details.setCollectionId("col-1");
        details.setFolderId("folderEntity-1");
        details.setSaved(true);

        return details;
    }

    private RequestDetailsResponseDTO getProductsRequestDetails() {
        RequestDetailsResponseDTO details = new RequestDetailsResponseDTO();
        details.setRequestId("req-3");
        details.setName("Get Products");
        details.setMethod("GET");
        details.setUrl("http://com.example.com/api/v1/products");
        details.setDescription("Retrieve list of products");
        details.setAuthType("bearer");

        // Headers
        List<HeaderDTO> headers = new ArrayList<>();
        headers.add(createHeader("h-3", "Authorization", "Bearer {{access_token}}", true, ""));
        details.setHeaders(headers);

        // Parameters
        List<ParameterDTO> parameters = new ArrayList<>();
        parameters.add(createParameter("p-1", "page", "1", "Page number", true));
        parameters.add(createParameter("p-2", "limit", "20", "Items per page", true));
        parameters.add(createParameter("p-3", "category", "", "Filter by category", false));
        details.setParameters(parameters);

        // Body
        BodyDTO body = new BodyDTO();
        body.setType("none");
        details.setBody(body);

        // Auth config
        AuthConfigDTO authConfig = new AuthConfigDTO();
        authConfig.setType("bearer");
        authConfig.setToken("{{access_token}}");
        details.setAuthConfig(authConfig);

        details.setPreRequestScript("");
        details.setTests("pm.test('Status code is 200', function() {\n  pm.response.to.have.status(200);\n});");
        details.setCreatedAt("2024-01-15T08:15:00Z");
        details.setUpdatedAt("2024-01-15T08:15:00Z");
        details.setCollectionId("col-1");
        details.setFolderId("folderEntity-2");
        details.setSaved(true);

        return details;
    }

    private RequestDetailsResponseDTO getCreateProductRequestDetails() {
        RequestDetailsResponseDTO details = new RequestDetailsResponseDTO();
        details.setRequestId("req-4");
        details.setName("Create Product");
        details.setMethod("POST");
        details.setUrl("http://com.example.com/api/v1/products");
        details.setDescription("Create a new product");
        details.setAuthType("bearer");

        // Headers
        List<HeaderDTO> headers = new ArrayList<>();
        headers.add(createHeader("h-4", "Authorization", "Bearer {{access_token}}", true, ""));
        headers.add(createHeader("h-5", "Content-Type", "application/json", true, ""));
        details.setHeaders(headers);

        details.setParameters(new ArrayList<>());

        // Body
        BodyDTO body = new BodyDTO();
        body.setType("raw");
        body.setRawType("json");
        body.setContent("{\n  \"name\": \"New Product\",\n  \"price\": 99.99,\n  \"category\": \"electronics\"\n}");
        details.setBody(body);

        // Auth config
        AuthConfigDTO authConfig = new AuthConfigDTO();
        authConfig.setType("bearer");
        authConfig.setToken("{{access_token}}");
        details.setAuthConfig(authConfig);

        details.setPreRequestScript("");
        details.setTests("pm.test('Status code is 201', function() {\n  pm.response.to.have.status(201);\n});");
        details.setCreatedAt("2024-01-14T16:45:00Z");
        details.setUpdatedAt("2024-01-14T16:45:00Z");
        details.setCollectionId("col-1");
        details.setFolderId("folderEntity-2");
        details.setSaved(true);

        return details;
    }

    private RequestDetailsResponseDTO getCreatePostRequestDetails() {
        RequestDetailsResponseDTO details = new RequestDetailsResponseDTO();
        details.setRequestId("req-5");
        details.setName("Create Post");
        details.setMethod("POST");
        details.setUrl("{{apiUrl}}/api/v1/posts");
        details.setDescription("Create a new post");
        details.setAuthType("bearer");

        // Headers
        List<HeaderDTO> headers = new ArrayList<>();
        headers.add(createHeader("h-6", "Content-Type", "application/json", true, ""));
        details.setHeaders(headers);

        details.setParameters(new ArrayList<>());

        // Body
        BodyDTO body = new BodyDTO();
        body.setType("raw");
        body.setRawType("json");
        body.setContent("{\n  \"content\": \"Hello world!\",\n  \"media_urls\": [\"https://example.com/image.jpg\"]\n}");
        details.setBody(body);

        // Auth config
        AuthConfigDTO authConfig = new AuthConfigDTO();
        authConfig.setType("bearer");
        authConfig.setToken("{{access_token}}");
        details.setAuthConfig(authConfig);

        details.setPreRequestScript("");
        details.setTests("pm.test('Status code is 201', function() {\n  pm.response.to.have.status(201);\n});");
        details.setCreatedAt("2024-01-12T11:30:00Z");
        details.setUpdatedAt("2024-01-12T11:30:00Z");
        details.setCollectionId("col-2");
        details.setFolderId("folderEntity-3");
        details.setSaved(true);

        return details;
    }

    private EnvironmentsResponseDTO generateStaticEnvironments() {
        List<EnvironmentDTO> environments = new ArrayList<>();

        EnvironmentDTO noEnv = new EnvironmentDTO();
        noEnv.setId("env-1");
        noEnv.setName("No Environment");
        noEnv.setActive(true);
        noEnv.setVariables(new ArrayList<>());
        environments.add(noEnv);

        EnvironmentDTO devEnv = new EnvironmentDTO();
        devEnv.setId("env-2");
        devEnv.setName("Development");
        devEnv.setActive(false);

        List<VariableDTO> devVariables = new ArrayList<>();
        devVariables.add(createVariable("env-var-1", "base_url", "https://api.dev.example.com", "string", true));
        devVariables.add(createVariable("env-var-2", "access_token", "dev_token_123", "string", true));
        devEnv.setVariables(devVariables);
        environments.add(devEnv);

        EnvironmentDTO prodEnv = new EnvironmentDTO();
        prodEnv.setId("env-3");
        prodEnv.setName("Production");
        prodEnv.setActive(false);

        List<VariableDTO> prodVariables = new ArrayList<>();
        prodVariables.add(createVariable("env-var-3", "base_url", "https://api.example.com", "string", true));
        prodVariables.add(createVariable("env-var-4", "access_token", "prod_token_456", "string", true));
        prodEnv.setVariables(prodVariables);
        environments.add(prodEnv);

        return new EnvironmentsResponseDTO(environments);
    }

    // ========== HELPER METHODS ==========

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

    // ========== OTHER METHODS ==========

    private void preloadCollectionsCache() {
        try {
            log.info("Preloading collections cache with static data");

            // Preload collections list
            CollectionsListResponseDTO collections = generateStaticCollectionsList("admin");
            collectionsCache.put("collections_list_admin", new CollectionsCache(collections, System.currentTimeMillis()));

            // Preload collectionEntity details
            CollectionDetailsResponseDTO ecommerceDetails = generateEcommerceCollectionDetails();
            collectionsCache.put("collection_details_admin_col-1",
                    new CollectionsCache(ecommerceDetails, System.currentTimeMillis()));

            CollectionDetailsResponseDTO socialMediaDetails = generateSocialMediaCollectionDetails();
            collectionsCache.put("collection_details_admin_col-2",
                    new CollectionsCache(socialMediaDetails, System.currentTimeMillis()));

            // Preload requestEntity details
            RequestDetailsResponseDTO loginRequest = getLoginRequestDetails();
            collectionsCache.put("request_details_admin_col-1_req-1",
                    new CollectionsCache(loginRequest, System.currentTimeMillis()));

            log.info("Collections cache preloaded with {} entries", collectionsCache.size());

        } catch (Exception e) {
            log.warn("Failed to preload collections cache: {}", e.getMessage());
        }
    }

    private boolean isCacheExpired(CollectionsCache cache) {
        return (System.currentTimeMillis() - cache.getTimestamp()) > CACHE_TTL_MS;
    }

    private void clearCollectionCache(String performedBy, String collectionId) {
        collectionsCache.keySet().removeIf(key ->
                key.contains(performedBy) && key.contains(collectionId)
        );
    }

    private void clearUserCollectionsCache(String performedBy) {
        collectionsCache.keySet().removeIf(key -> key.contains("collections_list_" + performedBy));
    }

    private ExecuteRequestResponseDTO executeSampleRequest(ExecuteRequestDTO requestDto) {
        try {
            // Generate sample response based on requestEntity
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
                        "  \"message\": \"RequestEntity processed successfully\",\n" +
                        "  \"timestamp\": \"" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\",\n" +
                        "  \"endpoint\": \"" + requestDto.getUrl() + "\",\n" +
                        "  \"method\": \"" + method + "\"\n" +
                        "}";
            }

            long timeMs = (long) (Math.random() * 200) + 100;
            long sizeBytes = responseBody.getBytes().length;

            // Generate headerEntities
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
            return new ExecuteRequestResponseDTO(
                    "",
                    500,
                    "Error executing requestEntity: " + e.getMessage(),
                    Collections.emptyList(),
                    0L,
                    0L
            );
        }
    }

    private SaveRequestResponseDTO saveSampleRequest(SaveRequestDTO requestDto) {
        return new SaveRequestResponseDTO(
                "req-" + System.currentTimeMillis(),
                "RequestEntity saved successfully"
        );
    }

    private CreateCollectionResponseDTO createSampleCollection(CreateCollectionDTO collectionDto) {
        return new CreateCollectionResponseDTO(
                "col-" + System.currentTimeMillis(),
                "CollectionEntity created successfully"
        );
    }

    private ImportResponseDTO importSampleCollection(ImportRequestDTO importRequest) {
        return new ImportResponseDTO(
                "col-import-" + System.currentTimeMillis(),
                "CollectionEntity imported successfully from " + importRequest.getSource()
        );
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

    // Code snippet generators (same as before but using the static data)
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
            js.append("  headerEntities: {\n");
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
            js.append("  body: ").append(request.getBody()).append("\n");
        }

        js.append("})\n");
        js.append(".then(response => response.json())\n");
        js.append(".then(data => console.log(data))\n");
        js.append(".catch(error => console.error('Error:', error));");

        return js.toString();
    }

    private String generatePythonSnippet(CodeSnippetRequestDTO request) {
        StringBuilder python = new StringBuilder();
        python.append("import requestEntities\n\n");

        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            python.append("headerEntities = {\n");
            for (HeaderDTO header : request.getHeaders()) {
                if (header.isEnabled()) {
                    python.append("    \"").append(header.getKey()).append("\": \"").append(header.getValue()).append("\",\n");
                }
            }
            python.append("}\n\n");
        } else {
            python.append("headerEntities = {}\n\n");
        }

        if (request.getBody() != null && !request.getBody().trim().isEmpty() &&
                !request.getMethod().equals("GET")) {
            python.append("data = ").append(request.getBody()).append("\n\n");
            python.append("response = requestEntities.").append(request.getMethod().toLowerCase());
            python.append("(\"").append(request.getUrl()).append("\", json=data, headerEntities=headerEntities)\n");
        } else {
            python.append("response = requestEntities.").append(request.getMethod().toLowerCase());
            python.append("(\"").append(request.getUrl()).append("\", headerEntities=headerEntities)\n");
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
        node.append("  hostname: 'api.example.com',\n");
        node.append("  port: 443,\n");
        node.append("  path: '/api/v1/endpoint',\n");
        node.append("  method: '").append(request.getMethod()).append("',\n");

        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            node.append("  headerEntities: {\n");
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

        node.append("const req = https.requestEntity(options, (res) => {\n");
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
            node.append("req.write(").append(request.getBody()).append(");\n");
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
            php.append("$headerEntities = [\n");
            for (HeaderDTO header : request.getHeaders()) {
                if (header.isEnabled()) {
                    php.append("    \"").append(header.getKey()).append(": ").append(header.getValue()).append("\",\n");
                }
            }
            php.append("];\n");
            php.append("curl_setopt($ch, CURLOPT_HTTPHEADER, $headerEntities);\n\n");
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

        ruby.append("requestEntity = Net::HTTP::").append(request.getMethod().charAt(0) + request.getMethod().substring(1).toLowerCase());
        ruby.append(".new(uri.request_uri)\n\n");

        if (request.getHeaders() != null) {
            for (HeaderDTO header : request.getHeaders()) {
                if (header.isEnabled()) {
                    ruby.append("requestEntity[\"").append(header.getKey()).append("\"] = \"").append(header.getValue()).append("\"\n");
                }
            }
        }

        if (request.getBody() != null && !request.getBody().trim().isEmpty() &&
                !request.getMethod().equals("GET")) {
            ruby.append("requestEntity.body = ").append(request.getBody()).append(".to_json\n\n");
        }

        ruby.append("response = http.requestEntity(requestEntity)\n");
        ruby.append("puts response.body");

        return ruby.toString();
    }

    // ========== FALLBACK METHODS ==========

    private CollectionsListResponseDTO getFallbackCollectionsList() {
        List<CollectionDTO> collections = new ArrayList<>();

        CollectionDTO collection = new CollectionDTO();
        collection.setId("col-1");
        collection.setName("E-Commerce API");
        collection.setDescription("Fallback collectionEntity");
        collection.setOwner("admin");
        collection.setFavorite(true);
        collection.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        collection.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        collection.setRequestsCount(5);
        collection.setFolderCount(2);
        collection.setTags(Arrays.asList("api", "fallback"));

        collections.add(collection);

        return new CollectionsListResponseDTO(collections, 1);
    }

    private CollectionDetailsResponseDTO getFallbackCollectionDetails(String collectionId) {
        CollectionDetailsResponseDTO details = new CollectionDetailsResponseDTO();
        details.setCollectionId(collectionId);
        details.setName("Fallback CollectionEntity");
        details.setDescription("Fallback collectionEntity details");
        details.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setTotalRequests(3);
        details.setTotalFolders(1);
        details.setFavorite(false);
        details.setOwner("admin");

        List<FolderDTO> folders = new ArrayList<>();
        FolderDTO folder = new FolderDTO();
        folder.setId("folderEntity-1");
        folder.setName("Fallback FolderEntity");
        folder.setDescription("Fallback folderEntity");
        folder.setRequestCount(2);
        folder.setExpanded(true);
        folders.add(folder);

        details.setFolders(folders);

        return details;
    }

    private RequestDetailsResponseDTO getFallbackRequestDetails(String requestId) {
        RequestDetailsResponseDTO details = new RequestDetailsResponseDTO();
        details.setRequestId(requestId);
        details.setName("Fallback RequestEntity");
        details.setMethod("GET");
        details.setUrl("{{base_url}}/api/v1/fallback");
        details.setDescription("Fallback requestEntity details");
        details.setAuthType("noauth");
        details.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setCollectionId("col-1");
        details.setFolderId("folderEntity-1");
        details.setSaved(true);

        return details;
    }

    // ========== INNER CLASSES ==========

    private static class CollectionsCache {
        private final Object data;
        private final long timestamp;

        public CollectionsCache(Object data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }

        public Object getData() {
            return data;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}