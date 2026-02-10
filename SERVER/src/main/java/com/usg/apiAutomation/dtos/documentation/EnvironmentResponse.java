package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentResponse {
    private List<EnvironmentDto> environments;
    private String timestamp;

    public EnvironmentResponse(List<EnvironmentDto> environments) {
        this.environments = environments;
        this.timestamp = LocalDateTime.now().toString();
    }
}