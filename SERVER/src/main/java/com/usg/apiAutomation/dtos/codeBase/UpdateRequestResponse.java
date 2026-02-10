package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRequestResponse {
    private String requestId;
    private String name;
    private String method;
    private String url;
    private String description;
    private String collectionId;
    private String folderId;
    private Date updatedAt;
    private List<String> tags;
    private String status;
}