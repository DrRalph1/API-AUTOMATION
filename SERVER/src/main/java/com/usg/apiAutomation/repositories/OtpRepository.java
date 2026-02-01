package com.usg.apiAutomation.repositories;

import com.usg.apiAutomation.entities.UserOtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<UserOtpEntity, Long> {
    Optional<UserOtpEntity> findTopByUserIdAndVerifiedFalseOrderByCreatedAtDesc(String userId);
}
