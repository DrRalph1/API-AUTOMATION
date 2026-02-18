package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {
    private String id;
    private String name;
    private String description;
    private String category;
    private boolean dangerous;
    private List<String> dependsOn;
    private Map<String, Object> metadata;
}