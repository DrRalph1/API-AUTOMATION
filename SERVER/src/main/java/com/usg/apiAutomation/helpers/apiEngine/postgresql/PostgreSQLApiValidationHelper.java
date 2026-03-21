package com.usg.apiAutomation.helpers.apiEngine.postgresql;

import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.CollectionInfoDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.ApiHeaderEntity;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.ApiParameterEntity;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.apiAutomation.repositories.apiGenerationEngine.GeneratedAPIRepository;
import com.usg.apiAutomation.services.schemaBrowser.PostgreSQLSchemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgreSQLApiValidationHelper {

    public void validateApiCodeUniqueness(GeneratedAPIRepository repository, String apiCode) {
        if (repository.existsByApiCode(apiCode)) {
            throw new RuntimeException("API code already exists: " + apiCode);
        }
    }

    public void validateApiCodeUniquenessOnUpdate(GeneratedAPIRepository repository,
                                                  String oldCode, String newCode) {
        if (!oldCode.equals(newCode) && repository.existsByApiCode(newCode)) {
            throw new RuntimeException("API code already exists: " + newCode);
        }
    }

    public CollectionInfoDTO validateAndGetCollectionInfo(CollectionInfoDTO collectionInfo) {
        if (collectionInfo == null) {
            throw new RuntimeException("Collection information is required");
        }

        if (collectionInfo.getCollectionId() == null || collectionInfo.getCollectionId().trim().isEmpty()) {
            throw new RuntimeException("Collection ID is required");
        }
        if (collectionInfo.getCollectionName() == null || collectionInfo.getCollectionName().trim().isEmpty()) {
            throw new RuntimeException("Collection name is required");
        }
        if (collectionInfo.getFolderId() == null || collectionInfo.getFolderId().trim().isEmpty()) {
            throw new RuntimeException("Folder ID is required");
        }
        if (collectionInfo.getFolderName() == null || collectionInfo.getFolderName().trim().isEmpty()) {
            throw new RuntimeException("Folder name is required");
        }

        return collectionInfo;
    }

    public void validateApiStatus(String status) {
        List<String> validStatuses = Arrays.asList("DRAFT", "ACTIVE", "DEPRECATED", "ARCHIVED");
        if (!validStatuses.contains(status)) {
            throw new RuntimeException("Invalid status: " + status +
                    ". Valid statuses: " + String.join(", ", validStatuses));
        }
    }

    public Map<String, Object> validateSourceObject(PostgreSQLSchemaService schemaService,
                                                    ApiSourceObjectDTO sourceObject,
                                                    SourceObjectDetailsProvider detailsProvider) {
        try {
            Map<String, Object> validation = schemaService.validateObject(
                    UUID.randomUUID().toString(),
                    null,
                    "system",
                    sourceObject.getObjectName(),
                    sourceObject.getObjectType(),
                    sourceObject.getOwner()
            );

            Map<String, Object> result = new HashMap<>();
            Map<String, Object> data = (Map<String, Object>) validation.get("data");

            if (data != null && Boolean.TRUE.equals(data.get("exists"))) {
                result.put("valid", true);
                result.put("exists", true);
                result.put("objectName", sourceObject.getObjectName());
                result.put("objectType", sourceObject.getObjectType());
                result.put("schema", sourceObject.getOwner());

                // PostgreSQL doesn't have synonyms, so skip synonym resolution

                Map<String, Object> details = detailsProvider.getSourceObjectDetails(sourceObject);
                result.put("details", details);

            } else {
                result.put("valid", false);
                result.put("exists", false);
                result.put("message", "Source object not found");
            }

            return result;

        } catch (Exception e) {
            log.error("Error validating source object: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("valid", false);
            error.put("error", e.getMessage());
            return error;
        }
    }

    public Map<String, String> validateRequiredHeaders(GeneratedApiEntity api,
                                                       ExecuteApiRequestDTO request) {
        Map<String, String> errors = new HashMap<>();

        if (api.getHeaders() == null || api.getHeaders().isEmpty()) {
            return errors;
        }

        Map<String, String> requestHeaders = request.getHeaders();
        if (requestHeaders == null) {
            requestHeaders = new HashMap<>();
        }

        String httpMethod = api.getHttpMethod();
        boolean isGetRequest = "GET".equalsIgnoreCase(httpMethod);
        boolean isHeadRequest = "HEAD".equalsIgnoreCase(httpMethod);
        boolean isDeleteRequest = "DELETE".equalsIgnoreCase(httpMethod);
        boolean skipContentTypeValidation = isGetRequest || isHeadRequest || isDeleteRequest;

        for (ApiHeaderEntity header : api.getHeaders()) {
            if (Boolean.TRUE.equals(header.getRequired()) &&
                    Boolean.TRUE.equals(header.getIsRequestHeader())) {

                String headerKey = header.getKey();

                if (skipContentTypeValidation && "Content-Type".equalsIgnoreCase(headerKey)) {
                    log.debug("Skipping Content-Type validation for {} request", httpMethod);
                    continue;
                }

                boolean found = false;

                if (requestHeaders.containsKey(headerKey)) {
                    found = true;
                } else {
                    for (String key : requestHeaders.keySet()) {
                        if (key.equalsIgnoreCase(headerKey)) {
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    errors.put(headerKey, "Required header '" + headerKey + "' is missing");
                }
            }
        }

        return errors;
    }

    public Map<String, String> validateRequiredParameters(GeneratedApiEntity api,
                                                          Map<String, Object> allParams) {
        System.out.println("=== Starting validateRequiredParameters ===");
        Map<String, String> errors = new HashMap<>();
        System.out.println("Initialized empty errors map");

        try {
            System.out.println("Checking if api or its parameters are null/empty");
            if (api == null || api.getParameters() == null || api.getParameters().isEmpty()) {
                System.out.println("api is null OR parameters is null OR parameters is empty - returning empty errors");
                return errors;
            }
            System.out.println("api has parameters, continuing validation");

            System.out.println("Checking if allParams is null");
            if (allParams == null) {
                allParams = new HashMap<>();
                System.out.println("allParams was null, initialized as empty HashMap");
            } else {
                System.out.println("allParams is not null, size: " + allParams.size());
            }

            log.debug("validateRequiredParameters: Processing {} parameters", api.getParameters().size());
            System.out.println("Processing " + api.getParameters().size() + " parameters");

            for (ApiParameterEntity param : api.getParameters()) {
                System.out.println("\n--- Processing parameter ---");

                if (param == null || param.getKey() == null) {
                    System.out.println("Parameter or parameter key is null - skipping");
                    continue;
                }
                System.out.println("Parameter key: " + param.getKey());

                Boolean required = param.getRequired();
                System.out.println("Required flag: " + required);

                if (required == null || !required) {
                    System.out.println("Parameter is not required - skipping");
                    continue;
                }

                String paramKey = param.getKey();
                boolean found = false;
                System.out.println("Checking for required parameter: " + paramKey);

                if (allParams.containsKey(paramKey)) {
                    System.out.println("Parameter key found in allParams");
                    Object value = allParams.get(paramKey);
                    System.out.println("Parameter value type: " + (value != null ? value.getClass().getName() : "null"));

                    if (value != null) {
                        System.out.println("Parameter value is not null");

                        if (value instanceof List || value.getClass().isArray()) {
                            System.out.println("Parameter value is a List or Array");

                            Collection<?> collection = value instanceof List ?
                                    (List<?>) value : Arrays.asList((Object[]) value);
                            System.out.println("Collection size: " + collection.size());

                            if (!collection.isEmpty()) {
                                System.out.println("Collection is not empty");
                                Object firstValue = collection.iterator().next();
                                System.out.println("First value in collection: " + firstValue);

                                found = firstValue != null && !firstValue.toString().trim().isEmpty();
                                System.out.println("After checking first value, found = " + found);
                            } else {
                                System.out.println("Collection is empty - parameter not found");
                            }
                        } else {
                            System.out.println("Parameter value is a single value (not collection)");
                            System.out.println("Value as string: '" + value.toString() + "'");

                            found = !value.toString().trim().isEmpty();
                            System.out.println("After trimming and checking emptiness, found = " + found);
                        }
                    } else {
                        System.out.println("Parameter value is null - parameter not found");
                    }
                } else {
                    // Try case-insensitive match for PostgreSQL
                    System.out.println("Parameter key NOT found in allParams, trying case-insensitive match");
                    for (Map.Entry<String, Object> entry : allParams.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(paramKey)) {
                            System.out.println("Found case-insensitive match: " + entry.getKey());
                            Object value = entry.getValue();
                            if (value != null) {
                                found = !value.toString().trim().isEmpty();
                                System.out.println("After case-insensitive check, found = " + found);
                            }
                            break;
                        }
                    }

                    if (!found) {
                        System.out.println("Parameter key NOT found in allParams");
                    }
                }

                if (!found) {
                    System.out.println("Required parameter [" + paramKey + "] not found or empty - adding to errors");
                    log.debug("Required parameter [{}] not found or empty", paramKey);
                    errors.put(paramKey, "Required parameter '" + paramKey + "' is missing or empty");
                } else {
                    System.out.println("Required parameter [" + paramKey + "] found and has valid value");
                }
            }

        } catch (Exception e) {
            System.out.println("EXCEPTION occurred: " + e.getMessage());
            e.printStackTrace();
            log.error("validateRequiredParameters: Exception: {}", e.getMessage(), e);
            errors.put("validation", "Error validating parameters: " + e.getMessage());
            System.out.println("Added validation error to errors map");
        }

        System.out.println("\n=== Returning errors map with " + errors.size() + " entries ===");
        return errors;
    }

    /**
     * Validates that required parameters are present (case-insensitive version)
     * This is a more PostgreSQL-friendly version that handles case-insensitive parameter names
     */
    public Map<String, String> validateRequiredParametersCaseInsensitive(GeneratedApiEntity api,
                                                                         Map<String, Object> allParams) {
        Map<String, String> errors = new HashMap<>();

        try {
            if (api == null || api.getParameters() == null || api.getParameters().isEmpty()) {
                return errors;
            }

            if (allParams == null) {
                allParams = new HashMap<>();
            }

            // Create a case-insensitive lookup map
            Map<String, Object> caseInsensitiveParams = new HashMap<>();
            for (Map.Entry<String, Object> entry : allParams.entrySet()) {
                caseInsensitiveParams.put(entry.getKey().toLowerCase(), entry.getValue());
            }

            for (ApiParameterEntity param : api.getParameters()) {
                if (param == null || param.getKey() == null) {
                    continue;
                }

                Boolean required = param.getRequired();
                if (required == null || !required) {
                    continue;
                }

                String paramKey = param.getKey().toLowerCase();
                boolean found = false;

                if (caseInsensitiveParams.containsKey(paramKey)) {
                    Object value = caseInsensitiveParams.get(paramKey);
                    if (value != null) {
                        if (value instanceof List || value.getClass().isArray()) {
                            Collection<?> collection = value instanceof List ?
                                    (List<?>) value : Arrays.asList((Object[]) value);
                            found = !collection.isEmpty() && collection.iterator().next() != null &&
                                    !collection.iterator().next().toString().trim().isEmpty();
                        } else {
                            found = !value.toString().trim().isEmpty();
                        }
                    }
                }

                if (!found) {
                    errors.put(param.getKey(), "Required parameter '" + param.getKey() + "' is missing or empty");
                }
            }

        } catch (Exception e) {
            log.error("validateRequiredParametersCaseInsensitive: Exception: {}", e.getMessage(), e);
            errors.put("validation", "Error validating parameters: " + e.getMessage());
        }

        return errors;
    }

    /**
     * Validate parameter data types against PostgreSQL data types
     */
    public Map<String, String> validateParameterDataTypes(GeneratedApiEntity api,
                                                          Map<String, Object> allParams) {
        Map<String, String> errors = new HashMap<>();

        try {
            if (api == null || api.getParameters() == null || api.getParameters().isEmpty()) {
                return errors;
            }

            if (allParams == null) {
                allParams = new HashMap<>();
            }

            Map<String, Object> caseInsensitiveParams = new HashMap<>();
            for (Map.Entry<String, Object> entry : allParams.entrySet()) {
                caseInsensitiveParams.put(entry.getKey().toLowerCase(), entry.getValue());
            }

            for (ApiParameterEntity param : api.getParameters()) {
                if (param == null || param.getKey() == null) {
                    continue;
                }

                String paramKey = param.getKey().toLowerCase();
                if (!caseInsensitiveParams.containsKey(paramKey)) {
                    continue;
                }

                Object value = caseInsensitiveParams.get(paramKey);
                if (value == null) {
                    continue;
                }

                String dataType = param.getOracleType();
                if (dataType == null || dataType.isEmpty()) {
                    continue;
                }

                String lowerDataType = dataType.toLowerCase();
                String stringValue = value.toString();

                // Validate based on PostgreSQL data type
                if (lowerDataType.contains("int") && !lowerDataType.contains("decimal") && !lowerDataType.contains("numeric")) {
                    // Integer validation
                    try {
                        Integer.parseInt(stringValue);
                    } catch (NumberFormatException e) {
                        errors.put(param.getKey(), "Parameter '" + param.getKey() +
                                "' must be a valid integer, but got: " + stringValue);
                    }
                } else if (lowerDataType.contains("decimal") || lowerDataType.contains("numeric")) {
                    // Decimal validation
                    try {
                        new java.math.BigDecimal(stringValue);
                    } catch (NumberFormatException e) {
                        errors.put(param.getKey(), "Parameter '" + param.getKey() +
                                "' must be a valid number, but got: " + stringValue);
                    }
                } else if (lowerDataType.contains("bool")) {
                    // Boolean validation
                    String lowerValue = stringValue.toLowerCase();
                    Set<String> validBooleans = new HashSet<>(Arrays.asList(
                            "true", "false", "1", "0", "yes", "no", "y", "n", "on", "off"
                    ));
                    if (!validBooleans.contains(lowerValue)) {
                        errors.put(param.getKey(), "Parameter '" + param.getKey() +
                                "' must be a boolean value (true/false, 1/0, yes/no, y/n, on/off), but got: " + stringValue);
                    }
                } else if (lowerDataType.contains("date") || lowerDataType.contains("timestamp")) {
                    // Date validation (basic)
                    List<String> dateFormats = Arrays.asList(
                            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss"
                    );
                    boolean validDate = false;
                    for (String format : dateFormats) {
                        try {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
                            sdf.setLenient(false);
                            sdf.parse(stringValue);
                            validDate = true;
                            break;
                        } catch (java.text.ParseException e) {
                            // Continue to next format
                        }
                    }
                    if (!validDate) {
                        errors.put(param.getKey(), "Parameter '" + param.getKey() +
                                "' must be a valid date/time, but got: " + stringValue);
                    }
                }
            }

        } catch (Exception e) {
            log.error("validateParameterDataTypes: Exception: {}", e.getMessage(), e);
            errors.put("validation", "Error validating parameter data types: " + e.getMessage());
        }

        return errors;
    }

    @FunctionalInterface
    public interface SourceObjectDetailsProvider {
        Map<String, Object> getSourceObjectDetails(ApiSourceObjectDTO sourceObject);
    }
}