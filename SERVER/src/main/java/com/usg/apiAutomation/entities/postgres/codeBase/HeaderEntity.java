package com.usg.apiAutomation.entities.postgres.codeBase;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "HeaderEntityCodeBase")
@Table(name = "tb_cbase_headers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeaderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    private String value;

    private String description;

    private Boolean required;

    private Boolean disabled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private RequestEntity requestEntity;
}