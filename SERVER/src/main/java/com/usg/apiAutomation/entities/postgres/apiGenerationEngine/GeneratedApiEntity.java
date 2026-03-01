package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "tb_eng_generated_apis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedApiEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @Column(name = "api_name", nullable = false)
    private String apiName;

    @Column(name = "api_code", nullable = false, unique = true)
    private String apiCode;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "version")
    private String version;

    @Column(name = "status")
    private String status;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "base_path")
    private String basePath;

    @Column(name = "endpoint_path")
    private String endpointPath;

    @Column(name = "category")
    private String category;

    @Column(name = "owner")
    private String owner;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "total_calls")
    private Long totalCalls;

    @Column(name = "last_called_at")
    private LocalDateTime lastCalledAt;

    @OneToOne(mappedBy = "generatedApi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ApiSchemaConfigEntity schemaConfig;

    @OneToOne(mappedBy = "generatedApi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ApiAuthConfigEntity authConfig;

    @OneToOne(mappedBy = "generatedApi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ApiRequestConfigEntity requestConfig;

    @OneToOne(mappedBy = "generatedApi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ApiResponseConfigEntity responseConfig;

    @OneToOne(mappedBy = "generatedApi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ApiSettingsEntity settings;

    @OneToMany(mappedBy = "generatedApi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApiParameterEntity> parameters = new ArrayList<>();

    @OneToMany(mappedBy = "generatedApi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApiResponseMappingEntity> responseMappings = new ArrayList<>();

    @OneToMany(mappedBy = "generatedApi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApiHeaderEntity> headers = new ArrayList<>();

    @OneToMany(mappedBy = "generatedApi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApiTestEntity> tests = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tb_eng_tags", joinColumns = @JoinColumn(name = "api_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Column(name = "source_object_info", columnDefinition = "jsonb")
    private String sourceObjectInfo;
}