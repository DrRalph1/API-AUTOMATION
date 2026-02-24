package com.usg.apiAutomation.repositories.postgres.apiSecurity;

import com.usg.apiAutomation.entities.postgres.apiSecurity.SecurityConfigurationEntity;  // Fix import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityConfigurationRepository extends JpaRepository<SecurityConfigurationEntity, String> {
    SecurityConfigurationEntity findByConfigKey(String key);
}