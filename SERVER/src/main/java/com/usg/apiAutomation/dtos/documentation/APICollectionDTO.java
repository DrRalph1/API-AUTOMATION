package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class APICollectionDto {
    private String id;
    private String name;
    private String description;
    private String version;
    private String owner;
    private String type;
    private boolean isFavorite;
    private boolean isExpanded;
    private String updatedAt;
    private String createdAt;
    private int totalEndpoints;
    private int totalFolders;
    private String color;
    private String status;
    private String baseUrl;
    private List<String> tags;
    private Map<String, String> metadata;
}