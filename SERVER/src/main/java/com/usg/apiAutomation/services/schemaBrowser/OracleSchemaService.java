package com.usg.apiAutomation.services.schemaBrowser;

import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiAutomation.enums.DatabaseTypeEnum;
import com.usg.apiAutomation.enums.OracleSqlStatementTypeEnum;
import com.usg.apiAutomation.repositories.schemaBrowser.oracle.*;
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

// Add these imports instead:
import java.sql.*;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class OracleSchemaService implements DatabaseSchemaService {

    private final OracleTableRepository oracleTableRepository;
    private final OracleViewRepository oracleViewRepository;
    private final OracleProcedureRepository oracleProcedureRepository;
    private final OracleFunctionRepository oracleFunctionRepository;
    private final OraclePackageRepository oraclePackageRepository;
    private final OracleOtherObjectsRepository oracleOtherObjectsRepository;
    private final OracleObjectRepository oracleObjectRepository;
    private final OracleSearchRepository oracleSearchRepository;
    private final OracleDependencyRepository oracleDependencyRepository;
    private final OracleDDLRepository oracleDDLRepository;
    private final OracleExecuteRepository oracleExecuteRepository;
    private final OracleRepository oracleRepository;
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
            Map<String, Object> schemaInfo = oracleRepository.getCurrentSchemaInfo();

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
            List<Map<String, Object>> tables = oracleTableRepository.getAllTablesForFrontend();

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
            Map<String, Object> paginatedData = oracleTableRepository.getAllTablesForFrontend(page, pageSize);

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
            Map<String, Object> tableDetails = oracleTableRepository.getTableDetailsForFrontend(tableName);

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
            Map<String, Object> tableDetails = oracleTableRepository.getTableDetailsForFrontend(tableName, page, pageSize);

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
            Map<String, Object> tableData = oracleTableRepository.getTableData(
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
            List<Map<String, Object>> views = oracleViewRepository.getAllViewsForFrontend();

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
            Map<String, Object> paginatedData = oracleViewRepository.getAllViewsForFrontend(page, pageSize);

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
            List<Map<String, Object>> procedures = oracleProcedureRepository.getAllProceduresForFrontend();

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
            Map<String, Object> paginatedData = oracleProcedureRepository.getAllProceduresForFrontend(page, pageSize);

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
            List<Map<String, Object>> functions = oracleFunctionRepository.getAllFunctionsForFrontend();

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
            Map<String, Object> paginatedData = oracleFunctionRepository.getAllFunctionsForFrontend(page, pageSize);

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
            List<Map<String, Object>> packages = oraclePackageRepository.getAllPackagesForFrontend();

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
            Map<String, Object> paginatedData = oraclePackageRepository.getAllPackagesForFrontend(page, pageSize);

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
            List<Map<String, Object>> triggers = oracleOtherObjectsRepository.getAllTriggersForFrontend();

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
            Map<String, Object> paginatedData = oracleOtherObjectsRepository.getAllTriggersForFrontend(page, pageSize);

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
            List<Map<String, Object>> synonyms = oracleOtherObjectsRepository.getAllSynonymsForFrontend();

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
            Map<String, Object> paginatedData = oracleOtherObjectsRepository.getAllSynonymsForFrontend(page, pageSize);

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
            List<Map<String, Object>> sequences = oracleOtherObjectsRepository.getAllSequencesForFrontend();

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
            Map<String, Object> paginatedData = oracleOtherObjectsRepository.getAllSequencesForFrontend(page, pageSize);

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
            List<Map<String, Object>> types = oracleOtherObjectsRepository.getAllTypesForFrontend();

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
            Map<String, Object> paginatedData = oracleOtherObjectsRepository.getAllTypesForFrontend(page, pageSize);

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
            Map<String, Object> objectDetails = oracleObjectRepository.getObjectDetails(objectName, objectType, owner);

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
            Map<String, Object> objectDetails = oracleObjectRepository.getObjectDetailsPaginated(
                    objectName, objectType, owner, page, pageSize, includeCounts);

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
            Map<String, Object> ddlResult = oracleDDLRepository.getObjectDDLForFrontend(objectName, objectType);

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
            Map<String, Object> targetDetails = oracleOtherObjectsRepository.getSynonymTargetDetails(synonymName);

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
            Map<String, Object> searchResults = oracleSearchRepository.searchObjectsForFrontend(
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
            Map<String, Object> tableData = oracleTableRepository.getTableDataAdvanced(
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
            Map<String, Object> parameters = oracleProcedureRepository.getProcedureParametersPaginated(
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
            Map<String, Object> parameters = oracleFunctionRepository.getFunctionParametersPaginated(
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
            List<Map<String, Object>> usedBy = oracleDependencyRepository.getUsedBy(objectName, objectType, owner);

            Map<String, Object> data = new HashMap<>();
            data.put("items", usedBy);
            data.put("totalCount", usedBy.size());
            data.put("objectName", objectName);
            data.put("objectType", objectType);
            data.put("owner", owner != null ? owner : oracleRepository.getCurrentUser());

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
            Map<String, Object> paginatedData = oracleDependencyRepository.getUsedByPaginated(
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
            data.put("owner", owner != null ? owner : oracleRepository.getCurrentUser());

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
            Map<String, Object> hierarchy = oracleDependencyRepository.getDependencyHierarchy(
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
            int count = oracleDependencyRepository.getUsedByCount(objectName, objectType, owner);

            Map<String, Object> data = new HashMap<>();
            data.put("count", count);
            data.put("objectName", objectName);
            data.put("objectType", objectType);
            data.put("owner", owner != null ? owner : oracleRepository.getCurrentUser());

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
            Map<String, Object> summary = oracleDependencyRepository.getUsedBySummary(
                    objectName, objectType, owner);

            summary.put("objectName", objectName);
            summary.put("objectType", objectType);
            summary.put("owner", owner != null ? owner : oracleRepository.getCurrentUser());
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
            Map<String, Object> items = oraclePackageRepository.getPackageItemsPaginated(
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
            Map<String, Object> columns = oracleTableRepository.getTableColumnsPaginated(
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
            Map<String, Object> searchResults = oracleSearchRepository.searchObjectsPaginated(
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
            Map<String, Object> counts = oracleObjectRepository.getObjectCountsOnly(
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
            List<Map<String, Object>> synonyms = oracleOtherObjectsRepository.getAllSynonymsWithDetails();

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
            List<Map<String, Object>> synonyms = oracleOtherObjectsRepository.getSynonymsByTargetType(targetType);

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
            Map<String, Object> synonymDetails = oracleOtherObjectsRepository.getSynonymDetails(synonymName);

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
            Map<String, Object> resolved = oracleOtherObjectsRepository.resolveSynonym(synonymName);

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
            Map<String, Object> validation = oracleOtherObjectsRepository.validateSynonym(synonymName);

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
            Map<String, Object> objectDetails = oracleObjectRepository.getObjectDetailsByNameAndType(
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
            Map<String, Object> result = oracleOtherObjectsRepository.getSynonymDetails(synonymName);

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
                    properties = oracleTableRepository.getTableDetails(resolvedOwner, objectName);
                    // Remove large data that belongs to other endpoints
                    properties.remove("columns");
                    properties.remove("constraints");
                    properties.remove("indexes");
                    break;

                case "VIEW":
                    properties = oracleViewRepository.getViewDetails(resolvedOwner, objectName);
                    properties.remove("columns");
                    break;

                case "PROCEDURE":
                case "FUNCTION":
                    Map<String, Object> procDetails = oracleProcedureRepository.getProcedureDetails(
                            resolvedOwner, objectName);
                    properties.putAll(procDetails);
                    properties.remove("parameters");
                    properties.remove("source");
                    break;

                case "PACKAGE":
                    properties = oraclePackageRepository.getPackageDetails(resolvedOwner, objectName);
                    properties.remove("procedures");
                    properties.remove("functions");
                    properties.remove("specSource");
                    properties.remove("bodySource");
                    break;

                case "SEQUENCE":
                    properties = oracleOtherObjectsRepository.getSequenceDetails(resolvedOwner, objectName);
                    break;

                case "SYNONYM":
                    Map<String, Object> synonymDetails = oracleOtherObjectsRepository.getSynonymDetails(objectName);
                    properties.putAll(synonymDetails);
                    break;

                default:
                    properties = oracleObjectRepository.getBasicObjectInfo(resolvedOwner, objectName, objectType);
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
            Map<String, Object> synonymInfo = oracleRepository.checkIfSynonymAndGetTarget(
                    objectName, upperType);

            if ((boolean) synonymInfo.getOrDefault("isSynonym", false) &&
                    !(boolean) synonymInfo.getOrDefault("isRemote", false)) {

                resolvedOwner = (String) synonymInfo.get("targetOwner");
                objectName = (String) synonymInfo.get("targetName");
                upperType = (String) synonymInfo.get("targetType");
            }

            if (upperType.equals("TABLE") || upperType.equals("VIEW")) {
                // Get columns for table/view
                Map<String, Object> columnsResult = oracleTableRepository.getTableColumnsPaginated(
                        objectName, resolvedOwner, page, pageSize);
                result.putAll(columnsResult);
                result.put("itemType", "column");

            } else if (upperType.equals("PROCEDURE") || upperType.equals("FUNCTION")) {
                // Get parameters for procedure/function
                Map<String, Object> paramsResult = oracleProcedureRepository.getProcedureParametersPaginated(
                        objectName, resolvedOwner, page, pageSize);
                result.putAll(paramsResult);
                result.put("itemType", "parameter");

            } else if (upperType.equals("PACKAGE")) {
                // Get package items
                Map<String, Object> packageItems = oraclePackageRepository.getPackageItemsPaginated(
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
            Map<String, Object> validation = oracleObjectRepository.validateObject(objectName, objectType, owner);

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
            List<Map<String, Object>> results = oracleSearchRepository.comprehensiveSearch(searchPattern);

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
            Map<String, Object> sizeInfo = oracleObjectRepository.getObjectSize(objectName, objectType);

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

        Map<String, Object> result = new HashMap<>();
        String trimmedQuery = query.trim().toUpperCase();
        OracleSqlStatementTypeEnum statementType = OracleSqlStatementTypeEnum.UNKNOWN;

        try {
            // Detect statement type
            statementType = detectSqlStatementType(query, trimmedQuery);
            log.info("RequestEntity ID: {}, Detected statement type: {}", requestId, statementType);

            // Check if operation is allowed in read-only mode
            if (readOnly && !isReadOnlyAllowed(statementType)) {
                String errorMsg = String.format(
                        "Operation not allowed in read-only mode. Statement type '%s' requires write access. " +
                                "Please use readOnly=false or switch to SELECT/query operations.",
                        statementType
                );
                log.warn("RequestEntity ID: {}, {}", requestId, errorMsg);

                result.put("success", false);
                result.put("message", errorMsg);
                result.put("error", errorMsg);
                result.put("data", Map.of(
                        "columns", new ArrayList<>(),
                        "rows", new ArrayList<>(),
                        "rowCount", 0
                ));
                return result;
            }

            // Execute based on statement type
            switch (statementType) {
                case SELECT:
                case WITH:
                case VIEW_QUERY:
                    Map<String, Object> queryResult = executeSelectQuery(query, timeoutSeconds);
                    result.put("success", true);
                    result.put("message", getSuccessMessage(statementType, queryResult));
                    // Wrap the queryResult inside a "data" field for consistency
                    result.put("data", queryResult);
                    break;

                case INSERT:
                case UPDATE:
                case DELETE:
                case MERGE:
                    int affectedRows = executeUpdateQuery(query, timeoutSeconds);
                    result.put("success", true);
                    result.put("message", String.format("%s executed successfully, %d rows affected",
                            statementType, affectedRows));
                    result.put("data", Map.of(
                            "rowsAffected", affectedRows,
                            "rowCount", affectedRows
                    ));
                    break;

                case PROCEDURE:
                case FUNCTION:
                case PACKAGE:
                case CREATE_PROCEDURE:
                case CREATE_FUNCTION:
                case CREATE_PACKAGE:
                case CREATE_TABLE:
                case CREATE_VIEW:
                case CREATE_TRIGGER:
                case CREATE_SEQUENCE:
                case CREATE_SYNONYM:
                case CREATE_TYPE:
                case ALTER:
                case DROP:
                case TRUNCATE:
                case GRANT:
                case REVOKE:
                case COMMENT:
                case RENAME:
                case DDL:
                    executeDDL(query, timeoutSeconds);
                    result.put("success", true);
                    result.put("message", String.format("%s compiled successfully", statementType));
                    result.put("data", Map.of(
                            "message", "Object compiled successfully",
                            "rowCount", 0
                    ));
                    break;

                case PLSQL_BLOCK:
                case ANONYMOUS_BLOCK:
                    Map<String, Object> plsqlResult = executePLSQLBlock(query, timeoutSeconds);

                    // Get the output from the procedure
                    String output = (String) plsqlResult.getOrDefault("output", "");
                    boolean success = (boolean) plsqlResult.getOrDefault("success", false);
                    String responseCode = (String) plsqlResult.getOrDefault("responseCode", "");

                    // Don't add extra generic messages - use the actual output as the message
                    String finalMessage = output.trim();
                    if (finalMessage.isEmpty()) {
                        finalMessage = success ? "PL/SQL block executed successfully" : "Execution failed";
                    }

                    result.put("success", success);
                    result.put("message", finalMessage);
                    result.put("data", Map.of(
                            "output", output,
                            "rows", plsqlResult.getOrDefault("rows", new ArrayList<>()),
                            "rowCount", plsqlResult.getOrDefault("rowCount", 0),
                            "responseCode", responseCode,
                            "batchNumber", plsqlResult.getOrDefault("batchNumber", "")
                    ));
                    break;

                case CALL:
                case EXECUTE:
                    Map<String, Object> callResult = executeCall(query, timeoutSeconds);
                    result.put("success", true);
                    result.put("message", "Procedure/function executed successfully");
                    result.put("data", callResult);
                    break;

                default:
                    if (trimmedQuery.startsWith("SELECT")) {
                        Map<String, Object> defaultResult = executeSelectQuery(query, timeoutSeconds);
                        result.put("success", true);
                        result.put("message", "Query executed successfully");
                        result.put("data", defaultResult);
                    } else {
                        int defaultAffected = executeUpdateQuery(query, timeoutSeconds);
                        result.put("success", true);
                        result.put("message", "Statement executed successfully");
                        result.put("data", Map.of("rowsAffected", defaultAffected));
                    }
            }

            log.info("RequestEntity ID: {}, Query executed successfully", requestId);
            return result;

        } catch (RuntimeException e) {
            // Extract the user-friendly message from the exception
            String errorMessage = e.getMessage();

            log.error("RequestEntity ID: {}, Error executing {}: {}",
                    requestId, statementType, errorMessage, e);

            // Check for privilege errors
            if (errorMessage.contains("Insufficient privileges") || errorMessage.contains("ORA-01031")) {
                result.put("success", false);
                result.put("message", errorMessage);
                result.put("error", "Insufficient database privileges");
            }
            // Check for syntax errors
            else if (errorMessage.contains("Invalid syntax") || errorMessage.contains("ORA-00922") ||
                    errorMessage.contains("Invalid SQL") || errorMessage.contains("ORA-00900")) {
                result.put("success", false);
                result.put("message", errorMessage);
                result.put("error", "SQL syntax error");
            }
            // Check for object does not exist
            else if (errorMessage.contains("does not exist") || errorMessage.contains("ORA-00942")) {
                result.put("success", false);
                result.put("message", errorMessage);
                result.put("error", "Object not found");
            }
            // Check for object already exists
            else if (errorMessage.contains("already exists") || errorMessage.contains("ORA-00955")) {
                result.put("success", false);
                result.put("message", errorMessage);
                result.put("error", "Object already exists");
            }
            // Check for quota issues
            else if (errorMessage.contains("quota") || errorMessage.contains("ORA-01536")) {
                result.put("success", false);
                result.put("message", errorMessage);
                result.put("error", "Insufficient tablespace quota");
            }
            // Check for Oracle error
            else if (errorMessage.contains("ORA-")) {
                result.put("success", false);
                result.put("message", errorMessage);
                result.put("error", "Database error");
            }
            // Generic error
            else {
                result.put("success", false);
                result.put("message", errorMessage);
                result.put("error", errorMessage);
            }

            result.put("data", Map.of(
                    "columns", new ArrayList<>(),
                    "rows", new ArrayList<>(),
                    "rowCount", 0
            ));
            return result;
        }
    }



    /**
     * Cleans SQL statements by removing trailing semicolons and other common issues
     * @param sql The SQL statement to clean
     * @return Cleaned SQL statement safe for JDBC execution
     */
    private String cleanSqlStatement(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        String cleaned = sql.trim();

        // Remove trailing semicolon(s) - JDBC doesn't need them and Oracle rejects them
        while (cleaned.endsWith(";")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
        }

        // Remove any leading/trailing whitespace
        cleaned = cleaned.trim();

        // Log the cleaning for debugging
        if (!cleaned.equals(sql.trim())) {
            log.info("Cleaned SQL statement - Original: [{}], Cleaned: [{}]", sql, cleaned);
        }

        return cleaned;
    }



    /**
     * Execute SELECT query
     */
    private Map<String, Object> executeSelectQuery(String query, int timeoutSeconds) {
        try {
            // CLEAN THE SQL BEFORE EXECUTION - THIS IS THE FIX
            String cleanedQuery = cleanSqlStatement(query);
            log.info("Original query: [{}], Cleaned query: [{}]", query, cleanedQuery);

            long startTime = System.currentTimeMillis();
            List<Map<String, Object>> rows = oracleJdbcTemplate.queryForList(cleanedQuery);
            long executionTime = System.currentTimeMillis() - startTime;

            // Extract columns from the first row (if any)
            List<String> columns = rows.isEmpty() ? new ArrayList<>() : new ArrayList<>(rows.get(0).keySet());

            Map<String, Object> result = new HashMap<>();
            result.put("columns", columns);
            result.put("rows", rows);
            result.put("rowCount", rows.size());
            result.put("executionTimeMs", executionTime);

            // Add pagination info for consistency (even though not paginated here)
            result.put("page", 1);
            result.put("pageSize", rows.size());
            result.put("totalPages", 1);
            result.put("totalRows", rows.size());

            return result;
        } catch (Exception e) {
            log.error("Error executing SELECT query", e);
            throw new RuntimeException("Failed to execute SELECT query: " + e.getMessage(), e);
        }
    }


    /**
     * Extracts the full Oracle error message from the exception chain
     */
    private String extractFullOracleError(Exception e) {
        Throwable cause = e;
        Set<String> seenMessages = new HashSet<>();
        StringBuilder fullError = new StringBuilder();

        while (cause != null) {
            String message = cause.getMessage();
            if (message != null && !message.isEmpty()) {
                // Avoid duplicate messages
                if (!seenMessages.contains(message)) {
                    seenMessages.add(message);

                    // Look for ORA-xxxxx pattern
                    if (message.contains("ORA-")) {
                        // This is the Oracle error we want
                        return message;
                    }

                    // Build chain if needed for debugging
                    if (fullError.length() > 0) {
                        fullError.append(" -> ");
                    }
                    fullError.append(message);
                }
            }
            cause = cause.getCause();
        }

        // If we found an ORA error, we would have returned it already
        // Otherwise return the chain or original message
        return fullError.length() > 0 ? fullError.toString() : e.getMessage();
    }


    /**
     * Execute UPDATE/INSERT/DELETE query
     */
    private int executeUpdateQuery(String query, int timeoutSeconds) {
        try {
            // Clean the SQL before execution
            String cleanedQuery = cleanSqlStatement(query);
            return oracleJdbcTemplate.update(cleanedQuery);
        } catch (Exception e) {
            log.error("Error executing UPDATE query", e);

            // Extract the full Oracle error
            String detailedError = extractFullOracleError(e);

            // Check for insufficient privileges
            if (detailedError.contains("ORA-01031")) {
                throw new RuntimeException("ORA-01031: insufficient privileges");
            }
            // Check for table or view does not exist
            else if (detailedError.contains("ORA-00942")) {
                throw new RuntimeException("ORA-00942: table or view does not exist");
            }
            // Check for invalid identifier
            else if (detailedError.contains("ORA-00904")) {
                throw new RuntimeException("ORA-00904: invalid identifier");
            }
            // Check for unique constraint violation
            else if (detailedError.contains("ORA-00001")) {
                throw new RuntimeException("ORA-00001: unique constraint violated");
            }
            // Check for check constraint violation
            else if (detailedError.contains("ORA-02290")) {
                throw new RuntimeException("ORA-02290: check constraint violated");
            }
            // Check for parent key not found
            else if (detailedError.contains("ORA-02291")) {
                throw new RuntimeException("ORA-02291: integrity constraint violated - parent key not found");
            }
            // Check for child record found
            else if (detailedError.contains("ORA-02292")) {
                throw new RuntimeException("ORA-02292: integrity constraint violated - child record found");
            }
            // Check for value too large
            else if (detailedError.contains("ORA-12899")) {
                throw new RuntimeException("ORA-12899: value too large for column");
            }
            // Check for cannot insert NULL
            else if (detailedError.contains("ORA-01400")) {
                throw new RuntimeException("ORA-01400: cannot insert NULL into column");
            }
            // Check for invalid number
            else if (detailedError.contains("ORA-01722")) {
                throw new RuntimeException("ORA-01722: invalid number");
            }
            // Generic Oracle error
            else if (detailedError.contains("ORA-")) {
                // Extract just the ORA error without the SQL
                Pattern pattern = Pattern.compile("ORA-\\d{5}:\\s*[^\\n]*");
                Matcher matcher = pattern.matcher(detailedError);
                if (matcher.find()) {
                    throw new RuntimeException(matcher.group());
                }
            }

            throw new RuntimeException("Failed to execute UPDATE query: " + detailedError);
        }
    }

    /**
     * Execute DDL statement (CREATE, ALTER, DROP, etc.)
     */
    private void executeDDL(String query, int timeoutSeconds) {
        try {
            // Clean the SQL before execution - remove trailing semicolons
            String cleanedQuery = cleanSqlStatement(query);
            log.info("Original DDL: [{}], Cleaned DDL: [{}]", query, cleanedQuery);

            oracleJdbcTemplate.execute(cleanedQuery);
        } catch (Exception e) {
            log.error("Error executing DDL", e);

            // Extract the root cause to get the actual Oracle error
            Throwable rootCause = e;
            while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                rootCause = rootCause.getCause();
            }

            String errorMessage = rootCause.getMessage();
            String cleanErrorMessage = errorMessage;

            // Extract just the Oracle error without the SQL
            if (errorMessage != null) {
                // Find the first line of the error (before the SQL)
                int newlineIndex = errorMessage.indexOf('\n');
                if (newlineIndex > 0) {
                    cleanErrorMessage = errorMessage.substring(0, newlineIndex);
                }

                // Remove any SQL from the message
                cleanErrorMessage = cleanErrorMessage.replaceAll("\\[CREATE.*", "").trim();
            }

            // Check for insufficient privileges (ORA-01031)
            if (errorMessage != null && errorMessage.contains("ORA-01031")) {
                String userFriendlyError = "Insufficient privileges (ORA-01031): You don't have permission to execute this DDL statement.\n\n" +
                        "Please contact your database administrator to grant the required privileges.\n" +
                        "Required privileges may include: CREATE TABLE, CREATE PROCEDURE, CREATE VIEW, etc.";
                throw new RuntimeException(userFriendlyError);
            }
            // Check for missing or invalid option (ORA-00922)
            else if (errorMessage != null && errorMessage.contains("ORA-00922")) {
                String userFriendlyError = "Invalid syntax or missing option (ORA-00922): " + cleanErrorMessage + "\n\n" +
                        "Please check your SQL syntax. Common issues:\n" +
                        "- Missing column definitions\n" +
                        "- Invalid data type\n" +
                        "- Schema name doesn't exist or you don't have permission to create objects in that schema\n" +
                        "- Missing required keywords like 'TABLE'";
                throw new RuntimeException(userFriendlyError);
            }
            // Check for invalid SQL (ORA-00900)
            else if (errorMessage != null && errorMessage.contains("ORA-00900")) {
                String userFriendlyError = "Invalid SQL statement (ORA-00900): " + cleanErrorMessage;
                throw new RuntimeException(userFriendlyError);
            }
            // Check for table or view does not exist (ORA-00942)
            else if (errorMessage != null && errorMessage.contains("ORA-00942")) {
                String userFriendlyError = "Table or view does not exist (ORA-00942): " + cleanErrorMessage;
                throw new RuntimeException(userFriendlyError);
            }
            // Check for object already exists (ORA-00955)
            else if (errorMessage != null && errorMessage.contains("ORA-00955")) {
                String userFriendlyError = "Object already exists (ORA-00955): " + cleanErrorMessage;
                throw new RuntimeException(userFriendlyError);
            }
            // Check for insufficient privileges on tablespace (ORA-01536)
            else if (errorMessage != null && errorMessage.contains("ORA-01536")) {
                String userFriendlyError = "Insufficient quota on tablespace (ORA-01536): " + cleanErrorMessage + "\n\n" +
                        "Please contact your DBA to increase your tablespace quota.";
                throw new RuntimeException(userFriendlyError);
            }
            // Generic Oracle error
            else if (errorMessage != null && errorMessage.contains("ORA-")) {
                // Extract the ORA error code and message only
                String[] lines = errorMessage.split("\n");
                String firstLine = lines[0];
                // Remove the SQL that follows
                if (firstLine.contains(":")) {
                    // Keep only up to the first colon after ORA
                    String oraPart = firstLine.substring(0, Math.min(firstLine.length(), firstLine.indexOf(":") + 50));
                    cleanErrorMessage = oraPart;
                }
                String userFriendlyError = "Oracle error: " + cleanErrorMessage;
                throw new RuntimeException(userFriendlyError);
            }
            // Unknown error
            else {
                // Get just the first line of the error
                String firstLine = errorMessage != null ? errorMessage.split("\n")[0] : e.getMessage();
                throw new RuntimeException("Failed to execute DDL: " + firstLine);
            }
        }
    }

    /**
     * Execute PL/SQL block with DBMS_OUTPUT capture - Generic approach for any PL/SQL block
     */
    private Map<String, Object> executePLSQLBlock(String query, int timeoutSeconds) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        StringBuilder output = new StringBuilder();
        Map<String, Object> outParams = new HashMap<>();

        try {
            Connection conn = oracleJdbcTemplate.getDataSource().getConnection();
            conn.setAutoCommit(false);

            try {
                // Enable DBMS_OUTPUT
                try (CallableStatement enableOutput = conn.prepareCall("BEGIN DBMS_OUTPUT.ENABLE(1000000); END;")) {
                    enableOutput.execute();
                }

                // Create a wrapper PL/SQL block that captures all OUT parameters
                // This is the key - we wrap the user's PL/SQL block to capture everything
                String wrappedQuery = wrapPlSqlBlockWithCapture(query);

                try (CallableStatement cs = conn.prepareCall(wrappedQuery)) {
                    boolean hadResults = cs.execute();

                    // Get any result sets
                    if (hadResults) {
                        ResultSet rs = cs.getResultSet();
                        if (rs != null) {
                            ResultSetMetaData meta = rs.getMetaData();
                            int columnCount = meta.getColumnCount();

                            while (rs.next()) {
                                Map<String, Object> row = new HashMap<>();
                                for (int i = 1; i <= columnCount; i++) {
                                    row.put(meta.getColumnName(i), rs.getObject(i));
                                }
                                rows.add(row);
                            }
                            rs.close();
                        }
                    }
                }

                // Get DBMS_OUTPUT content
                try (CallableStatement getOutput = conn.prepareCall(
                        "BEGIN DBMS_OUTPUT.GET_LINE(?, ?); END;")) {
                    getOutput.registerOutParameter(1, Types.VARCHAR);
                    getOutput.registerOutParameter(2, Types.INTEGER);

                    int status = 0;
                    while (status == 0) {
                        getOutput.execute();
                        String line = getOutput.getString(1);
                        status = getOutput.getInt(2);
                        if (line != null && status == 0) {
                            output.append(line).append("\n");
                        }
                    }
                }

                conn.commit();

                // Parse the output to extract meaningful information
                Map<String, String> parsedOutput = parseProcedureOutput(output.toString());

                // Build result
                result.put("rows", rows);
                result.put("rowCount", rows.size());
                result.put("output", output.toString());
                result.put("dbmsOutput", output.toString());
                result.put("outParams", outParams);

                // Extract response code and message from output
                String responseCode = parsedOutput.getOrDefault("responseCode", "");
                String message = parsedOutput.getOrDefault("message", output.toString().trim());
                String batchNumber = parsedOutput.getOrDefault("batchNumber", "");

                // Create user-friendly message based on response code
                if ("000".equals(responseCode) || message.toLowerCase().contains("successfully") ||
                        (responseCode.isEmpty() && !output.toString().toLowerCase().contains("error"))) {
                    result.put("success", true);
                    result.put("message", message.isEmpty() ? "PL/SQL block executed successfully" : message);
                    result.put("responseCode", responseCode.isEmpty() ? "000" : responseCode);
                } else if (!responseCode.isEmpty()) {
                    result.put("success", false);
                    result.put("message", message + " (Response Code: " + responseCode + ")");
                    result.put("responseCode", responseCode);
                } else if (output.toString().toLowerCase().contains("error") ||
                        output.toString().toLowerCase().contains("ora-")) {
                    result.put("success", false);
                    result.put("message", output.toString().trim());
                } else {
                    result.put("success", true);
                    result.put("message", output.toString().trim().isEmpty() ?
                            "PL/SQL block executed successfully" : output.toString().trim());
                }

                result.put("batchNumber", batchNumber);

                if (!rows.isEmpty()) {
                    result.put("columns", new ArrayList<>(rows.get(0).keySet()));
                }

                return result;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.close();
            }

        } catch (Exception e) {
            log.error("Error executing PL/SQL block", e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Failed to execute PL/SQL block: " + e.getMessage());
            errorResult.put("error", e.getMessage());
            errorResult.put("output", e.getMessage());
            errorResult.put("rows", new ArrayList<>());
            errorResult.put("rowCount", 0);

            return errorResult;
        }
    }

    /**
     * Wrap the user's PL/SQL block to capture DBMS_OUTPUT properly
     * This wrapper ensures we capture all output regardless of how the block is structured
     */
    private String wrapPlSqlBlockWithCapture(String originalQuery) {
        // Clean the query first - remove trailing semicolons
        String cleanedQuery = cleanSqlStatement(originalQuery);

        // Remove any trailing semicolons and clean up
        if (cleanedQuery.endsWith(";")) {
            cleanedQuery = cleanedQuery.substring(0, cleanedQuery.length() - 1);
        }

        log.debug("Wrapping PL/SQL block - Original: [{}], Cleaned: [{}]", originalQuery, cleanedQuery);

        // Check if it's already a complete block
        boolean isCompleteBlock = cleanedQuery.toUpperCase().startsWith("BEGIN") ||
                cleanedQuery.toUpperCase().startsWith("DECLARE");

        // Use a larger buffer and better output collection
        if (isCompleteBlock) {
            // Already has BEGIN/DECLARE, wrap it with better output capture
            return "DECLARE\n" +
                    "    TYPE output_array IS TABLE OF VARCHAR2(32767) INDEX BY BINARY_INTEGER;\n" +
                    "    v_output_lines output_array;\n" +
                    "    v_line_count NUMBER;\n" +
                    "    v_idx NUMBER := 1;\n" +
                    "    v_line VARCHAR2(32767);\n" +
                    "    v_status NUMBER;\n" +
                    "BEGIN\n" +
                    "    -- Enable DBMS_OUTPUT with maximum buffer\n" +
                    "    DBMS_OUTPUT.ENABLE(1000000);\n" +
                    "    \n" +
                    "    -- Execute the user's block\n" +
                    "    " + cleanedQuery + ";\n" +
                    "    \n" +
                    "    -- Collect all DBMS_OUTPUT lines\n" +
                    "    LOOP\n" +
                    "        DBMS_OUTPUT.GET_LINE(v_line, v_status);\n" +
                    "        EXIT WHEN v_status != 0;\n" +
                    "        v_output_lines(v_idx) := v_line;\n" +
                    "        v_idx := v_idx + 1;\n" +
                    "    END LOOP;\n" +
                    "    v_line_count := v_idx - 1;\n" +
                    "    \n" +
                    "    -- Output collected lines\n" +
                    "    FOR i IN 1..v_line_count LOOP\n" +
                    "        DBMS_OUTPUT.PUT_LINE(v_output_lines(i));\n" +
                    "    END LOOP;\n" +
                    "EXCEPTION\n" +
                    "    WHEN OTHERS THEN\n" +
                    "        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);\n" +
                    "        DBMS_OUTPUT.PUT_LINE('Error Code: ' || SQLCODE);\n" +
                    "        RAISE;\n" +
                    "END;";
        } else {
            // It's a simple statement or procedure call, wrap it
            return "DECLARE\n" +
                    "    TYPE output_array IS TABLE OF VARCHAR2(32767) INDEX BY BINARY_INTEGER;\n" +
                    "    v_output_lines output_array;\n" +
                    "    v_line_count NUMBER;\n" +
                    "    v_idx NUMBER := 1;\n" +
                    "    v_line VARCHAR2(32767);\n" +
                    "    v_status NUMBER;\n" +
                    "BEGIN\n" +
                    "    DBMS_OUTPUT.ENABLE(1000000);\n" +
                    "    " + cleanedQuery + ";\n" +
                    "    \n" +
                    "    LOOP\n" +
                    "        DBMS_OUTPUT.GET_LINE(v_line, v_status);\n" +
                    "        EXIT WHEN v_status != 0;\n" +
                    "        v_output_lines(v_idx) := v_line;\n" +
                    "        v_idx := v_idx + 1;\n" +
                    "    END LOOP;\n" +
                    "    v_line_count := v_idx - 1;\n" +
                    "    \n" +
                    "    FOR i IN 1..v_line_count LOOP\n" +
                    "        DBMS_OUTPUT.PUT_LINE(v_output_lines(i));\n" +
                    "    END LOOP;\n" +
                    "EXCEPTION\n" +
                    "    WHEN OTHERS THEN\n" +
                    "        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);\n" +
                    "        DBMS_OUTPUT.PUT_LINE('Error Code: ' || SQLCODE);\n" +
                    "        RAISE;\n" +
                    "END;";
        }
    }

    /**
     * Parse procedure output to extract response code, message, and batch number
     * This works with any procedure that outputs formatted messages
     */
    private Map<String, String> parseProcedureOutput(String output) {
        Map<String, String> parsed = new HashMap<>();

        if (output == null || output.isEmpty()) {
            return parsed;
        }

        String[] lines = output.split("\n");
        for (String line : lines) {
            // Match patterns like "Response Code: 000" or "Response Code: 000"
            if (line.matches(".*[Rr]esponse\\s+[Cc]ode\\s*:?\\s*\\w+.*")) {
                String code = extractValue(line, "[Rr]esponse\\s+[Cc]ode");
                if (code != null) {
                    parsed.put("responseCode", code.trim());
                }
            }

            // Match patterns like "Message: Transaction posted successfully"
            if (line.matches(".*[Mm]essage\\s*:?\\s*.+")) {
                String message = extractValue(line, "[Mm]essage");
                if (message != null) {
                    parsed.put("message", message.trim());
                }
            }

            // Match patterns like "Batch Number: BATCH123"
            if (line.matches(".*[Bb]atch\\s+[Nn]umber\\s*:?\\s*\\w+.*")) {
                String batchNo = extractValue(line, "[Bb]atch\\s+[Nn]umber");
                if (batchNo != null) {
                    parsed.put("batchNumber", batchNo.trim());
                }
            }

            // Match patterns like "Batch No: BATCH123"
            if (line.matches(".*[Bb]atch\\s+[Nn]o\\s*:?\\s*\\w+.*")) {
                String batchNo = extractValue(line, "[Bb]atch\\s+[Nn]o");
                if (batchNo != null) {
                    parsed.put("batchNumber", batchNo.trim());
                }
            }

            // Match patterns like "Transaction posted successfully with a batch reference number: BATCH123"
            if (line.matches(".*batch reference number:.*")) {
                String[] parts = line.split("batch reference number:");
                if (parts.length > 1) {
                    String batchNo = parts[1].trim();
                    if (!batchNo.isEmpty()) {
                        parsed.put("batchNumber", batchNo);
                    }
                }
            }

            // If line contains "successfully" and no message found yet, use it as message
            if (!parsed.containsKey("message") &&
                    (line.toLowerCase().contains("successfully") ||
                            line.toLowerCase().contains("completed"))) {
                parsed.put("message", line.trim());
            }

            // Capture error messages
            if (line.toLowerCase().contains("error") ||
                    line.toUpperCase().contains("ORA-") ||
                    line.toLowerCase().contains("exception")) {
                if (!parsed.containsKey("error")) {
                    parsed.put("error", line.trim());
                }
            }
        }

        // If we have a message that contains both code and message, try to parse it
        String message = parsed.get("message");
        if (message != null && message.contains("Response Code:")) {
            // Message contains embedded response code, extract it
            Pattern pattern = Pattern.compile("Response Code:\\s*(\\d+)");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                parsed.put("responseCode", matcher.group(1));
                // Clean the message by removing the response code part
                String cleanMessage = message.replaceAll("Response Code:\\s*\\d+", "").trim();
                if (!cleanMessage.isEmpty()) {
                    parsed.put("message", cleanMessage);
                }
            }
        }

        return parsed;
    }

    /**
     * Helper to extract value after a label
     */
    private String extractValue(String line, String labelPattern) {
        try {
            Pattern pattern = Pattern.compile(labelPattern + "\\s*:?\\s*(.*)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            // Fallback to simple split
            String[] parts = line.split(":");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }
        return null;
    }

    /**
     * Execute CALL statement (procedure/function call)
     */
    private Map<String, Object> executeCall(String query, int timeoutSeconds) {
        return executePLSQLBlock(query, timeoutSeconds);
    }


    private Map<String, Object> executeSQLStatement(String sql, boolean readOnly, Integer timeoutSeconds) {
        Map<String, Object> result = new HashMap<>();

        try {
            String trimmedSql = sql.trim().toUpperCase();

            if (trimmedSql.startsWith("SELECT")) {
                // Execute query
                List<Map<String, Object>> rows = oracleJdbcTemplate.queryForList(sql);

                result.put("success", true);
                result.put("message", "Query executed successfully");
                result.put("rows", rows);
                result.put("rowCount", rows.size());
                result.put("columns", rows.isEmpty() ? new ArrayList<>() : new ArrayList<>(rows.get(0).keySet()));
            } else {
                // Execute update (INSERT, UPDATE, DELETE)
                int affectedRows = oracleJdbcTemplate.update(sql);

                result.put("success", true);
                result.put("message", "Update executed successfully");
                result.put("rowsAffected", affectedRows);
                result.put("output", affectedRows + " row(s) affected");
            }

            return result;
        } catch (Exception e) {
            log.error("Error executing SQL statement", e);
            result.put("success", false);
            result.put("message", "SQL execution failed: " + e.getMessage());
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * Enhanced SQL statement type detection
     */
    private OracleSqlStatementTypeEnum detectSqlStatementType(String originalQuery, String upperQuery) {
        // Remove leading/trailing whitespace and comments
        String cleanedQuery = upperQuery.replaceAll("/\\*.*?\\*/", "").trim();

        // Check for PL/SQL blocks first
        if (cleanedQuery.startsWith("BEGIN") || cleanedQuery.startsWith("DECLARE")) {
            return OracleSqlStatementTypeEnum.ANONYMOUS_BLOCK;
        }

        // Check for CREATE statements
        if (cleanedQuery.startsWith("CREATE")) {
            if (cleanedQuery.startsWith("CREATE OR REPLACE")) {
                cleanedQuery = cleanedQuery.substring("CREATE OR REPLACE".length()).trim();
            } else {
                cleanedQuery = cleanedQuery.substring("CREATE".length()).trim();
            }

            if (cleanedQuery.startsWith("PROCEDURE")) return OracleSqlStatementTypeEnum.PROCEDURE;
            if (cleanedQuery.startsWith("FUNCTION")) return OracleSqlStatementTypeEnum.FUNCTION;
            if (cleanedQuery.startsWith("PACKAGE")) return OracleSqlStatementTypeEnum.PACKAGE;
            if (cleanedQuery.startsWith("VIEW")) return OracleSqlStatementTypeEnum.CREATE_VIEW;
            if (cleanedQuery.startsWith("TABLE")) return OracleSqlStatementTypeEnum.CREATE_TABLE;
            if (cleanedQuery.startsWith("INDEX")) return OracleSqlStatementTypeEnum.CREATE_INDEX;
            if (cleanedQuery.startsWith("TRIGGER")) return OracleSqlStatementTypeEnum.CREATE_TRIGGER;
            if (cleanedQuery.startsWith("SEQUENCE")) return OracleSqlStatementTypeEnum.CREATE_SEQUENCE;
            if (cleanedQuery.startsWith("SYNONYM")) return OracleSqlStatementTypeEnum.CREATE_SYNONYM;
            if (cleanedQuery.startsWith("TYPE")) return OracleSqlStatementTypeEnum.CREATE_TYPE;
            return OracleSqlStatementTypeEnum.DDL;
        }

        // Check for ALTER, DROP, TRUNCATE
        if (cleanedQuery.startsWith("ALTER") ||
                cleanedQuery.startsWith("DROP") ||
                cleanedQuery.startsWith("TRUNCATE") ||
                cleanedQuery.startsWith("RENAME")) {
            return OracleSqlStatementTypeEnum.DDL;
        }

        // Check for DML statements
        if (cleanedQuery.startsWith("INSERT")) return OracleSqlStatementTypeEnum.INSERT;
        if (cleanedQuery.startsWith("UPDATE")) return OracleSqlStatementTypeEnum.UPDATE;
        if (cleanedQuery.startsWith("DELETE")) return OracleSqlStatementTypeEnum.DELETE;
        if (cleanedQuery.startsWith("MERGE")) return OracleSqlStatementTypeEnum.MERGE;

        // Check for SELECT statements and variations
        if (cleanedQuery.startsWith("SELECT")) {
            // Check if it's a view definition
            if (cleanedQuery.contains("CREATE VIEW")) {
                return OracleSqlStatementTypeEnum.CREATE_VIEW;
            }
            return OracleSqlStatementTypeEnum.SELECT;
        }

        // Check for WITH clause (Common Table Expression)
        if (cleanedQuery.startsWith("WITH")) {
            return OracleSqlStatementTypeEnum.WITH;
        }

        // Check for EXPLAIN PLAN
        if (cleanedQuery.startsWith("EXPLAIN")) {
            return OracleSqlStatementTypeEnum.EXPLAIN_PLAN;
        }

        // Check for CALL statement (Java stored procedures)
        if (cleanedQuery.startsWith("CALL")) {
            return OracleSqlStatementTypeEnum.CALL;
        }

        // Check for EXECUTE/EXEC statement
        if (cleanedQuery.startsWith("EXEC") || cleanedQuery.startsWith("EXECUTE")) {
            return OracleSqlStatementTypeEnum.EXECUTE;
        }

        // Check for procedure/function definitions (without CREATE keyword)
        if (cleanedQuery.contains("PROCEDURE") && cleanedQuery.contains("IS") && cleanedQuery.contains("BEGIN")) {
            return OracleSqlStatementTypeEnum.PROCEDURE;
        }
        if (cleanedQuery.contains("FUNCTION") && cleanedQuery.contains("RETURN") && cleanedQuery.contains("IS")) {
            return OracleSqlStatementTypeEnum.FUNCTION;
        }
        if (cleanedQuery.contains("PACKAGE") && (cleanedQuery.contains("IS") || cleanedQuery.contains("AS"))) {
            return OracleSqlStatementTypeEnum.PACKAGE;
        }

        // Check for view queries (SELECT statements that reference views)
        if (cleanedQuery.contains("FROM") && cleanedQuery.matches(".*\\bVIEW\\b.*")) {
            return OracleSqlStatementTypeEnum.VIEW_QUERY;
        }

        // Check for PL/SQL blocks without BEGIN/DECLARE (implicit blocks)
        if (cleanedQuery.contains("IS") && cleanedQuery.contains("BEGIN") && cleanedQuery.contains("END")) {
            return OracleSqlStatementTypeEnum.PLSQL_BLOCK;
        }

        // Default - treat as regular SQL
        return OracleSqlStatementTypeEnum.UNKNOWN;
    }

    /**
     * Check if statement type is allowed in read-only mode
     */
    private boolean isReadOnlyAllowed(OracleSqlStatementTypeEnum statementType) {
        switch (statementType) {
            case SELECT:
            case WITH:
            case EXPLAIN_PLAN:
            case VIEW_QUERY:
            case CALL: // Some CALL statements might be read-only
                return true;
            default:
                return false;
        }
    }

    /**
     * Get appropriate success message based on statement type
     */
    private String getSuccessMessage(OracleSqlStatementTypeEnum statementType, Map<String, Object> results) {
        switch (statementType) {
            case SELECT:
            case WITH:
            case VIEW_QUERY:
                int rowCount = (int) results.getOrDefault("rowCount", 0);
                return String.format("%s executed successfully, %d rows returned", statementType, rowCount);

            case INSERT:
            case UPDATE:
            case DELETE:
            case MERGE:
                int affected = (int) results.getOrDefault("rowsAffected", 0);
                return String.format("%s executed successfully, %d rows affected", statementType, affected);

            case PROCEDURE:
            case FUNCTION:
            case PACKAGE:
                return String.format("%s compiled/executed successfully", statementType);

            case PLSQL_BLOCK:
            case ANONYMOUS_BLOCK:
                return "PL/SQL block executed successfully";

            case CREATE_VIEW:
            case CREATE_TABLE:
            case CREATE_INDEX:
            case CREATE_TRIGGER:
            case CREATE_SEQUENCE:
            case CREATE_SYNONYM:
            case CREATE_TYPE:
            case DDL:
                return "DDL statement executed successfully";

            default:
                return "Statement executed successfully";
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
            Map<String, Object> paginatedData = oracleTableRepository.getTablesPaginated(page, pageSize);

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
            Map<String, Object> paginatedData = oracleViewRepository.getViewsPaginated(page, pageSize);

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
            Map<String, Object> paginatedData = oracleProcedureRepository.getProceduresPaginated(page, pageSize);

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
            Map<String, Object> paginatedData = oracleFunctionRepository.getFunctionsPaginated(page, pageSize);

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
            Map<String, Object> paginatedData = oraclePackageRepository.getPackagesPaginated(page, pageSize);

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
            Map<String, Object> paginatedData = oracleOtherObjectsRepository.getSynonymsPaginated(page, pageSize);

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
            Map<String, Object> paginatedData = oracleOtherObjectsRepository.getSequencesPaginated(page, pageSize);

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
            Map<String, Object> paginatedData = oracleOtherObjectsRepository.getTypesPaginated(page, pageSize);

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
            Map<String, Object> paginatedData = oracleOtherObjectsRepository.getTriggersPaginated(page, pageSize);

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
            Map<String, Object> counts = oracleRepository.getAllObjectCounts();

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
            Map<String, Object> resolved = oracleOtherObjectsRepository.getSynonymTargetDetails(synonymName);

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
            Map<String, Object> diagnostics = oracleRepository.diagnoseDatabase();

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
            String currentUser = oracleRepository.getCurrentUser();
            String currentSchema = oracleRepository.getCurrentSchema();
            String dbVersion = oracleRepository.getDatabaseVersion();

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
            List<Map<String, Object>> tables = oracleTableRepository.getAllTables();

            String currentSchema = oracleRepository.getCurrentSchema();

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
            List<Map<String, Object>> tables = oracleTableRepository.getTablesBySchema(schemaName);

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
            Map<String, Object> tableDetails = oracleTableRepository.getTableDetails(tableName);
            List<Map<String, Object>> columns = oracleTableRepository.getTableColumns(tableName);
            List<Map<String, Object>> constraints = oracleTableRepository.getTableConstraints(tableName);
            List<Map<String, Object>> indexes = oracleTableRepository.getTableIndexes(tableName);

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
            List<Map<String, Object>> tables = oracleTableRepository.searchTables(searchPattern);

            String currentSchema = oracleRepository.getCurrentSchema();

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
            Map<String, Object> statistics = oracleTableRepository.getTableStatistics(tableName);

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
            List<Map<String, Object>> tables = oracleTableRepository.getTablesWithRowCount();

            String currentSchema = oracleRepository.getCurrentSchema();

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
            List<Map<String, Object>> tablespaceStats = oracleTableRepository.getTableCountByTablespace();

            String currentSchema = oracleRepository.getCurrentSchema();

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
            List<Map<String, Object>> tables = oracleTableRepository.getRecentTables(days);

            String currentSchema = oracleRepository.getCurrentSchema();

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
            List<Map<String, Object>> views = oracleViewRepository.getAllViews();

            String currentSchema = oracleRepository.getCurrentSchema();

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
            List<Map<String, Object>> views = oracleViewRepository.getViewsBySchema(schemaName);

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
            Map<String, Object> viewDetails = oracleViewRepository.getViewDetails(viewName);

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
            List<Map<String, Object>> procedures = oracleProcedureRepository.getAllProcedures();

            String currentSchema = oracleRepository.getCurrentSchema();

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
            List<Map<String, Object>> procedures = oracleProcedureRepository.getProceduresBySchema(schemaName);

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
            Map<String, Object> procedureDetails = oracleProcedureRepository.getProcedureDetails(procedureName);

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
            List<Map<String, Object>> functions = oracleFunctionRepository.getAllFunctions();

            String currentSchema = oracleRepository.getCurrentSchema();

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
            List<Map<String, Object>> functions = oracleFunctionRepository.getFunctionsBySchema(schemaName);

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
            Map<String, Object> functionDetails = oracleFunctionRepository.getFunctionDetails(functionName);

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
            List<Map<String, Object>> packages = oraclePackageRepository.getAllPackages();

            String currentSchema = oracleRepository.getCurrentSchema();

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
            List<Map<String, Object>> packages = oraclePackageRepository.getPackagesBySchema(schemaName);

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
            Map<String, Object> packageDetails = oraclePackageRepository.getPackageDetails(packageName);

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
            List<Map<String, Object>> triggers = oracleOtherObjectsRepository.getAllTriggers();

            String currentSchema = oracleRepository.getCurrentSchema();

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
            List<Map<String, Object>> triggers = oracleOtherObjectsRepository.getTriggersBySchema(schemaName);

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
            Map<String, Object> triggerDetails = oracleOtherObjectsRepository.getTriggerDetails(triggerName);

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
            List<Map<String, Object>> synonyms = oracleOtherObjectsRepository.getAllSynonyms();

            String currentSchema = oracleRepository.getCurrentSchema();

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
            List<Map<String, Object>> synonyms = oracleOtherObjectsRepository.getSynonymsBySchema(schemaName);

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
            List<Map<String, Object>> sequences = oracleOtherObjectsRepository.getAllSequences();

            String currentSchema = oracleRepository.getCurrentSchema();

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
            List<Map<String, Object>> sequences = oracleOtherObjectsRepository.getSequencesBySchema(schemaName);

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
            Map<String, Object> sequenceDetails = oracleOtherObjectsRepository.getSequenceDetails(sequenceName);

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
            List<Map<String, Object>> types = oracleOtherObjectsRepository.getAllTypes();

            String currentSchema = oracleRepository.getCurrentSchema();

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
            List<Map<String, Object>> types = oracleOtherObjectsRepository.getTypesBySchema(schemaName);

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
            Map<String, Object> typeDetails = oracleOtherObjectsRepository.getTypeDetails(typeName);

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
            List<Map<String, Object>> dbLinks = oracleOtherObjectsRepository.getAllDbLinks();

            String currentSchema = oracleRepository.getCurrentSchema();

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
            List<Map<String, Object>> dbLinks = oracleOtherObjectsRepository.getDbLinksBySchema(schemaName);

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
            List<Map<String, Object>> objects = oracleObjectRepository.getAllObjects();

            String currentSchema = oracleRepository.getCurrentSchema();

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
            List<Map<String, Object>> objects = oracleObjectRepository.getObjectsBySchema(schemaName);

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
            List<Map<String, Object>> objects = oracleSearchRepository.searchObjectsForFrontend(searchPattern, "ALL", 1000);

            String currentSchema = oracleRepository.getCurrentSchema();

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
            List<Map<String, Object>> objectCounts = oracleObjectRepository.getObjectCountByType();

            String currentSchema = oracleRepository.getCurrentSchema();

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
            List<Map<String, Object>> invalidObjects = oracleRepository.getInvalidObjects();

            String currentSchema = oracleRepository.getCurrentSchema();

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
            List<Map<String, Object>> objects = oracleObjectRepository.getObjectsByStatus(status);

            String currentSchema = oracleRepository.getCurrentSchema();

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
            Map<String, Object> searchResults = oracleSearchRepository.searchCombinedTypes(
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
            Map<String, Object> searchResults = oracleSearchRepository.searchProceduresWithSynonyms(
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
            return oracleSearchRepository.getSearchCount(query, typeArray);
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
            return oracleRepository.getCurrentUser();
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
            return oracleRepository.getCurrentSchema();
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
            Map<String, Object> synonymInfo = oracleRepository.checkIfSynonymAndGetTarget(
                    tableName, "TABLE");

            if ((boolean) synonymInfo.getOrDefault("isSynonym", false) &&
                    !(boolean) synonymInfo.getOrDefault("isRemote", false)) {
                resolvedOwner = (String) synonymInfo.get("targetOwner");
                tableName = (String) synonymInfo.get("targetName");
            }

            List<Map<String, Object>> constraints = oracleTableRepository.getTableConstraints(
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
            Map<String, Object> synonymInfo = oracleRepository.checkIfSynonymAndGetTarget(
                    objectName, objectType);

            Map<String, Object> ddlResult;
            if ((boolean) synonymInfo.getOrDefault("isSynonym", false) &&
                    !(boolean) synonymInfo.getOrDefault("isRemote", false)) {
                // Get DDL of target object
                ddlResult = oracleDDLRepository.getObjectDDLForFrontend(
                        (String) synonymInfo.get("targetName"),
                        (String) synonymInfo.get("targetType"));
            } else {
                ddlResult = oracleDDLRepository.getObjectDDLForFrontend(objectName, objectType);
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
        return oracleRepository.getCurrentUser();
    }

    private Map<String, Object> checkIfSynonym(String objectName) {
        return oracleRepository.checkIfSynonymAndGetTarget(objectName, null);
    }

    public ResponseEntity<Map<String, Object>> createErrorResponse(String requestId, String message, int statusCode) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("responseCode", statusCode);
        errorResponse.put("message", message);
        errorResponse.put("requestId", requestId);
        errorResponse.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(statusCode).body(errorResponse);
    }



    @Override
    public boolean objectExists(String owner, String objectName, String objectType) {
        try {
            String sql = "SELECT COUNT(*) FROM ALL_OBJECTS WHERE " +
                    "UPPER(OWNER) = UPPER(?) AND " +
                    "UPPER(OBJECT_NAME) = UPPER(?) AND " +
                    "UPPER(OBJECT_TYPE) = UPPER(?)";
            Integer count = oracleJdbcTemplate.queryForObject(sql, Integer.class, owner, objectName, objectType);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Error checking object existence: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> getSourceObjectDetails(ApiSourceObjectDTO sourceObject) {
        // Delegate to your existing method
        return getSourceObjectDetails(this, sourceObject);
    }

    // Your existing method
    public Map<String, Object> getSourceObjectDetails(OracleSchemaService oracleSchemaService, ApiSourceObjectDTO sourceObject) {
        // Your existing implementation...
        Map<String, Object> details = new HashMap<>();
        // ... rest of your code ...
        return details;
    }

    @Override
    public DatabaseTypeEnum getDatabaseType() {
        return DatabaseTypeEnum.ORACLE;
    }

    @Override
    public String getDatabaseVersion() {
        try {
            return oracleJdbcTemplate.queryForObject("SELECT VERSION FROM PRODUCT_COMPONENT_VERSION WHERE ROWNUM = 1", String.class);
        } catch (Exception e) {
            return "Oracle Database";
        }
    }

    @Override
    public boolean isConnected() {
        try {
            oracleJdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getObjectDDL(String objectName, String objectType, String owner) {
        try {
            // Get DDL using DBMS_METADATA
            String sql = "SELECT DBMS_METADATA.GET_DDL(?, ?) FROM DUAL";
            return oracleJdbcTemplate.queryForObject(sql, String.class, objectType, objectName);
        } catch (Exception e) {
            log.error("Error getting DDL: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, Object> getObjectStatistics(String objectName, String objectType, String owner) {
        try {
            // Get statistics from USER_TAB_STATISTICS or ALL_TAB_STATISTICS
            String sql = "SELECT NUM_ROWS, BLOCKS, EMPTY_BLOCKS, AVG_SPACE, " +
                    "CHAIN_CNT, AVG_ROW_LEN, LAST_ANALYZED " +
                    "FROM ALL_TAB_STATISTICS WHERE OWNER = ? AND TABLE_NAME = ?";
            Map<String, Object> stats = oracleJdbcTemplate.queryForMap(sql, owner, objectName);
            return stats;
        } catch (Exception e) {
            log.error("Error getting statistics: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}