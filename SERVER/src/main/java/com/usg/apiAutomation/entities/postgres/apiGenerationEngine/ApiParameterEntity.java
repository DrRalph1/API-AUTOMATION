package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "tb_eng_parameters")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiParameterEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private GeneratedApiEntity generatedApi;

    @Column(name = "param_key")
    private String key;

    @Column(name = "db_column")
    private String dbColumn;

    @Column(name = "db_parameter")
    private String dbParameter;

    @Column(name = "oracle_type")
    private String oracleType;

    @Column(name = "api_type")
    private String apiType;

    @Column(name = "parameter_type")
    private String parameterType;

    @Column(name = "required")
    private Boolean required;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "example")
    private String example;

    @Column(name = "validation_pattern")
    private String validationPattern;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "position")
    private Integer position;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiParameterEntity that = (ApiParameterEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(key, that.key) &&
                Objects.equals(dbColumn, that.dbColumn) &&
                Objects.equals(dbParameter, that.dbParameter) &&
                Objects.equals(oracleType, that.oracleType) &&
                Objects.equals(apiType, that.apiType) &&
                Objects.equals(parameterType, that.parameterType) &&
                Objects.equals(required, that.required) &&
                Objects.equals(description, that.description) &&
                Objects.equals(example, that.example) &&
                Objects.equals(validationPattern, that.validationPattern) &&
                Objects.equals(defaultValue, that.defaultValue) &&
                Objects.equals(position, that.position) &&
                Objects.equals(generatedApi != null ? generatedApi.getId() : null,
                        that.generatedApi != null ? that.generatedApi.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, key, dbColumn, dbParameter, oracleType, apiType,
                parameterType, required, description, example,
                validationPattern, defaultValue, position,
                generatedApi != null ? generatedApi.getId() : null);
    }

    @Override
    public String toString() {
        return "ApiParameterEntity{" +
                "id='" + id + '\'' +
                ", apiId='" + (generatedApi != null ? generatedApi.getId() : null) + '\'' +
                ", key='" + key + '\'' +
                ", dbColumn='" + dbColumn + '\'' +
                ", dbParameter='" + dbParameter + '\'' +
                ", oracleType='" + oracleType + '\'' +
                ", apiType='" + apiType + '\'' +
                ", parameterType='" + parameterType + '\'' +
                ", required=" + required +
                ", description='" + truncate(description, 50) + '\'' +
                ", example='" + truncate(example, 30) + '\'' +
                ", validationPattern='" + validationPattern + '\'' +
                ", defaultValue='" + truncate(defaultValue, 30) + '\'' +
                ", position=" + position +
                '}';
    }

    /**
     * Helper method to truncate long strings in toString()
     */
    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        if (value.length() <= maxLength) return value;
        return value.substring(0, maxLength) + "...";
    }

    /**
     * Helper method to check if this is an IN parameter
     */
    public boolean isInputParameter() {
        return "IN".equalsIgnoreCase(parameterType) ||
                "INOUT".equalsIgnoreCase(parameterType);
    }

    /**
     * Helper method to check if this is an OUT parameter
     */
    public boolean isOutputParameter() {
        return "OUT".equalsIgnoreCase(parameterType) ||
                "INOUT".equalsIgnoreCase(parameterType);
    }

    /**
     * Helper method to get the parameter direction
     */
    public String getParameterDirection() {
        if (parameterType == null) return "UNKNOWN";
        return switch (parameterType.toUpperCase()) {
            case "IN" -> "INPUT";
            case "OUT" -> "OUTPUT";
            case "INOUT" -> "INPUT_OUTPUT";
            default -> "UNKNOWN";
        };
    }

    /**
     * Helper method to validate if the parameter matches Oracle naming conventions
     */
    public boolean isValidOracleParameter() {
        if (dbParameter == null || dbParameter.trim().isEmpty()) {
            return false;
        }
        // Oracle parameter naming: must start with letter, contain only letters, numbers, _ and $
        return dbParameter.matches("^[a-zA-Z][a-zA-Z0-9_$]*$");
    }

    /**
     * Builder with defaults
     */
    public static class ApiParameterEntityBuilder {
        private Boolean required = false; // Default to not required
        private Integer position = 0; // Default position
        private String parameterType = "IN"; // Default to IN parameter
    }
}