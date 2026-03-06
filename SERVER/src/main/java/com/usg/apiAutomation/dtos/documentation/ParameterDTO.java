package com.usg.apiAutomation.dtos.documentation;

import com.usg.apiAutomation.entities.postgres.documentation.ParameterEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParameterDTO {
    // Core fields (matching entity)
    private String id;
    private String name;
    private String key;
    private String dbColumn;
    private String dbParameter;
    private String parameterType; // string, integer, boolean, etc.
    private String oracleType;
    private String apiType;
    private String parameterLocation; // query, path, header, body
    private Boolean required;
    private String description;
    private String defaultValue;
    private String example;
    private String value;
    private String format;
    private String validationPattern;
    private Boolean inBody;
    private Boolean isPrimaryKey;
    private String paramMode;
    private boolean isEnabled;
    private Integer position;

    // Relationship reference (optional)
    private String endpointId;

    // Legacy fields for backward compatibility (derived from entity fields)
    private String type;        // Derived from parameterType or apiType
    private String in;          // Derived from parameterLocation

    // Constructors
    public ParameterDTO(String name, String type, String in, boolean required, String description) {
        this.name = name;
        this.type = type;
        this.in = in;
        this.required = required;
        this.description = description;
    }

    public ParameterDTO(String name, String type, String in, boolean required,
                        String description, String example) {
        this.name = name;
        this.type = type;
        this.in = in;
        this.required = required;
        this.description = description;
        this.example = example;
    }

    // Helper methods to determine parameter characteristics
    public boolean isPathParameter() {
        return "path".equalsIgnoreCase(parameterLocation) || "path".equalsIgnoreCase(in);
    }

    public boolean isQueryParameter() {
        return "query".equalsIgnoreCase(parameterLocation) || "query".equalsIgnoreCase(in);
    }

    public boolean isHeaderParameter() {
        return "header".equalsIgnoreCase(parameterLocation) || "header".equalsIgnoreCase(in);
    }

    public boolean isBodyParameter() {
        return "body".equalsIgnoreCase(parameterLocation) ||
                "body".equalsIgnoreCase(in) ||
                Boolean.TRUE.equals(inBody);
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

    /**
     * Factory method to create a ParameterDTO from a ParameterEntity
     */
    public static ParameterDTO fromEntity(ParameterEntity entity) {
        if (entity == null) return null;

        ParameterDTO dto = new ParameterDTO();

        // Map all fields from entity
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setKey(entity.getKey());
        dto.setDbColumn(entity.getDbColumn());
        dto.setDbParameter(entity.getDbParameter());
        dto.setParameterType(entity.getParameterType());
        dto.setOracleType(entity.getOracleType());
        dto.setApiType(entity.getApiType());
        dto.setParameterLocation(entity.getParameterLocation());
        dto.setRequired(entity.getRequired());
        dto.setDescription(entity.getDescription());
        dto.setDefaultValue(entity.getDefaultValue());
        dto.setExample(entity.getExample());
        dto.setValue(entity.getValue());
        dto.setFormat(entity.getFormat());
        dto.setValidationPattern(entity.getValidationPattern());
        dto.setInBody(entity.getInBody());
        dto.setIsPrimaryKey(entity.getIsPrimaryKey());
        dto.setParamMode(entity.getParamMode());
        dto.setEnabled(entity.isEnabled());
        dto.setPosition(entity.getPosition());

        if (entity.getEndpoint() != null) {
            dto.setEndpointId(entity.getEndpoint().getId());
        }

        // Derive legacy fields
        dto.setType(entity.getParameterType() != null ?
                entity.getParameterType() : entity.getApiType() != null ?
                entity.getApiType() : "string");
        dto.setIn(entity.getParameterLocation() != null ?
                entity.getParameterLocation() : "query");

        return dto;
    }
}