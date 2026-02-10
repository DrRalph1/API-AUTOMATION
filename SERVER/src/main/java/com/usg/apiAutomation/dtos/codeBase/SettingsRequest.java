package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsRequest {
    private Map<String, Object> codeStyle;
    private Map<String, Object> templates;
    private Map<String, Object> exportSettings;
    private Map<String, Object> security;
    private Map<String, Object> notifications;
    private Map<String, Object> preferences;
    private Boolean autoGenerateTests;
    private Boolean darkMode;
}