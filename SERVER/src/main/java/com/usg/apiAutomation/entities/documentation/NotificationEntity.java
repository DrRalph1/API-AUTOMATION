package com.usg.apiAutomation.entities.documentation;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity(name = "NotificationEntity")
@Table(name = "tb_doc_notifications")
@Data
@NoArgsConstructor
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String message;

    @Column(nullable = false)
    private String type; // info, warning, success, error

    @Column(name = "is_read")
    private boolean isRead;

    private String icon;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "collection_id")
    private String collectionId;

    @Column(name = "endpoint_id")
    private String endpointId;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}