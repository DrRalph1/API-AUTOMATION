package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrantDto {
    private String grantee;
    private String privilege;
    private String grantable;
    private String grantor;
    private String objectType;
    private String objectName;
    private String hierarchy;
    private String common;
    private String inherited;
    private String adminOption;
}