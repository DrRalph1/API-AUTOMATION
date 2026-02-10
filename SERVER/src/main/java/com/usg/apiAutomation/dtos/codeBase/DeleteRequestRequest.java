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
public class DeleteRequestRequest {
    @NotBlank(message = "Request ID is required")
    private String requestId;

    @NotBlank(message = "Collection ID is required")
    private String collectionId;

    private String folderId;
    private Boolean deleteImplementations;
}