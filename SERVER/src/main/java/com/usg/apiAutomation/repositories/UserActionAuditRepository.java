package com.usg.apiAutomation.repositories;

import com.usg.apiAutomation.entities.UserActionAuditEntity;
import com.usg.apiAutomation.entities.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserActionAuditRepository extends JpaRepository<UserActionAuditEntity, UUID> {
    List<UserActionAuditEntity> findByUser(AppUserEntity user);
}
