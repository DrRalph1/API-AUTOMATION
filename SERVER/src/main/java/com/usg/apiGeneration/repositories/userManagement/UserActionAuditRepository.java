package com.usg.apiGeneration.repositories.userManagement;

import com.usg.apiGeneration.entities.postgres.userManagement.UserActionAuditEntity;
import com.usg.apiGeneration.entities.postgres.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserActionAuditRepository extends JpaRepository<UserActionAuditEntity, UUID> {
    List<UserActionAuditEntity> findByUser(UserEntity user);
}
