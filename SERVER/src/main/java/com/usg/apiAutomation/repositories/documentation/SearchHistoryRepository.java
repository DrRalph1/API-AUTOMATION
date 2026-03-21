package com.usg.apiAutomation.repositories.postgres.documentation;

import com.usg.apiAutomation.entities.postgres.documentation.SearchHistoryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository("SearchHistoryRepositoryDocumentation")
public interface SearchHistoryRepository extends JpaRepository<SearchHistoryEntity, String> {

    List<SearchHistoryEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT s.query, COUNT(s) as frequency FROM SearchHistoryEntityDocumentation s " +
            "WHERE s.userId = :userId AND s.createdAt > :since " +
            "GROUP BY s.query ORDER BY frequency DESC")
    List<Object[]> findTopSearchesByUser(@Param("userId") String userId,
                                         @Param("since") LocalDateTime since,
                                         Pageable pageable);

    @Query("SELECT s.query, COUNT(s) as frequency FROM SearchHistoryEntityDocumentation s " +
            "WHERE s.createdAt > :since " +
            "GROUP BY s.query ORDER BY frequency DESC")
    List<Object[]> findGlobalTopSearches(@Param("since") LocalDateTime since, Pageable pageable);

    void deleteByUserId(String userId);

    void deleteByCreatedAtBefore(LocalDateTime date);
}