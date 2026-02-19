package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class APICollectionResponseDTO {
    private List<APICollectionDTO> collections;
    private int totalCollections;
    private String timestamp;

    public APICollectionResponseDTO(List<APICollectionDTO> collections) {
        this.collections = collections;
        this.totalCollections = collections != null ? collections.size() : 0;
        this.timestamp = LocalDateTime.now().toString();
    }
}