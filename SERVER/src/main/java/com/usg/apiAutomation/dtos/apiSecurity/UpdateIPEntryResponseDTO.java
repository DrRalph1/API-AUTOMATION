package com.usg.apiAutomation.dtos.apiSecurity;

import lombok.Data;
import java.util.List;

@Data
public class UpdateIPEntryResponseDTO {
    private String id;
    private String name;
    private String ipRange;
    private String description;
    private List<String> endpoints;
    private String status;
    private String updatedAt;
    private String message;
}