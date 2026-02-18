package com.usg.apiAutomation.dtos.codeBase;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCollectionRequest {
    @NotBlank
    private String name;

    private String description;

    private String version;

    private String owner;

    private Boolean isFavorite = false;

    private Boolean isExpanded = false;
}