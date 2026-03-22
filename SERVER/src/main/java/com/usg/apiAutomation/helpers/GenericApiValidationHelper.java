// helpers/GenericApiValidationHelper.java
package com.usg.apiAutomation.helpers;

import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.CollectionInfoDTO;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.apiAutomation.enums.DatabaseType;
import com.usg.apiAutomation.repositories.apiGenerationEngine.GeneratedAPIRepository;
import com.usg.apiAutomation.services.schemaBrowser.DatabaseSchemaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Component
public class GenericApiValidationHelper {

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

    public Map<String, Object> validateSourceObject(
            DatabaseSchemaService schemaService,
            ApiSourceObjectDTO sourceObject,
            Function<ApiSourceObjectDTO, Map<String, Object>> detailsProvider) {

        Map<String, Object> result = new HashMap<>();
        DatabaseType databaseType = schemaService.getDatabaseType();

        try {
            log.info("Validating source object: {}.{} ({}) on database: {}",
                    sourceObject.getOwner(), sourceObject.getObjectName(),
                    sourceObject.getObjectType(), databaseType);

            // Check if object exists
            boolean exists = schemaService.objectExists(
                    sourceObject.getOwner(),
                    sourceObject.getObjectName(),
                    sourceObject.getObjectType()
            );

            if (!exists) {
                log.warn("Object not found: {}.{} on {}",
                        sourceObject.getOwner(), sourceObject.getObjectName(), databaseType);

                result.put("valid", false);
                result.put("exists", false);
                result.put("message", "Source object not found in " + databaseType + " database");
                result.put("objectName", sourceObject.getObjectName());
                result.put("objectType", sourceObject.getObjectType());
                result.put("owner", sourceObject.getOwner());
                result.put("databaseType", databaseType.getValue());
                result.put("details", new HashMap<>());

                return result;
            }

            // Get object details
            Map<String, Object> details = detailsProvider.apply(sourceObject);

            // Check if details contain error
            if (details.containsKey("hasError") && (boolean) details.get("hasError")) {
                result.put("valid", false);
                result.put("exists", true);
                result.put("message", "Error retrieving object details: " + details.get("error"));
                result.put("details", details);
                return result;
            }

            // Validate that we have parameters/columns
            boolean hasParameters = details.containsKey("parameters") &&
                    ((List<?>) details.get("parameters")).size() > 0;
            boolean hasColumns = details.containsKey("columns") &&
                    ((List<?>) details.get("columns")).size() > 0;

            // For procedures/functions, parameters are required
            String objectType = sourceObject.getObjectType().toUpperCase();
            boolean isValid = true;
            String validationMessage = "Source object validated successfully";

            if (("PROCEDURE".equals(objectType) || "FUNCTION".equals(objectType)) && !hasParameters) {
                isValid = false;
                validationMessage = "Object has no parameters defined. Please check the object definition.";
                log.warn("Procedure/Function {} on {} has no parameters",
                        sourceObject.getObjectName(), databaseType);
            } else if (("TABLE".equals(objectType) || "VIEW".equals(objectType)) && !hasColumns) {
                isValid = false;
                validationMessage = "Object has no columns defined. Please check the object definition.";
                log.warn("Table/View {} on {} has no columns",
                        sourceObject.getObjectName(), databaseType);
            }

            result.put("valid", isValid);
            result.put("exists", true);
            result.put("message", validationMessage);
            result.put("objectName", details.getOrDefault("objectName", sourceObject.getObjectName()));
            result.put("objectType", details.getOrDefault("objectType", sourceObject.getObjectType()));
            result.put("owner", details.getOrDefault("owner", sourceObject.getOwner()));
            result.put("databaseType", databaseType.getValue());
            result.put("details", details);

            // Add parameters/columns for frontend
            if (hasParameters) {
                result.put("parameters", details.get("parameters"));
                result.put("parameterCount", ((List<?>) details.get("parameters")).size());
            }
            if (hasColumns) {
                result.put("columns", details.get("columns"));
                result.put("columnCount", ((List<?>) details.get("columns")).size());
            }

            log.info("Validation completed for {}.{} on {}: valid={}",
                    sourceObject.getOwner(), sourceObject.getObjectName(),
                    databaseType, isValid);

        } catch (Exception e) {
            log.error("Error validating source object on {}: {}",
                    databaseType, e.getMessage(), e);
            result.put("valid", false);
            result.put("exists", false);
            result.put("message", "Validation error: " + e.getMessage());
            result.put("error", e.getMessage());
            result.put("databaseType", databaseType.getValue());
            result.put("details", new HashMap<>());
        }

        return result;
    }
}