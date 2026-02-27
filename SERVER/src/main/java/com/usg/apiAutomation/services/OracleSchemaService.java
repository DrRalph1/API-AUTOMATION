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

    private final OracleSchemaRepository oracleSchemaRepository;
    private final LoggerUtil loggerUtil;

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

            log.info("RequestEntity ID: {}, Retrieved {} parameters for procedure: {}",
                    requestId, ((List<?>) parameters.get("parameters")).size(), procedureName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting parameters for procedure {}: {}",
                    requestId, procedureName, e.getMessage());

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
}