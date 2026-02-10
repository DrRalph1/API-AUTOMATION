package com.usg.apiAutomation.services.systemActivities;

import com.usg.apiAutomation.dtos.systemActivities.database.*;
import com.usg.apiAutomation.repositories.DatabaseObjectRepository;
import com.usg.apiAutomation.utils.LoggerUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseObjectService {

    private final DatabaseObjectRepository databaseObjectRepository;
    private final LoggerUtil loggerUtil;

    // ========== SEARCH OBJECTS ==========
    @Transactional(readOnly = true)
    public Map<String, Object> searchObjects(DatabaseObjectSearchDTO searchDTO, Pageable pageable,
                                             String requestId, HttpServletRequest req) {

        Map<String, Object> result = new HashMap<>();

        try {
            List<DatabaseObjectDTO> objects;

            if (searchDTO == null) {
                objects = Collections.emptyList();
            } else {
                objects = databaseObjectRepository.findAllObjects(searchDTO);
            }

            // Apply pagination
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), objects.size());
            List<DatabaseObjectDTO> pageContent = objects.subList(start, end);

            Page<DatabaseObjectDTO> page = new PageImpl<>(
                    pageContent, pageable, objects.size()
            );

            // Get unique filters
            Map<String, List<String>> filters = getUniqueFilters(objects, searchDTO != null ? searchDTO.getObjectType() : null);

            // Build pagination metadata
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("page_number", page.getNumber());
            pagination.put("page_size", page.getSize());
            pagination.put("total_elements", page.getTotalElements());
            pagination.put("total_pages", page.getTotalPages());
            pagination.put("is_first", page.isFirst());
            pagination.put("is_last", page.isLast());

            result.put("objects", page.getContent());
            result.put("pagination", pagination);
            result.put("filters", filters);

            loggerUtil.log("database-objects",
                    "Request ID: " + requestId +
                            ", Search completed for type: " +
                            (searchDTO != null ? searchDTO.getObjectType() : "ALL") +
                            ", Found: " + objects.size() + " objects");

        } catch (Exception e) {
            loggerUtil.log("database-objects",
                    "Request ID: " + requestId +
                            ", Error searching objects: " + e.getMessage());
            throw e;
        }

        return result;
    }

    // ========== GET OBJECT DETAIL ==========
    @Transactional(readOnly = true)
    public DatabaseObjectDetailDTO getObjectDetail(String objectType, String objectName,
                                                   String owner, String requestId,
                                                   HttpServletRequest req) {

        try {
            DatabaseObjectDetailDTO detail = new DatabaseObjectDetailDTO();

            // Get basic object info
            DatabaseObjectDTO object = databaseObjectRepository.findObjectByName(
                    objectType, objectName, owner
            );

            if (object == null) {
                throw new RuntimeException("Object not found: " + objectName);
            }

            detail.setObjectInfo(object);

            // Get parameters for procedures/functions
            if ("PROCEDURE".equalsIgnoreCase(objectType) ||
                    "FUNCTION".equalsIgnoreCase(objectType)) {
                List<ParameterDTO> parameters = databaseObjectRepository.findObjectParameters(
                        object.getOwner(), object.getObjectName()
                );
                detail.setParameters(parameters);
            }

            // Get source code
            String sourceCode = databaseObjectRepository.getObjectSource(
                    objectType, object.getOwner(), object.getObjectName()
            );
            detail.setSourceCode(sourceCode);

            // Get dependencies
            List<DatabaseObjectDTO> dependencies = databaseObjectRepository.getDependencies(
                    object.getOwner(), object.getObjectName()
            );
            detail.setDependencies(dependencies);

            // Get usage statistics
            Map<String, Object> usageStats = databaseObjectRepository.getUsageStatistics(
                    object.getOwner(), object.getObjectName()
            );
            detail.setUsageStatistics(usageStats);

            loggerUtil.log("database-objects",
                    "Request ID: " + requestId +
                            ", Object detail retrieved: " + objectName);

            return detail;

        } catch (Exception e) {
            loggerUtil.log("database-objects",
                    "Request ID: " + requestId +
                            ", Error getting object detail: " + e.getMessage());
            throw e;
        }
    }

    // ========== GET DATABASE SUMMARY ==========
    @Transactional(readOnly = true)
    public DatabaseSummaryDTO getDatabaseSummary(String requestId, HttpServletRequest req) {

        try {
            DatabaseSummaryDTO summary = new DatabaseSummaryDTO();

            // Get counts by object type
            Map<String, Long> countsByType = databaseObjectRepository.getCountsByObjectType();
            summary.setCountsByType(countsByType);

            // Get counts by schema
            Map<String, Map<String, Long>> countsBySchema = databaseObjectRepository.getCountsBySchema();
            summary.setCountsBySchema(countsBySchema);

            // Get recent objects
            List<DatabaseObjectDTO> recentObjects = databaseObjectRepository.getRecentObjects(10);
            summary.setRecentObjects(recentObjects);

            // Get invalid objects
            List<DatabaseObjectDTO> invalidObjects = databaseObjectRepository.getInvalidObjects();
            summary.setInvalidObjects(invalidObjects);

            // Get top schemas
            Map<String, Long> topSchemas = databaseObjectRepository.getTopSchemas(5);
            summary.setTopSchemas(topSchemas);

            loggerUtil.log("database-objects",
                    "Request ID: " + requestId +
                            ", Database summary retrieved");

            return summary;

        } catch (Exception e) {
            loggerUtil.log("database-objects",
                    "Request ID: " + requestId +
                            ", Error getting database summary: " + e.getMessage());
            throw e;
        }
    }

    // ========== GET OBJECT DEPENDENCIES ==========
    @Transactional(readOnly = true)
    public List<DatabaseObjectDTO> getObjectDependencies(String objectType, String objectName,
                                                         String owner, String requestId,
                                                         HttpServletRequest req) {

        try {
            List<DatabaseObjectDTO> dependencies = databaseObjectRepository.getDependencies(
                    owner, objectName
            );

            loggerUtil.log("database-objects",
                    "Request ID: " + requestId +
                            ", Dependencies retrieved for: " + objectName +
                            ", Count: " + dependencies.size());

            return dependencies;

        } catch (Exception e) {
            loggerUtil.log("database-objects",
                    "Request ID: " + requestId +
                            ", Error getting dependencies: " + e.getMessage());
            throw e;
        }
    }

    // ========== VALIDATE OBJECT ==========
    @Transactional
    public ObjectValidationResultDTO validateObject(ObjectValidationRequestDTO validationRequest,
                                                    String requestId, HttpServletRequest req) {

        try {
            ObjectValidationResultDTO result = new ObjectValidationResultDTO();

            // Validate object exists
            boolean exists = databaseObjectRepository.objectExists(
                    validationRequest.getObjectType(),
                    validationRequest.getOwner(),
                    validationRequest.getObjectName()
            );

            if (!exists) {
                result.setValid(false);
                result.setMessage("Object does not exist");
                return result;
            }

            // Check object status
            String status = databaseObjectRepository.getObjectStatus(
                    validationRequest.getOwner(),
                    validationRequest.getObjectName()
            );

            result.setValid("VALID".equals(status));
            result.setStatus(status);

            // Get compilation errors if invalid
            if (!result.isValid()) {
                List<String> errors = databaseObjectRepository.getCompilationErrors(
                        validationRequest.getOwner(),
                        validationRequest.getObjectName()
                );
                result.setErrors(errors);
                result.setMessage("Object has compilation errors");
            } else {
                result.setMessage("Object is valid");
            }

            // Check dependencies
            List<String> missingDeps = databaseObjectRepository.checkDependencies(
                    validationRequest.getOwner(),
                    validationRequest.getObjectName()
            );
            result.setMissingDependencies(missingDeps);

            if (!missingDeps.isEmpty()) {
                result.setMessage("Object has missing dependencies");
            }

            loggerUtil.log("database-objects",
                    "Request ID: " + requestId +
                            ", Object validation completed: " + validationRequest.getObjectName() +
                            ", Valid: " + result.isValid());

            return result;

        } catch (Exception e) {
            loggerUtil.log("database-objects",
                    "Request ID: " + requestId +
                            ", Error validating object: " + e.getMessage());
            throw e;
        }
    }

    // ========== GET UNIQUE FILTERS ==========
    @Transactional(readOnly = true)
    public Map<String, List<String>> getUniqueFilters() {
        Map<String, List<String>> filters = new HashMap<>();

        // Get unique owners
        List<String> owners = databaseObjectRepository.findUniqueOwners();
        filters.put("owners", owners);

        // Get unique object types
        List<String> objectTypes = databaseObjectRepository.findUniqueObjectTypes();
        filters.put("objectTypes", objectTypes);

        return filters;
    }

    // ========== HELPER METHODS ==========
    private Map<String, List<String>> getUniqueFilters(List<DatabaseObjectDTO> objects, String objectType) {
        Map<String, List<String>> filters = new HashMap<>();

        if (objects == null || objects.isEmpty()) {
            return filters;
        }

        // Get unique owners
        List<String> owners = objects.stream()
                .map(DatabaseObjectDTO::getOwner)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        filters.put("owners", owners);

        // Get unique statuses
        if ("PROCEDURE".equalsIgnoreCase(objectType) || "FUNCTION".equalsIgnoreCase(objectType)) {
            List<String> statuses = objects.stream()
                    .map(DatabaseObjectDTO::getStatus)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            filters.put("statuses", statuses);
        }

        // Get unique object types
        if (objectType == null || "ALL".equalsIgnoreCase(objectType)) {
            List<String> objectTypes = objects.stream()
                    .map(DatabaseObjectDTO::getObjectType)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            filters.put("objectTypes", objectTypes);
        }

        return filters;
    }

    // ========== GET OBJECTS BY SCHEMA ==========
    @Transactional(readOnly = true)
    public Map<String, Object> getObjectsBySchema(String schemaName, Pageable pageable,
                                                  String requestId, HttpServletRequest req) {

        Map<String, Object> result = new HashMap<>();

        try {
            // Search for all object types in the schema
            DatabaseObjectSearchDTO searchDTO = DatabaseObjectSearchDTO.builder()
                    .owner(schemaName)
                    .objectType("ALL")
                    .build();

            List<DatabaseObjectDTO> objects = databaseObjectRepository.findAllObjects(searchDTO);

            // Apply pagination
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), objects.size());
            List<DatabaseObjectDTO> pageContent = objects.subList(start, end);

            Page<DatabaseObjectDTO> page = new PageImpl<>(
                    pageContent, pageable, objects.size()
            );

            // Get object type counts for this schema
            Map<String, Long> typeCounts = objects.stream()
                    .collect(Collectors.groupingBy(
                            DatabaseObjectDTO::getObjectType,
                            Collectors.counting()
                    ));

            result.put("objects", page.getContent());
            result.put("pagination", Map.of(
                    "page_number", page.getNumber(),
                    "page_size", page.getSize(),
                    "total_elements", page.getTotalElements(),
                    "total_pages", page.getTotalPages()
            ));
            result.put("type_counts", typeCounts);
            result.put("schema", schemaName);

            loggerUtil.log("database-objects",
                    "Request ID: " + requestId +
                            ", Schema objects retrieved: " + schemaName +
                            ", Count: " + objects.size());

        } catch (Exception e) {
            loggerUtil.log("database-objects",
                    "Request ID: " + requestId +
                            ", Error getting schema objects: " + e.getMessage());
            throw e;
        }

        return result;
    }
}