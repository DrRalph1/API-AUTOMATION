package com.usg.apiAutomation.repositories.postgres;

import com.usg.apiAutomation.entities.postgres.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID>, JpaSpecificationExecutor<AuditLogEntity> {

    @Query("SELECT DISTINCT TRIM(a.action) FROM AuditLogEntity a WHERE a.action IS NOT NULL AND TRIM(a.action) <> '' ORDER BY TRIM(a.action)")
    List<String> findDistinctActions();

    @Query("SELECT DISTINCT TRIM(a.userId) FROM AuditLogEntity a WHERE a.userId IS NOT NULL AND TRIM(a.userId) <> '' ORDER BY TRIM(a.userId)")
    List<String> findDistinctUsers();

    @Query("SELECT DISTINCT TRIM(a.operation) FROM AuditLogEntity a WHERE a.operation IS NOT NULL AND TRIM(a.operation) <> '' ORDER BY TRIM(a.operation)")
    List<String> findDistinctOperations();

    // Alternative: Using UPPER for case-insensitive distinct
    @Query("SELECT DISTINCT UPPER(TRIM(a.action)) FROM AuditLogEntity a WHERE a.action IS NOT NULL AND TRIM(a.action) <> ''")
    List<String> findDistinctActionsCaseInsensitive();
}