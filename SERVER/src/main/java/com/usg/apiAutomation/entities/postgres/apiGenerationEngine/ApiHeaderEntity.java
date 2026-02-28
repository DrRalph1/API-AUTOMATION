package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_eng_headers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiHeaderEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @ManyToOne
    @JoinColumn(name = "api_id")
    private GeneratedApiEntity generatedApi;

    @Column(name = "header_key")
    private String key;

    @Column(name = "header_value")
    private String value;

    @Column(name = "required")
    private Boolean required;

    @Column(name = "description")
    private String description;

    @Column(name = "is_request_header")
    private Boolean isRequestHeader;

    @Column(name = "is_response_header")
    private Boolean isResponseHeader;
}