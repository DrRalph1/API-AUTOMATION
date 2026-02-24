package com.usg.apiAutomation.entities.postgres;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "tb_api_validation",
        indexes = {
                @Index(name = "idx_api_key_secret_ip", columnList = "api_key, api_secret, server_ip")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiValidationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "micro_service_id", nullable = false, length = 100)
    private String microServiceId;

    @Column(name = "api_key", nullable = false, length = 200)
    private String apiKey;

    @Column(name = "api_secret", nullable = false, length = 200)
    private String apiSecret;

    @Column(name = "server_ip", nullable = false, length = 50)
    private String serverIp;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}
