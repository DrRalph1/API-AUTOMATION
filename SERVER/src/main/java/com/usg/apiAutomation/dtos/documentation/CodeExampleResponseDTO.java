package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeExampleResponse {
    private String language;
    private String endpointId;
    private String codeExample;
    private String description;
    private String timestamp;

    public CodeExampleResponse(String language, String endpointId, String codeExample, String description) {
        this.language = language;
        this.endpointId = endpointId;
        this.codeExample = codeExample;
        this.description = description;
        this.timestamp = LocalDateTime.now().toString();
    }
}