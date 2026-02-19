package com.usg.apiAutomation.repositories.codeBase;

import com.usg.apiAutomation.entities.codeBase.ParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository("ParameterRepositoryCodeBase")
public interface ParameterRepository extends JpaRepository<ParameterEntity, String> {

    // FIXED: Use @Query with proper path navigation through requestEntity
    @Query("SELECT p FROM ParameterEntityCodeBase p WHERE p.requestEntity.id = :requestId AND p.type = :type")
    List<ParameterEntity> findByRequestIdAndType(@Param("requestId") String requestId, @Param("type") String type);

    // FIXED: Use @Modifying and @Query for delete operation
    @Modifying
    @Transactional
    @Query("DELETE FROM ParameterEntityCodeBase p WHERE p.requestEntity.id = :requestId")
    void deleteByRequestId(@Param("requestId") String requestId);

    // Additional useful methods you might want to add:

    List<ParameterEntity> findByRequestEntityId(String requestEntityId);

    List<ParameterEntity> findByRequestEntityIdAndRequiredTrue(String requestEntityId);

    @Query("SELECT p FROM ParameterEntityCodeBase p WHERE p.requestEntity.id = :requestId ORDER BY p.name ASC")
    List<ParameterEntity> findByRequestIdOrdered(@Param("requestId") String requestId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ParameterEntityCodeBase p WHERE p.requestEntity.id = :requestId AND p.type = :type")
    void deleteByRequestIdAndType(@Param("requestId") String requestId, @Param("type") String type);

    @Query("SELECT COUNT(p) FROM ParameterEntityCodeBase p WHERE p.requestEntity.id = :requestId")
    long countByRequestId(@Param("requestId") String requestId);

    boolean existsByRequestEntityIdAndName(String requestEntityId, String name);
}