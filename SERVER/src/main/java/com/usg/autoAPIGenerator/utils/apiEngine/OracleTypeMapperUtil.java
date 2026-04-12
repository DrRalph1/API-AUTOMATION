package com.usg.autoAPIGenerator.utils.apiEngine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class OracleTypeMapperUtil {

    public String mapToApiType(String oracleType) {
        if (oracleType == null) return "string";

        String upperType = oracleType.toUpperCase();
        if (upperType.contains("VARCHAR") || upperType.contains("CHAR") || upperType.contains("CLOB")) {
            return "string";
        } else if (upperType.contains("NUMBER") || upperType.contains("INTEGER") ||
                upperType.contains("FLOAT") || upperType.contains("DECIMAL")) {
            return "integer";
        } else if (upperType.contains("DATE") || upperType.contains("TIMESTAMP")) {
            return "string";
        } else if (upperType.contains("BLOB") || upperType.contains("RAW")) {
            return "string";
        }
        return "string";
    }

    public String mapToFormat(String oracleType) {
        if (oracleType == null) return null;

        String upperType = oracleType.toUpperCase();
        if (upperType.contains("DATE")) {
            return "date";
        } else if (upperType.contains("TIMESTAMP")) {
            return "date-time";
        } else if (upperType.contains("NUMBER")) {
            return "double";
        } else if (upperType.contains("INTEGER")) {
            return "int32";
        } else if (upperType.contains("FLOAT")) {
            return "float";
        }
        return null;
    }

    public String mapOracleType(String dataType) {
        if (dataType == null) return "VARCHAR2";

        String upperType = dataType.toUpperCase();
        if (upperType.contains("VARCHAR")) return "VARCHAR2";
        if (upperType.contains("CHAR")) return "CHAR";
        if (upperType.contains("CLOB")) return "CLOB";
        if (upperType.contains("NUMBER")) return "NUMBER";
        if (upperType.contains("INTEGER")) return "NUMBER";
        if (upperType.contains("FLOAT")) return "NUMBER";
        if (upperType.contains("DATE")) return "DATE";
        if (upperType.contains("TIMESTAMP")) return "TIMESTAMP";
        if (upperType.contains("BLOB")) return "BLOB";
        return "VARCHAR2";
    }

    public int mapToSqlType(String oracleType) {
        if (oracleType == null) return Types.VARCHAR;

        String upperType = oracleType.toUpperCase();
        if (upperType.contains("VARCHAR")) return Types.VARCHAR;
        if (upperType.contains("CHAR")) return Types.CHAR;
        if (upperType.contains("CLOB")) return Types.CLOB;
        if (upperType.contains("NUMBER") || upperType.contains("NUMERIC")) return Types.NUMERIC;
        if (upperType.contains("INTEGER")) return Types.INTEGER;
        if (upperType.contains("DATE")) return Types.DATE;
        if (upperType.contains("TIMESTAMP")) return Types.TIMESTAMP;
        if (upperType.contains("BLOB")) return Types.BLOB;
        if (upperType.contains("BOOLEAN")) return Types.BOOLEAN;

        return Types.VARCHAR;
    }

    public String generateExample(String oracleType) {
        if (oracleType == null) return "";

        String upperType = oracleType.toUpperCase();
        if (upperType.contains("VARCHAR") || upperType.contains("CHAR")) {
            return "sample";
        } else if (upperType.contains("NUMBER") || upperType.contains("INTEGER")) {
            return "1";
        } else if (upperType.contains("DATE")) {
            return "2024-01-01";
        } else if (upperType.contains("TIMESTAMP")) {
            return "2024-01-01T00:00:00Z";
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
}