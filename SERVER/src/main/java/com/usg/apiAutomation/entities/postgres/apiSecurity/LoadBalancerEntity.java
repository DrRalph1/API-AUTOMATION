package com.usg.apiAutomation.entities.postgres.apiSecurity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "LoadBalancerEntity")
@Table(name = "tb_sec_load_balancer")
public class LoadBalancerEntity {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String algorithm;

    @Getter
    @Setter
    private String healthCheck;

    @Getter
    @Setter
    private Integer healthCheckInterval;

    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    private Integer totalConnections;

    @Getter
    @Setter
    private LocalDateTime createdAt;

    @Getter
    @Setter
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "loadBalancer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    @Setter
    private List<LoadBalancerServerEntity> servers = new ArrayList<>();
}