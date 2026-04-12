package com.usg.autoAPIGenerator.dtos.documentation;

import com.usg.autoAPIGenerator.entities.postgres.documentation.APICollectionEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CollectionDetailsWithEndpointsDTO {
    private String id;
    private String name;
    private String description;
    private String version;
    private String owner;
    private String type;
    private String createdAt;
    private String updatedAt;
    private String color;
    private String status;
    private String baseUrl;
    private List<String> tags;
    private boolean isFavorite;
    private boolean isExpanded;

    // Folders with their endpoints
    private List<FolderWithEndpointsDTO> folders;

    // Summary counts (primitives, cannot be null)
    private int totalFolders;
    private int totalEndpoints;

    // Metadata
    private Map<String, Object> metadata;

    // Constructor that takes APICollectionEntity and folders
    public CollectionDetailsWithEndpointsDTO(APICollectionEntity entity, List<FolderWithEndpointsDTO> folders) {
        if (entity != null) {
            this.id = entity.getId();
            this.name = entity.getName();
            this.description = entity.getDescription();
            this.version = entity.getVersion();
            this.owner = entity.getOwner();
            this.type = entity.getType();
            this.createdAt = formatDate(entity.getCreatedAt());
            this.updatedAt = formatDate(entity.getUpdatedAt());
            this.color = entity.getColor();
            this.status = entity.getStatus();
            this.baseUrl = entity.getBaseUrl();
            this.tags = entity.getTags() != null ? new ArrayList<>(entity.getTags()) : new ArrayList<>();
            this.isFavorite = entity.isFavorite();
            this.isExpanded = entity.isExpanded();

            // These are primitive ints, so they can't be null
            // Just assign them directly
            this.totalFolders = entity.getTotalFolders();
            this.totalEndpoints = entity.getTotalEndpoints();
        }

        this.folders = folders != null ? folders : new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return dateTime.format(formatter);
    }
}