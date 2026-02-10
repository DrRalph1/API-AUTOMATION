package com.usg.apiAutomation.dtos.systemActivities.role;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleResultDTO {
    private String roleName;
    private String description;
    private String roleId; // null for duplicates (unless we fetch existing)
    private String status; // "CREATED" or "DUPLICATE" or "PENDING_CREATION"
    private String message; // Optional message
}