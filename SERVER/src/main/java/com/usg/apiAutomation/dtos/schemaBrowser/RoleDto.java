package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {
    private String roleName;
    private String passwordRequired;
    private String authenticationType;
    private String common;
    private String oracleMaintained;
    private String implicit;
    private String inherited;
    private List<String> grantedRoles;
    private List<String> grantedPrivileges;
    private String created;
}