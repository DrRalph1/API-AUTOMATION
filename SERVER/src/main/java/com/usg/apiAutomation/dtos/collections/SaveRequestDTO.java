package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

@Data
public class SaveRequestDTO {
    private String collectionId;
    private String folderId;
    private String requestId;
    private RequestDTO request;
}