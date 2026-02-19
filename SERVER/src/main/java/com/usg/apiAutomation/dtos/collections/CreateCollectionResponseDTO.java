package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

@Data
public class CreateCollectionResponseDTO {
    private String collectionId;
    private String message;

    public CreateCollectionResponseDTO(String collectionId, String message) {
        this.collectionId = collectionId;
        this.message = message;
    }
}