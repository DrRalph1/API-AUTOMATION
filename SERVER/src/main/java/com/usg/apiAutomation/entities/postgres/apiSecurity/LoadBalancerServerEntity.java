package com.usg.apiAutomation.entities.postgres.apiSecurity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "LoadBalancerServerEntity")
@Table(name = "tb_sec_load_balancer_servers")
public class LoadBalancerServerEntity {

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
    private String address;

    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    private Integer connections;

    @ManyToOne
    @JoinColumn(name = "load_balancer_id")
    @Getter
    @Setter
    private LoadBalancerEntity loadBalancer;  // This MUST be LoadBalancerEntity, not LoadBalancerDTO DTO
}