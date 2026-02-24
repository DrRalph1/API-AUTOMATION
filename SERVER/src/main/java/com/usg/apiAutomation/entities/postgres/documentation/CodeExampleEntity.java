package com.usg.apiAutomation.entities.postgres.documentation;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "CodeExampleEntity")
@Table(name = "tb_doc_code_examples")
@Data
@NoArgsConstructor
public class CodeExampleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String language;

    @Column(length = 10000)
    private String code;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id", nullable = false)
    private APIEndpointEntity endpoint;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_default")
    private boolean isDefault;
}