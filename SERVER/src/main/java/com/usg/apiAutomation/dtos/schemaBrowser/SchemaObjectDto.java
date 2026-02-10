package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchemaObjectDto {
    private String id;
    private String name;
    private String type;
    private String owner;
    private String status;
    private Long rowCount;
    private String size;
    private String created;
    private String lastDDL;
    private String comment;
    private String tablespace;
    private String lastAnalyzed;
    private Long numRows;
    private Long blocks;
    private Long emptyBlocks;
    private Long avgSpace;
    private Long chainCnt;
    private Long avgRowLen;
    private String partitioned;
    private String temporary;
    private String secondary;
    private String nested;
    private String bufferPool;
    private Long sampleSize;
    private String isFavorite;
}