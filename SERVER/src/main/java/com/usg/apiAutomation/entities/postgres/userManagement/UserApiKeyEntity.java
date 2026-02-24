package com.usg.apiAutomation.entities.postgres.userManagement;

import com.usg.apiAutomation.entities.postgres.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "UserApiKeyEntity")
@Table(name = "tb_user_api_keys")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserApiKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "api_key_id", updatable = false, nullable = false)
    private UUID apiKeyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "api_key", nullable = false, unique = true, length = 255)
    private String apiKey;

    @Column(name = "key_name", length = 100)
    private String keyName;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "permissions", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String permissions;

    @Column(name = "created_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
}