package com.usg.autoAPIGenerator.dtos.collections;

import lombok.Data;

@Data
public class ImportResponseDTO {
    private String collectionId;
    private String message;

    public ImportResponseDTO(String collectionId, String message) {
        this.collectionId = collectionId;
        this.message = message;
    }
}
