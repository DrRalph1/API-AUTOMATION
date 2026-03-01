package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionInfoDTO {
    private String collectionId;
    private String collectionName;
    private String collectionType;
    private String folderId;
    private String folderName;
    private Boolean isNewCollection;
    private Boolean isNewFolder;
}