package com.usg.apiAutomation.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "tb_user_action_audit",
        indexes = {
                @Index(name = "idx_user_action_user", columnList = "user_id"),
                @Index(name = "idx_user_action_created_date", columnList = "created_date"),
                @Index(name = "idx_user_action_action", columnList = "action")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActionAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "audit_id", updatable = false, nullable = false)
    private UUID auditId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_action_user"))
    private AppUserEntity user;

    @Column(name = "action", nullable = false, length = 200)
    private String action;

    @Column(name = "request_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String requestData;

    @Column(name = "response_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String responseData;

    @Column(name = "created_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}
