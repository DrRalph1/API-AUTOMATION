package com.usg.apiGeneration.repositories.apiGenerationEngine;

import com.usg.apiGeneration.entities.postgres.apiGenerationEngine.ApiTestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiTestRepository extends JpaRepository<ApiTestEntity, String> {

    List<ApiTestEntity> findByGeneratedApiIdOrderByExecutedAtDesc(String apiId);

    List<ApiTestEntity> findByGeneratedApiIdAndStatus(String apiId, String status);
}