package com.usg.apiAutomation.utils.apiEngine;

import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiParameterDTO;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParameterValidatorUtil {

    private final JdbcTemplate oracleJdbcTemplate;

    public void validateParameters(List<ApiParameterDTO> configuredParams,
                                   Map<String, Object> providedParams,
                                   String schemaName,
                                   String objectName) {
        // Get actual database parameters
        String sql =
                "SELECT ARGUMENT_NAME, DATA_TYPE, IN_OUT, POSITION, " +
                        "DATA_LENGTH, DATA_PRECISION, DATA_SCALE, DEFAULTED " +
                        "FROM ALL_ARGUMENTS " +
                        "WHERE OWNER = ? AND OBJECT_NAME = ? " +
                        "AND DATA_LEVEL = 0 " +
                        "ORDER BY POSITION";

        List<Map<String, Object>> dbParams = oracleJdbcTemplate.queryForList(sql, schemaName, objectName);

        // Create a map for quick lookup
        Map<String, Map<String, Object>> dbParamMap = dbParams.stream()
                .collect(Collectors.toMap(
                        p -> ((String) p.get("ARGUMENT_NAME")).toLowerCase(),
                        p -> p,
                        (existing, replacement) -> existing
                ));

        // Validate each provided parameter
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

        // Check for required parameters that are missing
        for (Map<String, Object> dbParam : dbParams) {
            String paramName = (String) dbParam.get("ARGUMENT_NAME");
            String inOut = (String) dbParam.get("IN_OUT");
            String defaulted = (String) dbParam.get("DEFAULTED");

            // Skip OUT parameters
            if ("OUT".equalsIgnoreCase(inOut)) {
                continue;
            }

            // Check if parameter is required (no default value)
            if (!"YES".equalsIgnoreCase(defaulted)) {
                boolean isProvided = providedParams.containsKey(paramName) ||
                        providedParams.containsKey(paramName.toLowerCase());

                // Check if this parameter is marked as required in your configuration
                ApiParameterDTO configParam = configuredParams.stream()
                        .filter(p -> p.getKey().equalsIgnoreCase(paramName))
                        .findFirst()
                        .orElse(null);

                if (!isProvided && configParam != null && configParam.getRequired()) {
                    throw new ValidationException(
                            String.format("Required parameter '%s' is missing", paramName)
                    );
                }
            }
        }
    }

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
            return; // Null is allowed unless specified otherwise
        }

        String stringValue = value.toString().trim();

        // Validate based on Oracle data types
        switch (dataType) {
            case "VARCHAR2":
            case "VARCHAR":
            case "CHAR":
            case "NCHAR":
            case "NVARCHAR2":
            case "CLOB":
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

            default:
                log.warn("Unsupported data type '{}' for parameter '{}', skipping validation", dataType, paramName);
                break;
        }
    }

    private void validateStringType(String paramName, String value, String dataType, Integer maxLength) {
        // Check if value exceeds maximum length
        if (maxLength != null && value.length() > maxLength) {
            throw new ValidationException(
                    String.format("Parameter '%s' exceeds maximum length of %d characters. Current length: %d",
                            paramName, maxLength, value.length())
            );
        }

        // Check for invalid characters
        if (containsInvalidCharacters(value)) {
            throw new ValidationException(
                    String.format("Parameter '%s' contains invalid characters", paramName)
            );
        }
    }

    private void validateNumberType(String paramName, String value, String dataType,
                                    Integer precision, Integer scale) {
        BigDecimal number;
        try {
            number = new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new ValidationException(
                    String.format("Parameter '%s' must be a valid number. Provided value: '%s'",
                            paramName, value)
            );
        }

        // Check integer types
        if (dataType.equals("INTEGER") || dataType.equals("INT") || dataType.equals("SMALLINT")) {
            try {
                number.toBigIntegerExact(); // Check if it's an integer
            } catch (ArithmeticException e) {
                throw new ValidationException(
                        String.format("Parameter '%s' must be an integer value. Provided: %s",
                                paramName, value)
                );
            }
        }

        // Validate precision and scale
        if (precision != null) {
            int integerPartLength = number.precision() - number.scale();
            int maxIntegerLength = precision - (scale != null ? scale : 0);

            if (integerPartLength > maxIntegerLength) {
                throw new ValidationException(
                        String.format("Parameter '%s' integer part length (%d) exceeds maximum allowed (%d)",
                                paramName, integerPartLength, maxIntegerLength)
                );
            }
        }

        if (scale != null) {
            if (number.scale() > scale) {
                throw new ValidationException(
                        String.format("Parameter '%s' decimal places (%d) exceed maximum allowed (%d)",
                                paramName, number.scale(), scale)
                );
            }
        }
    }

    private void validateDateType(String paramName, String value, String dataType) {
        List<String> dateFormats = Arrays.asList(
                "yyyy-MM-dd",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "dd-MMM-yyyy",
                "MM/dd/yyyy",
                "dd/MM/yyyy",
                "yyyy/MM/dd"
        );

        boolean valid = false;
        for (String format : dateFormats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false);
                Date date = sdf.parse(value);

                // Additional validation for TIMESTAMP types
                if (dataType.startsWith("TIMESTAMP")) {
                    // Check if time portion is included
                    if (format.contains("HH:mm:ss") && value.contains(":")) {
                        valid = true;
                        break;
                    } else if (!dataType.startsWith("TIMESTAMP") && !format.contains("HH:mm:ss")) {
                        valid = true;
                        break;
                    }
                } else {
                    valid = true;
                    break;
                }
            } catch (ParseException e) {
                // Try next format
            }
        }

        if (!valid) {
            throw new ValidationException(
                    String.format("Parameter '%s' must be a valid date/time. Supported formats: %s",
                            paramName, String.join(", ", dateFormats))
            );
        }
    }

    private void validateBooleanType(String paramName, String value) {
        String lowerValue = value.toLowerCase().trim();
        if (!lowerValue.equals("true") && !lowerValue.equals("false") &&
                !lowerValue.equals("1") && !lowerValue.equals("0") &&
                !lowerValue.equals("yes") && !lowerValue.equals("no") &&
                !lowerValue.equals("y") && !lowerValue.equals("n")) {
            throw new ValidationException(
                    String.format("Parameter '%s' must be a boolean value (true/false, 1/0, yes/no, y/n)",
                            paramName)
            );
        }
    }

    private boolean containsInvalidCharacters(String value) {
        // Define invalid characters - control characters except tabs and newlines
        return value.chars().anyMatch(c ->
                c < 0x20 && c != 0x09 && c != 0x0A && c != 0x0D
        );
    }
}