package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchemaObjectsResponse {
    private List<SchemaObjectDto> objects;
    private String objectType;
    private Integer totalCount;
    private String schema;
    private String lastUpdated;

    public SchemaObjectsResponse(List<SchemaObjectDto> objects, String objectType, Integer totalCount) {
        this.objects = objects;
        this.objectType = objectType;
        this.totalCount = totalCount;
        this.lastUpdated = java.time.LocalDateTime.now().toString();
    }
}