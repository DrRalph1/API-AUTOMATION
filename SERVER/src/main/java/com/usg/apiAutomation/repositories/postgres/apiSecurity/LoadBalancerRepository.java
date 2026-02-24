package com.usg.apiAutomation.repositories.postgres.apiSecurity;

import com.usg.apiAutomation.entities.postgres.apiSecurity.LoadBalancerEntity;  // Import from entities.apiSecurity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoadBalancerRepository extends JpaRepository<LoadBalancerEntity, String> {
    List<LoadBalancerEntity> findByStatus(String status);

    @Query("SELECT SUM(l.totalConnections) FROM LoadBalancerEntity l")
    Long getTotalConnections();
}