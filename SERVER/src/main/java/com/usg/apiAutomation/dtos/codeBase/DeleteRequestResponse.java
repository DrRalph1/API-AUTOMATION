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
public class DeleteRequestResponse {
    private String requestId;
    private String requestName;
    private String collectionId;
    private String folderId;
    private Date deletedAt;
    private int deletedImplementations;
    private String status;
}