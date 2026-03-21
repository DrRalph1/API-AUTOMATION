package com.usg.apiAutomation.repositories.postgres.userManagement;

import com.usg.apiAutomation.entities.postgres.userManagement.UserApiKeyEntity;
import com.usg.apiAutomation.entities.postgres.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserApiKeyRepository extends JpaRepository<UserApiKeyEntity, UUID> {

    List<UserApiKeyEntity> findByUser(UserEntity user);

    List<UserApiKeyEntity> findByUserUserId(String userId);

    Optional<UserApiKeyEntity> findByApiKey(String apiKey);

    @Query("SELECT COUNT(k) FROM UserApiKeyEntity k WHERE k.user.userId = :userId AND k.isActive = true")
    long countActiveKeysByUserId(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserApiKeyEntity k WHERE k.user.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
}