package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "tb_eng_response_mappings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseMappingEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private GeneratedApiEntity generatedApi;

    @Column(name = "api_field")
    private String apiField;

    @Column(name = "db_column")
    private String dbColumn;

    @Column(name = "oracle_type")
    private String oracleType;

    @Column(name = "api_type")
    private String apiType;

    @Column(name = "format")
    private String format;

    @Column(name = "nullable")
    private Boolean nullable;

    @Column(name = "is_primary_key")
    private Boolean isPrimaryKey;

    @Column(name = "include_in_response")
    private Boolean includeInResponse;

    @Column(name = "position")
    private Integer position;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiResponseMappingEntity that = (ApiResponseMappingEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(apiField, that.apiField) &&
                Objects.equals(dbColumn, that.dbColumn) &&
                Objects.equals(oracleType, that.oracleType) &&
                Objects.equals(apiType, that.apiType) &&
                Objects.equals(format, that.format) &&
                Objects.equals(nullable, that.nullable) &&
                Objects.equals(isPrimaryKey, that.isPrimaryKey) &&
                Objects.equals(includeInResponse, that.includeInResponse) &&
                Objects.equals(position, that.position) &&
                Objects.equals(generatedApi != null ? generatedApi.getId() : null,
                        that.generatedApi != null ? that.generatedApi.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, apiField, dbColumn, oracleType, apiType,
                format, nullable, isPrimaryKey, includeInResponse, position,
                generatedApi != null ? generatedApi.getId() : null);
    }

    @Override
    public String toString() {
        return "ApiResponseMappingEntity{" +
                "id='" + id + '\'' +
                ", apiId='" + (generatedApi != null ? generatedApi.getId() : null) + '\'' +
                ", apiField='" + apiField + '\'' +
                ", dbColumn='" + dbColumn + '\'' +
                ", oracleType='" + oracleType + '\'' +
                ", apiType='" + apiType + '\'' +
                ", format='" + format + '\'' +
                ", nullable=" + nullable +
                ", isPrimaryKey=" + isPrimaryKey +
                ", includeInResponse=" + includeInResponse +
                ", position=" + position +
                '}';
    }

    /**
     * Checks if this field should be included in the API response
     */
    public boolean shouldInclude() {
        return includeInResponse == null || includeInResponse;
    }

    /**
     * Checks if this field is nullable in the response
     */
    public boolean isNullable() {
        return nullable != null && nullable;
    }

    /**
     * Checks if this is a primary key field
     */
    public boolean isPrimaryKey() {
        return isPrimaryKey != null && isPrimaryKey;
    }

    /**
     * Gets the formatted field name for the API response
     */
    public String getFormattedApiField() {
        if (apiField == null || apiField.trim().isEmpty()) {
            return dbColumn; // Fallback to db column
        }

        // Convert snake_case to camelCase if format suggests JSON
        if ("json".equalsIgnoreCase(apiType) && apiField.contains("_")) {
            return toCamelCase(apiField);
        }

        return apiField;
    }

    /**
     * Converts snake_case to camelCase
     */
    private String toCamelCase(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }

        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;

        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }

        return result.toString();
    }

    /**
     * Validates if the Oracle type is supported
     */
    public boolean isOracleTypeSupported() {
        if (oracleType == null) return false;

        String type = oracleType.toUpperCase();
        return type.startsWith("VARCHAR") ||
                type.startsWith("CHAR") ||
                type.startsWith("NUMBER") ||
                type.startsWith("DATE") ||
                type.startsWith("TIMESTAMP") ||
                type.startsWith("CLOB") ||
                type.startsWith("BLOB") ||
                type.equals("RAW") ||
                type.startsWith("ROWID");
    }

    /**
     * Gets the appropriate Java type for the Oracle type
     */
    public String getJavaType() {
        if (oracleType == null) return "Object";

        String type = oracleType.toUpperCase();
        if (type.startsWith("VARCHAR") || type.startsWith("CHAR")) {
            return "String";
        } else if (type.startsWith("NUMBER")) {
            return extractNumericType(type);
        } else if (type.startsWith("DATE") || type.startsWith("TIMESTAMP")) {
            return "LocalDateTime";
        } else if (type.startsWith("CLOB")) {
            return "String";
        } else if (type.startsWith("BLOB")) {
            return "byte[]";
        } else {
            return "Object";
        }
    }

    /**
     * Extracts appropriate numeric type from NUMBER(precision,scale)
     */
    private String extractNumericType(String numberType) {
        if (numberType.equals("NUMBER")) {
            return "Long"; // Default for NUMBER without precision
        }

        // Try to extract precision and scale
        try {
            String[] parts = numberType.replace("NUMBER", "").replace("(", "").replace(")", "").split(",");
            if (parts.length == 2) {
                int scale = Integer.parseInt(parts[1].trim());
                if (scale > 0) {
                    return "BigDecimal";
                }
            }

            int precision = Integer.parseInt(parts[0].trim());
            if (precision <= 5) return "Integer";
            if (precision <= 9) return "Long";
            return "BigDecimal";
        } catch (Exception e) {
            return "Long";
        }
    }

    /**
     * Builder with defaults
     */
    public static class ApiResponseMappingEntityBuilder {
        private Boolean nullable = true; // Default to nullable
        private Boolean includeInResponse = true; // Default to include
        private Integer position = 0; // Default position
        private String apiType = "string"; // Default API type
    }
}