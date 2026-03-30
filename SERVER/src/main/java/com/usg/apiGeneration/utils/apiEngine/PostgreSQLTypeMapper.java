package com.usg.apiGeneration.utils.apiEngine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Types;

/**
 * Utility class for mapping PostgreSQL data types to various target types
 * including API types, Java types, SQL types, and language-specific types.
 */
@Slf4j
@Component
public class PostgreSQLTypeMapper {

    /**
     * Maps PostgreSQL database type to API type (used in OpenAPI/Swagger specifications)
     *
     * @param pgType The PostgreSQL data type (e.g., varchar, integer, timestamp)
     * @return The corresponding API type (string, integer, etc.)
     */
    public String mapToApiType(String pgType) {
        if (pgType == null) return "string";

        String lowerType = pgType.toLowerCase();

        // String types
        if (lowerType.contains("varchar") ||
                lowerType.contains("char") ||
                lowerType.contains("text") ||
                lowerType.contains("citext") ||
                lowerType.contains("json") ||
                lowerType.contains("jsonb") ||
                lowerType.contains("xml") ||
                lowerType.contains("uuid") ||
                lowerType.contains("inet") ||
                lowerType.contains("cidr") ||
                lowerType.contains("macaddr") ||
                lowerType.contains("name") ||
                lowerType.contains("bpchar")) {
            return "string";
        }
        // Numeric types
        else if (lowerType.contains("int") ||
                lowerType.contains("smallint") ||
                lowerType.contains("bigint") ||
                lowerType.contains("integer") ||
                lowerType.contains("serial") ||
                lowerType.contains("bigserial") ||
                lowerType.contains("smallserial")) {
            return "integer";
        }
        // Floating point types
        else if (lowerType.contains("decimal") ||
                lowerType.contains("numeric") ||
                lowerType.contains("real") ||
                lowerType.contains("double") ||
                lowerType.contains("float")) {
            return "number";
        }
        // Date/Time types
        else if (lowerType.contains("date") ||
                lowerType.contains("time") ||
                lowerType.contains("timestamp") ||
                lowerType.contains("interval")) {
            return "string"; // Dates are represented as strings in JSON/OpenAPI
        }
        // Boolean type
        else if (lowerType.contains("bool")) {
            return "boolean";
        }
        // Binary types
        else if (lowerType.contains("bytea") ||
                lowerType.contains("bit") ||
                lowerType.contains("varbit")) {
            return "string"; // Binary data as string (base64 encoded)
        }
        // Geometric types
        else if (lowerType.contains("point") ||
                lowerType.contains("line") ||
                lowerType.contains("lseg") ||
                lowerType.contains("box") ||
                lowerType.contains("path") ||
                lowerType.contains("polygon") ||
                lowerType.contains("circle")) {
            return "string"; // Geometric types as string representation
        }
        // Network address types
        else if (lowerType.contains("inet") ||
                lowerType.contains("cidr") ||
                lowerType.contains("macaddr")) {
            return "string";
        }
        // Array types
        else if (lowerType.contains("[]") ||
                lowerType.contains("array")) {
            return "array";
        }
        // Range types
        else if (lowerType.contains("range")) {
            return "object";
        }

        // Default fallback
        return "string";
    }

    /**
     * Maps PostgreSQL database type to a format string (used in OpenAPI specifications)
     *
     * @param pgType The PostgreSQL data type
     * @return The format specifier (date, date-time, int32, int64, etc.)
     */
    public String mapToFormat(String pgType) {
        if (pgType == null) return null;

        String lowerType = pgType.toLowerCase();

        // Date types
        if (lowerType.equals("date")) {
            return "date";
        }
        // Timestamp types
        else if (lowerType.contains("timestamp")) {
            return "date-time";
        }
        // Time types
        else if (lowerType.contains("time")) {
            return "time";
        }
        // Integer types
        else if (lowerType.contains("smallint") ||
                lowerType.contains("smallserial")) {
            return "int16";
        }
        else if (lowerType.contains("integer") ||
                lowerType.contains("serial")) {
            return "int32";
        }
        else if (lowerType.contains("bigint") ||
                lowerType.contains("bigserial")) {
            return "int64";
        }
        // Floating point types
        else if (lowerType.contains("real")) {
            return "float";
        }
        else if (lowerType.contains("double") ||
                lowerType.contains("float")) {
            return "double";
        }
        // Decimal types
        else if (lowerType.contains("decimal") ||
                lowerType.contains("numeric")) {
            return "double";
        }
        // Boolean
        else if (lowerType.contains("bool")) {
            return "boolean";
        }

        return null;
    }

