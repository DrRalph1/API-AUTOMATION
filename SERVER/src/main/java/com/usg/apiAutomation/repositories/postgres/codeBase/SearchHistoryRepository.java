package com.usg.apiAutomation.repositories.postgres.codeBase;

import com.usg.apiAutomation.entities.postgres.codeBase.SearchHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("SearchHistoryRepositoryCodeBase")
public interface SearchHistoryRepository extends JpaRepository<SearchHistoryEntity, String> {

    List<SearchHistoryEntity> findByPerformedBy(String performedBy);

    Page<SearchHistoryEntity> findByPerformedByOrderBySearchedAtDesc(String performedBy, Pageable pageable);

    @Query("SELECT s.query, COUNT(s) as frequency FROM SearchHistoryEntityCodeBase s " +
            "WHERE s.performedBy = :performedBy " +
            "GROUP BY s.query ORDER BY frequency DESC")
    List<Object[]> findPopularSearchQueries(@Param("performedBy") String performedBy, Pageable pageable);
}