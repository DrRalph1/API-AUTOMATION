package com.usg.apiAutomation.services;

import com.usg.apiAutomation.repositories.oracle.OracleSchemaRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OracleSchemaService {

    private final OracleSchemaRepository oracleSchemaRepository;
    private final LoggerUtil loggerUtil;

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    // ============================================================
    // 1. CURRENT SCHEMA INFO (ORIGINAL)
    // ============================================================

    public Map<String, Object> getCurrentSchemaInfo(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting current schema info for user: {}", requestId, performedBy);

        try {
            Map<String, Object> schemaInfo = oracleSchemaRepository.getCurrentSchemaInfo();

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
    // 2. ALL TABLES FOR FRONTEND (ORIGINAL)
    // ============================================================

    public Map<String, Object> getAllTablesForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle tables for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> tables = oracleSchemaRepository.getAllTablesForFrontend();

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
    // 2a. ALL TABLES FOR FRONTEND (PAGINATED) - UPDATED
    // ============================================================

    public Map<String, Object> getAllTablesForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated tables for frontend, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = oracleSchemaRepository.getAllTablesForFrontend(page, pageSize);

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
    // 3. TABLE DETAILS FOR FRONTEND (ORIGINAL)
    // ============================================================

    public Map<String, Object> getTableDetailsForFrontend(String requestId, HttpServletRequest req,
                                                          String performedBy, String tableName) {
        log.info("RequestEntity ID: {}, Getting table details for frontend, table: {}, user: {}",
                requestId, tableName, performedBy);

        try {
            Map<String, Object> tableDetails = oracleSchemaRepository.getTableDetailsForFrontend(tableName);

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
    // 3a. TABLE DETAILS FOR FRONTEND (PAGINATED COLUMNS) - UPDATED
    // ============================================================

    public Map<String, Object> getTableDetailsForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                   String performedBy, String tableName,
                                                                   int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated table details for frontend, table: {}, page: {}, pageSize: {}",
                requestId, tableName, page, pageSize);

        try {
            Map<String, Object> tableDetails = oracleSchemaRepository.getTableDetailsForFrontend(tableName, page, pageSize);

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
    // 4. TABLE DATA (ORIGINAL)
    // ============================================================

    public Map<String, Object> getTableData(String requestId, HttpServletRequest req,
                                            String performedBy, String tableName,
                                            int page, int pageSize, String sortColumn, String sortDirection) {
        log.info("RequestEntity ID: {}, Getting table data for frontend, table: {}, page: {}, pageSize: {}, user: {}",
                requestId, tableName, page, pageSize, performedBy);

        try {
            Map<String, Object> tableData = oracleSchemaRepository.getTableData(
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
    // 5. ALL VIEWS FOR FRONTEND (ORIGINAL)
    // ============================================================

    public Map<String, Object> getAllViewsForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle views for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> views = oracleSchemaRepository.getAllViewsForFrontend();

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
    // 5a. ALL VIEWS FOR FRONTEND (PAGINATED) - UPDATED
    // ============================================================

    public Map<String, Object> getAllViewsForFrontendPaginated(String requestId, HttpServletRequest req,
                                                               String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated views for frontend, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = oracleSchemaRepository.getAllViewsForFrontend(page, pageSize);

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
    // 6. ALL PROCEDURES FOR FRONTEND (ORIGINAL)
    // ============================================================

    public Map<String, Object> getAllProceduresForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle procedures for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> procedures = oracleSchemaRepository.getAllProceduresForFrontend();

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
    // 6a. ALL PROCEDURES FOR FRONTEND (PAGINATED) - UPDATED
    // ============================================================

    public Map<String, Object> getAllProceduresForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                    String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated procedures for frontend, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = oracleSchemaRepository.getAllProceduresForFrontend(page, pageSize);

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
    // 7. ALL FUNCTIONS FOR FRONTEND (ORIGINAL)
    // ============================================================

    public Map<String, Object> getAllFunctionsForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle functions for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> functions = oracleSchemaRepository.getAllFunctionsForFrontend();

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
    // 7a. ALL FUNCTIONS FOR FRONTEND (PAGINATED) - UPDATED
    // ============================================================

    public Map<String, Object> getAllFunctionsForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                   String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated functions for frontend, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = oracleSchemaRepository.getAllFunctionsForFrontend(page, pageSize);

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
    // 8. ALL PACKAGES FOR FRONTEND (ORIGINAL)
    // ============================================================

    public Map<String, Object> getAllPackagesForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle packages for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> packages = oracleSchemaRepository.getAllPackagesForFrontend();

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

    // ============================================================
    // 8a. ALL PACKAGES FOR FRONTEND (PAGINATED) - UPDATED
    // ============================================================

    public Map<String, Object> getAllPackagesForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                  String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated packages for frontend, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = oracleSchemaRepository.getAllPackagesForFrontend(page, pageSize);

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
            result.put("message", "Packages retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated packages: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 9. ALL TRIGGERS FOR FRONTEND (ORIGINAL)
    // ============================================================

    public Map<String, Object> getAllTriggersForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle triggers for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> triggers = oracleSchemaRepository.getAllTriggersForFrontend();

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
    // 9a. ALL TRIGGERS FOR FRONTEND (PAGINATED) - UPDATED
    // ============================================================

    public Map<String, Object> getAllTriggersForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                  String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated triggers for frontend, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = oracleSchemaRepository.getAllTriggersForFrontend(page, pageSize);

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
    // 10. ALL SYNONYMS FOR FRONTEND (ORIGINAL)
    // ============================================================

    public Map<String, Object> getAllSynonymsForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle synonyms for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> synonyms = oracleSchemaRepository.getAllSynonymsForFrontend();

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

    // ============================================================
    // 10a. ALL SYNONYMS FOR FRONTEND (PAGINATED WITH LAZY LOADING) - UPDATED
    // ============================================================

    public Map<String, Object> getAllSynonymsForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                  String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated synonyms for frontend, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = oracleSchemaRepository.getAllSynonymsForFrontend(page, pageSize);

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
            result.put("message", "Synonyms retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated synonyms: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 11. ALL SEQUENCES FOR FRONTEND (ORIGINAL)
    // ============================================================

    public Map<String, Object> getAllSequencesForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle sequences for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> sequences = oracleSchemaRepository.getAllSequencesForFrontend();

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
    // 11a. ALL SEQUENCES FOR FRONTEND (PAGINATED) - UPDATED
    // ============================================================

    public Map<String, Object> getAllSequencesForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                   String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated sequences for frontend, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = oracleSchemaRepository.getAllSequencesForFrontend(page, pageSize);

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
    // 12. ALL TYPES FOR FRONTEND (ORIGINAL)
    // ============================================================

    public Map<String, Object> getAllTypesForFrontend(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle types for frontend, user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> types = oracleSchemaRepository.getAllTypesForFrontend();

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
    // 12a. ALL TYPES FOR FRONTEND (PAGINATED) - UPDATED
    // ============================================================

    public Map<String, Object> getAllTypesForFrontendPaginated(String requestId, HttpServletRequest req,
                                                               String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated types for frontend, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = oracleSchemaRepository.getAllTypesForFrontend(page, pageSize);

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
    // 13. GET OBJECT DETAILS (ORIGINAL)
    // ============================================================

    public Map<String, Object> getObjectDetails(String requestId, HttpServletRequest req,
                                                String performedBy, String objectName,
                                                String objectType, String owner) {
        log.info("RequestEntity ID: {}, Getting details for {}: {}, owner: {}",
                requestId, objectType, objectName, owner);

        try {
            Map<String, Object> objectDetails = oracleSchemaRepository.getObjectDetails(objectName, objectType, owner);

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
    // 13a. GET OBJECT DETAILS (PAGINATED) - UPDATED
    // ============================================================

    public Map<String, Object> getObjectDetailsPaginated(String requestId, HttpServletRequest req,
                                                         String performedBy, String objectName,
                                                         String objectType, String owner,
                                                         int page, int pageSize, boolean includeCounts) {
        log.info("RequestEntity ID: {}, Getting paginated details for {}: {}, owner: {}, page: {}, pageSize: {}, includeCounts: {}",
                requestId, objectType, objectName, owner, page, pageSize, includeCounts);

        try {
            Map<String, Object> objectDetails = oracleSchemaRepository.getObjectDetails(
                    objectName, objectType, owner, page, pageSize);

            // Add pagination metadata inside the data object
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
    // 14. GET OBJECT DDL (ORIGINAL)
    // ============================================================

    public Map<String, Object> getObjectDDL(String requestId, HttpServletRequest req,
                                            String performedBy, String objectName, String objectType) {
        log.info("RequestEntity ID: {}, Getting DDL for {}: {}, user: {}",
                requestId, objectType, objectName, performedBy);

        try {
            Map<String, Object> ddlResult = oracleSchemaRepository.getObjectDDLForFrontend(objectName, objectType);

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
    // 15. LAZY LOADING METHOD FOR SYNONYM TARGET DETAILS
    // ============================================================

    public Map<String, Object> getSynonymTargetDetails(String requestId, HttpServletRequest req,
                                                       String performedBy, String synonymName) {
        log.info("RequestEntity ID: {}, Getting lazy loaded target details for synonym: {}",
                requestId, synonymName);

        try {
            Map<String, Object> targetDetails = oracleSchemaRepository.getSynonymTargetDetails(synonymName);

            Map<String, Object> result = new HashMap<>();
            result.put("data", targetDetails);
            result.put("responseCode", 200);
            result.put("message", "Synonym target details retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting synonym target details for {}: {}",
                    requestId, synonymName, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 16. PAGINATED SEARCH FOR FRONTEND - UPDATED
    // ============================================================

    public Map<String, Object> searchObjectsForFrontendPaginated(String requestId, HttpServletRequest req,
                                                                 String performedBy, String searchQuery,
                                                                 String searchType, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Paginated search for frontend with query: {}, type: {}, page: {}, pageSize: {}",
                requestId, searchQuery, searchType, page, pageSize);

        try {
            Map<String, Object> searchResults = oracleSchemaRepository.searchObjectsForFrontend(
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
            Map<String, Object> tableData = oracleSchemaRepository.getTableDataAdvanced(
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
    // 18. PAGINATED PROCEDURE PARAMETERS SERVICE METHOD - UPDATED
    // ============================================================

    public Map<String, Object> getProcedureParametersPaginated(String requestId, HttpServletRequest req,
                                                               String performedBy, String procedureName,
                                                               String owner, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated parameters for procedure: {}, page: {}, pageSize: {}",
                requestId, procedureName, page, pageSize);

        try {
            Map<String, Object> parameters = oracleSchemaRepository.getProcedureParametersPaginated(
                    procedureName, owner, page, pageSize);

            Map<String, Object> result = new HashMap<>();
            result.put("data", parameters);
            result.put("responseCode", 200);
            result.put("message", "Procedure parameters retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            // Fix: Use "items" instead of "parameters" to match what the repository returns
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
    // 19. PAGINATED FUNCTION PARAMETERS SERVICE METHOD - UPDATED
    // ============================================================

    public Map<String, Object> getFunctionParametersPaginated(String requestId, HttpServletRequest req,
                                                              String performedBy, String functionName,
                                                              String owner, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated parameters for function: {}, page: {}, pageSize: {}",
                requestId, functionName, page, pageSize);

        try {
            Map<String, Object> parameters = oracleSchemaRepository.getFunctionParametersPaginated(
                    functionName, owner, page, pageSize);

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
// NEW: USED BY / DEPENDENT OBJECTS SERVICE METHODS
// ============================================================

    /**
     * Get all objects that depend on (use) the specified object
     */
    public Map<String, Object> getUsedBy(String requestId, HttpServletRequest req,
                                         String performedBy, String objectName,
                                         String objectType, String owner) {
        log.info("RequestEntity ID: {}, Getting used by for {}: {}, owner: {}",
                requestId, objectType, objectName, owner);

        try {
            List<Map<String, Object>> usedBy = oracleSchemaRepository.getUsedBy(objectName, objectType, owner);

            Map<String, Object> data = new HashMap<>();
            data.put("items", usedBy);
            data.put("totalCount", usedBy.size());
            data.put("objectName", objectName);
            data.put("objectType", objectType);
            data.put("owner", owner != null ? owner : oracleSchemaRepository.getCurrentUser());

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

    /**
     * Get paginated list of objects that depend on (use) the specified object
     */
    public Map<String, Object> getUsedByPaginated(String requestId, HttpServletRequest req,
                                                  String performedBy, String objectName,
                                                  String objectType, String owner,
                                                  int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated used by for {}: {}, page: {}, pageSize: {}",
                requestId, objectType, objectName, page, pageSize);

        try {
            Map<String, Object> paginatedData = oracleSchemaRepository.getUsedByPaginated(
                    objectName, objectType, owner, page, pageSize);

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
            data.put("owner", owner != null ? owner : oracleSchemaRepository.getCurrentUser());

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

    /**
     * Get complete dependency hierarchy (what this object depends on and what depends on it)
     */
    public Map<String, Object> getDependencyHierarchy(String requestId, HttpServletRequest req,
                                                      String performedBy, String objectName,
                                                      String objectType, String owner) {
        log.info("RequestEntity ID: {}, Getting dependency hierarchy for {}: {}, owner: {}",
                requestId, objectType, objectName, owner);

        try {
            Map<String, Object> hierarchy = oracleSchemaRepository.getDependencyHierarchy(
                    objectName, objectType, owner);

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

    /**
     * Get count of objects that depend on (use) the specified object
     */
    public Map<String, Object> getUsedByCount(String requestId, HttpServletRequest req,
                                              String performedBy, String objectName,
                                              String objectType, String owner) {
        log.info("RequestEntity ID: {}, Getting used by count for {}: {}, owner: {}",
                requestId, objectType, objectName, owner);

        try {
            int count = oracleSchemaRepository.getUsedByCount(objectName, objectType, owner);

            Map<String, Object> data = new HashMap<>();
            data.put("count", count);
            data.put("objectName", objectName);
            data.put("objectType", objectType);
            data.put("owner", owner != null ? owner : oracleSchemaRepository.getCurrentUser());

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

    /**
     * Get used by summary grouped by object type
     */
    public Map<String, Object> getUsedBySummary(String requestId, HttpServletRequest req,
                                                String performedBy, String objectName,
                                                String objectType, String owner) {
        log.info("RequestEntity ID: {}, Getting used by summary for {}: {}, owner: {}",
                requestId, objectType, objectName, owner);

        try {
            Map<String, Object> summary = oracleSchemaRepository.getUsedBySummary(
                    objectName, objectType, owner);

            summary.put("objectName", objectName);
            summary.put("objectType", objectType);
            summary.put("owner", owner != null ? owner : oracleSchemaRepository.getCurrentUser());
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

    /**
     * Get used by for a table (convenience method)
     */
    public Map<String, Object> getTableUsedBy(String requestId, HttpServletRequest req,
                                              String performedBy, String tableName, String owner) {
        log.info("RequestEntity ID: {}, Getting used by for table: {}, owner: {}",
                requestId, tableName, owner);
        return getUsedBy(requestId, req, performedBy, tableName, "TABLE", owner);
    }

    /**
     * Get used by for a procedure (convenience method)
     */
    public Map<String, Object> getProcedureUsedBy(String requestId, HttpServletRequest req,
                                                  String performedBy, String procedureName, String owner) {
        log.info("RequestEntity ID: {}, Getting used by for procedure: {}, owner: {}",
                requestId, procedureName, owner);
        return getUsedBy(requestId, req, performedBy, procedureName, "PROCEDURE", owner);
    }

    /**
     * Get used by for a function (convenience method)
     */
    public Map<String, Object> getFunctionUsedBy(String requestId, HttpServletRequest req,
                                                 String performedBy, String functionName, String owner) {
        log.info("RequestEntity ID: {}, Getting used by for function: {}, owner: {}",
                requestId, functionName, owner);
        return getUsedBy(requestId, req, performedBy, functionName, "FUNCTION", owner);
    }

    /**
     * Get used by for a package (convenience method)
     */
    public Map<String, Object> getPackageUsedBy(String requestId, HttpServletRequest req,
                                                String performedBy, String packageName, String owner) {
        log.info("RequestEntity ID: {}, Getting used by for package: {}, owner: {}",
                requestId, packageName, owner);
        return getUsedBy(requestId, req, performedBy, packageName, "PACKAGE", owner);
    }

    /**
     * Get used by for a view (convenience method)
     */
    public Map<String, Object> getViewUsedBy(String requestId, HttpServletRequest req,
                                             String performedBy, String viewName, String owner) {
        log.info("RequestEntity ID: {}, Getting used by for view: {}, owner: {}",
                requestId, viewName, owner);
        return getUsedBy(requestId, req, performedBy, viewName, "VIEW", owner);
    }

    /**
     * Get used by for a trigger (convenience method)
     */
    public Map<String, Object> getTriggerUsedBy(String requestId, HttpServletRequest req,
                                                String performedBy, String triggerName, String owner) {
        log.info("RequestEntity ID: {}, Getting used by for trigger: {}, owner: {}",
                requestId, triggerName, owner);
        return getUsedBy(requestId, req, performedBy, triggerName, "TRIGGER", owner);
    }



    // ============================================================
    // 20. PAGINATED PACKAGE ITEMS SERVICE METHOD - UPDATED
    // ============================================================

    public Map<String, Object> getPackageItemsPaginated(String requestId, HttpServletRequest req,
                                                        String performedBy, String packageName,
                                                        String owner, String itemType,
                                                        int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated items for package: {}, type: {}, page: {}, pageSize: {}",
                requestId, packageName, itemType, page, pageSize);

        try {
            Map<String, Object> items = oracleSchemaRepository.getPackageItemsPaginated(
                    packageName, owner, itemType, page, pageSize);

            Map<String, Object> result = new HashMap<>();
            result.put("data", items);
            result.put("responseCode", 200);
            result.put("message", "Package items retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} items for package: {}",
                    requestId, ((List<?>) items.get("items")).size(), packageName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting items for package {}: {}",
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
    // 21. PAGINATED TABLE COLUMNS SERVICE METHOD - UPDATED
    // ============================================================

    public Map<String, Object> getTableColumnsPaginated(String requestId, HttpServletRequest req,
                                                        String performedBy, String tableName,
                                                        String owner, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated columns for table: {}, page: {}, pageSize: {}",
                requestId, tableName, page, pageSize);

        try {
            Map<String, Object> columns = oracleSchemaRepository.getTableColumnsPaginated(
                    tableName, owner, page, pageSize);

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
    // 22. PAGINATED SEARCH SERVICE METHOD - UPDATED
    // ============================================================

    public Map<String, Object> searchObjectsPaginated(String requestId, HttpServletRequest req,
                                                      String performedBy, String searchQuery,
                                                      String searchType, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Paginated search with query: {}, type: {}, page: {}, pageSize: {}",
                requestId, searchQuery, searchType, page, pageSize);

        try {
            Map<String, Object> searchResults = oracleSchemaRepository.searchObjectsPaginated(
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
    // 23. GET OBJECT COUNTS ONLY SERVICE METHOD
    // ============================================================

    public Map<String, Object> getObjectCountsOnly(String requestId, HttpServletRequest req,
                                                   String performedBy, String objectName,
                                                   String objectType, String owner) {
        log.info("RequestEntity ID: {}, Getting counts only for {}: {}, owner: {}",
                requestId, objectType, objectName, owner);

        try {
            Map<String, Object> counts = oracleSchemaRepository.getObjectCountsOnly(
                    objectName, objectType, owner);

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
    // 24. ENHANCED SYNONYM METHODS
    // ============================================================

    public Map<String, Object> getAllSynonymsWithDetails(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle synonyms with details for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> synonyms = oracleSchemaRepository.getAllSynonymsWithDetails();

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

    public Map<String, Object> getSynonymsByTargetType(String requestId, HttpServletRequest req,
                                                       String performedBy, String targetType) {
        log.info("RequestEntity ID: {}, Getting synonyms by target type: {} for user: {}",
                requestId, targetType, performedBy);

        try {
            List<Map<String, Object>> synonyms = oracleSchemaRepository.getSynonymsByTargetType(targetType);

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

    public Map<String, Object> getSynonymDetailsEnhanced(String requestId, HttpServletRequest req,
                                                         String performedBy, String synonymName) {
        log.info("RequestEntity ID: {}, Getting enhanced details for synonym: {} for user: {}",
                requestId, synonymName, performedBy);

        try {
            Map<String, Object> synonymDetails = oracleSchemaRepository.getSynonymDetails(synonymName);

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

    public Map<String, Object> resolveSynonym(String requestId, HttpServletRequest req,
                                              String performedBy, String synonymName) {
        log.info("RequestEntity ID: {}, Resolving synonym: {} for user: {}", requestId, synonymName, performedBy);

        try {
            Map<String, Object> resolved = oracleSchemaRepository.resolveSynonym(synonymName);

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

    public Map<String, Object> validateSynonym(String requestId, HttpServletRequest req,
                                               String performedBy, String synonymName) {
        log.info("RequestEntity ID: {}, Validating synonym: {} for user: {}", requestId, synonymName, performedBy);

        try {
            Map<String, Object> validation = oracleSchemaRepository.validateSynonym(synonymName);

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

    // ============================================================
    // 25. ENHANCED OBJECT METHODS
    // ============================================================

    public Map<String, Object> getObjectDetailsByNameAndType(String requestId, HttpServletRequest req,
                                                             String performedBy, String objectName,
                                                             String objectType, String owner) {
        log.info("RequestEntity ID: {}, Getting details for {}: {}, owner: {}, user: {}",
                requestId, objectType, objectName, owner, performedBy);

        try {
            Map<String, Object> objectDetails = oracleSchemaRepository.getObjectDetailsByNameAndType(
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



    public Map<String, Object> resolveSynonymCustom(String requestId, String synonymName,
                                              String owner, String performedBy) {
        log.info("Resolving synonym: {}", synonymName);

        try {
            Map<String, Object> result = oracleSchemaRepository.getSynonymDetails(synonymName);

            // Add full path for easy reference
            if (result.containsKey("target_owner") && result.containsKey("target_name")) {
                String fullPath = result.get("target_owner") + "." + result.get("target_name");
                if (result.containsKey("db_link") && result.get("db_link") != null) {
                    fullPath += "@" + result.get("db_link");
                }
                result.put("fullPath", fullPath);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("data", result);
            response.put("responseCode", 200);
            response.put("message", "Synonym resolved successfully");
            response.put("requestId", requestId);
            response.put("timestamp", Instant.now().toString());

            return response;

        } catch (Exception e) {
            log.error("Error resolving synonym: {}", e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }


    public Map<String, Object> getObjectProperties(String requestId, String objectName,
                                                   String objectType, String owner, String performedBy) {
        log.info("Getting properties for {}: {}", objectType, objectName);

        try {
            String resolvedOwner = resolveOwner(owner);
            Map<String, Object> properties = new HashMap<>();

            switch (objectType.toUpperCase()) {
                case "TABLE":
                    properties = oracleSchemaRepository.getTableDetails(resolvedOwner, objectName);
                    // Remove large data that belongs to other endpoints
                    properties.remove("columns");
                    properties.remove("constraints");
                    properties.remove("indexes");
                    break;

                case "VIEW":
                    properties = oracleSchemaRepository.getViewDetails(resolvedOwner, objectName);
                    properties.remove("columns");
                    break;

                case "PROCEDURE":
                case "FUNCTION":
                    Map<String, Object> procDetails = oracleSchemaRepository.getProcedureDetails(
                            resolvedOwner, objectName);
                    properties.putAll(procDetails);
                    properties.remove("parameters");
                    properties.remove("source");
                    break;

                case "PACKAGE":
                    properties = oracleSchemaRepository.getPackageDetails(resolvedOwner, objectName);
                    properties.remove("procedures");
                    properties.remove("functions");
                    properties.remove("specSource");
                    properties.remove("bodySource");
                    break;

                case "SEQUENCE":
                    properties = oracleSchemaRepository.getSequenceDetails(resolvedOwner, objectName);
                    break;

                case "SYNONYM":
                    Map<String, Object> synonymDetails = oracleSchemaRepository.getSynonymDetails(objectName);
                    properties.putAll(synonymDetails);
                    break;

                default:
                    properties = oracleSchemaRepository.getBasicObjectInfo(resolvedOwner, objectName, objectType);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("data", properties);
            result.put("responseCode", 200);
            result.put("message", "Properties retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("Error getting properties: {}", e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }



    public Map<String, Object> getObjectColumnsPaginated(String requestId, String objectName,
                                                         String objectType, String owner,
                                                         int page, int pageSize, String performedBy) {
        log.info("Getting {} columns for {}: {}, page: {}", objectType, objectType, objectName, page);

        try {
            String resolvedOwner = resolveOwner(owner);
            String upperType = objectType.toUpperCase();
            Map<String, Object> result = new HashMap<>();

            // Check if it's a synonym that points to a table/view/procedure
            Map<String, Object> synonymInfo = oracleSchemaRepository.checkIfSynonymAndGetTarget(
                    objectName, upperType);

            if ((boolean) synonymInfo.getOrDefault("isSynonym", false) &&
                    !(boolean) synonymInfo.getOrDefault("isRemote", false)) {

                resolvedOwner = (String) synonymInfo.get("targetOwner");
                objectName = (String) synonymInfo.get("targetName");
                upperType = (String) synonymInfo.get("targetType");
            }

            if (upperType.equals("TABLE") || upperType.equals("VIEW")) {
                // Get columns for table/view
                Map<String, Object> columnsResult = oracleSchemaRepository.getTableColumnsPaginated(
                        objectName, resolvedOwner, page, pageSize);
                result.putAll(columnsResult);
                result.put("itemType", "column");

            } else if (upperType.equals("PROCEDURE") || upperType.equals("FUNCTION")) {
                // Get parameters for procedure/function
                Map<String, Object> paramsResult = oracleSchemaRepository.getProcedureParametersPaginated(
                        objectName, resolvedOwner, page, pageSize);
                result.putAll(paramsResult);
                result.put("itemType", "parameter");

            } else if (upperType.equals("PACKAGE")) {
                // Get package items
                Map<String, Object> packageItems = oracleSchemaRepository.getPackageItemsPaginated(
                        objectName, resolvedOwner, "ALL", page, pageSize);
                result.putAll(packageItems);
                result.put("itemType", "package_item");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("data", result);
            response.put("responseCode", 200);
            response.put("message", "Columns retrieved successfully");
            response.put("requestId", requestId);
            response.put("timestamp", Instant.now().toString());

            return response;

        } catch (Exception e) {
            log.error("Error getting columns: {}", e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }


    public Map<String, Object> getObjectBasicInfo(String requestId, String objectName,
                                                  String objectType, String owner, String performedBy) {
        log.info("Getting basic info for {}: {}", objectType, objectName);

        try {
            Map<String, Object> details = new HashMap<>();
            String resolvedOwner = resolveOwner(owner);

            // Add request metadata
            details.put("objectName", objectName);
            details.put("objectType", objectType);
            details.put("owner", resolvedOwner);
            details.put("queryTime", Instant.now().toString());
            details.put("performedBy", performedBy);

            // Check if it's a synonym first
            Map<String, Object> synonymCheck = checkIfSynonym(objectName);
            if ((boolean) synonymCheck.getOrDefault("isSynonym", false)) {
                details.put("isSynonym", true);
                details.put("synonymName", synonymCheck.get("synonymName"));
                details.put("targetOwner", synonymCheck.get("targetOwner"));
                details.put("targetName", synonymCheck.get("targetName"));
                details.put("targetType", synonymCheck.get("targetType"));
                details.put("targetStatus", synonymCheck.get("targetStatus"));
                details.put("dbLink", synonymCheck.get("dbLink"));
                details.put("isRemote", synonymCheck.get("dbLink") != null);
                details.put("fullPath", synonymCheck.get("dbLink") != null ?
                        synonymCheck.get("targetOwner") + "." + synonymCheck.get("targetName") + "@" + synonymCheck.get("dbLink") :
                        synonymCheck.get("targetOwner") + "." + synonymCheck.get("targetName"));
            }

            // Get basic object info from ALL_OBJECTS
            String sql = "SELECT owner, object_name, object_type, status, created, " +
                    "last_ddl_time, timestamp, temporary, generated, secondary, " +
                    "namespace, edition_name, sharing, editionable, oracle_maintained " +
                    "FROM all_objects WHERE UPPER(owner) = UPPER(?) " +
                    "AND UPPER(object_name) = UPPER(?) AND object_type = ?";

            try {
                Map<String, Object> basicInfo = oracleJdbcTemplate.queryForMap(
                        sql, resolvedOwner, objectName, objectType);
                details.putAll(basicInfo);
            } catch (EmptyResultDataAccessException e) {
                // Try without object type
                sql = "SELECT owner, object_name, object_type, status, created, last_ddl_time, " +
                        "timestamp, temporary, generated, secondary " +
                        "FROM all_objects WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?)";
                Map<String, Object> basicInfo = oracleJdbcTemplate.queryForMap(sql, resolvedOwner, objectName);
                details.putAll(basicInfo);
            }

            // Get object size information
            try {
                Map<String, Object> sizeInfo = getObjectSize(objectName, objectType);
                details.put("size", sizeInfo);
            } catch (Exception e) {
                log.debug("Could not get size info for {}.{}: {}", resolvedOwner, objectName, e.getMessage());
            }

            // Get object comments/description
            try {
                String comment = getObjectComment(resolvedOwner, objectName, objectType);
                if (comment != null && !comment.isEmpty()) {
                    details.put("comment", comment);
                }
            } catch (Exception e) {
                log.debug("Could not get comment for {}.{}", resolvedOwner, objectName);
            }

            // Get dependency counts
            try {
                // Objects that depend on this
                String usedByCount = "SELECT COUNT(*) FROM all_dependencies " +
                        "WHERE UPPER(referenced_owner) = UPPER(?) AND UPPER(referenced_name) = UPPER(?) " +
                        "AND UPPER(referenced_type) = UPPER(?)";
                int dependents = oracleJdbcTemplate.queryForObject(
                        usedByCount, Integer.class, resolvedOwner, objectName, objectType);
                details.put("dependentCount", dependents);

                // Objects that this depends on
                String dependsOnCount = "SELECT COUNT(*) FROM all_dependencies " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(name) = UPPER(?) " +
                        "AND UPPER(type) = UPPER(?)";
                int dependencies = oracleJdbcTemplate.queryForObject(
                        dependsOnCount, Integer.class, resolvedOwner, objectName, objectType);
                details.put("dependencyCount", dependencies);
            } catch (Exception e) {
                log.debug("Could not get dependency counts for {}.{}", resolvedOwner, objectName);
            }

            // Get privileges
            try {
                List<Map<String, Object>> privileges = getObjectPrivileges(resolvedOwner, objectName, objectType);
                if (!privileges.isEmpty()) {
                    details.put("privileges", privileges);
                    details.put("privilegeCount", privileges.size());
                }
            } catch (Exception e) {
                log.debug("Could not get privileges for {}.{}", resolvedOwner, objectName);
            }

            // Get object statistics
            try {
                Map<String, Object> stats = getObjectStatistics(resolvedOwner, objectName, objectType);
                if (!stats.isEmpty()) {
                    details.put("statistics", stats);
                }
            } catch (Exception e) {
                log.debug("Could not get statistics for {}.{}", resolvedOwner, objectName);
            }

            // Get partition info if applicable
            try {
                if (objectType.equalsIgnoreCase("TABLE") || objectType.equalsIgnoreCase("INDEX")) {
                    String partitionSql = "SELECT COUNT(*) FROM all_part_tables " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";
                    int partitionCount = oracleJdbcTemplate.queryForObject(
                            partitionSql, Integer.class, resolvedOwner, objectName);
                    if (partitionCount > 0) {
                        details.put("isPartitioned", true);
                        details.put("partitionCount", partitionCount);

                        // Get partition details
                        List<Map<String, Object>> partitions = getObjectPartitions(resolvedOwner, objectName, objectType);
                        details.put("partitions", partitions);
                    } else {
                        details.put("isPartitioned", false);
                    }
                }
            } catch (Exception e) {
                log.debug("Could not get partition info for {}.{}", resolvedOwner, objectName);
            }

            // Get type-specific additional info
            String upperType = objectType.toUpperCase();

            // TABLE specific info
            if (upperType.equals("TABLE")) {
                try {
                    // Get table-specific metadata
                    String tableSql = "SELECT " +
                            "    tablespace_name, " +
                            "    num_rows, " +
                            "    blocks, " +
                            "    empty_blocks, " +
                            "    avg_space, " +
                            "    chain_cnt, " +
                            "    avg_row_len, " +
                            "    sample_size, " +
                            "    last_analyzed, " +
                            "    degree, " +
                            "    instances, " +
                            "    cache, " +
                            "    table_lock, " +
                            "    row_movement, " +
                            "    compression, " +
                            "    compress_for, " +
                            "    dropped " +
                            "FROM all_tables " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";

                    Map<String, Object> tableInfo = oracleJdbcTemplate.queryForMap(tableSql, resolvedOwner, objectName);
                    details.put("tableInfo", tableInfo);

                    // Get column count
                    String colCount = "SELECT COUNT(*) FROM all_tab_columns " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";
                    int columnCount = oracleJdbcTemplate.queryForObject(colCount, Integer.class, resolvedOwner, objectName);
                    details.put("columnCount", columnCount);

                    // Get constraint counts by type
                    String constraintSql = "SELECT constraint_type, COUNT(*) as count " +
                            "FROM all_constraints " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?) " +
                            "GROUP BY constraint_type";
                    List<Map<String, Object>> constraintCounts = oracleJdbcTemplate.queryForList(
                            constraintSql, resolvedOwner, objectName);
                    if (!constraintCounts.isEmpty()) {
                        details.put("constraintCounts", constraintCounts);
                    }

                    // Get index count
                    String idxCount = "SELECT COUNT(*) FROM all_indexes " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";
                    int indexCount = oracleJdbcTemplate.queryForObject(idxCount, Integer.class, resolvedOwner, objectName);
                    details.put("indexCount", indexCount);

                    // Get trigger count
                    String trigCount = "SELECT COUNT(*) FROM all_triggers " +
                            "WHERE UPPER(table_owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";
                    int triggerCount = oracleJdbcTemplate.queryForObject(trigCount, Integer.class, resolvedOwner, objectName);
                    details.put("triggerCount", triggerCount);

                } catch (Exception e) {
                    log.debug("Could not get table-specific info for {}.{}", resolvedOwner, objectName);
                }
            }

            // VIEW specific info
            else if (upperType.equals("VIEW")) {
                try {
                    String viewSql = "SELECT text_length, text, read_only " +
                            "FROM all_views " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(view_name) = UPPER(?)";
                    Map<String, Object> viewInfo = oracleJdbcTemplate.queryForMap(viewSql, resolvedOwner, objectName);
                    details.put("viewInfo", viewInfo);

                    // Get column count
                    String colCount = "SELECT COUNT(*) FROM all_tab_columns " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";
                    int columnCount = oracleJdbcTemplate.queryForObject(colCount, Integer.class, resolvedOwner, objectName);
                    details.put("columnCount", columnCount);

                } catch (Exception e) {
                    log.debug("Could not get view-specific info for {}.{}", resolvedOwner, objectName);
                }
            }

            // PROCEDURE/FUNCTION specific info
            else if (upperType.equals("PROCEDURE") || upperType.equals("FUNCTION")) {
                try {
                    // Get parameter count
                    String paramCount = "SELECT COUNT(*) FROM all_arguments " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                            "AND package_name IS NULL AND argument_name IS NOT NULL";
                    int parameterCount = oracleJdbcTemplate.queryForObject(
                            paramCount, Integer.class, resolvedOwner, objectName);
                    details.put("parameterCount", parameterCount);

                    // Get return type for functions
                    if (upperType.equals("FUNCTION")) {
                        String returnSql = "SELECT data_type FROM all_arguments " +
                                "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                                "AND package_name IS NULL AND argument_name IS NULL AND position = 0";
                        try {
                            String returnType = oracleJdbcTemplate.queryForObject(
                                    returnSql, String.class, resolvedOwner, objectName);
                            details.put("returnType", returnType);
                        } catch (EmptyResultDataAccessException ex) {
                            // No return type found
                        }
                    }

                    // Check if it's part of a package
                    String packageCheck = "SELECT DISTINCT package_name FROM all_arguments " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                            "AND package_name IS NOT NULL";
                    List<String> packages = oracleJdbcTemplate.queryForList(
                            packageCheck, String.class, resolvedOwner, objectName);
                    if (!packages.isEmpty()) {
                        details.put("packageName", packages.get(0));
                        details.put("isPackageMember", true);
                    }

                } catch (Exception e) {
                    log.debug("Could not get procedure/function info for {}.{}", resolvedOwner, objectName);
                }
            }

            // PACKAGE specific info
            else if (upperType.equals("PACKAGE")) {
                try {
                    // Get spec status
                    String specStatus = "SELECT status FROM all_objects " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                            "AND object_type = 'PACKAGE'";
                    String status = oracleJdbcTemplate.queryForObject(specStatus, String.class, resolvedOwner, objectName);
                    details.put("specStatus", status);

                    // Get body status
                    try {
                        String bodyStatus = "SELECT status FROM all_objects " +
                                "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                                "AND object_type = 'PACKAGE BODY'";
                        String bodyStat = oracleJdbcTemplate.queryForObject(bodyStatus, String.class, resolvedOwner, objectName);
                        details.put("bodyStatus", bodyStat);
                        details.put("hasBody", true);
                    } catch (EmptyResultDataAccessException ex) {
                        details.put("hasBody", false);
                    }

                    // Get procedure count
                    String procCount = "SELECT COUNT(DISTINCT procedure_name) FROM all_arguments " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(package_name) = UPPER(?) " +
                            "AND procedure_name IS NOT NULL";
                    int procedureCount = oracleJdbcTemplate.queryForObject(
                            procCount, Integer.class, resolvedOwner, objectName);
                    details.put("procedureCount", procedureCount);

                } catch (Exception e) {
                    log.debug("Could not get package info for {}.{}", resolvedOwner, objectName);
                }
            }

            // SEQUENCE specific info
            else if (upperType.equals("SEQUENCE")) {
                try {
                    String seqSql = "SELECT min_value, max_value, increment_by, cycle_flag, " +
                            "order_flag, cache_size, last_number " +
                            "FROM all_sequences " +
                            "WHERE UPPER(sequence_owner) = UPPER(?) AND UPPER(sequence_name) = UPPER(?)";
                    Map<String, Object> seqInfo = oracleJdbcTemplate.queryForMap(seqSql, resolvedOwner, objectName);
                    details.put("sequenceInfo", seqInfo);
                } catch (Exception e) {
                    log.debug("Could not get sequence info for {}.{}", resolvedOwner, objectName);
                }
            }

            // INDEX specific info
            else if (upperType.equals("INDEX")) {
                try {
                    String idxSql = "SELECT index_type, table_owner, table_name, table_type, " +
                            "uniqueness, compression, prefix_length, tablespace_name, " +
                            "visibility, status, partitioned, temporary, generated, " +
                            "secondary, join_index, dropped " +
                            "FROM all_indexes " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(index_name) = UPPER(?)";
                    Map<String, Object> idxInfo = oracleJdbcTemplate.queryForMap(idxSql, resolvedOwner, objectName);
                    details.put("indexInfo", idxInfo);

                    // Get indexed columns
                    String colSql = "SELECT column_name, column_position, descend " +
                            "FROM all_ind_columns " +
                            "WHERE UPPER(index_owner) = UPPER(?) AND UPPER(index_name) = UPPER(?) " +
                            "ORDER BY column_position";
                    List<Map<String, Object>> columns = oracleJdbcTemplate.queryForList(
                            colSql, resolvedOwner, objectName);
                    if (!columns.isEmpty()) {
                        details.put("indexedColumns", columns);
                        details.put("indexedColumnCount", columns.size());
                    }

                } catch (Exception e) {
                    log.debug("Could not get index info for {}.{}", resolvedOwner, objectName);
                }
            }

            // TRIGGER specific info
            else if (upperType.equals("TRIGGER")) {
                try {
                    String trigSql = "SELECT trigger_type, triggering_event, table_owner, " +
                            "table_name, referencing_names, when_clause, status, " +
                            "description, trigger_body, crossedition, before_statement, " +
                            "before_row, after_row, instead_of_row, fire_once " +
                            "FROM all_triggers " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(trigger_name) = UPPER(?)";
                    Map<String, Object> trigInfo = oracleJdbcTemplate.queryForMap(trigSql, resolvedOwner, objectName);
                    details.put("triggerInfo", trigInfo);
                } catch (Exception e) {
                    log.debug("Could not get trigger info for {}.{}", resolvedOwner, objectName);
                }
            }

            // SYNONYM specific info
            else if (upperType.equals("SYNONYM")) {
                try {
                    String synSql = "SELECT table_owner, table_name, db_link " +
                            "FROM all_synonyms " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(synonym_name) = UPPER(?)";
                    Map<String, Object> synInfo = oracleJdbcTemplate.queryForMap(synSql, resolvedOwner, objectName);
                    details.putAll(synInfo);
                    details.put("isSynonym", true);
                    details.put("targetFullPath", synInfo.get("db_link") != null ?
                            synInfo.get("table_owner") + "." + synInfo.get("table_name") + "@" + synInfo.get("db_link") :
                            synInfo.get("table_owner") + "." + synInfo.get("table_name"));
                } catch (Exception e) {
                    log.debug("Could not get synonym info for {}.{}", resolvedOwner, objectName);
                }
            }

            // Get DDL availability
            try {
                boolean hasDDL = checkDDLAvailability(resolvedOwner, objectName, objectType);
                details.put("hasDDL", hasDDL);
            } catch (Exception e) {
                details.put("hasDDL", false);
            }

            // Get source availability (for code objects)
            if (isCodeObject(objectType)) {
                try {
                    String sourceCheck = "SELECT COUNT(*) FROM all_source " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(name) = UPPER(?) " +
                            "AND UPPER(type) = UPPER(?)";
                    int sourceLines = oracleJdbcTemplate.queryForObject(
                            sourceCheck, Integer.class, resolvedOwner, objectName, objectType);
                    details.put("hasSource", sourceLines > 0);
                    details.put("sourceLineCount", sourceLines);
                } catch (Exception e) {
                    details.put("hasSource", false);
                }
            }

            // Build final response
            Map<String, Object> result = new HashMap<>();
            result.put("data", details);
            result.put("responseCode", 200);
            result.put("message", "Comprehensive object info retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("Error getting comprehensive object info for {}.{}: {}",
                    owner, objectName, e.getMessage(), e);
            return createErrorResponse(requestId, "Failed to retrieve object info: " + e.getMessage());
        }
    }

// Helper methods needed for the above

    private Map<String, Object> getObjectSize(String objectName, String objectType) {
        Map<String, Object> sizeInfo = new HashMap<>();
        try {
            String sql = "SELECT bytes, blocks, extents FROM all_segments " +
                    "WHERE UPPER(segment_name) = UPPER(?) AND UPPER(segment_type) = UPPER(?)";
            Map<String, Object> result = oracleJdbcTemplate.queryForMap(sql, objectName, objectType);

            Long bytes = getLongValue(result.get("bytes"));
            sizeInfo.put("bytes", bytes);
            sizeInfo.put("readable", formatBytes(bytes));
            sizeInfo.put("blocks", result.get("blocks"));
            sizeInfo.put("extents", result.get("extents"));

        } catch (EmptyResultDataAccessException e) {
            sizeInfo.put("bytes", 0);
            sizeInfo.put("readable", "0 Bytes");
        }
        return sizeInfo;
    }

    private String getObjectComment(String owner, String objectName, String objectType) {
        try {
            if (objectType.equalsIgnoreCase("TABLE") || objectType.equalsIgnoreCase("VIEW")) {
                String sql = "SELECT comments FROM all_tab_comments " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";
                return oracleJdbcTemplate.queryForObject(sql, String.class, owner, objectName);
            } else if (objectType.equalsIgnoreCase("COLUMN")) {
                String sql = "SELECT comments FROM all_col_comments " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";
                return oracleJdbcTemplate.queryForObject(sql, String.class, owner, objectName);
            }
        } catch (EmptyResultDataAccessException e) {
            // No comment found
        }
        return null;
    }

    private List<Map<String, Object>> getObjectPrivileges(String owner, String objectName, String objectType) {
        String sql = "SELECT grantee, privilege, grantable, common, type " +
                "FROM all_tab_privs " +
                "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?) " +
                "ORDER BY grantee, privilege";
        return oracleJdbcTemplate.queryForList(sql, owner, objectName);
    }

    private Map<String, Object> getObjectStatistics(String owner, String objectName, String objectType) {
        Map<String, Object> stats = new HashMap<>();
        try {
            if (objectType.equalsIgnoreCase("TABLE")) {
                String sql = "SELECT num_rows, blocks, empty_blocks, avg_space, " +
                        "chain_cnt, avg_row_len, sample_size, last_analyzed " +
                        "FROM all_tab_statistics " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";
                stats = oracleJdbcTemplate.queryForMap(sql, owner, objectName);
            } else if (objectType.equalsIgnoreCase("INDEX")) {
                String sql = "SELECT blevel, leaf_blocks, distinct_keys, avg_leaf_blocks_per_key, " +
                        "avg_data_blocks_per_key, clustering_factor, num_rows, sample_size, last_analyzed " +
                        "FROM all_ind_statistics " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(index_name) = UPPER(?)";
                stats = oracleJdbcTemplate.queryForMap(sql, owner, objectName);
            }
        } catch (EmptyResultDataAccessException e) {
            // No statistics found
        }
        return stats;
    }

    private List<Map<String, Object>> getObjectPartitions(String owner, String objectName, String objectType) {
        try {
            if (objectType.equalsIgnoreCase("TABLE")) {
                String sql = "SELECT partition_name, subpartition_count, high_value, " +
                        "partition_position, tablespace_name, num_rows, last_analyzed " +
                        "FROM all_tab_partitions " +
                        "WHERE UPPER(table_owner) = UPPER(?) AND UPPER(table_name) = UPPER(?) " +
                        "ORDER BY partition_position";
                return oracleJdbcTemplate.queryForList(sql, owner, objectName);
            } else if (objectType.equalsIgnoreCase("INDEX")) {
                String sql = "SELECT partition_name, high_value, partition_position, " +
                        "tablespace_name, status, num_rows, last_analyzed " +
                        "FROM all_ind_partitions " +
                        "WHERE UPPER(index_owner) = UPPER(?) AND UPPER(index_name) = UPPER(?) " +
                        "ORDER BY partition_position";
                return oracleJdbcTemplate.queryForList(sql, owner, objectName);
            }
        } catch (Exception e) {
            log.debug("Could not get partitions: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    private boolean checkDDLAvailability(String owner, String objectName, String objectType) {
        try {
            String sql = "SELECT COUNT(*) FROM all_objects " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                    "AND object_type = ? AND status = 'VALID'";
            int count = oracleJdbcTemplate.queryForObject(sql, Integer.class, owner, objectName, objectType);
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isCodeObject(String objectType) {
        String upper = objectType.toUpperCase();
        return upper.equals("PROCEDURE") || upper.equals("FUNCTION") ||
                upper.equals("PACKAGE") || upper.equals("PACKAGE BODY") ||
                upper.equals("TYPE") || upper.equals("TYPE BODY") ||
                upper.equals("TRIGGER") || upper.equals("JAVA SOURCE");
    }


    private String formatBytes(long bytes) {
        if (bytes == 0) return "0 Bytes";
        String[] sizes = {"Bytes", "KB", "MB", "GB", "TB"};
        int i = (int) (Math.log(bytes) / Math.log(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, i), sizes[i]);
    }




    public Map<String, Object> validateObject(String requestId, HttpServletRequest req,
                                              String performedBy, String objectName,
                                              String objectType, String owner) {
        log.info("RequestEntity ID: {}, Validating {}: {}, owner: {}, user: {}",
                requestId, objectType, objectName, owner, performedBy);

        try {
            Map<String, Object> validation = oracleSchemaRepository.validateObject(objectName, objectType, owner);

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
            List<Map<String, Object>> results = oracleSchemaRepository.comprehensiveSearch(searchPattern);

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
            Map<String, Object> sizeInfo = oracleSchemaRepository.getObjectSize(objectName, objectType);

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
            Map<String, Object> queryResults = oracleSchemaRepository.executeQuery(query, timeoutSeconds, readOnly);

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
    // 29. PAGINATED METHODS FOR LARGE DATASETS - ALL UPDATED
    // ============================================================

    public Map<String, Object> getTablesPaginated(String requestId, HttpServletRequest req,
                                                  String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated tables, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = oracleSchemaRepository.getTablesPaginated(page, pageSize);

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
            Map<String, Object> paginatedData = oracleSchemaRepository.getViewsPaginated(page, pageSize);

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
            Map<String, Object> paginatedData = oracleSchemaRepository.getProceduresPaginated(page, pageSize);

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
            Map<String, Object> paginatedData = oracleSchemaRepository.getFunctionsPaginated(page, pageSize);

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
        log.info("RequestEntity ID: {}, Getting paginated packages, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = oracleSchemaRepository.getPackagesPaginated(page, pageSize);

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
            result.put("message", "Packages retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated packages: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getSynonymsPaginated(String requestId, HttpServletRequest req,
                                                    String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated synonyms, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = oracleSchemaRepository.getSynonymsPaginated(page, pageSize);

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
            result.put("message", "Synonyms retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            log.info("RequestEntity ID: {}, Retrieved {} synonyms (page {}/{})",
                    requestId, ((List<?>) paginatedData.get("items")).size(),
                    page, paginatedData.get("totalPages"));

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting paginated synonyms: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getSequencesPaginated(String requestId, HttpServletRequest req,
                                                     String performedBy, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Getting paginated sequences, page: {}, pageSize: {}",
                requestId, page, pageSize);

        try {
            Map<String, Object> paginatedData = oracleSchemaRepository.getSequencesPaginated(page, pageSize);

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
            Map<String, Object> paginatedData = oracleSchemaRepository.getTypesPaginated(page, pageSize);

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
            Map<String, Object> paginatedData = oracleSchemaRepository.getTriggersPaginated(page, pageSize);

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

    public Map<String, Object> getAllObjectCounts(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all object counts", requestId);

        try {
            Map<String, Object> counts = oracleSchemaRepository.getAllObjectCounts();

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
        log.info("RequestEntity ID: {}, Resolving synonym target: {}", requestId, synonymName);

        try {
            Map<String, Object> resolved = oracleSchemaRepository.getSynonymTargetDetails(synonymName);

            Map<String, Object> result = new HashMap<>();
            result.put("data", resolved);
            result.put("responseCode", 200);
            result.put("message", "Synonym resolved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error resolving synonym: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 30. DIAGNOSTIC METHODS
    // ============================================================

    public Map<String, Object> diagnoseDatabase(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Running Oracle database diagnostics for user: {}", requestId, performedBy);

        try {
            Map<String, Object> diagnostics = oracleSchemaRepository.diagnoseDatabase();

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
            log.error("RequestEntity ID: {}, Error diagnosing Oracle database: {}", requestId, e.getMessage());

            return createErrorResponse(requestId, e.getMessage());
        }
    }

    public Map<String, Object> getCurrentUserInfo(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting current user info for user: {}", requestId, performedBy);

        try {
            String currentUser = oracleSchemaRepository.getCurrentUser();
            String currentSchema = oracleSchemaRepository.getCurrentSchema();
            String dbVersion = oracleSchemaRepository.getDatabaseVersion();

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

    // ============================================================
    // 31. EXISTING TABLE METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllTables(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle tables for user: {}", requestId, performedBy);
        loggerUtil.log("oracleSchema",
                "RequestEntity ID: " + requestId + ", Getting all Oracle tables for user: " + performedBy);

        try {
            List<Map<String, Object>> tables = oracleSchemaRepository.getAllTables();

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    public Map<String, Object> getTablesBySchema(String requestId, HttpServletRequest req,
                                                 String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting tables from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> tables = oracleSchemaRepository.getTablesBySchema(schemaName);

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

    public Map<String, Object> getTableDetails(String requestId, HttpServletRequest req,
                                               String performedBy, String tableName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle table: {} for user: {}",
                requestId, tableName, performedBy);

        try {
            Map<String, Object> tableDetails = oracleSchemaRepository.getTableDetails(tableName);
            List<Map<String, Object>> columns = oracleSchemaRepository.getTableColumns(tableName);
            List<Map<String, Object>> constraints = oracleSchemaRepository.getTableConstraints(tableName);
            List<Map<String, Object>> indexes = oracleSchemaRepository.getTableIndexes(tableName);

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

    public Map<String, Object> searchTables(String requestId, HttpServletRequest req,
                                            String performedBy, String searchPattern) {
        log.info("RequestEntity ID: {}, Searching Oracle tables with pattern: {} for user: {}",
                requestId, searchPattern, performedBy);

        try {
            List<Map<String, Object>> tables = oracleSchemaRepository.searchTables(searchPattern);

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    public Map<String, Object> getTableStatistics(String requestId, HttpServletRequest req,
                                                  String performedBy, String tableName) {
        log.info("RequestEntity ID: {}, Getting statistics for Oracle table: {} for user: {}",
                requestId, tableName, performedBy);

        try {
            Map<String, Object> statistics = oracleSchemaRepository.getTableStatistics(tableName);

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

    public Map<String, Object> getTablesWithRowCount(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting Oracle tables with row count for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> tables = oracleSchemaRepository.getTablesWithRowCount();

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    public Map<String, Object> getTableCountByTablespace(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting Oracle table count by tablespace for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> tablespaceStats = oracleSchemaRepository.getTableCountByTablespace();

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    public Map<String, Object> getRecentTables(String requestId, HttpServletRequest req,
                                               String performedBy, int days) {
        log.info("RequestEntity ID: {}, Getting Oracle tables modified in last {} days for user: {}",
                requestId, days, performedBy);

        try {
            List<Map<String, Object>> tables = oracleSchemaRepository.getRecentTables(days);

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    // ============================================================
    // 32. EXISTING VIEW METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllViews(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle views for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> views = oracleSchemaRepository.getAllViews();

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    public Map<String, Object> getViewsBySchema(String requestId, HttpServletRequest req,
                                                String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting views from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> views = oracleSchemaRepository.getViewsBySchema(schemaName);

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

    public Map<String, Object> getViewDetails(String requestId, HttpServletRequest req,
                                              String performedBy, String viewName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle view: {} for user: {}",
                requestId, viewName, performedBy);

        try {
            Map<String, Object> viewDetails = oracleSchemaRepository.getViewDetails(viewName);

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
            log.error("RequestEntity ID: {}, Error getting details for Oracle view {}: {}",
                    requestId, viewName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 33. EXISTING PROCEDURE METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllProcedures(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle procedures for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> procedures = oracleSchemaRepository.getAllProcedures();

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    public Map<String, Object> getProceduresBySchema(String requestId, HttpServletRequest req,
                                                     String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting procedures from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> procedures = oracleSchemaRepository.getProceduresBySchema(schemaName);

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

    public Map<String, Object> getProcedureDetails(String requestId, HttpServletRequest req,
                                                   String performedBy, String procedureName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle procedure: {} for user: {}",
                requestId, procedureName, performedBy);

        try {
            Map<String, Object> procedureDetails = oracleSchemaRepository.getProcedureDetails(procedureName);

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
            log.error("RequestEntity ID: {}, Error getting details for Oracle procedure {}: {}",
                    requestId, procedureName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 34. EXISTING FUNCTION METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllFunctions(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle functions for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> functions = oracleSchemaRepository.getAllFunctions();

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    public Map<String, Object> getFunctionsBySchema(String requestId, HttpServletRequest req,
                                                    String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting functions from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> functions = oracleSchemaRepository.getFunctionsBySchema(schemaName);

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

    public Map<String, Object> getFunctionDetails(String requestId, HttpServletRequest req,
                                                  String performedBy, String functionName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle function: {} for user: {}",
                requestId, functionName, performedBy);

        try {
            Map<String, Object> functionDetails = oracleSchemaRepository.getFunctionDetails(functionName);

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
            log.error("RequestEntity ID: {}, Error getting details for Oracle function {}: {}",
                    requestId, functionName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 35. EXISTING PACKAGE METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllPackages(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle packages for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> packages = oracleSchemaRepository.getAllPackages();

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    public Map<String, Object> getPackagesBySchema(String requestId, HttpServletRequest req,
                                                   String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting packages from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> packages = oracleSchemaRepository.getPackagesBySchema(schemaName);

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

    public Map<String, Object> getPackageDetails(String requestId, HttpServletRequest req,
                                                 String performedBy, String packageName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle package: {} for user: {}",
                requestId, packageName, performedBy);

        try {
            Map<String, Object> packageDetails = oracleSchemaRepository.getPackageDetails(packageName);

            packageDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            packageDetails.put("generatedBy", performedBy);

            Map<String, Object> result = new HashMap<>();
            result.put("data", packageDetails);
            result.put("responseCode", 200);
            result.put("message", "Package details retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for Oracle package {}: {}",
                    requestId, packageName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 36. EXISTING TRIGGER METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllTriggers(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle triggers for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> triggers = oracleSchemaRepository.getAllTriggers();

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    public Map<String, Object> getTriggersBySchema(String requestId, HttpServletRequest req,
                                                   String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting triggers from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> triggers = oracleSchemaRepository.getTriggersBySchema(schemaName);

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

    public Map<String, Object> getTriggerDetails(String requestId, HttpServletRequest req,
                                                 String performedBy, String triggerName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle trigger: {} for user: {}",
                requestId, triggerName, performedBy);

        try {
            Map<String, Object> triggerDetails = oracleSchemaRepository.getTriggerDetails(triggerName);

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
            log.error("RequestEntity ID: {}, Error getting details for Oracle trigger {}: {}",
                    requestId, triggerName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 37. EXISTING SYNONYM METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllSynonyms(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle synonyms for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> synonyms = oracleSchemaRepository.getAllSynonyms();

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    public Map<String, Object> getSynonymsBySchema(String requestId, HttpServletRequest req,
                                                   String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting synonyms from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> synonyms = oracleSchemaRepository.getSynonymsBySchema(schemaName);

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

    // ============================================================
    // 38. EXISTING SEQUENCE METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllSequences(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle sequences for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> sequences = oracleSchemaRepository.getAllSequences();

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    public Map<String, Object> getSequencesBySchema(String requestId, HttpServletRequest req,
                                                    String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting sequences from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> sequences = oracleSchemaRepository.getSequencesBySchema(schemaName);

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

    public Map<String, Object> getSequenceDetails(String requestId, HttpServletRequest req,
                                                  String performedBy, String sequenceName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle sequence: {} for user: {}",
                requestId, sequenceName, performedBy);

        try {
            Map<String, Object> sequenceDetails = oracleSchemaRepository.getSequenceDetails(sequenceName);

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
            log.error("RequestEntity ID: {}, Error getting details for Oracle sequence {}: {}",
                    requestId, sequenceName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 39. EXISTING TYPE METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllTypes(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle types for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> types = oracleSchemaRepository.getAllTypes();

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    public Map<String, Object> getTypesBySchema(String requestId, HttpServletRequest req,
                                                String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting types from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> types = oracleSchemaRepository.getTypesBySchema(schemaName);

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

    public Map<String, Object> getTypeDetails(String requestId, HttpServletRequest req,
                                              String performedBy, String typeName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle type: {} for user: {}",
                requestId, typeName, performedBy);

        try {
            Map<String, Object> typeDetails = oracleSchemaRepository.getTypeDetails(typeName);

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
            log.error("RequestEntity ID: {}, Error getting details for Oracle type {}: {}",
                    requestId, typeName, e.getMessage());
            return createErrorResponse(requestId, e.getMessage());
        }
    }

    // ============================================================
    // 40. EXISTING DATABASE LINK METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllDbLinks(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle database links for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> dbLinks = oracleSchemaRepository.getAllDbLinks();

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    public Map<String, Object> getDbLinksBySchema(String requestId, HttpServletRequest req,
                                                  String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting database links from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> dbLinks = oracleSchemaRepository.getDbLinksBySchema(schemaName);

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

    // ============================================================
    // 41. EXISTING GENERAL OBJECT METHODS (LEGACY)
    // ============================================================

    public Map<String, Object> getAllObjects(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle objects for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> objects = oracleSchemaRepository.getAllObjects();

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    public Map<String, Object> getObjectsBySchema(String requestId, HttpServletRequest req,
                                                  String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting objects from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> objects = oracleSchemaRepository.getObjectsBySchema(schemaName);

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

    public Map<String, Object> searchObjects(String requestId, HttpServletRequest req,
                                             String performedBy, String searchPattern) {
        log.info("RequestEntity ID: {}, Searching Oracle objects with pattern: {} for user: {}",
                requestId, searchPattern, performedBy);

        try {
            List<Map<String, Object>> objects = oracleSchemaRepository.searchObjects(searchPattern);

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    public Map<String, Object> getObjectCountByType(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting Oracle object count by type for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> objectCounts = oracleSchemaRepository.getObjectCountByType();

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    public Map<String, Object> getInvalidObjects(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting invalid Oracle objects for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> invalidObjects = oracleSchemaRepository.getInvalidObjects();

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    public Map<String, Object> getObjectsByStatus(String requestId, HttpServletRequest req,
                                                  String performedBy, String status) {
        log.info("RequestEntity ID: {}, Getting Oracle objects with status: {} for user: {}",
                requestId, status, performedBy);

        try {
            List<Map<String, Object>> objects = oracleSchemaRepository.getObjectsByStatus(status);

            String currentSchema = oracleSchemaRepository.getCurrentSchema();

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

    // ============================================================
// NEW: COMBINED SEARCH SERVICE METHOD
// ============================================================

    public Map<String, Object> searchCombinedTypes(String requestId, HttpServletRequest req,
                                                   String performedBy, String query,
                                                   String types, int page, int pageSize) {
        log.info("RequestEntity ID: {}, Combined search, query: {}, types: {}, page: {}, pageSize: {}",
                requestId, query, types, page, pageSize);

        try {
            // Split the types
            String[] typeArray = types.split(",");

            // Perform the combined search
            Map<String, Object> searchResults = oracleSchemaRepository.searchCombinedTypes(
                    query, typeArray, page, pageSize);

            // Transform to frontend-friendly format
            List<Map<String, Object>> items = (List<Map<String, Object>>) searchResults.get("items");
            List<Map<String, Object>> transformedItems = new ArrayList<>();

            for (Map<String, Object> item : items) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("name", item.get("OBJECT_NAME"));
                transformed.put("owner", item.get("OWNER"));
                transformed.put("type", item.get("OBJECT_TYPE"));
                transformed.put("status", item.get("STATUS"));

                // If it's a synonym, add target info
                if ("SYNONYM".equalsIgnoreCase((String) item.get("OBJECT_TYPE"))) {
                    transformed.put("targetOwner", item.get("TABLE_OWNER"));
                    transformed.put("targetName", item.get("TABLE_NAME"));
                    transformed.put("targetType", item.get("TARGET_TYPE"));
                    transformed.put("isSynonym", true);
                } else {
                    transformed.put("isSynonym", false);
                }

                // Add to search score if available
                if (item.containsKey("SCORE")) {
                    transformed.put("score", item.get("SCORE"));
                }

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
// NEW: SEARCH PROCEDURES WITH SYNONYMS
// ============================================================

    public Map<String, Object> searchProceduresWithSynonyms(String requestId, HttpServletRequest req,
                                                            String performedBy, String query,
                                                            int page, int pageSize) {
        log.info("RequestEntity ID: {}, Searching procedures with synonyms, query: {}, page: {}, pageSize: {}",
                requestId, query, page, pageSize);

        try {
            // Search both procedures AND synonyms that target procedures
            Map<String, Object> searchResults = oracleSchemaRepository.searchProceduresWithSynonyms(
                    query, page, pageSize);

            List<Map<String, Object>> items = (List<Map<String, Object>>) searchResults.get("items");
            List<Map<String, Object>> transformedItems = new ArrayList<>();

            for (Map<String, Object> item : items) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("name", item.get("NAME"));
                transformed.put("owner", item.get("OWNER"));
                transformed.put("type", item.get("TYPE"));
                transformed.put("status", item.get("STATUS"));

                // Mark if it's a synonym
                boolean isSynonym = "SYNONYM".equalsIgnoreCase((String) item.get("TYPE"));
                transformed.put("isSynonym", isSynonym);

                if (isSynonym) {
                    transformed.put("targetOwner", item.get("TARGET_OWNER"));
                    transformed.put("targetName", item.get("TARGET_NAME"));
                    transformed.put("displayName", item.get("NAME") + " → " +
                            item.get("TARGET_OWNER") + "." + item.get("TARGET_NAME"));
                } else {
                    transformed.put("displayName", item.get("NAME"));
                }

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

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("responseCode", 200);
            result.put("message", "Search completed successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error searching procedures with synonyms: {}",
                    requestId, e.getMessage());

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
// NEW: GET SEARCH COUNT (Fast)
// ============================================================

    public int getSearchCount(String query, String types) {
        try {
            String[] typeArray = types.split(",");
            return oracleSchemaRepository.getSearchCount(query, typeArray);
        } catch (Exception e) {
            log.error("Error getting search count: {}", e.getMessage());
            return 0;
        }
    }

    // ============================================================
    // 42. HELPER METHODS
    // ============================================================

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
     * Helper method to calculate total pages
     */
    private int calculateTotalPages(long totalItems, int pageSize) {
        if (totalItems <= 0) return 0;
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    /**
     * Helper method to safely get long value from object
     */
    private long getLongValue(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Helper method to get the current user from the repository
     */
    public String getCurrentUser() {
        try {
            return oracleSchemaRepository.getCurrentUser();
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
            return oracleSchemaRepository.getCurrentSchema();
        } catch (Exception e) {
            log.error("Error getting current schema: {}", e.getMessage());
            return "UNKNOWN";
        }
    }


    public Map<String, Object> getTableConstraints(String requestId, String tableName,
                                                   String owner, String performedBy) {
        log.info("Getting constraints for table: {}", tableName);

        try {
            String resolvedOwner = resolveOwner(owner);

            // Check if it's a synonym that points to a table
            Map<String, Object> synonymInfo = oracleSchemaRepository.checkIfSynonymAndGetTarget(
                    tableName, "TABLE");

            if ((boolean) synonymInfo.getOrDefault("isSynonym", false) &&
                    !(boolean) synonymInfo.getOrDefault("isRemote", false)) {
                resolvedOwner = (String) synonymInfo.get("targetOwner");
                tableName = (String) synonymInfo.get("targetName");
            }

            List<Map<String, Object>> constraints = oracleSchemaRepository.getTableConstraints(
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
            String resolvedOwner = resolveOwner(owner);
            Map<String, Object> counts = new HashMap<>();

            String upperType = objectType.toUpperCase();

            // Column count for tables/views
            if (upperType.equals("TABLE") || upperType.equals("VIEW")) {
                String colSql = "SELECT COUNT(*) FROM all_tab_columns " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";
                int columnCount = oracleJdbcTemplate.queryForObject(
                        colSql, Integer.class, resolvedOwner, objectName);
                counts.put("columnCount", columnCount);
            }

            // Parameter count for procedures/functions/packages
            if (upperType.equals("PROCEDURE") || upperType.equals("FUNCTION") ||
                    upperType.equals("PACKAGE")) {
                String paramSql = "SELECT COUNT(*) FROM all_arguments " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                        "AND argument_name IS NOT NULL";
                int parameterCount = oracleJdbcTemplate.queryForObject(
                        paramSql, Integer.class, resolvedOwner, objectName);
                counts.put("parameterCount", parameterCount);
            }

            // Dependency count
            String depSql = "SELECT COUNT(*) FROM all_dependencies " +
                    "WHERE UPPER(referenced_owner) = UPPER(?) " +
                    "AND UPPER(referenced_name) = UPPER(?) " +
                    "AND UPPER(referenced_type) = UPPER(?)";
            int dependencyCount = oracleJdbcTemplate.queryForObject(
                    depSql, Integer.class, resolvedOwner, objectName, objectType);
            counts.put("dependencyCount", dependencyCount);

            // Synonym check
            String synSql = "SELECT COUNT(*) FROM all_synonyms " +
                    "WHERE UPPER(synonym_name) = UPPER(?)";
            int isSynonym = oracleJdbcTemplate.queryForObject(synSql, Integer.class, objectName);
            counts.put("isSynonym", isSynonym > 0);

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
            String resolvedOwner = resolveOwner(owner);

            // Check if it's a synonym
            Map<String, Object> synonymInfo = oracleSchemaRepository.checkIfSynonymAndGetTarget(
                    objectName, objectType);

            Map<String, Object> ddlResult;
            if ((boolean) synonymInfo.getOrDefault("isSynonym", false) &&
                    !(boolean) synonymInfo.getOrDefault("isRemote", false)) {
                // Get DDL of target object
                ddlResult = oracleSchemaRepository.getObjectDDLForFrontend(
                        (String) synonymInfo.get("targetName"),
                        (String) synonymInfo.get("targetType"));
            } else {
                ddlResult = oracleSchemaRepository.getObjectDDLForFrontend(objectName, objectType);
            }

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



    private String resolveOwner(String owner) {
        if (owner != null && !owner.isEmpty()) {
            return owner;
        }
        return oracleSchemaRepository.getCurrentUser();
    }

    private Map<String, Object> checkIfSynonym(String objectName) {
        return oracleSchemaRepository.checkIfSynonymAndGetTarget(objectName, null);
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