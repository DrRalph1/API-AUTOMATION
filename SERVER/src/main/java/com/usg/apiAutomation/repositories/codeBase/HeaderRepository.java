package com.usg.apiAutomation.repositories.codeBase;

import com.usg.apiAutomation.entities.codeBase.HeaderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface HeaderRepository extends JpaRepository<HeaderEntity, String> {

    // FIXED: Use @Query with proper path navigation through requestEntity
    @Query("SELECT h FROM HeaderEntity h WHERE h.requestEntity.id = :requestId")
    List<HeaderEntity> findByRequestId(@Param("requestId") String requestId);

    // Alternative using method naming convention (works with the relationship field name)
    List<HeaderEntity> findByRequestEntityId(String requestEntityId);

    // FIXED: Delete operation needs @Modifying and @Query
    @Modifying
    @Transactional
    @Query("DELETE FROM HeaderEntity h WHERE h.requestEntity.id = :requestId")
    void deleteByRequestId(@Param("requestId") String requestId);

    // Additional useful methods you might want:

    @Query("SELECT h FROM HeaderEntity h WHERE h.requestEntity.id = :requestId AND h.key = :key")
    Optional<HeaderEntity> findByRequestIdAndKey(@Param("requestId") String requestId, @Param("key") String key);

    @Query("SELECT h FROM HeaderEntity h WHERE h.requestEntity.id = :requestId AND h.required = true")
    List<HeaderEntity> findRequiredByRequestId(@Param("requestId") String requestId);

    @Query("SELECT h FROM HeaderEntity h WHERE h.requestEntity.id = :requestId ORDER BY h.key ASC")
    List<HeaderEntity> findByRequestIdOrdered(@Param("requestId") String requestId);

    @Modifying
    @Transactional
    @Query("DELETE FROM HeaderEntity h WHERE h.requestEntity.id = :requestId AND h.key = :key")
    void deleteByRequestIdAndKey(@Param("requestId") String requestId, @Param("key") String key);

    @Query("SELECT COUNT(h) FROM HeaderEntity h WHERE h.requestEntity.id = :requestId")
    long countByRequestId(@Param("requestId") String requestId);

    boolean existsByRequestEntityIdAndKey(String requestEntityId, String key);
}