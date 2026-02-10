package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class APIEndpointResponse {
    private List<APIEndpointDto> endpoints;
    private String collectionId;
    private int totalEndpoints;
    private String timestamp;

    public APIEndpointResponse(List<APIEndpointDto> endpoints, String collectionId, int totalEndpoints) {
        this.endpoints = endpoints;
        this.collectionId = collectionId;
        this.totalEndpoints = totalEndpoints;
        this.timestamp = LocalDateTime.now().toString();
    }
}