    /**
     * Maps PostgreSQL database type to a standard PostgreSQL type name
     *
     * @param dataType The database data type
     * @return Standardized PostgreSQL type name
     */
    public String mapToPostgreSQLType(String dataType) {
        if (dataType == null) return "varchar";

        String lowerType = dataType.toLowerCase();

        // Character types
        if (lowerType.contains("varchar")) return "varchar";
        if (lowerType.contains("char") && !lowerType.contains("varchar")) return "char";
        if (lowerType.contains("text")) return "text";
        if (lowerType.contains("citext")) return "citext";

        // Numeric types
        if (lowerType.contains("smallint")) return "smallint";
        if (lowerType.contains("integer") || lowerType.contains("int")) return "integer";
        if (lowerType.contains("bigint")) return "bigint";
        if (lowerType.contains("decimal")) return "decimal";
        if (lowerType.contains("numeric")) return "numeric";
        if (lowerType.contains("real")) return "real";
        if (lowerType.contains("double")) return "double precision";
        if (lowerType.contains("float")) return "float";

        // Serial types
        if (lowerType.contains("smallserial")) return "smallserial";
        if (lowerType.contains("serial")) return "serial";
        if (lowerType.contains("bigserial")) return "bigserial";

        // Date/Time types
        if (lowerType.contains("timestamp") && lowerType.contains("zone")) return "timestamptz";
        if (lowerType.contains("timestamp")) return "timestamp";
        if (lowerType.contains("date")) return "date";
        if (lowerType.contains("time") && lowerType.contains("zone")) return "timetz";
        if (lowerType.contains("time")) return "time";
        if (lowerType.contains("interval")) return "interval";

        // Boolean
        if (lowerType.contains("bool")) return "boolean";

        // Binary types
        if (lowerType.contains("bytea")) return "bytea";
        if (lowerType.contains("bit")) return "bit";
        if (lowerType.contains("varbit")) return "varbit";

        // JSON types
        if (lowerType.contains("jsonb")) return "jsonb";
        if (lowerType.contains("json")) return "json";

        // XML
        if (lowerType.contains("xml")) return "xml";

        // UUID
        if (lowerType.contains("uuid")) return "uuid";

        // Network types
        if (lowerType.contains("inet")) return "inet";
        if (lowerType.contains("cidr")) return "cidr";
        if (lowerType.contains("macaddr")) return "macaddr";

        // Geometric types
        if (lowerType.contains("point")) return "point";
        if (lowerType.contains("line")) return "line";
        if (lowerType.contains("lseg")) return "lseg";
        if (lowerType.contains("box")) return "box";
        if (lowerType.contains("path")) return "path";
        if (lowerType.contains("polygon")) return "polygon";
        if (lowerType.contains("circle")) return "circle";

        // Array types
        if (lowerType.contains("[]")) return "array";

        // Range types
        if (lowerType.contains("range")) return "range";

        // Default fallback
        return "varchar";
    }

