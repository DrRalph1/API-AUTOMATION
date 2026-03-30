package com.usg.apiGeneration.utils.apiEngine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Types;

/**
 * Utility class for mapping Oracle data types to various target types
 * including API types, Java types, SQL types, and language-specific types.
 */
@Slf4j
@Component
public class OracleTypeMapper {

    /**
     * Maps Oracle database type to API type (used in OpenAPI/Swagger specifications)
     *
     * @param oracleType The Oracle data type (e.g., VARCHAR2, NUMBER, DATE)
     * @return The corresponding API type (string, integer, etc.)
     */
    public String mapToApiType(String oracleType) {
        if (oracleType == null) return "string";

        String upperType = oracleType.toUpperCase();

        if (upperType.contains("VARCHAR") ||
                upperType.contains("CHAR") ||
                upperType.contains("CLOB") ||
                upperType.contains("LONG")) {
            return "string";
        }
        else if (upperType.contains("NUMBER") ||
                upperType.contains("INTEGER") ||
                upperType.contains("FLOAT") ||
                upperType.contains("DECIMAL") ||
                upperType.contains("NUMERIC") ||
                upperType.contains("BINARY_FLOAT") ||
                upperType.contains("BINARY_DOUBLE")) {
            return "number";
        }
        else if (upperType.contains("DATE") ||
                upperType.contains("TIMESTAMP")) {
            return "string"; // Dates are represented as strings in JSON/OpenAPI
        }
        else if (upperType.contains("BLOB") ||
                upperType.contains("RAW") ||
                upperType.contains("LONG RAW") ||
                upperType.contains("BFILE")) {
            return "string"; // Binary data as string (base64 encoded)
        }
        else if (upperType.contains("BOOLEAN")) {
            return "boolean";
        }
        else if (upperType.contains("ROWID") ||
                upperType.contains("UROWID")) {
            return "string";
        }
        else if (upperType.contains("XMLTYPE")) {
            return "string";
        }

        // Default fallback
        return "string";
    }

    /**
     * Maps Oracle database type to a format string (used in OpenAPI specifications)
     *
     * @param oracleType The Oracle data type
     * @return The format specifier (date, date-time, int32, int64, etc.)
     */
    public String mapToFormat(String oracleType) {
        if (oracleType == null) return null;

        String upperType = oracleType.toUpperCase();

        if (upperType.contains("DATE")) {
            return "date";
        }
        else if (upperType.contains("TIMESTAMP")) {
            return "date-time";
        }
        else if (upperType.contains("NUMBER")) {
            // Check if it's likely an integer based on scale/precision
            // This is a simplification - actual scale/precision would require additional info
            return "double";
        }
        else if (upperType.contains("INTEGER") ||
                upperType.contains("INT") ||
                upperType.contains("SMALLINT")) {
            return "int32";
        }
        else if (upperType.contains("FLOAT") ||
                upperType.contains("BINARY_FLOAT") ||
                upperType.contains("BINARY_DOUBLE")) {
            return "float";
        }
        else if (upperType.contains("DECIMAL") ||
                upperType.contains("NUMERIC")) {
            return "double";
        }

        return null;
    }

    /**
     * Maps Oracle database type to a standard Oracle type name
     *
     * @param dataType The database data type
     * @return Standardized Oracle type name
     */
    public String mapOracleType(String dataType) {
        if (dataType == null) return "VARCHAR2";

        String upperType = dataType.toUpperCase();

        if (upperType.contains("VARCHAR")) return "VARCHAR2";
        if (upperType.contains("CHAR")) return "CHAR";
        if (upperType.contains("NCHAR")) return "NCHAR";
        if (upperType.contains("NVARCHAR")) return "NVARCHAR2";
        if (upperType.contains("CLOB")) return "CLOB";
        if (upperType.contains("NCLOB")) return "NCLOB";
        if (upperType.contains("NUMBER")) return "NUMBER";
        if (upperType.contains("INTEGER")) return "NUMBER";
        if (upperType.contains("INT")) return "NUMBER";
        if (upperType.contains("FLOAT")) return "NUMBER";
        if (upperType.contains("DECIMAL")) return "NUMBER";
        if (upperType.contains("NUMERIC")) return "NUMBER";
        if (upperType.contains("BINARY_FLOAT")) return "BINARY_FLOAT";
        if (upperType.contains("BINARY_DOUBLE")) return "BINARY_DOUBLE";
        if (upperType.contains("DATE")) return "DATE";
        if (upperType.contains("TIMESTAMP")) return "TIMESTAMP";
        if (upperType.contains("BLOB")) return "BLOB";
        if (upperType.contains("RAW")) return "RAW";
        if (upperType.contains("LONG")) return "LONG";
        if (upperType.contains("XML")) return "XMLTYPE";
        if (upperType.contains("BOOLEAN")) return "BOOLEAN";
        if (upperType.contains("ROWID")) return "ROWID";

        return "VARCHAR2";
    }

