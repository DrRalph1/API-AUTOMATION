package com.usg.apiAutomation.repositories.documentation;

import com.usg.apiAutomation.entities.documentation.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("FolderRepositoryDocumentation")
public interface FolderRepository extends JpaRepository<FolderEntity, String> {

    List<FolderEntity> findByCollectionId(String collectionId);

    List<FolderEntity> findByParentFolderId(String parentFolderId);

    @Query("SELECT f FROM FolderEntity f WHERE f.collection.id = :collectionId AND f.parentFolder IS NULL")
    List<FolderEntity> findRootFoldersByCollectionId(@Param("collectionId") String collectionId);

    @Query("SELECT COUNT(e) FROM APIEndpointEntity e WHERE e.folder.id = :folderId")
    int countEndpointsInFolder(@Param("folderId") String folderId);

    boolean existsByNameAndCollectionId(String name, String collectionId);
}