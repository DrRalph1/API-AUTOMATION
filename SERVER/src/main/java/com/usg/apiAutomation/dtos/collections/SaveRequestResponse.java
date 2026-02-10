package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

@Data
public class SaveRequestResponse {
    private String requestId;
    private String message;

    public SaveRequestResponse(String requestId, String message) {
        this.requestId = requestId;
        this.message = message;
    }
}