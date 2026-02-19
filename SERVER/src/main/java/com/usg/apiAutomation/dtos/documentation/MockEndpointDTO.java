package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockEndpointDTO {
    private String id;
    private String method;
    private String path;
    private int statusCode;
    private int responseDelay; // in milliseconds
    private String responseBody;
    private String description;
    private List<HeaderDTO> responseHeaders;
    private boolean enabled;
}