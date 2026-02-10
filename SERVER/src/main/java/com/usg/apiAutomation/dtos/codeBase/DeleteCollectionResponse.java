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
public class DeleteCollectionResponse {
    private String collectionId;
    private String collectionName;
    private Date deletedAt;
    private int deletedRequests;
    private int deletedFolders;
    private String status;
}