package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolesPermissionsResponse {
    private List<RoleDto> roles;
    private int totalRoles;
    private Date generatedAt;
    private Map<String, List<String>> permissionCategories;
    private List<PermissionDto> allPermissions;
}