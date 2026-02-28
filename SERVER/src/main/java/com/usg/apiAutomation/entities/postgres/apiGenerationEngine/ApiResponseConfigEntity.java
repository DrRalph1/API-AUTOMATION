package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_eng_response_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseConfigEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @OneToOne
    @JoinColumn(name = "api_id")
    private GeneratedApiEntity generatedApi;

    @Column(name = "success_schema", columnDefinition = "text")
    private String successSchema;

    @Column(name = "error_schema", columnDefinition = "text")
    private String errorSchema;

    @Column(name = "include_metadata")
    private Boolean includeMetadata;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "compression")
    private String compression;

    @ElementCollection
    @CollectionTable(name = "tb_eng_metadata_fields", joinColumns = @JoinColumn(name = "response_config_id"))
    @Column(name = "field_name")
    private java.util.List<String> metadataFields;
}