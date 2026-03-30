package com.usg.apiGeneration.utils.apiEngine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PostgreSQLTypeMapperUtil {

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
                lowerType.contains("macaddr")) {
            return "string";
        }
        // Numeric types - integer
        else if (lowerType.contains("smallint") ||
                lowerType.contains("integer") ||
                lowerType.contains("bigint") ||
                lowerType.contains("int") ||
                lowerType.contains("serial") ||
                lowerType.contains("bigserial") ||
                lowerType.contains("smallserial")) {
            return "integer";
        }
        // Numeric types - decimal/float
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
            return "string";
        }
        // Boolean type
        else if (lowerType.contains("bool")) {
            return "boolean";
        }
        // Binary types
        else if (lowerType.contains("bytea") ||
                lowerType.contains("bit")) {
            return "string";
        }
        // Array types
        else if (lowerType.contains("[]") ||
                lowerType.contains("array")) {
            return "array";
        }
        // Geometric types
        else if (lowerType.contains("point") ||
                lowerType.contains("line") ||
                lowerType.contains("lseg") ||
                lowerType.contains("box") ||
                lowerType.contains("path") ||
                lowerType.contains("polygon") ||
                lowerType.contains("circle")) {
            return "string";
        }

        return "string";
    }

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

        return "varchar";
    }

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

        // Serial types
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

        return Types.VARCHAR;
    }

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
        // Integer types
        else if (lowerType.contains("smallint")) {
            return "123";
        }
        else if (lowerType.contains("integer") ||
                lowerType.contains("int") ||
                lowerType.contains("serial")) {
            return "12345";
        }
        else if (lowerType.contains("bigint") ||
                lowerType.contains("bigserial")) {
            return "1234567890";
        }
        // Decimal/Float types
        else if (lowerType.contains("decimal") ||
                lowerType.contains("numeric")) {
            return "123.45";
        }
        else if (lowerType.contains("real")) {
            return "123.456";
        }
        else if (lowerType.contains("double") ||
                lowerType.contains("float")) {
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

        return "";
    }

    public Map<String, String> mapToLanguageTypes(String apiType) {
        Map<String, String> typeMap = new HashMap<>();

        if (apiType == null) {
            typeMap.put("java", "String");
            typeMap.put("csharp", "string");
            typeMap.put("go", "string");
            typeMap.put("python", "str");
            typeMap.put("javascript", "string");
            return typeMap;
        }

        switch (apiType.toLowerCase()) {
            case "integer":
                typeMap.put("java", "Integer");
                typeMap.put("csharp", "int?");
                typeMap.put("go", "int64");
                typeMap.put("python", "int");
                typeMap.put("javascript", "number");
                break;
            case "number":
                typeMap.put("java", "Double");
                typeMap.put("csharp", "double?");
                typeMap.put("go", "float64");
                typeMap.put("python", "float");
                typeMap.put("javascript", "number");
                break;
            case "boolean":
                typeMap.put("java", "Boolean");
                typeMap.put("csharp", "bool?");
                typeMap.put("go", "bool");
                typeMap.put("python", "bool");
                typeMap.put("javascript", "boolean");
                break;
            case "array":
                typeMap.put("java", "List<Object>");
                typeMap.put("csharp", "List<object>");
                typeMap.put("go", "[]interface{}");
                typeMap.put("python", "list");
                typeMap.put("javascript", "array");
                break;
            case "object":
                typeMap.put("java", "Map<String, Object>");
                typeMap.put("csharp", "Dictionary<string, object>");
                typeMap.put("go", "map[string]interface{}");
                typeMap.put("python", "dict");
                typeMap.put("javascript", "object");
                break;
            default:
                typeMap.put("java", "String");
                typeMap.put("csharp", "string");
                typeMap.put("go", "string");
                typeMap.put("python", "str");
                typeMap.put("javascript", "string");
                break;
        }

        return typeMap;
    }

    /**
     * Helper method to get the appropriate API type for a PostgreSQL type
     */
    public String getApiType(String pgType) {
        return mapToApiType(pgType);
    }

    /**
     * Helper method to get the format for a PostgreSQL type
     */
    public String getFormat(String pgType) {
        return mapToFormat(pgType);
    }

    /**
     * Helper method to get the example value for a PostgreSQL type
     */
    public String getExample(String pgType) {
        return generateExample(pgType);
    }

    /**
     * Helper method to check if a PostgreSQL type is numeric
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
     * Helper method to check if a PostgreSQL type is a string type
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
     * Helper method to check if a PostgreSQL type is a date/time type
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
     * Helper method to check if a PostgreSQL type is a boolean type
     */
    public boolean isBooleanType(String pgType) {
        if (pgType == null) return false;
        return pgType.toLowerCase().contains("bool");
    }

    /**
     * Helper method to check if a PostgreSQL type is a binary type
     */
    public boolean isBinaryType(String pgType) {
        if (pgType == null) return false;
        String lowerType = pgType.toLowerCase();
        return lowerType.contains("bytea") || lowerType.contains("bit");
    }

    /**
     * Helper method to check if a PostgreSQL type is an array type
     */
    public boolean isArrayType(String pgType) {
        if (pgType == null) return false;
        String lowerType = pgType.toLowerCase();
        return lowerType.contains("[]") || lowerType.contains("array");
    }

    /**
     * Helper method to check if a PostgreSQL type is a JSON type
     */
    public boolean isJsonType(String pgType) {
        if (pgType == null) return false;
        String lowerType = pgType.toLowerCase();
        return lowerType.contains("jsonb") || lowerType.contains("json");
    }

    /**
     * Helper method to get the base type for an array type
     */
    public String getBaseTypeForArray(String arrayType) {
        if (arrayType == null) return null;
        return arrayType.replace("[]", "").trim();
    }
}