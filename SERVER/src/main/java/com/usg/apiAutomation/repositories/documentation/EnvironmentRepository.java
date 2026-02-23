package com.usg.apiAutomation.repositories.documentation;

import com.usg.apiAutomation.entities.documentation.EnvironmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("EnvironmentRepositoryDocumentation")
public interface EnvironmentRepository extends JpaRepository<EnvironmentEntity, String> {

    List<EnvironmentEntity> findByCreatedBy(String createdBy);

    Optional<EnvironmentEntity> findByNameAndCreatedBy(String name, String createdBy);

    @Query("SELECT e FROM EnvironmentRepositoryEntityDocumentation e WHERE e.active = true AND e.createdBy = :createdBy")
    List<EnvironmentEntity> findActiveEnvironmentsByUser(@Param("createdBy") String createdBy);

    @Query("SELECT e FROM EnvironmentRepositoryEntityDocumentation e WHERE e.createdBy = :createdBy ORDER BY e.lastUsed DESC")
    List<EnvironmentEntity> findRecentlyUsedByUser(@Param("createdBy") String createdBy);

    boolean existsByNameAndCreatedBy(String name, String createdBy);
}