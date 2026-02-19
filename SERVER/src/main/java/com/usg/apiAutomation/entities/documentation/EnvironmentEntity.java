package com.usg.apiAutomation.entities.documentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.usg.apiAutomation.helpers.HashMapConverter;
import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Entity
@Table(name = "tb_doc_environments")
public class EnvironmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String baseUrl;

    @Column(name = "is_active")  // CRITICAL: Map to database column name
    @JsonProperty("active")      // Keep JSON field name as "active"
    private boolean active;

    private String description;
    private String apiKey;
    private String secret;

    @Lob
    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> variables;

    private String createdBy;

    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}