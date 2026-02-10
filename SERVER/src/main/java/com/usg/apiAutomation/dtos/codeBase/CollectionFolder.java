package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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
}