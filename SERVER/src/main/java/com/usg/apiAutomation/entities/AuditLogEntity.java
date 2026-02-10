package com.usg.apiAutomation.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "tb_audit_logs",
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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "audit_id", updatable = false, nullable = false)
    private UUID auditId;

    @Column(name = "user_id", nullable = false)
    private String userId;  // ID of the userManagement who performed the action

    @Column(name = "action", nullable = false, length = 200)
    private String action;  // action performed

    @Column(name = "operation", nullable = false, length = 200)
    private String operation; // operation name or code

    @Column(name = "details", length = 2000)
    private String details; // additional optional details

    @Column(name = "is_success", nullable = false)
    @Builder.Default
    private Boolean isSuccess = true;

    @Column(name = "created_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isSuccess == null) {
            isSuccess = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}
