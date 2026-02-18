package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private String id;
    private String name;
    private String description;
    private List<String> permissions;
    private String color;
    private boolean isDefault;
    private int userCount;
    private Date createdAt;
    private Date updatedAt;
    private Map<String, Object> metadata;
}