package com.usg.apiAutomation.entities.collections;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity(name = "AuthConfigsEntityCollections")
@Table(name = "tb_col_auth_configs")
@Data
@ToString(exclude = {"request"})
public class AuthConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String type;

    private String token;

    @Column(name = "token_type")
    private String tokenType;

    private String username;

    private String password;

    @Column(name = "auth_key")
    private String key;

    private String value;

    @Column(name = "add_to")
    private String addTo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", unique = true)
    private RequestEntity request;
}