package com.usg.apiAutomation.repositories;

import com.usg.apiAutomation.dtos.systemActivities.database.DatabaseObjectDTO;
import com.usg.apiAutomation.dtos.systemActivities.database.DatabaseObjectSearchDTO;
import com.usg.apiAutomation.dtos.systemActivities.database.ParameterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DatabaseObjectRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    // ============================================================
    // 1. PROCEDURES
    // ============================================================

    public List<DatabaseObjectDTO> findProcedures(String owner, String status, String searchTerm) {
        String sql = """
            SELECT 
                owner,
                object_name as objectName,
                'PROCEDURE' as objectType,
                status,
                created,
                last_ddl_time as lastDdlTime
            FROM 
                all_procedures 
            WHERE 
                object_type = 'PROCEDURE'
                AND (:owner IS NULL OR owner = :owner)
                AND (:status IS NULL OR status = :status)
                AND (:searchTerm IS NULL OR LOWER(object_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
                AND owner NOT IN ('SYS', 'SYSTEM', 'XDB', 'CTXSYS')
            ORDER BY 
                owner, object_name
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("owner", owner)
                .addValue("status", status)
                .addValue("searchTerm", searchTerm);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> mapToDatabaseObjectDTO(rs));
    }

    // ============================================================
    // 2. FUNCTIONS
    // ============================================================

    public List<DatabaseObjectDTO> findFunctions(String owner, String status) {
        String sql = """
            SELECT 
                owner,
                object_name as objectName,
                'FUNCTION' as objectType,
                status,
                created,
                last_ddl_time as lastDdlTime
            FROM 
                all_procedures 
            WHERE 
                object_type = 'FUNCTION'
                AND (:owner IS NULL OR owner = :owner)
                AND (:status IS NULL OR status = :status)
                AND owner NOT IN ('SYS', 'SYSTEM', 'XDB', 'CTXSYS')
            ORDER BY 
                owner, object_name
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("owner", owner)
                .addValue("status", status);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> mapToDatabaseObjectDTO(rs));
    }

    // ============================================================
    // 3. VIEWS
    // ============================================================

    public List<DatabaseObjectDTO> findViews(String owner) {
        String sql = """
            SELECT 
                owner,
                view_name as objectName,
                'VIEW' as objectType,
                status,
                created,
                last_ddl_time as lastDdlTime,
                read_only as readOnly
            FROM 
                all_views
            WHERE 
                (:owner IS NULL OR owner = :owner)
                AND owner NOT IN ('SYS', 'SYSTEM', 'XDB', 'CTXSYS')
            ORDER BY 
                owner, view_name
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("owner", owner);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> mapToDatabaseObjectDTO(rs));
    }

    // ============================================================
    // 4. TRIGGERS
    // ============================================================

    public List<DatabaseObjectDTO> findTriggers(String owner, String tableName) {
        String sql = """
            SELECT 
                owner,
                trigger_name as objectName,
                'TRIGGER' as objectType,
                status,
                created,
                last_ddl_time as lastDdlTime,
                table_owner as tableOwner,
                table_name as tableName
            FROM 
                all_triggers
            WHERE 
                (:owner IS NULL OR owner = :owner)
                AND (:tableName IS NULL OR table_name = :tableName)
                AND owner NOT IN ('SYS', 'SYSTEM', 'XDB', 'CTXSYS')
            ORDER BY 
                owner, table_name, trigger_name
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("owner", owner)
                .addValue("tableName", tableName);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> mapToDatabaseObjectDTO(rs));
    }

    // ============================================================
    // 5. JOBS
    // ============================================================

    public List<DatabaseObjectDTO> findJobs(String owner, String jobType) {
        String sql = """
            SELECT 
                log_user as owner,
                'DBMS_JOB.' || job as objectName,
                'DBMS_JOB' as jobType,
                'JOB' as objectType,
                what as objectName,
                CASE WHEN broken = 'Y' THEN 'INVALID' ELSE 'VALID' END as status,
                NULL as created,
                next_date as lastDdlTime
            FROM 
                user_jobs
            WHERE 
                (:owner IS NULL OR log_user = :owner)
                AND (:jobType IS NULL OR 'DBMS_JOB' = :jobType)
                AND log_user NOT IN ('SYS', 'SYSTEM', 'XDB', 'CTXSYS')
            UNION ALL
            SELECT 
                owner,
                job_name as objectName,
                'SCHEDULER_JOB' as jobType,
                'JOB' as objectType,
                job_action as objectName,
                CASE WHEN enabled = 'TRUE' THEN 'VALID' ELSE 'INVALID' END as status,
                NULL as created,
                NULL as lastDdlTime
            FROM 
                all_scheduler_jobs
            WHERE 
                (:owner IS NULL OR owner = :owner)
                AND (:jobType IS NULL OR 'SCHEDULER_JOB' = :jobType)
                AND owner NOT IN ('SYS', 'SYSTEM', 'XDB', 'CTXSYS')
            ORDER BY 
                owner, objectName
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("owner", owner)
                .addValue("jobType", jobType);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> mapToDatabaseObjectDTO(rs));
    }

    // ============================================================
    // 6. FIND ALL OBJECTS WITH SEARCH CRITERIA
    // ============================================================

    public List<DatabaseObjectDTO> findAllObjects(DatabaseObjectSearchDTO searchDTO) {
        if (searchDTO == null) {
            return List.of();
        }

        switch (searchDTO.getObjectType().toUpperCase()) {
            case "PROCEDURE":
                return findProcedures(searchDTO.getOwner(), searchDTO.getStatus(), searchDTO.getSearchTerm());
            case "FUNCTION":
                return findFunctions(searchDTO.getOwner(), searchDTO.getStatus());
            case "VIEW":
                return findViews(searchDTO.getOwner());
            case "TRIGGER":
                return findTriggers(searchDTO.getOwner(), searchDTO.getTableName());
            case "JOB":
                return findJobs(searchDTO.getOwner(), searchDTO.getJobType());
            default:
                return List.of();
        }
    }

    // ============================================================
    // 7. FIND OBJECT BY NAME
    // ============================================================

    public DatabaseObjectDTO findObjectByName(String objectType, String objectName, String owner) {
        List<DatabaseObjectDTO> objects;

        switch (objectType.toUpperCase()) {
            case "PROCEDURE":
                objects = findProcedures(owner, null, objectName);
                break;
            case "FUNCTION":
                objects = findFunctions(owner, null);
                break;
            case "VIEW":
                objects = findViews(owner);
                break;
            case "TRIGGER":
                objects = findTriggers(owner, null);
                break;
            case "JOB":
                objects = findJobs(owner, null);
                break;
            default:
                objects = List.of();
        }

        return objects.stream()
                .filter(obj -> obj.getObjectName().equalsIgnoreCase(objectName))
                .findFirst()
                .orElse(null);
    }

    // ============================================================
    // 8. FIND OBJECT PARAMETERS
    // ============================================================

    public List<ParameterDTO> findObjectParameters(String owner, String objectName) {
        String sql = """
            SELECT 
                argument_name as parameterName,
                data_type as dataType,
                in_out as inOut,
                position as position,
                default_value as defaultValue
            FROM 
                all_arguments 
            WHERE 
                owner = :owner 
                AND object_name = :objectName
                AND position > 0
            ORDER BY 
                position
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("owner", owner)
                .addValue("objectName", objectName);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) ->
                ParameterDTO.builder()
                        .parameterName(rs.getString("parameterName"))
                        .dataType(rs.getString("dataType"))
                        .inOut(rs.getString("inOut"))
                        .position(rs.getInt("position"))
                        .defaultValue(rs.getString("defaultValue"))
                        .build()
        );
    }

    // ============================================================
    // 9. GET OBJECT SOURCE CODE
    // ============================================================

    public String getObjectSource(String objectType, String owner, String objectName) {
        String sql = """
            SELECT text 
            FROM all_source 
            WHERE owner = :owner 
            AND name = :objectName
            AND type = :objectType
            ORDER BY line
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("objectType", objectType)
                .addValue("owner", owner)
                .addValue("objectName", objectName);

        List<String> lines = namedParameterJdbcTemplate.query(sql, params,
                (rs, rowNum) -> rs.getString("text"));

        return String.join("", lines);
    }

    // ============================================================
    // 10. GET OBJECT DEPENDENCIES
    // ============================================================

    public List<DatabaseObjectDTO> getDependencies(String owner, String objectName) {
        String sql = """
            SELECT 
                referenced_owner as owner,
                referenced_name as objectName,
                referenced_type as objectType
            FROM 
                all_dependencies 
            WHERE 
                owner = :owner 
                AND name = :objectName
                AND referenced_owner NOT IN ('SYS', 'SYSTEM', 'XDB', 'CTXSYS')
            ORDER BY 
                referenced_owner, referenced_name
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("owner", owner)
                .addValue("objectName", objectName);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) ->
                DatabaseObjectDTO.builder()
                        .owner(rs.getString("owner"))
                        .objectName(rs.getString("objectName"))
                        .objectType(rs.getString("objectType"))
                        .build()
        );
    }

    // ============================================================
    // 11. GET USAGE STATISTICS
    // ============================================================

    public Map<String, Object> getUsageStatistics(String owner, String objectName) {
        String sql = """
            SELECT 
                COUNT(*) as execution_count,
                MAX(last_executed) as last_executed,
                MIN(first_executed) as first_executed
            FROM 
                all_objects 
            WHERE 
                owner = :owner 
                AND object_name = :objectName
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("owner", owner)
                .addValue("objectName", objectName);

        return namedParameterJdbcTemplate.queryForMap(sql, params);
    }

    // ============================================================
    // 12. GET COUNTS BY OBJECT TYPE
    // ============================================================

    public Map<String, Long> getCountsByObjectType() {
        String sql = """
            SELECT 'PROCEDURE' as object_type, COUNT(*) as count
            FROM all_procedures WHERE object_type = 'PROCEDURE'
            AND owner NOT IN ('SYS', 'SYSTEM', 'XDB', 'CTXSYS')
            UNION ALL
            SELECT 'FUNCTION' as object_type, COUNT(*) as count
            FROM all_procedures WHERE object_type = 'FUNCTION'
            AND owner NOT IN ('SYS', 'SYSTEM', 'XDB', 'CTXSYS')
            UNION ALL
            SELECT 'VIEW' as object_type, COUNT(*) as count
            FROM all_views
            WHERE owner NOT IN ('SYS', 'SYSTEM', 'XDB', 'CTXSYS')
            UNION ALL
            SELECT 'TRIGGER' as object_type, COUNT(*) as count
            FROM all_triggers
            WHERE owner NOT IN ('SYS', 'SYSTEM', 'XDB', 'CTXSYS')
            """;

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row.get("object_type"),
                        row -> ((Number) row.get("count")).longValue()
                ));
    }

    // ============================================================
    // 13. GET COUNTS BY SCHEMA
    // ============================================================

    public Map<String, Map<String, Long>> getCountsBySchema() {
        String sql = """
            WITH object_counts AS (
                SELECT owner, 'PROCEDURE' as object_type, COUNT(*) as count
                FROM all_procedures WHERE object_type = 'PROCEDURE'
                GROUP BY owner
                UNION ALL
                SELECT owner, 'FUNCTION' as object_type, COUNT(*) as count
                FROM all_procedures WHERE object_type = 'FUNCTION'
                GROUP BY owner
                UNION ALL
                SELECT owner, 'VIEW' as object_type, COUNT(*) as count
                FROM all_views
                GROUP BY owner
                UNION ALL
                SELECT owner, 'TRIGGER' as object_type, COUNT(*) as count
                FROM all_triggers
                GROUP BY owner
            )
            SELECT owner, object_type, count
            FROM object_counts
            WHERE owner NOT IN ('SYS', 'SYSTEM', 'XDB', 'CTXSYS')
            ORDER BY owner, object_type
            """;

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        Map<String, Map<String, Long>> schemaCounts = new java.util.HashMap<>();

        for (Map<String, Object> row : results) {
            String owner = (String) row.get("owner");
            String objectType = (String) row.get("object_type");
            Long count = ((Number) row.get("count")).longValue();

            schemaCounts.computeIfAbsent(owner, k -> new java.util.HashMap<>())
                    .put(objectType, count);
        }

        return schemaCounts;
    }

    // ============================================================
    // 14. GET RECENT OBJECTS
    // ============================================================

    public List<DatabaseObjectDTO> getRecentObjects(int limit) {
        String sql = """
            SELECT 
                owner,
                object_name,
                object_type,
                status,
                created,
                last_ddl_time
            FROM (
                -- Procedures
                SELECT 
                    owner,
                    object_name,
                    'PROCEDURE' as object_type,
                    status,
                    created,
                    last_ddl_time
                FROM all_procedures WHERE object_type = 'PROCEDURE'
                UNION ALL
                -- Functions
                SELECT 
                    owner,
                    object_name,
                    'FUNCTION' as object_type,
                    status,
                    created,
                    last_ddl_time
                FROM all_procedures WHERE object_type = 'FUNCTION'
                UNION ALL
                -- Views
                SELECT 
                    owner,
                    view_name as object_name,
                    'VIEW' as object_type,
                    status,
                    created,
                    last_ddl_time
                FROM all_views
                UNION ALL
                -- Triggers
                SELECT 
                    owner,
                    trigger_name as object_name,
                    'TRIGGER' as object_type,
                    status,
                    created,
                    last_ddl_time
                FROM all_triggers
            )
            WHERE owner NOT IN ('SYS', 'SYSTEM', 'XDB', 'CTXSYS')
            ORDER BY created DESC NULLS LAST
            FETCH FIRST ? ROWS ONLY
            """;

        return jdbcTemplate.query(sql, new Object[]{limit}, (rs, rowNum) -> mapToDatabaseObjectDTO(rs));
    }

    // ============================================================
    // 15. GET INVALID OBJECTS
    // ============================================================

    public List<DatabaseObjectDTO> getInvalidObjects() {
        String sql = """
            SELECT 
                owner,
                object_name,
                object_type,
                'INVALID' as status,
                created,
                last_ddl_time
            FROM (
                -- Invalid Procedures
                SELECT 
                    owner,
                    object_name,
                    'PROCEDURE' as object_type,
                    created,
                    last_ddl_time
                FROM all_procedures 
                WHERE object_type = 'PROCEDURE' AND status = 'INVALID'
                UNION ALL
                -- Invalid Functions
                SELECT 
                    owner,
                    object_name,
                    'FUNCTION' as object_type,
                    created,
                    last_ddl_time
                FROM all_procedures 
                WHERE object_type = 'FUNCTION' AND status = 'INVALID'
                UNION ALL
                -- Invalid Views
                SELECT 
                    owner,
                    view_name as object_name,
                    'VIEW' as object_type,
                    created,
                    last_ddl_time
                FROM all_views 
                WHERE status = 'INVALID'
                UNION ALL
                -- Invalid Triggers
                SELECT 
                    owner,
                    trigger_name as object_name,
                    'TRIGGER' as object_type,
                    created,
                    last_ddl_time
                FROM all_triggers 
                WHERE status = 'INVALID'
            )
            WHERE owner NOT IN ('SYS', 'SYSTEM', 'XDB', 'CTXSYS')
            ORDER BY owner, object_name
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapToDatabaseObjectDTO(rs));
    }

    // ============================================================
    // 16. GET TOP SCHEMAS
    // ============================================================

    public Map<String, Long> getTopSchemas(int limit) {
        String sql = """
            WITH object_counts AS (
                SELECT owner, COUNT(*) as total_objects
                FROM (
                    SELECT owner, object_name FROM all_procedures 
                    WHERE object_type IN ('PROCEDURE', 'FUNCTION')
                    UNION ALL
                    SELECT owner, view_name FROM all_views
                    UNION ALL
                    SELECT owner, trigger_name FROM all_triggers
                )
                WHERE owner NOT IN ('SYS', 'SYSTEM', 'XDB', 'CTXSYS')
                GROUP BY owner
            )
            SELECT owner, total_objects
            FROM object_counts
            ORDER BY total_objects DESC
            FETCH FIRST ? ROWS ONLY
            """;

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, limit);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row.get("owner"),
                        row -> ((Number) row.get("total_objects")).longValue()
                ));
    }

    // ============================================================
    // 17. CHECK IF OBJECT EXISTS
    // ============================================================

    public boolean objectExists(String objectType, String owner, String objectName) {
        DatabaseObjectDTO object = findObjectByName(objectType, objectName, owner);
        return object != null;
    }

    // ============================================================
    // 18. GET OBJECT STATUS
    // ============================================================

    public String getObjectStatus(String owner, String objectName) {
        String sql = """
            SELECT status 
            FROM all_objects 
            WHERE owner = ? 
            AND object_name = ?
            AND object_type IN ('PROCEDURE', 'FUNCTION', 'VIEW', 'TRIGGER')
            """;

        try {
            return jdbcTemplate.queryForObject(sql, String.class, owner, objectName);
        } catch (Exception e) {
            return null;
        }
    }

    // ============================================================
    // 19. GET COMPILATION ERRORS
    // ============================================================

    public List<String> getCompilationErrors(String owner, String objectName) {
        String sql = """
            SELECT text 
            FROM all_errors 
            WHERE owner = ? 
            AND name = ?
            ORDER BY line, position
            """;

        return jdbcTemplate.queryForList(sql, String.class, owner, objectName);
    }

    // ============================================================
    // 20. CHECK DEPENDENCIES
    // ============================================================

    public List<String> checkDependencies(String owner, String objectName) {
        String sql = """
            SELECT referenced_name 
            FROM all_dependencies 
            WHERE owner = ? 
            AND name = ?
            AND referenced_type IN ('TABLE', 'VIEW', 'PROCEDURE', 'FUNCTION')
            AND referenced_owner NOT IN ('SYS', 'SYSTEM', 'XDB', 'CTXSYS')
            AND NOT EXISTS (
                SELECT 1 
                FROM all_objects o 
                WHERE o.owner = all_dependencies.referenced_owner 
                AND o.object_name = all_dependencies.referenced_name
            )
            """;

        return jdbcTemplate.queryForList(sql, String.class, owner, objectName);
    }

    // ============================================================
    // 21. GET UNIQUE OWNERS
    // ============================================================

    public List<String> findUniqueOwners() {
        String sql = """
            SELECT DISTINCT owner
            FROM (
                SELECT owner FROM all_procedures
                UNION
                SELECT owner FROM all_views
                UNION
                SELECT owner FROM all_triggers
                UNION
                SELECT log_user as owner FROM user_jobs
                UNION
                SELECT owner FROM all_scheduler_jobs
            )
            WHERE owner NOT IN ('SYS', 'SYSTEM', 'XDB', 'CTXSYS')
            ORDER BY owner
            """;

        return jdbcTemplate.queryForList(sql, String.class);
    }

    // ============================================================
    // 22. GET UNIQUE OBJECT TYPES
    // ============================================================

    public List<String> findUniqueObjectTypes() {
        String sql = """
            SELECT DISTINCT 'PROCEDURE' as object_type FROM DUAL
            UNION ALL
            SELECT 'FUNCTION' FROM DUAL
            UNION ALL
            SELECT 'VIEW' FROM DUAL
            UNION ALL
            SELECT 'TRIGGER' FROM DUAL
            UNION ALL
            SELECT 'JOB' FROM DUAL
            """;

        return jdbcTemplate.queryForList(sql, String.class);
    }

    // ============================================================
    // 23. SEARCH OBJECTS BY MULTIPLE CRITERIA
    // ============================================================

    public List<DatabaseObjectDTO> searchObjects(String owner, String objectType, String status, String search) {
        String sql = """
            SELECT 
                owner,
                object_name,
                object_type,
                status,
                created,
                last_ddl_time
            FROM (
                -- Procedures
                SELECT 
                    owner,
                    object_name,
                    'PROCEDURE' as object_type,
                    status,
                    created,
                    last_ddl_time
                FROM all_procedures WHERE object_type = 'PROCEDURE'
                AND (:search IS NULL OR LOWER(object_name) LIKE LOWER(CONCAT('%', :search, '%')))
                UNION ALL
                -- Functions
                SELECT 
                    owner,
                    object_name,
                    'FUNCTION' as object_type,
                    status,
                    created,
                    last_ddl_time
                FROM all_procedures WHERE object_type = 'FUNCTION'
                AND (:search IS NULL OR LOWER(object_name) LIKE LOWER(CONCAT('%', :search, '%')))
                UNION ALL
                -- Views
                SELECT 
                    owner,
                    view_name as object_name,
                    'VIEW' as object_type,
                    status,
                    created,
                    last_ddl_time
                FROM all_views
                WHERE (:search IS NULL OR LOWER(view_name) LIKE LOWER(CONCAT('%', :search, '%')))
                UNION ALL
                -- Triggers
                SELECT 
                    owner,
                    trigger_name as object_name,
                    'TRIGGER' as object_type,
                    status,
                    created,
                    last_ddl_time
                FROM all_triggers
                WHERE (:search IS NULL OR LOWER(trigger_name) LIKE LOWER(CONCAT('%', :search, '%')))
            )
            WHERE (:owner IS NULL OR owner = :owner)
            AND (:status IS NULL OR status = :status)
            AND (:objectType IS NULL OR object_type = :objectType)
            AND owner NOT IN ('SYS', 'SYSTEM', 'XDB', 'CTXSYS')
            ORDER BY owner, object_name
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("owner", owner)
                .addValue("objectType", objectType)
                .addValue("status", status)
                .addValue("search", search);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> mapToDatabaseObjectDTO(rs));
    }

    // ============================================================
    // HELPER METHOD: Map ResultSet to DatabaseObjectDTO
    // ============================================================

    private DatabaseObjectDTO mapToDatabaseObjectDTO(ResultSet rs) throws SQLException {
        DatabaseObjectDTO dto = DatabaseObjectDTO.builder()
                .owner(rs.getString("owner"))
                .objectName(rs.getString("object_name"))
                .objectType(rs.getString("object_type"))
                .status(getStringOrNull(rs, "status"))
                .created(getTimestampAsLocalDateTime(rs, "created"))
                .lastDdlTime(getTimestampAsLocalDateTime(rs, "last_ddl_time"))
                .build();

        // Handle additional fields for specific object types
        try {
            if ("VIEW".equalsIgnoreCase(dto.getObjectType())) {
                dto.setReadOnly(getStringOrNull(rs, "read_only"));
            } else if ("TRIGGER".equalsIgnoreCase(dto.getObjectType())) {
                dto.setTableOwner(getStringOrNull(rs, "table_owner"));
                dto.setTableName(getStringOrNull(rs, "table_name"));
            } else if ("JOB".equalsIgnoreCase(dto.getObjectType())) {
                dto.setJobType(getStringOrNull(rs, "job_type"));
            }
        } catch (SQLException e) {
            // Column doesn't exist for this query, ignore
        }

        return dto;
    }

    private String getStringOrNull(ResultSet rs, String column) throws SQLException {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }

    private LocalDateTime getTimestampAsLocalDateTime(ResultSet rs, String column) throws SQLException {
        try {
            java.sql.Timestamp timestamp = rs.getTimestamp(column);
            return timestamp != null ? timestamp.toLocalDateTime() : null;
        } catch (SQLException e) {
            return null;
        }
    }
}