package com.usg.apiAutomation.services.schemaBrowser;

import com.usg.apiAutomation.repositories.schemaBrowser.postgresql.*;
import com.usg.apiAutomation.utils.LoggerUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostgreSQLSchemaService {

    private final PostgreSQLTableRepository tableRepository;
    private final PostgreSQLViewRepository viewRepository;
    private final PostgreSQLProcedureRepository procedureRepository;
    private final PostgreSQLFunctionRepository functionRepository;
    private final PostgreSQLOtherObjectsRepository otherObjectsRepository;
    private final PostgreSQLObjectRepository objectRepository;
    private final PostgreSQLSearchRepository searchRepository;
    private final PostgreSQLDependencyRepository dependencyRepository;
    private final PostgreSQLDDLRepository ddlRepository;
    private final PostgreSQLExecuteRepository executeRepository;

    @Autowired
    @Qualifier("PostgreSQLRepository")
    private final PostgreSQLRepository repository;

    private final LoggerUtil loggerUtil;

    @Autowired
    @Qualifier("postgresqlJdbcTemplate")
    private JdbcTemplate postgresqlJdbcTemplate;
    @Autowired
    private JdbcTemplate oracleJdbcTemplate;

    // ============================================================
    // 1. CURRENT SCHEMA INFO
    // ============================================================

    public Map<String, Object> getCurrentSchemaInfo(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting current schema info for user: {}", requestId, performedBy);

        try {
            Map<String, Object> schemaInfo = repository.getCurrentSchemaInfo();

            Map<String, Object> result = new HashMap<>();
            result.put("data", schemaInfo);
            result.put("responseCode", 200);
            result.put("message", "Current schema info retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved current schema info", requestId);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting current schema info: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 2. ALL TABLES FOR FRONTEND
    // ============================================================

    public Map<String, Object> getAllTablesForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all PostgreSQL tables for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> tables = tableRepository.getAllTablesForFrontend();

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

    // ============================================================
    // 2a. ALL TABLES FOR FRONTEND (PAGINATED)
    // ============================================================

    public Map<String, Object> getAllTablesForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated tables for frontend, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = tableRepository.getAllTablesForFrontend(page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", paginatedData.get("items"));
            data.put("pagination", Map.of(
                    "page", paginatedData.get("page"),
                    "pageSize", paginatedData.get("pageSize"),
                    "totalCount", paginatedData.get("totalCount"),
                    "totalPages", paginatedData.get("totalPages")
            ));
            data.put("totalCount", paginatedData.get("totalCount"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Tables retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated tables: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 3. TABLE DETAILS FOR FRONTEND
    // ============================================================

    public Map<String, Object> getTableDetailsForFrontend(String requestId, HttpServletRequest req,
                                                          String performedBy, String tableName) {
        log.info("RequestEntity ID: {}, Getting table details for frontend, table: {}, user: {}",
                requestId, tableName, performedBy);

        try {
            Map<String, Object> tableDetails = tableRepository.getTableDetailsForFrontend(tableName);

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

    // ============================================================
    // 3a. TABLE DETAILS FOR FRONTEND (PAGINATED COLUMNS)
    // ============================================================

    public Map<String, Object> getTableDetailsForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                   String performedBy, String tableName,
                                                                   int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated table details for frontend, table: {}, page: {}, pageSize: {}",
                requestId, tableName, page, pageSize);

        try {
            Map<String, Object> tableDetails = tableRepository.getTableDetailsForFrontend(tableName, page, pageSize);

            Map<String, Object> result = new HashMap<>();
            result.put("data", tableDetails);
            result.put("responseCode", 200);
            result.put("message", "Table details retrieved successfully with paginated columns");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated table details for {}: {}",
                    requestId, tableName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 4. TABLE DATA
    // ============================================================

    public Map<String, Object> getTableData(String requestId, HttpServletRequest req,
                                            String performedBy, String tableName,
                                            int page, int pageSize, String sortColumn, String sortDirection) {
        log.info("RequestEntity ID: {}, Getting table data for frontend, table: {}, page: {}, pageSize: {}, user: {}",
                requestId, tableName, page, pageSize, performedBy);

        try {
            Map<String, Object> tableData = tableRepository.getTableData(
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

    // ============================================================
    // 5. ALL VIEWS FOR FRONTEND
    // ============================================================

    public Map<String, Object> getAllViewsForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all PostgreSQL views for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> views = viewRepository.getAllViewsForFrontend();

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

    // ============================================================
    // 5a. ALL VIEWS FOR FRONTEND (PAGINATED)
    // ============================================================

    public Map<String, Object> getAllViewsForFrontendPaginated(String requestId, HttpServletRequest req,
                                                               String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated views for frontend, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = viewRepository.getAllViewsForFrontend(page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", paginatedData.get("items"));
            data.put("pagination", Map.of(
                    "page", paginatedData.get("page"),
                    "pageSize", paginatedData.get("pageSize"),
                    "totalCount", paginatedData.get("totalCount"),
                    "totalPages", paginatedData.get("totalPages")
            ));
            data.put("totalCount", paginatedData.get("totalCount"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Views retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated views: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 6. ALL PROCEDURES FOR FRONTEND
    // ============================================================

    public Map<String, Object> getAllProceduresForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all PostgreSQL procedures for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> procedures = procedureRepository.getAllProceduresForFrontend();

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

    // ============================================================
    // 6a. ALL PROCEDURES FOR FRONTEND (PAGINATED)
    // ============================================================

    public Map<String, Object> getAllProceduresForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                    String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated procedures for frontend, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = procedureRepository.getAllProceduresForFrontend(page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", paginatedData.get("items"));
            data.put("pagination", Map.of(
                    "page", paginatedData.get("page"),
                    "pageSize", paginatedData.get("pageSize"),
                    "totalCount", paginatedData.get("totalCount"),
                    "totalPages", paginatedData.get("totalPages")
            ));
            data.put("totalCount", paginatedData.get("totalCount"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Procedures retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated procedures: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 7. ALL FUNCTIONS FOR FRONTEND
    // ============================================================

    public Map<String, Object> getAllFunctionsForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all PostgreSQL functions for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> functions = functionRepository.getAllFunctionsForFrontend();

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

    // ============================================================
    // 7a. ALL FUNCTIONS FOR FRONTEND (PAGINATED)
    // ============================================================

    public Map<String, Object> getAllFunctionsForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                   String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated functions for frontend, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = functionRepository.getAllFunctionsForFrontend(page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", paginatedData.get("items"));
            data.put("pagination", Map.of(
                    "page", paginatedData.get("page"),
                    "pageSize", paginatedData.get("pageSize"),
                    "totalCount", paginatedData.get("totalCount"),
                    "totalPages", paginatedData.get("totalPages")
            ));
            data.put("totalCount", paginatedData.get("totalCount"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Functions retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated functions: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 8. ALL PACKAGES FOR FRONTEND (Schemas as package equivalents)
    // ============================================================

    public Map<String, Object> getAllPackagesForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all schemas (as packages equivalent) for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> schemas = repository.getAllSchemas();

            Map<String, Object> result = new HashMap<>();
            result.put("data", schemas);
            result.put("totalCount", schemas.size());
            result.put("responseCode", 200);
            result.put("message", "Schemas (package equivalents) retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} schemas as package equivalents", requestId, schemas.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting schemas: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 8a. ALL PACKAGES FOR FRONTEND (PAGINATED)
    // ============================================================

    public Map<String, Object> getAllPackagesForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                  String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated schemas for frontend, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = repository.getAllSchemasPaginated(page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", paginatedData.get("items"));
            data.put("pagination", Map.of(
                    "page", paginatedData.get("page"),
                    "pageSize", paginatedData.get("pageSize"),
                    "totalCount", paginatedData.get("totalCount"),
                    "totalPages", paginatedData.get("totalPages")
            ));
            data.put("totalCount", paginatedData.get("totalCount"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Schemas retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated schemas: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 9. ALL TRIGGERS FOR FRONTEND
    // ============================================================

    public Map<String, Object> getAllTriggersForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all PostgreSQL triggers for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> triggers = otherObjectsRepository.getAllTriggersForFrontend();

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

    // ============================================================
    // 9a. ALL TRIGGERS FOR FRONTEND (PAGINATED)
    // ============================================================

    public Map<String, Object> getAllTriggersForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                  String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated triggers for frontend, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = otherObjectsRepository.getAllTriggersForFrontend(page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", paginatedData.get("items"));
            data.put("pagination", Map.of(
                    "page", paginatedData.get("page"),
                    "pageSize", paginatedData.get("pageSize"),
                    "totalCount", paginatedData.get("totalCount"),
                    "totalPages", paginatedData.get("totalPages")
            ));
            data.put("totalCount", paginatedData.get("totalCount"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Triggers retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated triggers: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 10. ALL SYNONYMS FOR FRONTEND (PostgreSQL doesn't have synonyms)
    // ============================================================

    public Map<String, Object> getAllSynonymsForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Synonyms not supported in PostgreSQL", requestId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", new ArrayList<>());
        result.put("totalCount", 0);
        result.put("responseCode", 200);
        result.put("message", "Synonyms are not supported in PostgreSQL");
        result.put("requestId", requestId);
        result.put("timestamp", java.time.Instant.now().toString());

        return result;
    }

    // ============================================================
    // 10a. ALL SYNONYMS FOR FRONTEND (PAGINATED)
    // ============================================================

    public Map<String, Object> getAllSynonymsForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                  String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Synonyms not supported in PostgreSQL", requestId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", new ArrayList<>());
        result.put("totalCount", 0);
        result.put("responseCode", 200);
        result.put("message", "Synonyms are not supported in PostgreSQL");
        result.put("requestId", requestId);
        result.put("timestamp", java.time.Instant.now().toString());

        return result;
    }

    // ============================================================
    // 11. ALL SEQUENCES FOR FRONTEND
    // ============================================================

    public Map<String, Object> getAllSequencesForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all PostgreSQL sequences for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> sequences = otherObjectsRepository.getAllSequencesForFrontend();

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

    // ============================================================
    // 11a. ALL SEQUENCES FOR FRONTEND (PAGINATED)
    // ============================================================

    public Map<String, Object> getAllSequencesForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                   String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated sequences for frontend, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = otherObjectsRepository.getAllSequencesForFrontend(page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", paginatedData.get("items"));
            data.put("pagination", Map.of(
                    "page", paginatedData.get("page"),
                    "pageSize", paginatedData.get("pageSize"),
                    "totalCount", paginatedData.get("totalCount"),
                    "totalPages", paginatedData.get("totalPages")
            ));
            data.put("totalCount", paginatedData.get("totalCount"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Sequences retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated sequences: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 12. ALL TYPES FOR FRONTEND
    // ============================================================

    public Map<String, Object> getAllTypesForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all PostgreSQL types for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> types = otherObjectsRepository.getAllTypesForFrontend();

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

    // ============================================================
    // 12a. ALL TYPES FOR FRONTEND (PAGINATED)
    // ============================================================

    public Map<String, Object> getAllTypesForFrontendPaginated(String requestId, HttpServletRequest req,
                                                               String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated types for frontend, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = otherObjectsRepository.getAllTypesForFrontend(page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", paginatedData.get("items"));
            data.put("pagination", Map.of(
                    "page", paginatedData.get("page"),
                    "pageSize", paginatedData.get("pageSize"),
                    "totalCount", paginatedData.get("totalCount"),
                    "totalPages", paginatedData.get("totalPages")
            ));
            data.put("totalCount", paginatedData.get("totalCount"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Types retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated types: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 13. GET OBJECT DETAILS
    // ============================================================

    public Map<String, Object> getObjectDetails(String requestId, HttpServletRequest req,
                                                String performedBy, String objectName,
                                                String objectType, String schema) {
        log.info("RequestEntity ID: {}, Getting details for {}: {}, schema: {}",
                requestId, objectType, objectName, schema);

        try {
            Map<String, Object> objectDetails = objectRepository.getObjectDetails(objectName, objectType, schema);

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

    // ============================================================
    // 13a. GET OBJECT DETAILS (PAGINATED)
    // ============================================================

    public Map<String, Object> getObjectDetailsPaginated(String requestId, HttpServletRequest req,
                                                         String performedBy, String objectName,
                                                         String objectType, String schema,
                                                         int page, int pageSize, boolean includeCounts) {
        log.info("RequestEntity ID: {}, Getting paginated details for {}: {}, schema: {}, page: {}, pageSize: {}",
                requestId, objectType, objectName, schema, page, pageSize);

        try {
            Map<String, Object> objectDetails = objectRepository.getObjectDetailsPaginated(
                    objectName, objectType, schema, page, pageSize, includeCounts);

            Map<String, Object> pagination = new HashMap<>();
            pagination.put("page", page);
            pagination.put("pageSize", pageSize);
            pagination.put("totalItems", objectDetails.get("totalCount"));
            pagination.put("totalPages", calculateTotalPages(
                    getLongValue(objectDetails.get("totalCount")), pageSize));

            objectDetails.put("pagination", pagination);

            Map<String, Object> result = new HashMap<>();
            result.put("data", objectDetails);
            result.put("responseCode", 200);
            result.put("message", "Object details retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved paginated details for {}: {}, total items: {}",
                    requestId, objectType, objectName, objectDetails.get("totalCount"));

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated details for {} {}: {}",
                    requestId, objectType, objectName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 14. GET OBJECT DDL
    // ============================================================

    public Map<String, Object> getObjectDDL(String requestId, HttpServletRequest req,
                                            String performedBy, String objectName, String objectType) {
        log.info("RequestEntity ID: {}, Getting DDL for {}: {}, user: {}",
                requestId, objectType, objectName, performedBy);

        try {
            Map<String, Object> ddlResult = ddlRepository.getObjectDDLForFrontend(objectName, objectType);

            Map<String, Object> result = new HashMap<>();
            result.put("data", ddlResult.get("ddl"));
            result.put("status", ddlResult.get("status"));
            result.put("method", ddlResult.get("method"));
            result.put("executionTimeMs", ddlResult.get("executionTimeMs"));
            result.put("responseCode", 200);
            result.put("message", "DDL retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved DDL for {}: {} using method: {}",
                    requestId, objectType, objectName, ddlResult.get("method"));

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting DDL for {} {}: {}",
                    requestId, objectType, objectName, e.getMessage());

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("data", "-- Error retrieving DDL: " + e.getMessage());
            errorResult.put("status", "ERROR");
            errorResult.put("responseCode", 500);
            errorResult.put("message", e.getMessage());
            errorResult.put("requestId", requestId);
            errorResult.put("timestamp", java.time.Instant.now().toString());

            return errorResult;
        }
    }

    // ============================================================
    // 15. LAZY LOADING METHOD FOR SYNONYM TARGET DETAILS (Not supported)
    // ============================================================

    public Map<String, Object> getSynonymTargetDetails(String requestId, HttpServletRequest req,
                                                       String performedBy, String synonymName) {
        log.info("RequestEntity ID: {}, Synonyms not supported in PostgreSQL", requestId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", new HashMap<>());
        result.put("responseCode", 200);
        result.put("message", "Synonyms are not supported in PostgreSQL");
        result.put("requestId", requestId);
        result.put("timestamp", java.time.Instant.now().toString());

        return result;
    }

    // ============================================================
    // 16. PAGINATED SEARCH FOR FRONTEND
    // ============================================================

    public Map<String, Object> searchObjectsForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                 String performedBy, String searchQuery,
                                                                 String searchType, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Paginated search for frontend with query: {}, type: {}, page: {}, pageSize: {}",
                requestId, searchQuery, searchType, page, pageSize);

        try {
            Map<String, Object> searchResults = searchRepository.searchObjectsForFrontend(
                    searchQuery, searchType, page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", searchResults.get("items"));
            data.put("pagination", Map.of(
                    "page", searchResults.get("page"),
                    "pageSize", searchResults.get("pageSize"),
                    "totalCount", searchResults.get("totalCount"),
                    "totalPages", searchResults.get("totalPages")
            ));
            data.put("totalCount", searchResults.get("totalCount"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Search completed successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Found {} objects matching query: {}",
                    requestId, searchResults.get("totalCount"), searchQuery);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error searching objects: {}", requestId, e.getMessage());

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("items", new ArrayList<>());
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

    // ============================================================
    // 17. ADVANCED TABLE DATA SERVICE METHOD
    // ============================================================

    public Map<String, Object> getTableDataAdvanced(String requestId, HttpServletRequest req,
                                                    String performedBy, String tableName,
                                                    int page, int pageSize, String sortColumn,
                                                    String sortDirection, String filter) {
        log.info("RequestEntity ID: {}, Getting advanced table data for: {}, page: {}, pageSize: {}, filter: {}",
                requestId, tableName, page, pageSize, filter);

        try {
            Map<String, Object> tableData = tableRepository.getTableDataAdvanced(
                    tableName, page, pageSize, sortColumn, sortDirection, filter);

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
            log.error("RequestEntity ID: {}, Error getting advanced table data for {}: {}",
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

    // ============================================================
    // 18. PAGINATED PROCEDURE PARAMETERS SERVICE METHOD
    // ============================================================

    public Map<String, Object> getProcedureParametersPaginated(String requestId, HttpServletRequest req,
                                                               String performedBy, String procedureName,
                                                               String schema, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated parameters for procedure: {}, page: {}, pageSize: {}",
                requestId, procedureName, page, pageSize);

        try {
            Map<String, Object> parameters = procedureRepository.getProcedureParametersPaginated(
                    procedureName, schema, page, pageSize);

            Map<String, Object> result = new HashMap<>();
            result.put("data", parameters);
            result.put("responseCode", 200);
            result.put("message", "Procedure parameters retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            List<?> items = (List<?>) parameters.get("items");
            log.info("RequestEntity ID: {}, Retrieved {} parameters for procedure: {}",
                    requestId, items != null ? items.size() : 0, procedureName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting parameters for procedure {}: {}",
                    requestId, procedureName, e.getMessage(), e);

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("parameters", new ArrayList<>());
            errorData.put("totalCount", 0);
            errorData.put("page", page);
            errorData.put("pageSize", pageSize);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("data", errorData);
            errorResult.put("responseCode", 500);
            errorResult.put("message", e.getMessage());
            errorResult.put("requestId", requestId);
            errorResult.put("timestamp", java.time.Instant.now().toString());

            return errorResult;
        }
    }

    // ============================================================
    // 19. PAGINATED FUNCTION PARAMETERS SERVICE METHOD
    // ============================================================

    public Map<String, Object> getFunctionParametersPaginated(String requestId, HttpServletRequest req,
                                                              String performedBy, String functionName,
                                                              String schema, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated parameters for function: {}, page: {}, pageSize: {}",
                requestId, functionName, page, pageSize);

        try {
            Map<String, Object> parameters = functionRepository.getFunctionParametersPaginated(
                    functionName, schema, page, pageSize);

            Map<String, Object> result = new HashMap<>();
            result.put("data", parameters);
            result.put("responseCode", 200);
            result.put("message", "Function parameters retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} parameters for function: {}",
                    requestId, ((List<?>) parameters.get("parameters")).size(), functionName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting parameters for function {}: {}",
                    requestId, functionName, e.getMessage());

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("parameters", new ArrayList<>());
            errorData.put("totalCount", 0);
            errorData.put("page", page);
            errorData.put("pageSize", pageSize);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("data", errorData);
            errorResult.put("responseCode", 500);
            errorResult.put("message", e.getMessage());
            errorResult.put("requestId", requestId);
            errorResult.put("timestamp", java.time.Instant.now().toString());

            return errorResult;
        }
    }

    // ============================================================
    // 20. PAGINATED PACKAGE ITEMS SERVICE METHOD (Schemas items)
    // ============================================================

    public Map<String, Object> getPackageItemsPaginated(String requestId, HttpServletRequest req,
                                                        String performedBy, String packageName,
                                                        String schema, String itemType,
                                                        int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated items for schema: {}, type: {}, page: {}, pageSize: {}",
                requestId, packageName, itemType, page, pageSize);

        try {
            // Since PostgreSQL doesn't have packages, return items from schema
            Map<String, Object> items = repository.getSchemaItemsPaginated(
                    packageName, itemType, page, pageSize);

            Map<String, Object> result = new HashMap<>();
            result.put("data", items);
            result.put("responseCode", 200);
            result.put("message", "Schema items retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} items for schema: {}",
                    requestId, ((List<?>) items.get("items")).size(), packageName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting items for schema {}: {}",
                    requestId, packageName, e.getMessage());

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("items", new ArrayList<>());
            errorData.put("totalCount", 0);
            errorData.put("page", page);
            errorData.put("pageSize", pageSize);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("data", errorData);
            errorResult.put("responseCode", 500);
            errorResult.put("message", e.getMessage());
            errorResult.put("requestId", requestId);
            errorResult.put("timestamp", java.time.Instant.now().toString());

            return errorResult;
        }
    }

    // ============================================================
    // 21. PAGINATED TABLE COLUMNS SERVICE METHOD
    // ============================================================

    public Map<String, Object> getTableColumnsPaginated(String requestId, HttpServletRequest req,
                                                        String performedBy, String tableName,
                                                        String schema, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated columns for table: {}, page: {}, pageSize: {}",
                requestId, tableName, page, pageSize);

        try {
            Map<String, Object> columns = tableRepository.getTableColumnsPaginated(
                    tableName, schema, page, pageSize);

            Map<String, Object> result = new HashMap<>();
            result.put("data", columns);
            result.put("responseCode", 200);
            result.put("message", "Table columns retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} columns for table: {}",
                    requestId, ((List<?>) columns.get("items")).size(), tableName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting columns for table {}: {}",
                    requestId, tableName, e.getMessage());

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("items", new ArrayList<>());
            errorData.put("totalCount", 0);
            errorData.put("page", page);
            errorData.put("pageSize", pageSize);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("data", errorData);
            errorResult.put("responseCode", 500);
            errorResult.put("message", e.getMessage());
            errorResult.put("requestId", requestId);
            errorResult.put("timestamp", java.time.Instant.now().toString());

            return errorResult;
        }
    }

    // ============================================================
    // 22. GET OBJECT COUNTS ONLY SERVICE METHOD
    // ============================================================

    public Map<String, Object> getObjectCountsOnly(String requestId, HttpServletRequest req,
                                                   String performedBy, String objectName,
                                                   String objectType, String schema) {
        log.info("RequestEntity ID: {}, Getting counts only for {}: {}, schema: {}",
                requestId, objectType, objectName, schema);

        try {
            Map<String, Object> counts = objectRepository.getObjectCountsOnly(
                    objectName, objectType, schema);

            Map<String, Object> result = new HashMap<>();
            result.put("data", counts);
            result.put("responseCode", 200);
            result.put("message", "Object counts retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved counts for {}: {} - total columns: {}, total parameters: {}",
                    requestId, objectType, objectName, counts.get("totalColumns"), counts.get("totalParameters"));

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting counts for {} {}: {}",
                    requestId, objectType, objectName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 23. USED BY / DEPENDENT OBJECTS SERVICE METHODS
    // ============================================================

    public Map<String, Object> getUsedBy(String requestId, HttpServletRequest req,
                                         String performedBy, String objectName,
                                         String objectType, String schema) {
        log.info("RequestEntity ID: {}, Getting used by for {}: {}, schema: {}",
                requestId, objectType, objectName, schema);

        try {
            List<Map<String, Object>> usedBy = dependencyRepository.getUsedBy(objectName, objectType, schema);

            Map<String, Object> data = new HashMap<>();
            data.put("items", usedBy);
            data.put("totalCount", usedBy.size());
            data.put("objectName", objectName);
            data.put("objectType", objectType);
            data.put("schema", schema != null ? schema : repository.getCurrentSchema());

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Used by objects retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Found {} objects that depend on {}: {}",
                    requestId, usedBy.size(), objectType, objectName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting used by for {} {}: {}",
                    requestId, objectType, objectName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getUsedByPaginated(String requestId, HttpServletRequest req,
                                                  String performedBy, String objectName,
                                                  String objectType, String schema,
                                                  int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated used by for {}: {}, page: {}, pageSize: {}",
                requestId, objectType, objectName, page, pageSize);

        try {
            Map<String, Object> paginatedData = dependencyRepository.getUsedByPaginated(
                    objectName, objectType, schema, page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", paginatedData.get("items"));
            data.put("pagination", Map.of(
                    "page", paginatedData.get("page"),
                    "pageSize", paginatedData.get("pageSize"),
                    "totalCount", paginatedData.get("totalCount"),
                    "totalPages", paginatedData.get("totalPages")
            ));
            data.put("totalCount", paginatedData.get("totalCount"));
            data.put("objectName", objectName);
            data.put("objectType", objectType);
            data.put("schema", schema != null ? schema : repository.getCurrentSchema());

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Used by objects retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} of {} total objects that depend on {}: {}",
                    requestId, ((List<?>) paginatedData.get("items")).size(),
                    paginatedData.get("totalCount"), objectType, objectName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated used by for {} {}: {}",
                    requestId, objectType, objectName, e.getMessage());

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("items", new ArrayList<>());
            errorData.put("totalCount", 0);
            errorData.put("objectName", objectName);
            errorData.put("objectType", objectType);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("data", errorData);
            errorResult.put("responseCode", 500);
            errorResult.put("message", e.getMessage());
            errorResult.put("requestId", requestId);
            errorResult.put("timestamp", java.time.Instant.now().toString());

            return errorResult;
        }
    }

    public Map<String, Object> getDependencyHierarchy(String requestId, HttpServletRequest req,
                                                      String performedBy, String objectName,
                                                      String objectType, String schema) {
        log.info("RequestEntity ID: {}, Getting dependency hierarchy for {}: {}, schema: {}",
                requestId, objectType, objectName, schema);

        try {
            Map<String, Object> hierarchy = dependencyRepository.getDependencyHierarchy(
                    objectName, objectType, schema);

            hierarchy.put("generatedAt", java.time.LocalDateTime.now().toString());
            hierarchy.put("generatedBy", performedBy);

            Map<String, Object> result = new HashMap<>();
            result.put("data", hierarchy);
            result.put("responseCode", 200);
            result.put("message", "Dependency hierarchy retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved dependency hierarchy for {}: {} - depends on: {}, used by: {}",
                    requestId, objectType, objectName,
                    hierarchy.get("dependsOnCount"), hierarchy.get("usedByCount"));

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting dependency hierarchy for {} {}: {}",
                    requestId, objectType, objectName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getUsedByCount(String requestId, HttpServletRequest req,
                                              String performedBy, String objectName,
                                              String objectType, String schema) {
        log.info("RequestEntity ID: {}, Getting used by count for {}: {}, schema: {}",
                requestId, objectType, objectName, schema);

        try {
            int count = dependencyRepository.getUsedByCount(objectName, objectType, schema);

            Map<String, Object> data = new HashMap<>();
            data.put("count", count);
            data.put("objectName", objectName);
            data.put("objectType", objectType);
            data.put("schema", schema != null ? schema : repository.getCurrentSchema());

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Used by count retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Found {} objects that depend on {}: {}",
                    requestId, count, objectType, objectName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting used by count for {} {}: {}",
                    requestId, objectType, objectName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getUsedBySummary(String requestId, HttpServletRequest req,
                                                String performedBy, String objectName,
                                                String objectType, String schema) {
        log.info("RequestEntity ID: {}, Getting used by summary for {}: {}, schema: {}",
                requestId, objectType, objectName, schema);

        try {
            Map<String, Object> summary = dependencyRepository.getUsedBySummary(
                    objectName, objectType, schema);

            summary.put("objectName", objectName);
            summary.put("objectType", objectType);
            summary.put("schema", schema != null ? schema : repository.getCurrentSchema());
            summary.put("generatedAt", java.time.LocalDateTime.now().toString());
            summary.put("generatedBy", performedBy);

            Map<String, Object> result = new HashMap<>();
            result.put("data", summary);
            result.put("responseCode", 200);
            result.put("message", "Used by summary retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved used by summary for {}: {} - total: {}",
                    requestId, objectType, objectName, summary.get("totalCount"));

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting used by summary for {} {}: {}",
                    requestId, objectType, objectName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getTableUsedBy(String requestId, HttpServletRequest req,
                                              String performedBy, String tableName, String schema) {
        log.info("RequestEntity ID: {}, Getting used by for table: {}, schema: {}",
                requestId, tableName, schema);
        return getUsedBy(requestId, req, performedBy, tableName, "TABLE", schema);
    }

    public Map<String, Object> getProcedureUsedBy(String requestId, HttpServletRequest req,
                                                  String performedBy, String procedureName, String schema) {
        log.info("RequestEntity ID: {}, Getting used by for procedure: {}, schema: {}",
                requestId, procedureName, schema);
        return getUsedBy(requestId, req, performedBy, procedureName, "PROCEDURE", schema);
    }

    public Map<String, Object> getFunctionUsedBy(String requestId, HttpServletRequest req,
                                                 String performedBy, String functionName, String schema) {
        log.info("RequestEntity ID: {}, Getting used by for function: {}, schema: {}",
                requestId, functionName, schema);
        return getUsedBy(requestId, req, performedBy, functionName, "FUNCTION", schema);
    }

    public Map<String, Object> getPackageUsedBy(String requestId, HttpServletRequest req,
                                                String performedBy, String packageName, String schema) {
        log.info("RequestEntity ID: {}, Getting used by for schema: {}, schema: {}",
                requestId, packageName, schema);
        return getUsedBy(requestId, req, performedBy, packageName, "SCHEMA", schema);
    }

    public Map<String, Object> getViewUsedBy(String requestId, HttpServletRequest req,
                                             String performedBy, String viewName, String schema) {
        log.info("RequestEntity ID: {}, Getting used by for view: {}, schema: {}",
                requestId, viewName, schema);
        return getUsedBy(requestId, req, performedBy, viewName, "VIEW", schema);
    }

    public Map<String, Object> getTriggerUsedBy(String requestId, HttpServletRequest req,
                                                String performedBy, String triggerName, String schema) {
        log.info("RequestEntity ID: {}, Getting used by for trigger: {}, schema: {}",
                requestId, triggerName, schema);
        return getUsedBy(requestId, req, performedBy, triggerName, "TRIGGER", schema);
    }

    // ============================================================
    // 24. ENHANCED SYNONYM METHODS (Not supported)
    // ============================================================

    public Map<String, Object> getAllSynonymsWithDetails(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Synonyms not supported in PostgreSQL", requestId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", new ArrayList<>());
        result.put("totalCount", 0);
        result.put("responseCode", 200);
        result.put("message", "Synonyms are not supported in PostgreSQL");
        result.put("requestId", requestId);
        result.put("timestamp", java.time.Instant.now().toString());

        return result;
    }

    public Map<String, Object> getSynonymsByTargetType(String requestId, HttpServletRequest req,
                                                       String performedBy, String targetType) {
        log.info("RequestEntity ID: {}, Synonyms not supported in PostgreSQL", requestId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", new ArrayList<>());
        result.put("totalCount", 0);
        result.put("targetType", targetType);
        result.put("responseCode", 200);
        result.put("message", "Synonyms are not supported in PostgreSQL");
        result.put("requestId", requestId);
        result.put("timestamp", java.time.Instant.now().toString());

        return result;
    }

    public Map<String, Object> getSynonymDetailsEnhanced(String requestId, HttpServletRequest req,
                                                         String performedBy, String synonymName) {
        log.info("RequestEntity ID: {}, Synonyms not supported in PostgreSQL", requestId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", new HashMap<>());
        result.put("responseCode", 200);
        result.put("message", "Synonyms are not supported in PostgreSQL");
        result.put("requestId", requestId);
        result.put("timestamp", java.time.Instant.now().toString());

        return result;
    }

    public Map<String, Object> resolveSynonym(String requestId, HttpServletRequest req,
                                              String performedBy, String synonymName) {
        log.info("RequestEntity ID: {}, Synonyms not supported in PostgreSQL", requestId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", new HashMap<>());
        result.put("responseCode", 200);
        result.put("message", "Synonyms are not supported in PostgreSQL");
        result.put("requestId", requestId);
        result.put("timestamp", java.time.Instant.now().toString());

        return result;
    }

    public Map<String, Object> validateSynonym(String requestId, HttpServletRequest req,
                                               String performedBy, String synonymName) {
        log.info("RequestEntity ID: {}, Synonyms not supported in PostgreSQL", requestId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", new HashMap<>());
        result.put("responseCode", 200);
        result.put("message", "Synonyms are not supported in PostgreSQL");
        result.put("requestId", requestId);
        result.put("timestamp", java.time.Instant.now().toString());

        return result;
    }

    // ============================================================
    // 25. ENHANCED OBJECT METHODS
    // ============================================================

    public Map<String, Object> getObjectDetailsByNameAndType(String requestId, HttpServletRequest req,
                                                             String performedBy, String objectName,
                                                             String objectType, String schema) {
        log.info("RequestEntity ID: {}, Getting details for {}: {}, schema: {}, user: {}",
                requestId, objectType, objectName, schema, performedBy);

        try {
            Map<String, Object> objectDetails = objectRepository.getObjectDetailsByNameAndType(
                    objectName, objectType, schema);

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

    public Map<String, Object> resolveSynonymCustom(String requestId, String synonymName,
                                                    String owner, String performedBy) {
        log.info("RequestEntity ID: {}, Synonyms not supported in PostgreSQL", requestId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", new HashMap<>());
        result.put("responseCode", 200);
        result.put("message", "Synonyms are not supported in PostgreSQL");
        result.put("requestId", requestId);
        result.put("timestamp", Instant.now().toString());

        return result;
    }

    public Map<String, Object> getObjectProperties(String requestId, String objectName,
                                                   String objectType, String owner, String performedBy) {
        log.info("RequestEntity ID: {}, Getting properties for {}: {}", requestId, objectType, objectName);

        try {
            String resolvedSchema = resolveSchema(owner);
            Map<String, Object> properties = new HashMap<>();

            switch (objectType.toUpperCase()) {
                case "TABLE":
                    properties = tableRepository.getTableDetails(resolvedSchema, objectName);
                    properties.remove("columns");
                    properties.remove("constraints");
                    properties.remove("indexes");
                    break;

                case "VIEW":
                    properties = viewRepository.getViewDetails(resolvedSchema, objectName);
                    properties.remove("columns");
                    break;

                case "MATERIALIZED VIEW":
                    properties = viewRepository.getMaterializedViewDetails(resolvedSchema, objectName);
                    break;

                case "PROCEDURE":
                    properties = procedureRepository.getProcedureDetails(resolvedSchema, objectName);
                    properties.remove("parameters");
                    properties.remove("sourceCode");
                    break;

                case "FUNCTION":
                    properties = functionRepository.getFunctionDetails(resolvedSchema, objectName);
                    properties.remove("parameters");
                    properties.remove("sourceCode");
                    break;

                case "SEQUENCE":
                    properties = otherObjectsRepository.getSequenceDetails(resolvedSchema, objectName);
                    break;

                case "TRIGGER":
                    properties = otherObjectsRepository.getTriggerDetails(resolvedSchema, objectName);
                    break;

                default:
                    properties = objectRepository.getBasicObjectInfo(resolvedSchema, objectName, objectType);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("data", properties);
            result.put("responseCode", 200);
            result.put("message", "Properties retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting properties: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getObjectColumnsPaginated(String requestId, String objectName,
                                                         String objectType, String owner,
                                                         int page, int pageSize, String performedBy) {
        log.info("RequestEntity ID: {}, Getting {} columns for {}: {}, page: {}",
                requestId, objectType, objectName, page);

        try {
            String resolvedSchema = resolveSchema(owner);
            String upperType = objectType.toUpperCase();
            Map<String, Object> result = new HashMap<>();

            if (upperType.equals("TABLE") || upperType.equals("VIEW") || upperType.equals("MATERIALIZED VIEW")) {
                Map<String, Object> columnsResult = tableRepository.getTableColumnsPaginated(
                        objectName, resolvedSchema, page, pageSize);
                result.putAll(columnsResult);
                result.put("itemType", "column");

            } else if (upperType.equals("PROCEDURE") || upperType.equals("FUNCTION")) {
                Map<String, Object> paramsResult = procedureRepository.getProcedureParametersPaginated(
                        objectName, resolvedSchema, page, pageSize);
                result.putAll(paramsResult);
                result.put("itemType", "parameter");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("data", result);
            response.put("responseCode", 200);
            response.put("message", "Columns retrieved successfully");
            response.put("requestId", requestId);
            response.put("timestamp", Instant.now().toString());

            return response;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting columns: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getObjectBasicInfo(String requestId, String objectName,
                                                  String objectType, String owner, String performedBy) {
        log.info("RequestEntity ID: {}, Getting detailed info for {}: {}", requestId, objectType, objectName);

        try {
            Map<String, Object> details = new HashMap<>();
            String resolvedSchema = resolveSchema(owner);

            details.put("objectName", objectName);
            details.put("objectType", objectType);
            details.put("owner", resolvedSchema);
            details.put("queryTime", Instant.now().toString());
            details.put("performedBy", performedBy);

            // Get detailed object info with all metadata
            Map<String, Object> objectInfo = objectRepository.getDetailedObjectInfo(resolvedSchema, objectName, objectType);
            details.putAll(objectInfo);

            // Add additional metadata like size information
            Map<String, Object> sizeInfo = getObjectSizeInfo(resolvedSchema, objectName);
            details.put("sizeInfo", sizeInfo);

            Map<String, Object> result = new HashMap<>();
            result.put("data", details);
            result.put("responseCode", 200);
            result.put("message", "Comprehensive object info retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting comprehensive object info: {}",
                    requestId, e.getMessage(), e);
            return createErrorResponse(requestId, "Failed to retrieve object info: " + e.getMessage());
        }
    }

    private Map<String, Object> getObjectSizeInfo(String owner, String objectName) {
        String sql = "SELECT " +
                "    pg_size_pretty(pg_total_relation_size(c.oid)) as total_size, " +
                "    pg_size_pretty(pg_relation_size(c.oid)) as table_size, " +
                "    pg_size_pretty(pg_indexes_size(c.oid)) as indexes_size, " +
                "    pg_size_pretty(pg_table_size(c.oid)) as toast_size " +
                "FROM pg_class c " +
                "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                "WHERE n.nspname = ? AND c.relname = ?";

        try {
            return oracleJdbcTemplate.queryForMap(sql, owner, objectName);
        } catch (Exception e) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("total_size", "unknown");
            empty.put("table_size", "unknown");
            empty.put("indexes_size", "unknown");
            empty.put("toast_size", "unknown");
            return empty;
        }
    }



    public Map<String, Object> validateObject(String requestId, HttpServletRequest req,
                                              String performedBy, String objectName,
                                              String objectType, String schema) {
        log.info("RequestEntity ID: {}, Validating {}: {}, schema: {}, user: {}",
                requestId, objectType, objectName, schema, performedBy);

        try {
            Map<String, Object> validation = objectRepository.validateObject(objectName, objectType, schema);

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

    // ============================================================
    // 26. ENHANCED SEARCH METHODS
    // ============================================================

    public Map<String, Object> comprehensiveSearch(String requestId, HttpServletRequest req,
                                                   String performedBy, String searchPattern) {
        log.info("RequestEntity ID: {}, Performing comprehensive search with pattern: {} for user: {}",
                requestId, searchPattern, performedBy);

        try {
            List<Map<String, Object>> results = searchRepository.comprehensiveSearch(searchPattern);

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

    // ============================================================
    // 27. GET OBJECT SIZE
    // ============================================================

    public Map<String, Object> getObjectSize(String requestId, HttpServletRequest req,
                                             String performedBy, String objectName, String objectType) {
        log.info("RequestEntity ID: {}, Getting size for {}: {}, user: {}",
                requestId, objectType, objectName, performedBy);

        try {
            Map<String, Object> sizeInfo = objectRepository.getObjectSize(objectName, objectType);

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

    // ============================================================
    // 28. EXECUTE QUERY METHODS
    // ============================================================

    public Map<String, Object> executeQuery(String requestId, HttpServletRequest req,
                                            String performedBy, String query,
                                            int timeoutSeconds, boolean readOnly) {
        log.info("RequestEntity ID: {}, Executing query, timeout: {}, readOnly: {}, user: {}",
                requestId, timeoutSeconds, readOnly, performedBy);

        try {
            Map<String, Object> queryResults = executeRepository.executeQuery(query, timeoutSeconds, readOnly);

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

    // ============================================================
    // 29. PAGINATED METHODS FOR LARGE DATASETS
    // ============================================================

    public Map<String, Object> getTablesPaginated(String requestId, HttpServletRequest req,
                                                  String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated tables, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = tableRepository.getTablesPaginated(page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", paginatedData.get("items"));
            data.put("pagination", Map.of(
                    "page", paginatedData.get("page"),
                    "pageSize", paginatedData.get("pageSize"),
                    "totalCount", paginatedData.get("totalCount"),
                    "totalPages", paginatedData.get("totalPages")
            ));
            data.put("totalCount", paginatedData.get("totalCount"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Tables retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated tables: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getViewsPaginated(String requestId, HttpServletRequest req,
                                                 String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated views, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = viewRepository.getViewsPaginated(page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", paginatedData.get("items"));
            data.put("pagination", Map.of(
                    "page", paginatedData.get("page"),
                    "pageSize", paginatedData.get("pageSize"),
                    "totalCount", paginatedData.get("totalCount"),
                    "totalPages", paginatedData.get("totalPages")
            ));
            data.put("totalCount", paginatedData.get("totalCount"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Views retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated views: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getProceduresPaginated(String requestId, HttpServletRequest req,
                                                      String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated procedures, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = procedureRepository.getProceduresPaginated(page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", paginatedData.get("items"));
            data.put("pagination", Map.of(
                    "page", paginatedData.get("page"),
                    "pageSize", paginatedData.get("pageSize"),
                    "totalCount", paginatedData.get("totalCount"),
                    "totalPages", paginatedData.get("totalPages")
            ));
            data.put("totalCount", paginatedData.get("totalCount"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Procedures retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated procedures: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getFunctionsPaginated(String requestId, HttpServletRequest req,
                                                     String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated functions, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = functionRepository.getFunctionsPaginated(page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", paginatedData.get("items"));
            data.put("pagination", Map.of(
                    "page", paginatedData.get("page"),
                    "pageSize", paginatedData.get("pageSize"),
                    "totalCount", paginatedData.get("totalCount"),
                    "totalPages", paginatedData.get("totalPages")
            ));
            data.put("totalCount", paginatedData.get("totalCount"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Functions retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated functions: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getPackagesPaginated(String requestId, HttpServletRequest req,
                                                    String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated schemas, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = repository.getAllSchemasPaginated(page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", paginatedData.get("items"));
            data.put("pagination", Map.of(
                    "page", paginatedData.get("page"),
                    "pageSize", paginatedData.get("pageSize"),
                    "totalCount", paginatedData.get("totalCount"),
                    "totalPages", paginatedData.get("totalPages")
            ));
            data.put("totalCount", paginatedData.get("totalCount"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Schemas retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated schemas: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getSynonymsPaginated(String requestId, HttpServletRequest req,
                                                    String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Synonyms not supported in PostgreSQL", requestId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", new HashMap<>());
        result.put("responseCode", 200);
        result.put("message", "Synonyms are not supported in PostgreSQL");
        result.put("requestId", requestId);
        result.put("timestamp", java.time.Instant.now().toString());

        return result;
    }

    public Map<String, Object> getSequencesPaginated(String requestId, HttpServletRequest req,
                                                     String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated sequences, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = otherObjectsRepository.getSequencesPaginated(page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", paginatedData.get("items"));
            data.put("pagination", Map.of(
                    "page", paginatedData.get("page"),
                    "pageSize", paginatedData.get("pageSize"),
                    "totalCount", paginatedData.get("totalCount"),
                    "totalPages", paginatedData.get("totalPages")
            ));
            data.put("totalCount", paginatedData.get("totalCount"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Sequences retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated sequences: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getTypesPaginated(String requestId, HttpServletRequest req,
                                                 String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated types, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = otherObjectsRepository.getTypesPaginated(page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", paginatedData.get("items"));
            data.put("pagination", Map.of(
                    "page", paginatedData.get("page"),
                    "pageSize", paginatedData.get("pageSize"),
                    "totalCount", paginatedData.get("totalCount"),
                    "totalPages", paginatedData.get("totalPages")
            ));
            data.put("totalCount", paginatedData.get("totalCount"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Types retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated types: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getTriggersPaginated(String requestId, HttpServletRequest req,
                                                    String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated triggers, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = otherObjectsRepository.getTriggersPaginated(page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", paginatedData.get("items"));
            data.put("pagination", Map.of(
                    "page", paginatedData.get("page"),
                    "pageSize", paginatedData.get("pageSize"),
                    "totalCount", paginatedData.get("totalCount"),
                    "totalPages", paginatedData.get("totalPages")
            ));
            data.put("totalCount", paginatedData.get("totalCount"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Triggers retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated triggers: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 30. DIAGNOSTIC METHODS
    // ============================================================

    public Map<String, Object> diagnoseDatabase(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Running PostgreSQL database diagnostics for user: {}", requestId, performedBy);

        try {
            Map<String, Object> diagnostics = repository.diagnoseDatabase();

            diagnostics.put("generatedAt", java.time.LocalDateTime.now().toString());
            diagnostics.put("generatedBy", performedBy);

            Map<String, Object> result = new HashMap<>();
            result.put("data", diagnostics);
            result.put("responseCode", 200);
            result.put("message", "Database diagnostics completed successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Database diagnostics completed with status: {}",
                    requestId, diagnostics.get("diagnosticStatus"));

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error diagnosing PostgreSQL database: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getCurrentUserInfo(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting current user info for user: {}", requestId, performedBy);

        try {
            String currentUser = repository.getCurrentUser();
            String currentSchema = repository.getCurrentSchema();
            String dbVersion = repository.getDatabaseVersion();

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("currentUser", currentUser);
            userInfo.put("currentSchema", currentSchema);
            userInfo.put("databaseVersion", dbVersion);

            Map<String, Object> result = new HashMap<>();
            result.put("data", userInfo);
            result.put("responseCode", 200);
            result.put("message", "User info retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved user info: user={}, schema={}",
                    requestId, currentUser, currentSchema);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting user info: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getAllObjectCounts(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all object counts", requestId);

        try {
            Map<String, Object> counts = repository.getAllObjectCounts();

            Map<String, Object> result = new HashMap<>();
            result.put("data", counts);
            result.put("responseCode", 200);
            result.put("message", "Object counts retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved counts: {}", requestId, counts);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting object counts: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> resolveSynonymTarget(String requestId, HttpServletRequest req,
                                                    String performedBy, String synonymName) {
        log.info("RequestEntity ID: {}, Synonyms not supported in PostgreSQL", requestId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", new HashMap<>());
        result.put("responseCode", 200);
        result.put("message", "Synonyms are not supported in PostgreSQL");
        result.put("requestId", requestId);
        result.put("timestamp", java.time.Instant.now().toString());

        return result;
    }

    // ============================================================
    // 31. EXISTING METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllTables(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all PostgreSQL tables for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> tables = tableRepository.getAllTables();

            String currentSchema = repository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("totalCount", tables.size());
            result.put("database", "PostgreSQL");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} PostgreSQL tables from schema: {}",
                    requestId, tables.size(), currentSchema);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL tables: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL tables: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getTablesBySchema(String requestId, HttpServletRequest req,
                                                 String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting tables from PostgreSQL schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> tables = tableRepository.getTablesBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("totalCount", tables.size());
            result.put("database", "PostgreSQL");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} tables from PostgreSQL schema: {}",
                    requestId, tables.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL tables from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL tables from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getTableDetails(String requestId, HttpServletRequest req,
                                               String performedBy, String tableName) {
        log.info("RequestEntity ID: {}, Getting details for PostgreSQL table: {} for user: {}",
                requestId, tableName, performedBy);

        try {
            Map<String, Object> tableDetails = tableRepository.getTableDetails(tableName);
            List<Map<String, Object>> columns = tableRepository.getTableColumns(tableName);
            List<Map<String, Object>> constraints = tableRepository.getTableConstraints(tableName);
            List<Map<String, Object>> indexes = tableRepository.getTableIndexes(tableName);

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

            log.info("RequestEntity ID: {}, Retrieved details for PostgreSQL table: {}, found {} columns",
                    requestId, tableName, columns.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for PostgreSQL table {}: {}",
                    requestId, tableName, e.getMessage());
            throw new RuntimeException("Failed to retrieve details for PostgreSQL table " + tableName + ": " + e.getMessage(), e);
        }
    }

    public Map<String, Object> searchTables(String requestId, HttpServletRequest req,
                                            String performedBy, String searchPattern) {
        log.info("RequestEntity ID: {}, Searching PostgreSQL tables with pattern: {} for user: {}",
                requestId, searchPattern, performedBy);

        try {
            List<Map<String, Object>> tables = tableRepository.searchTables(searchPattern);

            String currentSchema = repository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("currentSchema", currentSchema);
            result.put("totalCount", tables.size());
            result.put("searchPattern", searchPattern);
            result.put("database", "PostgreSQL");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Found {} PostgreSQL tables matching pattern: {}",
                    requestId, tables.size(), searchPattern);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error searching PostgreSQL tables with pattern {}: {}",
                    requestId, searchPattern, e.getMessage());
            throw new RuntimeException("Failed to search PostgreSQL tables: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getTableStatistics(String requestId, HttpServletRequest req,
                                                  String performedBy, String tableName) {
        log.info("RequestEntity ID: {}, Getting statistics for PostgreSQL table: {} for user: {}",
                requestId, tableName, performedBy);

        try {
            Map<String, Object> statistics = tableRepository.getTableStatistics(tableName);

            Map<String, Object> result = new HashMap<>();
            result.put("tableName", tableName);
            result.put("statistics", statistics);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved statistics for PostgreSQL table: {}", requestId, tableName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting statistics for PostgreSQL table {}: {}",
                    requestId, tableName, e.getMessage());
            throw new RuntimeException("Failed to retrieve statistics for PostgreSQL table " + tableName + ": " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getTablesWithRowCount(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting PostgreSQL tables with row count for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> tables = tableRepository.getTablesWithRowCount();

            String currentSchema = repository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("totalCount", tables.size());
            result.put("database", "PostgreSQL");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved row counts for {} PostgreSQL tables", requestId, tables.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL tables with row count: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL tables with row count: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getTableCountByTablespace(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting PostgreSQL table count by tablespace for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> tablespaceStats = tableRepository.getTableCountByTablespace();

            String currentSchema = repository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("tablespaceStats", tablespaceStats);
            result.put("totalTablespaces", tablespaceStats.size());
            result.put("database", "PostgreSQL");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved tablespace statistics for {} tablespaces",
                    requestId, tablespaceStats.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL table count by tablespace: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL table count by tablespace: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getRecentTables(String requestId, HttpServletRequest req,
                                               String performedBy, int days) {
        log.info("RequestEntity ID: {}, Getting PostgreSQL tables modified in last {} days for user: {}",
                requestId, days, performedBy);

        try {
            List<Map<String, Object>> tables = tableRepository.getRecentTables(days);

            String currentSchema = repository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("totalCount", tables.size());
            result.put("days", days);
            result.put("database", "PostgreSQL");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Found {} PostgreSQL tables modified in last {} days",
                    requestId, tables.size(), days);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting recent PostgreSQL tables: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve recent PostgreSQL tables: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 32. EXISTING VIEW METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllViews(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all PostgreSQL views for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> views = viewRepository.getAllViews();

            String currentSchema = repository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("views", views);
            result.put("totalCount", views.size());
            result.put("database", "PostgreSQL");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} PostgreSQL views", requestId, views.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL views: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL views: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getViewsBySchema(String requestId, HttpServletRequest req,
                                                String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting views from PostgreSQL schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> views = viewRepository.getViewsBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("views", views);
            result.put("totalCount", views.size());
            result.put("database", "PostgreSQL");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} views from PostgreSQL schema: {}",
                    requestId, views.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL views from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL views from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getViewDetails(String requestId, HttpServletRequest req,
                                              String performedBy, String viewName) {
        log.info("RequestEntity ID: {}, Getting details for PostgreSQL view: {} for user: {}",
                requestId, viewName, performedBy);

        try {
            Map<String, Object> viewDetails = viewRepository.getViewDetails(viewName);

            viewDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            viewDetails.put("generatedBy", performedBy);

            Map<String, Object> result = new HashMap<>();
            result.put("data", viewDetails);
            result.put("responseCode", 200);
            result.put("message", "View details retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for PostgreSQL view {}: {}",
                    requestId, viewName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 33. EXISTING PROCEDURE METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllProcedures(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all PostgreSQL procedures for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> procedures = procedureRepository.getAllProcedures();

            String currentSchema = repository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("procedures", procedures);
            result.put("totalCount", procedures.size());
            result.put("database", "PostgreSQL");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} PostgreSQL procedures", requestId, procedures.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL procedures: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL procedures: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getProceduresBySchema(String requestId, HttpServletRequest req,
                                                     String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting procedures from PostgreSQL schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> procedures = procedureRepository.getProceduresBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("procedures", procedures);
            result.put("totalCount", procedures.size());
            result.put("database", "PostgreSQL");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} procedures from PostgreSQL schema: {}",
                    requestId, procedures.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL procedures from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL procedures from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getProcedureDetails(String requestId, HttpServletRequest req,
                                                   String performedBy, String procedureName) {
        log.info("RequestEntity ID: {}, Getting details for PostgreSQL procedure: {} for user: {}",
                requestId, procedureName, performedBy);

        try {
            Map<String, Object> procedureDetails = procedureRepository.getProcedureDetails(procedureName);

            procedureDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            procedureDetails.put("generatedBy", performedBy);

            Map<String, Object> result = new HashMap<>();
            result.put("data", procedureDetails);
            result.put("responseCode", 200);
            result.put("message", "Procedure details retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for PostgreSQL procedure {}: {}",
                    requestId, procedureName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 34. EXISTING FUNCTION METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllFunctions(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all PostgreSQL functions for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> functions = functionRepository.getAllFunctions();

            String currentSchema = repository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("functions", functions);
            result.put("totalCount", functions.size());
            result.put("database", "PostgreSQL");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} PostgreSQL functions", requestId, functions.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL functions: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL functions: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getFunctionsBySchema(String requestId, HttpServletRequest req,
                                                    String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting functions from PostgreSQL schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> functions = functionRepository.getFunctionsBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("functions", functions);
            result.put("totalCount", functions.size());
            result.put("database", "PostgreSQL");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} functions from PostgreSQL schema: {}",
                    requestId, functions.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL functions from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL functions from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getFunctionDetails(String requestId, HttpServletRequest req,
                                                  String performedBy, String functionName) {
        log.info("RequestEntity ID: {}, Getting details for PostgreSQL function: {} for user: {}",
                requestId, functionName, performedBy);

        try {
            Map<String, Object> functionDetails = functionRepository.getFunctionDetails(functionName);

            functionDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            functionDetails.put("generatedBy", performedBy);

            Map<String, Object> result = new HashMap<>();
            result.put("data", functionDetails);
            result.put("responseCode", 200);
            result.put("message", "Function details retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for PostgreSQL function {}: {}",
                    requestId, functionName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 35. EXISTING SEQUENCE METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllSequences(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all PostgreSQL sequences for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> sequences = otherObjectsRepository.getAllSequences();

            String currentSchema = repository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("sequences", sequences);
            result.put("totalCount", sequences.size());
            result.put("database", "PostgreSQL");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} PostgreSQL sequences", requestId, sequences.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL sequences: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL sequences: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getSequencesBySchema(String requestId, HttpServletRequest req,
                                                    String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting sequences from PostgreSQL schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> sequences = otherObjectsRepository.getSequencesBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("sequences", sequences);
            result.put("totalCount", sequences.size());
            result.put("database", "PostgreSQL");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} sequences from PostgreSQL schema: {}",
                    requestId, sequences.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL sequences from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL sequences from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getSequenceDetails(String requestId, HttpServletRequest req,
                                                  String performedBy, String sequenceName) {
        log.info("RequestEntity ID: {}, Getting details for PostgreSQL sequence: {} for user: {}",
                requestId, sequenceName, performedBy);

        try {
            Map<String, Object> sequenceDetails = otherObjectsRepository.getSequenceDetails(sequenceName);

            sequenceDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            sequenceDetails.put("generatedBy", performedBy);

            Map<String, Object> result = new HashMap<>();
            result.put("data", sequenceDetails);
            result.put("responseCode", 200);
            result.put("message", "Sequence details retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for PostgreSQL sequence {}: {}",
                    requestId, sequenceName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 36. EXISTING TRIGGER METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllTriggers(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all PostgreSQL triggers for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> triggers = otherObjectsRepository.getAllTriggers();

            String currentSchema = repository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("triggers", triggers);
            result.put("totalCount", triggers.size());
            result.put("database", "PostgreSQL");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} PostgreSQL triggers", requestId, triggers.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL triggers: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL triggers: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getTriggersBySchema(String requestId, HttpServletRequest req,
                                                   String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting triggers from PostgreSQL schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> triggers = otherObjectsRepository.getTriggersBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("triggers", triggers);
            result.put("totalCount", triggers.size());
            result.put("database", "PostgreSQL");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} triggers from PostgreSQL schema: {}",
                    requestId, triggers.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL triggers from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL triggers from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getTriggerDetails(String requestId, HttpServletRequest req,
                                                 String performedBy, String triggerName) {
        log.info("RequestEntity ID: {}, Getting details for PostgreSQL trigger: {} for user: {}",
                requestId, triggerName, performedBy);

        try {
            Map<String, Object> triggerDetails = otherObjectsRepository.getTriggerDetails(triggerName);

            triggerDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            triggerDetails.put("generatedBy", performedBy);

            Map<String, Object> result = new HashMap<>();
            result.put("data", triggerDetails);
            result.put("responseCode", 200);
            result.put("message", "Trigger details retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for PostgreSQL trigger {}: {}",
                    requestId, triggerName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 37. EXISTING TYPE METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllTypes(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all PostgreSQL types for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> types = otherObjectsRepository.getAllTypes();

            String currentSchema = repository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("types", types);
            result.put("totalCount", types.size());
            result.put("database", "PostgreSQL");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} PostgreSQL types", requestId, types.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL types: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL types: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getTypesBySchema(String requestId, HttpServletRequest req,
                                                String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting types from PostgreSQL schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> types = otherObjectsRepository.getTypesBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("types", types);
            result.put("totalCount", types.size());
            result.put("database", "PostgreSQL");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} types from PostgreSQL schema: {}",
                    requestId, types.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL types from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL types from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getTypeDetails(String requestId, HttpServletRequest req,
                                              String performedBy, String typeName) {
        log.info("RequestEntity ID: {}, Getting details for PostgreSQL type: {} for user: {}",
                requestId, typeName, performedBy);

        try {
            Map<String, Object> typeDetails = otherObjectsRepository.getTypeDetails(typeName);

            typeDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            typeDetails.put("generatedBy", performedBy);

            Map<String, Object> result = new HashMap<>();
            result.put("data", typeDetails);
            result.put("responseCode", 200);
            result.put("message", "Type details retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for PostgreSQL type {}: {}",
                    requestId, typeName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 38. EXISTING GENERAL OBJECT METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllObjects(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all PostgreSQL objects for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> objects = objectRepository.getAllObjects();

            String currentSchema = repository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("objects", objects);
            result.put("totalCount", objects.size());
            result.put("database", "PostgreSQL");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} PostgreSQL objects", requestId, objects.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL objects: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL objects: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getObjectsBySchema(String requestId, HttpServletRequest req,
                                                  String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting objects from PostgreSQL schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> objects = objectRepository.getObjectsBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("objects", objects);
            result.put("totalCount", objects.size());
            result.put("database", "PostgreSQL");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} objects from PostgreSQL schema: {}",
                    requestId, objects.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL objects from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL objects from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    public Map<String, Object> searchObjects(String requestId, HttpServletRequest req,
                                             String performedBy, String searchPattern) {
        log.info("RequestEntity ID: {}, Searching PostgreSQL objects with pattern: {} for user: {}",
                requestId, searchPattern, performedBy);

        try {
            List<Map<String, Object>> objects = searchRepository.searchObjectsForFrontend(searchPattern, "ALL", 1000);

            String currentSchema = repository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("objects", objects);
            result.put("totalCount", objects.size());
            result.put("searchPattern", searchPattern);
            result.put("database", "PostgreSQL");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Found {} PostgreSQL objects matching pattern: {}",
                    requestId, objects.size(), searchPattern);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error searching PostgreSQL objects with pattern {}: {}",
                    requestId, searchPattern, e.getMessage());
            throw new RuntimeException("Failed to search PostgreSQL objects: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getObjectCountByType(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting PostgreSQL object count by type for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> objectCounts = objectRepository.getObjectCountByType();

            String currentSchema = repository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("objectCounts", objectCounts);
            result.put("totalCount", objectCounts.size());
            result.put("database", "PostgreSQL");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved object counts for {} object types", requestId, objectCounts.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL object count by type: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL object count by type: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getInvalidObjects(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting invalid PostgreSQL objects for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> invalidObjects = repository.getInvalidObjects();

            String currentSchema = repository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("invalidObjects", invalidObjects);
            result.put("totalCount", invalidObjects.size());
            result.put("database", "PostgreSQL");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} invalid PostgreSQL objects", requestId, invalidObjects.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting invalid PostgreSQL objects: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve invalid PostgreSQL objects: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getObjectsByStatus(String requestId, HttpServletRequest req,
                                                  String performedBy, String status) {
        log.info("RequestEntity ID: {}, Getting PostgreSQL objects with status: {} for user: {}",
                requestId, status, performedBy);

        try {
            List<Map<String, Object>> objects = objectRepository.getObjectsByStatus(status);

            String currentSchema = repository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("objects", objects);
            result.put("totalCount", objects.size());
            result.put("status", status);
            result.put("database", "PostgreSQL");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} PostgreSQL objects with status: {}",
                    requestId, objects.size(), status);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL objects with status {}: {}",
                    requestId, status, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL objects by status: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 39. NEW: COMBINED SEARCH SERVICE METHOD
    // ============================================================

    public Map<String, Object> searchCombinedTypes(String requestId, HttpServletRequest req,
                                                   String performedBy, String query,
                                                   String types, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Combined search, query: {}, types: {}, page: {}, pageSize: {}",
                requestId, query, types, page, pageSize);

        try {
            String[] typeArray = types.split(",");

            Map<String, Object> searchResults = searchRepository.searchCombinedTypes(
                    query, typeArray, page, pageSize);

            List<Map<String, Object>> items = (List<Map<String, Object>>) searchResults.get("items");
            List<Map<String, Object>> transformedItems = new ArrayList<>();

            for (Map<String, Object> item : items) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("name", item.get("object_name"));
                transformed.put("owner", item.get("owner"));
                transformed.put("type", item.get("object_type"));
                transformed.put("status", item.get("status"));

                // PostgreSQL doesn't have synonyms
                transformed.put("isSynonym", false);
                transformedItems.add(transformed);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("items", transformedItems);
            data.put("pagination", Map.of(
                    "page", searchResults.get("page"),
                    "pageSize", searchResults.get("pageSize"),
                    "totalCount", searchResults.get("totalCount"),
                    "totalPages", searchResults.get("totalPages")
            ));
            data.put("totalCount", searchResults.get("totalCount"));
            data.put("query", query);
            data.put("types", typeArray);

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Search completed successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error in combined search: {}", requestId, e.getMessage());

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("items", new ArrayList<>());
            errorData.put("totalCount", 0);
            errorData.put("query", query);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("data", errorData);
            errorResult.put("responseCode", 500);
            errorResult.put("message", e.getMessage());
            errorResult.put("requestId", requestId);
            errorResult.put("timestamp", java.time.Instant.now().toString());

            return errorResult;
        }
    }

    // ============================================================
    // 40. NEW: SEARCH PROCEDURES WITH SYNONYMS (Not supported)
    // ============================================================

    public Map<String, Object> searchProceduresWithSynonyms(String requestId, HttpServletRequest req,
                                                            String performedBy, String query,
                                                            int page, int pageSize) {
        log.info("RequestEntity ID: {}, Synonyms not supported in PostgreSQL", requestId);

        // Just return procedures without synonyms
        return searchObjectsForFrontendPaginated(requestId, req, performedBy, query, "PROCEDURE", page, pageSize);
    }

    // ============================================================
    // 41. NEW: GET SEARCH COUNT
    // ============================================================

    public int getSearchCount(String query, String types) {
        try {
            String[] typeArray = types.split(",");
            return searchRepository.getSearchCount(query, typeArray);
        } catch (Exception e) {
            log.error("Error getting search count: {}", e.getMessage());
            return 0;
        }
    }





    // ============================================================
// PACKAGE METHODS (Schemas as package equivalents)
// ============================================================

    public Map<String, Object> getAllPackages(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all PostgreSQL schemas for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> schemas = repository.getAllSchemas();

            Map<String, Object> result = new HashMap<>();
            result.put("schemas", schemas);
            result.put("totalCount", schemas.size());
            result.put("database", "PostgreSQL");
            result.put("schema", repository.getCurrentSchema());
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} PostgreSQL schemas", requestId, schemas.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL schemas: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL schemas: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getPackagesBySchema(String requestId, HttpServletRequest req,
                                                   String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting PostgreSQL schemas with name: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            Map<String, Object> schema = repository.getSchemaDetails(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("schema", schema);
            result.put("database", "PostgreSQL");
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved PostgreSQL schema: {}", requestId, schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL schema: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getPackageDetails(String requestId, HttpServletRequest req,
                                                 String performedBy, String packageName) {
        log.info("RequestEntity ID: {}, Getting details for PostgreSQL schema: {} for user: {}",
                requestId, packageName, performedBy);

        try {
            Map<String, Object> schemaDetails = repository.getSchemaDetails(packageName);

            schemaDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            schemaDetails.put("generatedBy", performedBy);

            Map<String, Object> result = new HashMap<>();
            result.put("data", schemaDetails);
            result.put("responseCode", 200);
            result.put("message", "Schema details retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for PostgreSQL schema {}: {}",
                    requestId, packageName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

// ============================================================
// SYNONYM METHODS (Not supported - return empty results)
// ============================================================

    public Map<String, Object> getAllSynonyms(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Synonyms not supported in PostgreSQL", requestId);

        Map<String, Object> result = new HashMap<>();
        result.put("synonyms", new ArrayList<>());
        result.put("totalCount", 0);
        result.put("database", "PostgreSQL");
        result.put("schema", repository.getCurrentSchema());
        result.put("generatedAt", java.time.LocalDateTime.now().toString());
        result.put("generatedBy", performedBy);
        result.put("message", "Synonyms are not supported in PostgreSQL");

        return result;
    }

    public Map<String, Object> getSynonymsBySchema(String requestId, HttpServletRequest req,
                                                   String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Synonyms not supported in PostgreSQL", requestId);

        Map<String, Object> result = new HashMap<>();
        result.put("synonyms", new ArrayList<>());
        result.put("totalCount", 0);
        result.put("database", "PostgreSQL");
        result.put("schema", schemaName);
        result.put("generatedAt", java.time.LocalDateTime.now().toString());
        result.put("generatedBy", performedBy);
        result.put("message", "Synonyms are not supported in PostgreSQL");

        return result;
    }

    public Map<String, Object> getSynonymDetails(String requestId, HttpServletRequest req,
                                                 String performedBy, String synonymName) {
        log.info("RequestEntity ID: {}, Synonyms not supported in PostgreSQL", requestId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", new HashMap<>());
        result.put("responseCode", 200);
        result.put("message", "Synonyms are not supported in PostgreSQL");
        result.put("requestId", requestId);
        result.put("timestamp", java.time.Instant.now().toString());

        return result;
    }

// ============================================================
// DATABASE LINK METHODS (PostgreSQL uses Foreign Data Wrappers)
// ============================================================

    public Map<String, Object> getAllDbLinks(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting PostgreSQL foreign data wrappers for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> dbLinks = otherObjectsRepository.getAllDbLinks();

            Map<String, Object> result = new HashMap<>();
            result.put("dbLinks", dbLinks);
            result.put("totalCount", dbLinks.size());
            result.put("database", "PostgreSQL");
            result.put("schema", repository.getCurrentSchema());
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} PostgreSQL foreign data wrappers", requestId, dbLinks.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL foreign data wrappers: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL foreign data wrappers: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getDbLinksBySchema(String requestId, HttpServletRequest req,
                                                  String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting PostgreSQL foreign data wrappers for schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> dbLinks = otherObjectsRepository.getDbLinksBySchema(schemaName);

            Map<String, Object> result = new HashMap<>();
            result.put("dbLinks", dbLinks);
            result.put("totalCount", dbLinks.size());
            result.put("database", "PostgreSQL");
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} PostgreSQL foreign data wrappers for schema: {}",
                    requestId, dbLinks.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting PostgreSQL foreign data wrappers for schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve PostgreSQL foreign data wrappers: " + e.getMessage(), e);
        }
    }

// ============================================================
// SEARCH METHODS (Paginated)
// ============================================================

    public Map<String, Object> searchObjectsPaginated(String requestId, HttpServletRequest req,
                                                      String performedBy, String searchQuery,
                                                      String searchType, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Paginated search with query: {}, type: {}, page: {}, pageSize: {}",
                requestId, searchQuery, searchType, page, pageSize);

        try {
            Map<String, Object> searchResults = searchRepository.searchObjectsPaginated(
                    searchQuery, searchType, page, pageSize);

            Map<String, Object> result = new HashMap<>();
            result.put("data", searchResults);
            result.put("responseCode", 200);
            result.put("message", "Search completed successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Found {} objects matching query: {}",
                    requestId, searchResults.get("totalCount"), searchQuery);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error searching objects: {}", requestId, e.getMessage());

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("results", new ArrayList<>());
            errorData.put("totalCount", 0);
            errorData.put("page", page);
            errorData.put("pageSize", pageSize);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("data", errorData);
            errorResult.put("responseCode", 500);
            errorResult.put("message", e.getMessage());
            errorResult.put("requestId", requestId);
            errorResult.put("timestamp", java.time.Instant.now().toString());

            return errorResult;
        }
    }


    // ============================================================
// 43. MATERIALIZED VIEWS FOR FRONTEND (PAGINATED)
// ============================================================

    public Map<String, Object> getAllMaterializedViewsForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                           String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated materialized views for frontend, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = viewRepository.getAllMaterializedViewsForFrontend(page, pageSize);

            Map<String, Object> data = new HashMap<>();
            data.put("items", paginatedData.get("items"));
            data.put("pagination", Map.of(
                    "page", paginatedData.get("page"),
                    "pageSize", paginatedData.get("pageSize"),
                    "totalCount", paginatedData.get("totalCount"),
                    "totalPages", paginatedData.get("totalPages")
            ));
            data.put("totalCount", paginatedData.get("totalCount"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Materialized views retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} materialized views", requestId,
                    ((List<?>) paginatedData.get("items")).size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated materialized views: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }




    // ============================================================
// 44. MATERIALIZED VIEWS FOR FRONTEND (NON-PAGINATED)
// ============================================================

    public Map<String, Object> getAllMaterializedViewsForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all materialized views for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> materializedViews = viewRepository.getAllMaterializedViewsForFrontend();

            Map<String, Object> result = new HashMap<>();
            result.put("data", materializedViews);
            result.put("totalCount", materializedViews.size());
            result.put("responseCode", 200);
            result.put("message", "Materialized views retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} materialized views for frontend", requestId, materializedViews.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting materialized views for frontend: {}", requestId, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }




    // ============================================================
    // 42. HELPER METHODS
    // ============================================================

    private Map<String, Object> createErrorResponse(String requestId, String errorMessage) {
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("data", null);
        errorResult.put("responseCode", 500);
        errorResult.put("message", errorMessage);
        errorResult.put("requestId", requestId);
        errorResult.put("timestamp", java.time.Instant.now().toString());
        return errorResult;
    }

    private int calculateTotalPages(long totalItems, int pageSize) {
        if (totalItems <= 0) return 0;
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    private long getLongValue(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String resolveSchema(String schema) {
        if (schema != null && !schema.isEmpty()) {
            return schema;
        }
        return repository.getCurrentSchema();
    }

    public String getCurrentUser() {
        try {
            return repository.getCurrentUser();
        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage());
            return "UNKNOWN";
        }
    }

    public String getCurrentSchema() {
        try {
            return repository.getCurrentSchema();
        } catch (Exception e) {
            log.error("Error getting current schema: {}", e.getMessage());
            return "public";
        }
    }

    public Map<String, Object> getTableConstraints(String requestId, String tableName,
                                                   String owner, String performedBy) {
        log.info("Getting constraints for table: {}", tableName);

        try {
            String resolvedOwner = resolveSchema(owner);

            List<Map<String, Object>> constraints = tableRepository.getTableConstraints(
                    resolvedOwner, tableName);

            Map<String, Object> data = new HashMap<>();
            data.put("constraints", constraints);
            data.put("count", constraints.size());

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Constraints retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("Error getting constraints: {}", e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getObjectCounts(String requestId, String objectName,
                                               String objectType, String owner, String performedBy) {
        log.info("Getting counts for {}: {}", objectType, objectName);

        try {
            String resolvedOwner = resolveSchema(owner);
            Map<String, Object> counts = new HashMap<>();

            String upperType = objectType.toUpperCase();

            // Column count for tables/views
            if (upperType.equals("TABLE") || upperType.equals("VIEW") || upperType.equals("MATERIALIZED VIEW")) {
                String colSql = "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE table_schema = ? AND table_name = ?";
                int columnCount = postgresqlJdbcTemplate.queryForObject(
                        colSql, Integer.class, resolvedOwner, objectName);
                counts.put("columnCount", columnCount);
            }

            // Parameter count for functions/procedures
            if (upperType.equals("PROCEDURE") || upperType.equals("FUNCTION")) {
                String paramSql = "SELECT COUNT(*) FROM pg_proc_info WHERE proname = ?";
                try {
                    int parameterCount = postgresqlJdbcTemplate.queryForObject(
                            paramSql, Integer.class, objectName);
                    counts.put("parameterCount", parameterCount);
                } catch (Exception e) {
                    counts.put("parameterCount", 0);
                }
            }

            // Dependency count
            String depSql = "SELECT COUNT(*) FROM pg_depend dep " +
                    "JOIN pg_class c ON dep.objid = c.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ?";
            int dependencyCount = postgresqlJdbcTemplate.queryForObject(
                    depSql, Integer.class, resolvedOwner, objectName);
            counts.put("dependencyCount", dependencyCount);

            // PostgreSQL doesn't have synonyms
            counts.put("isSynonym", false);

            Map<String, Object> result = new HashMap<>();
            result.put("data", counts);
            result.put("responseCode", 200);
            result.put("message", "Counts retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("Error getting counts: {}", e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getObjectDDL(String requestId, String objectName,
                                            String objectType, String owner, String performedBy) {
        log.info("Getting DDL for {}: {}", objectType, objectName);

        try {
            String resolvedOwner = resolveSchema(owner);

            Map<String, Object> ddlResult = ddlRepository.getObjectDDLForFrontend(objectName, objectType);

            Map<String, Object> data = new HashMap<>();
            data.put("ddl", ddlResult.get("ddl"));
            data.put("status", ddlResult.get("status"));
            data.put("method", ddlResult.get("method"));

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "DDL retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("Error getting DDL: {}", e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public ResponseEntity<Map<String, Object>> createErrorResponse(String requestId, String message, int statusCode) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("responseCode", statusCode);
        errorResponse.put("message", message);
        errorResponse.put("requestId", requestId);
        errorResponse.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(statusCode).body(errorResponse);
    }
}