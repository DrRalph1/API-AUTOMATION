package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

import java.util.List;

// Request/Response DTOs
@Data
public class CollectionsListResponse {
    private List<CollectionDto> collections;
    private int totalCount;

    public CollectionsListResponse(List<CollectionDto> collections, int totalCount) {
        this.collections = collections;
        this.totalCount = totalCount;
    }
}