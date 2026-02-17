package com.usg.apiAutomation.entities;

import com.usg.apiAutomation.dtos.apiSecurity.LoadBalancer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_load_balancer_servers")
public class LoadBalancerServerEntity {
    // Getters and Setters
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private String address;
    @Setter
    @Getter
    private String status;
    @Setter
    @Getter
    private Integer connections;

    @ManyToOne
    @JoinColumn(name = "load_balancer_id")
    private LoadBalancerEntity loadBalancerEntity;

    public LoadBalancer getLoadBalancer() { return loadBalancer; }
    public void setLoadBalancer(LoadBalancer loadBalancer) { this.loadBalancer = loadBalancer; }
}