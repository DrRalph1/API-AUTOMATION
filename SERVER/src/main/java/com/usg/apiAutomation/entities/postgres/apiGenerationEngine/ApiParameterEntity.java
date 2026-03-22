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
    @EqualsAndHashCode.Include
    @ToString.Include
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

    @Column(name = "parameter_type")
    private String parameterType;

    @Column(name = "oracle_type")
    private String oracleType;

    @Column(name = "api_type")
    private String apiType;

    @Column(name = "parameter_location") // query, path, header, body
    private String parameterLocation;

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

    @Column(name = "in_body")
    private Boolean inBody;

    @Column(name = "is_primary_key")
    private Boolean isPrimaryKey;

    @Column(name = "param_mode") // IN, OUT, IN OUT
    private String paramMode;

    @Column(name = "position")
    private Integer position;

    /* ===========================
       Equals & HashCode
       =========================== */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiParameterEntity that = (ApiParameterEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(key, that.key) &&
                Objects.equals(dbColumn, that.dbColumn) &&
                Objects.equals(oracleType, that.oracleType) &&
                Objects.equals(apiType, that.apiType) &&
                Objects.equals(parameterLocation, that.parameterLocation) &&
                Objects.equals(required, that.required) &&
                Objects.equals(description, that.description) &&
                Objects.equals(example, that.example) &&
                Objects.equals(validationPattern, that.validationPattern) &&
                Objects.equals(defaultValue, that.defaultValue) &&
                Objects.equals(inBody, that.inBody) &&
                Objects.equals(isPrimaryKey, that.isPrimaryKey) &&
                Objects.equals(paramMode, that.paramMode) &&
                Objects.equals(position, that.position) &&
                Objects.equals(
                        generatedApi != null ? generatedApi.getId() : null,
                        that.generatedApi != null ? that.generatedApi.getId() : null
                );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id, key, dbColumn, oracleType, apiType,
                parameterLocation, required, description, example,
                validationPattern, defaultValue,
                inBody, isPrimaryKey, paramMode, position,
                generatedApi != null ? generatedApi.getId() : null
        );
    }

    /* ===========================
       toString
       =========================== */

    @Override
    public String toString() {
        return "ApiParameterEntity{" +
                "id='" + id + '\'' +
                ", apiId='" + (generatedApi != null ? generatedApi.getId() : null) + '\'' +
                ", key='" + key + '\'' +
                ", dbColumn='" + dbColumn + '\'' +
                ", oracleType='" + oracleType + '\'' +
                ", apiType='" + apiType + '\'' +
                ", parameterLocation='" + parameterLocation + '\'' +
                ", required=" + required +
                ", inBody=" + inBody +
                ", isPrimaryKey=" + isPrimaryKey +
                ", paramMode='" + paramMode + '\'' +
                ", description='" + truncate(description, 50) + '\'' +
                ", example='" + truncate(example, 30) + '\'' +
                ", defaultValue='" + truncate(defaultValue, 30) + '\'' +
                ", position=" + position +
                '}';
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        if (value.length() <= maxLength) return value;
        return value.substring(0, maxLength) + "...";
    }

    /* ===========================
       Helper Methods
       =========================== */

    public boolean isInputParameter() {
        return "IN".equalsIgnoreCase(paramMode) ||
                "IN OUT".equalsIgnoreCase(paramMode) ||
                "INOUT".equalsIgnoreCase(paramMode);
    }

    public boolean isOutputParameter() {
        return "OUT".equalsIgnoreCase(paramMode) ||
                "IN OUT".equalsIgnoreCase(paramMode) ||
                "INOUT".equalsIgnoreCase(paramMode);
    }

    public String getParameterDirection() {
        if (paramMode == null) return "UNKNOWN";
        return switch (paramMode.toUpperCase().replace(" ", "")) {
            case "IN" -> "INPUT";
            case "OUT" -> "OUTPUT";
            case "INOUT" -> "INPUT_OUTPUT";
            default -> "UNKNOWN";
        };
    }

    public boolean isPrimaryKey() {
        return Boolean.TRUE.equals(isPrimaryKey);
    }

    public boolean isInBody() {
        return Boolean.TRUE.equals(inBody);
    }

    /* ===========================
       Builder Defaults
       =========================== */

    public static class ApiParameterEntityBuilder {
        private Boolean required = false;
        private Boolean inBody = false;
        private Boolean isPrimaryKey = false;
        private Integer position = 0;
        private String paramMode = "IN";
    }
}