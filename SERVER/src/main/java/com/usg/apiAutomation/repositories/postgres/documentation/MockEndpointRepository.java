package com.usg.apiAutomation.repositories.postgres.documentation;

import com.usg.apiAutomation.entities.postgres.documentation.MockEndpointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockEndpointRepository extends JpaRepository<MockEndpointEntity, String> {

    List<MockEndpointEntity> findByMockServerId(String mockServerId);

    List<MockEndpointEntity> findByMockServerIdAndIsEnabledTrue(String mockServerId);

    List<MockEndpointEntity> findBySourceEndpointId(String sourceEndpointId);

    List<MockEndpointEntity> findByMethod(String method);

    boolean existsByPathAndMethodAndMockServerId(String path, String method, String mockServerId);
}