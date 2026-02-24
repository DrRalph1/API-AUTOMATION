package com.usg.apiAutomation.repositories.postgres.documentation;

import com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("HeaderRepositoryDocumentation")
public interface HeaderRepository extends JpaRepository<HeaderEntity, String> {

    List<HeaderEntity> findByEndpointId(String endpointId);

    List<HeaderEntity> findByResponseExampleId(String responseExampleId);

    List<HeaderEntity> findByMockEndpointId(String mockEndpointId);

    void deleteByEndpointId(String endpointId);
}

