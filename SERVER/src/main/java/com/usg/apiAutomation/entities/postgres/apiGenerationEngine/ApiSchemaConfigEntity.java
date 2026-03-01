package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tb_eng_schema_configs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiSchemaConfigEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiSchemaConfigEntity that = (ApiSchemaConfigEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(schemaName, that.schemaName) &&
                Objects.equals(objectType, that.objectType) &&
                Objects.equals(objectName, that.objectName) &&
                Objects.equals(operation, that.operation) &&
                Objects.equals(primaryKeyColumn, that.primaryKeyColumn) &&
                Objects.equals(sequenceName, that.sequenceName) &&
                Objects.equals(enablePagination, that.enablePagination) &&
                Objects.equals(pageSize, that.pageSize) &&
                Objects.equals(enableSorting, that.enableSorting) &&
                Objects.equals(defaultSortColumn, that.defaultSortColumn) &&
                Objects.equals(defaultSortDirection, that.defaultSortDirection) &&
                Objects.equals(isSynonym, that.isSynonym) &&
                Objects.equals(targetType, that.targetType) &&
                Objects.equals(targetName, that.targetName) &&
                Objects.equals(targetOwner, that.targetOwner) &&
                Objects.equals(generatedApi != null ? generatedApi.getId() : null,
                        that.generatedApi != null ? that.generatedApi.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, schemaName, objectType, objectName, operation,
                primaryKeyColumn, sequenceName, enablePagination, pageSize,
                enableSorting, defaultSortColumn, defaultSortDirection,
                isSynonym, targetType, targetName, targetOwner,
                generatedApi != null ? generatedApi.getId() : null);
    }

    @Override
    public String toString() {
        return "ApiSchemaConfigEntity{" +
                "id='" + id + '\'' +
                ", apiId='" + (generatedApi != null ? generatedApi.getId() : null) + '\'' +
                ", schemaName='" + schemaName + '\'' +
                ", objectType='" + objectType + '\'' +
                ", objectName='" + objectName + '\'' +
                ", operation='" + operation + '\'' +
                ", primaryKeyColumn='" + primaryKeyColumn + '\'' +
                ", sequenceName='" + sequenceName + '\'' +
                ", enablePagination=" + enablePagination +
                ", pageSize=" + pageSize +
                ", enableSorting=" + enableSorting +
                ", defaultSortColumn='" + defaultSortColumn + '\'' +
                ", defaultSortDirection='" + defaultSortDirection + '\'' +
                ", isSynonym=" + isSynonym +
                ", targetType='" + targetType + '\'' +
                ", targetName='" + targetName + '\'' +
                ", targetOwner='" + targetOwner + '\'' +
                '}';
    }
}