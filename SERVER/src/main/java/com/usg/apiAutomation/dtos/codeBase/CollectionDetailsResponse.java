package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDetailsResponse {
    private String id;
    private String name;
    private String description;
    private String version;
    private String owner;
    private String createdAt;
    private String updatedAt;
    private Boolean isExpanded;
    private Boolean isFavorite;
    private List<CollectionFolder> folders;
    private int totalRequests;
}