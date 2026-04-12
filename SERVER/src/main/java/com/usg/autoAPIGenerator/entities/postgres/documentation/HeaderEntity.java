package com.usg.autoAPIGenerator.entities.postgres.documentation;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "HeaderEntityDocumentation")
@Table(name = "tb_doc_headers")
@Data
@NoArgsConstructor
public class HeaderEntity {

    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "api_id")
    private String generatedApiId;

    @Column(nullable = true)
    private String key;

    @Column(nullable = true)
    private String value;

    private String description;

    @Column(name = "is_required")
    private boolean isRequired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id")
    private APIEndpointEntity endpoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_example_id")
    private ResponseExampleEntity responseExample;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mock_endpoint_id")
    private MockEndpointEntity mockEndpoint;
}