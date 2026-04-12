package com.usg.autoAPIGenerator.repositories.apiSecurity;

import com.usg.autoAPIGenerator.entities.postgres.apiSecurity.IPWhitelistEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPWhitelistEntryRepository extends JpaRepository<IPWhitelistEntryEntity, String> {

    List<IPWhitelistEntryEntity> findByStatus(String status);

    // Find all entries ordered by created date descending (most recent first)
    List<IPWhitelistEntryEntity> findAllByOrderByCreatedAtDesc();

    // Find all entries ordered by updated date descending (most recently updated first)
    List<IPWhitelistEntryEntity> findAllByOrderByUpdatedAtDesc();

    // Find all entries ordered by name ascending (A to Z)
    List<IPWhitelistEntryEntity> findAllByOrderByNameAsc();

    // Find all entries ordered by name descending (Z to A)
    List<IPWhitelistEntryEntity> findAllByOrderByNameDesc();

    // Find active entries ordered by created date descending
    List<IPWhitelistEntryEntity> findByStatusIgnoreCaseOrderByCreatedAtDesc(String status);

    // Count active entries (case-insensitive)
    @Query("SELECT COUNT(e) FROM IPWhitelistEntryEntity e WHERE UPPER(e.status) = 'ACTIVE'")
    long countActiveEntries();

    // Find entries by IP range (for checking duplicates)
    Optional<IPWhitelistEntryEntity> findByIpRange(String ipRange);

    // Find entries by name (for checking duplicates)
    Optional<IPWhitelistEntryEntity> findByName(String name);
}