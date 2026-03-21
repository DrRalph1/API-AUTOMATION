package com.usg.apiAutomation.repositories.postgres.collections;

import com.usg.apiAutomation.entities.postgres.collections.EnvironmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("EnvironmentRepositoryCollections")
public interface EnvironmentRepository extends JpaRepository<EnvironmentEntity, String> {

    List<EnvironmentEntity> findByOwner(String owner);

    @Query("SELECT e FROM EnvironmentsEntityCollections e LEFT JOIN FETCH e.variables WHERE e.id = :id")
    Optional<EnvironmentEntity> findByIdWithVariables(@Param("id") String id);

    Optional<EnvironmentEntity> findByOwnerAndIsActiveTrue(String owner);

    @Query("UPDATE EnvironmentsEntityCollections e SET e.isActive = false WHERE e.owner = :owner AND e.isActive = true")
    void deactivateAllForOwner(@Param("owner") String owner);
}