package com.usg.apiGeneration.repositories.codeBase;

import com.usg.apiGeneration.entities.postgres.codeBase.LanguageConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LanguageConfigRepository extends JpaRepository<LanguageConfigEntity, String> {

    Optional<LanguageConfigEntity> findByLanguage(String language);

    List<LanguageConfigEntity> findByIsActiveTrue();

    boolean existsByLanguage(String language);
}