package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnDto {
    private String name;
    private String type;
    private Integer length;
    private Integer precision;
    private Integer scale;
    private String nullable;
    private Integer position;
    private String key;
    private String defaultValue;
    private String comment;
    private String charset;
    private String collation;
    private String autoIncrement;
    private String generated;
    private String virtual;
    private String hidden;
    private String identity;
    private Long charLength;
    private Long byteLength;
    private String dataDefault;
    private String lowValue;
    private String highValue;
}