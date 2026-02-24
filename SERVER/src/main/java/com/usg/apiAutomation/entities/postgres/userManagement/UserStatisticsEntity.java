package com.usg.apiAutomation.entities.postgres.userManagement;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "UserStatisticsEntity")
@Table(name = "tb_user_statistics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_id")
    private Long statId;

    @Column(name = "total_users", nullable = false)
    @Builder.Default
    private Integer totalUsers = 0;

    @Column(name = "active_users", nullable = false)
    @Builder.Default
    private Integer activeUsers = 0;

    @Column(name = "pending_users", nullable = false)
    @Builder.Default
    private Integer pendingUsers = 0;

    @Column(name = "suspended_users", nullable = false)
    @Builder.Default
    private Integer suspendedUsers = 0;

    @Column(name = "admins_count", nullable = false)
    @Builder.Default
    private Integer adminsCount = 0;

    @Column(name = "developers_count", nullable = false)
    @Builder.Default
    private Integer developersCount = 0;

    @Column(name = "viewers_count", nullable = false)
    @Builder.Default
    private Integer viewersCount = 0;

    @Column(name = "mfa_enabled_users", nullable = false)
    @Builder.Default
    private Integer mfaEnabledUsers = 0;

    @Column(name = "avg_security_score")
    private Double avgSecurityScore;

    @Column(name = "new_users_24h", nullable = false)
    @Builder.Default
    private Integer newUsersLast24Hours = 0;

    @Column(name = "new_users_7d", nullable = false)
    @Builder.Default
    private Integer newUsersLast7Days = 0;

    @Column(name = "new_users_30d", nullable = false)
    @Builder.Default
    private Integer newUsersLast30Days = 0;

    @Column(name = "calculated_at", nullable = false)
    @Builder.Default
    private LocalDateTime calculatedAt = LocalDateTime.now();

    @Version
    @Column(name = "version")
    private Integer version;
}