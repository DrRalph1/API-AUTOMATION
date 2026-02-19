package com.usg.apiAutomation.dtos.collections;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCollectionDTO {

    @NotBlank(message = "CollectionEntity name is required")
    @Size(min = 1, max = 100, message = "CollectionEntity name must be between 1 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    // Optional fields for more advanced updates
    private Boolean isPublic;

    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    private String[] tags;

    // Version information for optimistic locking (optional)
    private Integer version;
}