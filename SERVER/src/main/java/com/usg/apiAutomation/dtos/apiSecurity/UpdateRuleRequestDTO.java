package com.usg.apiAutomation.dtos.apiSecurity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateRuleRequestDTO {

    @Pattern(regexp = "^(?!\\s*$).+", message = "Rule name cannot be empty if provided")
    private String name;

    private String description;

    private String endpoint;

    @Pattern(regexp = "^(GET|POST|PUT|DELETE|PATCH|ALL)$",
            message = "Method must be one of: GET, POST, PUT, DELETE, PATCH, ALL")
    private String method;

    @Min(value = 1, message = "Limit must be greater than 0")
    private Integer limit;

    @Pattern(regexp = "^(\\d+[smhd]|\\d+)$",
            message = "Window must be a valid time string (e.g., 1s, 5m, 1h, 1d)")
    private String window;

    @Min(value = 0, message = "Burst must be non-negative")
    private Integer burst;

    @Pattern(regexp = "^(throttle|block)$",
            message = "Action must be either 'throttle' or 'block'")
    private String action;

    @Pattern(regexp = "^(active|inactive)$",
            message = "Status must be either 'active' or 'inactive'")
    private String status;

    // Optional additional options
    private Object options;

    /**
     * Custom validation method to check if at least one field is provided for update
     * This should be called in the service layer before processing the update
     * @return true if at least one field is present
     */
    public boolean hasAtLeastOneField() {
        return name != null ||
                description != null ||
                endpoint != null ||
                method != null ||
                limit != null ||
                window != null ||
                burst != null ||
                action != null ||
                status != null ||
                options != null;
    }

    /**
     * Get all fields that are present in the requestEntity (non-null)
     * @return Array of field names that are being updated
     */
    public String[] getPresentFields() {
        java.util.ArrayList<String> fields = new java.util.ArrayList<>();
        if (name != null) fields.add("name");
        if (description != null) fields.add("description");
        if (endpoint != null) fields.add("endpoint");
        if (method != null) fields.add("method");
        if (limit != null) fields.add("limit");
        if (window != null) fields.add("window");
        if (burst != null) fields.add("burst");
        if (action != null) fields.add("action");
        if (status != null) fields.add("status");
        if (options != null) fields.add("options");
        return fields.toArray(new String[0]);
    }

    /**
     * Builder style method to create requestEntity with name
     */
    public UpdateRuleRequestDTO withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Builder style method to create requestEntity with description
     */
    public UpdateRuleRequestDTO withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Builder style method to create requestEntity with endpoint
     */
    public UpdateRuleRequestDTO withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Builder style method to create requestEntity with method
     */
    public UpdateRuleRequestDTO withMethod(String method) {
        this.method = method;
        return this;
    }

    /**
     * Builder style method to create requestEntity with limit
     */
    public UpdateRuleRequestDTO withLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Builder style method to create requestEntity with window
     */
    public UpdateRuleRequestDTO withWindow(String window) {
        this.window = window;
        return this;
    }

    /**
     * Builder style method to create requestEntity with burst
     */
    public UpdateRuleRequestDTO withBurst(Integer burst) {
        this.burst = burst;
        return this;
    }

    /**
     * Builder style method to create requestEntity with action
     */
    public UpdateRuleRequestDTO withAction(String action) {
        this.action = action;
        return this;
    }

    /**
     * Builder style method to create requestEntity with status
     */
    public UpdateRuleRequestDTO withStatus(String status) {
        this.status = status;
        return this;
    }

    /**
     * Builder style method to create requestEntity with options
     */
    public UpdateRuleRequestDTO withOptions(Object options) {
        this.options = options;
        return this;
    }

    @Override
    public String toString() {
        return "UpdateRuleRequestDTO{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", method='" + method + '\'' +
                ", limit=" + limit +
                ", window='" + window + '\'' +
                ", burst=" + burst +
                ", action='" + action + '\'' +
                ", status='" + status + '\'' +
                ", options=" + options +
                '}';
    }
}