package com.usg.apiAutomation.repositories.postgres.collections;

import com.usg.apiAutomation.entities.postgres.collections.CollectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("CollectionRepositoryCollections")
public interface CollectionRepository extends JpaRepository<CollectionEntity, String> {

    List<CollectionEntity> findByOwner(String owner);

    List<CollectionEntity> findByOwnerAndIsFavoriteTrue(String owner);

    @Query("SELECT c FROM CollectionEntityCollections c LEFT JOIN FETCH c.variables WHERE c.id = :id")
    Optional<CollectionEntity> findByIdWithVariables(@Param("id") String id);

    @Query("SELECT c FROM CollectionEntityCollections c LEFT JOIN FETCH c.folders f LEFT JOIN FETCH f.requests WHERE c.id = :id")
    Optional<CollectionEntity> findByIdWithFoldersAndRequests(@Param("id") String id);

    @Query("SELECT COUNT(c) FROM CollectionEntityCollections c WHERE c.owner = :owner")
    long countByOwner(@Param("owner") String owner);

    void deleteByOwnerAndId(String owner, String id);
}