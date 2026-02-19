package com.usg.apiAutomation.dtos.collections;

import lombok.Data;
import java.util.List;

@Data
public class CollectionDTO {
    private String id;
    private String name;
    private String description;
    private boolean isExpanded;
    private boolean isFavorite;
    private boolean isEditing;
    private String createdAt;
    private int requestsCount;
    private List<VariableDTO> variables;
    private List<FolderDTO> folders;
    private String owner;
    private String updatedAt;
    private int folderCount;
    private List<String> tags;
    private String color;
}