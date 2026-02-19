package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class APIEndpointResponseDTO {
    private List<APIEndpointDTO> endpoints;
    private String collectionId;
    private String folderId;
    private int totalEndpoints;
    private String timestamp;

    public APIEndpointResponseDTO(List<APIEndpointDTO> endpoints, String collectionId, String folderId, int totalEndpoints) {
        this.endpoints = endpoints;
        this.collectionId = collectionId;
        this.folderId = folderId;
        this.totalEndpoints = totalEndpoints;
        this.timestamp = LocalDateTime.now().toString();
    }
}