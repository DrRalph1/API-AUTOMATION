package com.usg.apiAutomation.entities.postgres.collections;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity(name = "EnvironmentVariablesEntityCollections")
@Table(name = "tb_col_environment_variables")
@Data
@ToString(exclude = {"environment"})
public class EnvironmentVariableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String key;

    @Column(nullable = false, length = 2000)
    private String value;

    @Column(nullable = false)
    private String type;

    @Column(name = "is_enabled")
    private boolean isEnabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "environment_id", nullable = false)
    private EnvironmentEntity environment;
}