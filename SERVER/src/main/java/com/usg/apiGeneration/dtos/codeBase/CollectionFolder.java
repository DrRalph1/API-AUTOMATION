package com.usg.apiGeneration.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionFolder {
    private String id;
    private String name;
    private String description;
    private Boolean isExpanded;
    private Boolean hasRequests;
    private int requestCount;
    private List<RequestItem> requests;  // Added field to store requests under this folder
}