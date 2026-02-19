package com.usg.apiAutomation.entities.documentation;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity(name = "ResponseExampleEntity")
@Table(name = "tb_doc_response_examples")
@Data
@NoArgsConstructor
public class ResponseExampleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    private String description;

    @Column(name = "content_type")
    private String contentType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "example", columnDefinition = "jsonb")
    private Map<String, Object> example;

    @Column(name = "schema_definition", length = 5000)
    private String schema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id", nullable = false)
    private APIEndpointEntity endpoint;

    @OneToMany(mappedBy = "responseExample", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HeaderEntity> headers = new ArrayList<>();
}