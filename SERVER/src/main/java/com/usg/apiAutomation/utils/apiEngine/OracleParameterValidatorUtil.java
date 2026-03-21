package com.usg.apiAutomation.utils.apiEngine.oracle;

import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiParameterDTO;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OracleParameterValidatorUtil {

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    public void validateParameters(List<ApiParameterDTO> configuredParams,
                                   Map<String, Object> providedParams,
                                   String owner,
                                   String procedureName) {

        log.info("Validating parameters for {}.{}", owner, procedureName);

        // Get actual parameter definitions from Oracle
        String sql = "SELECT ARGUMENT_NAME, DATA_TYPE, IN_OUT, POSITION, " +
                "DATA_LENGTH, DATA_PRECISION, DATA_SCALE, DEFAULTED " +
                "FROM ALL_ARGUMENTS " +
                "WHERE OWNER = ? AND OBJECT_NAME = ? AND DATA_LEVEL = 0 " +
                "ORDER BY POSITION";

        try {
            List<Map<String, Object>> actualParams = oracleJdbcTemplate.queryForList(
                    sql, owner, procedureName);

            log.info("Found {} parameters for {}.{}", actualParams.size(), owner, procedureName);

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
                if (!providedParams.containsKey(paramName) &&
                        !providedParams.containsKey(paramName.toLowerCase())) {

                    log.error("Required parameter '{}' not provided", paramName);
                    throw new ValidationException(
                            String.format("Required parameter '%s' not provided", paramName)
                    );
                }
            }

            // Validate data types (basic validation)
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

            log.info("✅ Parameter validation passed for {}.{}", owner, procedureName);

        } catch (Exception e) {
            if (e instanceof ValidationException) {
                throw e;
            }
            log.error("Error validating parameters: {}", e.getMessage(), e);
            throw new ValidationException("Error validating parameters: " + e.getMessage());
        }
    }

    private void validateDataType(String paramName, Object value, String expectedType) {
        String upperType = expectedType.toUpperCase();

        if (upperType.contains("VARCHAR") || upperType.contains("CHAR")) {
            if (!(value instanceof String)) {
                throw new ValidationException(
                        String.format("Parameter '%s' should be of type String, but got %s",
                                paramName, value.getClass().getSimpleName())
                );
            }
        } else if (upperType.contains("NUMBER") || upperType.contains("NUMERIC") ||
                upperType.contains("INTEGER")) {
            if (!(value instanceof Number) && !(value instanceof String)) {
                throw new ValidationException(
                        String.format("Parameter '%s' should be of type Number, but got %s",
                                paramName, value.getClass().getSimpleName())
                );
            }
            // If it's a string, try to parse it as a number
            if (value instanceof String) {
                try {
                    Double.parseDouble((String) value);
                } catch (NumberFormatException e) {
                    throw new ValidationException(
                            String.format("Parameter '%s' should be a valid number, but got '%s'",
                                    paramName, value)
                    );
                }
            }
        } else if (upperType.contains("DATE") || upperType.contains("TIMESTAMP")) {
            if (!(value instanceof String)) {
                throw new ValidationException(
                        String.format("Parameter '%s' should be a date string, but got %s",
                                paramName, value.getClass().getSimpleName())
                );
            }
            // Date format validation could be added here
        }
    }
}