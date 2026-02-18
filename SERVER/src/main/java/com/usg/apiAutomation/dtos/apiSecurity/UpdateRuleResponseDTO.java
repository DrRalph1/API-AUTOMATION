package com.usg.apiAutomation.dtos.apiSecurity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateRuleResponseDTO {

    private String id;
    private String name;
    private String endpoint;
    private Integer limit;
    private String window;
    private String status;
    private String updatedAt;
    private String message;

    // Additional fields that might be returned in the response
    private String description;
    private String method;
    private Integer burst;
    private String action;
    private String createdBy;
    private String updatedBy;

    /**
     * Constructor for success response with minimal fields
     */
    public static UpdateRuleResponseDTO success(String id, String name, String status, String updatedAt) {
        UpdateRuleResponseDTO response = new UpdateRuleResponseDTO();
        response.setId(id);
        response.setName(name);
        response.setStatus(status);
        response.setUpdatedAt(updatedAt);
        response.setMessage("Rate limit rule updated successfully");
        return response;
    }

    /**
     * Constructor for success response with full details
     */
    public static UpdateRuleResponseDTO successWithDetails(
            String id, String name, String endpoint, Integer limit,
            String window, String status, String updatedAt,
            String description, String method, Integer burst, String action) {
        UpdateRuleResponseDTO response = new UpdateRuleResponseDTO();
        response.setId(id);
        response.setName(name);
        response.setEndpoint(endpoint);
        response.setLimit(limit);
        response.setWindow(window);
        response.setStatus(status);
        response.setUpdatedAt(updatedAt);
        response.setDescription(description);
        response.setMethod(method);
        response.setBurst(burst);
        response.setAction(action);
        response.setMessage("Rate limit rule updated successfully");
        return response;
    }

    /**
     * Constructor for error response
     */
    public static UpdateRuleResponseDTO error(String message) {
        UpdateRuleResponseDTO response = new UpdateRuleResponseDTO();
        response.setMessage(message);
        return response;
    }
}