package com.usg.apiAutomation.repositories.apiSecurity;

import com.usg.apiAutomation.entities.apiSecurity.SecurityEventEntity;  // Fix import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEventEntity, String> {
    @Query("SELECT e FROM SecurityEventEntity e WHERE e.timestamp >= :since")
    List<SecurityEventEntity> findRecentEvents(LocalDateTime since);

    /**
     * Count recent critical security events (severity = 'critical' or 'high')
     * @param since DateTime to count events from
     * @return Count of critical events since the specified time
     */
    @Query("SELECT COUNT(e) FROM SecurityEventEntity e " +
            "WHERE e.timestamp >= :since " +
            "AND (e.severity = 'critical' OR e.severity = 'high')")
    long countRecentCriticalEvents(@Param("since") LocalDateTime since);
}