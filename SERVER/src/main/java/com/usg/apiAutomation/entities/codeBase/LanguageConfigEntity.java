package com.usg.apiAutomation.entities.codeBase;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity(name = "LanguageConfigEntity")
@Table(name = "tb_cbase_language_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String language;

    @Column(nullable = false)
    private String displayName;

    private String framework;

    private String color;

    private String icon;

    private String command;

    private String packageManager;

    @Column(name = "file_extension")
    private String fileExtension;

    @Column(name = "formatter_name")
    private String formatterName;

    @Column(columnDefinition = "jsonb")
    private String components;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}