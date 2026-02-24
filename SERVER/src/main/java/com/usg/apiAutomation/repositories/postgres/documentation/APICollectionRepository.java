package com.usg.apiAutomation.repositories.postgres.documentation;

import com.usg.apiAutomation.entities.postgres.documentation.APICollectionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface APICollectionRepository extends JpaRepository<APICollectionEntity, String> {

    List<APICollectionEntity> findByOwner(String owner);

    List<APICollectionEntity> findByIsFavoriteTrue();

    List<APICollectionEntity> findByType(String type);

    Page<APICollectionEntity> findByOwner(String owner, Pageable pageable);

    @Query("SELECT c FROM APICollectionEntity c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<APICollectionEntity> searchCollections(@Param("searchTerm") String searchTerm);

    @Query("SELECT c FROM APICollectionEntity c WHERE c.owner = :owner AND c.isFavorite = true")
    List<APICollectionEntity> findFavoriteCollectionsByOwner(@Param("owner") String owner);

    @Query("SELECT COUNT(e) FROM APIEndpointEntity e WHERE e.collection.id = :collectionId")
    int countEndpointsByCollectionId(@Param("collectionId") String collectionId);

    Optional<APICollectionEntity> findByNameAndVersion(String name, String version);

    @Query("SELECT c FROM APICollectionEntity c ORDER BY c.updatedAt DESC")
    List<APICollectionEntity> findRecentlyUpdated(Pageable pageable);
}