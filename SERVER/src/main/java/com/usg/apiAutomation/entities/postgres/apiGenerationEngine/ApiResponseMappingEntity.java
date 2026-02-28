package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_eng_response_mappings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseMappingEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @ManyToOne
    @JoinColumn(name = "api_id")
    private GeneratedApiEntity generatedApi;

    @Column(name = "api_field")
    private String apiField;

    @Column(name = "db_column")
    private String dbColumn;

    @Column(name = "oracle_type")
    private String oracleType;

    @Column(name = "api_type")
    private String apiType;

    @Column(name = "format")
    private String format;

    @Column(name = "nullable")
    private Boolean nullable;

    @Column(name = "is_primary_key")
    private Boolean isPrimaryKey;

    @Column(name = "include_in_response")
    private Boolean includeInResponse;

    @Column(name = "position")
    private Integer position;
}