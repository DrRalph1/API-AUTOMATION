package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublishDocumentationResponseDTO {
    private String publishedUrl;
    private String collectionId;
    private String message;
    private String timestamp;

    public PublishDocumentationResponseDTO(String publishedUrl, String collectionId, String message) {
        this.publishedUrl = publishedUrl;
        this.collectionId = collectionId;
        this.message = message;
        this.timestamp = LocalDateTime.now().toString();
    }
}