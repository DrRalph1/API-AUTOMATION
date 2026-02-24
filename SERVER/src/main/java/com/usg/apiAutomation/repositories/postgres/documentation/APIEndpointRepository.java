package com.usg.apiAutomation.repositories.postgres.documentation;

import com.usg.apiAutomation.entities.postgres.documentation.APIEndpointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface APIEndpointRepository extends JpaRepository<APIEndpointEntity, String> {

    // FIXED: Correct JPA query - no join needed
    @Query("SELECT e FROM APIEndpointEntity e WHERE e.folder.id = :folderId")
    List<APIEndpointEntity> findByFolderId(@Param("folderId") String folderId);

    // Alternative using method naming convention (Spring Data JPA will implement this)
    // List<APIEndpointEntity> findByFolderId(String folderId);

    // Direct native query as fallback
    @Query(value = "SELECT * FROM tb_doc_api_endpoints WHERE folder_id = :folderId", nativeQuery = true)
    List<APIEndpointEntity> findEndpointsByFolderIdNative(@Param("folderId") String folderId);

    // Direct JPA query without join
    @Query("SELECT e FROM APIEndpointEntity e WHERE e.folder.id = :folderId")
    List<APIEndpointEntity> findEndpointsDirectlyByFolderId(@Param("folderId") String folderId);

    List<APIEndpointEntity> findByCollectionId(String collectionId);

    @Query("SELECT COUNT(e) FROM APIEndpointEntity e WHERE e.collection.id = :collectionId")
    long countByCollectionId(@Param("collectionId") String collectionId);

    @Query("SELECT e FROM APIEndpointEntity e WHERE e.collection.id = :collectionId AND e.folder IS NULL")
    List<APIEndpointEntity> findByCollectionIdAndFolderIsNull(@Param("collectionId") String collectionId);

    @Query("SELECT e FROM APIEndpointEntity e WHERE " +
            "LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(e.url) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<APIEndpointEntity> searchEndpoints(@Param("query") String query);
}