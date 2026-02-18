package com.usg.apiAutomation.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "tb_app_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_username", columnNames = {"username"})
        },
        indexes = {
                @Index(name = "idx_username", columnList = "username"),
                @Index(name = "idx_user_is_active", columnList = "is_active"),
                @Index(name = "idx_is_default_password", columnList = "is_default_password")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @Column(name = "user_id", updatable = false, nullable = false)
    private String userId;

    @Column(name = "username", nullable = false, unique = true, length = 150)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @Column(name = "phone_number", length = 16)
    private String phoneNumber;

    // ✅ Changed from email_address to email for consistency
    @Column(name = "email_address", length = 255)
    private String emailAddress;

    // ✅ Add missing fields
    @Column(name = "staff_id", length = 50)
    private String staffId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_role"))
    private UserRoleEntity role;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_default_password", nullable = false)
    @Builder.Default
    private Boolean isDefaultPassword = true;

    // ✅ LOCKOUT SECURITY FIELDS
    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private Instant accountLockedUntil;

    // ✅ LAST LOGIN (NEW FIELD)
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Column(name = "mfa_enabled", nullable = false)
    @Builder.Default
    private Boolean mfaEnabled = false;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    @Builder.Default
    private Boolean phoneVerified = false;

    @Column(name = "security_score")
    private Integer securityScore;

    @Column(name = "avatar_color", length = 20)
    private String avatarColor;

    @Column(name = "timezone", length = 50)
    private String timezone;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (isDefaultPassword == null) {
            isDefaultPassword = true;
        }
        if (failedLoginAttempts == null) {
            failedLoginAttempts = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}