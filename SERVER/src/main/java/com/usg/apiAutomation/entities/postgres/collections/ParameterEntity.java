package com.usg.apiAutomation.entities.postgres.collections;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity(name = "ParametersEntityCollections")
@Table(name = "tb_col_parameters")
@Data
@ToString(exclude = {"request"})
public class ParameterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String key;

    @Column(length = 2000)
    private String value;

    @Column(length = 1000)
    private String description;

    @Column(name = "is_enabled")
    private boolean isEnabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private RequestEntity request;
}