    /**
     * Maps PostgreSQL type to java.sql.Types constant for JDBC operations
     *
     * @param pgType The PostgreSQL data type
     * @return The corresponding java.sql.Types constant
     */
    public int mapToSqlType(String pgType) {
        if (pgType == null) return Types.VARCHAR;

        String lowerType = pgType.toLowerCase();

        // Character types
        if (lowerType.contains("varchar")) return Types.VARCHAR;
        if (lowerType.contains("char")) return Types.CHAR;
        if (lowerType.contains("text")) return Types.LONGVARCHAR;
        if (lowerType.contains("citext")) return Types.VARCHAR;

        // Numeric types
        if (lowerType.contains("smallint")) return Types.SMALLINT;
        if (lowerType.contains("integer") || lowerType.contains("int")) return Types.INTEGER;
        if (lowerType.contains("bigint")) return Types.BIGINT;
        if (lowerType.contains("decimal")) return Types.DECIMAL;
        if (lowerType.contains("numeric")) return Types.NUMERIC;
        if (lowerType.contains("real")) return Types.REAL;
        if (lowerType.contains("double")) return Types.DOUBLE;
        if (lowerType.contains("float")) return Types.FLOAT;

        // Serial types (auto-incrementing integers)
        if (lowerType.contains("serial")) return Types.INTEGER;

        // Date/Time types
        if (lowerType.contains("timestamp")) return Types.TIMESTAMP;
        if (lowerType.contains("date")) return Types.DATE;
        if (lowerType.contains("time")) return Types.TIME;
        if (lowerType.contains("interval")) return Types.OTHER;

        // Boolean
        if (lowerType.contains("bool")) return Types.BOOLEAN;

        // Binary types
        if (lowerType.contains("bytea")) return Types.BINARY;
        if (lowerType.contains("bit")) return Types.BIT;

        // JSON types
        if (lowerType.contains("jsonb") || lowerType.contains("json")) return Types.VARCHAR;

        // XML
        if (lowerType.contains("xml")) return Types.SQLXML;

        // UUID
        if (lowerType.contains("uuid")) return Types.VARCHAR;

        // Network and geometric types
        if (lowerType.contains("inet") ||
                lowerType.contains("cidr") ||
                lowerType.contains("macaddr") ||
                lowerType.contains("point") ||
                lowerType.contains("line") ||
                lowerType.contains("lseg") ||
                lowerType.contains("box") ||
                lowerType.contains("path") ||
                lowerType.contains("polygon") ||
                lowerType.contains("circle")) {
            return Types.VARCHAR;
        }

        // Array types
        if (lowerType.contains("[]") || lowerType.contains("array")) {
            return Types.ARRAY;
        }

        // Default fallback
        return Types.VARCHAR;
    }

    /**
     * Generates an example value for a given PostgreSQL data type
     *
     * @param pgType The PostgreSQL data type
     * @return An example value as a string
     */
    public String generateExample(String pgType) {
        if (pgType == null) return "";

        String lowerType = pgType.toLowerCase();

        // String types
        if (lowerType.contains("varchar") ||
                lowerType.contains("char") ||
                lowerType.contains("text") ||
                lowerType.contains("citext")) {
            return "sample text";
        }
        // JSON types
        else if (lowerType.contains("jsonb") || lowerType.contains("json")) {
            return "{\"key\": \"value\"}";
        }
        // XML type
        else if (lowerType.contains("xml")) {
            return "<root>sample</root>";
        }
        // UUID
        else if (lowerType.contains("uuid")) {
            return "550e8400-e29b-41d4-a716-446655440000";
        }
        // Numeric types - integer
        else if (lowerType.contains("smallint")) {
            return "123";
        }
        else if (lowerType.contains("integer") ||
                lowerType.contains("int")) {
            return "12345";
        }
        else if (lowerType.contains("bigint")) {
            return "1234567890";
        }
        // Numeric types - serial
        else if (lowerType.contains("serial")) {
            return "1";
        }
        // Numeric types - decimal
        else if (lowerType.contains("decimal") ||
                lowerType.contains("numeric")) {
            return "123.45";
        }
        // Numeric types - floating point
        else if (lowerType.contains("real")) {
            return "123.456";
        }
        else if (lowerType.contains("double")) {
            return "123.456789";
        }
        // Date types
        else if (lowerType.equals("date")) {
            return "2024-01-01";
        }
        // Timestamp types
        else if (lowerType.contains("timestamp")) {
            return "2024-01-01T00:00:00Z";
        }
        // Time types
        else if (lowerType.contains("time")) {
            return "00:00:00";
        }
        // Interval
        else if (lowerType.contains("interval")) {
            return "1 day";
        }
        // Boolean
        else if (lowerType.contains("bool")) {
            return "true";
        }
        // Binary types
        else if (lowerType.contains("bytea")) {
            return "[BASE64_ENCODED_BINARY_DATA]";
        }
        else if (lowerType.contains("bit")) {
            return "1010";
        }
        // Network types
        else if (lowerType.contains("inet")) {
            return "192.168.1.1";
        }
        else if (lowerType.contains("cidr")) {
            return "192.168.1.0/24";
        }
        else if (lowerType.contains("macaddr")) {
            return "08:00:2b:01:02:03";
        }
        // Geometric types
        else if (lowerType.contains("point")) {
            return "(1,1)";
        }
        else if (lowerType.contains("line")) {
            return "{1,-1,0}";
        }
        else if (lowerType.contains("lseg")) {
            return "[(1,1),(2,2)]";
        }
        else if (lowerType.contains("box")) {
            return "(1,1),(2,2)";
        }
        else if (lowerType.contains("path")) {
            return "((1,1),(2,2))";
        }
        else if (lowerType.contains("polygon")) {
            return "((1,1),(2,2),(3,1))";
        }
        else if (lowerType.contains("circle")) {
            return "<(1,1),1>";
        }
        // Array types
        else if (lowerType.contains("[]") || lowerType.contains("array")) {
            return "['element1', 'element2']";
        }
        // Range types
        else if (lowerType.contains("range")) {
            return "[1,10)";
        }

        // Default fallback for any other types
        return "sample";
    }

