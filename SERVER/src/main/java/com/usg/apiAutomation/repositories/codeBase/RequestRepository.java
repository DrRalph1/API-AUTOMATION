package com.usg.apiAutomation.repositories.codeBase;

import com.usg.apiAutomation.entities.codeBase.RequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface RequestRepository extends JpaRepository<RequestEntity, String> {

    List<RequestEntity> findByCollectionId(String collectionId);

    Page<RequestEntity> findByCollectionId(String collectionId, Pageable pageable);

    List<RequestEntity> findByCollectionIdAndFolderId(String collectionId, String folderId);

    @Query("SELECT r FROM RequestEntity r WHERE r.collection.id = :collectionId AND r.folder IS NULL")
    List<RequestEntity> findRootRequestsByCollectionId(@Param("collectionId") String collectionId);

    List<RequestEntity> findByFolderId(String folderId);

    Page<RequestEntity> findByFolderId(String folderId, Pageable pageable);

    List<RequestEntity> findByMethod(String method);

    @Query("SELECT r FROM RequestEntity r WHERE :tag MEMBER OF r.tags")
    List<RequestEntity> findByTag(@Param("tag") String tag);

    @Query("SELECT r FROM RequestEntity r WHERE " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.method) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.url) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<RequestEntity> searchRequests(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT r FROM RequestEntity r WHERE " +
            "(:collectionId IS NULL OR r.collection.id = :collectionId) AND " +
            "(:folderId IS NULL OR r.folder.id = :folderId) AND " +
            "(:method IS NULL OR r.method = :method) AND " +
            "(:tag IS NULL OR :tag MEMBER OF r.tags)")
    List<RequestEntity> findRequestsByFilters(
            @Param("collectionId") String collectionId,
            @Param("folderId") String folderId,
            @Param("method") String method,
            @Param("tag") String tag);

    boolean existsByNameAndCollectionId(String name, String collectionId);

    long countByCollectionId(String collectionId);

    long countByFolderId(String folderId);

    List<RequestEntity> findByUpdatedAtAfterOrderByUpdatedAtDesc(LocalDateTime since);

    @Query("SELECT r FROM RequestEntity r WHERE r.implementationsCount > 0")
    List<RequestEntity> findRequestsWithImplementations();

    @Query("SELECT r FROM RequestEntity r WHERE r.implementationsCount = 0 OR r.implementationsCount IS NULL")
    List<RequestEntity> findRequestsWithoutImplementations();

    List<RequestEntity> findByIsFavoriteTrue();

    List<RequestEntity> findByCollectionIdAndIsFavoriteTrue(String collectionId);

    @Query("SELECT r FROM RequestEntity r LEFT JOIN FETCH r.implementations WHERE r.id = :id")
    Optional<RequestEntity> findByIdWithImplementations(@Param("id") String id);

    @Query("SELECT r FROM RequestEntity r LEFT JOIN FETCH r.tags LEFT JOIN FETCH r.implementations WHERE r.id = :id")
    Optional<RequestEntity> findByIdWithAllDetails(@Param("id") String id);

    @Query("SELECT DISTINCT r FROM RequestEntity r JOIN r.implementations i WHERE i.language = :language")
    List<RequestEntity> findByImplementationLanguage(@Param("language") String language);

    @Query("SELECT r.method, COUNT(r) FROM RequestEntity r GROUP BY r.method")
    List<Object[]> getRequestStatsByMethod();

    @Query("SELECT c.name, COUNT(r) FROM RequestEntity r JOIN r.collection c GROUP BY c.id, c.name")
    List<Object[]> getRequestStatsByCollection();

    List<RequestEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT r FROM RequestEntity r WHERE r.implementationsCount > 0 ORDER BY r.updatedAt DESC")
    List<RequestEntity> findMostRecentlyUsed(Pageable pageable);

    @Query("SELECT r FROM RequestEntity r ORDER BY r.implementationsCount DESC")
    List<RequestEntity> findMostPopularRequests(Pageable pageable);

    List<RequestEntity> findByUrlContaining(String urlPattern);

    @Modifying
    @Transactional
    @Query("UPDATE RequestEntity r SET r.isFavorite = :isFavorite WHERE r.id IN :ids")
    int batchUpdateFavoriteStatus(@Param("ids") List<String> ids, @Param("isFavorite") boolean isFavorite);

    @Modifying
    @Transactional
    void deleteByCollectionId(String collectionId);

    @Modifying
    @Transactional
    void deleteByFolderId(String folderId);
}