package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_eng_parameters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiParameterEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @ManyToOne
    @JoinColumn(name = "api_id")
    private GeneratedApiEntity generatedApi;

    @Column(name = "param_key")
    private String key;

    @Column(name = "db_column")
    private String dbColumn;

    @Column(name = "db_parameter")
    private String dbParameter;

    @Column(name = "oracle_type")
    private String oracleType;

    @Column(name = "api_type")
    private String apiType;

    @Column(name = "parameter_type")
    private String parameterType;

    @Column(name = "required")
    private Boolean required;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "example")
    private String example;

    @Column(name = "validation_pattern")
    private String validationPattern;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "position")
    private Integer position;
}