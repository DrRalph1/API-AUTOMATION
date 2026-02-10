package com.usg.apiAutomation.dtos.codeBase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestItem {
    private String id;
    private String name;
    private String method;
    private String description;
    private List<String> tags;
    private String lastModified;
}