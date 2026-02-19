package com.usg.apiAutomation.repositories.documentation;

import com.usg.apiAutomation.entities.documentation.CodeExampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeExampleRepository extends JpaRepository<CodeExampleEntity, String> {

    List<CodeExampleEntity> findByEndpointId(String endpointId);

    Optional<CodeExampleEntity> findByEndpointIdAndLanguage(String endpointId, String language);

    List<CodeExampleEntity> findByLanguage(String language);

    List<CodeExampleEntity> findByEndpointIdAndIsDefaultTrue(String endpointId);

    void deleteByEndpointId(String endpointId);
}