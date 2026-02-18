package com.usg.apiAutomation.dtos.apiSecurity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecurityConfigurationDTO {
    private String id;
    private String key;
    private String value;
    private String dataType;
    private String description;
    private String updatedAt;
    private String updatedBy;
    private String createdAt;
    private String configKey;
    private String configValue;

    // Nested configuration object for complex configurations
    private Map<String, Object> configuration;

    // Validation flags
    private Boolean isValid;
    private String validationMessage;

    // Category for grouping configurations
    private String category;

    // Is this configuration editable
    private Boolean isEditable;

    // Default value if applicable
    private String defaultValue;

    // Allowed values for enum/dropdown type configurations
    private String[] allowedValues;

    // Minimum value for numeric configurations
    private String minValue;

    // Maximum value for numeric configurations
    private String maxValue;

    // Constructor for simple key-value pair
    public SecurityConfigurationDTO(String key, String value) {
        this.key = key;
        this.value = value;
        this.dataType = "string";
    }

    // Constructor for typed configuration
    public SecurityConfigurationDTO(String key, String value, String dataType) {
        this.key = key;
        this.value = value;
        this.dataType = dataType;
    }

    // Constructor with full details
    public SecurityConfigurationDTO(String id, String key, String value, String dataType,
                                    String description, String category, Boolean isEditable) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.dataType = dataType;
        this.description = description;
        this.category = category;
        this.isEditable = isEditable;
    }
}