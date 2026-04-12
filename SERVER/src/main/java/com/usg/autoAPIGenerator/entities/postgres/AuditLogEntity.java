package com.usg.autoAPIGenerator.entities.postgres;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "tb_sys_audit_logs",
        indexes = {
                @Index(name = "idx_audit_user", columnList = "user_id"),
                @Index(name = "idx_audit_action", columnList = "action"),
                @Index(name = "idx_audit_operation", columnList = "operation"),
                @Index(name = "idx_audit_created_date", columnList = "created_date")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Better UUID generation
    @Column(name = "audit_id", columnDefinition = "UUID")
    private UUID auditId;

    @Version
    @Column(name = "version")
    private Long version; // Add version field for optimistic locking

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "action", nullable = false, length = 200)
    private String action;

    @Column(name = "operation", nullable = false, length = 200)
    private String operation;

    @Column(name = "details", length = 2000)
    private String details;

    @Column(name = "is_success", nullable = false)
    @Builder.Default
    private Boolean isSuccess = true;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isSuccess == null) {
            isSuccess = true;
        }
        if (version == null) {
            version = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}