package com.usg.apiAutomation.entities.postgres.documentation;

import com.usg.apiAutomation.helpers.HashMapConverter;
import lombok.Data;
import jakarta.persistence.*;
import java.util.Map;

@Data
@Entity
@Table(name = "tb_doc_settings")
public class DocumentationSettingsEntity {
    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String userId;
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

    @Lob
    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> customSettings;
}