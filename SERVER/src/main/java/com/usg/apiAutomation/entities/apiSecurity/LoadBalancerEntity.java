package com.usg.apiAutomation.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "tb_load_balancers")
public class LoadBalancerEntity {
    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String algorithm;
    private String healthCheck;
    private String healthCheckInterval;
    private String status;
    private Integer totalConnections;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "loadBalancer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<LoadBalancerServerEntity> servers = new ArrayList<>();

}