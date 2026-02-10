package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCollectionResponse {
    private String collectionId;
    private String name;
    private String description;
    private String version;
    private Date updatedAt;
    private Boolean isFavorite;
    private Boolean isExpanded;
    private String status;
}