package com.usg.apiGeneration.dtos.apiSecurity;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.util.List;

@Data
public class UpdateIPEntryRequestDTO {

    private String name;

    @Pattern(regexp = "^(\\*|([0-9]{1,3}\\.){3}[0-9]{1,3}(\\/[0-9]{1,2})?|([0-9]{1,3}\\.){1,3}\\*|([0-9]{1,3}\\.){0,3}\\*|([0-9a-fA-F:]+)(\\/[0-9]{1,3})?)$",
            message = "Invalid IP range format. Use: 192.168.1.100, 192.168.1.0/24, 192.168.1.*, or 192.168.*.*")
    private String ipRange;

    private String description;

    private List<String> endpoints;

    @Pattern(regexp = "^(active|inactive|pending)$",
            message = "Status must be active, inactive, or pending")
    private String status;
}