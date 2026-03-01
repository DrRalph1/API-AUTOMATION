package com.usg.apiAutomation.entities.postgres.collections;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity(name = "VariablesEntityCollections")
@Table(name = "tb_col_variables")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class VariableEntity {

    @Id
    private String id;

    @Column(nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String key;

    @Column(nullable = false, length = 2000)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String value;

    @Column(nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String type;

    @Column(name = "is_enabled")
    private boolean isEnabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CollectionEntity collection;

    // Helper method to maintain bidirectional relationship
    public void setCollection(CollectionEntity collection) {
        this.collection = collection;
    }
}