package com.usg.apiAutomation.repositories.postgres.apiSecurity;

// CORRECT IMPORT - from entities.apiSecurity, NOT dtos.apiSecurity
import com.usg.apiAutomation.entities.postgres.apiSecurity.RateLimitRuleEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RateLimitRuleRepository extends JpaRepository<RateLimitRuleEntity, String> {
    List<RateLimitRuleEntity> findByStatus(String status);

    @Query("SELECT COUNT(r) FROM RateLimitRuleEntity r WHERE r.status = 'active'")
    long countActiveRules();
}