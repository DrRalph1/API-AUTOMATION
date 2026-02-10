package com.usg.apiAutomation.services;

import com.usg.apiAutomation.dtos.documentation.*;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DocumentationService {

    private final LoggerUtil loggerUtil;

    // Cache for documentation data
    private final Map<String, DocCache> documentationCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 30 * 60 * 1000; // 30 minutes cache TTL

    // Sample data for FinTech APIs
    private static final String[] API_TYPES = {"REST", "SOAP", "GraphQL", "gRPC", "WebSocket"};
    private static final String[] ENVIRONMENTS = {"sandbox", "uat", "production", "staging", "development"};
    private static final String[] HTTP_METHODS = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};
    private static final String[] CONTENT_TYPES = {"application/json", "application/xml", "application/x-www-form-urlencoded", "multipart/form-data"};
    private static final String[] STATUS_CODES = {"200", "201", "400", "401", "403", "404", "429", "500"};
    private static final String[] PROGRAMMING_LANGUAGES = {"curl", "javascript", "python", "nodejs", "java", "csharp", "php", "go", "ruby"};
    private static final String[] TAG_CATEGORIES = {"authentication", "payments", "accounts", "transactions", "compliance", "reports", "webhooks"};
    private static final String[] API_OWNERS = {"FinTech Core Team", "Payments Team", "Compliance Team", "Platform Team", "Integration Team"};

    @PostConstruct
    public void init() {
        log.info("DocumentationService initialized");
        preloadDocumentationCache();
    }

    // ========== PUBLIC SERVICE METHODS ==========

    public APICollectionResponse getAPICollections(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Getting API collections for user: {}", requestId, performedBy);
            loggerUtil.log("documentation",
                    "Request ID: " + requestId + ", Getting API collections for user: " + performedBy);

            // Check cache first
            String cacheKey = "api_collections_" + performedBy;
            DocCache cachedData = documentationCache.get(cacheKey);

            if (cachedData != null && !isCacheExpired(cachedData)) {
                log.debug("Request ID: {}, Returning cached API collections", requestId);
                return (APICollectionResponse) cachedData.getData();
            }

            APICollectionResponse collections = generateAPICollections();

            // Update cache
            documentationCache.put(cacheKey, new DocCache(collections, System.currentTimeMillis()));

            log.info("Request ID: {}, Retrieved {} API collections", requestId, collections.getCollections().size());

            return collections;

        } catch (Exception e) {
            String errorMsg = "Error retrieving API collections: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return getFallbackAPICollections();
        }
    }

    public APIEndpointResponse getAPIEndpoints(String requestId, HttpServletRequest req, String performedBy,
                                               String collectionId, String folderId) {
        try {
            log.info("Request ID: {}, Getting API endpoints for collection: {}, folder: {}",
                    requestId, collectionId, folderId);

            // Check cache first
            String cacheKey = "api_endpoints_" + performedBy + "_" + collectionId + "_" + folderId;
            DocCache cachedData = documentationCache.get(cacheKey);

            if (cachedData != null && !isCacheExpired(cachedData)) {
                log.debug("Request ID: {}, Returning cached API endpoints", requestId);
                return (APIEndpointResponse) cachedData.getData();
            }

            APIEndpointResponse endpoints = generateAPIEndpoints(collectionId, folderId);

            // Update cache
            documentationCache.put(cacheKey, new DocCache(endpoints, System.currentTimeMillis()));

            log.info("Request ID: {}, Retrieved {} API endpoints", requestId, endpoints.getEndpoints().size());

            return endpoints;

        } catch (Exception e) {
            String errorMsg = "Error retrieving API endpoints: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return new APIEndpointResponse(Collections.emptyList(), collectionId, 0);
        }
    }

    public EndpointDetailResponse getEndpointDetails(String requestId, HttpServletRequest req, String performedBy,
                                                     String collectionId, String endpointId) {
        try {
            log.info("Request ID: {}, Getting endpoint details for: {}",
                    requestId, endpointId);

            // Check cache first
            String cacheKey = "endpoint_details_" + performedBy + "_" + collectionId + "_" + endpointId;
            DocCache cachedData = documentationCache.get(cacheKey);

            if (cachedData != null && !isCacheExpired(cachedData)) {
                log.debug("Request ID: {}, Returning cached endpoint details", requestId);
                return (EndpointDetailResponse) cachedData.getData();
            }

            EndpointDetailResponse details = generateEndpointDetails(endpointId);

            // Update cache
            documentationCache.put(cacheKey, new DocCache(details, System.currentTimeMillis()));

            log.info("Request ID: {}, Retrieved details for endpoint: {}", requestId, endpointId);

            return details;

        } catch (Exception e) {
            String errorMsg = "Error retrieving endpoint details: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return getFallbackEndpointDetails(endpointId);
        }
    }

    public CodeExampleResponse getCodeExamples(String requestId, HttpServletRequest req, String performedBy,
                                               String endpointId, String language) {
        try {
            log.info("Request ID: {}, Getting code examples for endpoint: {}, language: {}",
                    requestId, endpointId, language);

            CodeExampleResponse examples = generateCodeExamples(endpointId, language);

            log.info("Request ID: {}, Retrieved code examples for {}", requestId, language);

            return examples;

        } catch (Exception e) {
            String errorMsg = "Error retrieving code examples: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return new CodeExampleResponse(language, endpointId, "", "Error generating code examples: " + e.getMessage());
        }
    }

    public SearchDocumentationResponse searchDocumentation(String requestId, HttpServletRequest req, String performedBy,
                                                           String searchQuery, String searchType, int maxResults) {
        try {
            log.info("Request ID: {}, Searching documentation with query: {}, type: {}",
                    requestId, searchQuery, searchType);

            SearchDocumentationResponse searchResults = performDocumentationSearch(searchQuery, searchType, maxResults);

            log.info("Request ID: {}, Found {} search results for query: {}",
                    requestId, searchResults.getResults().size(), searchQuery);

            return searchResults;

        } catch (Exception e) {
            String errorMsg = "Error searching documentation: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return new SearchDocumentationResponse(Collections.emptyList(), searchQuery, 0);
        }
    }

    public PublishDocumentationResponse publishDocumentation(String requestId, HttpServletRequest req, String performedBy,
                                                             String collectionId, String title,
                                                             String visibility, String customDomain) {
        try {
            log.info("Request ID: {}, Publishing documentation for collection: {}", requestId, collectionId);

            PublishDocumentationResponse publishResponse = generatePublishResponse(collectionId, title, visibility, customDomain);

            log.info("Request ID: {}, Documentation published successfully with URL: {}",
                    requestId, publishResponse.getPublishedUrl());

            return publishResponse;

        } catch (Exception e) {
            String errorMsg = "Error publishing documentation: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return new PublishDocumentationResponse("", collectionId, "Error publishing documentation: " + e.getMessage());
        }
    }

    public EnvironmentResponse getEnvironments(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Getting environments for user: {}", requestId, performedBy);

            EnvironmentResponse environments = generateEnvironments();

            log.info("Request ID: {}, Retrieved {} environments", requestId, environments.getEnvironments().size());

            return environments;

        } catch (Exception e) {
            String errorMsg = "Error retrieving environments: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return new EnvironmentResponse(Collections.emptyList());
        }
    }

    public NotificationResponse getNotifications(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Getting notifications for user: {}", requestId, performedBy);

            NotificationResponse notifications = generateNotifications();

            log.info("Request ID: {}, Retrieved {} notifications", requestId, notifications.getNotifications().size());

            return notifications;

        } catch (Exception e) {
            String errorMsg = "Error retrieving notifications: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return new NotificationResponse(Collections.emptyList());
        }
    }

    public ChangelogResponse getChangelog(String requestId, HttpServletRequest req, String performedBy,
                                          String collectionId) {
        try {
            log.info("Request ID: {}, Getting changelog for collection: {}", requestId, collectionId);

            ChangelogResponse changelog = generateChangelog(collectionId);

            log.info("Request ID: {}, Retrieved {} changelog entries", requestId, changelog.getEntries().size());

            return changelog;

        } catch (Exception e) {
            String errorMsg = "Error retrieving changelog: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return new ChangelogResponse(Collections.emptyList(), collectionId);
        }
    }

    public GenerateMockResponse generateMockServer(String requestId, HttpServletRequest req, String performedBy,
                                                   String collectionId, Map<String, String> options) {
        try {
            log.info("Request ID: {}, Generating mock server for collection: {}", requestId, collectionId);

            GenerateMockResponse mockResponse = generateMockServerResponse(collectionId, options);

            log.info("Request ID: {}, Generated mock server with {} endpoints",
                    requestId, mockResponse.getMockEndpoints().size());

            return mockResponse;

        } catch (Exception e) {
            String errorMsg = "Error generating mock server: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return new GenerateMockResponse(Collections.emptyList(), collectionId,
                    "Error generating mock server: " + e.getMessage());
        }
    }

    public void clearDocumentationCache(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Clearing documentation cache for user: {}", requestId, performedBy);

            int beforeSize = documentationCache.size();
            documentationCache.clear();
            int afterSize = documentationCache.size();

            log.info("Request ID: {}, Cleared {} documentation cache entries", requestId, beforeSize - afterSize);
            loggerUtil.log("documentation",
                    "Request ID: " + requestId + ", Cleared documentation cache for user: " + performedBy);

        } catch (Exception e) {
            String errorMsg = "Error clearing documentation cache: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
        }
    }

    // ========== PRIVATE HELPER METHODS ==========

    private void preloadDocumentationCache() {
        try {
            log.info("Preloading documentation cache with sample data");

            // Preload API collections
            APICollectionResponse collections = generateAPICollections();
            documentationCache.put("api_collections_admin", new DocCache(collections, System.currentTimeMillis()));

            // Preload FinTech Core endpoints
            APIEndpointResponse endpoints = generateAPIEndpoints("fintech-core", "accounts");
            documentationCache.put("api_endpoints_admin_fintech-core_accounts",
                    new DocCache(endpoints, System.currentTimeMillis()));

            log.info("Documentation cache preloaded with {} entries", documentationCache.size());

        } catch (Exception e) {
            log.warn("Failed to preload documentation cache: {}", e.getMessage());
        }
    }

    private boolean isCacheExpired(DocCache cache) {
        return (System.currentTimeMillis() - cache.getTimestamp()) > CACHE_TTL_MS;
    }

    private APICollectionResponse generateAPICollections() {
        List<APICollectionDto> collections = new ArrayList<>();

        // Generate FinTech Core Banking API collection
        APICollectionDto coreCollection = new APICollectionDto();
        coreCollection.setId("fintech-core");
        coreCollection.setName("FinTech Core Banking API");
        coreCollection.setDescription("Core banking operations including accounts, transactions, payments, and transfers");
        coreCollection.setVersion("v2.1");
        coreCollection.setOwner(API_OWNERS[0]);
        coreCollection.setType(API_TYPES[0]); // REST
        coreCollection.setFavorite(true);
        coreCollection.setExpanded(true);
        coreCollection.setUpdatedAt("Today, 9:30 AM");
        coreCollection.setCreatedAt("Jan 15, 2024");
        coreCollection.setTotalEndpoints(15);
        coreCollection.setTotalFolders(6);
        coreCollection.setColor("#3B82F6"); // Blue
        collections.add(coreCollection);

        // Generate Loan Management API collection
        APICollectionDto loansCollection = new APICollectionDto();
        loansCollection.setId("fintech-loans");
        loansCollection.setName("Loan Management API");
        loansCollection.setDescription("Loan applications, approvals, and disbursements");
        loansCollection.setVersion("v1.5");
        loansCollection.setOwner(API_OWNERS[2]);
        loansCollection.setType(API_TYPES[0]); // REST
        loansCollection.setFavorite(false);
        loansCollection.setExpanded(false);
        loansCollection.setUpdatedAt("1 week ago");
        loansCollection.setCreatedAt("Feb 1, 2024");
        loansCollection.setTotalEndpoints(8);
        loansCollection.setTotalFolders(3);
        loansCollection.setColor("#10B981"); // Green
        collections.add(loansCollection);

        // Generate Compliance & AML API collection
        APICollectionDto complianceCollection = new APICollectionDto();
        complianceCollection.setId("fintech-compliance");
        complianceCollection.setName("Compliance & AML API");
        complianceCollection.setDescription("Anti-money laundering and compliance checks");
        complianceCollection.setVersion("v3.2");
        complianceCollection.setOwner(API_OWNERS[2]);
        complianceCollection.setType(API_TYPES[0]); // REST
        complianceCollection.setFavorite(true);
        complianceCollection.setExpanded(false);
        complianceCollection.setUpdatedAt("3 days ago");
        complianceCollection.setCreatedAt("Mar 1, 2024");
        complianceCollection.setTotalEndpoints(12);
        complianceCollection.setTotalFolders(4);
        complianceCollection.setColor("#8B5CF6"); // Purple
        collections.add(complianceCollection);

        // Generate Payment Processing API collection
        APICollectionDto paymentsCollection = new APICollectionDto();
        paymentsCollection.setId("fintech-payments");
        paymentsCollection.setName("Payment Processing API");
        paymentsCollection.setDescription("Payment gateway integration and processing");
        paymentsCollection.setVersion("v2.0");
        paymentsCollection.setOwner(API_OWNERS[1]);
        paymentsCollection.setType(API_TYPES[0]); // REST
        paymentsCollection.setFavorite(false);
        paymentsCollection.setExpanded(false);
        paymentsCollection.setUpdatedAt("2 days ago");
        paymentsCollection.setCreatedAt("Dec 10, 2023");
        paymentsCollection.setTotalEndpoints(20);
        paymentsCollection.setTotalFolders(5);
        paymentsCollection.setColor("#F59E0B"); // Amber
        collections.add(paymentsCollection);

        // Generate Webhooks API collection
        APICollectionDto webhooksCollection = new APICollectionDto();
        webhooksCollection.setId("fintech-webhooks");
        webhooksCollection.setName("Webhooks & Notifications API");
        webhooksCollection.setDescription("Real-time notifications and webhook management");
        webhooksCollection.setVersion("v1.2");
        webhooksCollection.setOwner(API_OWNERS[3]);
        webhooksCollection.setType(API_TYPES[3]); // WebSocket
        webhooksCollection.setFavorite(false);
        webhooksCollection.setExpanded(false);
        webhooksCollection.setUpdatedAt("Yesterday, 4:20 PM");
        webhooksCollection.setCreatedAt("Feb 20, 2024");
        webhooksCollection.setTotalEndpoints(6);
        webhooksCollection.setTotalFolders(2);
        webhooksCollection.setColor("#EF4444"); // Red
        collections.add(webhooksCollection);

        return new APICollectionResponse(collections);
    }

    private APIEndpointResponse generateAPIEndpoints(String collectionId, String folderId) {
        List<APIEndpointDto> endpoints = new ArrayList<>();

        if ("fintech-core".equals(collectionId)) {
            switch (folderId) {
                case "accounts":
                    endpoints.add(createAccountEndpoint("create-account"));
                    endpoints.add(createAccountEndpoint("get-account"));
                    endpoints.add(createAccountEndpoint("list-accounts"));
                    endpoints.add(createAccountEndpoint("update-account"));
                    endpoints.add(createAccountEndpoint("close-account"));
                    break;
                case "transactions":
                    endpoints.add(createTransactionEndpoint("cash-deposit"));
                    endpoints.add(createTransactionEndpoint("cash-withdrawal"));
                    endpoints.add(createTransactionEndpoint("funds-transfer"));
                    endpoints.add(createTransactionEndpoint("bulk-transactions"));
                    break;
                case "enquiries":
                    endpoints.add(createEnquiryEndpoint("balance-enquiry"));
                    endpoints.add(createEnquiryEndpoint("account-statement"));
                    endpoints.add(createEnquiryEndpoint("mini-statement"));
                    endpoints.add(createEnquiryEndpoint("transaction-history"));
                    break;
                case "payments":
                    endpoints.add(createPaymentEndpoint("bill-payment"));
                    endpoints.add(createPaymentEndpoint("merchant-payment"));
                    endpoints.add(createPaymentEndpoint("scheduled-payment"));
                    endpoints.add(createPaymentEndpoint("payment-status"));
                    break;
                case "cards":
                    endpoints.add(createCardEndpoint("issue-card"));
                    endpoints.add(createCardEndpoint("block-card"));
                    endpoints.add(createCardEndpoint("card-details"));
                    endpoints.add(createCardEndpoint("card-transactions"));
                    break;
                default:
                    // Return all endpoints for the collection
                    endpoints.add(createAccountEndpoint("create-account"));
                    endpoints.add(createTransactionEndpoint("cash-deposit"));
                    endpoints.add(createEnquiryEndpoint("balance-enquiry"));
                    endpoints.add(createPaymentEndpoint("bill-payment"));
                    break;
            }
        }

        return new APIEndpointResponse(endpoints, collectionId, endpoints.size());
    }

    private EndpointDetailResponse generateEndpointDetails(String endpointId) {
        EndpointDetailResponse details = new EndpointDetailResponse();
        details.setEndpointId(endpointId);
        details.setName(getEndpointName(endpointId));
        details.setMethod(getEndpointMethod(endpointId));
        details.setUrl(getEndpointUrl(endpointId));
        details.setDescription(getEndpointDescription(endpointId));
        details.setCategory(getEndpointCategory(endpointId));
        details.setTags(getEndpointTags(endpointId));
        details.setLastModified(getLastModifiedTime(endpointId));
        details.setVersion("v2.1");
        details.setRequiresAuthentication(true);
        details.setRateLimit("100 requests/minute");
        details.setDeprecated(false);

        // Add headers
        List<HeaderDto> headers = new ArrayList<>();
        headers.add(new HeaderDto("Content-Type", "application/json", "Required", true));
        headers.add(new HeaderDto("Authorization", "Bearer {access-token}", "Required", true));
        headers.add(new HeaderDto("X-Client-Id", "{client-id}", "Required", true));
        if (endpointId.contains("transaction")) {
            headers.add(new HeaderDto("X-Transaction-Id", "{unique-transaction-id}", "Required", true));
        }
        details.setHeaders(headers);

        // Add request parameters
        if (endpointId.contains("account")) {
            List<ParameterDto> parameters = new ArrayList<>();
            parameters.add(new ParameterDto("customerId", "string", "body", true, "Customer identifier"));
            parameters.add(new ParameterDto("accountType", "string", "body", true, "Type of account (SAVINGS, CHECKING)"));
            parameters.add(new ParameterDto("currency", "string", "body", true, "Account currency (USD, EUR, etc.)"));
            parameters.add(new ParameterDto("initialDeposit", "number", "body", false, "Initial deposit amount"));
            details.setParameters(parameters);

            // Request body example
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("customerId", "CUST123456");
            requestBody.put("accountType", "SAVINGS");
            requestBody.put("currency", "USD");
            requestBody.put("initialDeposit", 1000.00);
            requestBody.put("branchCode", "NYC001");
            requestBody.put("productCode", "SAV-PRO");
            requestBody.put("overdraftLimit", 0);
            requestBody.put("interestRate", 2.5);
            details.setRequestBodyExample(formatJson(requestBody));
        }

        // Add response examples
        List<ResponseExampleDto> responseExamples = new ArrayList<>();

        // Success response
        ResponseExampleDto successResponse = new ResponseExampleDto();
        successResponse.setStatusCode(200);
        successResponse.setDescription("Success");

        Map<String, Object> successExample = new HashMap<>();
        successExample.put("success", true);
        successExample.put("transactionId", "TXN202403150001");
        successExample.put("accountNumber", "ACC987654321");
        successExample.put("newBalance", 15000.00);
        successExample.put("referenceNumber", "DEP20240315001");
        successExample.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        successResponse.setExample(formatJson(successExample));
        responseExamples.add(successResponse);

        // Error responses
        ResponseExampleDto errorResponse = new ResponseExampleDto();
        errorResponse.setStatusCode(400);
        errorResponse.setDescription("Bad Request");

        Map<String, Object> errorExample = new HashMap<>();
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("code", "VALIDATION_ERROR");
        errorDetails.put("message", "Invalid request parameters");
        errorDetails.put("details", Arrays.asList("accountNumber must be valid format"));
        errorExample.put("error", errorDetails);
        errorResponse.setExample(formatJson(errorExample));
        responseExamples.add(errorResponse);

        details.setResponseExamples(responseExamples);

        // Add rate limiting info
        Map<String, Object> rateLimitInfo = new HashMap<>();
        rateLimitInfo.put("requestsPerMinute", 100);
        rateLimitInfo.put("burstCapacity", 150);
        rateLimitInfo.put("strategy", "token_bucket");
        details.setRateLimitInfo(rateLimitInfo);

        // Add changelog entries
        List<ChangelogEntryDto> changelogEntries = new ArrayList<>();
        changelogEntries.add(new ChangelogEntryDto("v2.1", "2024-03-15", "Added real-time validation"));
        changelogEntries.add(new ChangelogEntryDto("v2.0", "2023-12-01", "Enhanced security features"));
        details.setChangelog(changelogEntries);

        return details;
    }

    private CodeExampleResponse generateCodeExamples(String endpointId, String language) {
        String codeExample = "";
        String endpointUrl = getEndpointUrl(endpointId);
        String method = getEndpointMethod(endpointId);

        switch (language) {
            case "curl":
                codeExample = String.format(
                        "curl -X %s \"%s\" \\\n" +
                                "  -H \"Content-Type: application/json\" \\\n" +
                                "  -H \"Authorization: Bearer YOUR_ACCESS_TOKEN\" \\\n" +
                                "  -H \"X-Client-Id: YOUR_CLIENT_ID\"",
                        method, endpointUrl
                );
                if (method.equals("POST") || method.equals("PUT")) {
                    codeExample += String.format(" \\\n  -d '%s'", getSampleRequestBody());
                }
                break;

            case "javascript":
                codeExample = String.format(
                        "fetch('%s', {\n" +
                                "  method: '%s',\n" +
                                "  headers: {\n" +
                                "    'Content-Type': 'application/json',\n" +
                                "    'Authorization': 'Bearer YOUR_ACCESS_TOKEN',\n" +
                                "    'X-Client-Id': 'YOUR_CLIENT_ID'\n" +
                                "  }%s\n" +
                                "})",
                        endpointUrl, method,
                        (method.equals("POST") || method.equals("PUT")) ? ",\n  body: JSON.stringify(" + getSampleRequestBody() + ")" : ""
                );
                break;

            case "python":
                codeExample = String.format(
                        "import requests\n\n" +
                                "headers = {\n" +
                                "    'Content-Type': 'application/json',\n" +
                                "    'Authorization': 'Bearer YOUR_ACCESS_TOKEN',\n" +
                                "    'X-Client-Id': 'YOUR_CLIENT_ID'\n" +
                                "}\n\n" +
                                "response = requests.%s('%s', headers=headers%s)",
                        method.toLowerCase(), endpointUrl,
                        (method.equals("POST") || method.equals("PUT")) ? ", json=" + getSampleRequestBody() : ""
                );
                break;

            case "nodejs":
                codeExample = String.format(
                        "const https = require('https');\n\n" +
                                "const options = {\n" +
                                "  hostname: 'api.fintech.com',\n" +
                                "  path: '%s',\n" +
                                "  method: '%s',\n" +
                                "  headers: {\n" +
                                "    'Content-Type': 'application/json',\n" +
                                "    'Authorization': 'Bearer YOUR_ACCESS_TOKEN',\n" +
                                "    'X-Client-Id': 'YOUR_CLIENT_ID'\n" +
                                "  }\n" +
                                "};",
                        endpointUrl.replace("https://api.fintech.com", ""), method
                );
                break;

            case "java":
                codeExample = String.format(
                        "HttpClient client = HttpClient.newHttpClient();\n" +
                                "HttpRequest request = HttpRequest.newBuilder()\n" +
                                "    .uri(URI.create(\"%s\"))\n" +
                                "    .header(\"Content-Type\", \"application/json\")\n" +
                                "    .header(\"Authorization\", \"Bearer YOUR_ACCESS_TOKEN\")\n" +
                                "    .header(\"X-Client-Id\", \"YOUR_CLIENT_ID\")\n" +
                                "    .%s(%s)\n" +
                                "    .build();",
                        endpointUrl,
                        method.equals("GET") ? "GET" : method,
                        (method.equals("POST") || method.equals("PUT")) ? "HttpRequest.BodyPublishers.ofString(\"" + getSampleRequestBody() + "\")" : ""
                );
                break;

            default:
                codeExample = String.format("// Code example for %s in %s", endpointId, language);
                break;
        }

        return new CodeExampleResponse(language, endpointId, codeExample, "Generated sample code");
    }

    private SearchDocumentationResponse performDocumentationSearch(String searchQuery, String searchType, int maxResults) {
        List<SearchResultDto> results = new ArrayList<>();

        // Generate sample search results based on query
        for (int i = 1; i <= Math.min(maxResults, 15); i++) {
            SearchResultDto result = new SearchResultDto();
            result.setId("search-result-" + i);
            result.setTitle(getSearchResultTitle(i, searchQuery));
            result.setType(getSearchResultType(i));
            result.setCategory(getSearchResultCategory(i));
            result.setDescription(getSearchResultDescription(i, searchQuery));
            result.setRelevanceScore((int) (Math.random() * 100));
            result.setCollection("fintech-core");
            result.setEndpointUrl("/v2.1/" + getSearchResultEndpoint(i));
            result.setLastUpdated(LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            results.add(result);
        }

        return new SearchDocumentationResponse(results, searchQuery, results.size());
    }

    private PublishDocumentationResponse generatePublishResponse(String collectionId, String title, String visibility, String customDomain) {
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        String baseUrl = "https://docs.fintech.com";

        if (customDomain != null && !customDomain.isEmpty()) {
            baseUrl = "https://" + customDomain;
        }

        String publishedUrl = baseUrl + "/view/" + randomId + "/" + collectionId.toLowerCase().replace(" ", "-");

        return new PublishDocumentationResponse(
                publishedUrl,
                collectionId,
                "Documentation published successfully with " + visibility + " visibility"
        );
    }

    private EnvironmentResponse generateEnvironments() {
        List<EnvironmentDto> environments = new ArrayList<>();

        environments.add(createEnvironment("sandbox", "https://api.sandbox.fintech.com", true));
        environments.add(createEnvironment("uat", "https://api.uat.fintech.com", false));
        environments.add(createEnvironment("production", "https://api.fintech.com", false));
        environments.add(createEnvironment("staging", "https://api.staging.fintech.com", false));
        environments.add(createEnvironment("development", "https://api.dev.fintech.com", false));

        return new EnvironmentResponse(environments);
    }

    private NotificationResponse generateNotifications() {
        List<NotificationDto> notifications = new ArrayList<>();

        notifications.add(createNotification("notif-1", "API Rate Limit Warning",
                "You've used 85% of your API rate limit", "warning", false, "10 minutes ago"));
        notifications.add(createNotification("notif-2", "New API Version Available",
                "FinTech Core Banking API v2.2 is now available", "info", false, "2 hours ago"));
        notifications.add(createNotification("notif-3", "Compliance Update",
                "AML compliance requirements have been updated", "success", true, "1 day ago"));
        notifications.add(createNotification("notif-4", "Scheduled Maintenance",
                "API maintenance scheduled for March 20, 2:00 AM UTC", "info", false, "2 days ago"));
        notifications.add(createNotification("notif-5", "Documentation Published",
                "Your API documentation has been successfully published", "success", true, "3 days ago"));

        return new NotificationResponse(notifications);
    }

    private ChangelogResponse generateChangelog(String collectionId) {
        List<ChangelogEntryDto> entries = new ArrayList<>();

        if ("fintech-core".equals(collectionId)) {
            entries.add(new ChangelogEntryDto("v2.1", "March 2024",
                    Arrays.asList("Added real-time transaction notifications",
                            "Enhanced AML compliance checks",
                            "Improved error handling for failed transactions")));
            entries.add(new ChangelogEntryDto("v2.0", "December 2023",
                    Arrays.asList("New payment processing endpoints",
                            "Enhanced security with 2FA",
                            "Added bulk transaction support")));
            entries.add(new ChangelogEntryDto("v1.5", "September 2023",
                    Arrays.asList("Account statement enhancements",
                            "Performance improvements",
                            "Deprecated legacy authentication")));
        }

        return new ChangelogResponse(entries, collectionId);
    }

    private GenerateMockResponse generateMockServerResponse(String collectionId, Map<String, String> options) {
        List<MockEndpointDto> mockEndpoints = new ArrayList<>();

        // Generate mock endpoints for the collection
        if ("fintech-core".equals(collectionId)) {
            mockEndpoints.add(createMockEndpoint("create-account", "POST", "/api/v2.1/accounts", 201));
            mockEndpoints.add(createMockEndpoint("get-account", "GET", "/api/v2.1/accounts/{id}", 200));
            mockEndpoints.add(createMockEndpoint("cash-deposit", "POST", "/api/v2.1/transactions/deposit", 200));
            mockEndpoints.add(createMockEndpoint("balance-enquiry", "GET", "/api/v2.1/accounts/{id}/balance", 200));
        }

        return new GenerateMockResponse(mockEndpoints, collectionId,
                "Mock server generated with " + mockEndpoints.size() + " endpoints");
    }

    // ========== HELPER METHODS FOR CREATING DTOs ==========

    private APIEndpointDto createAccountEndpoint(String endpointId) {
        APIEndpointDto endpoint = new APIEndpointDto();
        endpoint.setId(endpointId);
        endpoint.setName(getEndpointName(endpointId));
        endpoint.setMethod(getEndpointMethod(endpointId));
        endpoint.setUrl(getEndpointUrl(endpointId));
        endpoint.setDescription(getEndpointDescription(endpointId));
        endpoint.setTags(getEndpointTags(endpointId));
        endpoint.setLastModified(getLastModifiedTime(endpointId));
        endpoint.setRequiresAuth(true);
        endpoint.setDeprecated(false);
        endpoint.setFolder("accounts");
        return endpoint;
    }

    private APIEndpointDto createTransactionEndpoint(String endpointId) {
        APIEndpointDto endpoint = new APIEndpointDto();
        endpoint.setId(endpointId);
        endpoint.setName(getEndpointName(endpointId));
        endpoint.setMethod(getEndpointMethod(endpointId));
        endpoint.setUrl(getEndpointUrl(endpointId));
        endpoint.setDescription(getEndpointDescription(endpointId));
        endpoint.setTags(getEndpointTags(endpointId));
        endpoint.setLastModified(getLastModifiedTime(endpointId));
        endpoint.setRequiresAuth(true);
        endpoint.setDeprecated(false);
        endpoint.setFolder("transactions");
        return endpoint;
    }

    private APIEndpointDto createEnquiryEndpoint(String endpointId) {
        APIEndpointDto endpoint = new APIEndpointDto();
        endpoint.setId(endpointId);
        endpoint.setName(getEndpointName(endpointId));
        endpoint.setMethod(getEndpointMethod(endpointId));
        endpoint.setUrl(getEndpointUrl(endpointId));
        endpoint.setDescription(getEndpointDescription(endpointId));
        endpoint.setTags(getEndpointTags(endpointId));
        endpoint.setLastModified(getLastModifiedTime(endpointId));
        endpoint.setRequiresAuth(true);
        endpoint.setDeprecated(false);
        endpoint.setFolder("enquiries");
        return endpoint;
    }

    private APIEndpointDto createPaymentEndpoint(String endpointId) {
        APIEndpointDto endpoint = new APIEndpointDto();
        endpoint.setId(endpointId);
        endpoint.setName(getEndpointName(endpointId));
        endpoint.setMethod(getEndpointMethod(endpointId));
        endpoint.setUrl(getEndpointUrl(endpointId));
        endpoint.setDescription(getEndpointDescription(endpointId));
        endpoint.setTags(getEndpointTags(endpointId));
        endpoint.setLastModified(getLastModifiedTime(endpointId));
        endpoint.setRequiresAuth(true);
        endpoint.setDeprecated(false);
        endpoint.setFolder("payments");
        return endpoint;
    }

    private APIEndpointDto createCardEndpoint(String endpointId) {
        APIEndpointDto endpoint = new APIEndpointDto();
        endpoint.setId(endpointId);
        endpoint.setName(getEndpointName(endpointId));
        endpoint.setMethod(getEndpointMethod(endpointId));
        endpoint.setUrl(getEndpointUrl(endpointId));
        endpoint.setDescription(getEndpointDescription(endpointId));
        endpoint.setTags(getEndpointTags(endpointId));
        endpoint.setLastModified(getLastModifiedTime(endpointId));
        endpoint.setRequiresAuth(true);
        endpoint.setDeprecated(false);
        endpoint.setFolder("cards");
        return endpoint;
    }

    private EnvironmentDto createEnvironment(String id, String baseUrl, boolean isActive) {
        EnvironmentDto env = new EnvironmentDto();
        env.setId(id);
        env.setName(id.substring(0, 1).toUpperCase() + id.substring(1));
        env.setBaseUrl(baseUrl);
        env.setActive(isActive);
        env.setDescription(id.equals("sandbox") ? "Testing environment with mock data" :
                id.equals("production") ? "Live production environment" :
                        id.equals("uat") ? "User acceptance testing" : "Development environment");
        return env;
    }

    private NotificationDto createNotification(String id, String title, String message,
                                               String type, boolean read, String time) {
        NotificationDto notification = new NotificationDto();
        notification.setId(id);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(read);
        notification.setTime(time);
        notification.setIcon(getNotificationIcon(type));
        return notification;
    }

    private MockEndpointDto createMockEndpoint(String id, String method, String path, int statusCode) {
        MockEndpointDto endpoint = new MockEndpointDto();
        endpoint.setId(id);
        endpoint.setMethod(method);
        endpoint.setPath(path);
        endpoint.setStatusCode(statusCode);
        endpoint.setResponseDelay((int) (Math.random() * 1000) + 100); // 100-1100ms delay
        endpoint.setResponseBody(getMockResponseBody(id, statusCode));
        endpoint.setDescription("Mock endpoint for " + id);
        return endpoint;
    }

    // ========== SAMPLE DATA GENERATORS ==========

    private String getEndpointName(String endpointId) {
        Map<String, String> names = new HashMap<>();
        names.put("create-account", "Create Account");
        names.put("get-account", "Get Account Details");
        names.put("list-accounts", "List Accounts");
        names.put("cash-deposit", "Cash Deposit");
        names.put("cash-withdrawal", "Cash Withdrawal");
        names.put("funds-transfer", "Funds Transfer");
        names.put("balance-enquiry", "Balance Enquiry");
        names.put("account-statement", "Account Statement");
        names.put("mini-statement", "Mini Statement");
        names.put("bill-payment", "Bill Payment");
        names.put("issue-card", "Issue New Card");

        return names.getOrDefault(endpointId, "API Endpoint");
    }

    private String getEndpointMethod(String endpointId) {
        if (endpointId.contains("get") || endpointId.contains("list") || endpointId.contains("balance") || endpointId.contains("statement")) {
            return "GET";
        } else if (endpointId.contains("create") || endpointId.contains("deposit") || endpointId.contains("withdrawal") ||
                endpointId.contains("transfer") || endpointId.contains("payment") || endpointId.contains("issue")) {
            return "POST";
        } else if (endpointId.contains("update")) {
            return "PUT";
        } else if (endpointId.contains("delete") || endpointId.contains("close") || endpointId.contains("block")) {
            return "DELETE";
        }
        return "GET";
    }

    private String getEndpointUrl(String endpointId) {
        Map<String, String> urls = new HashMap<>();
        urls.put("create-account", "https://api.fintech.com/v2.1/accounts");
        urls.put("get-account", "https://api.fintech.com/v2.1/accounts/{accountNumber}");
        urls.put("list-accounts", "https://api.fintech.com/v2.1/customers/{customerId}/accounts");
        urls.put("cash-deposit", "https://api.fintech.com/v2.1/transactions/deposit");
        urls.put("cash-withdrawal", "https://api.fintech.com/v2.1/transactions/withdrawal");
        urls.put("funds-transfer", "https://api.fintech.com/v2.1/transactions/transfer");
        urls.put("balance-enquiry", "https://api.fintech.com/v2.1/accounts/{accountNumber}/balance");
        urls.put("account-statement", "https://api.fintech.com/v2.1/accounts/{accountNumber}/statement");
        urls.put("mini-statement", "https://api.fintech.com/v2.1/accounts/{accountNumber}/statement/mini");
        urls.put("bill-payment", "https://api.fintech.com/v2.1/payments/bill");
        urls.put("issue-card", "https://api.fintech.com/v2.1/cards");

        return urls.getOrDefault(endpointId, "https://api.fintech.com/v2.1/api");
    }

    private String getEndpointDescription(String endpointId) {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("create-account", "Create a new bank account for a customer");
        descriptions.put("get-account", "Retrieve detailed information about a specific account");
        descriptions.put("list-accounts", "List all accounts for a specific customer");
        descriptions.put("cash-deposit", "Process cash deposit into an account");
        descriptions.put("cash-withdrawal", "Process cash withdrawal from an account");
        descriptions.put("funds-transfer", "Transfer funds between accounts");
        descriptions.put("balance-enquiry", "Check current balance of an account");
        descriptions.put("account-statement", "Retrieve account statement for a period");
        descriptions.put("mini-statement", "Get last 10 transactions");
        descriptions.put("bill-payment", "Pay utility bills and invoices");
        descriptions.put("issue-card", "Issue new debit/credit card");

        return descriptions.getOrDefault(endpointId, "API endpoint for financial operations");
    }

    private List<String> getEndpointTags(String endpointId) {
        if (endpointId.contains("account")) {
            return Arrays.asList("accounts", "onboarding", "management");
        } else if (endpointId.contains("transaction") || endpointId.contains("deposit") || endpointId.contains("withdrawal")) {
            return Arrays.asList("transactions", "processing", "cash");
        } else if (endpointId.contains("payment") || endpointId.contains("transfer")) {
            return Arrays.asList("payments", "transfers", "processing");
        } else if (endpointId.contains("enquiry") || endpointId.contains("statement") || endpointId.contains("balance")) {
            return Arrays.asList("enquiry", "balance", "reporting");
        } else if (endpointId.contains("card")) {
            return Arrays.asList("cards", "issuance", "management");
        }
        return Arrays.asList("api", "endpoint");
    }

    private String getLastModifiedTime(String endpointId) {
        // Return recent timestamps for sample data
        if (endpointId.contains("create") || endpointId.contains("deposit")) {
            return "Today, 9:00 AM";
        } else if (endpointId.contains("get") || endpointId.contains("balance")) {
            return "Yesterday, 3:45 PM";
        } else if (endpointId.contains("transfer") || endpointId.contains("payment")) {
            return "2 days ago";
        }
        return "1 week ago";
    }

    private String getEndpointCategory(String endpointId) {
        if (endpointId.contains("account")) return "Account Management";
        if (endpointId.contains("transaction")) return "Transaction Processing";
        if (endpointId.contains("enquiry") || endpointId.contains("statement") || endpointId.contains("balance")) return "Account Enquiries";
        if (endpointId.contains("payment")) return "Payment Processing";
        if (endpointId.contains("card")) return "Card Management";
        return "General";
    }

    private String getSampleRequestBody() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("customerId", "CUST123456");
        requestBody.put("accountType", "SAVINGS");
        requestBody.put("currency", "USD");
        requestBody.put("amount", 1000.00);
        requestBody.put("reference", "TEST_REF_001");
        return formatJson(requestBody);
    }

    private String getSearchResultTitle(int index, String query) {
        String[] titles = {"Create Account API", "Transaction Processing", "Balance Enquiry",
                "Payment Gateway", "Card Management", "Account Statements"};
        return titles[(index - 1) % titles.length] + " - " + query;
    }

    private String getSearchResultType(int index) {
        String[] types = {"Endpoint", "Collection", "Folder", "Parameter", "Response", "Example"};
        return types[(index - 1) % types.length];
    }

    private String getSearchResultCategory(int index) {
        String[] categories = {"Accounts", "Transactions", "Payments", "Enquiries", "Cards", "Security"};
        return categories[(index - 1) % categories.length];
    }

    private String getSearchResultDescription(int index, String query) {
        return "Documentation related to '" + query + "' in " + getSearchResultCategory(index) + " category";
    }

    private String getSearchResultEndpoint(int index) {
        String[] endpoints = {"accounts", "transactions/deposit", "accounts/{id}/balance",
                "payments/bill", "cards", "security/auth"};
        return endpoints[(index - 1) % endpoints.length];
    }

    private String getNotificationIcon(String type) {
        switch (type) {
            case "warning":
                return "alert-triangle";
            case "success":
                return "check-circle";
            case "error":
                return "x-circle";
            default:
                return "info";
        }
    }

    private String getMockResponseBody(String endpointId, int statusCode) {
        if (statusCode >= 200 && statusCode < 300) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", "MOCK_" + UUID.randomUUID().toString().substring(0, 8));
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            response.put("message", "Mock response for " + endpointId);
            return formatJson(response);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("code", "MOCK_ERROR");
            errorDetails.put("message", "Mock error response for testing");
            errorDetails.put("endpoint", endpointId);
            errorResponse.put("error", errorDetails);
            return formatJson(errorResponse);
        }
    }

    // ========== FALLBACK METHODS ==========

    private APICollectionResponse getFallbackAPICollections() {
        List<APICollectionDto> collections = new ArrayList<>();

        APICollectionDto fallback = new APICollectionDto();
        fallback.setId("fintech-core");
        fallback.setName("FinTech Core Banking API");
        fallback.setDescription("Core banking operations");
        fallback.setVersion("v2.1");
        fallback.setOwner("FinTech Core Team");
        fallback.setType("REST");
        fallback.setFavorite(true);
        fallback.setExpanded(true);
        fallback.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        collections.add(fallback);
        return new APICollectionResponse(collections);
    }

    private EndpointDetailResponse getFallbackEndpointDetails(String endpointId) {
        EndpointDetailResponse details = new EndpointDetailResponse();
        details.setEndpointId(endpointId);
        details.setName("Fallback Endpoint");
        details.setMethod("GET");
        details.setUrl("https://api.fintech.com/v2.1/api");
        details.setDescription("Fallback endpoint details");
        details.setCategory("General");
        details.setTags(Arrays.asList("fallback", "api"));
        details.setLastModified(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setVersion("v1.0");
        details.setRequiresAuthentication(true);
        details.setRateLimit("10 requests/minute");
        details.setDeprecated(false);

        return details;
    }

    // ========== UTILITY METHODS ==========

    private String formatJson(Map<String, Object> data) {
        // Simple JSON formatter for sample data
        // In production, use Jackson or Gson
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        int i = 0;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            sb.append("  \"").append(entry.getKey()).append("\": ");
            if (entry.getValue() instanceof String) {
                sb.append("\"").append(entry.getValue()).append("\"");
            } else if (entry.getValue() instanceof Number) {
                sb.append(entry.getValue());
            } else if (entry.getValue() instanceof Boolean) {
                sb.append(entry.getValue());
            } else if (entry.getValue() instanceof Map) {
                sb.append("{}"); // Simplified
            } else if (entry.getValue() instanceof List) {
                sb.append("[]"); // Simplified
            } else if (entry.getValue() == null) {
                sb.append("null");
            } else {
                sb.append("\"").append(entry.getValue()).append("\"");
            }
            if (i < data.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
            i++;
        }
        sb.append("}");
        return sb.toString();
    }

    // ========== INNER CLASSES ==========

    private static class DocCache {
        private final Object data;
        private final long timestamp;

        public DocCache(Object data, long timestamp) {
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