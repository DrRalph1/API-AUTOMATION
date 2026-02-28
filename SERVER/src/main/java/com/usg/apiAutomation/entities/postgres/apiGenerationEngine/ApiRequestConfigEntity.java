package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_eng_request_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequestConfigEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @OneToOne
    @JoinColumn(name = "api_id")
    private GeneratedApiEntity generatedApi;

    @Column(name = "schema_type")
    private String schemaType;

    @Column(name = "sample", columnDefinition = "text")
    private String sample;

    @Column(name = "max_size")
    private Long maxSize;

    @Column(name = "validate_schema")
    private Boolean validateSchema;

    @Column(name = "allowed_media_types", columnDefinition = "text")
    private String allowedMediaTypes;

    @ElementCollection
    @CollectionTable(name = "tb_eng_required_fields", joinColumns = @JoinColumn(name = "request_config_id"))
    @Column(name = "field_name")
    private java.util.List<String> requiredFields;
}