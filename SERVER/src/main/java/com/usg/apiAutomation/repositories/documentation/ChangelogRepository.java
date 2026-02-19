package com.usg.apiAutomation.repositories.documentation;

import com.usg.apiAutomation.entities.documentation.ChangelogEntryEntity;
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
}