    /**
     * Gets the Java class name for a PostgreSQL data type
     *
     * @param pgType The PostgreSQL data type
     * @return The corresponding Java class name
     */
    public String mapToJavaType(String pgType) {
        if (pgType == null) return "String";

        String lowerType = pgType.toLowerCase();

        // Integer types
        if (lowerType.contains("smallint")) return "Short";
        if (lowerType.contains("integer") || lowerType.contains("int")) return "Integer";
        if (lowerType.contains("bigint")) return "Long";

        // Floating point types
        if (lowerType.contains("real")) return "Float";
        if (lowerType.contains("double") || lowerType.contains("float")) return "Double";
        if (lowerType.contains("decimal") || lowerType.contains("numeric")) return "java.math.BigDecimal";

        // Serial types (auto-increment)
        if (lowerType.contains("serial")) return "Integer";

        // Date/Time types
        if (lowerType.contains("timestamp")) return "java.sql.Timestamp";
        if (lowerType.contains("date")) return "java.sql.Date";
        if (lowerType.contains("time")) return "java.sql.Time";
        if (lowerType.contains("interval")) return "String";

        // Boolean
        if (lowerType.contains("bool")) return "Boolean";

        // String types
        if (lowerType.contains("varchar") ||
                lowerType.contains("char") ||
                lowerType.contains("text") ||
                lowerType.contains("citext") ||
                lowerType.contains("json") ||
                lowerType.contains("jsonb") ||
                lowerType.contains("xml") ||
                lowerType.contains("uuid") ||
                lowerType.contains("inet") ||
                lowerType.contains("cidr") ||
                lowerType.contains("macaddr")) {
            return "String";
        }

        // Binary types
        if (lowerType.contains("bytea")) return "byte[]";
        if (lowerType.contains("bit")) return "java.sql.BitSet";

        // Geometric types
        if (lowerType.contains("point") ||
                lowerType.contains("line") ||
                lowerType.contains("lseg") ||
                lowerType.contains("box") ||
                lowerType.contains("path") ||
                lowerType.contains("polygon") ||
                lowerType.contains("circle")) {
            return "String";
        }

        // Array types
        if (lowerType.contains("[]") || lowerType.contains("array")) {
            return "java.sql.Array";
        }

        // Default fallback
        return "String";
    }

