package com.usg.apiGeneration.entities.postgres.collections;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity(name = "ParametersEntityCollections")
@Table(name = "tb_col_parameters")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class ParameterEntity {

    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id;

    @Column(name = "api_id")
    private String generatedApiId;

    @Column(name = "key", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String key;

    @Column(name = "db_column")
    @EqualsAndHashCode.Include
    @ToString.Include
    private String dbColumn;

    @Column(name = "db_parameter")
    @EqualsAndHashCode.Include
    @ToString.Include
    private String dbParameter;

    @Column(name = "parameter_type")
    @EqualsAndHashCode.Include
    @ToString.Include
    private String parameterType;

    @Column(name = "oracle_type")
    @EqualsAndHashCode.Include
    @ToString.Include
    private String oracleType;

    @Column(name = "api_type")
    @EqualsAndHashCode.Include
    @ToString.Include
    private String apiType;

    @Column(name = "parameter_location") // query, path, header, body
    private String parameterLocation;

    @Column(name = "required")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Boolean required = false;

    @Column(length = 1000)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String description;

    @Column(name = "example", length = 1000)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String example;

    @Column(name = "validation_pattern", length = 500)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String validationPattern;

    @Column(name = "default_value", length = 500)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String defaultValue;

    @Column(name = "value", length = 2000)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String value;

    @Column(name = "in_body")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Boolean inBody = false;

    @Column(name = "is_primary_key")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Boolean isPrimaryKey = false;

    @Column(name = "param_mode")
    @EqualsAndHashCode.Include
    @ToString.Include
    private String paramMode = "IN";

    @Column(name = "position")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Integer position = 0;

    @Column(name = "is_enabled")
    private boolean isEnabled = true;

    // Helper method to maintain bidirectional relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RequestEntity request;

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
}