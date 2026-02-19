package com.usg.apiAutomation.dtos.documentation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.usg.apiAutomation.entities.documentation.APIEndpointEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIEndpointDTO {

    private String id;
    private String name;
    private String method;
    private String url;
    private String description;
    private List<String> tags;
    private String lastModified;
    private Boolean requiresAuth;
    private Boolean deprecated;
    private String folder;
    private String collectionId;
    private String category;
    private Map<String, Object> rateLimit;  // Changed from String to Map
    private List<HeaderDTO> headers;
    private String requestBody;
    private String apiVersion;

    // Static factory method to convert from entity
    public static APIEndpointDTO fromEntity(APIEndpointEntity entity) {
        if (entity == null) return null;

        APIEndpointDTO dto = new APIEndpointDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setMethod(entity.getMethod());
        dto.setUrl(entity.getUrl());
        dto.setDescription(entity.getDescription());
        dto.setTags(entity.getTags());

        if (entity.getUpdatedAt() != null) {
            dto.setLastModified(entity.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        dto.setRequiresAuth(entity.isRequiresAuth());
        dto.setDeprecated(entity.isDeprecated());
        dto.setFolder(entity.getFolder() != null ? entity.getFolder().getId() : null);
        dto.setCollectionId(entity.getCollection() != null ? entity.getCollection().getId() : null);
        dto.setCategory(entity.getCategory());
        dto.setRateLimit(entity.getRateLimit());  // Now this will work with Map type
        dto.setApiVersion(entity.getApiVersion());

        return dto;
    }
}