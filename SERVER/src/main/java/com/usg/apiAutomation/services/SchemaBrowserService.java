package com.usg.apiAutomation.services;

import com.usg.apiAutomation.dtos.schemaBrowser.*;
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
public class SchemaBrowserService {

    private final LoggerUtil loggerUtil;

    // Cache for schema browser data
    private final Map<String, SchemaCache> schemaCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 2 * 60 * 1000; // 2 minutes cache TTL

    // Sample database connections (would be replaced with real data)
    private static final String[] DATABASE_TYPES = {"oracle", "postgresql", "mysql", "mongodb", "redis"};
    private static final String[] SCHEMA_OWNERS = {"HR", "SCOTT", "SYS", "SYSTEM", "APP_USER", "ADMIN"};
    private static final String[] COLUMN_TYPES = {"NUMBER", "VARCHAR2", "DATE", "TIMESTAMP", "CLOB", "BLOB", "RAW", "CHAR"};
    private static final String[] CONSTRAINT_TYPES = {"PRIMARY KEY", "FOREIGN KEY", "UNIQUE", "CHECK", "NOT NULL"};
    private static final String[] OBJECT_STATUSES = {"VALID", "INVALID", "COMPILED", "ERROR"};

    @PostConstruct
    public void init() {
        log.info("SchemaBrowserService initialized");
        preloadSchemaCache();
    }

    // ========== PUBLIC SERVICE METHODS ==========

