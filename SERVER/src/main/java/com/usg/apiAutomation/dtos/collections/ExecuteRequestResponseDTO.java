package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

import java.util.List;

@Data
public class ExecuteRequestResponseDTO {
    private String body;
    private int statusCode;
    private String statusText;
    private List<HeaderDTO> headers;
    private long timeMs;
    private long sizeBytes;

    public ExecuteRequestResponseDTO(String body, int statusCode, String statusText,
                                     List<HeaderDTO> headers, long timeMs, long sizeBytes) {
        this.body = body;
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.headers = headers;
        this.timeMs = timeMs;
        this.sizeBytes = sizeBytes;
    }
}