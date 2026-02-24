package com.usg.apiAutomation.repositories.postgres.userManagement;

import com.usg.apiAutomation.entities.postgres.userManagement.UserActionAuditEntity;
import com.usg.apiAutomation.entities.postgres.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserActionAuditRepository extends JpaRepository<UserActionAuditEntity, UUID> {
    List<UserActionAuditEntity> findByUser(UserEntity user);
}