    public SchemaConnectionsResponse getSchemaConnections(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("RequestEntity ID: {}, Getting schema connections for user: {}", requestId, performedBy);
            loggerUtil.log("schemaBrowser",
                    "RequestEntity ID: " + requestId + ", Getting schema connections for user: " + performedBy);

            // Check cache first
            String cacheKey = "schema_connections_" + performedBy;
            SchemaCache cachedData = schemaCache.get(cacheKey);

            if (cachedData != null && !isCacheExpired(cachedData)) {
                log.debug("RequestEntity ID: {}, Returning cached schema connections", requestId);
                return (SchemaConnectionsResponse) cachedData.getData();
            }

            SchemaConnectionsResponse connections = generateSchemaConnections();

            // Update cache
            schemaCache.put(cacheKey, new SchemaCache(connections, System.currentTimeMillis()));

            log.info("RequestEntity ID: {}, Retrieved {} schema connections", requestId, connections.getConnections().size());

            return connections;

        } catch (Exception e) {
            String errorMsg = "Error retrieving schema connections: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
            return getFallbackSchemaConnections();
        }
    }

    public SchemaObjectsResponse getSchemaObjects(String requestId, HttpServletRequest req, String performedBy,
                                                  String connectionId, String objectType, String filter) {
        try {
            log.info("RequestEntity ID: {}, Getting schema objects for connection: {}, type: {}, filter: {}",
                    requestId, connectionId, objectType, filter);

            // Check cache first
            String cacheKey = "schema_objects_" + performedBy + "_" + connectionId + "_" + objectType + "_" +
                    (filter != null ? filter.hashCode() : "all");
            SchemaCache cachedData = schemaCache.get(cacheKey);

            if (cachedData != null && !isCacheExpired(cachedData)) {
                log.debug("RequestEntity ID: {}, Returning cached schema objects", requestId);
                return (SchemaObjectsResponse) cachedData.getData();
            }

            SchemaObjectsResponse objects = generateSchemaObjects(connectionId, objectType, filter);

            // Update cache
            schemaCache.put(cacheKey, new SchemaCache(objects, System.currentTimeMillis()));

            log.info("RequestEntity ID: {}, Retrieved {} schema objects of type {}",
                    requestId, objects.getObjects().size(), objectType);

            return objects;

        } catch (Exception e) {
            String errorMsg = "Error retrieving schema objects: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
            return new SchemaObjectsResponse(Collections.emptyList(), objectType, 0);
        }
    }

    public ObjectDetailsResponse getObjectDetails(String requestId, HttpServletRequest req, String performedBy,
                                                  String connectionId, String objectType, String objectName) {
        try {
            log.info("RequestEntity ID: {}, Getting object details for: {}.{}",
                    requestId, objectType, objectName);

            // Check cache first
            String cacheKey = "object_details_" + performedBy + "_" + connectionId + "_" +
                    objectType + "_" + objectName;
            SchemaCache cachedData = schemaCache.get(cacheKey);

            if (cachedData != null && !isCacheExpired(cachedData)) {
                log.debug("RequestEntity ID: {}, Returning cached object details", requestId);
                return (ObjectDetailsResponse) cachedData.getData();
            }

            ObjectDetailsResponse details = generateObjectDetails(connectionId, objectType, objectName);

            // Update cache
            schemaCache.put(cacheKey, new SchemaCache(details, System.currentTimeMillis()));

            log.info("RequestEntity ID: {}, Retrieved details for {}.{}", requestId, objectType, objectName);

            return details;

        } catch (Exception e) {
            String errorMsg = "Error retrieving object details: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
            return getFallbackObjectDetails(objectType, objectName);
        }
    }

    public TableDataResponse getTableData(String requestId, HttpServletRequest req, String performedBy,
                                          String connectionId, String tableName,
                                          int page, int pageSize, String sortColumn, String sortDirection) {
        try {
            log.info("RequestEntity ID: {}, Getting table data for: {}, page: {}, size: {}",
                    requestId, tableName, page, pageSize);

            // Check cache first (with pagination)
            String cacheKey = "table_data_" + performedBy + "_" + connectionId + "_" +
                    tableName + "_" + page + "_" + pageSize;
            SchemaCache cachedData = schemaCache.get(cacheKey);

            if (cachedData != null && !isCacheExpired(cachedData)) {
                log.debug("RequestEntity ID: {}, Returning cached table data", requestId);
                return (TableDataResponse) cachedData.getData();
            }

            TableDataResponse tableData = generateTableData(tableName, page, pageSize, sortColumn, sortDirection);

            // Update cache
            schemaCache.put(cacheKey, new SchemaCache(tableData, System.currentTimeMillis()));

            log.info("RequestEntity ID: {}, Retrieved {} rows from table {}",
                    requestId, tableData.getData().size(), tableName);

            return tableData;

        } catch (Exception e) {
            String errorMsg = "Error retrieving table data: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
            return new TableDataResponse(Collections.emptyList(), 0, page, pageSize, 0);
        }
    }

    public DDLResponse getObjectDDL(String requestId, HttpServletRequest req, String performedBy,
                                    String connectionId, String objectType, String objectName) {
        try {
            log.info("RequestEntity ID: {}, Getting DDL for: {}.{}", requestId, objectType, objectName);

            DDLResponse ddl = generateObjectDDL(objectType, objectName);

            log.info("RequestEntity ID: {}, Retrieved DDL for {}.{}", requestId, objectType, objectName);

            return ddl;

        } catch (Exception e) {
            String errorMsg = "Error retrieving object DDL: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
            return new DDLResponse("", objectType, objectName, "Error generating DDL: " + e.getMessage());
        }
    }

    public SearchResponse searchSchema(String requestId, HttpServletRequest req, String performedBy,
                                       String connectionId, String searchQuery,
                                       String searchType, int maxResults) {
        try {
            log.info("RequestEntity ID: {}, Searching schema with query: {}, type: {}",
                    requestId, searchQuery, searchType);

            SearchResponse searchResults = performSchemaSearch(searchQuery, searchType, maxResults);

            log.info("RequestEntity ID: {}, Found {} search results for query: {}",
                    requestId, searchResults.getResults().size(), searchQuery);

            return searchResults;

        } catch (Exception e) {
            String errorMsg = "Error searching schema: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
            return new SearchResponse(Collections.emptyList(), searchQuery, 0);
        }
    }

    public ExecuteQueryResponse executeQuery(String requestId, HttpServletRequest req, String performedBy,
                                             String connectionId, String query,
                                             int timeoutSeconds, boolean readOnly) {
        try {
            log.info("RequestEntity ID: {}, Executing query for user: {}", requestId, performedBy);

            ExecuteQueryResponse queryResult = executeSampleQuery(query, timeoutSeconds, readOnly);

            log.info("RequestEntity ID: {}, Query executed successfully, returned {} rows",
                    requestId, queryResult.getRowCount());

            return queryResult;

        } catch (Exception e) {
            String errorMsg = "Error executing query: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
            return new ExecuteQueryResponse(Collections.emptyList(), query, 0, "Error: " + e.getMessage());
        }
    }

    public GenerateAPIResponse generateAPIFromObject(String requestId, HttpServletRequest req, String performedBy,
                                                     String connectionId, String objectType,
                                                     String objectName, String apiType,
                                                     Map<String, String> options) {
        try {
            log.info("RequestEntity ID: {}, Generating API for: {}.{}, type: {}",
                    requestId, objectType, objectName, apiType);

            GenerateAPIResponse apiResponse = generateSampleAPI(objectType, objectName, apiType, options);

            log.info("RequestEntity ID: {}, Generated API for {}.{} with {} endpoints",
                    requestId, objectType, objectName, apiResponse.getEndpoints().size());

            return apiResponse;

        } catch (Exception e) {
            String errorMsg = "Error generating API: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
            return new GenerateAPIResponse(Collections.emptyList(), objectType, objectName,
                    "Error generating API: " + e.getMessage());
        }
    }

    public void clearSchemaCache(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("RequestEntity ID: {}, Clearing schema cache for user: {}", requestId, performedBy);

            int beforeSize = schemaCache.size();
            schemaCache.clear();
            int afterSize = schemaCache.size();

            log.info("RequestEntity ID: {}, Cleared {} schema cache entries", requestId, beforeSize - afterSize);
            loggerUtil.log("schemaBrowser",
                    "RequestEntity ID: " + requestId + ", Cleared schema cache for user: " + performedBy);

        } catch (Exception e) {
            String errorMsg = "Error clearing schema cache: " + e.getMessage();
            log.error("RequestEntity ID: {}, {}", requestId, errorMsg);
        }
    }

    // ========== PRIVATE HELPER METHODS ==========

    private void preloadSchemaCache() {
        try {
            log.info("Preloading schema cache with sample data");

            // Preload connections
            SchemaConnectionsResponse connections = generateSchemaConnections();
            schemaCache.put("schema_connections_admin", new SchemaCache(connections, System.currentTimeMillis()));

            // Preload common objects
            SchemaObjectsResponse tables = generateSchemaObjects("conn-1", "TABLE", null);
            schemaCache.put("schema_objects_admin_conn-1_TABLE_all",
                    new SchemaCache(tables, System.currentTimeMillis()));

            log.info("Schema cache preloaded with {} entries", schemaCache.size());

        } catch (Exception e) {
            log.warn("Failed to preload schema cache: {}", e.getMessage());
        }
    }

    private boolean isCacheExpired(SchemaCache cache) {
        return (System.currentTimeMillis() - cache.getTimestamp()) > CACHE_TTL_MS;
    }

    private SchemaConnectionsResponse generateSchemaConnections() {
        List<ConnectionDto> connections = new ArrayList<>();

        // Generate sample connections
        for (int i = 1; i <= 5; i++) {
            ConnectionDto connection = new ConnectionDto();
            connection.setId("conn-" + i);
            connection.setName(getConnectionName(i));
            connection.setDescription(getConnectionDescription(i));
            connection.setHost(getConnectionHost(i));
            connection.setPort(getConnectionPort(i));
            connection.setService(getConnectionService(i));
            connection.setUsername(getConnectionUsername(i));
            connection.setStatus(i <= 3 ? "connected" : "disconnected");
            connection.setType(DATABASE_TYPES[(i - 1) % DATABASE_TYPES.length]);
            connection.setColor(i <= 3 ? "green" : "red");
            connection.setLastUsed(LocalDateTime.now().minusHours(i).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            connection.setDriver(getConnectionDriver(i));
            connection.setVersion(getConnectionVersion(i));
            connection.setMaxConnections(50);
            connection.setCurrentConnections((int) (Math.random() * 20 + 5));
            connection.setDatabaseSize(String.format("%.1f GB", Math.random() * 10 + 1));
            connection.setTablespaceUsed(String.format("%.0f%%", Math.random() * 40 + 30));
            connection.setSid(i == 1 ? "ORCL" : i == 2 ? "XE" : "PROD");
            connection.setServiceName(i == 1 ? "ORCL" : i == 2 ? "XE" : "PROD_SVC");

            connections.add(connection);
        }

        return new SchemaConnectionsResponse(connections);
    }

    private SchemaObjectsResponse generateSchemaObjects(String connectionId, String objectType, String filter) {
        List<SchemaObjectDto> objects = new ArrayList<>();

        // Generate sample objects based on type
        switch (objectType.toUpperCase()) {
            case "TABLE":
                for (int i = 1; i <= 20; i++) {
                    if (filter == null || getTableName(i).toLowerCase().contains(filter.toLowerCase())) {
                        objects.add(createTableObject(i));
                    }
                }
                break;
            case "VIEW":
                for (int i = 1; i <= 10; i++) {
                    if (filter == null || getViewName(i).toLowerCase().contains(filter.toLowerCase())) {
                        objects.add(createViewObject(i));
                    }
                }
                break;
            case "PROCEDURE":
                for (int i = 1; i <= 8; i++) {
                    if (filter == null || getProcedureName(i).toLowerCase().contains(filter.toLowerCase())) {
                        objects.add(createProcedureObject(i));
                    }
                }
                break;
            case "FUNCTION":
                for (int i = 1; i <= 6; i++) {
                    if (filter == null || getFunctionName(i).toLowerCase().contains(filter.toLowerCase())) {
                        objects.add(createFunctionObject(i));
                    }
                }
                break;
            case "PACKAGE":
                for (int i = 1; i <= 4; i++) {
                    if (filter == null || getPackageName(i).toLowerCase().contains(filter.toLowerCase())) {
                        objects.add(createPackageObject(i));
                    }
                }
                break;
            case "SEQUENCE":
                for (int i = 1; i <= 5; i++) {
                    if (filter == null || getSequenceName(i).toLowerCase().contains(filter.toLowerCase())) {
                        objects.add(createSequenceObject(i));
                    }
                }
                break;
            case "SYNONYM":
                for (int i = 1; i <= 3; i++) {
                    if (filter == null || getSynonymName(i).toLowerCase().contains(filter.toLowerCase())) {
                        objects.add(createSynonymObject(i));
                    }
                }
                break;
            case "TRIGGER":
                for (int i = 1; i <= 7; i++) {
                    if (filter == null || getTriggerName(i).toLowerCase().contains(filter.toLowerCase())) {
                        objects.add(createTriggerObject(i));
                    }
                }
                break;
            case "TYPE":
                for (int i = 1; i <= 3; i++) {
                    if (filter == null || getTypeName(i).toLowerCase().contains(filter.toLowerCase())) {
                        objects.add(createTypeObject(i));
                    }
                }
                break;
            case "INDEX":
                for (int i = 1; i <= 5; i++) {
                    if (filter == null || getIndexName(i).toLowerCase().contains(filter.toLowerCase())) {
                        objects.add(createIndexObject(i));
                    }
                }
                break;
            default:
                // Return all types
                for (int i = 1; i <= 10; i++) {
                    objects.add(createTableObject(i));
                }
                break;
        }

        return new SchemaObjectsResponse(objects, objectType, objects.size());
    }

    private ObjectDetailsResponse generateObjectDetails(String connectionId, String objectType, String objectName) {
        ObjectDetailsResponse details = new ObjectDetailsResponse();
        details.setObjectName(objectName);
        details.setObjectType(objectType);
        details.setOwner(SCHEMA_OWNERS[(int) (Math.random() * SCHEMA_OWNERS.length)]);
        details.setStatus("VALID");
        details.setCreated(LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setLastDDL(LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setTablespace("USERS");
        details.setRowCount((long) (Math.random() * 10000 + 100));
        details.setSize(String.format("%.2f MB", Math.random() * 50 + 1));

        // Add columns for tables
        if (objectType.equalsIgnoreCase("TABLE")) {
            List<ColumnDto> columns = new ArrayList<>();
            for (int i = 1; i <= 8; i++) {
                ColumnDto column = new ColumnDto();
                column.setName(getColumnName(i));
                column.setType(COLUMN_TYPES[(i - 1) % COLUMN_TYPES.length]);
                column.setNullable(i % 3 == 0 ? "Y" : "N");
                column.setPosition(i);
                column.setKey(i == 1 ? "PK" : (i == 4 ? "FK" : null));
                column.setDefaultValue(i == 6 ? "SYSDATE" : null);
                columns.add(column);
            }
            details.setColumns(columns);
        }

        // Add parameters for procedures/functions
        if (objectType.equalsIgnoreCase("PROCEDURE") || objectType.equalsIgnoreCase("FUNCTION")) {
            List<ParameterDto> parameters = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                ParameterDto param = new ParameterDto();
                param.setName("p_param_" + i);
                param.setType(i == 1 ? "IN" : (i == 5 ? "OUT" : "IN OUT"));
                param.setDataType("VARCHAR2");
                param.setPosition(i);
                param.setDefaultValue(i == 3 ? "'DEFAULT'" : null);
                parameters.add(param);
            }
            details.setParameters(parameters);
        }

        // Add constraints for tables
        if (objectType.equalsIgnoreCase("TABLE")) {
            List<ConstraintDto> constraints = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                ConstraintDto constraint = new ConstraintDto();
                constraint.setName("CON_" + objectName.toUpperCase() + "_" + i);
                constraint.setType(CONSTRAINT_TYPES[(i - 1) % CONSTRAINT_TYPES.length]);
                constraint.setColumns(getConstraintColumns(i));
                constraint.setStatus("ENABLED");
                constraints.add(constraint);
            }
            details.setConstraints(constraints);
        }

        details.setComment("Sample " + objectType + " for demonstration purposes");
        details.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return details;
    }

    private TableDataResponse generateTableData(String tableName, int page, int pageSize, String sortColumn, String sortDirection) {
        List<Map<String, Object>> data = new ArrayList<>();
        int totalRows = 150; // Sample total rows

        // Generate sample data for the page
        int startRow = (page - 1) * pageSize;
        int endRow = Math.min(startRow + pageSize, totalRows);

        for (int i = startRow + 1; i <= endRow; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("ID", i);
            row.put("NAME", "Item " + i);
            row.put("DESCRIPTION", "Description for item " + i);
            row.put("CREATED_DATE", LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE));
            row.put("STATUS", i % 3 == 0 ? "ACTIVE" : "INACTIVE");
            row.put("AMOUNT", Math.random() * 1000);
            data.add(row);
        }

        int totalPages = (int) Math.ceil((double) totalRows / pageSize);

        return new TableDataResponse(data, totalRows, page, pageSize, totalPages);
    }

    private DDLResponse generateObjectDDL(String objectType, String objectName) {
        String ddl = "";

        switch (objectType.toUpperCase()) {
            case "TABLE":
                ddl = String.format(
                        "CREATE TABLE %s (\n" +
                                "    ID NUMBER PRIMARY KEY,\n" +
                                "    NAME VARCHAR2(100) NOT NULL,\n" +
                                "    DESCRIPTION VARCHAR2(500),\n" +
                                "    CREATED_DATE DATE DEFAULT SYSDATE,\n" +
                                "    STATUS VARCHAR2(20) CHECK (STATUS IN ('ACTIVE', 'INACTIVE', 'PENDING')),\n" +
                                "    CONSTRAINT %s_NAME_UNQ UNIQUE (NAME)\n" +
                                ") TABLESPACE USERS;",
                        objectName, objectName.toUpperCase()
                );
                break;
            case "PROCEDURE":
                ddl = String.format(
                        "CREATE OR REPLACE PROCEDURE %s (\n" +
                                "    p_id IN NUMBER,\n" +
                                "    p_name IN VARCHAR2,\n" +
                                "    p_result OUT VARCHAR2\n" +
                                ") AS\n" +
                                "BEGIN\n" +
                                "    -- Sample procedure logic\n" +
                                "    p_result := 'Processed: ' || p_name;\n" +
                                "END %s;",
                        objectName, objectName
                );
                break;
            case "SEQUENCE":
                ddl = String.format(
                        "CREATE SEQUENCE %s\n" +
                                "    START WITH 1\n" +
                                "    INCREMENT BY 1\n" +
                                "    MAXVALUE 999999999999999999999999999\n" +
                                "    MINVALUE 1\n" +
                                "    NOCYCLE\n" +
                                "    CACHE 20\n" +
                                "    NOORDER;",
                        objectName
                );
                break;
            default:
                ddl = String.format("-- DDL for %s %s\nCREATE %s %s (...);",
                        objectType, objectName, objectType, objectName);
                break;
        }

        return new DDLResponse(ddl, objectType, objectName, "Generated sample DDL");
    }

    private SearchResponse performSchemaSearch(String searchQuery, String searchType, int maxResults) {
        List<SearchResultDto> results = new ArrayList<>();

        // Generate sample search results
        for (int i = 1; i <= Math.min(maxResults, 10); i++) {
            SearchResultDto result = new SearchResultDto();
            result.setId("result-" + i);
            result.setName(getSearchResultName(i, searchQuery));
            result.setType(getSearchResultType(i));
            result.setOwner(SCHEMA_OWNERS[i % SCHEMA_OWNERS.length]);
            result.setScore((int) (Math.random() * 100));
            result.setSnippet("Contains '" + searchQuery + "' in definition...");
            result.setLastModified(LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            results.add(result);
        }

        return new SearchResponse(results, searchQuery, results.size());
    }

    private ExecuteQueryResponse executeSampleQuery(String query, int timeoutSeconds, boolean readOnly) {
        List<Map<String, Object>> results = new ArrayList<>();

        // Generate sample query results
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("EMPLOYEE_ID", 100 + i);
            row.put("FIRST_NAME", getFirstName(i));
            row.put("LAST_NAME", getLastName(i));
            row.put("EMAIL", (getFirstName(i).charAt(0) + getLastName(i)).toLowerCase() + "@company.com");
            row.put("HIRE_DATE", LocalDateTime.now().minusYears(i).format(DateTimeFormatter.ISO_LOCAL_DATE));
            row.put("SALARY", 50000 + (i * 1000));
            row.put("DEPARTMENT_NAME", getDepartmentName(i));
            results.add(row);
        }

        return new ExecuteQueryResponse(results, query, results.size(), "Query executed successfully");
    }

    private GenerateAPIResponse generateSampleAPI(String objectType, String objectName, String apiType, Map<String, String> options) {
        List<APIEndpointDto> endpoints = new ArrayList<>();

        // Generate sample API endpoints based on type
        if (objectType.equalsIgnoreCase("TABLE")) {
            endpoints.add(createEndpoint("GET", "/api/" + objectName.toLowerCase(), "Get all " + objectName));
            endpoints.add(createEndpoint("GET", "/api/" + objectName.toLowerCase() + "/{id}", "Get " + objectName + " by ID"));
            endpoints.add(createEndpoint("POST", "/api/" + objectName.toLowerCase(), "Create new " + objectName));
            endpoints.add(createEndpoint("PUT", "/api/" + objectName.toLowerCase() + "/{id}", "Update " + objectName));
            endpoints.add(createEndpoint("DELETE", "/api/" + objectName.toLowerCase() + "/{id}", "Delete " + objectName));
        } else if (objectType.equalsIgnoreCase("PROCEDURE")) {
            endpoints.add(createEndpoint("POST", "/api/execute/" + objectName.toLowerCase(), "Execute " + objectName));
        }

        return new GenerateAPIResponse(endpoints, objectType, objectName,
                "API generated successfully with " + endpoints.size() + " endpoints");
    }

    // ========== HELPER METHODS FOR CREATING OBJECTS ==========

    private SchemaObjectDto createTableObject(int index) {
        SchemaObjectDto obj = new SchemaObjectDto();
        obj.setId("TBL_" + index);
        obj.setName(getTableName(index));
        obj.setType("TABLE");
        obj.setOwner(SCHEMA_OWNERS[index % SCHEMA_OWNERS.length]);
        obj.setStatus(OBJECT_STATUSES[index % OBJECT_STATUSES.length]);
        obj.setRowCount((long) (Math.random() * 10000 + 100));
        obj.setSize(String.format("%.2f MB", Math.random() * 50 + 1));
        obj.setCreated(LocalDateTime.now().minusDays(index * 10).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        obj.setLastDDL(LocalDateTime.now().minusDays(index).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        obj.setComment("Table for storing " + getTableName(index).toLowerCase().replace("_", " "));
        return obj;
    }

    private SchemaObjectDto createViewObject(int index) {
        SchemaObjectDto obj = new SchemaObjectDto();
        obj.setId("VW_" + index);
        obj.setName(getViewName(index));
        obj.setType("VIEW");
        obj.setOwner(SCHEMA_OWNERS[index % SCHEMA_OWNERS.length]);
        obj.setStatus("VALID");
        obj.setComment("View combining multiple tables for reporting");
        obj.setCreated(LocalDateTime.now().minusDays(index * 5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return obj;
    }

    private SchemaObjectDto createProcedureObject(int index) {
        SchemaObjectDto obj = new SchemaObjectDto();
        obj.setId("PROC_" + index);
        obj.setName(getProcedureName(index));
        obj.setType("PROCEDURE");
        obj.setOwner(SCHEMA_OWNERS[index % SCHEMA_OWNERS.length]);
        obj.setStatus("VALID");
        obj.setCreated(LocalDateTime.now().minusDays(index * 3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        obj.setComment("Stored procedure for data manipulation");
        return obj;
    }

    private SchemaObjectDto createFunctionObject(int index) {
        SchemaObjectDto obj = new SchemaObjectDto();
        obj.setId("FUNC_" + index);
        obj.setName(getFunctionName(index));
        obj.setType("FUNCTION");
        obj.setOwner(SCHEMA_OWNERS[index % SCHEMA_OWNERS.length]);
        obj.setStatus("VALID");
        obj.setCreated(LocalDateTime.now().minusDays(index * 2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        obj.setComment("Function for calculations and data retrieval");
        return obj;
    }

    private SchemaObjectDto createPackageObject(int index) {
        SchemaObjectDto obj = new SchemaObjectDto();
        obj.setId("PKG_" + index);
        obj.setName(getPackageName(index));
        obj.setType("PACKAGE");
        obj.setOwner(SCHEMA_OWNERS[index % SCHEMA_OWNERS.length]);
        obj.setStatus("VALID");
        obj.setCreated(LocalDateTime.now().minusDays(index * 7).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        obj.setComment("Package containing related procedures and functions");
        return obj;
    }

    private SchemaObjectDto createSequenceObject(int index) {
        SchemaObjectDto obj = new SchemaObjectDto();
        obj.setId("SEQ_" + index);
        obj.setName(getSequenceName(index));
        obj.setType("SEQUENCE");
        obj.setOwner(SCHEMA_OWNERS[index % SCHEMA_OWNERS.length]);
        obj.setStatus("VALID");
        obj.setCreated(LocalDateTime.now().minusDays(index * 15).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        obj.setComment("Sequence for generating unique identifiers");
        return obj;
    }

    private SchemaObjectDto createSynonymObject(int index) {
        SchemaObjectDto obj = new SchemaObjectDto();
        obj.setId("SYN_" + index);
        obj.setName(getSynonymName(index));
        obj.setType("SYNONYM");
        obj.setOwner(SCHEMA_OWNERS[index % SCHEMA_OWNERS.length]);
        obj.setStatus("VALID");
        obj.setCreated(LocalDateTime.now().minusDays(index * 8).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        obj.setComment("Synonym for " + getTableName(index));
        return obj;
    }

    private SchemaObjectDto createTriggerObject(int index) {
        SchemaObjectDto obj = new SchemaObjectDto();
        obj.setId("TRG_" + index);
        obj.setName(getTriggerName(index));
        obj.setType("TRIGGER");
        obj.setOwner(SCHEMA_OWNERS[index % SCHEMA_OWNERS.length]);
        obj.setStatus("ENABLED");
        obj.setCreated(LocalDateTime.now().minusDays(index * 4).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        obj.setComment("Database trigger for automatic actions");
        return obj;
    }

    private SchemaObjectDto createTypeObject(int index) {
        SchemaObjectDto obj = new SchemaObjectDto();
        obj.setId("TYP_" + index);
        obj.setName(getTypeName(index));
        obj.setType("TYPE");
        obj.setOwner(SCHEMA_OWNERS[index % SCHEMA_OWNERS.length]);
        obj.setStatus("VALID");
        obj.setCreated(LocalDateTime.now().minusDays(index * 6).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        obj.setComment("User-defined type for data structures");
        return obj;
    }

    private SchemaObjectDto createIndexObject(int index) {
        SchemaObjectDto obj = new SchemaObjectDto();
        obj.setId("IDX_" + index);
        obj.setName(getIndexName(index));
        obj.setType("INDEX");
        obj.setOwner(SCHEMA_OWNERS[index % SCHEMA_OWNERS.length]);
        obj.setStatus("VALID");
        obj.setCreated(LocalDateTime.now().minusDays(index * 9).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        obj.setComment("Database index for performance optimization");
        return obj;
    }

    private APIEndpointDto createEndpoint(String method, String path, String description) {
        APIEndpointDto endpoint = new APIEndpointDto();
        endpoint.setMethod(method);
        endpoint.setPath(path);
        endpoint.setDescription(description);
        endpoint.setRequestExample(getRequestExample(method));
        endpoint.setResponseExample(getResponseExample(method));
        endpoint.setOperationId(method.toLowerCase() + path.replace("/", "_").replace("{", "").replace("}", ""));
        endpoint.setSummary(description);
        return endpoint;
    }

    // ========== SAMPLE DATA GENERATORS ==========

    private String getConnectionName(int index) {
        String[] names = {"CBX_DMX", "SCOTT_DEV", "PROD_HR", "TEST_DB", "BACKUP_DB"};
        return names[(index - 1) % names.length];
    }

    private String getConnectionDescription(int index) {
        String[] descs = {"Production HR Database", "Development Database", "Production System",
                "Test Environment", "Backup Database"};
        return descs[(index - 1) % descs.length];
    }

    private String getConnectionHost(int index) {
        return "db" + index + ".company.com";
    }

    private String getConnectionPort(int index) {
        return index == 1 ? "1521" : "1522";
    }

    private String getConnectionService(int index) {
        return index == 1 ? "ORCL" : index == 2 ? "XE" : "PROD";
    }

    private String getConnectionUsername(int index) {
        return index == 1 ? "HR" : index == 2 ? "SCOTT" : "SYSTEM";
    }

    private String getConnectionDriver(int index) {
        return index == 1 ? "Oracle JDBC" : "Oracle Thin Driver";
    }

    private String getConnectionVersion(int index) {
        return index == 1 ? "19c" : "21c";
    }

    private String getTableName(int index) {
        String[] tables = {"EMPLOYEES", "DEPARTMENTS", "JOBS", "LOCATIONS", "COUNTRIES",
                "REGIONS", "JOB_HISTORY", "SALARY_GRADES", "PROJECTS", "TASKS"};
        return tables[(index - 1) % tables.length];
    }

    private String getViewName(int index) {
        String[] views = {"EMP_DETAILS_VIEW", "DEPT_SUMMARY", "SALARY_REPORT", "PROJECT_STATUS"};
        return views[(index - 1) % views.length];
    }

    private String getProcedureName(int index) {
        String[] procs = {"ADD_EMPLOYEE", "UPDATE_SALARY", "DELETE_EMPLOYEE", "CALCULATE_BONUS"};
        return procs[(index - 1) % procs.length];
    }

    private String getFunctionName(int index) {
        String[] funcs = {"GET_EMPLOYEE_NAME", "CALCULATE_TAX", "VALIDATE_EMAIL", "GENERATE_ID"};
        return funcs[(index - 1) % funcs.length];
    }

    private String getPackageName(int index) {
        String[] pkgs = {"EMP_PKG", "UTIL_PKG", "SECURITY_PKG", "REPORT_PKG"};
        return pkgs[(index - 1) % pkgs.length];
    }

    private String getSequenceName(int index) {
        String[] seqs = {"EMPLOYEES_SEQ", "DEPARTMENTS_SEQ", "PROJECTS_SEQ"};
        return seqs[(index - 1) % seqs.length];
    }

    private String getSynonymName(int index) {
        String[] syns = {"EMP", "DEPT", "LOC"};
        return syns[(index - 1) % syns.length];
    }

    private String getTriggerName(int index) {
        String[] trigs = {"BIU_EMPLOYEES", "AUDIT_DEPARTMENTS", "UPDATE_TIMESTAMP"};
        return trigs[(index - 1) % trigs.length];
    }

    private String getTypeName(int index) {
        String[] types = {"EMPLOYEE_REC", "ADDRESS_TYP", "PHONE_TYP"};
        return types[(index - 1) % types.length];
    }

    private String getIndexName(int index) {
        String[] indexes = {"EMP_NAME_IX", "DEPT_NAME_IX", "PROJ_START_IX"};
        return indexes[(index - 1) % indexes.length];
    }

    private String getColumnName(int index) {
        String[] cols = {"ID", "NAME", "DESCRIPTION", "CREATED_DATE", "STATUS", "AMOUNT", "QUANTITY", "PRICE"};
        return cols[(index - 1) % cols.length];
    }

    private String getConstraintColumns(int index) {
        String[] cols = {"ID", "NAME,EMAIL", "DEPARTMENT_ID", "STATUS,CREATED_DATE"};
        return cols[(index - 1) % cols.length];
    }

    private String getSearchResultName(int index, String query) {
        String[] names = {"EMPLOYEES", "CUSTOMERS", "ORDERS", "PRODUCTS", "INVOICES"};
        return names[(index - 1) % names.length] + "_" + query.toUpperCase();
    }

    private String getSearchResultType(int index) {
        String[] types = {"TABLE", "VIEW", "PROCEDURE", "FUNCTION", "PACKAGE"};
        return types[(index - 1) % types.length];
    }

    private String getFirstName(int index) {
        String[] names = {"John", "Jane", "Robert", "Mary", "David"};
        return names[(index - 1) % names.length];
    }

    private String getLastName(int index) {
        String[] names = {"Smith", "Johnson", "Williams", "Brown", "Jones"};
        return names[(index - 1) % names.length];
    }

    private String getDepartmentName(int index) {
        String[] depts = {"IT", "HR", "Finance", "Marketing", "Sales"};
        return depts[(index - 1) % depts.length];
    }

    private Map<String, Object> getRequestExample(String method) {
        Map<String, Object> example = new HashMap<>();
        if (method.equals("POST") || method.equals("PUT")) {
            example.put("name", "Sample Name");
            example.put("description", "Sample Description");
            example.put("active", true);
        } else if (method.equals("GET")) {
            example.put("id", 123);
        }
        return example;
    }

    private Map<String, Object> getResponseExample(String method) {
        Map<String, Object> example = new HashMap<>();
        example.put("id", 123);
        example.put("name", "Sample Response");
        example.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        example.put("success", true);
        return example;
    }

    // ========== FALLBACK METHODS ==========

    private SchemaConnectionsResponse getFallbackSchemaConnections() {
        List<ConnectionDto> connections = new ArrayList<>();

        ConnectionDto conn = new ConnectionDto();
        conn.setId("conn-1");
        conn.setName("CBX_DMX");
        conn.setDescription("Development Database");
        conn.setHost("db.unionsg.com");
        conn.setPort("1521");
        conn.setService("ORCL");
        conn.setUsername("HR");
        conn.setStatus("connected");
        conn.setType("oracle");
        conn.setColor("green");
        conn.setLastUsed(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        connections.add(conn);

        return new SchemaConnectionsResponse(connections);
    }

    private ObjectDetailsResponse getFallbackObjectDetails(String objectType, String objectName) {
        ObjectDetailsResponse details = new ObjectDetailsResponse();
        details.setObjectName(objectName);
        details.setObjectType(objectType);
        details.setOwner("HR");
        details.setStatus("VALID");
        details.setCreated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setComment("Fallback details for " + objectType + " " + objectName);
        details.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return details;
    }

    // ========== INNER CLASSES ==========

    private static class SchemaCache {
        private final Object data;
        private final long timestamp;

        public SchemaCache(Object data, long timestamp) {
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