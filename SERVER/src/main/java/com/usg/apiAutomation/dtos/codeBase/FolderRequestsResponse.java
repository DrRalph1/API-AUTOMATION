package com.usg.apiAutomation.dtos.codeBase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderRequestsResponse {
    private String collectionId;
    private String folderId;
    private List<RequestItem> requests;
    private int total;
}