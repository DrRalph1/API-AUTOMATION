package com.usg.apiGeneration.repositories.documentation;

import com.usg.apiGeneration.entities.postgres.documentation.ChangelogEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangelogRepository extends JpaRepository<ChangelogEntryEntity, String> {

    List<ChangelogEntryEntity> findByCollectionIdOrderByDateDesc(String collectionId);

    List<ChangelogEntryEntity> findByEndpointIdOrderByDateDesc(String endpointId);

    @Query("SELECT c FROM ChangelogEntryEntity c WHERE c.collection.id = :collectionId AND c.version = :version")
    List<ChangelogEntryEntity> findByCollectionIdAndVersion(@Param("collectionId") String collectionId,
                                                            @Param("version") String version);

    @Query("SELECT DISTINCT c.version FROM ChangelogEntryEntity c WHERE c.collection.id = :collectionId")
    List<String> findDistinctVersionsByCollectionId(@Param("collectionId") String collectionId);

    // Add this method to find changelog entries by endpoint ID
    List<ChangelogEntryEntity> findByEndpointId(String endpointId);

    // You might also want this method to find by collection ID
    List<ChangelogEntryEntity> findByCollectionId(String collectionId);


    // Optional: delete all changelog entries for an endpoint
    void deleteByEndpointId(String endpointId);
}