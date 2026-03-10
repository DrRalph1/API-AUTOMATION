package com.usg.apiAutomation.dtos;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardApiResponseDTO {
    private String responseCode;
    private String message;
    private List<Map<String, Object>> data;
}