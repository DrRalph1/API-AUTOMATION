package com.usg.apiAutomation.entities.postgres.userManagement;

import com.usg.apiAutomation.entities.postgres.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "UserDeviceEntity")
@Table(name = "tb_user_devices")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "device_id", updatable = false, nullable = false)
    private UUID deviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    @Column(name = "device_details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String deviceDetails;

    @Column(name = "created_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
}