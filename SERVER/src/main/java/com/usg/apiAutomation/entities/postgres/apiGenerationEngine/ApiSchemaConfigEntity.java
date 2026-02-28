package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_eng_schema_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiSchemaConfigEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @OneToOne
    @JoinColumn(name = "api_id")
    private GeneratedApiEntity generatedApi;

    @Column(name = "schema_name")
    private String schemaName;

    @Column(name = "object_type")
    private String objectType;

    @Column(name = "object_name")
    private String objectName;

    @Column(name = "operation")
    private String operation;

    @Column(name = "primary_key_column")
    private String primaryKeyColumn;

    @Column(name = "sequence_name")
    private String sequenceName;

    @Column(name = "enable_pagination")
    private Boolean enablePagination;

    @Column(name = "page_size")
    private Integer pageSize;

    @Column(name = "enable_sorting")
    private Boolean enableSorting;

    @Column(name = "default_sort_column")
    private String defaultSortColumn;

    @Column(name = "default_sort_direction")
    private String defaultSortDirection;

    @Column(name = "is_synonym")
    private Boolean isSynonym;

    @Column(name = "target_type")
    private String targetType;

    @Column(name = "target_name")
    private String targetName;

    @Column(name = "target_owner")
    private String targetOwner;
}