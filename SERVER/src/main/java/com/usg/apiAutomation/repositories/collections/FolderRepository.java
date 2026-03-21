package com.usg.apiAutomation.repositories.postgres.collections;

import com.usg.apiAutomation.entities.postgres.collections.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<FolderEntity, String> {

    List<FolderEntity> findByCollectionId(String collectionId);

    @Query("SELECT f FROM FoldersEntityCollections f LEFT JOIN FETCH f.requests WHERE f.collection.id = :collectionId")
    List<FolderEntity> findByCollectionIdWithRequests(@Param("collectionId") String collectionId);

    void deleteByCollectionId(String collectionId);
}