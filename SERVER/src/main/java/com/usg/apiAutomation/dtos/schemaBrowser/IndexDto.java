package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexDto {
    private String name;
    private String type;
    private String columns;
    private String uniqueness;
    private String status;
    private String tablespace;
    private Long leafBlocks;
    private Long distinctKeys;
    private Long avgLeafBlocksPerKey;
    private Long avgDataBlocksPerKey;
    private Long clusteringFactor;
    private String compression;
    private String prefixLength;
    private String visibility;
    private String partitioned;
    private String temporary;
    private String generated;
    private String secondary;
    private String bufferPool;
    private String lastAnalyzed;
}