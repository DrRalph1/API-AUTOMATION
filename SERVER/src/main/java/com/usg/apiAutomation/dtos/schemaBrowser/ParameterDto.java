package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParameterDto {
    private String name;
    private String type; // IN, OUT, IN OUT
    private String dataType;
    private Integer position;
    private String defaultValue;
    private String comment;
    private Integer length;
    private Integer precision;
    private Integer scale;
    private String mode;
    private Boolean noCopy;
    private String charset;
    private String collation;
    private Boolean optional;
}