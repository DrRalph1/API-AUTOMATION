package com.usg.apiAutomation.entities.postgres.collections;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity(name = "HeadersEntityCollections")
@Table(name = "tb_col_headers")
@Data
@ToString(exclude = {"request"})
public class HeaderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String key;

    @Column(nullable = false, length = 2000)
    private String value;

    @Column(length = 1000)
    private String description;

    @Column(name = "is_enabled")
    private boolean isEnabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private RequestEntity request;
}