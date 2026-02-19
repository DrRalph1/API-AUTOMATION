package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentResponseDTO {
    private List<EnvironmentDTO> environments;
    private String timestamp;

    public EnvironmentResponseDTO(List<EnvironmentDTO> environments) {
        this.environments = environments;
        this.timestamp = LocalDateTime.now().toString();
    }
}