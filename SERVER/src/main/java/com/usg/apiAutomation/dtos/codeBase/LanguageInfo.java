package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageInfo {
    private String id;
    private String name;
    private String framework;
    private String color;
    private String icon;
    private String command;
    private Boolean isAvailable;
    private int implementationCount;
}