package com.usg.apiAutomation.repositories.postgres.documentation;

import com.usg.apiAutomation.entities.postgres.documentation.ResponseExampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseExampleRepository extends JpaRepository<ResponseExampleEntity, String> {

    List<ResponseExampleEntity> findByEndpointId(String endpointId);

    List<ResponseExampleEntity> findByEndpointIdAndStatusCode(String endpointId, int statusCode);

    void deleteByEndpointId(String endpointId);
}
