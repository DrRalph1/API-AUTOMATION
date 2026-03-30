package com.usg.apiGeneration.repositories.apiSecurity;

import com.usg.apiGeneration.entities.postgres.apiSecurity.IPWhitelistEntryEntity;  // Fix import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPWhitelistEntryRepository extends JpaRepository<IPWhitelistEntryEntity, String> {
    List<IPWhitelistEntryEntity> findByStatus(String status);

    @Query("SELECT COUNT(i) FROM IPWhitelistEntryEntity i WHERE i.status = 'active'")
    long countActiveEntries();
}