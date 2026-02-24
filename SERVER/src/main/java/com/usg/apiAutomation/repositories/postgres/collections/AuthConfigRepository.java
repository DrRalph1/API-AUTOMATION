package com.usg.apiAutomation.repositories.postgres.collections;

import com.usg.apiAutomation.dtos.collections.AuthConfigDTO;
import com.usg.apiAutomation.entities.postgres.collections.AuthConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("AuthConfigsRepositoryCollections")
public interface AuthConfigRepository extends JpaRepository<AuthConfigEntity, String> {

    Optional<AuthConfigEntity> findByRequestId(String requestId);

    @Query("SELECT new com.usg.apiAutomation.dtos.collections.AuthConfigDTO(" +
            "a.type, a.token, a.tokenType, a.username, a.password, a.key, a.value, a.addTo) " +
            "FROM AuthConfigsEntityCollections a WHERE a.request.id = :requestId")
    Optional<AuthConfigDTO> findAuthConfigDTOByRequestId(@Param("requestId") String requestId);

    void deleteByRequestId(String requestId);
}