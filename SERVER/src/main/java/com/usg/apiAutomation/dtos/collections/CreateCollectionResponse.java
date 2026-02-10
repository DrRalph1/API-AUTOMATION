package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

@Data
public class CreateCollectionResponse {
    private String collectionId;
    private String message;

    public CreateCollectionResponse(String collectionId, String message) {
        this.collectionId = collectionId;
        this.message = message;
    }
}