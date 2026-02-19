package com.usg.apiAutomation.dtos.documentation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.usg.apiAutomation.entities.documentation.FolderEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FolderDTO {

    private String id;
    private String name;
    private String description;
    private String collectionId;
    private String parentFolderId;
    private Integer endpointCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer displayOrder;

    // For nested folder structure
    private List<FolderDTO> subFolders;

    // For UI purposes (not in entity)
    private Integer totalEndpoints;
    private Boolean isExpanded;
    private Boolean isLoading;
    private String color;
    private List<APIEndpointDTO> endpoints;

    // Helper method to convert from entity
    public static FolderDTO fromEntity(FolderEntity entity) {
        if (entity == null) return null;

        FolderDTO dto = new FolderDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCollectionId(entity.getCollection() != null ? entity.getCollection().getId() : null);
        dto.setParentFolderId(entity.getParentFolder() != null ? entity.getParentFolder().getId() : null);
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Convert subfolders recursively
        if (entity.getSubFolders() != null && !entity.getSubFolders().isEmpty()) {
            dto.setSubFolders(entity.getSubFolders().stream()
                    .map(FolderDTO::fromEntity)
                    .toList());
        }

        return dto;
    }

}