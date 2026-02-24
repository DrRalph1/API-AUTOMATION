package com.usg.apiAutomation.repositories.postgres.codeBase;

import com.usg.apiAutomation.entities.postgres.codeBase.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("FolderRepositoryCodeBase")
public interface FolderRepository extends JpaRepository<FolderEntity, String> {

    List<FolderEntity> findByCollectionId(String collectionId);

    // CORRECTED: Count requests by folder ID by querying RequestEntity directly
    @Query("SELECT COUNT(r) FROM RequestEntityCodeBase r WHERE r.folder.id = :folderId")
    long countRequestsByFolderId(@Param("folderId") String folderId);

    boolean existsByNameAndCollectionId(String name, String collectionId);

    @Query("SELECT f FROM FolderEntityCodeBase f WHERE f.collection.id = :collectionId AND f.isExpanded = true")
    List<FolderEntity> findExpandedFoldersByCollectionId(@Param("collectionId") String collectionId);

    // Optional: Find folders with their request counts
    @Query("SELECT f, COUNT(r) FROM FolderEntityCodeBase f LEFT JOIN RequestEntityCodeBase r ON r.folder.id = f.id WHERE f.collection.id = :collectionId GROUP BY f")
    List<Object[]> findFoldersWithRequestCount(@Param("collectionId") String collectionId);

    // Delete all folders in a collection
    void deleteByCollectionId(String collectionId);
}