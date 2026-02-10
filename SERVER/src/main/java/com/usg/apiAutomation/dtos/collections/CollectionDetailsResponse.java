package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

import java.util.List;

@Data
public class CollectionDetailsResponse {
    private String collectionId;
    private String name;
    private String description;
    private String createdAt;
    private String updatedAt;
    private int totalRequests;
    private int totalFolders;
    private boolean isFavorite;
    private String owner;
    private List<VariableDto> variables;
    private List<FolderDto> folders;
    private String comments;
    private String lastActivity;
}