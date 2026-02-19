package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

import java.util.List;

// RequestEntity/Response DTOs
@Data
public class CollectionsListResponseDTO {
    private List<CollectionDTO> collections;
    private int totalCount;

    public CollectionsListResponseDTO(List<CollectionDTO> collections, int totalCount) {
        this.collections = collections;
        this.totalCount = totalCount;
    }
}