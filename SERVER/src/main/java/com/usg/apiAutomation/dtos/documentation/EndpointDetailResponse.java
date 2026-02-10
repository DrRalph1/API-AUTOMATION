package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndpointDetailResponse {
    private String endpointId;
    private String name;
    private String method;
    private String url;
    private String description;
    private String category;
    private List<String> tags;
    private String lastModified;
    private String version;
    private boolean requiresAuthentication;
    private String rateLimit;
    private boolean deprecated;
    private List<HeaderDto> headers;
    private List<ParameterDto> parameters;
    private String requestBodyExample;
    private List<ResponseExampleDto> responseExamples;
    private Map<String, Object> rateLimitInfo;
    private List<ChangelogEntryDto> changelog;
    private String timestamp;

}