    /**
     * Checks if a PostgreSQL type is numeric
     *
     * @param pgType The PostgreSQL data type
     * @return true if the type is numeric
     */
    public boolean isNumericType(String pgType) {
        if (pgType == null) return false;

        String lowerType = pgType.toLowerCase();
        return lowerType.contains("int") ||
                lowerType.contains("decimal") ||
                lowerType.contains("numeric") ||
                lowerType.contains("real") ||
                lowerType.contains("double") ||
                lowerType.contains("float") ||
                lowerType.contains("serial");
    }

    /**
     * Checks if a PostgreSQL type is a string type
     *
     * @param pgType The PostgreSQL data type
     * @return true if the type is a string type
     */
    public boolean isStringType(String pgType) {
        if (pgType == null) return false;

        String lowerType = pgType.toLowerCase();
        return lowerType.contains("varchar") ||
                lowerType.contains("char") ||
                lowerType.contains("text") ||
                lowerType.contains("citext") ||
                lowerType.contains("json") ||
                lowerType.contains("jsonb") ||
                lowerType.contains("xml") ||
                lowerType.contains("uuid");
    }

    /**
     * Checks if a PostgreSQL type is a date/time type
     *
     * @param pgType The PostgreSQL data type
     * @return true if the type is a date/time type
     */
    public boolean isDateTimeType(String pgType) {
        if (pgType == null) return false;

        String lowerType = pgType.toLowerCase();
        return lowerType.contains("date") ||
                lowerType.contains("time") ||
                lowerType.contains("timestamp") ||
                lowerType.contains("interval");
    }

    /**
     * Checks if a PostgreSQL type is a boolean type
     *
     * @param pgType The PostgreSQL data type
     * @return true if the type is a boolean type
     */
    public boolean isBooleanType(String pgType) {
        if (pgType == null) return false;
        return pgType.toLowerCase().contains("bool");
    }

    /**
     * Checks if a PostgreSQL type is a binary type
     *
     * @param pgType The PostgreSQL data type
     * @return true if the type is a binary type
     */
    public boolean isBinaryType(String pgType) {
        if (pgType == null) return false;
        String lowerType = pgType.toLowerCase();
        return lowerType.contains("bytea") ||
                lowerType.contains("bit");
    }

    /**
     * Gets the appropriate JDBC type name for a PostgreSQL type
     *
     * @param pgType The PostgreSQL data type
     * @return The JDBC type name
     */
    public String getJdbcTypeName(String pgType) {
        int sqlType = mapToSqlType(pgType);
        return getJdbcTypeName(sqlType);
    }

    /**
     * Gets the JDBC type name from the SQL type constant
     *
     * @param sqlType The java.sql.Types constant
     * @return The JDBC type name
     */
    private String getJdbcTypeName(int sqlType) {
        switch (sqlType) {
            case Types.BIT: return "BIT";
            case Types.TINYINT: return "TINYINT";
            case Types.SMALLINT: return "SMALLINT";
            case Types.INTEGER: return "INTEGER";
            case Types.BIGINT: return "BIGINT";
            case Types.FLOAT: return "FLOAT";
            case Types.REAL: return "REAL";
            case Types.DOUBLE: return "DOUBLE";
            case Types.NUMERIC: return "NUMERIC";
            case Types.DECIMAL: return "DECIMAL";
            case Types.CHAR: return "CHAR";
            case Types.VARCHAR: return "VARCHAR";
            case Types.LONGVARCHAR: return "LONGVARCHAR";
            case Types.DATE: return "DATE";
            case Types.TIME: return "TIME";
            case Types.TIMESTAMP: return "TIMESTAMP";
            case Types.BINARY: return "BINARY";
            case Types.VARBINARY: return "VARBINARY";
            case Types.LONGVARBINARY: return "LONGVARBINARY";
            case Types.BOOLEAN: return "BOOLEAN";
            case Types.CLOB: return "CLOB";
            case Types.BLOB: return "BLOB";
            case Types.ARRAY: return "ARRAY";
            case Types.STRUCT: return "STRUCT";
            case Types.REF: return "REF";
            case Types.JAVA_OBJECT: return "JAVA_OBJECT";
            case Types.SQLXML: return "SQLXML";
            default: return "OTHER";
        }
    }
}