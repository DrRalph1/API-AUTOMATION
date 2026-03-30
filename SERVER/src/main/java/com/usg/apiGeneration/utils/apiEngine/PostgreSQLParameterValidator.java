package com.usg.apiGeneration.utils.apiEngine;

import com.usg.apiGeneration.dtos.apiGenerationEngine.ApiParameterDTO;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Validates parameters against PostgreSQL database object definitions
 * Handles data type validation, required parameters, length checks, etc.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostgreSQLParameterValidator {

    private final JdbcTemplate postgresqlJdbcTemplate;

    /**
     * Main validation method that validates provided parameters against database object
     *
     * @param configuredParams Parameters configured in the API
     * @param providedParams Parameters provided in the request
     * @param schemaName Database schema name
     * @param objectName Database object name (procedure, function, table, etc.)
     * @throws ValidationException if validation fails
     */
    public void validateParameters(List<ApiParameterDTO> configuredParams,
                                   Map<String, Object> providedParams,
                                   String schemaName,
                                   String objectName) {

        log.debug("Validating parameters for {}.{} with {} provided params",
                schemaName, objectName, providedParams != null ? providedParams.size() : 0);

        // Get actual database parameters
        List<Map<String, Object>> dbParams = getDatabaseParameters(schemaName, objectName);

        // Create a map for quick lookup
        Map<String, Map<String, Object>> dbParamMap = createDbParamMap(dbParams);

        // Validate each provided parameter
        validateProvidedParameters(providedParams, dbParamMap);

        // Check for required parameters that are missing
        validateRequiredParameters(dbParams, providedParams, configuredParams);
    }

    /**
     * Fetches parameter definitions from PostgreSQL system catalogs
     */
    private List<Map<String, Object>> getDatabaseParameters(String schemaName, String objectName) {
        List<Map<String, Object>> parameters = new ArrayList<>();

        try {
            // Get function/procedure OID
            String oidSql = "SELECT p.oid, p.prokind FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ?";

            Map<String, Object> procInfo = null;
            try {
                procInfo = postgresqlJdbcTemplate.queryForMap(oidSql, schemaName, objectName);
            } catch (Exception e) {
                log.warn("Could not find function/procedure {}.{}", schemaName, objectName);
                return parameters;
            }

            Long oid = (Long) procInfo.get("oid");
            String prokind = (String) procInfo.get("prokind");

            if (oid == null) {
                log.warn("Could not get OID for {}.{}", schemaName, objectName);
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

                // Get data length and precision from pg_type
                addTypeAttributes(param, argTypes[i]);

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
     * Add type attributes like length, precision, scale
     */
    private void addTypeAttributes(Map<String, Object> param, String typeOid) {
        try {
            String sql = "SELECT typname, typlen, typmodin, typmodout FROM pg_type WHERE oid = ?::regtype::oid";
            Map<String, Object> typeInfo = postgresqlJdbcTemplate.queryForMap(sql, typeOid);

            String typname = (String) typeInfo.get("typname");

            // Set length for character types
            if (typname.contains("varchar") || typname.contains("char")) {
                // Length is often encoded in typmod
                param.put("DATA_LENGTH", getCharacterLength(typname));
            }
            // Set precision and scale for numeric types
            else if (typname.contains("numeric") || typname.contains("decimal")) {
                param.put("DATA_PRECISION", 38); // Default PostgreSQL precision
                param.put("DATA_SCALE", 0);
            }
            // Set length for other types
            else {
                Integer typLen = (Integer) typeInfo.get("typlen");
                if (typLen != null && typLen > 0) {
                    param.put("DATA_LENGTH", typLen);
                }
            }
        } catch (Exception e) {
            log.debug("Could not get type attributes: {}", e.getMessage());
        }
    }

    /**
     * Get character length from type name
     */
    private Integer getCharacterLength(String typeName) {
        Pattern pattern = Pattern.compile("\\((\\d+)\\)");
        java.util.regex.Matcher matcher = pattern.matcher(typeName);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
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
     * Creates a lookup map of parameter names to their definitions
     */
    private Map<String, Map<String, Object>> createDbParamMap(List<Map<String, Object>> dbParams) {
        Map<String, Map<String, Object>> paramMap = new HashMap<>();

        for (Map<String, Object> param : dbParams) {
            String paramName = (String) param.get("ARGUMENT_NAME");
            if (paramName != null) {
                // Store both lowercase and original case versions for flexible matching
                paramMap.put(paramName.toLowerCase(), param);
                paramMap.put(paramName, param);
            }
        }

        return paramMap;
    }

    /**
     * Validates each provided parameter against its database definition
     */
    private void validateProvidedParameters(Map<String, Object> providedParams,
                                            Map<String, Map<String, Object>> dbParamMap) {
        if (providedParams == null) return;

        for (Map.Entry<String, Object> entry : providedParams.entrySet()) {
            String paramName = entry.getKey();
            Object paramValue = entry.getValue();

            Map<String, Object> dbParam = dbParamMap.get(paramName.toLowerCase());
            if (dbParam != null) {
                validateDataType(paramName, paramValue, dbParam);
            } else {
                log.debug("Parameter '{}' not found in database, skipping type validation", paramName);
            }
        }
    }

    /**
     * Validates that all required parameters are provided
     */
    private void validateRequiredParameters(List<Map<String, Object>> dbParams,
                                            Map<String, Object> providedParams,
                                            List<ApiParameterDTO> configuredParams) {
        if (dbParams == null || dbParams.isEmpty()) return;

        // Create a set of provided parameter names (case-insensitive)
        Set<String> providedParamNames = new HashSet<>();
        if (providedParams != null) {
            providedParams.keySet().forEach(key -> providedParamNames.add(key.toLowerCase()));
        }

        for (Map<String, Object> dbParam : dbParams) {
            String paramName = (String) dbParam.get("ARGUMENT_NAME");
            if (paramName == null) continue; // Skip unnamed parameters

            String inOut = (String) dbParam.get("IN_OUT");
            String defaulted = (String) dbParam.get("DEFAULTED");

            // Skip OUT parameters for input validation
            if ("OUT".equalsIgnoreCase(inOut)) {
                continue;
            }

            // Check if parameter is required (no default value)
            boolean hasDefault = "YES".equalsIgnoreCase(defaulted);

            if (!hasDefault) {
                boolean isProvided = providedParamNames.contains(paramName.toLowerCase());

                // Check if this parameter is marked as required in API configuration
                ApiParameterDTO configParam = findConfiguredParameter(configuredParams, paramName);

                if (!isProvided && configParam != null && Boolean.TRUE.equals(configParam.getRequired())) {
                    throw new ValidationException(
                            String.format("Required parameter '%s' is missing", paramName)
                    );
                }
            }
        }
    }

    /**
     * Finds a configured parameter by name (case-insensitive)
     */
    private ApiParameterDTO findConfiguredParameter(List<ApiParameterDTO> configuredParams, String paramName) {
        if (configuredParams == null || configuredParams.isEmpty()) return null;

        return configuredParams.stream()
                .filter(p -> p.getKey() != null && p.getKey().equalsIgnoreCase(paramName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Validates a parameter's value against its PostgreSQL data type
     */
    private void validateDataType(String paramName, Object value, Map<String, Object> dbParam) {
        String dataType = ((String) dbParam.get("DATA_TYPE")).toLowerCase();
        Integer length = dbParam.get("DATA_LENGTH") != null ?
                ((Number) dbParam.get("DATA_LENGTH")).intValue() : null;
        Integer precision = dbParam.get("DATA_PRECISION") != null ?
                ((Number) dbParam.get("DATA_PRECISION")).intValue() : null;
        Integer scale = dbParam.get("DATA_SCALE") != null ?
                ((Number) dbParam.get("DATA_SCALE")).intValue() : null;
        String inOut = (String) dbParam.get("IN_OUT");

        // Skip validation for OUT parameters
        if ("OUT".equalsIgnoreCase(inOut)) {
            return;
        }

        // Handle null values
        if (value == null) {
            return;
        }

        // Convert value to string for validation
        String stringValue = value.toString().trim();

        // Validate based on PostgreSQL data types
        if (dataType.contains("varchar") || dataType.contains("char") ||
                dataType.contains("text") || dataType.contains("citext")) {
            validateStringType(paramName, stringValue, dataType, length);
        }
        else if (dataType.contains("int") || dataType.contains("integer") ||
                dataType.contains("smallint") || dataType.contains("bigint") ||
                dataType.contains("decimal") || dataType.contains("numeric") ||
                dataType.contains("real") || dataType.contains("double") ||
                dataType.contains("float")) {
            validateNumberType(paramName, stringValue, dataType, precision, scale);
        }
        else if (dataType.contains("date") || dataType.contains("time") ||
                dataType.contains("timestamp") || dataType.contains("interval")) {
            validateDateTimeType(paramName, stringValue, dataType);
        }
        else if (dataType.contains("bool")) {
            validateBooleanType(paramName, stringValue);
        }
        else if (dataType.contains("bytea") || dataType.contains("bit")) {
            validateBinaryType(paramName, value);
        }
        else if (dataType.contains("json") || dataType.contains("jsonb")) {
            validateJsonType(paramName, stringValue);
        }
        else if (dataType.contains("xml")) {
            validateXmlType(paramName, stringValue);
        }
        else if (dataType.contains("uuid")) {
            validateUuidType(paramName, stringValue);
        }
        else if (dataType.contains("inet") || dataType.contains("cidr")) {
            validateNetworkType(paramName, stringValue);
        }
        else if (dataType.contains("macaddr")) {
            validateMacAddrType(paramName, stringValue);
        }
        else {
            log.warn("Unsupported data type '{}' for parameter '{}', skipping validation", dataType, paramName);
        }
    }

    /**
     * Validates string type parameters
     */
    private void validateStringType(String paramName, String value, String dataType, Integer maxLength) {
        // Check if value exceeds maximum length
        if (maxLength != null && maxLength > 0 && value.length() > maxLength) {
            throw new ValidationException(
                    String.format("Parameter '%s' exceeds maximum length of %d characters. Current length: %d",
                            paramName, maxLength, value.length())
            );
        }

        // Check for invalid characters (control characters except common whitespace)
        if (containsInvalidCharacters(value)) {
            throw new ValidationException(
                    String.format("Parameter '%s' contains invalid control characters", paramName)
            );
        }
    }

    /**
     * Validates numeric type parameters
     */
    private void validateNumberType(String paramName, String value, String dataType,
                                    Integer precision, Integer scale) {
        BigDecimal number;
        try {
            // Replace comma with dot for decimal separator if needed
            String normalizedValue = value.replace(',', '.');
            number = new BigDecimal(normalizedValue);
        } catch (NumberFormatException e) {
            throw new ValidationException(
                    String.format("Parameter '%s' must be a valid number. Provided value: '%s'",
                            paramName, value)
            );
        }

        // Check integer types
        if (dataType.contains("int") && !dataType.contains("decimal") && !dataType.contains("numeric")) {
            try {
                number.toBigIntegerExact(); // Check if it's an exact integer
            } catch (ArithmeticException e) {
                throw new ValidationException(
                        String.format("Parameter '%s' must be an integer value. Provided: %s",
                                paramName, value)
                );
            }
        }

        // Validate precision and scale if specified
        if (precision != null && precision > 0) {
            int integerPartLength = number.precision() - number.scale();
            int maxIntegerLength = precision - (scale != null ? scale : 0);

            if (integerPartLength > maxIntegerLength) {
                throw new ValidationException(
                        String.format("Parameter '%s' integer part length (%d) exceeds maximum allowed (%d)",
                                paramName, integerPartLength, maxIntegerLength)
                );
            }
        }

        if (scale != null && scale > 0) {
            if (number.scale() > scale) {
                throw new ValidationException(
                        String.format("Parameter '%s' decimal places (%d) exceed maximum allowed (%d)",
                                paramName, number.scale(), scale)
                );
            }
        }

        // Range validation for specific numeric types
        if (dataType.contains("real")) {
            try {
                float fValue = Float.parseFloat(value);
                if (Float.isInfinite(fValue) || Float.isNaN(fValue)) {
                    throw new ValidationException(
                            String.format("Parameter '%s' must be a valid finite float value", paramName)
                    );
                }
            } catch (NumberFormatException e) {
                throw new ValidationException(
                        String.format("Parameter '%s' must be a valid float value", paramName)
                );
            }
        }
        else if (dataType.contains("double")) {
            try {
                double dValue = Double.parseDouble(value);
                if (Double.isInfinite(dValue) || Double.isNaN(dValue)) {
                    throw new ValidationException(
                            String.format("Parameter '%s' must be a valid finite double value", paramName)
                    );
                }
            } catch (NumberFormatException e) {
                throw new ValidationException(
                        String.format("Parameter '%s' must be a valid double value", paramName)
                );
            }
        }
    }

    /**
     * Validates date/time type parameters
     */
    private void validateDateTimeType(String paramName, String value, String dataType) {
        List<String> dateFormats = Arrays.asList(
                "yyyy-MM-dd",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "dd-MMM-yyyy",
                "dd-MMM-yy",
                "MM/dd/yyyy",
                "MM/dd/yyyy HH:mm:ss",
                "dd/MM/yyyy",
                "dd/MM/yyyy HH:mm:ss",
                "yyyy/MM/dd",
                "yyyy/MM/dd HH:mm:ss",
                "yyyyMMdd",
                "yyyy-MM-dd HH:mm:ss.SSS",
                "HH:mm:ss",
                "HH:mm:ss.SSS"
        );

        boolean valid = false;
        String matchedFormat = null;

        for (String format : dateFormats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false);
                sdf.parse(value);
                valid = true;
                matchedFormat = format;
                break;
            } catch (ParseException e) {
                // Try next format
            }
        }

        if (!valid) {
            throw new ValidationException(
                    String.format("Parameter '%s' must be a valid date/time. Supported formats include: %s",
                            paramName, "yyyy-MM-dd, yyyy-MM-dd HH:mm:ss, etc.")
            );
        }

        log.debug("Parameter '{}' validated as date/time with format: {}", paramName, matchedFormat);
    }

    /**
     * Validates boolean type parameters
     */
    private void validateBooleanType(String paramName, String value) {
        String lowerValue = value.toLowerCase().trim();

        if (!lowerValue.equals("true") && !lowerValue.equals("false") &&
                !lowerValue.equals("1") && !lowerValue.equals("0") &&
                !lowerValue.equals("yes") && !lowerValue.equals("no") &&
                !lowerValue.equals("y") && !lowerValue.equals("n") &&
                !lowerValue.equals("on") && !lowerValue.equals("off")) {

            throw new ValidationException(
                    String.format("Parameter '%s' must be a boolean value (true/false, 1/0, yes/no, y/n, on/off)",
                            paramName)
            );
        }
    }

    /**
     * Validates binary type parameters (bytea)
     */
    private void validateBinaryType(String paramName, Object value) {
        if (value instanceof byte[]) {
            log.debug("Parameter '{}' validated as binary data, length: {} bytes",
                    paramName, ((byte[]) value).length);
        } else if (value instanceof String) {
            // Check if it's a valid base64 string or hex string
            String stringValue = (String) value;
            try {
                // Try base64
                Base64.getDecoder().decode(stringValue);
                log.debug("Parameter '{}' validated as base64 encoded binary data", paramName);
            } catch (IllegalArgumentException e) {
                // Try hex
                if (stringValue.matches("^[0-9a-fA-F]+$")) {
                    log.debug("Parameter '{}' validated as hex encoded binary data", paramName);
                } else {
                    throw new ValidationException(
                            String.format("Parameter '%s' must be valid base64 or hex encoded binary data", paramName)
                    );
                }
            }
        } else {
            throw new ValidationException(
                    String.format("Parameter '%s' must be binary data (byte array, base64 string, or hex string)", paramName)
            );
        }
    }

    /**
     * Validates JSON type parameters
     */
    private void validateJsonType(String paramName, String value) {
        String trimmed = value.trim();

        // Check if it's a valid JSON object or array
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            throw new ValidationException(
                    String.format("Parameter '%s' must be valid JSON (should start with { or [)", paramName)
            );
        }

        // Basic JSON validation - try to parse with simple pattern
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

        log.debug("Parameter '{}' passed basic JSON validation", paramName);
    }

    /**
     * Validates XML type parameters
     */
    private void validateXmlType(String paramName, String value) {
        String trimmed = value.trim();

        if (!trimmed.startsWith("<") || !trimmed.endsWith(">")) {
            throw new ValidationException(
                    String.format("Parameter '%s' must be valid XML (should start with < and end with >)",
                            paramName)
            );
        }

        // Check for matching tags (simple validation)
        int firstClose = trimmed.indexOf('>');
        if (firstClose > 0) {
            String firstTag = trimmed.substring(1, firstClose).split("\\s+")[0];
            String closingTag = "</" + firstTag + ">";
            if (!trimmed.contains(closingTag) && !trimmed.endsWith("/>")) {
                log.debug("XML parameter '{}' may be incomplete - missing closing tag for {}",
                        paramName, firstTag);
            }
        }
    }

    /**
     * Validates UUID type parameters
     */
    private void validateUuidType(String paramName, String value) {
        String uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

        if (!value.matches(uuidPattern)) {
            throw new ValidationException(
                    String.format("Parameter '%s' must be a valid UUID (format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)",
                            paramName)
            );
        }
    }

    /**
     * Validates network address type parameters (inet, cidr)
     */
    private void validateNetworkType(String paramName, String value) {
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
     * Validates MAC address type parameters
     */
    private void validateMacAddrType(String paramName, String value) {
        String macPattern = "^([0-9a-fA-F]{2}[:-]){5}([0-9a-fA-F]{2})$";

        if (!value.matches(macPattern)) {
            throw new ValidationException(
                    String.format("Parameter '%s' must be a valid MAC address (format: xx:xx:xx:xx:xx:xx or xx-xx-xx-xx-xx-xx)",
                            paramName)
            );
        }
    }

    /**
     * Checks if a string contains invalid control characters
     */
    private boolean containsInvalidCharacters(String value) {
        // Define invalid characters - control characters except common whitespace
        // Allow: tab (0x09), newline (0x0A), carriage return (0x0D)
        return value.chars().anyMatch(c ->
                c < 0x20 && c != 0x09 && c != 0x0A && c != 0x0D
        );
    }

    /**
     * Validates that a value matches a specific pattern (if configured)
     */
    public void validatePattern(String paramName, String value, String pattern) {
        if (pattern == null || pattern.isEmpty()) return;

        if (!value.matches(pattern)) {
            throw new ValidationException(
                    String.format("Parameter '%s' must match pattern: %s", paramName, pattern)
            );
        }
    }

    /**
     * Validates that a value is within a specific length range
     */
    public void validateLength(String paramName, String value, Integer minLength, Integer maxLength) {
        if (value == null) return;

        int length = value.length();

        if (minLength != null && length < minLength) {
            throw new ValidationException(
                    String.format("Parameter '%s' length (%d) is less than minimum (%d)",
                            paramName, length, minLength)
            );
        }

        if (maxLength != null && length > maxLength) {
            throw new ValidationException(
                    String.format("Parameter '%s' length (%d) exceeds maximum (%d)",
                            paramName, length, maxLength)
            );
        }
    }

    /**
     * Validates that a numeric value is within a specific range
     */
    public void validateRange(String paramName, Number value, Number min, Number max) {
        if (value == null) return;

        double doubleValue = value.doubleValue();

        if (min != null && doubleValue < min.doubleValue()) {
            throw new ValidationException(
                    String.format("Parameter '%s' value (%s) is less than minimum (%s)",
                            paramName, value, min)
            );
        }

        if (max != null && doubleValue > max.doubleValue()) {
            throw new ValidationException(
                    String.format("Parameter '%s' value (%s) exceeds maximum (%s)",
                            paramName, value, max)
            );
        }
    }

    /**
     * Validates that a value is one of the allowed values
     */
    public void validateEnum(String paramName, String value, List<String> allowedValues) {
        if (value == null || allowedValues == null || allowedValues.isEmpty()) return;

        if (!allowedValues.contains(value)) {
            throw new ValidationException(
                    String.format("Parameter '%s' value '%s' is not allowed. Allowed values: %s",
                            paramName, value, String.join(", ", allowedValues))
            );
        }
    }
}