package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublishImplementationResponse {
    private String language;
    private String requestId;
    private String collectionId;
    private String target;
    private Date publishedAt;
    private String status;
    private Map<String, Object> publishData;
    private String publishId;
    private String url;
    private String version;
}