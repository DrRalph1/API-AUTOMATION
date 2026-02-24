package com.usg.apiAutomation.repositories.postgres.userManagement;

import com.usg.apiAutomation.entities.postgres.userManagement.UserSessionEntity;
import com.usg.apiAutomation.entities.postgres.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSessionEntity, UUID> {

    List<UserSessionEntity> findByUser(UserEntity user);

    List<UserSessionEntity> findByUserUserId(String userId);

    @Query("SELECT COUNT(s) FROM UserSessionEntity s WHERE s.user.userId = :userId AND s.isActive = true")
    long countActiveSessionsByUserId(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("UPDATE UserSessionEntity s SET s.isActive = false WHERE s.user.userId = :userId")
    void deactivateAllSessions(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserSessionEntity s WHERE s.user.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
}