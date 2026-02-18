package com.usg.apiAutomation.repositories.apiSecurity;

import com.usg.apiAutomation.entities.apiSecurity.SecurityConfigurationEntity;  // Fix import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityConfigurationRepository extends JpaRepository<SecurityConfigurationEntity, String> {
    SecurityConfigurationEntity findByConfigKey(String key);
}