    /**
     * Maps Oracle type to java.sql.Types constant for JDBC operations
     *
     * @param oracleType The Oracle data type
     * @return The corresponding java.sql.Types constant
     */
    public int mapToSqlType(String oracleType) {
        if (oracleType == null) return Types.VARCHAR;

        String upperType = oracleType.toUpperCase();

        if (upperType.contains("VARCHAR")) return Types.VARCHAR;
        if (upperType.contains("CHAR")) return Types.CHAR;
        if (upperType.contains("NCHAR")) return Types.NCHAR;
        if (upperType.contains("NVARCHAR")) return Types.NVARCHAR;
        if (upperType.contains("CLOB")) return Types.CLOB;
        if (upperType.contains("NCLOB")) return Types.NCLOB;
        if (upperType.contains("NUMBER") || upperType.contains("NUMERIC")) return Types.NUMERIC;
        if (upperType.contains("INTEGER") || upperType.contains("INT")) return Types.INTEGER;
        if (upperType.contains("FLOAT")) return Types.FLOAT;
        if (upperType.contains("DECIMAL")) return Types.DECIMAL;
        if (upperType.contains("BINARY_FLOAT")) return Types.FLOAT;
        if (upperType.contains("BINARY_DOUBLE")) return Types.DOUBLE;
        if (upperType.contains("DATE")) return Types.DATE;
        if (upperType.contains("TIMESTAMP")) return Types.TIMESTAMP;
        if (upperType.contains("BLOB")) return Types.BLOB;
        if (upperType.contains("RAW")) return Types.VARBINARY;
        if (upperType.contains("LONG")) return Types.LONGVARCHAR;
        if (upperType.contains("BOOLEAN")) return Types.BOOLEAN;
        if (upperType.contains("ROWID")) return Types.VARCHAR;
        if (upperType.contains("XML")) return Types.SQLXML;

        return Types.VARCHAR;
    }

    /**
     * Generates an example value for a given Oracle data type
     *
     * @param oracleType The Oracle data type
     * @return An example value as a string
     */
    public String generateExample(String oracleType) {
        if (oracleType == null) return "";

        String upperType = oracleType.toUpperCase();

        if (upperType.contains("VARCHAR") ||
                upperType.contains("CHAR") ||
                upperType.contains("CLOB") ||
                upperType.contains("LONG")) {
            return "sample";
        }
        else if (upperType.contains("NUMBER") ||
                upperType.contains("INTEGER") ||
                upperType.contains("INT")) {
            return "123";
        }
        else if (upperType.contains("FLOAT") ||
                upperType.contains("DECIMAL") ||
                upperType.contains("NUMERIC") ||
                upperType.contains("BINARY_FLOAT") ||
                upperType.contains("BINARY_DOUBLE")) {
            return "123.45";
        }
        else if (upperType.contains("DATE")) {
            return "2024-01-01";
        }
        else if (upperType.contains("TIMESTAMP")) {
            return "2024-01-01T00:00:00Z";
        }
        else if (upperType.contains("BOOLEAN")) {
            return "true";
        }
        else if (upperType.contains("BLOB") ||
                upperType.contains("RAW") ||
                upperType.contains("LONG RAW")) {
            return "[BASE64_ENCODED_BINARY_DATA]";
        }
        else if (upperType.contains("ROWID")) {
            return "AAAB9FAAFAAAABTAAT";
        }
        else if (upperType.contains("UROWID")) {
            return "AAAB9FAAFAAAABTAAT";
        }
        else if (upperType.contains("XMLTYPE") || upperType.contains("XML")) {
            return "<root>sample</root>";
        }
        else if (upperType.contains("BFILE")) {
            return "/path/to/sample/file.pdf";
        }
        else if (upperType.contains("NCLOB")) {
            return "sample unicode text";
        }
        else if (upperType.contains("NVARCHAR2")) {
            return "sample unicode string";
        }
        else if (upperType.contains("NCHAR")) {
            return "s";
        }

        // Default fallback for any other types
        return "sample";
    }

}