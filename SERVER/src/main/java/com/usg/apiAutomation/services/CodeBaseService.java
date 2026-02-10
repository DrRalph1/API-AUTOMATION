package com.usg.apiAutomation.services;

import com.usg.apiAutomation.dtos.codeBase.*;
import com.usg.apiAutomation.utils.LoggerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CodeBaseService {

    @Autowired
    private LoggerUtil loggerUtil;

    // ============================================================
    // 1. GET COLLECTIONS LIST
    // ============================================================
    public CollectionsListResponse getCollectionsList(String requestId, String performedBy) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Getting collections list for codebase");

            // Sample data - in real implementation, this would come from database
            CollectionsListResponse response = new CollectionsListResponse();

            List<CollectionItem> collections = Arrays.asList(
                    createCollectionItem("user-management", "User Management API",
                            "Complete user authentication, authorization, and profile management",
                            "v2.1", "API Script Team", "Today, 9:30 AM", true, true),
                    createCollectionItem("payment-api", "Payment Processing API",
                            "Secure payment processing with multiple payment methods",
                            "v1.5", "API Script Team", "1 week ago", false, true),
                    createCollectionItem("notification-api", "Notification API",
                            "Send email, SMS, and push notifications",
                            "v3.2", "API Script Team", "3 days ago", false, false),
                    createCollectionItem("inventory-api", "Inventory Management API",
                            "Track and manage product inventory",
                            "v1.0", "API Script Team", "2 weeks ago", false, true),
                    createCollectionItem("reporting-api", "Reporting API",
                            "Generate and manage reports",
                            "v2.3", "API Script Team", "Yesterday", true, false)
            );

            response.setCollections(collections);
            response.setTotal(collections.size());

            return response;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error getting collections list: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 2. GET COLLECTION DETAILS
    // ============================================================
    public CollectionDetailsResponse getCollectionDetails(String requestId, String performedBy, String collectionId) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Getting collection details for: " + collectionId);

            // Sample data - in real implementation, this would come from database
            CollectionDetailsResponse response = new CollectionDetailsResponse();

            if ("user-management".equals(collectionId)) {
                response.setId("user-management");
                response.setName("User Management API");
                response.setDescription("Complete user authentication, authorization, and profile management");
                response.setVersion("v2.1");
                response.setOwner("API Script Team");
                response.setCreatedAt("Jan 15, 2024");
                response.setUpdatedAt("Today, 9:30 AM");
                response.setIsFavorite(true);
                response.setIsExpanded(true);

                // Add folders with request counts
                List<CollectionFolder> folders = Arrays.asList(
                        createFolder("authentication", "Authentication",
                                "User registration, login, and token management", true, true, 8),
                        createFolder("profile-management", "Profile Management",
                                "User profile updates and management", true, false, 6),
                        createFolder("roles-permissions", "Roles & Permissions",
                                "Manage user roles and permissions", false, true, 5),
                        createFolder("security", "Security",
                                "Security and audit related endpoints", false, false, 3)
                );
                response.setFolders(folders);

            } else if ("payment-api".equals(collectionId)) {
                response.setId("payment-api");
                response.setName("Payment Processing API");
                response.setDescription("Secure payment processing with multiple payment methods");
                response.setVersion("v1.5");
                response.setOwner("API Script Team");
                response.setCreatedAt("Feb 1, 2024");
                response.setUpdatedAt("1 week ago");
                response.setIsFavorite(true);
                response.setIsExpanded(false);

                // Add folders
                List<CollectionFolder> folders = Arrays.asList(
                        createFolder("payments", "Payments",
                                "Process payments and transactions", false, true, 12),
                        createFolder("refunds", "Refunds",
                                "Handle payment refunds", false, false, 4),
                        createFolder("webhooks", "Webhooks",
                                "Payment webhook endpoints", false, true, 3)
                );
                response.setFolders(folders);

            } else {
                throw new RuntimeException("Collection not found: " + collectionId);
            }

            return response;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error getting collection details: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 3. GET FOLDER REQUESTS
    // ============================================================
    public FolderRequestsResponse getFolderRequests(String requestId, String performedBy,
                                                    String collectionId, String folderId) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Getting folder requests for folder: " + folderId + " in collection: " + collectionId);

            FolderRequestsResponse response = new FolderRequestsResponse();
            response.setCollectionId(collectionId);
            response.setFolderId(folderId);

            List<RequestItem> requests = new ArrayList<>();

            if ("authentication".equals(folderId) && "user-management".equals(collectionId)) {
                requests.add(createRequestItem("register-user", "Register User", "POST",
                        "Create a new user account with email and password",
                        Arrays.asList("auth", "register", "signup"), "Today, 9:00 AM"));
                requests.add(createRequestItem("login-user", "Login User", "POST",
                        "Authenticate user and return JWT token",
                        Arrays.asList("auth", "login", "authentication"), "Today, 9:15 AM"));
                requests.add(createRequestItem("logout-user", "Logout User", "POST",
                        "Invalidate user session and token",
                        Arrays.asList("auth", "logout", "session"), "Yesterday, 4:30 PM"));
                requests.add(createRequestItem("refresh-token", "Refresh Token", "POST",
                        "Refresh expired JWT token",
                        Arrays.asList("auth", "token", "refresh"), "2 days ago"));
                requests.add(createRequestItem("verify-email", "Verify Email", "POST",
                        "Verify user email address",
                        Arrays.asList("auth", "email", "verification"), "3 days ago"));
            } else if ("profile-management".equals(folderId) && "user-management".equals(collectionId)) {
                requests.add(createRequestItem("get-user-profile", "Get User Profile", "GET",
                        "Retrieve user profile information",
                        Arrays.asList("users", "profile", "read"), "Today, 10:30 AM"));
                requests.add(createRequestItem("update-user-profile", "Update User Profile", "PUT",
                        "Update user profile information",
                        Arrays.asList("users", "profile", "update"), "Today, 11:45 AM"));
                requests.add(createRequestItem("change-password", "Change Password", "POST",
                        "Change user password",
                        Arrays.asList("users", "security", "password"), "Yesterday, 2:15 PM"));
            } else if ("payments".equals(folderId) && "payment-api".equals(collectionId)) {
                requests.add(createRequestItem("create-payment", "Create Payment", "POST",
                        "Create a new payment transaction",
                        Arrays.asList("payments", "transaction", "create"), "Today, 8:30 AM"));
                requests.add(createRequestItem("get-payment", "Get Payment Details", "GET",
                        "Retrieve payment details by ID",
                        Arrays.asList("payments", "transaction", "read"), "Today, 9:45 AM"));
                requests.add(createRequestItem("list-payments", "List Payments", "GET",
                        "List all payments with filtering",
                        Arrays.asList("payments", "transaction", "list"), "Yesterday, 3:20 PM"));
            }

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

            // Sample data - in real implementation, this would come from database
            RequestDetailsResponse response = new RequestDetailsResponse();

            if ("register-user".equals(requestIdParam)) {
                response.setId("register-user");
                response.setName("Register User");
                response.setMethod("POST");
                response.setUrl("https://api.example.com/v2.1/users/register");
                response.setDescription("Create a new user account with email and password");
                response.setLastModified("Today, 9:00 AM");
                response.setCollectionId("user-management");
                response.setFolderId("authentication");

                // Add tags
                response.setTags(Arrays.asList("auth", "register", "signup"));

                // Add headers
                List<HeaderItem> headers = Arrays.asList(
                        HeaderItem.builder()
                                .key("Content-Type")
                                .value("application/json")
                                .build(),
                        HeaderItem.builder()
                                .key("Accept")
                                .value("application/json")
                                .build()
                );
                response.setHeaders(headers);

                // Add request body
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("email", "user@example.com");
                requestBody.put("password", "SecurePass123!");
                requestBody.put("firstName", "John");
                requestBody.put("lastName", "Doe");
                requestBody.put("phoneNumber", "+1234567890");
                response.setBody(requestBody);

                // Add response examples
                Map<String, Object> responseExample = new HashMap<>();
                responseExample.put("success", true);
                responseExample.put("message", "User registered successfully");
                responseExample.put("data", Map.of(
                        "id", "usr_123456",
                        "email", "user@example.com",
                        "firstName", "John",
                        "lastName", "Doe"
                ));
                response.setResponseExample(responseExample);

                // Add implementations
                Map<String, Map<String, String>> implementations = new HashMap<>();
                implementations.put("java", createJavaImplementation());
                implementations.put("javascript", createJavascriptImplementation());
                implementations.put("python", createPythonImplementation());
                implementations.put("csharp", createCSharpImplementation());
                response.setImplementations(implementations);

            } else if ("get-user-profile".equals(requestIdParam)) {
                response.setId("get-user-profile");
                response.setName("Get User Profile");
                response.setMethod("GET");
                response.setUrl("https://api.example.com/v2.1/users/{id}");
                response.setDescription("Retrieve user profile information by ID");
                response.setLastModified("Today, 10:30 AM");
                response.setCollectionId("user-management");
                response.setFolderId("profile-management");

                // Add tags
                response.setTags(Arrays.asList("users", "profile", "read"));

                // Add headers
                List<HeaderItem> headers = Arrays.asList(
                        HeaderItem.builder()
                                .key("Content-Type")
                                .value("application/json")
                                .build(),
                        HeaderItem.builder()
                                .key("Accept")
                                .value("application/json")
                                .build()
                );
                response.setHeaders(headers);

                // Add path parameters
                List<ParameterItem> pathParams = Arrays.asList(
                        ParameterItem.builder()
                                .name("id")
                                .type("string")
                                .required(true)
                                .description("User ID")
                                .build()
                );
                response.setPathParameters(pathParams);

                // Add implementations
                Map<String, Map<String, String>> implementations = new HashMap<>();
                implementations.put("java", createJavaGetUserImplementation());
                implementations.put("javascript", createJavascriptGetUserImplementation());
                response.setImplementations(implementations);

            } else {
                throw new RuntimeException("Request not found: " + requestIdParam);
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

            ImplementationResponse response = new ImplementationResponse();
            response.setLanguage(language);
            response.setComponent(component);
            response.setRequestId(requestIdParam);
            response.setCollectionId(collectionId);
            response.setGeneratedAt(new Date());

            // Get the appropriate implementation based on language and component
            String code = getImplementationCode(language, component, requestIdParam);
            response.setCode(code);
            response.setFileName(getFileName(component, language));
            response.setFileSize((long) code.length());
            response.setLinesOfCode(code.split("\n").length);

            // Add language info
            Map<String, Object> languageInfo = getLanguageInfo(language);
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

            AllImplementationsResponse response = new AllImplementationsResponse();
            response.setRequestId(requestIdParam);
            response.setCollectionId(collectionId);
            response.setRetrievedAt(new Date());

            // Get implementations for all languages
            Map<String, Map<String, String>> implementations = new HashMap<>();

            if ("register-user".equals(requestIdParam)) {
                implementations.put("java", createJavaImplementation());
                implementations.put("javascript", createJavascriptImplementation());
                implementations.put("python", createPythonImplementation());
                implementations.put("csharp", createCSharpImplementation());
                implementations.put("php", createPhpImplementation());
            } else if ("get-user-profile".equals(requestIdParam)) {
                implementations.put("java", createJavaGetUserImplementation());
                implementations.put("javascript", createJavascriptGetUserImplementation());
                implementations.put("python", createPythonGetUserImplementation());
            }

            response.setImplementations(implementations);
            response.setTotalLanguages(implementations.size());

            // Calculate total files
            int totalFiles = implementations.values().stream()
                    .mapToInt(Map::size)
                    .sum();
            response.setTotalFiles(totalFiles);

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
    public GenerateImplementationResponse generateImplementation(String requestId, String performedBy,
                                                                 GenerateImplementationRequest requestDto) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Generating implementation for request: " + requestDto.getRequestId() +
                    ", language: " + requestDto.getLanguage());

            // Simulate generation process
            GenerateImplementationResponse response = new GenerateImplementationResponse();
            response.setRequestId(requestDto.getRequestId());
            response.setLanguage(requestDto.getLanguage());
            response.setGeneratedAt(new Date());
            response.setStatus("generated");

            // Generate code based on request details
            Map<String, String> implementations = generateCodeForRequest(requestDto);
            response.setImplementations(implementations);

            // Add quick start guide
            response.setQuickStartGuide(getQuickStartGuide(requestDto.getLanguage()));

            // Add features
            response.setFeatures(getFeatures());

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
    // 8. EXPORT IMPLEMENTATION
    // ============================================================
    public ExportResponse exportImplementation(String requestId, String performedBy,
                                               ExportRequest exportRequest) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Exporting implementation in format: " + exportRequest.getFormat());

            ExportResponse response = new ExportResponse();
            response.setFormat(exportRequest.getFormat());
            response.setLanguage(exportRequest.getLanguage());
            response.setExportedAt(new Date());
            response.setStatus("ready");

            // Generate export data
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("packageName", "api-implementation-" + exportRequest.getLanguage());
            exportData.put("version", "1.0.0");
            exportData.put("filesCount", getFilesCount(exportRequest.getLanguage()));
            exportData.put("downloadUrl", "/downloads/" + UUID.randomUUID() + ".zip");
            exportData.put("downloadId", UUID.randomUUID().toString());
            exportData.put("expiresAt", new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)); // 24 hours

            if ("complete".equals(exportRequest.getFormat())) {
                exportData.put("includes", Arrays.asList("source", "config", "tests", "documentation", "docker"));
                exportData.put("totalSize", "5.2 MB");
            } else if ("single".equals(exportRequest.getFormat())) {
                exportData.put("includes", Arrays.asList("source"));
                exportData.put("totalSize", "1.8 MB");
            } else if ("minimal".equals(exportRequest.getFormat())) {
                exportData.put("includes", Arrays.asList("controller", "service"));
                exportData.put("totalSize", "0.8 MB");
            }

            response.setExportData(exportData);

            return response;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error exporting implementation: " + e.getMessage());
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

            LanguagesResponse response = new LanguagesResponse();

            List<LanguageInfo> languages = Arrays.asList(
                    createLanguageInfo("java", "Java", "Spring Boot", "#f89820", "coffee"),
                    createLanguageInfo("javascript", "JavaScript", "Node.js/Express", "#f0db4f", "file-code"),
                    createLanguageInfo("python", "Python", "FastAPI/Django", "#3776ab", "code"),
                    createLanguageInfo("csharp", "C#", ".NET Core", "#9b4993", "box"),
                    createLanguageInfo("php", "PHP", "Laravel", "#777bb4", "package"),
                    createLanguageInfo("go", "Go", "Gin", "#00add8", "terminal"),
                    createLanguageInfo("ruby", "Ruby", "Ruby on Rails", "#cc342d", "server"),
                    createLanguageInfo("kotlin", "Kotlin", "Ktor/Spring", "#7f52ff", "cpu"),
                    createLanguageInfo("swift", "Swift", "Vapor", "#f05138", "monitor"),
                    createLanguageInfo("rust", "Rust", "Actix-web", "#dea584", "hard-drive")
            );

            response.setLanguages(languages);
            response.setTotal(languages.size());

            return response;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error getting languages: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 10. SEARCH IMPLEMENTATIONS
    // ============================================================
    public SearchResponse searchImplementations(String requestId, String performedBy,
                                                SearchRequest searchRequest) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Searching implementations for query: " + searchRequest.getQuery());

            SearchResponse response = new SearchResponse();
            response.setQuery(searchRequest.getQuery());
            response.setSearchAt(new Date());

            // Sample search results - in real implementation, this would search database
            List<SearchResult> results = new ArrayList<>();

            if (searchRequest.getQuery().toLowerCase().contains("user") ||
                    searchRequest.getQuery().toLowerCase().contains("auth")) {
                results.add(createSearchResult("register-user", "Register User",
                        "User Management API", "authentication", "POST",
                        Arrays.asList("java", "javascript", "python", "csharp"),
                        "Today, 9:00 AM", 4));
                results.add(createSearchResult("login-user", "Login User",
                        "User Management API", "authentication", "POST",
                        Arrays.asList("java", "javascript", "python"),
                        "Today, 9:15 AM", 3));
                results.add(createSearchResult("get-user-profile", "Get User Profile",
                        "User Management API", "profile-management", "GET",
                        Arrays.asList("java", "javascript"),
                        "Today, 10:30 AM", 2));
            }

            if (searchRequest.getQuery().toLowerCase().contains("payment")) {
                results.add(createSearchResult("create-payment", "Create Payment",
                        "Payment Processing API", "payments", "POST",
                        Arrays.asList("java", "javascript", "csharp", "python"),
                        "Today, 8:30 AM", 4));
                results.add(createSearchResult("get-payment", "Get Payment Details",
                        "Payment Processing API", "payments", "GET",
                        Arrays.asList("java", "javascript"),
                        "Today, 9:45 AM", 2));
            }

            response.setResults(results);
            response.setTotal(results.size());

            return response;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error searching implementations: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 11. IMPORT SPECIFICATION
    // ============================================================
    public ImportSpecResponse importSpecification(String requestId, String performedBy,
                                                  ImportSpecRequest importRequest) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Importing specification from source: " + importRequest.getSource());

            ImportSpecResponse response = new ImportSpecResponse();
            response.setSource(importRequest.getSource());
            response.setImportedAt(new Date());
            response.setStatus("imported");

            // Process import based on source
            Map<String, Object> importData = new HashMap<>();

            switch (importRequest.getSource()) {
                case "openapi":
                    importData.put("format", "OpenAPI 3.0");
                    importData.put("endpoints", 15);
                    importData.put("schemas", 8);
                    importData.put("tags", Arrays.asList("auth", "users", "payments"));
                    break;
                case "postman":
                    importData.put("format", "Postman Collection");
                    importData.put("requests", 12);
                    importData.put("folders", 3);
                    importData.put("environments", 1);
                    break;
                case "github":
                    importData.put("format", "GitHub Repository");
                    importData.put("files", 25);
                    importData.put("languages", Arrays.asList("java", "javascript"));
                    break;
                default:
                    importData.put("format", "Custom");
            }

            String collectionId = "imported-" + UUID.randomUUID().toString().substring(0, 8);
            importData.put("collectionId", collectionId);
            importData.put("name", "Imported API Collection");
            importData.put("description", "Imported from " + importRequest.getSource());

            response.setImportData(importData);

            return response;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error importing specification: " + e.getMessage());
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
            response.setValid(true);

            // Simulate validation
            List<ValidationIssue> issues = new ArrayList<>();

            if (validationRequest.getCode() != null && validationRequest.getCode().contains("TODO")) {
                issues.add(ValidationIssue.builder()
                        .type("warning")
                        .message("Found TODO comment")
                        .line(validationRequest.getCode().split("\n").length - 1)
                        .severity("low")
                        .build());
            }

            if (validationRequest.getCode() != null && validationRequest.getCode().length() < 100) {
                issues.add(ValidationIssue.builder()
                        .type("suggestion")
                        .message("Consider adding more error handling")
                        .severity("info")
                        .build());
            }

            response.setIssues(issues);
            response.setScore(issues.isEmpty() ? 100 : 85);

            return response;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error validating implementation: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 13. TEST IMPLEMENTATION
    // ============================================================
    public TestResponse testImplementation(String requestId, String performedBy,
                                           TestImplementationRequest testRequest) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Testing implementation for request: " + testRequest.getRequestId());

            TestResponse response = new TestResponse();
            response.setRequestId(testRequest.getRequestId());
            response.setLanguage(testRequest.getLanguage());
            response.setTestedAt(new Date());

            // Simulate test results
            List<TestResult> testResults = Arrays.asList(
                    TestResult.builder()
                            .name("Authentication Test")
                            .status("PASSED")
                            .duration("0.8s")
                            .message("User authentication successful")
                            .build(),
                    TestResult.builder()
                            .name("Validation Test")
                            .status("PASSED")
                            .duration("0.3s")
                            .message("Input validation passed")
                            .build(),
                    TestResult.builder()
                            .name("Database Test")
                            .status("PASSED")
                            .duration("1.2s")
                            .message("Database operations successful")
                            .build(),
                    TestResult.builder()
                            .name("Error Handling Test")
                            .status("PASSED")
                            .duration("0.5s")
                            .message("Error responses correct")
                            .build(),
                    TestResult.builder()
                            .name("Performance Test")
                            .status("PASSED")
                            .duration("2.1s")
                            .message("Response time under threshold")
                            .build()
            );

            response.setTestResults(testResults);
            response.setTestsPassed(5);
            response.setTestsFailed(0);
            response.setTotalTests(5);
            response.setCoverage("95%");
            response.setExecutionTime("5.9s");
            response.setStatus("PASSED");

            return response;

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error testing implementation: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 14. CLEAR CACHE
    // ============================================================
    public void clearCache(String requestId, String performedBy) {
        try {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Clearing codebase cache for user: " + performedBy);

            // In real implementation, clear cache here
            // cacheManager.clearCodebaseCache(performedBy);

        } catch (Exception e) {
            loggerUtil.log("codebase", "Request ID: " + requestId +
                    ", Error clearing cache: " + e.getMessage());
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

            Map<String, Object> response = new HashMap<>();

            List<Map<String, Object>> languages = Arrays.asList(
                    createLanguageDetail("java", "Java", "Spring Boot", "#f89820",
                            Arrays.asList("controller", "service", "repository", "model", "dto"),
                            ".java", "Maven/Gradle"),
                    createLanguageDetail("javascript", "JavaScript", "Node.js/Express", "#f0db4f",
                            Arrays.asList("controller", "service", "model", "routes", "middleware"),
                            ".js", "npm/yarn"),
                    createLanguageDetail("python", "Python", "FastAPI/Django", "#3776ab",
                            Arrays.asList("fastapi", "schemas", "models", "routes", "services"),
                            ".py", "pip/poetry"),
                    createLanguageDetail("csharp", "C#", ".NET Core", "#9b4993",
                            Arrays.asList("controller", "service", "model", "repository", "dto"),
                            ".cs", "NuGet"),
                    createLanguageDetail("php", "PHP", "Laravel", "#777bb4",
                            Arrays.asList("controller", "service", "model", "migration"),
                            ".php", "Composer")
            );

            response.put("languages", languages);
            response.put("total", languages.size());
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

            Map<String, Object> guide = new HashMap<>();
            guide.put("language", language);
            guide.put("generatedAt", new Date());

            switch (language) {
                case "java":
                    guide.put("steps", Arrays.asList(
                            Map.of("number", 1, "title", "Install JDK", "command", "brew install openjdk@17", "description", "Install Java Development Kit 17"),
                            Map.of("number", 2, "title", "Install Maven", "command", "brew install maven", "description", "Install Maven build tool"),
                            Map.of("number", 3, "title", "Build Project", "command", "mvn clean install", "description", "Build the Spring Boot application"),
                            Map.of("number", 4, "title", "Run Application", "command", "mvn spring-boot:run", "description", "Start the Spring Boot server"),
                            Map.of("number", 5, "title", "Test API", "command", "curl http://localhost:8080/api/v1/users", "description", "Test the API endpoints")
                    ));
                    break;
                case "javascript":
                    guide.put("steps", Arrays.asList(
                            Map.of("number", 1, "title", "Install Node.js", "command", "brew install node", "description", "Install Node.js runtime"),
                            Map.of("number", 2, "title", "Install Dependencies", "command", "npm install", "description", "Install project dependencies"),
                            Map.of("number", 3, "title", "Start Server", "command", "npm start", "description", "Start the Express.js server"),
                            Map.of("number", 4, "title", "Test API", "command", "curl http://localhost:3000/api/v1/users", "description", "Test the API endpoints")
                    ));
                    break;
                case "python":
                    guide.put("steps", Arrays.asList(
                            Map.of("number", 1, "title", "Install Python", "command", "brew install python", "description", "Install Python 3.9+"),
                            Map.of("number", 2, "title", "Create Virtual Environment", "command", "python -m venv venv", "description", "Create virtual environment"),
                            Map.of("number", 3, "title", "Activate Virtual Environment", "command", "source venv/bin/activate", "description", "Activate the virtual environment"),
                            Map.of("number", 4, "title", "Install Dependencies", "command", "pip install -r requirements.txt", "description", "Install required packages"),
                            Map.of("number", 5, "title", "Run FastAPI Server", "command", "uvicorn main:app --reload", "description", "Start the FastAPI server"),
                            Map.of("number", 6, "title", "Access API Docs", "command", "open http://localhost:8000/docs", "description", "Open Swagger documentation")
                    ));
                    break;
                default:
                    guide.put("steps", Arrays.asList(
                            Map.of("number", 1, "title", "Install Dependencies", "command", "See language-specific instructions", "description", "Install required dependencies"),
                            Map.of("number", 2, "title", "Build Project", "command", "Follow build instructions", "description", "Build the project"),
                            Map.of("number", 3, "title", "Run Application", "command", "Start the server", "description", "Run the application"),
                            Map.of("number", 4, "title", "Test Endpoints", "command", "Test with curl or Postman", "description", "Verify the API works")
                    ));
            }

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

    private CollectionItem createCollectionItem(String id, String name, String description,
                                                String version, String owner, String updatedAt,
                                                boolean isExpanded, boolean isFavorite) {
        CollectionItem item = new CollectionItem();
        item.setId(id);
        item.setName(name);
        item.setDescription(description);
        item.setVersion(version);
        item.setOwner(owner);
        item.setUpdatedAt(updatedAt);
        item.setIsExpanded(isExpanded);
        item.setIsFavorite(isFavorite);
        return item;
    }

    private CollectionFolder createFolder(String id, String name, String description,
                                          boolean isExpanded, boolean hasRequests, int requestCount) {
        CollectionFolder folder = new CollectionFolder();
        folder.setId(id);
        folder.setName(name);
        folder.setDescription(description);
        folder.setIsExpanded(isExpanded);
        folder.setHasRequests(hasRequests);
        folder.setRequestCount(requestCount);
        return folder;
    }

    private RequestItem createRequestItem(String id, String name, String method, String description,
                                          List<String> tags, String lastModified) {
        RequestItem item = new RequestItem();
        item.setId(id);
        item.setName(name);
        item.setMethod(method);
        item.setDescription(description);
        item.setTags(tags);
        item.setLastModified(lastModified);
        return item;
    }

    private Map<String, String> createJavaImplementation() {
        Map<String, String> implementation = new HashMap<>();
        implementation.put("controller", getJavaControllerCode());
        implementation.put("service", getJavaServiceCode());
        implementation.put("repository", getJavaRepositoryCode());
        implementation.put("model", getJavaModelCode());
        implementation.put("dto", getJavaDtoCode());
        return implementation;
    }

    private Map<String, String> createJavascriptImplementation() {
        Map<String, String> implementation = new HashMap<>();
        implementation.put("controller", getJavascriptControllerCode());
        implementation.put("service", getJavascriptServiceCode());
        implementation.put("model", getJavascriptModelCode());
        implementation.put("routes", getJavascriptRoutesCode());
        return implementation;
    }

    private Map<String, String> createPythonImplementation() {
        Map<String, String> implementation = new HashMap<>();
        implementation.put("fastapi", getPythonFastAPICode());
        implementation.put("schemas", getPythonSchemasCode());
        implementation.put("models", getPythonModelsCode());
        implementation.put("services", getPythonServicesCode());
        return implementation;
    }

    private Map<String, String> createCSharpImplementation() {
        Map<String, String> implementation = new HashMap<>();
        implementation.put("controller", getCSharpControllerCode());
        implementation.put("service", getCSharpServiceCode());
        implementation.put("model", getCSharpModelCode());
        return implementation;
    }

    private Map<String, String> createPhpImplementation() {
        Map<String, String> implementation = new HashMap<>();
        implementation.put("controller", getPhpControllerCode());
        implementation.put("service", getPhpServiceCode());
        implementation.put("model", getPhpModelCode());
        return implementation;
    }

    private Map<String, String> createJavaGetUserImplementation() {
        Map<String, String> implementation = new HashMap<>();
        implementation.put("controller",
                "package com.example.api.controller;\n\n" +
                        "import com.example.api.dto.UserResponse;\n" +
                        "import com.example.api.service.UserService;\n" +
                        "import lombok.RequiredArgsConstructor;\n" +
                        "import org.springframework.http.ResponseEntity;\n" +
                        "import org.springframework.web.bind.annotation.*;\n\n" +
                        "@RestController\n" +
                        "@RequestMapping(\"/api/v1/users\")\n" +
                        "@RequiredArgsConstructor\n" +
                        "public class UserController {\n\n" +
                        "    private final UserService userService;\n\n" +
                        "    @GetMapping(\"/{id}\")\n" +
                        "    public ResponseEntity<UserResponse> getUser(@PathVariable String id) {\n" +
                        "        UserResponse user = userService.getUserById(id);\n" +
                        "        return ResponseEntity.ok(user);\n" +
                        "    }\n" +
                        "}");
        return implementation;
    }

    private Map<String, String> createJavascriptGetUserImplementation() {
        Map<String, String> implementation = new HashMap<>();
        implementation.put("controller",
                "// controllers/userController.js\n" +
                        "const userService = require('../services/userService');\n\n" +
                        "exports.getUser = async (req, res) => {\n" +
                        "  try {\n" +
                        "    const user = await userService.getUserById(req.params.id);\n" +
                        "    res.json({\n" +
                        "      success: true,\n" +
                        "      data: user\n" +
                        "    });\n" +
                        "  } catch (error) {\n" +
                        "    res.status(404).json({\n" +
                        "      success: false,\n" +
                        "      message: error.message\n" +
                        "    });\n" +
                        "  }\n" +
                        "};");
        return implementation;
    }

    private Map<String, String> createPythonGetUserImplementation() {
        Map<String, String> implementation = new HashMap<>();
        implementation.put("fastapi",
                "# main.py\n" +
                        "from fastapi import FastAPI, HTTPException\n" +
                        "from pydantic import BaseModel\n\n" +
                        "app = FastAPI()\n\n" +
                        "class UserResponse(BaseModel):\n" +
                        "    id: str\n" +
                        "    email: str\n    firstName: str\n    lastName: str\n\n" +
                        "@app.get(\"/api/v1/users/{user_id}\")\n" +
                        "async def get_user(user_id: str):\n" +
                        "    # Implementation here\n" +
                        "    return UserResponse(id=user_id, email=\"user@example.com\", \n" +
                        "                       firstName=\"John\", lastName=\"Doe\")");
        return implementation;
    }

    private String getImplementationCode(String language, String component, String requestId) {
        // This would fetch from database in real implementation
        switch (language) {
            case "java":
                switch (component) {
                    case "controller": return getJavaControllerCode();
                    case "service": return getJavaServiceCode();
                    case "repository": return getJavaRepositoryCode();
                    case "model": return getJavaModelCode();
                    case "dto": return getJavaDtoCode();
                    default: return "// Java " + component + " implementation";
                }
            case "javascript":
                switch (component) {
                    case "controller": return getJavascriptControllerCode();
                    case "service": return getJavascriptServiceCode();
                    case "model": return getJavascriptModelCode();
                    case "routes": return getJavascriptRoutesCode();
                    default: return "// JavaScript " + component + " implementation";
                }
            case "python":
                switch (component) {
                    case "fastapi": return getPythonFastAPICode();
                    case "schemas": return getPythonSchemasCode();
                    case "models": return getPythonModelsCode();
                    case "services": return getPythonServicesCode();
                    default: return "# Python " + component + " implementation";
                }
            default:
                return "// Implementation for " + language + " " + component;
        }
    }

    private String getJavaControllerCode() {
        return "package com.example.api.controller;\n\n" +
                "import com.example.api.dto.*;\n" +
                "import com.example.api.service.UserService;\n" +
                "import lombok.RequiredArgsConstructor;\n" +
                "import org.springframework.http.ResponseEntity;\n" +
                "import org.springframework.web.bind.annotation.*;\n" +
                "import jakarta.validation.Valid;\n\n" +
                "@RestController\n" +
                "@RequestMapping(\"/api/v1/users\")\n" +
                "@RequiredArgsConstructor\n" +
                "public class UserController {\n\n" +
                "    private final UserService userService;\n\n" +
                "    @PostMapping(\"/register\")\n" +
                "    public ResponseEntity<ApiResponse<UserResponse>> registerUser(\n" +
                "            @Valid @RequestBody UserRegistrationRequest request) {\n" +
                "        UserResponse user = userService.registerUser(request);\n" +
                "        return ResponseEntity.ok(ApiResponse.success(user, \"User registered successfully\"));\n" +
                "    }\n" +
                "}";
    }

    private String getJavaServiceCode() {
        return "package com.example.api.service;\n\n" +
                "import com.example.api.dto.*;\n" +
                "import com.example.api.model.User;\n" +
                "import com.example.api.repository.UserRepository;\n" +
                "import lombok.RequiredArgsConstructor;\n" +
                "import org.springframework.security.crypto.password.PasswordEncoder;\n" +
                "import org.springframework.stereotype.Service;\n" +
                "import org.springframework.transaction.annotation.Transactional;\n\n" +
                "@Service\n" +
                "@RequiredArgsConstructor\n" +
                "public class UserService {\n\n" +
                "    private final UserRepository userRepository;\n" +
                "    private final PasswordEncoder passwordEncoder;\n\n" +
                "    @Transactional\n" +
                "    public UserResponse registerUser(UserRegistrationRequest request) {\n" +
                "        // Implementation here\n" +
                "        return new UserResponse();\n" +
                "    }\n" +
                "}";
    }

    private String getJavascriptControllerCode() {
        return "// controllers/userController.js\n" +
                "const userService = require('../services/userService');\n\n" +
                "exports.registerUser = async (req, res) => {\n" +
                "  try {\n" +
                "    const user = await userService.registerUser(req.body);\n" +
                "    res.status(201).json({\n" +
                "      success: true,\n" +
                "      message: 'User registered successfully',\n" +
                "      data: user\n" +
                "    });\n" +
                "  } catch (error) {\n" +
                "    res.status(400).json({\n" +
                "      success: false,\n" +
                "      message: error.message\n" +
                "    });\n" +
                "  }\n" +
                "};";
    }

    private String getPythonFastAPICode() {
        return "# main.py\n" +
                "from fastapi import FastAPI, HTTPException\n" +
                "from pydantic import BaseModel\n\n" +
                "app = FastAPI()\n\n" +
                "class UserCreate(BaseModel):\n" +
                "    email: str\n    password: str\n    firstName: str\n    lastName: str\n\n" +
                "@app.post(\"/api/v1/users/register\")\n" +
                "async def register_user(user_data: UserCreate):\n" +
                "    # Implementation here\n" +
                "    return {\"message\": \"User registered successfully\"}";
    }

    private String getFileName(String component, String language) {
        Map<String, String> extensions = Map.of(
                "java", ".java",
                "javascript", ".js",
                "python", ".py",
                "csharp", ".cs",
                "php", ".php",
                "go", ".go",
                "ruby", ".rb",
                "kotlin", ".kt",
                "swift", ".swift",
                "rust", ".rs"
        );

        Map<String, String> componentNames = Map.ofEntries(
                Map.entry("controller", "UserController"),
                Map.entry("service", "UserService"),
                Map.entry("repository", "UserRepository"),
                Map.entry("model", "User"),
                Map.entry("dto", "UserDTO"),
                Map.entry("routes", "userRoutes"),
                Map.entry("config", "config"),
                Map.entry("server", "server"),
                Map.entry("fastapi", "main"),
                Map.entry("schemas", "schemas"),
                Map.entry("models", "models"),
                Map.entry("services", "user_service")
        );

        String componentName = componentNames.getOrDefault(component, component);
        String extension = extensions.getOrDefault(language, ".txt");

        return componentName + extension;
    }

    private String getFileExtension(String language) {
        Map<String, String> extensions = Map.of(
                "java", ".java",
                "javascript", ".js",
                "python", ".py",
                "csharp", ".cs",
                "php", ".php",
                "go", ".go",
                "ruby", ".rb",
                "kotlin", ".kt",
                "swift", ".swift",
                "rust", ".rs"
        );
        return extensions.getOrDefault(language, ".txt");
    }

    private String getFormatterName(String language) {
        Map<String, String> formatters = Map.of(
                "java", "Java",
                "javascript", "JavaScript",
                "python", "Python",
                "csharp", "C#",
                "php", "PHP",
                "go", "Go",
                "ruby", "Ruby",
                "kotlin", "Kotlin",
                "swift", "Swift",
                "rust", "Rust"
        );
        return formatters.getOrDefault(language, "Plain Text");
    }

    private Map<String, Object> getLanguageInfo(String language) {
        Map<String, Object> info = new HashMap<>();

        switch (language) {
            case "java":
                info.put("name", "Java");
                info.put("framework", "Spring Boot");
                info.put("color", "#f89820");
                info.put("icon", "coffee");
                info.put("command", "mvn spring-boot:run");
                info.put("packageManager", "Maven/Gradle");
                break;
            case "javascript":
                info.put("name", "JavaScript");
                info.put("framework", "Node.js/Express");
                info.put("color", "#f0db4f");
                info.put("icon", "file-code");
                info.put("command", "npm start");
                info.put("packageManager", "npm/yarn");
                break;
            case "python":
                info.put("name", "Python");
                info.put("framework", "FastAPI/Django");
                info.put("color", "#3776ab");
                info.put("icon", "code");
                info.put("command", "uvicorn main:app --reload");
                info.put("packageManager", "pip/poetry");
                break;
            default:
                info.put("name", language);
                info.put("framework", "N/A");
                info.put("color", "#64748b");
                info.put("icon", "code");
                info.put("command", "N/A");
                info.put("packageManager", "N/A");
        }

        return info;
    }

    private LanguageInfo createLanguageInfo(String id, String name, String framework, String color, String icon) {
        LanguageInfo language = new LanguageInfo();
        language.setId(id);
        language.setName(name);
        language.setFramework(framework);
        language.setColor(color);
        language.setIcon(icon);
        return language;
    }

    private Map<String, Object> createLanguageDetail(String id, String name, String framework, String color,
                                                     List<String> components, String extension, String packageManager) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", id);
        detail.put("name", name);
        detail.put("framework", framework);
        detail.put("color", color);
        detail.put("components", components);
        detail.put("extension", extension);
        detail.put("packageManager", packageManager);
        return detail;
    }

    private SearchResult createSearchResult(String id, String name, String collection,
                                            String folder, String method, List<String> languages,
                                            String lastModified, int implementations) {
        SearchResult result = new SearchResult();
        result.setId(id);
        result.setName(name);
        result.setCollection(collection);
        result.setFolder(folder);
        result.setMethod(method);
        result.setLanguages(languages);
        result.setLastModified(lastModified);
        result.setImplementations(implementations);
        return result;
    }

    private Map<String, String> generateCodeForRequest(GenerateImplementationRequest request) {
        // Generate code based on request details
        Map<String, String> implementations = new HashMap<>();

        switch (request.getLanguage()) {
            case "java":
                implementations.put("controller", getJavaControllerCode());
                implementations.put("service", getJavaServiceCode());
                implementations.put("repository", "// Generated Java repository...");
                implementations.put("model", "// Generated Java model...");
                break;
            case "javascript":
                implementations.put("controller", getJavascriptControllerCode());
                implementations.put("service", getJavascriptServiceCode());
                implementations.put("model", "// Generated JavaScript model...");
                break;
            default:
                implementations.put("main", "// Generated " + request.getLanguage() + " implementation...");
        }

        return implementations;
    }

    private Map<String, String> getQuickStartGuide(String language) {
        Map<String, String> guide = new HashMap<>();

        switch (language) {
            case "java":
                guide.put("step1", "mvn install");
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

    private List<String> getFeatures() {
        return Arrays.asList(
                "JWT Authentication",
                "Password Hashing",
                "Input Validation",
                "Clean Architecture",
                "Dependency Injection",
                "Repository Pattern",
                "Error Handling",
                "Logging",
                "Testing",
                "Documentation"
        );
    }

    private int getFilesCount(String language) {
        switch (language) {
            case "java": return 15;
            case "javascript": return 12;
            case "python": return 8;
            case "csharp": return 10;
            default: return 5;
        }
    }

    // Additional helper methods for other code snippets
    private String getJavaRepositoryCode() {
        return "package com.example.api.repository;\n\n" +
                "import com.example.api.model.User;\n" +
                "import org.springframework.data.jpa.repository.JpaRepository;\n" +
                "import org.springframework.stereotype.Repository;\n\n" +
                "@Repository\n" +
                "public interface UserRepository extends JpaRepository<User, String> {\n" +
                "    boolean existsByEmail(String email);\n" +
                "    User findByEmail(String email);\n" +
                "}";
    }

    private String getJavaModelCode() {
        return "package com.example.api.model;\n\n" +
                "import jakarta.persistence.*;\n" +
                "import lombok.*;\n" +
                "import org.hibernate.annotations.CreationTimestamp;\n" +
                "import org.hibernate.annotations.UpdateTimestamp;\n\n" +
                "import java.time.LocalDateTime;\n" +
                "import java.util.Set;\n\n" +
                "@Entity\n" +
                "@Table(name = \"users\")\n" +
                "@Getter\n" +
                "@Setter\n" +
                "@NoArgsConstructor\n" +
                "@AllArgsConstructor\n" +
                "@Builder\n" +
                "public class User {\n\n" +
                "    @Id\n" +
                "    @GeneratedValue(strategy = GenerationType.UUID)\n" +
                "    private String id;\n\n" +
                "    @Column(unique = true, nullable = false)\n" +
                "    private String email;\n\n" +
                "    @Column(nullable = false)\n" +
                "    private String password;\n\n" +
                "    private String firstName;\n    private String lastName;\n    private String phoneNumber;\n\n" +
                "    @CreationTimestamp\n" +
                "    private LocalDateTime createdAt;\n\n" +
                "    @UpdateTimestamp\n" +
                "    private LocalDateTime updatedAt;\n" +
                "}";
    }

    private String getJavaDtoCode() {
        return "package com.example.api.dto;\n\n" +
                "import jakarta.validation.constraints.Email;\n" +
                "import jakarta.validation.constraints.NotBlank;\n" +
                "import lombok.Data;\n\n" +
                "@Data\n" +
                "public class UserRegistrationRequest {\n\n" +
                "    @NotBlank\n    @Email\n    private String email;\n\n" +
                "    @NotBlank\n    private String password;\n\n" +
                "    @NotBlank\n    private String firstName;\n\n" +
                "    @NotBlank\n    private String lastName;\n\n" +
                "    private String phoneNumber;\n" +
                "}";
    }

    private String getJavascriptServiceCode() {
        return "// services/userService.js\n" +
                "const User = require('../models/User');\n" +
                "const bcrypt = require('bcrypt');\n\n" +
                "exports.registerUser = async (userData) => {\n" +
                "  try {\n" +
                "    const hashedPassword = await bcrypt.hash(userData.password, 10);\n" +
                "    const user = new User({\n" +
                "      email: userData.email,\n" +
                "      password: hashedPassword,\n" +
                "      firstName: userData.firstName,\n" +
                "      lastName: userData.lastName,\n" +
                "      phoneNumber: userData.phoneNumber\n" +
                "    });\n" +
                "    return await user.save();\n" +
                "  } catch (error) {\n" +
                "    throw error;\n" +
                "  }\n" +
                "};";
    }

    private String getJavascriptModelCode() {
        return "// models/User.js\n" +
                "const mongoose = require('mongoose');\n\n" +
                "const userSchema = new mongoose.Schema({\n" +
                "  email: { type: String, required: true, unique: true },\n" +
                "  password: { type: String, required: true },\n" +
                "  firstName: { type: String, required: true },\n" +
                "  lastName: { type: String, required: true },\n" +
                "  phoneNumber: { type: String },\n" +
                "  createdAt: { type: Date, default: Date.now },\n" +
                "  updatedAt: { type: Date, default: Date.now }\n" +
                "});\n\n" +
                "module.exports = mongoose.model('User', userSchema);";
    }

    private String getJavascriptRoutesCode() {
        return "// routes/userRoutes.js\n" +
                "const express = require('express');\n" +
                "const userController = require('../controllers/userController');\n\n" +
                "const router = express.Router();\n\n" +
                "router.post('/register', userController.registerUser);\n" +
                "router.post('/login', userController.login);\n" +
                "router.get('/:id', userController.getUser);\n\n" +
                "module.exports = router;";
    }

    private String getPythonSchemasCode() {
        return "# schemas.py\n" +
                "from pydantic import BaseModel, EmailStr\n\n" +
                "class UserCreate(BaseModel):\n" +
                "    email: EmailStr\n" +
                "    password: str\n" +
                "    firstName: str\n" +
                "    lastName: str\n" +
                "    phoneNumber: str | None = None\n\n" +
                "class UserResponse(BaseModel):\n" +
                "    id: str\n" +
                "    email: EmailStr\n" +
                "    firstName: str\n" +
                "    lastName: str\n" +
                "    phoneNumber: str | None\n" +
                "    createdAt: str\n" +
                "    updatedAt: str";
    }

    private String getCSharpControllerCode() {
        return "using Microsoft.AspNetCore.Mvc;\n" +
                "using System.Threading.Tasks;\n\n" +
                "namespace Api.Controllers\n" +
                "{\n" +
                "    [ApiController]\n" +
                "    [Route(\"api/v1/users\")]\n" +
                "    public class UserController : ControllerBase\n" +
                "    {\n" +
                "        private readonly IUserService _userService;\n\n" +
                "        public UserController(IUserService userService)\n" +
                "        {\n" +
                "            _userService = userService;\n" +
                "        }\n\n" +
                "        [HttpPost(\"register\")]\n" +
                "        public async Task<IActionResult> Register([FromBody] UserRegistrationRequest request)\n" +
                "        {\n" +
                "            var result = await _userService.RegisterUserAsync(request);\n" +
                "            return Ok(result);\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }

    private String getPhpControllerCode() {
        return "<?php\n\n" +
                "namespace App\\Http\\Controllers;\n\n" +
                "use Illuminate\\Http\\Request;\n" +
                "use App\\Services\\UserService;\n\n" +
                "class UserController extends Controller\n" +
                "{\n" +
                "    protected $userService;\n\n" +
                "    public function __construct(UserService $userService)\n" +
                "    {\n" +
                "        $this->userService = $userService;\n" +
                "    }\n\n" +
                "    public function register(Request $request)\n" +
                "    {\n" +
                "        $user = $this->userService->register($request->all());\n" +
                "        return response()->json([\n" +
                "            'success' => true,\n" +
                "            'data' => $user\n" +
                "        ], 201);\n" +
                "    }\n" +
                "}";
    }


    private String getPythonModelsCode() {
        return "# models.py\n" +
                "from sqlalchemy import Column, String, DateTime\n" +
                "from sqlalchemy.ext.declarative import declarative_base\n" +
                "from sqlalchemy.sql import func\n\n" +
                "Base = declarative_base()\n\n" +
                "class User(Base):\n" +
                "    __tablename__ = 'users'\n\n" +
                "    id = Column(String, primary_key=True, index=True)\n" +
                "    email = Column(String, unique=True, index=True, nullable=False)\n" +
                "    password = Column(String, nullable=False)\n" +
                "    firstName = Column(String, nullable=False)\n" +
                "    lastName = Column(String, nullable=False)\n" +
                "    phoneNumber = Column(String)\n" +
                "    createdAt = Column(DateTime(timezone=True), server_default=func.now())\n" +
                "    updatedAt = Column(DateTime(timezone=True), onupdate=func.now())";
    }

    private String getPythonServicesCode() {
        return "# services/user_service.py\n" +
                "from sqlalchemy.orm import Session\n" +
                "from . import models, schemas\n" +
                "import bcrypt\n\n" +
                "class UserService:\n" +
                "    @staticmethod\n" +
                "    async def register_user(db: Session, user_data: schemas.UserCreate):\n" +
                "        # Hash password\n" +
                "        hashed_password = bcrypt.hashpw(user_data.password.encode(), bcrypt.gensalt())\n" +
                "        \n" +
                "        db_user = models.User(\n" +
                "            email=user_data.email,\n" +
                "            password=hashed_password.decode(),\n" +
                "            firstName=user_data.firstName,\n" +
                "            lastName=user_data.lastName,\n" +
                "            phoneNumber=user_data.phoneNumber\n" +
                "        )\n" +
                "        \n" +
                "        db.add(db_user)\n" +
                "        db.commit()\n" +
                "        db.refresh(db_user)\n" +
                "        \n" +
                "        return db_user";
    }

    private String getCSharpServiceCode() {
        return "using System.Threading.Tasks;\n" +
                "using Api.Models;\n" +
                "using Api.Repositories;\n\n" +
                "namespace Api.Services\n" +
                "{\n" +
                "    public interface IUserService\n" +
                "    {\n" +
                "        Task<User> RegisterUserAsync(UserRegistrationRequest request);\n" +
                "    }\n\n" +
                "    public class UserService : IUserService\n" +
                "    {\n" +
                "        private readonly IUserRepository _userRepository;\n\n" +
                "        public UserService(IUserRepository userRepository)\n" +
                "        {\n" +
                "            _userRepository = userRepository;\n" +
                "        }\n\n" +
                "        public async Task<User> RegisterUserAsync(UserRegistrationRequest request)\n" +
                "        {\n" +
                "            // Implementation here\n" +
                "            return await _userRepository.CreateUserAsync(request);\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }

    private String getCSharpModelCode() {
        return "using System;\n\n" +
                "namespace Api.Models\n" +
                "{\n" +
                "    public class User\n" +
                "    {\n" +
                "        public string Id { get; set; }\n" +
                "        public string Email { get; set; }\n" +
                "        public string Password { get; set; }\n" +
                "        public string FirstName { get; set; }\n" +
                "        public string LastName { get; set; }\n" +
                "        public string PhoneNumber { get; set; }\n" +
                "        public DateTime CreatedAt { get; set; }\n" +
                "        public DateTime UpdatedAt { get; set; }\n" +
                "    }\n\n" +
                "    public class UserRegistrationRequest\n" +
                "    {\n" +
                "        public string Email { get; set; }\n" +
                "        public string Password { get; set; }\n" +
                "        public string FirstName { get; set; }\n" +
                "        public string LastName { get; set; }\n" +
                "        public string PhoneNumber { get; set; }\n" +
                "    }\n" +
                "}";
    }

    private String getPhpServiceCode() {
        return "<?php\n\n" +
                "namespace App\\Services;\n\n" +
                "use App\\Models\\User;\n" +
                "use Illuminate\\Support\\Facades\\Hash;\n\n" +
                "class UserService\n" +
                "{\n" +
                "    public function register(array $data)\n" +
                "    {\n" +
                "        $user = User::create([\n" +
                "            'email' => $data['email'],\n" +
                "            'password' => Hash::make($data['password']),\n" +
                "            'first_name' => $data['firstName'],\n" +
                "            'last_name' => $data['lastName'],\n" +
                "            'phone_number' => $data['phoneNumber'] ?? null,\n" +
                "        ]);\n\n" +
                "        return $user;\n" +
                "    }\n" +
                "}";
    }

    private String getPhpModelCode() {
        return "<?php\n\n" +
                "namespace App\\Models;\n\n" +
                "use Illuminate\\Database\\Eloquent\\Factories\\HasFactory;\n" +
                "use Illuminate\\Foundation\\Auth\\User as Authenticatable;\n" +
                "use Illuminate\\Notifications\\Notifiable;\n\n" +
                "class User extends Authenticatable\n" +
                "{\n" +
                "    use HasFactory, Notifiable;\n\n" +
                "    protected $fillable = [\n" +
                "        'email',\n" +
                "        'password',\n" +
                "        'first_name',\n" +
                "        'last_name',\n" +
                "        'phone_number',\n" +
                "    ];\n\n" +
                "    protected $hidden = [\n" +
                "        'password',\n" +
                "        'remember_token',\n" +
                "    ];\n" +
                "}";
    }

}