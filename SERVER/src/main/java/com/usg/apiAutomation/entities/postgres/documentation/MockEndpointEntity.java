package com.usg.apiAutomation.entities.postgres.documentation;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity(name = "MockEndpointEntity")
@Table(name = "tb_doc_mock_endpoints")
@Data
@NoArgsConstructor
public class MockEndpointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false)
    private String path;

    @Column(name = "status_code")
    private int statusCode = 200;

    @Column(name = "response_delay")
    private int responseDelay; // in milliseconds

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_body", columnDefinition = "jsonb")
    private Map<String, Object> responseBody;

    private String description;

    @Column(name = "is_enabled")
    private boolean isEnabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mock_server_id", nullable = false)
    private MockServerEntity mockServer;

    @OneToMany(mappedBy = "mockEndpoint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HeaderEntity> responseHeaders = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_endpoint_id")
    private APIEndpointEntity sourceEndpoint;
}