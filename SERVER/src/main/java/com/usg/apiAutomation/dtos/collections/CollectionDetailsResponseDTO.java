package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

import java.util.List;

@Data
public class CollectionDetailsResponseDTO {
    private String collectionId;
    private String name;
    private String description;
    private String createdAt;
    private String updatedAt;
    private int totalRequests;
    private int totalFolders;
    private boolean isFavorite;
    private String owner;
    private List<VariableDTO> variables;
    private List<FolderDTO> folders;
    private String comments;
    private String lastActivity;
}