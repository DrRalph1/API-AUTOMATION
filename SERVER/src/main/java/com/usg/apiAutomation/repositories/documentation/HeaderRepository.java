package com.usg.apiAutomation.repositories.documentation;

import com.usg.apiAutomation.entities.documentation.HeaderEntity;
import com.usg.apiAutomation.entities.documentation.ParameterEntity;
import com.usg.apiAutomation.entities.documentation.ResponseExampleEntity;
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

