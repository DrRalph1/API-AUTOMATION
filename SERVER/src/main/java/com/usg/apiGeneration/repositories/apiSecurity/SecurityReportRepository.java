package com.usg.apiGeneration.repositories.apiSecurity;

import com.usg.apiGeneration.entities.postgres.apiSecurity.SecurityReportEntity;  // Import from entities.apiSecurity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecurityReportRepository extends JpaRepository<SecurityReportEntity, String> {

    Optional<SecurityReportEntity> findByReportId(String reportId);

}