package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDto {
    private String id;
    private String name;
    private String type;
    private String owner;
    private Integer score;
    private String snippet;
    private String lastModified;
    private String schema;
    private String database;
    private String objectId;
    private String subType;
    private String path;
    private String highlight;
    private String context;
}