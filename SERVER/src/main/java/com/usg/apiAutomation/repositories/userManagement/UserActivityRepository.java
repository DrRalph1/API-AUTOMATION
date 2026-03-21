package com.usg.apiAutomation.repositories.postgres.userManagement;

import com.usg.apiAutomation.entities.postgres.userManagement.UserActivityEntity;
import com.usg.apiAutomation.entities.postgres.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface UserActivityRepository extends JpaRepository<UserActivityEntity, UUID> {

    List<UserActivityEntity> findByUser(UserEntity user);

    Page<UserActivityEntity> findByUserUserId(String userId, Pageable pageable);

    List<UserActivityEntity> findByUserUserIdAndCreatedDateBetween(String userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(a) FROM UserActivityEntity a WHERE a.user.userId = :userId AND a.activityType = 'login' AND a.createdDate >= :since")
    long countLoginsSince(@Param("userId") String userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(a) FROM UserActivityEntity a WHERE a.user.userId = :userId AND a.activityType = 'api_call' AND a.createdDate >= :since")
    long countApiCallsSince(@Param("userId") String userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(a) FROM UserActivityEntity a WHERE a.user.userId = :userId AND a.isSuccess = false AND a.createdDate >= :since")
    long countFailedAttemptsSince(@Param("userId") String userId, @Param("since") LocalDateTime since);
}