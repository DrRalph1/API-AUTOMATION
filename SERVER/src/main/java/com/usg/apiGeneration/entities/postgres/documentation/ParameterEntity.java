package com.usg.apiGeneration.entities.postgres.documentation;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;

@Entity(name = "ParameterEntityDocumentation")
@Table(name = "tb_doc_parameters")
@Data
@NoArgsConstructor
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

    @Column(nullable = false)
    private String name;

    @Column(name = "key")
    private String key;

    @Column(name = "db_column")
    private String dbColumn;

    @Column(name = "db_parameter")
    private String dbParameter;

    @Column(name = "parameter_type", nullable = false)
    private String parameterType; // string, integer, boolean, etc.

    @Column(name = "oracle_type")
    private String oracleType;

    @Column(name = "api_type")
    private String apiType;

    @Column(name = "parameter_location") // query, path, header, body
    private String parameterLocation;

    @Column(name = "required")
    private Boolean required = false;

    @Column(length = 1000)
    private String description;

    @Column(name = "default_value", length = 500)
    private String defaultValue;

    @Column(name = "example", length = 1000)
    private String example;

    @Column(name = "value", length = 2000)
    private String value;

    @Column(name = "format", length = 100)
    private String format;

    @Column(name = "validation_pattern", length = 500)
    private String validationPattern;

    @Column(name = "in_body")
    private Boolean inBody = false;

    @Column(name = "is_primary_key")
    private Boolean isPrimaryKey = false;

    @Column(name = "param_mode")
    private String paramMode = "IN";

    @Column(name = "is_enabled")
    private boolean isEnabled = true;

    @Column(name = "position")
    private Integer position = 0;

    // Relationship to APIEndpoint
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private APIEndpointEntity endpoint;

    /* ===========================
       Helper Methods
       =========================== */

    public boolean isRequired() {
        return Boolean.TRUE.equals(required);
    }

    public boolean isInBody() {
        return Boolean.TRUE.equals(inBody);
    }

    public boolean isPrimaryKey() {
        return Boolean.TRUE.equals(isPrimaryKey);
    }

    public boolean isPathParameter() {
        return "path".equalsIgnoreCase(parameterLocation);
    }

    public boolean isQueryParameter() {
        return "query".equalsIgnoreCase(parameterLocation);
    }

    public boolean isHeaderParameter() {
        return "header".equalsIgnoreCase(parameterLocation);
    }

    public boolean isBodyParameter() {
        return "body".equalsIgnoreCase(parameterLocation) || Boolean.TRUE.equals(inBody);
    }

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
}


