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

    public CollectionsListResponse getCollectionsList(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Getting collections list for user: {}", requestId, performedBy);
            loggerUtil.log("collections",
                    "Request ID: " + requestId + ", Getting collections list for user: " + performedBy);

            // Check cache first
            String cacheKey = "collections_list_" + performedBy;
            CollectionsCache cachedData = collectionsCache.get(cacheKey);

            if (cachedData != null && !isCacheExpired(cachedData)) {
                log.debug("Request ID: {}, Returning cached collections list", requestId);
                return (CollectionsListResponse) cachedData.getData();
            }

            CollectionsListResponse collections = generateStaticCollectionsList(performedBy);

            // Update cache
            collectionsCache.put(cacheKey, new CollectionsCache(collections, System.currentTimeMillis()));

            log.info("Request ID: {}, Retrieved {} collections", requestId, collections.getCollections().size());

            return collections;

        } catch (Exception e) {
            String errorMsg = "Error retrieving collections list: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return getFallbackCollectionsList();
        }
    }

    public CollectionDetailsResponse getCollectionDetails(String requestId, HttpServletRequest req, String performedBy,
                                                          String collectionId) {
        try {
            log.info("Request ID: {}, Getting collection details for: {}", requestId, collectionId);

            // Check cache first
            String cacheKey = "collection_details_" + performedBy + "_" + collectionId;
            CollectionsCache cachedData = collectionsCache.get(cacheKey);

            if (cachedData != null && !isCacheExpired(cachedData)) {
                log.debug("Request ID: {}, Returning cached collection details", requestId);
                return (CollectionDetailsResponse) cachedData.getData();
            }

            CollectionDetailsResponse details = generateStaticCollectionDetails(collectionId);

            // Update cache
            collectionsCache.put(cacheKey, new CollectionsCache(details, System.currentTimeMillis()));

            log.info("Request ID: {}, Retrieved details for collection: {}", requestId, collectionId);

            return details;

        } catch (Exception e) {
            String errorMsg = "Error retrieving collection details: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return getFallbackCollectionDetails(collectionId);
        }
    }

    public RequestDetailsResponse getRequestDetails(String requestId, HttpServletRequest req, String performedBy,
                                                    String collectionId, String requestIdParam) {
        try {
            log.info("Request ID: {}, Getting request details for: {}", requestId, requestIdParam);

            // Check cache first
            String cacheKey = "request_details_" + performedBy + "_" + collectionId + "_" + requestIdParam;
            CollectionsCache cachedData = collectionsCache.get(cacheKey);

            if (cachedData != null && !isCacheExpired(cachedData)) {
                log.debug("Request ID: {}, Returning cached request details", requestId);
                return (RequestDetailsResponse) cachedData.getData();
            }

            RequestDetailsResponse details = generateStaticRequestDetails(collectionId, requestIdParam);

            // Update cache
            collectionsCache.put(cacheKey, new CollectionsCache(details, System.currentTimeMillis()));

            log.info("Request ID: {}, Retrieved details for request: {}", requestId, requestIdParam);

            return details;

        } catch (Exception e) {
            String errorMsg = "Error retrieving request details: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return getFallbackRequestDetails(requestIdParam);
        }
    }

    public ExecuteRequestResponse executeRequest(String requestId, HttpServletRequest req, String performedBy,
                                                 ExecuteRequestDto requestDto) {
        try {
            log.info("Request ID: {}, Executing request for user: {}", requestId, performedBy);
            loggerUtil.log("collections",
                    "Request ID: " + requestId + ", Executing request: " + requestDto.getMethod() + " " + requestDto.getUrl());

            ExecuteRequestResponse response = executeSampleRequest(requestDto);

            log.info("Request ID: {}, Request executed successfully, status: {}",
                    requestId, response.getStatusCode());

            return response;

        } catch (Exception e) {
            String errorMsg = "Error executing request: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return new ExecuteRequestResponse(
                    "",
                    500,
                    "Error executing request: " + e.getMessage(),
                    Collections.emptyList(),
                    0L,
                    0L
            );
        }
    }

    public SaveRequestResponse saveRequest(String requestId, HttpServletRequest req, String performedBy,
                                           SaveRequestDto requestDto) {
        try {
            log.info("Request ID: {}, Saving request for user: {}", requestId, performedBy);

            SaveRequestResponse response = saveSampleRequest(requestDto);

            log.info("Request ID: {}, Request saved successfully: {}", requestId, response.getRequestId());

            // Clear relevant cache
            clearCollectionCache(performedBy, requestDto.getCollectionId());

            return response;

        } catch (Exception e) {
            String errorMsg = "Error saving request: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return new SaveRequestResponse("", "Error saving request: " + e.getMessage());
        }
    }

    public CreateCollectionResponse createCollection(String requestId, HttpServletRequest req, String performedBy,
                                                     CreateCollectionDto collectionDto) {
        try {
            log.info("Request ID: {}, Creating collection for user: {}", requestId, performedBy);

            CreateCollectionResponse response = createSampleCollection(collectionDto);

            log.info("Request ID: {}, Collection created successfully: {}", requestId, response.getCollectionId());

            // Clear collections list cache
            clearUserCollectionsCache(performedBy);

            return response;

        } catch (Exception e) {
            String errorMsg = "Error creating collection: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return new CreateCollectionResponse("", "Error creating collection: " + e.getMessage());
        }
    }

    public CodeSnippetResponse generateCodeSnippet(String requestId, HttpServletRequest req, String performedBy,
                                                   CodeSnippetRequestDto snippetRequest) {
        try {
            log.info("Request ID: {}, Generating code snippet for language: {}",
                    requestId, snippetRequest.getLanguage());

            CodeSnippetResponse snippet = generateSampleCodeSnippet(snippetRequest);

            log.info("Request ID: {}, Generated code snippet for {}", requestId, snippetRequest.getLanguage());

            return snippet;

        } catch (Exception e) {
            String errorMsg = "Error generating code snippet: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return new CodeSnippetResponse("", snippetRequest.getLanguage(),
                    "Error generating code snippet: " + e.getMessage());
        }
    }

    public EnvironmentsResponse getEnvironments(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Getting environments for user: {}", requestId, performedBy);

            EnvironmentsResponse environments = generateStaticEnvironments();

            log.info("Request ID: {}, Retrieved {} environments", requestId, environments.getEnvironments().size());

            return environments;

        } catch (Exception e) {
            String errorMsg = "Error retrieving environments: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return new EnvironmentsResponse(Collections.emptyList());
        }
    }

    public ImportResponse importCollection(String requestId, HttpServletRequest req, String performedBy,
                                           ImportRequestDto importRequest) {
        try {
            log.info("Request ID: {}, Importing collection for user: {}", requestId, performedBy);

            ImportResponse response = importSampleCollection(importRequest);

            log.info("Request ID: {}, Collection imported successfully: {}", requestId, response.getCollectionId());

            // Clear collections list cache
            clearUserCollectionsCache(performedBy);

            return response;

        } catch (Exception e) {
            String errorMsg = "Error importing collection: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return new ImportResponse("", "Error importing collection: " + e.getMessage());
        }
    }

    public void clearCollectionsCache(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Clearing collections cache for user: {}", requestId, performedBy);

            int beforeSize = collectionsCache.size();

            // Clear all cache entries for this user
            collectionsCache.keySet().removeIf(key -> key.contains(performedBy));

            int afterSize = collectionsCache.size();

            log.info("Request ID: {}, Cleared {} collections cache entries", requestId, beforeSize - afterSize);
            loggerUtil.log("collections",
                    "Request ID: " + requestId + ", Cleared collections cache for user: " + performedBy);

        } catch (Exception e) {
            String errorMsg = "Error clearing collections cache: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
        }
    }

    // ========== STATIC DATA GENERATORS ==========

    private CollectionsListResponse generateStaticCollectionsList(String userId) {
        List<CollectionDto> collections = new ArrayList<>();

        // First collection - E-Commerce API (from React component)
        CollectionDto ecommerceCollection = new CollectionDto();
        ecommerceCollection.setId("col-1");
        ecommerceCollection.setName("E-Commerce API");
        ecommerceCollection.setDescription("Complete e-commerce platform endpoints");
        ecommerceCollection.setExpanded(true);
        ecommerceCollection.setFavorite(true);
        ecommerceCollection.setEditing(false);
        ecommerceCollection.setCreatedAt("2024-01-15T10:30:00Z");
        ecommerceCollection.setRequestsCount(12);

        List<VariableDto> ecommerceVariables = new ArrayList<>();
        ecommerceVariables.add(createVariable("var-1", "baseUrl", "{{base_url}}", "string", true));
        ecommerceCollection.setVariables(ecommerceVariables);

        // Second collection - Social Media API (from React component)
        CollectionDto socialMediaCollection = new CollectionDto();
        socialMediaCollection.setId("col-2");
        socialMediaCollection.setName("Social Media API");
        socialMediaCollection.setDescription("Social media platform endpoints");
        socialMediaCollection.setExpanded(false);
        socialMediaCollection.setFavorite(false);
        socialMediaCollection.setEditing(false);
        socialMediaCollection.setCreatedAt("2024-01-10T14:20:00Z");
        socialMediaCollection.setRequestsCount(8);

        List<VariableDto> socialMediaVariables = new ArrayList<>();
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

        return new CollectionsListResponse(collections, collections.size());
    }

    private CollectionDetailsResponse generateStaticCollectionDetails(String collectionId) {
        if ("col-1".equals(collectionId)) {
            return generateEcommerceCollectionDetails();
        } else if ("col-2".equals(collectionId)) {
            return generateSocialMediaCollectionDetails();
        }
        return generateEcommerceCollectionDetails(); // Default to first collection
    }

    private CollectionDetailsResponse generateEcommerceCollectionDetails() {
        CollectionDetailsResponse details = new CollectionDetailsResponse();
        details.setCollectionId("col-1");
        details.setName("E-Commerce API");
        details.setDescription("Complete e-commerce platform endpoints");
        details.setCreatedAt("2024-01-15T10:30:00Z");
        details.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setTotalRequests(12);
        details.setTotalFolders(2);
        details.setFavorite(true);
        details.setOwner("admin");

        List<VariableDto> variables = new ArrayList<>();
        variables.add(createVariable("var-1", "baseUrl", "{{base_url}}", "string", true));
        details.setVariables(variables);

        // Generate folders with requests
        List<FolderDto> folders = new ArrayList<>();

        // Authentication folder
        FolderDto authFolder = new FolderDto();
        authFolder.setId("folder-1");
        authFolder.setName("Authentication");
        authFolder.setDescription("User authentication and authorization");
        authFolder.setExpanded(true);
        authFolder.setEditing(false);
        authFolder.setRequestCount(2);

        List<RequestDto> authRequests = new ArrayList<>();

        // Login request
        RequestDto loginRequest = new RequestDto();
        loginRequest.setId("req-1");
        loginRequest.setName("Login");
        loginRequest.setMethod("POST");
        loginRequest.setUrl("http://com.example.com/api/v1/auth/login");
        loginRequest.setDescription("Authenticate user with email and password");
        loginRequest.setEditing(false);
        loginRequest.setStatus("saved");
        loginRequest.setLastModified("2024-01-15T09:45:00Z");

        AuthConfigDto noAuth = new AuthConfigDto();
        noAuth.setType("noauth");
        loginRequest.setAuth(noAuth);

        List<ParameterDto> loginParams = new ArrayList<>();
        loginParams.add(createParameter("p-1", "test_param", "test_value", "Test parameter", true));
        loginRequest.setParams(loginParams);

        List<HeaderDto> loginHeaders = new ArrayList<>();
        loginHeaders.add(createHeader("h-1", "Content-Type", "application/json", true, ""));
        loginRequest.setHeaders(loginHeaders);

        loginRequest.setBody("{\n  \"email\": \"user@example.com\",\n  \"password\": \"password123\"\n}");
        loginRequest.setTests("pm.test('Status code is 200', function() {\n  pm.response.to.have.status(200);\n});");
        loginRequest.setPreRequestScript("");
        loginRequest.setSaved(true);
        loginRequest.setCollectionId("col-1");
        loginRequest.setFolderId("folder-1");
        authRequests.add(loginRequest);

        // Refresh Token request
        RequestDto refreshRequest = new RequestDto();
        refreshRequest.setId("req-2");
        refreshRequest.setName("Refresh Token");
        refreshRequest.setMethod("POST");
        refreshRequest.setUrl("http://com.example.com/api/v1/auth/refresh");
        refreshRequest.setDescription("Refresh access token");
        refreshRequest.setEditing(false);
        refreshRequest.setStatus("saved");
        refreshRequest.setLastModified("2024-01-14T14:20:00Z");

        AuthConfigDto bearerAuth = new AuthConfigDto();
        bearerAuth.setType("bearer");
        bearerAuth.setToken("{{access_token}}");
        refreshRequest.setAuth(bearerAuth);

        refreshRequest.setParams(new ArrayList<>());

        List<HeaderDto> refreshHeaders = new ArrayList<>();
        refreshHeaders.add(createHeader("h-2", "Content-Type", "application/json", true, ""));
        refreshRequest.setHeaders(refreshHeaders);

        refreshRequest.setBody("{\n  \"refresh_token\": \"{{refresh_token}}\"\n}");
        refreshRequest.setTests("");
        refreshRequest.setPreRequestScript("");
        refreshRequest.setSaved(true);
        refreshRequest.setCollectionId("col-1");
        refreshRequest.setFolderId("folder-1");
        authRequests.add(refreshRequest);

        authFolder.setRequests(authRequests);
        folders.add(authFolder);

        // Products folder
        FolderDto productsFolder = new FolderDto();
        productsFolder.setId("folder-2");
        productsFolder.setName("Products");
        productsFolder.setDescription("Product management endpoints");
        productsFolder.setExpanded(true);
        productsFolder.setEditing(false);
        productsFolder.setRequestCount(2);

        List<RequestDto> productRequests = new ArrayList<>();

        // Get Products request
        RequestDto getProductsRequest = new RequestDto();
        getProductsRequest.setId("req-3");
        getProductsRequest.setName("Get Products");
        getProductsRequest.setMethod("GET");
        getProductsRequest.setUrl("http://com.example.com/api/v1/products");
        getProductsRequest.setDescription("Retrieve list of products");
        getProductsRequest.setEditing(false);
        getProductsRequest.setStatus("saved");
        getProductsRequest.setLastModified("2024-01-15T08:15:00Z");

        AuthConfigDto bearerAuth2 = new AuthConfigDto();
        bearerAuth2.setType("bearer");
        bearerAuth2.setToken("{{access_token}}");
        getProductsRequest.setAuth(bearerAuth2);

        List<ParameterDto> productParams = new ArrayList<>();
        productParams.add(createParameter("p-1", "page", "1", "Page number", true));
        productParams.add(createParameter("p-2", "limit", "20", "Items per page", true));
        productParams.add(createParameter("p-3", "category", "", "Filter by category", false));
        getProductsRequest.setParams(productParams);

        List<HeaderDto> productHeaders = new ArrayList<>();
        productHeaders.add(createHeader("h-3", "Authorization", "Bearer {{access_token}}", true, ""));
        getProductsRequest.setHeaders(productHeaders);

        getProductsRequest.setBody("");
        getProductsRequest.setTests("pm.test('Status code is 200', function() {\n  pm.response.to.have.status(200);\n});");
        getProductsRequest.setPreRequestScript("");
        getProductsRequest.setSaved(true);
        getProductsRequest.setCollectionId("col-1");
        getProductsRequest.setFolderId("folder-2");
        productRequests.add(getProductsRequest);

        // Create Product request
        RequestDto createProductRequest = new RequestDto();
        createProductRequest.setId("req-4");
        createProductRequest.setName("Create Product");
        createProductRequest.setMethod("POST");
        createProductRequest.setUrl("http://com.example.com/api/v1/products");
        createProductRequest.setDescription("Create a new product");
        createProductRequest.setEditing(false);
        createProductRequest.setStatus("saved");
        createProductRequest.setLastModified("2024-01-14T16:45:00Z");

        AuthConfigDto bearerAuth3 = new AuthConfigDto();
        bearerAuth3.setType("bearer");
        bearerAuth3.setToken("{{access_token}}");
        createProductRequest.setAuth(bearerAuth3);

        createProductRequest.setParams(new ArrayList<>());

        List<HeaderDto> createProductHeaders = new ArrayList<>();
        createProductHeaders.add(createHeader("h-4", "Authorization", "Bearer {{access_token}}", true, ""));
        createProductHeaders.add(createHeader("h-5", "Content-Type", "application/json", true, ""));
        createProductRequest.setHeaders(createProductHeaders);

        createProductRequest.setBody("{\n  \"name\": \"New Product\",\n  \"price\": 99.99,\n  \"category\": \"electronics\"\n}");
        createProductRequest.setTests("pm.test('Status code is 201', function() {\n  pm.response.to.have.status(201);\n});");
        createProductRequest.setPreRequestScript("");
        createProductRequest.setSaved(true);
        createProductRequest.setCollectionId("col-1");
        createProductRequest.setFolderId("folder-2");
        productRequests.add(createProductRequest);

        productsFolder.setRequests(productRequests);
        folders.add(productsFolder);

        details.setFolders(folders);
        details.setComments("Sample e-commerce API collection for testing");
        details.setLastActivity(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return details;
    }

    private CollectionDetailsResponse generateSocialMediaCollectionDetails() {
        CollectionDetailsResponse details = new CollectionDetailsResponse();
        details.setCollectionId("col-2");
        details.setName("Social Media API");
        details.setDescription("Social media platform endpoints");
        details.setCreatedAt("2024-01-10T14:20:00Z");
        details.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setTotalRequests(8);
        details.setTotalFolders(1);
        details.setFavorite(false);
        details.setOwner("admin");

        List<VariableDto> variables = new ArrayList<>();
        variables.add(createVariable("var-2", "apiUrl", "{{api_url}}", "string", true));
        details.setVariables(variables);

        // Generate folders with requests
        List<FolderDto> folders = new ArrayList<>();

        // Posts folder
        FolderDto postsFolder = new FolderDto();
        postsFolder.setId("folder-3");
        postsFolder.setName("Posts");
        postsFolder.setDescription("Post management endpoints");
        postsFolder.setExpanded(false);
        postsFolder.setEditing(false);
        postsFolder.setRequestCount(1);

        List<RequestDto> postRequests = new ArrayList<>();

        // Create Post request
        RequestDto createPostRequest = new RequestDto();
        createPostRequest.setId("req-5");
        createPostRequest.setName("Create Post");
        createPostRequest.setMethod("POST");
        createPostRequest.setUrl("{{apiUrl}}/api/v1/posts");
        createPostRequest.setDescription("Create a new post");
        createPostRequest.setEditing(false);
        createPostRequest.setStatus("saved");
        createPostRequest.setLastModified("2024-01-12T11:30:00Z");

        AuthConfigDto bearerAuth = new AuthConfigDto();
        bearerAuth.setType("bearer");
        bearerAuth.setToken("{{access_token}}");
        createPostRequest.setAuth(bearerAuth);

        createPostRequest.setParams(new ArrayList<>());

        List<HeaderDto> postHeaders = new ArrayList<>();
        postHeaders.add(createHeader("h-6", "Content-Type", "application/json", true, ""));
        createPostRequest.setHeaders(postHeaders);

        createPostRequest.setBody("{\n  \"content\": \"Hello world!\",\n  \"media_urls\": [\"https://example.com/image.jpg\"]\n}");
        createPostRequest.setTests("pm.test('Status code is 201', function() {\n  pm.response.to.have.status(201);\n});");
        createPostRequest.setPreRequestScript("");
        createPostRequest.setSaved(true);
        createPostRequest.setCollectionId("col-2");
        createPostRequest.setFolderId("folder-3");
        postRequests.add(createPostRequest);

        postsFolder.setRequests(postRequests);
        folders.add(postsFolder);

        details.setFolders(folders);
        details.setComments("Sample social media API collection");
        details.setLastActivity(LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return details;
    }

    private RequestDetailsResponse generateStaticRequestDetails(String collectionId, String requestId) {
        // Based on the requestId, return the appropriate request details
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
                return getLoginRequestDetails(); // Default to login request
        }
    }

    private RequestDetailsResponse getLoginRequestDetails() {
        RequestDetailsResponse details = new RequestDetailsResponse();
        details.setRequestId("req-1");
        details.setName("Login");
        details.setMethod("POST");
        details.setUrl("http://com.example.com/api/v1/auth/login");
        details.setDescription("Authenticate user with email and password");
        details.setAuthType("noauth");

        // Headers
        List<HeaderDto> headers = new ArrayList<>();
        headers.add(createHeader("h-1", "Content-Type", "application/json", true, ""));
        details.setHeaders(headers);

        // Parameters
        List<ParameterDto> parameters = new ArrayList<>();
        parameters.add(createParameter("p-1", "test_param", "test_value", "Test parameter", true));
        details.setParameters(parameters);

        // Body
        BodyDto body = new BodyDto();
        body.setType("raw");
        body.setRawType("json");
        body.setContent("{\n  \"email\": \"user@example.com\",\n  \"password\": \"password123\"\n}");
        details.setBody(body);

        // Auth config
        AuthConfigDto authConfig = new AuthConfigDto();
        authConfig.setType("noauth");
        details.setAuthConfig(authConfig);

        details.setPreRequestScript("");
        details.setTests("pm.test('Status code is 200', function() {\n  pm.response.to.have.status(200);\n});");
        details.setCreatedAt("2024-01-15T09:45:00Z");
        details.setUpdatedAt("2024-01-15T09:45:00Z");
        details.setCollectionId("col-1");
        details.setFolderId("folder-1");
        details.setSaved(true);

        return details;
    }

    private RequestDetailsResponse getRefreshTokenRequestDetails() {
        RequestDetailsResponse details = new RequestDetailsResponse();
        details.setRequestId("req-2");
        details.setName("Refresh Token");
        details.setMethod("POST");
        details.setUrl("http://com.example.com/api/v1/auth/refresh");
        details.setDescription("Refresh access token");
        details.setAuthType("bearer");

        // Headers
        List<HeaderDto> headers = new ArrayList<>();
        headers.add(createHeader("h-2", "Content-Type", "application/json", true, ""));
        details.setHeaders(headers);

        details.setParameters(new ArrayList<>());

        // Body
        BodyDto body = new BodyDto();
        body.setType("raw");
        body.setRawType("json");
        body.setContent("{\n  \"refresh_token\": \"{{refresh_token}}\"\n}");
        details.setBody(body);

        // Auth config
        AuthConfigDto authConfig = new AuthConfigDto();
        authConfig.setType("bearer");
        authConfig.setToken("{{access_token}}");
        details.setAuthConfig(authConfig);

        details.setPreRequestScript("");
        details.setTests("");
        details.setCreatedAt("2024-01-14T14:20:00Z");
        details.setUpdatedAt("2024-01-14T14:20:00Z");
        details.setCollectionId("col-1");
        details.setFolderId("folder-1");
        details.setSaved(true);

        return details;
    }

    private RequestDetailsResponse getProductsRequestDetails() {
        RequestDetailsResponse details = new RequestDetailsResponse();
        details.setRequestId("req-3");
        details.setName("Get Products");
        details.setMethod("GET");
        details.setUrl("http://com.example.com/api/v1/products");
        details.setDescription("Retrieve list of products");
        details.setAuthType("bearer");

        // Headers
        List<HeaderDto> headers = new ArrayList<>();
        headers.add(createHeader("h-3", "Authorization", "Bearer {{access_token}}", true, ""));
        details.setHeaders(headers);

        // Parameters
        List<ParameterDto> parameters = new ArrayList<>();
        parameters.add(createParameter("p-1", "page", "1", "Page number", true));
        parameters.add(createParameter("p-2", "limit", "20", "Items per page", true));
        parameters.add(createParameter("p-3", "category", "", "Filter by category", false));
        details.setParameters(parameters);

        // Body
        BodyDto body = new BodyDto();
        body.setType("none");
        details.setBody(body);

        // Auth config
        AuthConfigDto authConfig = new AuthConfigDto();
        authConfig.setType("bearer");
        authConfig.setToken("{{access_token}}");
        details.setAuthConfig(authConfig);

        details.setPreRequestScript("");
        details.setTests("pm.test('Status code is 200', function() {\n  pm.response.to.have.status(200);\n});");
        details.setCreatedAt("2024-01-15T08:15:00Z");
        details.setUpdatedAt("2024-01-15T08:15:00Z");
        details.setCollectionId("col-1");
        details.setFolderId("folder-2");
        details.setSaved(true);

        return details;
    }

    private RequestDetailsResponse getCreateProductRequestDetails() {
        RequestDetailsResponse details = new RequestDetailsResponse();
        details.setRequestId("req-4");
        details.setName("Create Product");
        details.setMethod("POST");
        details.setUrl("http://com.example.com/api/v1/products");
        details.setDescription("Create a new product");
        details.setAuthType("bearer");

        // Headers
        List<HeaderDto> headers = new ArrayList<>();
        headers.add(createHeader("h-4", "Authorization", "Bearer {{access_token}}", true, ""));
        headers.add(createHeader("h-5", "Content-Type", "application/json", true, ""));
        details.setHeaders(headers);

        details.setParameters(new ArrayList<>());

        // Body
        BodyDto body = new BodyDto();
        body.setType("raw");
        body.setRawType("json");
        body.setContent("{\n  \"name\": \"New Product\",\n  \"price\": 99.99,\n  \"category\": \"electronics\"\n}");
        details.setBody(body);

        // Auth config
        AuthConfigDto authConfig = new AuthConfigDto();
        authConfig.setType("bearer");
        authConfig.setToken("{{access_token}}");
        details.setAuthConfig(authConfig);

        details.setPreRequestScript("");
        details.setTests("pm.test('Status code is 201', function() {\n  pm.response.to.have.status(201);\n});");
        details.setCreatedAt("2024-01-14T16:45:00Z");
        details.setUpdatedAt("2024-01-14T16:45:00Z");
        details.setCollectionId("col-1");
        details.setFolderId("folder-2");
        details.setSaved(true);

        return details;
    }

    private RequestDetailsResponse getCreatePostRequestDetails() {
        RequestDetailsResponse details = new RequestDetailsResponse();
        details.setRequestId("req-5");
        details.setName("Create Post");
        details.setMethod("POST");
        details.setUrl("{{apiUrl}}/api/v1/posts");
        details.setDescription("Create a new post");
        details.setAuthType("bearer");

        // Headers
        List<HeaderDto> headers = new ArrayList<>();
        headers.add(createHeader("h-6", "Content-Type", "application/json", true, ""));
        details.setHeaders(headers);

        details.setParameters(new ArrayList<>());

        // Body
        BodyDto body = new BodyDto();
        body.setType("raw");
        body.setRawType("json");
        body.setContent("{\n  \"content\": \"Hello world!\",\n  \"media_urls\": [\"https://example.com/image.jpg\"]\n}");
        details.setBody(body);

        // Auth config
        AuthConfigDto authConfig = new AuthConfigDto();
        authConfig.setType("bearer");
        authConfig.setToken("{{access_token}}");
        details.setAuthConfig(authConfig);

        details.setPreRequestScript("");
        details.setTests("pm.test('Status code is 201', function() {\n  pm.response.to.have.status(201);\n});");
        details.setCreatedAt("2024-01-12T11:30:00Z");
        details.setUpdatedAt("2024-01-12T11:30:00Z");
        details.setCollectionId("col-2");
        details.setFolderId("folder-3");
        details.setSaved(true);

        return details;
    }

    private EnvironmentsResponse generateStaticEnvironments() {
        List<EnvironmentDto> environments = new ArrayList<>();

        EnvironmentDto noEnv = new EnvironmentDto();
        noEnv.setId("env-1");
        noEnv.setName("No Environment");
        noEnv.setActive(true);
        noEnv.setVariables(new ArrayList<>());
        environments.add(noEnv);

        EnvironmentDto devEnv = new EnvironmentDto();
        devEnv.setId("env-2");
        devEnv.setName("Development");
        devEnv.setActive(false);

        List<VariableDto> devVariables = new ArrayList<>();
        devVariables.add(createVariable("env-var-1", "base_url", "https://api.dev.example.com", "string", true));
        devVariables.add(createVariable("env-var-2", "access_token", "dev_token_123", "string", true));
        devEnv.setVariables(devVariables);
        environments.add(devEnv);

        EnvironmentDto prodEnv = new EnvironmentDto();
        prodEnv.setId("env-3");
        prodEnv.setName("Production");
        prodEnv.setActive(false);

        List<VariableDto> prodVariables = new ArrayList<>();
        prodVariables.add(createVariable("env-var-3", "base_url", "https://api.example.com", "string", true));
        prodVariables.add(createVariable("env-var-4", "access_token", "prod_token_456", "string", true));
        prodEnv.setVariables(prodVariables);
        environments.add(prodEnv);

        return new EnvironmentsResponse(environments);
    }

    // ========== HELPER METHODS ==========

    private VariableDto createVariable(String id, String key, String value, String type, boolean enabled) {
        VariableDto variable = new VariableDto();
        variable.setId(id);
        variable.setKey(key);
        variable.setValue(value);
        variable.setType(type);
        variable.setEnabled(enabled);
        return variable;
    }

    private ParameterDto createParameter(String id, String key, String value, String description, boolean enabled) {
        ParameterDto parameter = new ParameterDto();
        parameter.setId(id);
        parameter.setKey(key);
        parameter.setValue(value);
        parameter.setDescription(description);
        parameter.setEnabled(enabled);
        return parameter;
    }

    private HeaderDto createHeader(String id, String key, String value, boolean enabled, String description) {
        HeaderDto header = new HeaderDto();
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
            CollectionsListResponse collections = generateStaticCollectionsList("admin");
            collectionsCache.put("collections_list_admin", new CollectionsCache(collections, System.currentTimeMillis()));

            // Preload collection details
            CollectionDetailsResponse ecommerceDetails = generateEcommerceCollectionDetails();
            collectionsCache.put("collection_details_admin_col-1",
                    new CollectionsCache(ecommerceDetails, System.currentTimeMillis()));

            CollectionDetailsResponse socialMediaDetails = generateSocialMediaCollectionDetails();
            collectionsCache.put("collection_details_admin_col-2",
                    new CollectionsCache(socialMediaDetails, System.currentTimeMillis()));

            // Preload request details
            RequestDetailsResponse loginRequest = getLoginRequestDetails();
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

    private ExecuteRequestResponse executeSampleRequest(ExecuteRequestDto requestDto) {
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
            List<HeaderDto> headers = new ArrayList<>();
            headers.add(createHeader("res-header-1", "Content-Type", "application/json", true, ""));
            headers.add(createHeader("res-header-2", "X-RateLimit-Limit", "1000", true, ""));
            headers.add(createHeader("res-header-3", "X-RateLimit-Remaining", "999", true, ""));
            headers.add(createHeader("res-header-4", "X-Powered-By", "Express", true, ""));
            headers.add(createHeader("res-header-5", "Date", LocalDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME), true, ""));

            return new ExecuteRequestResponse(
                    responseBody,
                    Integer.parseInt(statusCode),
                    statusText,
                    headers,
                    timeMs,
                    sizeBytes
            );

        } catch (Exception e) {
            return new ExecuteRequestResponse(
                    "",
                    500,
                    "Error executing request: " + e.getMessage(),
                    Collections.emptyList(),
                    0L,
                    0L
            );
        }
    }

    private SaveRequestResponse saveSampleRequest(SaveRequestDto requestDto) {
        return new SaveRequestResponse(
                "req-" + System.currentTimeMillis(),
                "Request saved successfully"
        );
    }

    private CreateCollectionResponse createSampleCollection(CreateCollectionDto collectionDto) {
        return new CreateCollectionResponse(
                "col-" + System.currentTimeMillis(),
                "Collection created successfully"
        );
    }

    private ImportResponse importSampleCollection(ImportRequestDto importRequest) {
        return new ImportResponse(
                "col-import-" + System.currentTimeMillis(),
                "Collection imported successfully from " + importRequest.getSource()
        );
    }

    private CodeSnippetResponse generateSampleCodeSnippet(CodeSnippetRequestDto snippetRequest) {
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

        return new CodeSnippetResponse(code, language, "Code snippet generated successfully");
    }

    // Code snippet generators (same as before but using the static data)
    private String generateCurlSnippet(CodeSnippetRequestDto request) {
        StringBuilder curl = new StringBuilder();
        curl.append("curl -X ").append(request.getMethod()).append(" \\\n");
        curl.append("  \"").append(request.getUrl()).append("\"");

        if (request.getHeaders() != null) {
            for (HeaderDto header : request.getHeaders()) {
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

    private String generateJavaScriptSnippet(CodeSnippetRequestDto request) {
        StringBuilder js = new StringBuilder();
        js.append("fetch(\"").append(request.getUrl()).append("\", {\n");
        js.append("  method: \"").append(request.getMethod()).append("\",\n");

        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            js.append("  headers: {\n");
            for (int i = 0; i < request.getHeaders().size(); i++) {
                HeaderDto header = request.getHeaders().get(i);
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

    private String generatePythonSnippet(CodeSnippetRequestDto request) {
        StringBuilder python = new StringBuilder();
        python.append("import requests\n\n");

        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            python.append("headers = {\n");
            for (HeaderDto header : request.getHeaders()) {
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

    private String generateJavaSnippet(CodeSnippetRequestDto request) {
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
            for (HeaderDto header : request.getHeaders()) {
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

    private String generateNodeJsSnippet(CodeSnippetRequestDto request) {
        StringBuilder node = new StringBuilder();
        node.append("const https = require('https');\n\n");

        node.append("const options = {\n");
        node.append("  hostname: 'api.example.com',\n");
        node.append("  port: 443,\n");
        node.append("  path: '/api/v1/endpoint',\n");
        node.append("  method: '").append(request.getMethod()).append("',\n");

        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            node.append("  headers: {\n");
            for (int i = 0; i < request.getHeaders().size(); i++) {
                HeaderDto header = request.getHeaders().get(i);
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
            node.append("req.write(").append(request.getBody()).append(");\n");
        }

        node.append("req.end();");

        return node.toString();
    }

    private String generatePhpSnippet(CodeSnippetRequestDto request) {
        StringBuilder php = new StringBuilder();
        php.append("<?php\n\n");

        php.append("$ch = curl_init();\n\n");
        php.append("curl_setopt($ch, CURLOPT_URL, \"").append(request.getUrl()).append("\");\n");
        php.append("curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);\n");
        php.append("curl_setopt($ch, CURLOPT_CUSTOMREQUEST, \"").append(request.getMethod()).append("\");\n\n");

        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            php.append("$headers = [\n");
            for (HeaderDto header : request.getHeaders()) {
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

    private String generateRubySnippet(CodeSnippetRequestDto request) {
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
            for (HeaderDto header : request.getHeaders()) {
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

    // ========== FALLBACK METHODS ==========

    private CollectionsListResponse getFallbackCollectionsList() {
        List<CollectionDto> collections = new ArrayList<>();

        CollectionDto collection = new CollectionDto();
        collection.setId("col-1");
        collection.setName("E-Commerce API");
        collection.setDescription("Fallback collection");
        collection.setOwner("admin");
        collection.setFavorite(true);
        collection.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        collection.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        collection.setRequestsCount(5);
        collection.setFolderCount(2);
        collection.setTags(Arrays.asList("api", "fallback"));

        collections.add(collection);

        return new CollectionsListResponse(collections, 1);
    }

    private CollectionDetailsResponse getFallbackCollectionDetails(String collectionId) {
        CollectionDetailsResponse details = new CollectionDetailsResponse();
        details.setCollectionId(collectionId);
        details.setName("Fallback Collection");
        details.setDescription("Fallback collection details");
        details.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setTotalRequests(3);
        details.setTotalFolders(1);
        details.setFavorite(false);
        details.setOwner("admin");

        List<FolderDto> folders = new ArrayList<>();
        FolderDto folder = new FolderDto();
        folder.setId("folder-1");
        folder.setName("Fallback Folder");
        folder.setDescription("Fallback folder");
        folder.setRequestCount(2);
        folder.setExpanded(true);
        folders.add(folder);

        details.setFolders(folders);

        return details;
    }

    private RequestDetailsResponse getFallbackRequestDetails(String requestId) {
        RequestDetailsResponse details = new RequestDetailsResponse();
        details.setRequestId(requestId);
        details.setName("Fallback Request");
        details.setMethod("GET");
        details.setUrl("{{base_url}}/api/v1/fallback");
        details.setDescription("Fallback request details");
        details.setAuthType("noauth");
        details.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setCollectionId("col-1");
        details.setFolderId("folder-1");
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