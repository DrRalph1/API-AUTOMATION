package com.usg.apiAutomation.repositories.codeBase;

import com.usg.apiAutomation.entities.codeBase.CollectionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<CollectionEntity, String> {

    List<CollectionEntity> findByOwnerOrderByUpdatedAtDesc(String owner);

    List<CollectionEntity> findByOwnerAndIsFavoriteTrueOrderByUpdatedAtDesc(String owner);

    @Query("SELECT c FROM CollectionEntity c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<CollectionEntity> searchCollections(@Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(r) FROM RequestEntity r WHERE r.collection.id = :collectionId")
    int countRequestsByCollectionId(@Param("collectionId") String collectionId);

    @Query("SELECT COUNT(f) FROM FolderEntity f WHERE f.collection.id = :collectionId")
    int countFoldersByCollectionId(@Param("collectionId") String collectionId);

    boolean existsByNameAndOwner(String name, String owner);

    // FIXED: Either use this approach (with proper naming convention)
    List<CollectionEntity> findByUpdatedAtAfter(LocalDateTime since);

    // OR this approach (keep the name but add @Query)
    // @Query("SELECT c FROM CollectionEntity c WHERE c.updatedAt > :since")
    // List<CollectionEntity> findRecentlyUpdated(@Param("since") LocalDateTime since);

    @Query("SELECT c.owner, COUNT(c) FROM CollectionEntity c GROUP BY c.owner")
    List<Object[]> getCollectionStatsByOwner();

    @Query("SELECT c, COUNT(r) as requestCount FROM CollectionEntity c " +
            "LEFT JOIN RequestEntity r ON r.collection.id = c.id GROUP BY c ORDER BY requestCount DESC")
    List<Object[]> findCollectionsWithMostRequests(Pageable pageable);

    List<CollectionEntity> findByOwnerAndIsExpandedTrue(String owner);

    List<CollectionEntity> findByIsFavoriteTrue();

    List<CollectionEntity> findByVersion(String version);

    @Query("SELECT c, COUNT(r) as requestCount, COUNT(f) as folderCount " +
            "FROM CollectionEntity c " +
            "LEFT JOIN RequestEntity r ON r.collection.id = c.id " +
            "LEFT JOIN FolderEntity f ON f.collection.id = c.id " +
            "WHERE c.id = :collectionId " +
            "GROUP BY c")
    Optional<Object[]> findCollectionWithCounts(@Param("collectionId") String collectionId);

    @Query("SELECT DISTINCT c FROM CollectionEntity c " +
            "JOIN RequestEntity r ON r.collection.id = c.id " +
            "WHERE :tag MEMBER OF r.tags")
    List<CollectionEntity> findByRequestTag(@Param("tag") String tag);

    long countByOwner(String owner);
}