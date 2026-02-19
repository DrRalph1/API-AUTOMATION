package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentationSettingsDto {
    private boolean autoSave;
    private boolean darkMode;
    private String defaultLanguage;
    private String defaultEnvironment;
    private boolean showLineNumbers;
    private boolean wordWrap;
    private int fontSize;
    private String fontFamily;
    private String theme;
    private boolean showSidebar;
    private boolean compactMode;
    private Map<String, Object> customSettings;
}