package com.usg.apiAutomation.repositories.documentation;

import com.usg.apiAutomation.entities.documentation.ParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Parameter;
import java.util.List;

@Repository("ParameterRepositoryDocumentation")
public interface ParameterRepository extends JpaRepository<ParameterEntity, String> {

    List<ParameterEntity> findByEndpointId(String endpointId);


    void deleteByEndpointId(String endpointId);
}
