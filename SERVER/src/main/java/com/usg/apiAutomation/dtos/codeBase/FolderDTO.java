package com.usg.apiAutomation.dtos.codeBase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FolderDTO {
    private String id;
    private String name;
    private String description;
    private Boolean isExpanded;
    private Boolean hasRequests;
    private Integer requestCount;
    private String collectionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}