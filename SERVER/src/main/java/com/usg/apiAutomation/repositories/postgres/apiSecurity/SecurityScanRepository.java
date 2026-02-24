package com.usg.apiAutomation.repositories.postgres.apiSecurity;

import com.usg.apiAutomation.entities.postgres.apiSecurity.SecurityScanEntity;  // Import from entities.apiSecurity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityScanRepository extends JpaRepository<SecurityScanEntity, String> {
}