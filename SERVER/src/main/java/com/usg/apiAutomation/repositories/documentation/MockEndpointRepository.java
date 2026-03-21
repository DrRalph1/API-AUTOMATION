package com.usg.apiAutomation.repositories.documentation;

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

    // Add this method to return all mock endpoints for a source endpoint (handles duplicates)
    List<MockEndpointEntity> findAllBySourceEndpointId(String sourceEndpointId);
}