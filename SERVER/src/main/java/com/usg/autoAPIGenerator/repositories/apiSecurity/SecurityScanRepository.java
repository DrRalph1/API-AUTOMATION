package com.usg.autoAPIGenerator.repositories.apiSecurity;

import com.usg.autoAPIGenerator.entities.postgres.apiSecurity.SecurityScanEntity;  // Import from entities.apiSecurity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityScanRepository extends JpaRepository<SecurityScanEntity, String> {
}