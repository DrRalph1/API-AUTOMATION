package com.usg.apiAutomation.dtos.documentation;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderWithEndpointsDTO {
    private String id;
    private String name;
    private String description;
    private String collectionId;
    private String parentFolderId;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Endpoints in this folder
    private List<APIEndpointDTO> endpoints;

    // Summary
    private int endpointCount;

    // For nested folders (if needed)
    private List<FolderWithEndpointsDTO> subFolders;

    // Metadata
    private Map<String, Object> metadata;
}