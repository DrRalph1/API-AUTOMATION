package com.usg.apiAutomation.repositories.postgres.userManagement;

import com.usg.apiAutomation.entities.postgres.userManagement.UserDeviceEntity;
import com.usg.apiAutomation.entities.postgres.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface UserDeviceRepository extends JpaRepository<UserDeviceEntity, UUID> {

    List<UserDeviceEntity> findByUser(UserEntity user);

    List<UserDeviceEntity> findByUserUserId(String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserDeviceEntity d WHERE d.user.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(d) FROM UserDeviceEntity d WHERE d.user.userId = :userId")
    long countByUserId(@Param("userId") String userId);
}