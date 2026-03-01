package com.usg.apiAutomation.entities.postgres.collections;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity(name = "ParametersEntityCollections")
@Table(name = "tb_col_parameters")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class ParameterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id;

    @Column(nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String key;

    @Column(length = 2000)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String value;

    @Column(length = 1000)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String description;

    @Column(name = "is_enabled")
    private boolean isEnabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RequestEntity request;

    // Helper method to maintain bidirectional relationship
    public void setRequest(RequestEntity request) {
        this.request = request;
    }
}