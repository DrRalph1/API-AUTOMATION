package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

@Data
public class SaveRequestDto {
    private String collectionId;
    private String folderId;
    private String requestId;
    private RequestDto request;
}