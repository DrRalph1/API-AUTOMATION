package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteCollectionRequest {
    @NotBlank(message = "Collection ID is required")
    private String collectionId;

    private Boolean deleteRequests;
    private String confirmationToken;
}