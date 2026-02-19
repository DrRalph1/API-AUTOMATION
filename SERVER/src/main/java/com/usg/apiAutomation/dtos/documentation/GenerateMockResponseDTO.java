package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateMockResponse {
    private List<MockEndpointDto> mockEndpoints;
    private String collectionId;
    private String message;
    private String mockServerUrl;
    private String timestamp;

    public GenerateMockResponse(List<MockEndpointDto> mockEndpoints, String collectionId, String message) {
        this.mockEndpoints = mockEndpoints;
        this.collectionId = collectionId;
        this.message = message;
        this.mockServerUrl = "https://mock.fintech.com/" + UUID.randomUUID().toString().substring(0, 8);
        this.timestamp = LocalDateTime.now().toString();
    }
}