package com.usg.autoAPIGenerator.repositories.documentation;

import com.usg.autoAPIGenerator.entities.postgres.documentation.ParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("ParameterRepositoryDocumentation")
public interface ParameterRepository extends JpaRepository<ParameterEntity, String> {

    List<ParameterEntity> findByEndpointId(String endpointId);


    void deleteByEndpointId(String endpointId);
}
