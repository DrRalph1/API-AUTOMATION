package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

@Data
public class SaveRequestResponseDTO {
    private String requestId;
    private String message;

    public SaveRequestResponseDTO(String requestId, String message) {
        this.requestId = requestId;
        this.message = message;
    }
}