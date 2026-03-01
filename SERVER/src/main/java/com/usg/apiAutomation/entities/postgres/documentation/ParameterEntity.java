package com.usg.apiAutomation.entities.postgres.documentation;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "ParameterEntityDocumentation")
@Table(name = "tb_doc_parameters")
@Data
@NoArgsConstructor
public class ParameterEntity {

    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // string, integer, boolean, etc.

    @Column(name = "param_in", nullable = false)  // Changed from "in" to "param_in"
    private String in; // path, query, header, body

    @Column(name = "is_required")
    private boolean isRequired;

    @Column(length = 1000)
    private String description;

    @Column(name = "default_value")
    private String defaultValue;

    private String example;

    private String format;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id", nullable = false)
    private APIEndpointEntity endpoint;
}