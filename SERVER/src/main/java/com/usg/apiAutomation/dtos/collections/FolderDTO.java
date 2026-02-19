package com.usg.apiAutomation.dtos.collections;

import lombok.Data;
import java.util.List;

@Data
public class FolderDTO {
    private String id;
    private String name;
    private String description;
    private boolean isExpanded;
    private boolean isEditing;
    private List<RequestDTO> requests;
    private int requestCount;
}