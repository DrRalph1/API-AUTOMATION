package com.usg.autoAPIGenerator.utils.apiEngine;

import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiParameterDTO;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Validates parameters against Oracle database object definitions
 * Handles data type validation, required parameters, length checks, etc.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OracleParameterValidator {

    private final JdbcTemplate oracleJdbcTemplate;

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
     * Fetches parameter definitions from Oracle ALL_ARGUMENTS view
     */
    private List<Map<String, Object>> getDatabaseParameters(String schemaName, String objectName) {
        String sql =
                "SELECT " +
                        "    ARGUMENT_NAME, " +
                        "    DATA_TYPE, " +
                        "    IN_OUT, " +
                        "    POSITION, " +
                        "    DATA_LENGTH, " +
                        "    DATA_PRECISION, " +
                        "    DATA_SCALE, " +
                        "    DEFAULTED, " +
                        "    PLS_TYPE " +
                        "FROM ALL_ARGUMENTS " +
                        "WHERE OWNER = ? AND OBJECT_NAME = ? " +
                        "AND DATA_LEVEL = 0 " +  // Only top-level parameters, not record fields
                        "ORDER BY POSITION";

        try {
            return oracleJdbcTemplate.queryForList(sql, schemaName.toUpperCase(), objectName.toUpperCase());
        } catch (Exception e) {
            log.warn("Could not fetch database parameters: {}", e.getMessage());
            return new ArrayList<>();
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
            if (paramName == null) continue; // Skip unnamed parameters (like RETURN for functions)

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
     * Validates a parameter's value against its Oracle data type
     */
    private void validateDataType(String paramName, Object value, Map<String, Object> dbParam) {
        String dataType = ((String) dbParam.get("DATA_TYPE")).toUpperCase();
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
            // Null is allowed - Oracle will handle based on column nullability
            return;
        }

        // Convert value to string for validation
        String stringValue = value.toString().trim();

        // Validate based on Oracle data types
        switch (dataType) {
            case "VARCHAR2":
            case "VARCHAR":
            case "CHAR":
            case "NCHAR":
            case "NVARCHAR2":
            case "CLOB":
            case "NCLOB":
            case "LONG":
                validateStringType(paramName, stringValue, dataType, length);
                break;

            case "NUMBER":
            case "INTEGER":
            case "INT":
            case "SMALLINT":
            case "DECIMAL":
            case "NUMERIC":
            case "FLOAT":
            case "BINARY_FLOAT":
            case "BINARY_DOUBLE":
            case "REAL":
                validateNumberType(paramName, stringValue, dataType, precision, scale);
                break;

            case "DATE":
            case "TIMESTAMP":
            case "TIMESTAMP WITH TIME ZONE":
            case "TIMESTAMP WITH LOCAL TIME ZONE":
                validateDateType(paramName, stringValue, dataType);
                break;

            case "BOOLEAN":
                validateBooleanType(paramName, stringValue);
                break;

            case "BLOB":
            case "RAW":
            case "LONG RAW":
            case "BFILE":
                validateBinaryType(paramName, value);
                break;

            case "ROWID":
            case "UROWID":
                validateRowIdType(paramName, stringValue);
                break;

            case "XMLTYPE":
                validateXmlType(paramName, stringValue);
                break;

            default:
                log.warn("Unsupported data type '{}' for parameter '{}', skipping validation", dataType, paramName);
                break;
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

        // Additional validation for CHAR types (fixed length)
        if (dataType.equals("CHAR") || dataType.equals("NCHAR")) {
            if (maxLength != null && value.length() > maxLength) {
                throw new ValidationException(
                        String.format("Parameter '%s' for CHAR column must not exceed %d characters. Current length: %d",
                                paramName, maxLength, value.length())
                );
            }
            // Note: Oracle will pad shorter values with spaces, so we don't need to enforce exact length
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
        if (dataType.equals("INTEGER") || dataType.equals("INT") || dataType.equals("SMALLINT")) {
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
        switch (dataType) {
            case "BINARY_FLOAT":
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
                break;

            case "BINARY_DOUBLE":
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
                break;
        }
    }

    /**
     * Validates date/time type parameters
     */
    private void validateDateType(String paramName, String value, String dataType) {
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
                "yyyy-MM-dd HH:mm:ss.SSS"
        );

        boolean valid = false;
        String matchedFormat = null;

        for (String format : dateFormats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false);
                Date date = sdf.parse(value);

                // For TIMESTAMP types, ensure we have time component if required
                if (dataType.startsWith("TIMESTAMP") && dataType.contains("TIME")) {
                    if (format.contains("HH:mm:ss")) {
                        valid = true;
                        matchedFormat = format;
                        break;
                    }
                } else if (dataType.equals("DATE")) {
                    // DATE in Oracle can include time, but we're flexible
                    valid = true;
                    matchedFormat = format;
                    break;
                } else {
                    valid = true;
                    matchedFormat = format;
                    break;
                }
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

        log.debug("Parameter '{}' validated as date with format: {}", paramName, matchedFormat);
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
     * Validates binary type parameters
     */
    private void validateBinaryType(String paramName, Object value) {
        if (value instanceof byte[]) {
            // Valid binary data
            log.debug("Parameter '{}' validated as binary data, length: {} bytes",
                    paramName, ((byte[]) value).length);
        } else if (value instanceof String) {
            // Check if it's a valid base64 string
            String stringValue = (String) value;
            try {
                Base64.getDecoder().decode(stringValue);
                log.debug("Parameter '{}' validated as base64 encoded binary data", paramName);
            } catch (IllegalArgumentException e) {
                throw new ValidationException(
                        String.format("Parameter '%s' must be valid base64 encoded binary data", paramName)
                );
            }
        } else {
            throw new ValidationException(
                    String.format("Parameter '%s' must be binary data (byte array or base64 string)", paramName)
            );
        }
    }

    /**
     * Validates ROWID type parameters
     */
    private void validateRowIdType(String paramName, String value) {
        // Oracle ROWID format: block.row.file (e.g., AAAAB4AABAAABqAAA)
        // Length is typically 18 characters, but can be up to 20 for extended rowids
        String rowidPattern = "^[A-Za-z0-9+/]{18,20}$";

        if (!value.matches(rowidPattern)) {
            throw new ValidationException(
                    String.format("Parameter '%s' must be a valid ROWID format (18-20 alphanumeric characters)",
                            paramName)
            );
        }
    }

    /**
     * Validates XML type parameters
     */
    private void validateXmlType(String paramName, String value) {
        // Basic XML validation - check for root element
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
            String firstTag = trimmed.substring(1, firstClose).split("\\s+")[0]; // Handle attributes
            String closingTag = "</" + firstTag + ">";
            if (!trimmed.contains(closingTag) && !trimmed.endsWith("/>")) {
                log.debug("XML parameter '{}' may be incomplete - missing closing tag for {}",
                        paramName, firstTag);
                // Don't throw - let Oracle validate fully
            }
        }

        log.debug("Parameter '{}' passed basic XML validation", paramName);
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