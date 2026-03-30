package com.usg.apiGeneration.dtos.codeBase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeaderDTO {
    private String key;
    private String value;
    private String description;
}