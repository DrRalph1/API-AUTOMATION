package com.usg.apiAutomation.repositories.postgres;

import com.usg.apiAutomation.entities.postgres.UserOtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<UserOtpEntity, Long> {
    Optional<UserOtpEntity> findTopByUserIdAndVerifiedFalseOrderByCreatedAtDesc(String userId);
}
