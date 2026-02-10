package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

@Data
public class ImportResponse {
    private String collectionId;
    private String message;

    public ImportResponse(String collectionId, String message) {
        this.collectionId = collectionId;
        this.message = message;
    }
}
