package com.usg.apiAutomation.repositories.postgres.documentation;

import com.usg.apiAutomation.entities.postgres.documentation.DocumentationSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentationSettingsRepository extends JpaRepository<DocumentationSettingsEntity, String> {

    Optional<DocumentationSettingsEntity> findByUserId(String userId);

    void deleteByUserId(String userId);
}