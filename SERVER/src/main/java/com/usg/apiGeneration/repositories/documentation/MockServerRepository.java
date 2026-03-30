package com.usg.apiGeneration.repositories.documentation;

import com.usg.apiGeneration.entities.postgres.documentation.MockServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MockServerRepository extends JpaRepository<MockServerEntity, String> {

    Optional<MockServerEntity> findByCollectionId(String collectionId);

    List<MockServerEntity> findByIsActiveTrue();

    @Query("SELECT m FROM MockServerEntity m WHERE m.isActive = true AND m.createdBy = :createdBy")
    List<MockServerEntity> findActiveByCreatedBy(@Param("createdBy") String createdBy);

    @Modifying
    @Transactional
    @Query("UPDATE MockServerEntity m SET m.isActive = false WHERE m.expiresAt < :currentDate")
    int deactivateExpiredServers(@Param("currentDate") LocalDateTime currentDate);

    // Add this method to return all mock servers for a collection (handles duplicates)
    List<MockServerEntity> findAllByCollectionId(String collectionId);

    @Query("SELECT m FROM MockServerEntity m LEFT JOIN FETCH m.collection WHERE m.collection.id = :collectionId")
    List<MockServerEntity> findByCollectionIdWithoutCollections(@Param("collectionId") String collectionId);

    @Query("SELECT COUNT(m) FROM MockServerEntity m WHERE m.collection.id = :collectionId")
    long countByCollectionId(@Param("collectionId") String collectionId);
}