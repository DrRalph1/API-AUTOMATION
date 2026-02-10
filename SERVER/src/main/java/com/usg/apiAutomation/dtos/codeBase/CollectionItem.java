package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionItem {
    private String id;
    private String name;
    private String description;
    private String version;
    private String owner;
    private String updatedAt;
    private Boolean isExpanded;
    private Boolean isFavorite;
    private int requestCount;
    private int folderCount;
}