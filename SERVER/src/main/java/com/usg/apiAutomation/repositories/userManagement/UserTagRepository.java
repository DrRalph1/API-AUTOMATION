package com.usg.apiAutomation.repositories.postgres.userManagement;

import com.usg.apiAutomation.entities.postgres.userManagement.UserTagEntity;
import com.usg.apiAutomation.entities.postgres.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface UserTagRepository extends JpaRepository<UserTagEntity, UUID> {

    List<UserTagEntity> findByUser(UserEntity user);

    List<UserTagEntity> findByUserUserId(String userId);

    @Query("SELECT DISTINCT t.tagName FROM UserTagEntity t ORDER BY t.tagName")
    List<String> findAllDistinctTagNames();

    @Modifying
    @Transactional
    @Query("DELETE FROM UserTagEntity t WHERE t.user.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
}