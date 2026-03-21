package com.usg.apiAutomation.utils.apiEngine;

import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiParameterDTO;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PostgreSQLParameterValidatorUtil {

    @Autowired
    @Qualifier("postgresqlJdbcTemplate")
    private JdbcTemplate postgresqlJdbcTemplate;

    public void validateParameters(List<ApiParameterDTO> configuredParams,
                                   Map<String, Object> providedParams,
                                   String schema,
                                   String objectName) {

        log.info("Validating parameters for {}.{}", schema, objectName);

        // Get actual parameter definitions from PostgreSQL
        List<Map<String, Object>> actualParams = getDatabaseParameters(schema, objectName);

        if (actualParams.isEmpty()) {
            log.warn("No parameters found for {}.{} - skipping validation", schema, objectName);
            return;
        }

        log.info("Found {} parameters for {}.{}", actualParams.size(), schema, objectName);

        // Log actual parameters for debugging
        actualParams.forEach(param ->
                log.debug("Parameter: {} - Type: {} - Mode: {} - Position: {}",
                        param.get("ARGUMENT_NAME"),
                        param.get("DATA_TYPE"),
                        param.get("IN_OUT"),
                        param.get("POSITION"))
        );

        // Validate required parameters are provided
        List<Map<String, Object>> requiredParams = actualParams.stream()
                .filter(p -> {
                    String inOut = (String) p.get("IN_OUT");
                    return "IN".equals(inOut) || "IN/OUT".equals(inOut);
                })
                .filter(p -> {
                    String defaultValue = (String) p.get("DEFAULTED");
                    return !"YES".equalsIgnoreCase(defaultValue);
                })
                .collect(Collectors.toList());

        log.info("Required parameters: {}", requiredParams.size());

        for (Map<String, Object> param : requiredParams) {
            String paramName = (String) param.get("ARGUMENT_NAME");
            if (paramName == null) continue;

            if (!providedParams.containsKey(paramName) &&
                    !providedParams.containsKey(paramName.toLowerCase())) {

                log.error("Required parameter '{}' not provided", paramName);
                throw new ValidationException(
                        String.format("Required parameter '%s' not provided", paramName)
                );
            }
        }

        // Validate data types
        for (Map.Entry<String, Object> entry : providedParams.entrySet()) {
            String paramName = entry.getKey();
            Object paramValue = entry.getValue();

            // Find the parameter definition
            Map<String, Object> paramDef = actualParams.stream()
                    .filter(p -> paramName.equalsIgnoreCase((String) p.get("ARGUMENT_NAME")))
                    .findFirst()
                    .orElse(null);

            if (paramDef != null) {
                String dataType = (String) paramDef.get("DATA_TYPE");
                log.debug("Validating parameter {} of type {} with value: {}",
                        paramName, dataType, paramValue);

                // Basic type validation
                if (paramValue != null) {
                    validateDataType(paramName, paramValue, dataType);
                }
            }
        }

        log.info("✅ Parameter validation passed for {}.{}", schema, objectName);
    }

    /**
     * Fetches parameter definitions from PostgreSQL system catalogs
     */
    private List<Map<String, Object>> getDatabaseParameters(String schema, String objectName) {
        List<Map<String, Object>> parameters = new ArrayList<>();

        try {
            // Get function/procedure OID
            String oidSql = "SELECT p.oid, p.prokind FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ?";

            Map<String, Object> procInfo = null;
            try {
                procInfo = postgresqlJdbcTemplate.queryForMap(oidSql, schema, objectName);
            } catch (Exception e) {
                log.warn("Could not find function/procedure {}.{}", schema, objectName);
                return parameters;
            }

            Long oid = (Long) procInfo.get("oid");
            String prokind = (String) procInfo.get("prokind");

            if (oid == null) {
                log.warn("Could not get OID for {}.{}", schema, objectName);
                return parameters;
            }

            // Get argument information
            String argsSql = "SELECT " +
                    "    proargnames, " +
                    "    proargtypes, " +
                    "    proargmodes, " +
                    "    pronargdefaults " +
                    "FROM pg_proc WHERE oid = ?";

            Map<String, Object> argsInfo = postgresqlJdbcTemplate.queryForMap(argsSql, oid);

            java.sql.Array proargnames = (java.sql.Array) argsInfo.get("proargnames");
            java.sql.Array proargtypes = (java.sql.Array) argsInfo.get("proargtypes");
            java.sql.Array proargmodes = (java.sql.Array) argsInfo.get("proargmodes");
            Integer pronargdefaults = (Integer) argsInfo.get("pronargdefaults");

            String[] argNames = proargnames != null ? (String[]) proargnames.getArray() : new String[0];
            String[] argTypes = proargtypes != null ? (String[]) proargtypes.getArray() : new String[0];
            String[] argModes = proargmodes != null ? (String[]) proargmodes.getArray() : new String[0];

            int defaultCount = pronargdefaults != null ? pronargdefaults : 0;

            for (int i = 0; i < argNames.length; i++) {
                Map<String, Object> param = new HashMap<>();
                param.put("ARGUMENT_NAME", argNames[i]);
                param.put("DATA_TYPE", getDataTypeName(argTypes[i]));
                param.put("IN_OUT", getParameterMode(i < argModes.length ? argModes[i] : "i"));
                param.put("POSITION", i + 1);
                param.put("DEFAULTED", (i >= argNames.length - defaultCount) ? "YES" : "NO");

                parameters.add(param);
            }

        } catch (Exception e) {
            log.warn("Could not fetch database parameters: {}", e.getMessage());
        }

        return parameters;
    }

    /**
     * Get PostgreSQL data type name from type OID
     */
    private String getDataTypeName(String typeOid) {
        try {
            String sql = "SELECT typname FROM pg_type WHERE oid = ?::regtype::oid";
            return postgresqlJdbcTemplate.queryForObject(sql, String.class, typeOid);
        } catch (Exception e) {
            return typeOid;
        }
    }

    /**
     * Get parameter mode from PostgreSQL mode code
     */
    private String getParameterMode(String modeCode) {
        if (modeCode == null) return "IN";
        switch (modeCode) {
            case "i": return "IN";
            case "o": return "OUT";
            case "b": return "IN/OUT";
            case "v": return "VARIADIC";
            default: return "IN";
        }
    }

    /**
     * Validates a parameter's value against its PostgreSQL data type
     */
    private void validateDataType(String paramName, Object value, String expectedType) {
        String lowerType = expectedType.toLowerCase();

        // String types
        if (lowerType.contains("varchar") || lowerType.contains("char") ||
                lowerType.contains("text") || lowerType.contains("citext") ||
                lowerType.contains("json") || lowerType.contains("jsonb") ||
                lowerType.contains("xml") || lowerType.contains("uuid") ||
                lowerType.contains("inet") || lowerType.contains("cidr") ||
                lowerType.contains("macaddr")) {

            if (!(value instanceof String)) {
                throw new ValidationException(
                        String.format("Parameter '%s' should be of type String, but got %s",
                                paramName, value.getClass().getSimpleName())
                );
            }

            // Additional validation for specific string types
            if (lowerType.contains("json") || lowerType.contains("jsonb")) {
                validateJsonFormat(paramName, (String) value);
            } else if (lowerType.contains("uuid")) {
                validateUuidFormat(paramName, (String) value);
            } else if (lowerType.contains("inet") || lowerType.contains("cidr")) {
                validateNetworkFormat(paramName, (String) value);
            } else if (lowerType.contains("macaddr")) {
                validateMacAddrFormat(paramName, (String) value);
            }
        }
        // Numeric types
        else if (lowerType.contains("int") || lowerType.contains("integer") ||
                lowerType.contains("smallint") || lowerType.contains("bigint") ||
                lowerType.contains("decimal") || lowerType.contains("numeric") ||
                lowerType.contains("real") || lowerType.contains("double") ||
                lowerType.contains("float")) {

            validateNumericValue(paramName, value, lowerType);
        }
        // Date/Time types
        else if (lowerType.contains("date") || lowerType.contains("time") ||
                lowerType.contains("timestamp") || lowerType.contains("interval")) {

            if (!(value instanceof String)) {
                throw new ValidationException(
                        String.format("Parameter '%s' should be a date/time string, but got %s",
                                paramName, value.getClass().getSimpleName())
                );
            }
            validateDateTimeFormat(paramName, (String) value, lowerType);
        }
        // Boolean type
        else if (lowerType.contains("bool")) {
            validateBooleanValue(paramName, value);
        }
        // Binary type
        else if (lowerType.contains("bytea") || lowerType.contains("bit")) {
            validateBinaryValue(paramName, value);
        }
        // Array types
        else if (lowerType.contains("[]") || lowerType.contains("array")) {
            if (!(value instanceof List) && !(value instanceof Object[])) {
                throw new ValidationException(
                        String.format("Parameter '%s' should be an array, but got %s",
                                paramName, value.getClass().getSimpleName())
                );
            }
        }
        // Unknown type - warn but don't fail
        else {
            log.warn("Unknown PostgreSQL type '{}' for parameter '{}', skipping validation",
                    expectedType, paramName);
        }
    }

    /**
     * Validates numeric values
     */
    private void validateNumericValue(String paramName, Object value, String dataType) {
        // Check if it's a number
        if (!(value instanceof Number) && !(value instanceof String)) {
            throw new ValidationException(
                    String.format("Parameter '%s' should be of type Number, but got %s",
                            paramName, value.getClass().getSimpleName())
            );
        }

        // If it's a string, try to parse it as a number
        BigDecimal numericValue = null;
        if (value instanceof String) {
            try {
                numericValue = new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                throw new ValidationException(
                        String.format("Parameter '%s' should be a valid number, but got '%s'",
                                paramName, value)
                );
            }
        } else if (value instanceof Number) {
            numericValue = new BigDecimal(((Number) value).toString());
        }

        // Additional validation for integer types
        if (dataType.contains("int") && !dataType.contains("decimal") && !dataType.contains("numeric")) {
            if (numericValue != null && numericValue.scale() > 0) {
                throw new ValidationException(
                        String.format("Parameter '%s' should be an integer, but got decimal value: %s",
                                paramName, numericValue)
                );
            }
        }

        // Additional validation for real/float types
        if (dataType.contains("real") || dataType.contains("float")) {
            if (numericValue != null) {
                float floatValue = numericValue.floatValue();
                if (Float.isInfinite(floatValue) || Float.isNaN(floatValue)) {
                    throw new ValidationException(
                            String.format("Parameter '%s' must be a valid finite float value", paramName)
                    );
                }
            }
        }
    }

    /**
     * Validates date/time format
     */
    private void validateDateTimeFormat(String paramName, String value, String dataType) {
        List<String> dateFormats = Arrays.asList(
                "yyyy-MM-dd",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "MM/dd/yyyy",
                "dd/MM/yyyy",
                "yyyy/MM/dd",
                "HH:mm:ss",
                "HH:mm:ss.SSS"
        );

        boolean valid = false;
        for (String format : dateFormats) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
                sdf.setLenient(false);
                sdf.parse(value);
                valid = true;
                break;
            } catch (java.text.ParseException e) {
                // Try next format
            }
        }

        if (!valid) {
            throw new ValidationException(
                    String.format("Parameter '%s' must be a valid date/time. Supported formats include: yyyy-MM-dd, yyyy-MM-dd HH:mm:ss, etc.",
                            paramName)
            );
        }
    }

    /**
     * Validates boolean values
     */
    private void validateBooleanValue(String paramName, Object value) {
        if (value instanceof Boolean) {
            return;
        }

        if (value instanceof String) {
            String strValue = ((String) value).toLowerCase();
            Set<String> validBooleans = new HashSet<>(Arrays.asList(
                    "true", "false", "1", "0", "yes", "no", "y", "n", "on", "off"
            ));
            if (!validBooleans.contains(strValue)) {
                throw new ValidationException(
                        String.format("Parameter '%s' must be a boolean value (true/false, 1/0, yes/no, y/n, on/off), but got '%s'",
                                paramName, value)
                );
            }
            return;
        }

        throw new ValidationException(
                String.format("Parameter '%s' should be a boolean, but got %s",
                        paramName, value.getClass().getSimpleName())
        );
    }

    /**
     * Validates binary values
     */
    private void validateBinaryValue(String paramName, Object value) {
        if (value instanceof byte[]) {
            log.debug("Parameter '{}' validated as binary data, length: {} bytes",
                    paramName, ((byte[]) value).length);
            return;
        }

        if (value instanceof String) {
            String strValue = (String) value;
            // Check if it's valid base64 or hex
            try {
                java.util.Base64.getDecoder().decode(strValue);
                log.debug("Parameter '{}' validated as base64 encoded binary data", paramName);
                return;
            } catch (IllegalArgumentException e) {
                // Not valid base64, try hex
                if (strValue.matches("^[0-9a-fA-F]+$")) {
                    log.debug("Parameter '{}' validated as hex encoded binary data", paramName);
                    return;
                }
            }
            throw new ValidationException(
                    String.format("Parameter '%s' must be valid base64 or hex encoded binary data", paramName)
            );
        }

        throw new ValidationException(
                String.format("Parameter '%s' should be binary data (byte array, base64 string, or hex string), but got %s",
                        paramName, value.getClass().getSimpleName())
        );
    }

    /**
     * Validates JSON format
     */
    private void validateJsonFormat(String paramName, String value) {
        String trimmed = value.trim();

        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            throw new ValidationException(
                    String.format("Parameter '%s' must be valid JSON (should start with { or [)", paramName)
            );
        }

        // Basic JSON validation - check balanced braces/brackets
        int braceCount = 0;
        int bracketCount = 0;
        boolean inQuotes = false;

        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '"' && (i == 0 || trimmed.charAt(i-1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (!inQuotes) {
                if (c == '{') braceCount++;
                else if (c == '}') braceCount--;
                else if (c == '[') bracketCount++;
                else if (c == ']') bracketCount--;
            }
        }

        if (braceCount != 0 || bracketCount != 0) {
            throw new ValidationException(
                    String.format("Parameter '%s' must be valid JSON (unbalanced brackets/braces)", paramName)
            );
        }
    }

    /**
     * Validates UUID format
     */
    private void validateUuidFormat(String paramName, String value) {
        String uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

        if (!value.matches(uuidPattern)) {
            throw new ValidationException(
                    String.format("Parameter '%s' must be a valid UUID (format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)",
                            paramName)
            );
        }
    }

    /**
     * Validates network address format (inet/cidr)
     */
    private void validateNetworkFormat(String paramName, String value) {
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(/([0-9]|[1-2][0-9]|3[0-2]))?$";
        String ipv6Pattern = "^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))(/([0-9]|[1-9][0-9]|1[0-1][0-9]|12[0-8]))?$";

        if (!value.matches(ipv4Pattern) && !value.matches(ipv6Pattern)) {
            throw new ValidationException(
                    String.format("Parameter '%s' must be a valid IP address (IPv4 or IPv6) with optional CIDR mask",
                            paramName)
            );
        }
    }

    /**
     * Validates MAC address format
     */
    private void validateMacAddrFormat(String paramName, String value) {
        String macPattern = "^([0-9a-fA-F]{2}[:-]){5}([0-9a-fA-F]{2})$";

        if (!value.matches(macPattern)) {
            throw new ValidationException(
                    String.format("Parameter '%s' must be a valid MAC address (format: xx:xx:xx:xx:xx:xx or xx-xx-xx-xx-xx-xx)",
                            paramName)
            );
        }
    }
}