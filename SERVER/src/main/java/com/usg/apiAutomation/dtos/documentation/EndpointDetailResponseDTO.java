package com.usg.apiAutomation.dtos.documentation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EndpointDetailResponseDTO {

    private String endpointId;
    private String name;
    private String method;
    private String url;
    private String description;
    private String category;
    private List<String> tags;
    private String lastModified;
    private String version;
    private Boolean requiresAuthentication;
    private Map<String, Object> rateLimit;  // Changed from String to Map
    private String formattedRateLimit;
    private Boolean deprecated;
    private List<HeaderDTO> headers;
    private List<ParameterDTO> parameters;
    private String requestBodyExample;
    private List<ResponseExampleDTO> responseExamples;
    private Map<String, Object> rateLimitInfo;
    private List<ChangelogEntryDTO> changelog;
    private Map<String, Object> metadata;
}