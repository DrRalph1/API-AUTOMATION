package com.usg.apiAutomation.dtos.collections;

import lombok.Data;
import java.util.List;

@Data
public class FolderDto {
    private String id;
    private String name;
    private String description;
    private boolean isExpanded;
    private boolean isEditing;
    private List<RequestDto> requests;
    private int requestCount;
}