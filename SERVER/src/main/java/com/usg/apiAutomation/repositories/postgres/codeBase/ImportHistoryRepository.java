package com.usg.apiAutomation.repositories.postgres.codeBase;

import com.usg.apiAutomation.entities.postgres.codeBase.ImportHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportHistoryRepository extends JpaRepository<ImportHistoryEntity, String> {

    List<ImportHistoryEntity> findByCollectionId(String collectionId);

    List<ImportHistoryEntity> findBySource(String source);

    List<ImportHistoryEntity> findByStatus(String status);
}