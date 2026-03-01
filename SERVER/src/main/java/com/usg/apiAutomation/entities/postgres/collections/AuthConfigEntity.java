package com.usg.apiAutomation.entities.postgres.collections;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "AuthConfigsEntityCollections")
@Table(name = "tb_col_auth_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class AuthConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id;

    @Column(name = "type")
    @EqualsAndHashCode.Include
    @ToString.Include
    private String type;

    @Column(name = "auth_key")
    @EqualsAndHashCode.Include
    @ToString.Include
    private String key;

    @Column(name = "value")
    private String value;

    @Column(name = "token")
    private String token;

    @Column(name = "token_type")
    private String tokenType;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "add_to")
    private String addTo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RequestEntity request;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        // Ensure type is set
        if (type == null) {
            type = "none";
        }
    }
}