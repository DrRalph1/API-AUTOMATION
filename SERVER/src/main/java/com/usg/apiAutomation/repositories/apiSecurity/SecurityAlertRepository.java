package com.usg.apiAutomation.repositories.postgres.apiSecurity;

import com.usg.apiAutomation.entities.postgres.apiSecurity.SecurityAlertEntity;  // Fix import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityAlertRepository extends JpaRepository<SecurityAlertEntity, String> {
    @Query("SELECT COUNT(a) FROM SecurityAlertEntity a WHERE a.isRead = false")
    long countUnreadAlerts();

    @Query("SELECT a FROM SecurityAlertEntity a WHERE a.isRead = false ORDER BY a.timestamp DESC")
    List<SecurityAlertEntity> findUnreadAlerts();
}