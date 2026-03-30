package com.usg.apiGeneration.repositories.apiSecurity;

import com.usg.apiGeneration.entities.postgres.apiSecurity.SecurityConfigurationEntity;  // Fix import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityConfigurationRepository extends JpaRepository<SecurityConfigurationEntity, String> {
    SecurityConfigurationEntity findByConfigKey(String key);
}