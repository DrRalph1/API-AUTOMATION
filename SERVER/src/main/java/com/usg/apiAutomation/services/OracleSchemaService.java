package com.usg.apiAutomation.services;

import com.usg.apiAutomation.repositories.oracle.OracleSchemaRepository;
import com.usg.apiAutomation.utils.LoggerUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OracleSchemaService {

    private final OracleSchemaRepository oracleSchemeRepository;
    private final LoggerUtil loggerUtil;

    // ==================== ENHANCED SYNONYM METHODS ====================

    /**
     * Get all synonyms with detailed information
     * Endpoint: GET /plx/api/oracle/schema/synonyms/details
     */
    public Map<String, Object> getAllSynonymsWithDetails(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle synonyms with details for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> synonyms = oracleSchemeRepository.getAllSynonymsWithDetails();

            Map<String, Object> result = new HashMap<>();
            result.put("data", synonyms);
            result.put("totalCount", synonyms.size());
            result.put("responseCode", 200);
            result.put("message", "Synonyms retrieved successfully with details");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} synonyms with details", requestId, synonyms.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting synonyms with details: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    /**
     * Get synonyms filtered by target type
     * Endpoint: GET /plx/api/oracle/schema/synonyms/byTargetType/{targetType}
     */
    public Map<String, Object> getSynonymsByTargetType(String requestId, HttpServletRequest req,
                                                       String performedBy, String targetType) {
        log.info("RequestEntity ID: {}, Getting synonyms by target type: {} for user: {}",
                requestId, targetType, performedBy);

        try {
            List<Map<String, Object>> synonyms = oracleSchemeRepository.getSynonymsByTargetType(targetType);

            Map<String, Object> result = new HashMap<>();
            result.put("data", synonyms);
            result.put("totalCount", synonyms.size());
            result.put("targetType", targetType);
            result.put("responseCode", 200);
            result.put("message", "Synonyms filtered by target type retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} synonyms for target type: {}",
                    requestId, synonyms.size(), targetType);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting synonyms by target type: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    /**
     * Get detailed information about a specific synonym
     * Endpoint: GET /plx/api/oracle/schema/synonyms/{synonymName}/details
     */
    public Map<String, Object> getSynonymDetailsEnhanced(String requestId, HttpServletRequest req,
                                                         String performedBy, String synonymName) {
        log.info("RequestEntity ID: {}, Getting enhanced details for synonym: {} for user: {}",
                requestId, synonymName, performedBy);

        try {
            Map<String, Object> synonymDetails = oracleSchemeRepository.getSynonymDetails(synonymName);

            Map<String, Object> result = new HashMap<>();
            result.put("data", synonymDetails);
            result.put("responseCode", 200);
            result.put("message", "Synonym details retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved enhanced details for synonym: {}", requestId, synonymName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting enhanced details for synonym {}: {}",
                    requestId, synonymName, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    /**
     * Resolve a synonym to its target object
     * Endpoint: GET /plx/api/oracle/schema/synonyms/{synonymName}/resolve
     */
    public Map<String, Object> resolveSynonym(String requestId, HttpServletRequest req,
                                              String performedBy, String synonymName) {
        log.info("RequestEntity ID: {}, Resolving synonym: {} for user: {}", requestId, synonymName, performedBy);

        try {
            Map<String, Object> resolved = oracleSchemeRepository.resolveSynonym(synonymName);

            Map<String, Object> result = new HashMap<>();
            result.put("data", resolved);
            result.put("responseCode", 200);
            result.put("message", "Synonym resolved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Resolved synonym: {} to target", requestId, synonymName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error resolving synonym {}: {}", requestId, synonymName, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    /**
     * Validate a synonym and check its target
     * Endpoint: GET /plx/api/oracle/schema/synonyms/{synonymName}/validate
     */
    public Map<String, Object> validateSynonym(String requestId, HttpServletRequest req,
                                               String performedBy, String synonymName) {
        log.info("RequestEntity ID: {}, Validating synonym: {} for user: {}", requestId, synonymName, performedBy);

        try {
            Map<String, Object> validation = oracleSchemeRepository.validateSynonym(synonymName);

            Map<String, Object> result = new HashMap<>();
            result.put("data", validation);
            result.put("responseCode", 200);
            result.put("message", "Synonym validation completed");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Validation completed for synonym: {}, exists: {}",
                    requestId, synonymName, validation.get("exists"));

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error validating synonym {}: {}", requestId, synonymName, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ==================== ENHANCED OBJECT METHODS ====================

    /**
     * Get details for any object by name and type
     * Endpoint: GET /plx/api/oracle/schema/objects/{objectType}/{objectName}/details
     */
    public Map<String, Object> getObjectDetailsByNameAndType(String requestId, HttpServletRequest req,
                                                             String performedBy, String objectName,
                                                             String objectType, String owner) {
        log.info("RequestEntity ID: {}, Getting details for {}: {}, owner: {}, user: {}",
                requestId, objectType, objectName, owner, performedBy);

        try {
            Map<String, Object> objectDetails = oracleSchemeRepository.getObjectDetailsByNameAndType(
                    objectName, objectType, owner);

            Map<String, Object> result = new HashMap<>();
            result.put("data", objectDetails);
            result.put("responseCode", 200);
            result.put("message", "Object details retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved details for {}: {}", requestId, objectType, objectName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for {} {}: {}",
                    requestId, objectType, objectName, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    /**
     * Validate if an object exists and is accessible
     * Endpoint: GET /plx/api/oracle/schema/objects/validate
     */
    public Map<String, Object> validateObject(String requestId, HttpServletRequest req,
                                              String performedBy, String objectName,
                                              String objectType, String owner) {
        log.info("RequestEntity ID: {}, Validating {}: {}, owner: {}, user: {}",
                requestId, objectType, objectName, owner, performedBy);

        try {
            Map<String, Object> validation = oracleSchemeRepository.validateObject(objectName, objectType, owner);

            Map<String, Object> result = new HashMap<>();
            result.put("data", validation);
            result.put("responseCode", 200);
            result.put("message", "Object validation completed");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Validation completed for {}: {}, exists: {}",
                    requestId, objectType, objectName, validation.get("exists"));

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error validating {} {}: {}",
                    requestId, objectType, objectName, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ==================== ENHANCED SEARCH METHODS ====================

    /**
     * Comprehensive search across all objects including synonym targets
     * Endpoint: GET /plx/api/oracle/schema/comprehensive-search
     */
    public Map<String, Object> comprehensiveSearch(String requestId, HttpServletRequest req,
                                                   String performedBy, String searchPattern) {
        log.info("RequestEntity ID: {}, Performing comprehensive search with pattern: {} for user: {}",
                requestId, searchPattern, performedBy);

        try {
            List<Map<String, Object>> results = oracleSchemeRepository.comprehensiveSearch(searchPattern);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("results", results);
            responseData.put("totalCount", results.size());
            responseData.put("searchPattern", searchPattern);

            Map<String, Object> result = new HashMap<>();
            result.put("data", responseData);
            result.put("responseCode", 200);
            result.put("message", "Comprehensive search completed successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Comprehensive search found {} results for pattern: {}",
                    requestId, results.size(), searchPattern);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error in comprehensive search: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ==================== DDL METHODS ====================

    /**
     * Get object DDL
     * Endpoint: GET /plx/api/oracle/schema/objects/{objectType}/{objectName}/ddl
     */
    public Map<String, Object> getObjectDDL(String requestId, HttpServletRequest req,
                                            String performedBy, String objectType, String objectName) {
        log.info("RequestEntity ID: {}, Getting DDL for {}: {}, user: {}",
                requestId, objectType, objectName, performedBy);

        try {
            Map<String, Object> ddlResult = oracleSchemeRepository.getObjectDDLForFrontend(objectName, objectType);

            Map<String, Object> result = new HashMap<>();
            result.put("data", ddlResult.get("ddl"));
            result.put("responseCode", 200);
            result.put("message", "DDL retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved DDL for {}: {}", requestId, objectType, objectName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting DDL for {} {}: {}",
                    requestId, objectType, objectName, e.getMessage());

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("data", "-- Error retrieving DDL: " + e.getMessage());
            errorResult.put("responseCode", 500);
            errorResult.put("message", e.getMessage());
            errorResult.put("requestId", requestId);
            errorResult.put("timestamp", java.time.Instant.now().toString());

            return errorResult;
        }
    }

    /**
     * Get object size information
     * Endpoint: GET /plx/api/oracle/schema/objects/{objectType}/{objectName}/size
     */
    public Map<String, Object> getObjectSize(String requestId, HttpServletRequest req,
                                             String performedBy, String objectName, String objectType) {
        log.info("RequestEntity ID: {}, Getting size for {}: {}, user: {}",
                requestId, objectType, objectName, performedBy);

        try {
            Map<String, Object> sizeInfo = oracleSchemeRepository.getObjectSize(objectName, objectType);

            Map<String, Object> result = new HashMap<>();
            result.put("data", sizeInfo);
            result.put("responseCode", 200);
            result.put("message", "Object size retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved size for {}: {}", requestId, objectType, objectName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting size for {} {}: {}",
                    requestId, objectType, objectName, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ==================== FRONTEND TABLE METHODS ====================

    /**
     * Get all tables with frontend-friendly format
     * Endpoint: GET /plx/api/oracle/schema/tables
     */
    public Map<String, Object> getAllTablesForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle tables for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> tables = oracleSchemeRepository.getAllTablesForFrontend();

            Map<String, Object> result = new HashMap<>();
            result.put("data", tables);
            result.put("totalCount", tables.size());
            result.put("responseCode", 200);
            result.put("message", "Tables retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} tables for frontend", requestId, tables.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting tables for frontend: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    /**
     * Get table details with frontend-friendly format
     * Endpoint: GET /plx/api/oracle/schema/tables/{tableName}/details
     */
    public Map<String, Object> getTableDetailsForFrontend(String requestId, HttpServletRequest req,
                                                          String performedBy, String tableName) {
        log.info("RequestEntity ID: {}, Getting table details for frontend, table: {}, user: {}",
                requestId, tableName, performedBy);

        try {
            Map<String, Object> tableDetails = oracleSchemeRepository.getTableDetailsForFrontend(tableName);

            Map<String, Object> result = new HashMap<>();
            result.put("data", tableDetails);
            result.put("responseCode", 200);
            result.put("message", "Table details retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved details for table: {}", requestId, tableName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting table details for frontend, table {}: {}",
                    requestId, tableName, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    /**
     * Get table data with pagination
     * Endpoint: GET /plx/api/oracle/schema/tables/{tableName}/data
     */
    public Map<String, Object> getTableData(String requestId, HttpServletRequest req,
                                            String performedBy, String tableName,
                                            int page, int pageSize, String sortColumn, String sortDirection) {
        log.info("RequestEntity ID: {}, Getting table data for frontend, table: {}, page: {}, pageSize: {}, user: {}",
                requestId, tableName, page, pageSize, performedBy);

        try {
            Map<String, Object> tableData = oracleSchemeRepository.getTableDataWithPagination(
                    tableName, page, pageSize, sortColumn, sortDirection);

            Map<String, Object> result = new HashMap<>();
            result.put("data", tableData);
            result.put("responseCode", 200);
            result.put("message", "Table data retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} rows for table: {}",
                    requestId, ((List<?>) tableData.get("rows")).size(), tableName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting table data for frontend, table {}: {}",
                    requestId, tableName, e.getMessage());

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("rows", new ArrayList<>());
            errorData.put("columns", new ArrayList<>());
            errorData.put("page", page);
            errorData.put("pageSize", pageSize);
            errorData.put("totalRows", 0);
            errorData.put("totalPages", 0);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("data", errorData);
            errorResult.put("responseCode", 500);
            errorResult.put("message", e.getMessage());
            errorResult.put("requestId", requestId);
            errorResult.put("timestamp", java.time.Instant.now().toString());

            return errorResult;
        }
    }

    // ==================== FRONTEND VIEW METHODS ====================

    /**
     * Get all views with frontend-friendly format
     * Endpoint: GET /plx/api/oracle/schema/views
     */
    public Map<String, Object> getAllViewsForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle views for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> views = oracleSchemeRepository.getAllViewsForFrontend();

            Map<String, Object> result = new HashMap<>();
            result.put("data", views);
            result.put("totalCount", views.size());
            result.put("responseCode", 200);
            result.put("message", "Views retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} views for frontend", requestId, views.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting views for frontend: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ==================== FRONTEND PROCEDURE METHODS ====================

    /**
     * Get all procedures with frontend-friendly format
     * Endpoint: GET /plx/api/oracle/schema/procedures
     */
    public Map<String, Object> getAllProceduresForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle procedures for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> procedures = oracleSchemeRepository.getAllProceduresForFrontend();

            Map<String, Object> result = new HashMap<>();
            result.put("data", procedures);
            result.put("totalCount", procedures.size());
            result.put("responseCode", 200);
            result.put("message", "Procedures retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} procedures for frontend", requestId, procedures.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting procedures for frontend: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ==================== FRONTEND FUNCTION METHODS ====================

    /**
     * Get all functions with frontend-friendly format
     * Endpoint: GET /plx/api/oracle/schema/functions
     */
    public Map<String, Object> getAllFunctionsForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle functions for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> functions = oracleSchemeRepository.getAllFunctionsForFrontend();

            Map<String, Object> result = new HashMap<>();
            result.put("data", functions);
            result.put("totalCount", functions.size());
            result.put("responseCode", 200);
            result.put("message", "Functions retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} functions for frontend", requestId, functions.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting functions for frontend: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ==================== FRONTEND PACKAGE METHODS ====================

    /**
     * Get all packages with frontend-friendly format
     * Endpoint: GET /plx/api/oracle/schema/packages
     */
    public Map<String, Object> getAllPackagesForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle packages for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> packages = oracleSchemeRepository.getAllPackagesForFrontend();

            Map<String, Object> result = new HashMap<>();
            result.put("data", packages);
            result.put("totalCount", packages.size());
            result.put("responseCode", 200);
            result.put("message", "Packages retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} packages for frontend", requestId, packages.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting packages for frontend: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ==================== FRONTEND SEQUENCE METHODS ====================

    /**
     * Get all sequences with frontend-friendly format
     * Endpoint: GET /plx/api/oracle/schema/sequences
     */
    public Map<String, Object> getAllSequencesForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle sequences for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> sequences = oracleSchemeRepository.getAllSequencesForFrontend();

            Map<String, Object> result = new HashMap<>();
            result.put("data", sequences);
            result.put("totalCount", sequences.size());
            result.put("responseCode", 200);
            result.put("message", "Sequences retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} sequences for frontend", requestId, sequences.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting sequences for frontend: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ==================== FRONTEND SYNONYM METHODS ====================

    /**
     * Get all synonyms with frontend-friendly format
     * Endpoint: GET /plx/api/oracle/schema/synonyms
     */
    public Map<String, Object> getAllSynonymsForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle synonyms for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> synonyms = oracleSchemeRepository.getAllSynonymsForFrontend();

            Map<String, Object> result = new HashMap<>();
            result.put("data", synonyms);
            result.put("totalCount", synonyms.size());
            result.put("responseCode", 200);
            result.put("message", "Synonyms retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} synonyms for frontend", requestId, synonyms.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting synonyms for frontend: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ==================== FRONTEND TYPE METHODS ====================

    /**
     * Get all types with frontend-friendly format
     * Endpoint: GET /plx/api/oracle/schema/types
     */
    public Map<String, Object> getAllTypesForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle types for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> types = oracleSchemeRepository.getAllTypesForFrontend();

            Map<String, Object> result = new HashMap<>();
            result.put("data", types);
            result.put("totalCount", types.size());
            result.put("responseCode", 200);
            result.put("message", "Types retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} types for frontend", requestId, types.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting types for frontend: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ==================== FRONTEND TRIGGER METHODS ====================

    /**
     * Get all triggers with frontend-friendly format
     * Endpoint: GET /plx/api/oracle/schema/triggers
     */
    public Map<String, Object> getAllTriggersForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle triggers for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> triggers = oracleSchemeRepository.getAllTriggersForFrontend();

            Map<String, Object> result = new HashMap<>();
            result.put("data", triggers);
            result.put("totalCount", triggers.size());
            result.put("responseCode", 200);
            result.put("message", "Triggers retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} triggers for frontend", requestId, triggers.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting triggers for frontend: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ==================== FRONTEND SEARCH METHODS ====================

    /**
     * Search schema objects
     * Endpoint: GET /plx/api/oracle/schema/search
     */
    public Map<String, Object> searchObjectsForFrontend(String requestId, HttpServletRequest req,
                                                        String performedBy, String searchQuery,
                                                        String searchType, int maxResults) {
        log.info("RequestEntity ID: {}, Searching Oracle objects with query: {}, type: {}, maxResults: {}",
                requestId, searchQuery, searchType, maxResults);

        try {
            List<Map<String, Object>> results = oracleSchemeRepository.searchObjectsForFrontend(
                    searchQuery, searchType, maxResults);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("results", results);
            responseData.put("totalCount", results.size());
            responseData.put("query", searchQuery);
            responseData.put("type", searchType);

            Map<String, Object> result = new HashMap<>();
            result.put("data", responseData);
            result.put("responseCode", 200);
            result.put("message", "Search completed successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Found {} objects matching query: {}",
                    requestId, results.size(), searchQuery);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error searching objects: {}", requestId, e.getMessage());

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("results", new ArrayList<>());
            errorData.put("totalCount", 0);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("data", errorData);
            errorResult.put("responseCode", 500);
            errorResult.put("message", e.getMessage());
            errorResult.put("requestId", requestId);
            errorResult.put("timestamp", java.time.Instant.now().toString());

            return errorResult;
        }
    }

    // ==================== EXECUTE QUERY METHODS ====================

    /**
     * Execute SQL query
     * Endpoint: POST /plx/api/oracle/schema/execute
     */
    public Map<String, Object> executeQuery(String requestId, HttpServletRequest req,
                                            String performedBy, String query,
                                            int timeoutSeconds, boolean readOnly) {
        log.info("RequestEntity ID: {}, Executing query, timeout: {}, readOnly: {}, user: {}",
                requestId, timeoutSeconds, readOnly, performedBy);

        try {
            Map<String, Object> queryResults = oracleSchemeRepository.executeQuery(query, timeoutSeconds, readOnly);

            Map<String, Object> result = new HashMap<>();
            result.put("data", queryResults);
            result.put("responseCode", 200);
            result.put("message", "Query executed successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Query executed, returned {} rows",
                    requestId, queryResults.get("rowCount"));

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error executing query: {}", requestId, e.getMessage());

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("rows", new ArrayList<>());
            errorData.put("columns", new ArrayList<>());
            errorData.put("rowCount", 0);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("data", errorData);
            errorResult.put("responseCode", 500);
            errorResult.put("message", e.getMessage());
            errorResult.put("requestId", requestId);
            errorResult.put("timestamp", java.time.Instant.now().toString());

            return errorResult;
        }
    }

    // ==================== EXISTING TABLE METHODS ====================

    /**
     * Get all tables from Oracle schema (legacy format)
     */
    public Map<String, Object> getAllTables(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle tables for user: {}", requestId, performedBy);
        loggerUtil.log("oracleSchema",
                "RequestEntity ID: " + requestId + ", Getting all Oracle tables for user: " + performedBy);

        try {
            List<Map<String, Object>> tables = oracleSchemeRepository.getAllTables();

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("totalCount", tables.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle tables from schema: {}",
                    requestId, tables.size(), currentSchema);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle tables: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle tables: " + e.getMessage(), e);
        }
    }

    /**
     * Get tables from a specific Oracle schema (legacy format)
     */
    public Map<String, Object> getTablesBySchema(String requestId, HttpServletRequest req,
                                                 String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting tables from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> tables = oracleSchemeRepository.getTablesBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("totalCount", tables.size());
            result.put("database", "Oracle");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} tables from Oracle schema: {}",
                    requestId, tables.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle tables from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle tables from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed information about a specific table (legacy format)
     */
    public Map<String, Object> getTableDetails(String requestId, HttpServletRequest req,
                                               String performedBy, String tableName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle table: {} for user: {}",
                requestId, tableName, performedBy);

        try {
            Map<String, Object> tableDetails = oracleSchemeRepository.getTableDetails(tableName);
            List<Map<String, Object>> columns = oracleSchemeRepository.getTableColumns(tableName);
            List<Map<String, Object>> constraints = oracleSchemeRepository.getTableConstraints(tableName);
            List<Map<String, Object>> indexes = oracleSchemeRepository.getTableIndexes(tableName);

            Map<String, Object> result = new HashMap<>();
            result.put("tableInfo", tableDetails);
            result.put("columns", columns);
            result.put("constraints", constraints);
            result.put("indexes", indexes);
            result.put("columnCount", columns.size());
            result.put("constraintCount", constraints.size());
            result.put("indexCount", indexes.size());
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved details for Oracle table: {}, found {} columns",
                    requestId, tableName, columns.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for Oracle table {}: {}",
                    requestId, tableName, e.getMessage());
            throw new RuntimeException("Failed to retrieve details for Oracle table " + tableName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Search for tables by name pattern (legacy format)
     */
    public Map<String, Object> searchTables(String requestId, HttpServletRequest req,
                                            String performedBy, String searchPattern) {
        log.info("RequestEntity ID: {}, Searching Oracle tables with pattern: {} for user: {}",
                requestId, searchPattern, performedBy);

        try {
            List<Map<String, Object>> tables = oracleSchemeRepository.searchTables(searchPattern);

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("currentSchema", currentSchema);
            result.put("totalCount", tables.size());
            result.put("searchPattern", searchPattern);
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Found {} Oracle tables matching pattern: {}",
                    requestId, tables.size(), searchPattern);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error searching Oracle tables with pattern {}: {}",
                    requestId, searchPattern, e.getMessage());
            throw new RuntimeException("Failed to search Oracle tables: " + e.getMessage(), e);
        }
    }

    /**
     * Get table statistics (legacy format)
     */
    public Map<String, Object> getTableStatistics(String requestId, HttpServletRequest req,
                                                  String performedBy, String tableName) {
        log.info("RequestEntity ID: {}, Getting statistics for Oracle table: {} for user: {}",
                requestId, tableName, performedBy);

        try {
            Map<String, Object> statistics = oracleSchemeRepository.getTableStatistics(tableName);

            Map<String, Object> result = new HashMap<>();
            result.put("tableName", tableName);
            result.put("statistics", statistics);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved statistics for Oracle table: {}", requestId, tableName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting statistics for Oracle table {}: {}",
                    requestId, tableName, e.getMessage());
            throw new RuntimeException("Failed to retrieve statistics for Oracle table " + tableName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get tables with row count (legacy format)
     */
    public Map<String, Object> getTablesWithRowCount(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting Oracle tables with row count for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> tables = oracleSchemeRepository.getTablesWithRowCount();

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("totalCount", tables.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved row counts for {} Oracle tables", requestId, tables.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle tables with row count: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle tables with row count: " + e.getMessage(), e);
        }
    }

    /**
     * Get table count by tablespace (legacy format)
     */
    public Map<String, Object> getTableCountByTablespace(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting Oracle table count by tablespace for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> tablespaceStats = oracleSchemeRepository.getTableCountByTablespace();

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("tablespaceStats", tablespaceStats);
            result.put("totalTablespaces", tablespaceStats.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved tablespace statistics for {} tablespaces",
                    requestId, tablespaceStats.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle table count by tablespace: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle table count by tablespace: " + e.getMessage(), e);
        }
    }

    /**
     * Get recent tables (legacy format)
     */
    public Map<String, Object> getRecentTables(String requestId, HttpServletRequest req,
                                               String performedBy, int days) {
        log.info("RequestEntity ID: {}, Getting Oracle tables modified in last {} days for user: {}",
                requestId, days, performedBy);

        try {
            List<Map<String, Object>> tables = oracleSchemeRepository.getRecentTables(days);

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("totalCount", tables.size());
            result.put("days", days);
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Found {} Oracle tables modified in last {} days",
                    requestId, tables.size(), days);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting recent Oracle tables: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve recent Oracle tables: " + e.getMessage(), e);
        }
    }

    // ==================== EXISTING VIEW METHODS ====================

    /**
     * Get all views from Oracle schema (legacy format)
     */
    public Map<String, Object> getAllViews(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle views for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> views = oracleSchemeRepository.getAllViews();

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("views", views);
            result.put("totalCount", views.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle views", requestId, views.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle views: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle views: " + e.getMessage(), e);
        }
    }

    /**
     * Get views from a specific Oracle schema (legacy format)
     */
    public Map<String, Object> getViewsBySchema(String requestId, HttpServletRequest req,
                                                String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting views from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> views = oracleSchemeRepository.getViewsBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("views", views);
            result.put("totalCount", views.size());
            result.put("database", "Oracle");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} views from Oracle schema: {}",
                    requestId, views.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle views from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle views from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed information about a specific view (legacy format)
     */
    public Map<String, Object> getViewDetails(String requestId, HttpServletRequest req,
                                              String performedBy, String viewName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle view: {} for user: {}",
                requestId, viewName, performedBy);

        try {
            Map<String, Object> viewDetails = oracleSchemeRepository.getViewDetails(viewName);

            viewDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            viewDetails.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved details for Oracle view: {}",
                    requestId, viewName);

            return viewDetails;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for Oracle view {}: {}",
                    requestId, viewName, e.getMessage());
            throw new RuntimeException("Failed to retrieve details for Oracle view " + viewName + ": " + e.getMessage(), e);
        }
    }

    // ==================== EXISTING PROCEDURE METHODS ====================

    /**
     * Get all procedures from Oracle schema (legacy format)
     */
    public Map<String, Object> getAllProcedures(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle procedures for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> procedures = oracleSchemeRepository.getAllProcedures();

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("procedures", procedures);
            result.put("totalCount", procedures.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle procedures", requestId, procedures.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle procedures: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle procedures: " + e.getMessage(), e);
        }
    }

    /**
     * Get procedures from a specific Oracle schema (legacy format)
     */
    public Map<String, Object> getProceduresBySchema(String requestId, HttpServletRequest req,
                                                     String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting procedures from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> procedures = oracleSchemeRepository.getProceduresBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("procedures", procedures);
            result.put("totalCount", procedures.size());
            result.put("database", "Oracle");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} procedures from Oracle schema: {}",
                    requestId, procedures.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle procedures from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle procedures from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed information about a specific procedure (legacy format)
     */
    public Map<String, Object> getProcedureDetails(String requestId, HttpServletRequest req,
                                                   String performedBy, String procedureName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle procedure: {} for user: {}",
                requestId, procedureName, performedBy);

        try {
            Map<String, Object> procedureDetails = oracleSchemeRepository.getProcedureDetails(procedureName);

            procedureDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            procedureDetails.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved details for Oracle procedure: {}",
                    requestId, procedureName);

            return procedureDetails;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for Oracle procedure {}: {}",
                    requestId, procedureName, e.getMessage());
            throw new RuntimeException("Failed to retrieve details for Oracle procedure " + procedureName + ": " + e.getMessage(), e);
        }
    }

    // ==================== EXISTING FUNCTION METHODS ====================

    /**
     * Get all functions from Oracle schema (legacy format)
     */
    public Map<String, Object> getAllFunctions(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle functions for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> functions = oracleSchemeRepository.getAllFunctions();

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("functions", functions);
            result.put("totalCount", functions.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle functions", requestId, functions.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle functions: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle functions: " + e.getMessage(), e);
        }
    }

    /**
     * Get functions from a specific Oracle schema (legacy format)
     */
    public Map<String, Object> getFunctionsBySchema(String requestId, HttpServletRequest req,
                                                    String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting functions from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> functions = oracleSchemeRepository.getFunctionsBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("functions", functions);
            result.put("totalCount", functions.size());
            result.put("database", "Oracle");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} functions from Oracle schema: {}",
                    requestId, functions.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle functions from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle functions from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed information about a specific function (legacy format)
     */
    public Map<String, Object> getFunctionDetails(String requestId, HttpServletRequest req,
                                                  String performedBy, String functionName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle function: {} for user: {}",
                requestId, functionName, performedBy);

        try {
            Map<String, Object> functionDetails = oracleSchemeRepository.getFunctionDetails(functionName);

            functionDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            functionDetails.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved details for Oracle function: {}",
                    requestId, functionName);

            return functionDetails;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for Oracle function {}: {}",
                    requestId, functionName, e.getMessage());
            throw new RuntimeException("Failed to retrieve details for Oracle function " + functionName + ": " + e.getMessage(), e);
        }
    }

    // ==================== EXISTING PACKAGE METHODS ====================

    /**
     * Get all packages from Oracle schema (legacy format)
     */
    public Map<String, Object> getAllPackages(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle packages for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> packages = oracleSchemeRepository.getAllPackages();

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("packages", packages);
            result.put("totalCount", packages.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle packages", requestId, packages.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle packages: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle packages: " + e.getMessage(), e);
        }
    }

    /**
     * Get packages from a specific Oracle schema (legacy format)
     */
    public Map<String, Object> getPackagesBySchema(String requestId, HttpServletRequest req,
                                                   String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting packages from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> packages = oracleSchemeRepository.getPackagesBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("packages", packages);
            result.put("totalCount", packages.size());
            result.put("database", "Oracle");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} packages from Oracle schema: {}",
                    requestId, packages.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle packages from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle packages from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed information about a specific package (legacy format)
     */
    public Map<String, Object> getPackageDetails(String requestId, HttpServletRequest req,
                                                 String performedBy, String packageName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle package: {} for user: {}",
                requestId, packageName, performedBy);

        try {
            Map<String, Object> packageDetails = oracleSchemeRepository.getPackageDetails(packageName);

            packageDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            packageDetails.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved details for Oracle package: {}",
                    requestId, packageName);

            return packageDetails;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for Oracle package {}: {}",
                    requestId, packageName, e.getMessage());
            throw new RuntimeException("Failed to retrieve details for Oracle package " + packageName + ": " + e.getMessage(), e);
        }
    }

    // ==================== EXISTING TRIGGER METHODS ====================

    /**
     * Get all triggers from Oracle schema (legacy format)
     */
    public Map<String, Object> getAllTriggers(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle triggers for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> triggers = oracleSchemeRepository.getAllTriggers();

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("triggers", triggers);
            result.put("totalCount", triggers.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle triggers", requestId, triggers.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle triggers: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle triggers: " + e.getMessage(), e);
        }
    }

    /**
     * Get triggers from a specific Oracle schema (legacy format)
     */
    public Map<String, Object> getTriggersBySchema(String requestId, HttpServletRequest req,
                                                   String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting triggers from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> triggers = oracleSchemeRepository.getTriggersBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("triggers", triggers);
            result.put("totalCount", triggers.size());
            result.put("database", "Oracle");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} triggers from Oracle schema: {}",
                    requestId, triggers.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle triggers from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle triggers from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed information about a specific trigger (legacy format)
     */
    public Map<String, Object> getTriggerDetails(String requestId, HttpServletRequest req,
                                                 String performedBy, String triggerName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle trigger: {} for user: {}",
                requestId, triggerName, performedBy);

        try {
            Map<String, Object> triggerDetails = oracleSchemeRepository.getTriggerDetails(triggerName);

            triggerDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            triggerDetails.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved details for Oracle trigger: {}",
                    requestId, triggerName);

            return triggerDetails;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for Oracle trigger {}: {}",
                    requestId, triggerName, e.getMessage());
            throw new RuntimeException("Failed to retrieve details for Oracle trigger " + triggerName + ": " + e.getMessage(), e);
        }
    }

    // ==================== EXISTING SYNONYM METHODS ====================

    /**
     * Get all synonyms from Oracle schema (legacy format)
     */
    public Map<String, Object> getAllSynonyms(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle synonyms for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> synonyms = oracleSchemeRepository.getAllSynonyms();

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("synonyms", synonyms);
            result.put("totalCount", synonyms.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle synonyms", requestId, synonyms.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle synonyms: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle synonyms: " + e.getMessage(), e);
        }
    }

    /**
     * Get synonyms from a specific Oracle schema (legacy format)
     */
    public Map<String, Object> getSynonymsBySchema(String requestId, HttpServletRequest req,
                                                   String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting synonyms from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> synonyms = oracleSchemeRepository.getSynonymsBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("synonyms", synonyms);
            result.put("totalCount", synonyms.size());
            result.put("database", "Oracle");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} synonyms from Oracle schema: {}",
                    requestId, synonyms.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle synonyms from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle synonyms from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    // ==================== EXISTING SEQUENCE METHODS ====================

    /**
     * Get all sequences from Oracle schema (legacy format)
     */
    public Map<String, Object> getAllSequences(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle sequences for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> sequences = oracleSchemeRepository.getAllSequences();

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("sequences", sequences);
            result.put("totalCount", sequences.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle sequences", requestId, sequences.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle sequences: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle sequences: " + e.getMessage(), e);
        }
    }

    /**
     * Get sequences from a specific Oracle schema (legacy format)
     */
    public Map<String, Object> getSequencesBySchema(String requestId, HttpServletRequest req,
                                                    String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting sequences from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> sequences = oracleSchemeRepository.getSequencesBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("sequences", sequences);
            result.put("totalCount", sequences.size());
            result.put("database", "Oracle");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} sequences from Oracle schema: {}",
                    requestId, sequences.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle sequences from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle sequences from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed information about a specific sequence (legacy format)
     */
    public Map<String, Object> getSequenceDetails(String requestId, HttpServletRequest req,
                                                  String performedBy, String sequenceName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle sequence: {} for user: {}",
                requestId, sequenceName, performedBy);

        try {
            Map<String, Object> sequenceDetails = oracleSchemeRepository.getSequenceDetails(sequenceName);

            sequenceDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            sequenceDetails.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved details for Oracle sequence: {}",
                    requestId, sequenceName);

            return sequenceDetails;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for Oracle sequence {}: {}",
                    requestId, sequenceName, e.getMessage());
            throw new RuntimeException("Failed to retrieve details for Oracle sequence " + sequenceName + ": " + e.getMessage(), e);
        }
    }

    // ==================== EXISTING TYPE METHODS ====================

    /**
     * Get all types from Oracle schema (legacy format)
     */
    public Map<String, Object> getAllTypes(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle types for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> types = oracleSchemeRepository.getAllTypes();

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("types", types);
            result.put("totalCount", types.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle types", requestId, types.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle types: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle types: " + e.getMessage(), e);
        }
    }

    /**
     * Get types from a specific Oracle schema (legacy format)
     */
    public Map<String, Object> getTypesBySchema(String requestId, HttpServletRequest req,
                                                String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting types from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> types = oracleSchemeRepository.getTypesBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("types", types);
            result.put("totalCount", types.size());
            result.put("database", "Oracle");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} types from Oracle schema: {}",
                    requestId, types.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle types from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle types from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed information about a specific type (legacy format)
     */
    public Map<String, Object> getTypeDetails(String requestId, HttpServletRequest req,
                                              String performedBy, String typeName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle type: {} for user: {}",
                requestId, typeName, performedBy);

        try {
            Map<String, Object> typeDetails = oracleSchemeRepository.getTypeDetails(typeName);

            typeDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            typeDetails.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved details for Oracle type: {}",
                    requestId, typeName);

            return typeDetails;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for Oracle type {}: {}",
                    requestId, typeName, e.getMessage());
            throw new RuntimeException("Failed to retrieve details for Oracle type " + typeName + ": " + e.getMessage(), e);
        }
    }

    // ==================== EXISTING DATABASE LINK METHODS ====================

    /**
     * Get all database links from Oracle schema (legacy format)
     */
    public Map<String, Object> getAllDbLinks(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle database links for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> dbLinks = oracleSchemeRepository.getAllDbLinks();

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("dbLinks", dbLinks);
            result.put("totalCount", dbLinks.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle database links", requestId, dbLinks.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle database links: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle database links: " + e.getMessage(), e);
        }
    }

    /**
     * Get database links from a specific Oracle schema (legacy format)
     */
    public Map<String, Object> getDbLinksBySchema(String requestId, HttpServletRequest req,
                                                  String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting database links from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> dbLinks = oracleSchemeRepository.getDbLinksBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("dbLinks", dbLinks);
            result.put("totalCount", dbLinks.size());
            result.put("database", "Oracle");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} database links from Oracle schema: {}",
                    requestId, dbLinks.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle database links from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle database links from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    // ==================== EXISTING GENERAL OBJECT METHODS ====================

    /**
     * Get all objects from Oracle schema (legacy format)
     */
    public Map<String, Object> getAllObjects(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle objects for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> objects = oracleSchemeRepository.getAllObjects();

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("objects", objects);
            result.put("totalCount", objects.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle objects", requestId, objects.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle objects: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle objects: " + e.getMessage(), e);
        }
    }

    /**
     * Get objects from a specific Oracle schema (legacy format)
     */
    public Map<String, Object> getObjectsBySchema(String requestId, HttpServletRequest req,
                                                  String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting objects from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> objects = oracleSchemeRepository.getObjectsBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("objects", objects);
            result.put("totalCount", objects.size());
            result.put("database", "Oracle");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} objects from Oracle schema: {}",
                    requestId, objects.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle objects from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle objects from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Search for objects by name pattern (legacy format)
     */
    public Map<String, Object> searchObjects(String requestId, HttpServletRequest req,
                                             String performedBy, String searchPattern) {
        log.info("RequestEntity ID: {}, Searching Oracle objects with pattern: {} for user: {}",
                requestId, searchPattern, performedBy);

        try {
            List<Map<String, Object>> objects = oracleSchemeRepository.searchObjects(searchPattern);

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("objects", objects);
            result.put("totalCount", objects.size());
            result.put("searchPattern", searchPattern);
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Found {} Oracle objects matching pattern: {}",
                    requestId, objects.size(), searchPattern);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error searching Oracle objects with pattern {}: {}",
                    requestId, searchPattern, e.getMessage());
            throw new RuntimeException("Failed to search Oracle objects: " + e.getMessage(), e);
        }
    }

    /**
     * Get object count by type (legacy format)
     */
    public Map<String, Object> getObjectCountByType(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting Oracle object count by type for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> objectCounts = oracleSchemeRepository.getObjectCountByType();

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("objectCounts", objectCounts);
            result.put("totalCount", objectCounts.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved object counts for {} object types", requestId, objectCounts.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle object count by type: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle object count by type: " + e.getMessage(), e);
        }
    }

    /**
     * Get invalid objects (legacy format)
     */
    public Map<String, Object> getInvalidObjects(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting invalid Oracle objects for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> invalidObjects = oracleSchemeRepository.getInvalidObjects();

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("invalidObjects", invalidObjects);
            result.put("totalCount", invalidObjects.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} invalid Oracle objects", requestId, invalidObjects.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting invalid Oracle objects: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve invalid Oracle objects: " + e.getMessage(), e);
        }
    }

    /**
     * Get objects by status (legacy format)
     */
    public Map<String, Object> getObjectsByStatus(String requestId, HttpServletRequest req,
                                                  String performedBy, String status) {
        log.info("RequestEntity ID: {}, Getting Oracle objects with status: {} for user: {}",
                requestId, status, performedBy);

        try {
            List<Map<String, Object>> objects = oracleSchemeRepository.getObjectsByStatus(status);

            // Get the actual schema name from the repository
            String currentSchema = oracleSchemeRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("objects", objects);
            result.put("totalCount", objects.size());
            result.put("status", status);
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle objects with status: {}",
                    requestId, objects.size(), status);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle objects with status {}: {}",
                    requestId, status, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle objects by status: " + e.getMessage(), e);
        }
    }

    // ==================== EXISTING DIAGNOSTIC METHODS ====================

    /**
     * Run comprehensive database diagnostics (legacy format)
     */
    public Map<String, Object> diagnoseDatabase(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Running Oracle database diagnostics for user: {}", requestId, performedBy);

        try {
            Map<String, Object> diagnostics = oracleSchemeRepository.diagnoseDatabase();

            diagnostics.put("generatedAt", java.time.LocalDateTime.now().toString());
            diagnostics.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Database diagnostics completed with status: {}",
                    requestId, diagnostics.get("diagnosticStatus"));

            return diagnostics;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error diagnosing Oracle database: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to diagnose Oracle database: " + e.getMessage(), e);
        }
    }

    /**
     * Get current database schema information
     * Endpoint: GET /plx/api/oracle/schema/info
     */
    public Map<String, Object> getCurrentSchemaInfo(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting current Oracle schema info for user: {}", requestId, performedBy);

        try {
            String currentUser = oracleSchemeRepository.getCurrentUser();
            String currentSchema = oracleSchemeRepository.getCurrentSchema();
            String dbVersion = oracleSchemeRepository.getDatabaseVersion();

            Map<String, Object> schemaInfo = new HashMap<>();
            schemaInfo.put("currentUser", currentUser);
            schemaInfo.put("currentSchema", currentSchema);
            schemaInfo.put("databaseVersion", dbVersion);

            Map<String, Object> result = new HashMap<>();
            result.put("data", schemaInfo);
            result.put("responseCode", 200);
            result.put("message", "Schema info retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved schema info: user={}, schema={}",
                    requestId, currentUser, currentSchema);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting schema info: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    /**
     * Get recent objects (modified in last N days)
     * Endpoint: GET /plx/api/oracle/schema/recent/{days}
     */
    public Map<String, Object> getRecentObjects(String requestId, HttpServletRequest req,
                                                String performedBy, int days) {
        log.info("RequestEntity ID: {}, Getting recent Oracle objects modified in last {} days for user: {}",
                requestId, days, performedBy);

        try {
            String sql = "SELECT owner, object_name, object_type, status, created, last_ddl_time " +
                    "FROM all_objects " +
                    "WHERE last_ddl_time > SYSDATE - ? AND owner = ? " +
                    "ORDER BY last_ddl_time DESC";

            String currentUser = oracleSchemeRepository.getCurrentUser();
            List<Map<String, Object>> objects = oracleSchemeRepository.executeQuery(
                    sql, 30, true).get("rows") instanceof List ?
                    (List<Map<String, Object>>) oracleSchemeRepository.executeQuery(sql, 30, true).get("rows") :
                    new ArrayList<>();

            Map<String, Object> result = new HashMap<>();
            result.put("data", objects);
            result.put("totalCount", objects.size());
            result.put("days", days);
            result.put("responseCode", 200);
            result.put("message", "Recent objects retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Found {} objects modified in last {} days",
                    requestId, objects.size(), days);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting recent objects: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Helper method to create a standard error response
     */
    private Map<String, Object> createErrorResponse(String requestId, String errorMessage) {
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("data", null);
        errorResult.put("responseCode", 500);
        errorResult.put("message", errorMessage);
        errorResult.put("requestId", requestId);
        errorResult.put("timestamp", java.time.Instant.now().toString());
        return errorResult;
    }

    /**
     * Helper method to get the current user from the repository
     */
    public String getCurrentUser() {
        try {
            return oracleSchemeRepository.getCurrentUser();
        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage());
            return "UNKNOWN";
        }
    }

    /**
     * Helper method to get the current schema from the repository
     */
    public String getCurrentSchema() {
        try {
            return oracleSchemeRepository.getCurrentSchema();
        } catch (Exception e) {
            log.error("Error getting current schema: {}", e.getMessage());
            return "UNKNOWN";
        }
    }
}