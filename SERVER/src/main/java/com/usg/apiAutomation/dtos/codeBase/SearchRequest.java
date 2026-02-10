package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    @NotBlank(message = "Search query is required")
    private String query;

    private String language;
    private String collectionId;
    private String method;
    private List<String> tags;
    private Integer page;
    private Integer pageSize;
    private String sortBy;
    private String sortOrder;
}