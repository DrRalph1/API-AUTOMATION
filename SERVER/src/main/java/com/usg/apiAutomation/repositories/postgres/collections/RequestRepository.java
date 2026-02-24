package com.usg.apiAutomation.repositories.postgres.collections;

import com.usg.apiAutomation.dtos.collections.RequestSummaryDTO;
import com.usg.apiAutomation.entities.postgres.collections.RequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity, String> {

    List<RequestEntity> findByCollectionId(String collectionId);

    List<RequestEntity> findByFolderId(String folderId);

    @Query("SELECT new com.usg.apiAutomation.dtos.collections.RequestSummaryDTO(" +
            "r.id, r.name, r.method, r.url, r.description, r.authType, " +
            "r.body, r.tests, r.preRequestScript, r.isSaved) " +
            "FROM RequestsEntityCollections r WHERE r.folder.id = :folderId")
    List<RequestSummaryDTO> findRequestSummariesByFolderId(@Param("folderId") String folderId);

    int countByFolderId(String folderId);

    // Keep this for backward compatibility if needed, but avoid using it
    @Query("SELECT r FROM RequestsEntityCollections r " +
            "LEFT JOIN FETCH r.headers h " +
            "LEFT JOIN FETCH r.params p " +
            "LEFT JOIN FETCH r.authConfig ac " +
            "WHERE r.id = :requestId")
    Optional<RequestEntity> findByIdWithDetails(@Param("requestId") String requestId);
}