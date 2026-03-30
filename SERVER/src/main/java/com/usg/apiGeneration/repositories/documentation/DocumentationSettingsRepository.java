package com.usg.apiGeneration.repositories.documentation;

import com.usg.apiGeneration.entities.postgres.documentation.DocumentationSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentationSettingsRepository extends JpaRepository<DocumentationSettingsEntity, String> {

    Optional<DocumentationSettingsEntity> findByUserId(String userId);

    void deleteByUserId(String userId);
}