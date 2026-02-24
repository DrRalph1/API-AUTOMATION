package com.usg.apiAutomation.entities.postgres.documentation;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "HeaderEntityDocumentation")
@Table(name = "tb_doc_headers")
@Data
@NoArgsConstructor
public class HeaderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
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