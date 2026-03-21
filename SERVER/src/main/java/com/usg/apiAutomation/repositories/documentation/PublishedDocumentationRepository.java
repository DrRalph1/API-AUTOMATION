package com.usg.apiAutomation.repositories.postgres.documentation;

import com.usg.apiAutomation.entities.postgres.documentation.PublishedDocumentationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PublishedDocumentationRepository extends JpaRepository<PublishedDocumentationEntity, String> {

    List<PublishedDocumentationEntity> findByCollectionId(String collectionId);

    Optional<PublishedDocumentationEntity> findByPublishedUrl(String publishedUrl);

    @Query("SELECT p FROM PublishedDocumentationEntity p WHERE p.collection.id = :collectionId AND p.isActive = true")
    Optional<PublishedDocumentationEntity> findActiveByCollectionId(@Param("collectionId") String collectionId);

    List<PublishedDocumentationEntity> findByPublishedBy(String publishedBy);

    List<PublishedDocumentationEntity> findByVisibility(String visibility);
}